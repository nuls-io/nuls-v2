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
package io.nuls.transaction.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.rocksdb.storage.CrossChainTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.TxUnverifiedStorageService;
import io.nuls.transaction.db.rocksdb.storage.TxVerifiedStorageService;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.CrossTxData;
import io.nuls.transaction.model.bo.TxRegister;
import io.nuls.transaction.model.bo.TxWrapper;
import io.nuls.transaction.model.dto.AccountSignDTO;
import io.nuls.transaction.model.dto.BlockHeaderDigestDTO;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.service.TransactionService;
import io.nuls.transaction.utils.TransactionManager;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    private TransactionManager transactionManager = TransactionManager.getInstance();

    @Autowired
    private TxUnverifiedStorageService txUnverifiedStorageService;

    @Autowired
    private TxVerifiedStorageService txVerifiedStorageService;

    @Autowired
    private CrossChainTxStorageService crossChainTxStorageService;

    @Override
    public boolean register(TxRegister txRegister) {
        return transactionManager.register(txRegister);
    }

    @Override
    public boolean newTx(int chainId, Transaction tx) throws NulsException {
        //todo 判断已验证未打包的交易里面是否有此交易；已确认的交易中是否有此交易
        Transaction txExist = txVerifiedStorageService.getTx(chainId, tx.getHash());
        if(null != txExist){
            throw new NulsException(TxErrorCode.TRANSACTION_ALREADY_EXISTS);
        }
        TxWrapper txWrapper = new TxWrapper(chainId, tx);
        txUnverifiedStorageService.putTx(txWrapper);
        return true;
    }



    @Override
    public Transaction getTransaction(NulsDigestData hash) throws NulsException {
        //todo 是否需要
        return new Transaction();
    }

    @Override
    public Map<String, String> createCrossMultiTransaction(int currentChainId, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) throws NulsException {
        return createCrossMultiTransaction(currentChainId, listFrom, listTo, remark, null);
    }


    @Override
    public Map<String, String> createCrossMultiTransaction(int currentChainId, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark, AccountSignDTO accountSignDTO) throws NulsException {
        Transaction tx = new Transaction(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
        CrossTxData txData = new CrossTxData();
        txData.setChainId(currentChainId);
        tx.setRemark(StringUtils.bytes(remark));
        try {
            tx.setTxData(txData.serialize());
            List<CoinFrom> coinFromList = assemblyCoinFrom(currentChainId, listFrom, true);
            List<CoinTo> coinToList = assemblyCoinTo(listTo);
            valiCoin(coinFromList, coinToList);
            //多签交易，计算签名大小，取多签地址m来计算
            byte[] fromAddress = coinFromList.get(0).getAddress();
            MultiSigAccount multiSigAccount = TxUtil.getMultiSigAccount(fromAddress);
            int txSize = tx.size();
            txSize += getMultiSignAddressSignatureSize(multiSigAccount.getM());
            CoinData coinData = getCoinData(coinFromList, coinToList, txSize);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //如果发起者没有发送自己的数据则不用签名
            if(null == accountSignDTO){
                //如果密码为空直接返回未签名的多签交易
                Map<String, String> map = new HashMap<>(2);
                map.put(TxConstant.MULTI_TX_HEX, HexUtil.encode(tx.serialize()));
                return map;
            }else {
                //获多签交易发起者的eckey,进行第一个签名
                String priKey = TxUtil.getPrikey(accountSignDTO.getAddress(), accountSignDTO.getPassword());
                ECKey ecKey = ECKey.fromPrivate(new BigInteger(ECKey.SIGNUM, HexUtil.decode(priKey)));
                //验证签名地址账户是否属于多签账户
                if (!AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), ecKey.getPubKey())) {
                    throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH);
                }
                //组装多签地址信息
                MultiSignTxSignature multiSignTxSignature = new MultiSignTxSignature();
                multiSignTxSignature.setM(multiSigAccount.getM());
                multiSignTxSignature.setPubKeyList(multiSigAccount.getPubKeyList());
                //多签交易有且只有一个地址的eckey
                return txMultiSignProcess(new TxWrapper(currentChainId, tx), ecKey, multiSignTxSignature);
            }
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
        }
    }

    @Override
    public String createCrossTransaction(int currentChainId, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) throws NulsException {
        Transaction tx = assemblyCrossTransaction(currentChainId, listFrom, listTo, remark, false);
        return tx.getHash().getDigestHex();
    }

    /**
     * 组装跨链交易
     *
     * @param currentChainId 当前链的id Current chainId
     * @param listFrom       交易的转出者数据 payer coins
     * @param listTo         交易的接收者数据 payee  coins
     * @param remark         交易备注 remark
     * @param isMultiSign    是否是多签地址交易 is Multi-sign address transaction
     * @return
     */
    private Transaction assemblyCrossTransaction(int currentChainId, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark, boolean isMultiSign) throws NulsException {
        Transaction tx = new Transaction(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
        try {
            CrossTxData txData = new CrossTxData();
            txData.setChainId(currentChainId);
            tx.setTxData(txData.serialize());
            tx.setRemark(StringUtils.bytes(remark));
            List<CoinFrom> coinFromList = assemblyCoinFrom(currentChainId, listFrom, isMultiSign);
            List<CoinTo> coinToList = assemblyCoinTo(listTo);
            valiCoin(coinFromList, coinToList);
            int txSize = tx.size();
            txSize += getSignatureSize(coinFromList);
            CoinData coinData = getCoinData(coinFromList, coinToList, txSize);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //签名
            List<ECKey> signEcKeys = new ArrayList<>();
            for (CoinDTO coinDTO : listFrom) {
                String priKey = TxUtil.getPrikey(coinDTO.getAddress(), coinDTO.getPassword());
                ECKey ecKey = ECKey.fromPrivate(new BigInteger(ECKey.SIGNUM, HexUtil.decode(priKey)));
                signEcKeys.add(ecKey);
            }
            SignatureUtil.createTransactionSignture(tx, signEcKeys);
            this.newTx(currentChainId, tx);
            return tx;
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
        }
    }

    /**
     * 验证跨链coin 数据是否存在， from和to不能是同一链
     * @param coinFromList
     * @param coinToList
     * @throws NulsException
     */
    private void valiCoin(List<CoinFrom> coinFromList, List<CoinTo> coinToList) throws NulsException {
        if (coinFromList.size() == 0 || coinToList.size() == 0) {
            throw new NulsException(TxErrorCode.COINDATA_IS_INCOMPLETE);
        }
        byte[] toAddress = coinToList.get(0).getAddress();
        int chainIdTo = AddressTool.getChainIdByAddress(toAddress);
        byte[] fromAddress = coinFromList.get(0).getAddress();
        int chainIdFrom = AddressTool.getChainIdByAddress(fromAddress);
        //from和to地址是同一链的地址，则不能创建跨链交易
        if(chainIdFrom == chainIdTo){
            throw new NulsException(TxErrorCode.PAYEE_AND_PAYER_IS_THE_SAME_CHAIN);
        }
    }

    @Override
    public Map<String, String> signMultiTransaction(String address, String password, String tx) throws NulsException {
        Transaction transaction = TxUtil.getTransaction(tx);
        String priKey = TxUtil.getPrikey(address, password);
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(ECKey.SIGNUM, HexUtil.decode(priKey)));
        MultiSignTxSignature multiSignTxSignature = new MultiSignTxSignature();
        multiSignTxSignature.parse(new NulsByteBuffer(transaction.getTransactionSignature()));
        //验证签名地址账户是否属于多签账户
        if (!AddressTool.validSignAddress(multiSignTxSignature.getPubKeyList(), ecKey.getPubKey())) {
            throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH);
        }
        return txMultiSignProcess(new TxWrapper(AddressTool.getChainIdByAddress(address), transaction), ecKey);
    }

    @Override
    public Map<String, String> txMultiSignProcess(TxWrapper txWrapper, ECKey ecKey) throws NulsException {
        return txMultiSignProcess(txWrapper, ecKey, null);
    }

    @Override
    public Map<String, String> txMultiSignProcess(TxWrapper txWrapper, ECKey ecKey, MultiSignTxSignature multiSignTxSignature) throws NulsException {
        Transaction tx = txWrapper.getTx();
        try {
            if(null == multiSignTxSignature) {
                multiSignTxSignature = new MultiSignTxSignature();
                if(null != tx.getTransactionSignature()) {
                    multiSignTxSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
                }else{
                    //组装多签交易签名信息
                    List<CoinFrom> coinFromList = TxUtil.getCoinData(tx).getFrom();
                    if (null == coinFromList || coinFromList.size() == 0) {
                        throw new NulsException(TxErrorCode.COINDATA_IS_INCOMPLETE);
                    }
                    byte[] fromAddress = coinFromList.get(0).getAddress();
                    MultiSigAccount multiSigAccount = TxUtil.getMultiSigAccount(fromAddress);
                    multiSignTxSignature.setM(multiSigAccount.getM());
                    multiSignTxSignature.setPubKeyList(multiSigAccount.getPubKeyList());
                }
            }
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            if (multiSignTxSignature.getP2PHKSignatures() != null && multiSignTxSignature.getP2PHKSignatures().size() > 0) {
                p2PHKSignatures = multiSignTxSignature.getP2PHKSignatures();
            }
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            p2PHKSignature.setPublicKey(ecKey.getPubKey());
            //用当前交易的hash和账户的eckey签名
            p2PHKSignature.setSignData(SignatureUtil.signDigest(tx.getHash().getDigestBytes(), ecKey));
            p2PHKSignatures.add(p2PHKSignature);
            Map<String, String> map = new HashMap<>();
            if (p2PHKSignatures.size() == multiSignTxSignature.getM()) {
                p2PHKSignatures.sort(P2PHKSignature.PUBKEY_COMPARATOR);
                multiSignTxSignature.setP2PHKSignatures(p2PHKSignatures);
                tx.setTransactionSignature(multiSignTxSignature.serialize());
                this.newTx(txWrapper.getChainId(), tx);
                map.put(TxConstant.MULTI_TX_HASH, tx.getHash().getDigestHex());
            }else{
                multiSignTxSignature.setP2PHKSignatures(p2PHKSignatures);
                tx.setTransactionSignature(multiSignTxSignature.serialize());
                map.put(TxConstant.MULTI_TX_HEX, HexUtil.encode(tx.serialize()));
            }
            return map;
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.IO_ERROR);
        }
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
        Set<String> signAddress = new HashSet<>();
        for (CoinFrom coinFrom : coinFroms) {
            byte[] address = coinFrom.getAddress();
            signAddress.add(AddressTool.getStringAddressByBytes(address));
        }
        size += signAddress.size() * P2PHKSignature.SERIALIZE_LENGTH;
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
     * assembly coinFrom
     *
     * @param listFrom Initiator set coinFrom
     * @return List<CoinFrom>
     * @throws NulsException
     */
    private List<CoinFrom> assemblyCoinFrom(int currentChainId, List<CoinDTO> listFrom, boolean isMultiSign) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        byte[] multiAddress = null;
        for (CoinDTO coinDTO : listFrom) {
            String addr = coinDTO.getAddress();
            byte[] address = AddressTool.getAddress(addr);
            if (isMultiSign) {
                //如果为多签交易，所有froms中有且仅有一个地址，并且只能是多签地址，但可以包含多个资产(from)
                if (null == multiAddress) {
                    multiAddress = address;
                } else if (!Arrays.equals(multiAddress, address)) {
                    throw new NulsException(TxErrorCode.ONLY_ONE_MULTI_SIGNATURE_ADDRESS_ALLOWED);
                }
                if (!AddressTool.isMultiSignAddress(address)) {
                    throw new NulsException(TxErrorCode.IS_NOT_MULTI_SIGNATURE_ADDRESS);
                }
            } else {
                //不是多签交易，from中不能有多签地址
                if (AddressTool.isMultiSignAddress(address)) {
                    throw new NulsException(TxErrorCode.IS_MULTI_SIGNATURE_ADDRESS);
                }
            }
            if (!AddressTool.validAddress(currentChainId, addr)) {
                //转账交易转出地址必须是本链地址
                throw new NulsException(TxErrorCode.ADDRESS_IS_NOT_THE_CURRENT_CHAIN);
            }
            int assetChainId = coinDTO.getAssetsChainId();
            int assetId = coinDTO.getAssetsId();
            if (!TxUtil.assetExist(assetChainId, assetId)) {
                throw new NulsException(TxErrorCode.ASSET_NOT_EXIST);
            }
            //检查对应资产余额 是否足够
            BigInteger amount = coinDTO.getAmount();
            BigInteger balance = TxUtil.getBalance(address, assetChainId, assetId);
            if (BigIntegerUtils.isLessThan(balance, amount)) {
                throw new NulsException(TxErrorCode.INSUFFICIENT_BALANCE);
            }
            byte[] nonce = TxUtil.getNonce(address, assetChainId, assetId);
            CoinFrom coinFrom = new CoinFrom(address, assetChainId, assetId, amount, nonce, TxConstant.CORSS_TX_LOCKED);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }

    /**
     * assembly coinTo 组装to
     * 条件：to中所有地址必须是同一条链的地址
     *
     * @param listTo Initiator set coinTo
     * @return List<CoinTo>
     * @throws NulsException
     */
    private List<CoinTo> assemblyCoinTo(List<CoinDTO> listTo) throws NulsException {
        List<CoinTo> coinTos = new ArrayList<>();
        Integer chainIdOfTo = null;
        for (CoinDTO coinDTO : listTo) {
            byte[] address = AddressTool.getAddress(coinDTO.getAddress());
            if (null == chainIdOfTo) {
                chainIdOfTo = AddressTool.getChainIdByAddress(address);
            } else {
                if (chainIdOfTo != AddressTool.getChainIdByAddress(address)) {
                    throw new NulsException(TxErrorCode.CROSS_TX_PAYEE_CHAINID_NOT_SAME);
                }
            }
            CoinTo coinTo = new CoinTo();
            coinTo.setAddress(address);
            int chainId = coinDTO.getAssetsChainId();
            int assetId = coinDTO.getAssetsId();
            coinTo.setAmount(coinDTO.getAmount());
            if (!TxUtil.assetExist(chainId, assetId)) {
                //资产不存在 chainId assetId
                throw new NulsException(TxErrorCode.ASSET_NOT_EXIST);
            }
            coinTo.setAssetsChainId(chainId);
            coinTo.setAssetsId(assetId);
            coinTos.add(coinTo);
        }
        return coinTos;
    }


    /**
     * assembly coinData
     *
     * @param listFrom
     * @param listTo
     * @param txSize
     * @return
     * @throws NulsException
     */
    private CoinData getCoinData(List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException {
        BigInteger feeTotalFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : listFrom) {
            txSize += coinFrom.size();
            if (TxUtil.isNulsAsset(coinFrom)) {
                feeTotalFrom = feeTotalFrom.add(coinFrom.getAmount());
            }
        }
        BigInteger feeTotalTo = BigInteger.ZERO;
        for (CoinTo coinTo : listTo) {
            txSize += coinTo.size();
            if (TxUtil.isNulsAsset(coinTo)) {
                feeTotalTo = feeTotalTo.add(coinTo.getAmount());
            }
        }
        //本交易预计收取的手续费
        BigInteger targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
        //实际收取的手续费, 可能自己已经组装完成
        BigInteger actualFee = feeTotalFrom.subtract(feeTotalTo);
        if (BigIntegerUtils.isLessThan(actualFee, BigInteger.ZERO)) {
            //所有from中账户的nuls余额总和小于to的总和，不够支付手续费
            throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
        } else if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
            //只从资产为nuls的coinfrom中收取手续费
            actualFee = getFeeDirect(listFrom, targetFee, actualFee);
            if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
                //如果没收到足够的手续费，则从CoinFrom中资产不是nuls的coin账户中查找nuls余额，并组装新的coinfrom来收取手续费
                if (!getFeeIndirect(listFrom, txSize, targetFee, actualFee)) {
                    //所有from中账户的nuls余额总和都不够支付手续费
                    throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
                }
            }
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        return coinData;
    }

    /**
     * Only collect fees from CoinFrom's coins whose assets are nuls, and return the actual amount charged.
     * 只从CoinFrom中资产为nuls的coin中收取手续费，返回实际收取的数额
     *
     * @param listFrom  All coins transferred out 转出的所有coin
     * @param targetFee The amount of the fee that needs to be charged 需要收取的手续费数额
     * @param actualFee Actual amount charged 实际收取的数额
     * @return BigInteger The amount of the fee actually charged 实际收取的手续费数额
     * @throws NulsException
     */
    private BigInteger getFeeDirect(List<CoinFrom> listFrom, BigInteger targetFee, BigInteger actualFee) throws NulsException {
        for (CoinFrom coinFrom : listFrom) {
            if (TxUtil.isNulsAsset(coinFrom)) {
                BigInteger mainAsset = TxUtil.getBalance(coinFrom.getAddress(), TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID);
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
     * 从CoinFrom中资产不为nuls的coin中收取nuls手续费，返回是否收取完成
     * Only collect the nuls fee from the coin in CoinFrom whose assets are not nuls, and return whether the charge is completed.
     *
     * @param listFrom  All coins transferred out 转出的所有coin
     * @param txSize    Current transaction size
     * @param targetFee Estimated fee
     * @param actualFee actual Fee
     * @return boolean
     * @throws NulsException
     */
    private boolean getFeeIndirect(List<CoinFrom> listFrom, int txSize, BigInteger targetFee, BigInteger actualFee) throws NulsException {
        ListIterator<CoinFrom> iterator = listFrom.listIterator();
        while (iterator.hasNext()) {
            CoinFrom coinFrom = iterator.next();
            if (!TxUtil.isNulsAsset(coinFrom)) {
                BigInteger mainAsset = TxUtil.getBalance(coinFrom.getAddress(), TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID);
                if (BigIntegerUtils.isEqualOrLessThan(mainAsset, BigInteger.ZERO)) {
                    continue;
                }
                CoinFrom feeCoinFrom = new CoinFrom();
                byte[] address = coinFrom.getAddress();
                feeCoinFrom.setAddress(address);
                feeCoinFrom.setNonce(TxUtil.getNonce(address, TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID));
                txSize += feeCoinFrom.size();
                //新增coinfrom，重新计算本交易预计收取的手续费
                targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
                //当前还差的手续费
                BigInteger current = targetFee.subtract(actualFee);
                //此账户可以支付的手续费
                BigInteger fee = BigIntegerUtils.isEqualOrGreaterThan(mainAsset, current) ? current : mainAsset;

                feeCoinFrom.setLocked(TxConstant.CORSS_TX_LOCKED);
                feeCoinFrom.setAssetsChainId(TxConstant.NULS_CHAINID);
                feeCoinFrom.setAssetsId(TxConstant.NULS_CHAIN_ASSETID);
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
     * 跨链交易验证器
     * 交易类型为跨链交易
     * 地址和签名一一对应
     * from里面的资产是否存在，是否可以进行跨链交易
     * 必须包含NULS资产的from
     * 验证txData发起链id和from地址链id是否一致
     *
     * @param chainId
     * @param tx
     * @return Result
     */
    @Override
    public boolean crossTransactionValidator(int chainId, Transaction tx) throws NulsException {
        /*if (!transactionManager.baseTxValidate(chainId, tx)) {
            return false;
        }*/
        if (null == tx.getCoinData() || tx.getCoinData().length == 0) {
            throw new NulsException(TxErrorCode.COINDATA_NOT_FOUND);
        }
        CoinData coinData = TxUtil.getCoinData(tx);
        if (!validateCoinFrom(coinData.getFrom())) {
            return false;
        }
        //验证txData发起链id和from地址链id是否一致
        int fromChainId = getCrossTxFromsOriginChainId(tx);
        CrossTxData crossTxData = TxUtil.getCrossTxData(tx);
        if(fromChainId != crossTxData.getChainId()){
            throw new NulsException(TxErrorCode.CROSS_TX_PAYER_CHAINID_MISMATCH);
        }
        return true;
    }

    /**
     * 验证跨链交易的付款方数据
     *
     * @param listFrom
     * @return
     */
    private boolean validateCoinFrom(List<CoinFrom> listFrom) throws NulsException {
        if (null == listFrom || listFrom.size() == 0) {
            throw new NulsException(TxErrorCode.COINFROM_NOT_FOUND);
        }
        boolean hasNulsFrom = false;
        for (CoinFrom coinFrom : listFrom) {
            //是否有nuls(手续费)
            if (TxUtil.isNulsAsset(coinFrom)) {
                hasNulsFrom = true;
            }
            //只有主网才会进入跨链交易验证器，直接验证资产即可
            if (!TxUtil.assetExist(coinFrom.getAssetsChainId(), coinFrom.getAssetsId())) {
                throw new NulsException(TxErrorCode.ASSET_NOT_EXIST);
            }
        }
        if (!hasNulsFrom) {
            throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
        }
        return true;
    }



    /**
     * 获取跨链交易tx中froms里面地址的链id
     * @param tx
     * @return
     */
    private int getCrossTxFromsOriginChainId(Transaction tx) throws NulsException {
        CoinData coinData = TxUtil.getCoinData(tx);
        if(null == coinData.getFrom() || coinData.getFrom().size() == 0){
            throw new NulsException(TxErrorCode.COINFROM_NOT_FOUND);
        }
        return AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());

    }

    @Override
    public boolean crossTransactionCommit(int chainId, Transaction tx, BlockHeaderDigestDTO blockHeader) {
        //todo 调账本记账
        return true;
    }

    @Override
    public boolean crossTransactionRollback(int chainId, Transaction tx, BlockHeaderDigestDTO blockHeader) {
        //todo 调账本回滚？
        return true;
    }

    @Override
    public List<String> packableTxs(int chainId, long endtimestamp, String maxTxDataSize) throws NulsException {
        TimeService.currentTimeMillis();
        /**
         * 1.取出交易的逻辑
         * 2.调用批量验证的逻辑 batchVerify ？
         */
        return null;
    }

    @Override
    public boolean batchVerify(int chainId, List<String> txHexList) throws NulsException {

        List<Transaction> txList = new ArrayList<>();

        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<String, List<String>> moduleVerifyMap = new HashMap<>();
        for (String txHex : txHexList) {
            //将txHex转换为Transaction对象
            Transaction tx = TxUtil.getTransaction(txHex);
            txList.add(tx);
            if(tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER){
                CrossTxData crossTxData = TxUtil.getCrossTxData(tx);
                if(crossTxData.getChainId() != chainId){
                    //如果是跨链交易，发起链不是当前链，则核对(跨链验证的结果)
                    CrossChainTx crossChainTx =  crossChainTxStorageService.getTx(tx.getHash());
                    //todo
                    /**
                     * 核对(跨链验证的结果)
                     */
                }
            }
            //验证单个交易
            transactionManager.verify(chainId, tx);

            //验证coinData
            TxUtil.verifyCoinData(txHex);
            //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
            TxRegister txRegister = transactionManager.getTxRegister(tx.getType());
            if(moduleVerifyMap.containsKey(txRegister.getModuleValidator())){
                moduleVerifyMap.get(txRegister.getModuleValidator()).add(txHex);
            }else{
                List<String> txHexs = new ArrayList<>();
                txHexs.add(txHex);
                moduleVerifyMap.put(txRegister.getModuleValidator(), txHexs);
            }
        }
        //todo 是否需要统一验证coinData?
        TxUtil.verifyCoinData(txHexList);
        //统一验证
        TxUtil.txsModuleValidators(moduleVerifyMap);

        return true;
    }
}
