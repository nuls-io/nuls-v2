/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.BigIntegerUtils;
import io.nuls.tools.model.StringUtils;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossTxData;
import io.nuls.transaction.model.dto.AccountSignDTO;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.rpc.call.AccountCall;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.service.TxGenerateService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: Charlie
 * @date: 2019/3/15
 */
@Service
public class TxGenerateServiceImpl implements TxGenerateService {

    @Autowired
    private TxService txService;

    @Autowired
    private TxConfig txConfig;

    private static final Map<String, NonceHashData> PRE_HASH_MAP = new HashMap<>(TxConstant.INIT_CAPACITY_16);

    @Override
    public Map<String, String> createCrossMultiTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) throws NulsException {
        return createCrossMultiTransaction(chain, listFrom, listTo, remark, null);
    }


    @Override
    public Map<String, String> createCrossMultiTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark, AccountSignDTO accountSignDTO) throws NulsException {
        Transaction tx = new Transaction(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
        CrossTxData txData = new CrossTxData();
        txData.setChainId(chain.getChainId());
        tx.setRemark(StringUtils.bytes(remark));
        try {
            tx.setTxData(txData.serialize());
            List<CoinFrom> coinFromList = assemblyCoinFrom(chain, listFrom, true);
            List<CoinTo> coinToList = assemblyCoinTo(listTo);
            valiCoin(coinFromList, coinToList);
            //多签交易，计算签名大小，取多签地址m来计算
            byte[] fromAddress = coinFromList.get(0).getAddress();
            MultiSigAccount multiSigAccount = AccountCall.getMultiSigAccount(fromAddress);
            int txSize = tx.size();
            txSize += getMultiSignAddressSignatureSize(multiSigAccount.getM());
            CoinData coinData = getCoinData(chain, coinFromList, coinToList, txSize);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //如果发起者没有发送自己的数据则不用签名
            if (null == accountSignDTO) {
                //如果密码为空直接返回未签名的多签交易
                Map<String, String> map = new HashMap<>(2);
                map.put(TxConstant.MULTI_TX_HEX, HexUtil.encode(tx.serialize()));
                return map;
            } else {
                //获多签交易发起者的eckey,进行第一个签名
                if (!AccountCall.isEncrypted(accountSignDTO.getAddress())) {
                    throw new NulsException(TxErrorCode.ACCOUNT_NOT_ENCRYPTED);
                }
                String priKey = AccountCall.getPrikey(accountSignDTO.getAddress(), accountSignDTO.getPassword());
                ECKey ecKey = ECKey.fromPrivate(new BigInteger(ECKey.SIGNUM, HexUtil.decode(priKey)));
                //验证待签名地址账户是否属于多签账户
                if (!AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), ecKey.getPubKey())) {
                    throw new NulsException(TxErrorCode.ADDRESS_NOT_BELONG_TO_MULTI_SIGN_ACCOUNT);
                }
                //组装多签地址信息
                MultiSignTxSignature multiSignTxSignature = new MultiSignTxSignature();
                multiSignTxSignature.setM(multiSigAccount.getM());
                multiSignTxSignature.setPubKeyList(multiSigAccount.getPubKeyList());
                //多签交易有且只有一个地址的eckey
                return txMultiSignProcess(chain, tx, ecKey, multiSignTxSignature);
            }
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
        }
    }

    @Override
    public String createCrossTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark) throws NulsException {
        Transaction tx = assemblyCrossTransaction(chain, listFrom, listTo, remark, false);
        return tx.getHash().getDigestHex();
    }

    /**
     * 组装跨链交易
     *
     * @param chain       当前链的id Current chainId
     * @param listFrom    交易的转出者数据 payer coins
     * @param listTo      交易的接收者数据 payee  coins
     * @param remark      交易备注 remark
     * @param isMultiSign 是否是多签地址交易 is Multi-sign address transaction
     * @return
     */
    private Transaction assemblyCrossTransaction(Chain chain, List<CoinDTO> listFrom, List<CoinDTO> listTo, String remark, boolean isMultiSign) throws NulsException {
        Transaction tx = new Transaction(TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER);
        try {
            CrossTxData txData = new CrossTxData();
            txData.setChainId(chain.getChainId());
            tx.setTxData(txData.serialize());
            tx.setRemark(StringUtils.bytes(remark));
            List<CoinFrom> coinFromList = assemblyCoinFrom(chain, listFrom, isMultiSign);
            List<CoinTo> coinToList = assemblyCoinTo(listTo);
            valiCoin(coinFromList, coinToList);
            int txSize = tx.size();
            txSize += getSignatureSize(coinFromList);
            CoinData coinData = getCoinData(chain, coinFromList, coinToList, txSize);
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            //签名
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            for (CoinDTO coinDTO : listFrom) {
                String digestBytesStr = HexUtil.encode(tx.getHash().getDigestBytes());
                P2PHKSignature p2PHKSignature = AccountCall.signDigest(coinDTO.getAddress(), coinDTO.getPassword(), digestBytesStr);
                p2PHKSignatures.add(p2PHKSignature);
            }
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            this.cacheTxHash(tx);
            txService.newTx(chain, tx);
            return tx;
        } catch (IOException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(e);
        }
    }

    /**
     * 验证跨链coin 数据是否存在， from和to不能是同一链
     *
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
        if (chainIdFrom == chainIdTo) {
            throw new NulsException(TxErrorCode.PAYEE_AND_PAYER_IS_THE_SAME_CHAIN);
        }
    }

    @Override
    public Map<String, String> signMultiTransaction(Chain chain, String address, String password, String tx) throws NulsException {
        if (!AccountCall.isEncrypted(address)) {
            throw new NulsException(TxErrorCode.ACCOUNT_NOT_ENCRYPTED);
        }
        Transaction transaction = TxUtil.getTransaction(tx);
        String priKey = AccountCall.getPrikey(address, password);
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(ECKey.SIGNUM, HexUtil.decode(priKey)));
        MultiSignTxSignature multiSignTxSignature = new MultiSignTxSignature();
        multiSignTxSignature.parse(new NulsByteBuffer(transaction.getTransactionSignature()));
        //验证该地址是否属于多签账户
        if (!AddressTool.validSignAddress(multiSignTxSignature.getPubKeyList(), ecKey.getPubKey())) {
            throw new NulsException(TxErrorCode.ADDRESS_NOT_BELONG_TO_MULTI_SIGN_ACCOUNT);
        }
        return txMultiSignProcess(chain, transaction, ecKey);
    }

    @Override
    public Map<String, String> txMultiSignProcess(Chain chain, Transaction tx, ECKey ecKey) throws NulsException {
        return txMultiSignProcess(chain, tx, ecKey, null);
    }

    @Override
    public Map<String, String> txMultiSignProcess(Chain chain, Transaction tx, ECKey ecKey, MultiSignTxSignature multiSignTxSignature) throws NulsException {
        try {
            if (null == multiSignTxSignature) {
                multiSignTxSignature = new MultiSignTxSignature();
                if (null != tx.getTransactionSignature()) {
                    multiSignTxSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
                } else {
                    //组装多签交易签名信息
                    List<CoinFrom> coinFromList = TxUtil.getCoinData(tx).getFrom();
                    if (null == coinFromList || coinFromList.size() == 0) {
                        throw new NulsException(TxErrorCode.COINDATA_IS_INCOMPLETE);
                    }
                    byte[] fromAddress = coinFromList.get(0).getAddress();
                    MultiSigAccount multiSigAccount = AccountCall.getMultiSigAccount(fromAddress);
                    if (null == multiSigAccount) {
                        throw new NulsException(TxErrorCode.ACCOUNT_NOT_EXIST);
                    }
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
                this.cacheTxHash(tx);
                txService.newTx(chain, tx);
                map.put(TxConstant.MULTI_TX_HASH, tx.getHash().getDigestHex());
            } else {
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
    private List<CoinFrom> assemblyCoinFrom(Chain chain, List<CoinDTO> listFrom, boolean isMultiSign) throws NulsException {
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
                if (!AccountCall.isEncrypted(addr)) {
                    throw new NulsException(TxErrorCode.ACCOUNT_NOT_ENCRYPTED);
                }
            }
            if (!AddressTool.validAddress(chain.getChainId(), addr)) {
                //转账交易转出地址必须是本链地址
                throw new NulsException(TxErrorCode.ADDRESS_IS_NOT_THE_CURRENT_CHAIN);
            }
            int assetChainId = coinDTO.getAssetsChainId();
            int assetId = coinDTO.getAssetsId();
            //检查对应资产余额 是否足够
            BigInteger amount = coinDTO.getAmount();
            BigInteger balance = LedgerCall.getBalanceNonce(chain, address, assetChainId, assetId);
            if (BigIntegerUtils.isLessThan(balance, amount)) {
                throw new NulsException(TxErrorCode.INSUFFICIENT_BALANCE);
            }
            byte[] nonce = getNonce(chain, addr, assetChainId, assetId);
            CoinFrom coinFrom = new CoinFrom(address, assetChainId, assetId, amount, nonce, TxConstant.CORSS_TX_LOCKED);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }


    /**
     * 获取nonce
     * 先获取上一个发出去的交易的hash,用来计算当前交易的nonce,如果没有缓存上一个交易hash则直接向账本获取nonce
     *
     * @param chain
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     * @throws NulsException
     */
    public byte[] getNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException {
        String key = address + "_" + assetChainId + "_" + assetId;
        NonceHashData nonceHashData = PRE_HASH_MAP.get(key);
        if (null == nonceHashData || (NetworkCall.getCurrentTimeMillis() - nonceHashData.getCacheTimestamp() > txConfig.getHashTtl())) {
            return LedgerCall.getNonce(chain, address, assetChainId, assetId);
        } else {
            return TxUtil.getNonceByPreHash(nonceHashData.getHash());
        }
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
            String address = AddressTool.getStringAddressByBytes(coinFrom.getAddress());
            String key = address + "_" + coinFrom.getAssetsChainId() + "_" + coinFrom.getAssetsId();
            PRE_HASH_MAP.put(key, new NonceHashData(tx.getHash(), NetworkCall.getCurrentTimeMillis()));
        }
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
            //todo
            /*if (!ChainCall.verifyAssetExist(chainId, assetId)) {
                //资产不存在 chainId assetId
                throw new NulsException(TxErrorCode.ASSET_NOT_EXIST);
            }*/
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
    private CoinData getCoinData(Chain chain, List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize) throws NulsException {
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
            actualFee = getFeeDirect(chain, listFrom, targetFee, actualFee);
            if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
                //如果没收到足够的手续费，则从CoinFrom中资产不是nuls的coin账户中查找nuls余额，并组装新的coinfrom来收取手续费
                if (!getFeeIndirect(chain, listFrom, txSize, targetFee, actualFee)) {
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
    private BigInteger getFeeDirect(Chain chain, List<CoinFrom> listFrom, BigInteger targetFee, BigInteger actualFee) throws NulsException {
        for (CoinFrom coinFrom : listFrom) {
            if (TxUtil.isNulsAsset(coinFrom)) {
                BigInteger mainAsset = LedgerCall.getBalanceNonce(chain, coinFrom.getAddress(), txConfig.getMainChainId(), txConfig.getMainAssetId());
                //可用余额=当前余额减去本次转出
                mainAsset = mainAsset.subtract(coinFrom.getAmount());
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
    private boolean getFeeIndirect(Chain chain, List<CoinFrom> listFrom, int txSize, BigInteger targetFee, BigInteger actualFee) throws NulsException {
        ListIterator<CoinFrom> iterator = listFrom.listIterator();
        while (iterator.hasNext()) {
            CoinFrom coinFrom = iterator.next();
            if (!TxUtil.isNulsAsset(coinFrom)) {
                BigInteger mainAsset = LedgerCall.getBalanceNonce(chain, coinFrom.getAddress(), txConfig.getMainChainId(), txConfig.getMainAssetId());
                if (BigIntegerUtils.isEqualOrLessThan(mainAsset, BigInteger.ZERO)) {
                    continue;
                }
                CoinFrom feeCoinFrom = new CoinFrom();
                byte[] address = coinFrom.getAddress();
                feeCoinFrom.setAddress(address);
                feeCoinFrom.setNonce(getNonce(chain, AddressTool.getStringAddressByBytes(address), txConfig.getMainChainId(), txConfig.getMainAssetId()));
                txSize += feeCoinFrom.size();
                //新增coinfrom，重新计算本交易预计收取的手续费
                targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
                //当前还差的手续费
                BigInteger current = targetFee.subtract(actualFee);
                //此账户可以支付的手续费
                BigInteger fee = BigIntegerUtils.isEqualOrGreaterThan(mainAsset, current) ? current : mainAsset;

                feeCoinFrom.setLocked(TxConstant.CORSS_TX_LOCKED);
                feeCoinFrom.setAssetsChainId(txConfig.getMainChainId());
                feeCoinFrom.setAssetsId(txConfig.getMainAssetId());
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
}
