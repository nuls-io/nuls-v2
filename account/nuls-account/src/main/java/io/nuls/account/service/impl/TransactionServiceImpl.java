/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.account.service.impl;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.MultiSignTransactionResultDto;
import io.nuls.account.rpc.call.NetworkCall;
import io.nuls.account.rpc.call.TransactionCmdCall;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.TxUtil;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.tools.log.Log;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: qinyifeng
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private AccountService accountService;
    @Autowired
    private ChainManager chainManager;

    @Autowired
    private MultiSignAccountService multiSignAccountService;

    @Override
    public List<Transaction> accountTxValidate(int chainId, List<Transaction> txList) {
        Set<Transaction> result = new HashSet<>();
        if (null == txList || txList.isEmpty()) {
            return new ArrayList<>(result);
        }
        Map<String, Transaction> aliasNamesMap = new HashMap<>();
        Map<String, Transaction> accountAddressMap = new HashMap<>();
        try {
            for (Transaction transaction : txList) {
                Log.debug("start=======type: "+transaction.getType()+"===hash: "+transaction.getHash());
                if (transaction.getType() == AccountConstant.TX_TYPE_ACCOUNT_ALIAS) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(transaction.getTxData()));
                    String address = AddressTool.getStringAddressByBytes(alias.getAddress());
                    //check alias
                    Transaction tmp = aliasNamesMap.get(alias.getAlias());
                    // the alias is already exist
                    if (tmp != null) {
                        result.add(transaction);
                        result.add(tmp);
                        continue;
                    } else {
                        aliasNamesMap.put(alias.getAlias(), transaction);
                    }
                    //check address
                    tmp = accountAddressMap.get(address);
                    // the address is already exist
                    if (tmp != null) {
                        result.add(transaction);
                        result.add(tmp);
                        continue;
                    } else {
                        accountAddressMap.put(address, transaction);
                    }
                }
                Log.debug("end=======");
            }
        } catch (Exception e) {
            Log.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION, e);
        }
        return new ArrayList<>(result);
    }

    @Override
    public String transfer(int chainId, List<CoinDto> fromList, List<CoinDto> toList, String remark) {
        Transaction tx = this.assemblyTransaction(chainId, fromList, toList, remark);
        return tx.getHash().getDigestHex();
    }

    @Override
    public Transaction transferByAlias(int chainId, CoinDto from, CoinDto to, String remark) {
        List<CoinDto> fromList = Arrays.asList(from);
        List<CoinDto> toList = Arrays.asList(to);
        Transaction tx = this.assemblyTransaction(chainId, fromList, toList, remark);
        return tx;
    }

    @Override
    public MultiSignTransactionResultDto createMultiSignTransfer(int chainId,int assetsId, Account account, String password, MultiSigAccount multiSigAccount, String toAddress, BigInteger amount, String remark)
            throws NulsException, IOException {
        //create transaction
        Transaction transaction = new Transaction(AccountConstant.TX_TYPE_TRANSFER);
        transaction.setTime(NetworkCall.getCurrentTimeMillis());
        transaction.setRemark(StringUtils.bytes(remark));
        //build coin data
        //buildMultiSignTransactionCoinData(transaction, chainId,assetsId, multiSigAccount, toAddress, amount);
        CoinDto from = new CoinDto(multiSigAccount.getAddress().getBase58(),chainId,assetsId,amount,null);
        CoinDto to = new CoinDto(toAddress,chainId,assetsId,amount,null);
        assemblyCoinData(transaction, chainId, List.of(from), List.of(to));
        //sign
        TransactionSignature transactionSignature = buildMultiSignTransactionSignature(transaction, multiSigAccount, account, password);
        //缓存当前交易hash
        this.cacheTxHash(transaction);
        //process transaction
        boolean isBroadcasted = txMutilProcessing(multiSigAccount, transaction, transactionSignature);
        MultiSignTransactionResultDto multiSignTransactionResultDto = new MultiSignTransactionResultDto();
        multiSignTransactionResultDto.setBroadcasted(isBroadcasted);
        multiSignTransactionResultDto.setTransaction(transaction);
        return multiSignTransactionResultDto;
    }

    @Override
    public MultiSignTransactionResultDto signMultiSignTransaction(int chainId, Account account, String password, String txHex)
            throws NulsException, IOException {
        //create transaction
        Transaction transaction = new Transaction();
        transaction.parse(new NulsByteBuffer(HexUtil.decode(txHex)));

        CoinData coinData = new CoinData();
        coinData.parse(new NulsByteBuffer(transaction.getCoinData()));
        List<CoinFrom> list = coinData.getFrom();
        if (list == null || list.size() != 1) {
            throw new NulsRuntimeException(AccountErrorCode.TX_NOT_EFFECTIVE);
        }
        byte[] address = list.get(0).getAddress();
        MultiSigAccount multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(chainId, AddressTool.getStringAddressByBytes(address));
        if (multiSigAccount == null) {
            throw new NulsRuntimeException(AccountErrorCode.TX_NOT_EFFECTIVE);
        }
        //验证签名地址账户是否属于多签账户
        if (!AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), account.getPubKey())) {
            throw new NulsRuntimeException(AccountErrorCode.SIGN_ADDRESS_NOT_MATCH);
        }
        TransactionSignature transactionSignature = buildMultiSignTransactionSignature(transaction, null, account, password);
        //process transaction
        txMutilProcessing(multiSigAccount, transaction, transactionSignature);
        boolean isBroadcasted = txMutilProcessing(multiSigAccount, transaction, transactionSignature);
        MultiSignTransactionResultDto multiSignTransactionResultDto = new MultiSignTransactionResultDto();
        multiSignTransactionResultDto.setBroadcasted(isBroadcasted);
        multiSignTransactionResultDto.setTransaction(transaction);
        return multiSignTransactionResultDto;
    }

    @Override
    public MultiSignTransactionResultDto createSetAliasMultiSignTransaction(int chainId, Account account, String password, MultiSigAccount multiSigAccount, String toAddress, String aliasName, String remark)
            throws NulsException, IOException {
        //create transaction
        AliasTransaction transaction = new AliasTransaction();
        transaction.setTime(NetworkCall.getCurrentTimeMillis());
        transaction.setRemark(StringUtils.bytes(remark));
        Alias alias = new Alias(multiSigAccount.getAddress().getAddressBytes(), aliasName);
        transaction.setTxData(alias.serialize());
        //build coin data
        buildMultiSignTransactionCoinData(transaction, chainId,-1, multiSigAccount, toAddress, BigInteger.ONE);
        //sign
        TransactionSignature transactionSignature = buildMultiSignTransactionSignature(transaction, multiSigAccount, account, password);
        //缓存当前交易hash
        this.cacheTxHash(transaction);
        //process transaction
        boolean isBroadcasted = txMutilProcessing(multiSigAccount, transaction, transactionSignature);
        MultiSignTransactionResultDto multiSignTransactionResultDto = new MultiSignTransactionResultDto();
        multiSignTransactionResultDto.setBroadcasted(isBroadcasted);
        multiSignTransactionResultDto.setTransaction(transaction);
        return multiSignTransactionResultDto;
    }

    private Transaction buildMultiSignTransactionCoinData(Transaction transaction, int chainId, int assetsId, MultiSigAccount multiSigAccount, String toAddress, BigInteger amount) throws IOException {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (assetsId == -1) {
            assetsId = chain.getConfig().getAssetsId();
        }
        //查询账本获取nonce值
        byte[] nonce = TxUtil.getNonce(chainId, chainId, assetsId, multiSigAccount.getAddress().getAddressBytes());
        CoinFrom coinFrom = new CoinFrom(multiSigAccount.getAddress().getAddressBytes(), chainId, assetsId, amount, nonce, AccountConstant.NORMAL_TX_LOCKED);
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(toAddress), chainId, assetsId, amount);
        int txSize = transaction.size() + coinFrom.size() + coinTo.size() + ((int) multiSigAccount.getM()) * P2PHKSignature.SERIALIZE_LENGTH;
        //计算手续费
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        //总费用为
        BigInteger totalAmount = amount.add(fee);
        coinFrom.setAmount(totalAmount);
        //检查余额是否充足
        BigInteger mainAsset = TxUtil.getBalance(chainId, chainId, assetsId, coinFrom.getAddress());
        //余额不足
        if (BigIntegerUtils.isLessThan(mainAsset, totalAmount)) {
            throw new NulsRuntimeException(AccountErrorCode.INSUFFICIENT_FEE);
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(Arrays.asList(coinFrom));
        coinData.setTo(Arrays.asList(coinTo));
        transaction.setCoinData(coinData.serialize());
        transaction.setHash(NulsDigestData.calcDigestData(transaction.serializeForHash()));
        return transaction;
    }

    private TransactionSignature buildMultiSignTransactionSignature(Transaction transaction, MultiSigAccount multiSigAccount, Account account, String password) throws NulsException, IOException {
        //使用签名账户对交易进行签名
        //TransactionSignature transactionSignature = new TransactionSignature();
//        List<P2PHKSignature> p2PHKSignatures;
//        if (transaction.getTransactionSignature() != null) {
//            transactionSignature.parse(new NulsByteBuffer(transaction.getTransactionSignature()));
//            p2PHKSignatures = transactionSignature.getP2PHKSignatures();
//            for (P2PHKSignature p2PHKSignature: p2PHKSignatures) {
//                if(Arrays.equals(p2PHKSignature.getPublicKey(),account.getPubKey())){
//                    throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ALREADY_SIGNED);
//                }
//            }
//
//        } else {
//            p2PHKSignatures = new ArrayList<>();
//        }
//        ECKey eckey = account.getEcKey(password);
//        P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByEckey(transaction, eckey);
//        p2PHKSignatures.add(p2PHKSignature);
//        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
//        transaction.setTransactionSignature(transactionSignature.serialize());

        MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
        List<P2PHKSignature> p2PHKSignatures;
        if (transaction.getTransactionSignature() != null) {
            transactionSignature.parse(new NulsByteBuffer(transaction.getTransactionSignature()));
            p2PHKSignatures = transactionSignature.getP2PHKSignatures();
            for (P2PHKSignature p2PHKSignature : p2PHKSignatures) {
                if (Arrays.equals(p2PHKSignature.getPublicKey(), account.getPubKey())) {
                    throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ALREADY_SIGNED);
                }
            }

        } else {
            p2PHKSignatures = new ArrayList<>();
            if (multiSigAccount == null) {
                throw new NulsRuntimeException(AccountErrorCode.TX_NOT_EFFECTIVE);
            }
            transactionSignature.setM(multiSigAccount.getM());
            transactionSignature.setPubKeyList(multiSigAccount.getPubKeyList());
        }
        ECKey eckey = account.getEcKey(password);
        P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByEckey(transaction, eckey);
        p2PHKSignatures.add(p2PHKSignature);
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        transaction.setTransactionSignature(transactionSignature.serialize());
        return transactionSignature;
    }

    private Transaction assemblyTransaction(int chainId, List<CoinDto> fromList, List<CoinDto> toList, String remark) {
        Transaction tx = new Transaction(AccountConstant.TX_TYPE_TRANSFER);
        tx.setRemark(StringUtils.bytes(remark));
        try {
            //组装CoinData中的coinFrom、coinTo数据
            assemblyCoinData(tx, chainId, fromList, toList);
            //计算交易数据摘要哈希
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //创建ECKey用于签名
            List<ECKey> signEcKeys = new ArrayList<>();
            for (CoinDto from : fromList) {
                //检查账户是否存在
                Account account = accountService.getAccount(from.getAssetsChainId(), from.getAddress());
                if (null == account) {
                    throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
                }
                ECKey ecKey = account.getEcKey(from.getPassword());
                signEcKeys.add(ecKey);
            }
            //交易签名
            SignatureUtil.createTransactionSignture(tx, signEcKeys);
            //缓存当前交易hash
            this.cacheTxHash(tx);
            //发起新交易
            TransactionCmdCall.newTx(chainId, tx.hex());
        } catch (NulsException e) {
            Log.error("assemblyTransaction exception.", e);
            throw new NulsRuntimeException(e.getErrorCode());
        } catch (IOException e) {
            Log.error("assemblyTransaction io exception.", e);
            throw new NulsRuntimeException(AccountErrorCode.SERIALIZE_ERROR);
        } catch (Exception e) {
            Log.error("assemblyTransaction error.", e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return tx;
    }

    /**
     * 组装CoinData数据
     * assembly coinFrom data
     *
     * @param tx
     * @param chainId
     * @param fromList
     * @param toList
     * @return
     */
    private Transaction assemblyCoinData(Transaction tx, int chainId, List<CoinDto> fromList, List<CoinDto> toList) {
        try {
            //组装coinFrom、coinTo数据
            List<CoinFrom> coinFromList = assemblyCoinFrom(chainId, fromList);
            List<CoinTo> coinToList = assemblyCoinTo(chainId, toList);
            //来源地址或转出地址为空
            if (coinFromList.size() == 0 || coinToList.size() == 0) {
                throw new NulsRuntimeException(AccountErrorCode.COINDATA_IS_INCOMPLETE);
            }
            //交易总大小=交易数据大小+签名数据大小
            int txSize = tx.size() + getSignatureSize(coinFromList);
            //组装coinData数据
            CoinData coinData = getCoinData(chainId, coinFromList, coinToList, txSize);
            tx.setCoinData(coinData.serialize());
        } catch (NulsException e) {
            Log.error("assemblyCoinData exception.", e);
            throw new NulsRuntimeException(e.getErrorCode());
        } catch (IOException e) {
            Log.error("assemblyCoinData io exception.", e);
            throw new NulsRuntimeException(AccountErrorCode.SERIALIZE_ERROR);
        } catch (Exception e) {
            Log.error("assemblyCoinData error.", e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return tx;
    }

    /**
     * 组装coinFrom数据
     * assembly coinFrom data
     *
     * @param listFrom Initiator set coinFrom
     * @return List<CoinFrom>
     * @throws NulsException
     */
    private List<CoinFrom> assemblyCoinFrom(int chainId, List<CoinDto> listFrom) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        for (CoinDto coinDto : listFrom) {
            String address = coinDto.getAddress();
            byte[] addressByte = AddressTool.getAddress(address);
            //转账交易转出地址必须是本链地址
            if (!AddressTool.validAddress(chainId, address)) {
                throw new NulsException(AccountErrorCode.IS_NOT_CURRENT_CHAIN_ADDRESS);
            }
            //检查该链是否有该资产
            int assetChainId = coinDto.getAssetsChainId();
            int assetId = coinDto.getAssetsId();
            if (!this.assetExist(assetChainId, assetId)) {
                throw new NulsException(AccountErrorCode.ASSET_NOT_EXIST);
            }
            //检查对应资产余额是否足够
            BigInteger amount = coinDto.getAmount();
            BigInteger balance = TxUtil.getBalance(chainId, assetChainId, assetId, addressByte);
            if (BigIntegerUtils.isLessThan(balance, amount)) {
                throw new NulsException(AccountErrorCode.INSUFFICIENT_BALANCE);
            }
            //查询账本获取nonce值
            byte[] nonce = TxUtil.getNonce(chainId, assetChainId, assetId, addressByte);
            CoinFrom coinFrom = new CoinFrom(addressByte, assetChainId, assetId, amount, nonce, AccountConstant.NORMAL_TX_LOCKED);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }

    /**
     * 组装coinTo数据
     * assembly coinTo data
     * 条件：to中所有地址必须是同一条链的地址
     *
     * @param listTo Initiator set coinTo
     * @return List<CoinTo>
     * @throws NulsException
     */
    private List<CoinTo> assemblyCoinTo(int chainId, List<CoinDto> listTo) throws NulsException {
        List<CoinTo> coinTos = new ArrayList<>();
        for (CoinDto coinDto : listTo) {
            String address = coinDto.getAddress();
            byte[] addressByte = AddressTool.getAddress(address);
            //转账交易转出地址必须是本链地址
            if (!AddressTool.validAddress(chainId, address)) {
                throw new NulsException(AccountErrorCode.IS_NOT_CURRENT_CHAIN_ADDRESS);
            }
            //检查该链是否有该资产
            int assetsChainId = coinDto.getAssetsChainId();
            int assetId = coinDto.getAssetsId();
            if (!this.assetExist(assetsChainId, assetId)) {
                throw new NulsException(AccountErrorCode.ASSET_NOT_EXIST);
            }
            CoinTo coinTo = new CoinTo();
            coinTo.setAddress(addressByte);
            coinTo.setAssetsChainId(assetsChainId);
            coinTo.setAssetsId(assetId);
            coinTo.setAmount(coinDto.getAmount());
            coinTos.add(coinTo);
        }
        return coinTos;
    }


    /**
     * 组装coinData数据
     * assembly coinData
     *
     * @param listFrom
     * @param listTo
     * @param txSize
     * @return
     * @throws NulsException
     */
    private CoinData getCoinData(int chainId, List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException {
        //总来源费用
        BigInteger feeTotalFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : listFrom) {
            txSize += coinFrom.size();
            if (this.assetExist(chainId, coinFrom.getAssetsId())) {
                feeTotalFrom = feeTotalFrom.add(coinFrom.getAmount());
            }
        }
        //总转出费用
        BigInteger feeTotalTo = BigInteger.ZERO;
        for (CoinTo coinTo : listTo) {
            txSize += coinTo.size();
            if (this.assetExist(chainId, coinTo.getAssetsId())) {
                feeTotalTo = feeTotalTo.add(coinTo.getAmount());
            }
        }
        //本交易预计收取的手续费
        BigInteger targetFee = TransactionFeeCalculator.getNormalTxFee(txSize);
        //实际收取的手续费, 可能自己已经组装完成
        BigInteger actualFee = feeTotalFrom.subtract(feeTotalTo);
        if (BigIntegerUtils.isLessThan(actualFee, BigInteger.ZERO)) {
            //所有from中账户的当前链主资产余额总和小于to的总和，不够支付手续费
            throw new NulsException(AccountErrorCode.INSUFFICIENT_FEE);
        } else if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
            //只从资产为当前链主资产的coinfrom中收取手续费
            actualFee = getFeeDirect(chainId, listFrom, targetFee, actualFee);
            if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
                //如果没收到足够的手续费，则从CoinFrom中资产不是当前链主资产的coin账户中查找当前链主资产余额，并组装新的coinfrom来收取手续费
                if (!getFeeIndirect(chainId, listFrom, txSize, targetFee, actualFee)) {
                    //所有from中账户的当前链主资产余额总和都不够支付手续费
                    throw new NulsException(AccountErrorCode.INSUFFICIENT_FEE);
                }
            }
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        return coinData;
    }

    /**
     * 校验该链是否有该资产
     *
     * @param chainId
     * @param assetId
     * @return
     */
    @Override
    public boolean assetExist(int chainId, int assetId) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
        }
        if (chain.getConfig().getAssetsId() == assetId) {
            return true;
        }
        return false;
    }

    /**
     * Only the fee is charged from the coin in CoinFrom for the current chain main asset, and the actual amount is returned.
     * 只从CoinFrom中资产为当前链主资产的coin中收取手续费，返回实际收取的数额
     *
     * @param listFrom  All coins transferred out 转出的所有coin
     * @param targetFee The amount of the fee that needs to be charged 需要收取的手续费数额
     * @param actualFee Actual amount charged 实际收取的数额
     * @return BigInteger The amount of the fee actually charged 实际收取的手续费数额
     * @throws NulsException
     */
    private BigInteger getFeeDirect(int chainId, List<CoinFrom> listFrom, BigInteger targetFee, BigInteger actualFee) throws NulsException {
        Chain chain = chainManager.getChainMap().get(chainId);
        int assetsId = chain.getConfig().getAssetsId();
        for (CoinFrom coinFrom : listFrom) {
            //必须为当前链主资产
            if (TxUtil.isChainAssetExist(chain, coinFrom)) {
                BigInteger mainAsset = TxUtil.getBalance(chainId, chainId, assetsId, coinFrom.getAddress());
                //当前还差的手续费
                BigInteger current = targetFee.subtract(actualFee);
                //如果余额大于等于目标手续费，则直接收取全额手续费
                if (BigIntegerUtils.isEqualOrGreaterThan(mainAsset, current)) {
                    coinFrom.setAmount(coinFrom.getAmount().add(current));
                    actualFee = actualFee.add(current);
                    break;
                } else if (BigIntegerUtils.isGreaterThan(mainAsset, BigInteger.ZERO)) {
                    coinFrom.setAmount(coinFrom.getAmount().add(mainAsset));
                    actualFee = actualFee.add(mainAsset);
                    continue;
                }
            }
        }
        return actualFee;
    }

    /**
     * 从CoinFrom中资产不为当前链主资产的coin中收取当前链主资产手续费，返回是否收取完成
     * From the coin in CoinFrom, the current chain main asset handling fee is not collected in the coin of the current chain main asset, and the return is collected.
     *
     * @param listFrom  All coins transferred out 转出的所有coin
     * @param txSize    Current transaction size
     * @param targetFee Estimated fee
     * @param actualFee actual Fee
     * @return boolean
     * @throws NulsException
     */
    private boolean getFeeIndirect(int chainId, List<CoinFrom> listFrom, int txSize, BigInteger targetFee, BigInteger actualFee) throws NulsException {
        ListIterator<CoinFrom> iterator = listFrom.listIterator();
        Chain chain = chainManager.getChainMap().get(chainId);
        int assetsId = chain.getConfig().getAssetsId();
        while (iterator.hasNext()) {
            CoinFrom coinFrom = iterator.next();
            //如果不为当前链主资产
            if (!TxUtil.isChainAssetExist(chain, coinFrom)) {
                //查询该地址在当前链的主资产余额
                BigInteger mainAsset = TxUtil.getBalance(chainId, chainId, assetsId, coinFrom.getAddress());
                if (BigIntegerUtils.isEqualOrLessThan(mainAsset, BigInteger.ZERO)) {
                    continue;
                }
                //组装手续费作为CoinFrom
                CoinFrom feeCoinFrom = new CoinFrom();
                byte[] address = coinFrom.getAddress();
                feeCoinFrom.setAddress(address);
                feeCoinFrom.setNonce(TxUtil.getNonce(chainId, chainId, assetsId, address));
                txSize += feeCoinFrom.size();
                //由于新增CoinFrom，需要重新计算本交易预计收取的手续费
                targetFee = TransactionFeeCalculator.getNormalTxFee(txSize);
                //当前还差的手续费
                BigInteger current = targetFee.subtract(actualFee);
                //此账户可以支付的手续费
                BigInteger fee = BigIntegerUtils.isEqualOrGreaterThan(mainAsset, current) ? current : mainAsset;

                feeCoinFrom.setLocked(AccountConstant.NORMAL_TX_LOCKED);
                feeCoinFrom.setAssetsChainId(chainId);
                feeCoinFrom.setAssetsId(assetsId);
                feeCoinFrom.setAmount(fee);

                iterator.add(feeCoinFrom);
                actualFee = actualFee.add(fee);
                if (BigIntegerUtils.isEqualOrGreaterThan(actualFee, targetFee)) {
                    break;
                }
            }
        }
        //最终的实际收取数额大于等于预计收取数额，则可以正确组装CoinData
        if (BigIntegerUtils.isEqualOrGreaterThan(actualFee, targetFee)) {
            return true;
        }
        return false;
    }

    /**
     * 通过coinfrom计算签名数据的size
     * 如果coinfrom有重复地址则只计算一次；如果有多签地址，只计算m个地址的size
     *
     * @param coinFroms
     * @return
     */
    private int getSignatureSize(List<CoinFrom> coinFroms) {
        int size = 0;
        Set<String> commonAddress = new HashSet<>();
        Set<MultiSigAccount> multiSignAddress = new HashSet<>();
        for (CoinFrom coinFrom : coinFroms) {
            String address = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
            MultiSigAccount multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(coinFrom.getAssetsChainId(), address);
            if (multiSigAccount != null) {
                //多签地址
                multiSignAddress.add(multiSigAccount);
            } else {
                commonAddress.add(address);
            }
        }
        size += commonAddress.size() * P2PHKSignature.SERIALIZE_LENGTH;
        for (MultiSigAccount account : multiSignAddress) {
            //不管是单签还是多签账户，最终签名的方式都一样，所以长度也一样（注意，1.0版本中多签账户签名后长度为72）
            size += account.getM() * P2PHKSignature.SERIALIZE_LENGTH;
        }
        return size;
    }

    /**
     * 获取多重签名地址，最小签名数，签名后的size
     *
     * @param signNumber m
     * @return int
     */
    private int getMultiSignAddressSignatureSize(int signNumber) {
        int size = signNumber * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }

    /**
     * 多签交易处理
     * 如果达到最少签名数则广播交易，否则什么也不做
     **/
    public boolean txMutilProcessing(MultiSigAccount multiSigAccount, Transaction tx, TransactionSignature transactionSignature) throws IOException {
        //当已签名数等于M则自动广播该交易
        if (multiSigAccount.getM() == transactionSignature.getP2PHKSignatures().size()) {
            TransactionCmdCall.newTx(multiSigAccount.getChainId(), HexUtil.encode(tx.serialize()));
            // Result saveResult = accountLedgerService.verifyAndSaveUnconfirmedTransaction(tx);
//            if (saveResult.isFailed()) {
//                if (KernelErrorCode.DATA_SIZE_ERROR.getCode().equals(saveResult.getErrorCode().getCode())) {
//                    //重新算一次交易(不超出最大交易数据大小下)的最大金额
//                    Result rs = accountLedgerService.getMaxAmountOfOnce(fromAddr, tx, TransactionFeeCalculator.OTHER_PRECE_PRE_1024_BYTES);
//                    if (rs.isSuccess()) {
//                        Na maxAmount = (Na) rs.getData();
//                        rs = Result.getFailed(KernelErrorCode.DATA_SIZE_ERROR_EXTEND);
//                        rs.setMsg(rs.getMsg() + maxAmount.toDouble());
//                    }
//                    return rs;
//                }
//                return saveResult;
//            }
//            transactionService.newTx(tx);
//            Result sendResult = transactionService.broadcastTx(tx);
//            if (sendResult.isFailed()) {
//                accountLedgerService.deleteTransaction(tx);
//                return sendResult;
//            }
//            return Result.getSuccess().setData(tx.getHash().getDigestHex());
            return true;
        }
        return false;
    }

    /**
     * 缓存发出的交易hash
     *
     * @param tx
     * @throws NulsException
     */
    private void cacheTxHash(Transaction tx) throws NulsException {
        CoinData coinData = TxUtil.getCoinData(tx);
        for (CoinFrom coinFrom : coinData.getFrom()) {
            TxUtil.PRE_HASH_MAP.put(AddressTool.getStringAddressByBytes(coinFrom.getAddress()), tx.getHash());
        }
    }

}
