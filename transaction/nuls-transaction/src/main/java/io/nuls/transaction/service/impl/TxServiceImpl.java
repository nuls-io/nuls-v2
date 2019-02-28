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
import io.nuls.base.signture.TransactionSignature;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.rocksdb.storage.ConfirmedTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.CtxStorageService;
import io.nuls.transaction.db.rocksdb.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.db.rocksdb.storage.UnverifiedTxStorageService;
import io.nuls.transaction.manager.TransactionManager;
import io.nuls.transaction.model.bo.*;
import io.nuls.transaction.model.dto.AccountSignDTO;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.rpc.call.*;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.CtxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
@Service
public class TxServiceImpl implements TxService {

    @Autowired
    private TransactionManager transactionManager;

    @Autowired
    private PackablePool packablePool;

    @Autowired
    private UnverifiedTxStorageService unverifiedTxStorageService;

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    @Autowired
    private CtxStorageService ctxStorageService;

    @Autowired
    private ConfirmedTxService confirmedTxService;

    @Autowired
    private TransactionH2Service transactionH2Service;

    @Autowired
    private CtxService ctxService;

    @Autowired
    private ConfirmedTxStorageService confirmedTxStorageService;

    @Override
    public boolean register(Chain chain, TxRegister txRegister) {
        return transactionManager.register(chain, txRegister);
    }

    private static final Map<String, NulsDigestData> PRE_HASH_MAP = new HashMap<>(TxConstant.INIT_CAPACITY_16);

    @Override
    public void newTx(Chain chain, Transaction tx) throws NulsException {
        Transaction txExist = getTransaction(chain, tx.getHash());
        if (null == txExist) {
            unverifiedTxStorageService.putTx(chain, tx);
        }
    }

    @Override
    public Transaction getTransaction(Chain chain, NulsDigestData hash) {
        Transaction tx = unconfirmedTxStorageService.getTx(chain.getChainId(), hash);
        if(null == tx){
            tx = confirmedTxService.getConfirmedTransaction(chain, hash);
        }
        return tx;
    }

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
            CoinData coinData = getCoinData(chain,  coinFromList, coinToList, txSize);
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
                if (!AccountCall.isEncrypted(accountSignDTO.getAddress())){
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
            this.newTx(chain, tx);
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
        if (!AccountCall.isEncrypted(address)){
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
                    if(null == multiSigAccount){
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
                this.newTx(chain, tx);
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
                if (!AccountCall.isEncrypted(addr)){
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
            BigInteger balance = LedgerCall.getBalance(chain, address, assetChainId, assetId);
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
     * @param chain
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     * @throws NulsException
     */
    public byte[] getNonce(Chain chain, String address, int assetChainId, int assetId) throws NulsException{
        NulsDigestData hash = PRE_HASH_MAP.get(address);
        if(null == hash){
            return  LedgerCall.getNonce(chain, address, assetChainId, assetId);
        }else{
            return TxUtil.getNonceByPreHash(hash);
        }
    }

    /**
     * 缓存发出的交易hash
     * @param tx
     * @throws NulsException
     */
    private void cacheTxHash(Transaction tx) throws NulsException{
        CoinData coinData = TxUtil.getCoinData(tx);
        for (CoinFrom coinFrom : coinData.getFrom()){
           PRE_HASH_MAP.put(AddressTool.getStringAddressByBytes(coinFrom.getAddress()), tx.getHash());
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
                BigInteger mainAsset = LedgerCall.getBalance(chain, coinFrom.getAddress(), TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID);
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
                BigInteger mainAsset = LedgerCall.getBalance(chain, coinFrom.getAddress(), TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID);
                if (BigIntegerUtils.isEqualOrLessThan(mainAsset, BigInteger.ZERO)) {
                    continue;
                }
                CoinFrom feeCoinFrom = new CoinFrom();
                byte[] address = coinFrom.getAddress();
                feeCoinFrom.setAddress(address);
                feeCoinFrom.setNonce(getNonce(chain, AddressTool.getStringAddressByBytes(address), TxConstant.NULS_CHAINID, TxConstant.NULS_CHAIN_ASSETID));
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
     * @param chain
     * @param tx
     * @return Result
     */
    @Override
    public boolean crossTransactionValidator(Chain chain, Transaction tx) throws NulsException {
        if (null == tx.getCoinData() || tx.getCoinData().length == 0) {
            throw new NulsException(TxErrorCode.COINDATA_NOT_FOUND);
        }
        CoinData coinData = TxUtil.getCoinData(tx);
        if (!validateCoinFrom(coinData.getFrom())) {
            return false;
        }
        if (!validateCoinTo(coinData.getTo())) {
            return false;
        }
        //验证txData发起链id和from地址链id是否一致
        int fromChainId = TxUtil.getCrossTxFromsOriginChainId(tx);
        CrossTxData crossTxData = TxUtil.getInstance(tx.getTxData(), CrossTxData.class);
        if (fromChainId != crossTxData.getChainId()) {
            throw new NulsException(TxErrorCode.CROSS_TX_PAYER_CHAINID_MISMATCH);
        }
        //验证跨链交易coinData,链的账目等
        try {
            if(!ChainCall.verifyCtxAsset(chain, tx.hex())){
                return false;
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    @Override
    public List<String> transactionModuleValidator(Chain chain, List<String> txHexList) throws NulsException {
        Map<String, CrossTxData> map = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        List<String> list = new ArrayList<>();
        //todo 有测试代码
//        int test = 0;//测试代码 主动制造不通过的情况, 会造成不通过交易之后含有相同地址资产的交易不通过
        for(String hex : txHexList){
            Transaction tx = TxUtil.getTransaction(hex);
            CrossTxData crossTxData = TxUtil.getInstance(tx.getTxData(), CrossTxData.class);
            if(map.containsValue(crossTxData)){
                list.add(tx.getHash().getDigestHex());
            }
//            if(test == 2){
//                list.add(tx.getHash().getDigestHex());
//            }
//            test++;

        }
        return list;
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
        Set<String> uniqueCoin = new HashSet<>();
        for (CoinFrom coinFrom : listFrom) {
            //是否有nuls(手续费)
            if (TxUtil.isNulsAsset(coinFrom)) {
                hasNulsFrom = true;
            }
            //验证账户地址,资产链id,资产id的组合唯一性
            int assetsChainId = coinFrom.getAssetsChainId();
            int assetsId = coinFrom.getAssetsId();
            boolean rs = uniqueCoin.add(AddressTool.getStringAddressByBytes(coinFrom.getAddress()) + "-" + assetsChainId + "-" + assetsId);
            if (!rs) {
                throw new NulsException(TxErrorCode.COINFROM_HAS_DUPLICATE_COIN);
            }
        }
        if (!hasNulsFrom) {
            throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
        }
        return true;
    }

    private boolean validateCoinTo(List<CoinTo> listTo) throws NulsException {
        if (null == listTo || listTo.size() == 0) {
            throw new NulsException(TxErrorCode.COINTO_NOT_FOUND);
        }
        //验证跨链交易的from和to的资产是否存在(有效)
        Set<String> uniqueCoin = new HashSet<>();
        for (CoinTo coinTo : listTo) {
            //验证账户地址,资产链id,资产id的组合唯一性
            int assetsChainId = coinTo.getAssetsChainId();
            int assetsId = coinTo.getAssetsId();
            boolean rs = uniqueCoin.add(AddressTool.getStringAddressByBytes(coinTo.getAddress()) + "-" + assetsChainId + "-" + assetsId);
            if (!rs) {
                throw new NulsException(TxErrorCode.COINFROM_HAS_DUPLICATE_COIN);
            }
        }
        return true;
    }


    @Override
    public boolean crossTransactionCommit(Chain chain, List<String> txHexList, String blockHeaderHex) throws NulsException {
        List<NulsDigestData> txHash = new ArrayList<>();
        List<String> successedCoinDataHexs = new ArrayList<>();
        boolean rs = true;
        for(String txHex : txHexList){
            Transaction tx = TxUtil.getTransaction(txHex);
            txHash.add(tx.getHash());
            String coinDataHex = HexUtil.encode(tx.getCoinData());
           if(!ChainCall.ctxAssetCirculateCommit(chain, txHexList, blockHeaderHex)){
               rs = false;
               break;
           }
           successedCoinDataHexs.add(coinDataHex);
        }
        if(rs) {
            //保存生效高度
            BlockHeader blockHeader = TxUtil.getInstance(blockHeaderHex, BlockHeader.class);
            long effectHeight = blockHeader.getHeight() + TxConstant.CTX_EFFECT_THRESHOLD;
            return confirmedTxStorageService.saveCrossTxEffectList(chain.getChainId(), effectHeight, txHash);
        }else{
            for(String coinDataHex : successedCoinDataHexs){
                if(!ChainCall.ctxAssetCirculateRollback(chain, txHexList, blockHeaderHex)){
                    throw new NulsException(TxErrorCode.FAILED);
                }
            }
            return false;
        }
    }

    @Override
    public boolean crossTransactionRollback(Chain chain, List<String> txHexList, String blockHeaderHex) throws NulsException {
        List<String> successedCoinDataHexs = new ArrayList<>();
        boolean rs = true;
        for(String txHex : txHexList){
            Transaction tx = TxUtil.getTransaction(txHex);
            String coinDataHex = HexUtil.encode(tx.getCoinData());
            if(!ChainCall.ctxAssetCirculateRollback(chain, txHexList, blockHeaderHex)){
                rs = false;
                break;
            }
            successedCoinDataHexs.add(coinDataHex);
        }
        if(rs) {
            BlockHeader blockHeader = TxUtil.getInstance(blockHeaderHex, BlockHeader.class);
            long effectHeight = blockHeader.getHeight() + TxConstant.CTX_EFFECT_THRESHOLD;
            return confirmedTxStorageService.removeCrossTxEffectList(chain.getChainId(), effectHeight);
        }else{
            for(String coinDataHex : successedCoinDataHexs){
                if(!ChainCall.ctxAssetCirculateCommit(chain, txHexList, blockHeaderHex)){
                    throw new NulsException(TxErrorCode.FAILED);
                }
            }
            return false;
        }
    }

    /**
     * 1.按时间取出交易执行时间为endtimestamp-500，预留500毫秒给统一验证，
     * 2.取交易同时执行交易验证，然后coinData的验证(先发送开始验证的标识)
     * 3.冲突检测，模块统一验证，如果有没验证通过的交易，则将该交易之后的所有交易再从1.开始执行一次
     */
    @Override
    public List<String> getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize) throws NulsException {
        //重置重新打包标识为false
        chain.getRePackage().set(false);
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        List<Transaction> packingTxList = new ArrayList<>();
        long totalSize = 0L;
        List<String> packableTxs = null;
        try {
            //向账本模块发送要批量验证coinData的标识
            if(!LedgerCall.coinDataBatchNotify(chain)){
                chain.getLogger().error("Call ledger bathValidateBegin interface failed");
                throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
            }
            chain.getLogger().debug("=================================================");
            chain.getLogger().debug("获取打包交易开始,当前待打包队列交易数: {} ", packablePool.getPoolSize(chain));
            chain.getLogger().debug("交易最大容量: {} ", maxTxDataSize);
            chain.getLogger().debug("--------------while-----------");
            long loopDebug = NetworkCall.getCurrentTimeMillis();
            while (true) {
                long currentTimeMillis = NetworkCall.getCurrentTimeMillis();
//                chain.getLogger().debug("");
//                chain.getLogger().debug("########## (循环开始)当前网络时间: {} ", currentTimeMillis);
//                chain.getLogger().debug("########## 预留的[获取打包交易]结束时间: {}, 还剩{}秒 ", endtimestamp, (endtimestamp - currentTimeMillis)/1000.0);
                if (endtimestamp - currentTimeMillis <= TxConstant.VERIFY_OFFSET) {
                    chain.getLogger().debug("########## 打包时间到: {}, -endtimestamp:{} , -offset:{}", currentTimeMillis, endtimestamp, TxConstant.VERIFY_OFFSET);
                    break;
                }
//                chain.getLogger().debug("########## 开始获取交易");
                Transaction tx = packablePool.get(chain);
                if (tx == null) {
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        Log.error("packaging error ", e);
                    }
                    continue;
                }
                long txSize = tx.size();
                if ((totalSize + txSize) > maxTxDataSize) {
                    packablePool.addInFirst(chain, tx, false);
                    chain.getLogger().debug("交易已达最大容量, 实际值: {} - 预定最大值maxTxDataSize:{}",totalSize + txSize, maxTxDataSize);
                    break;
                }
                //从已确认的交易中进行重复交易判断
                Transaction repeatTx = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
                if (repeatTx != null) {
                    clearInvalidTx(chain, tx);
                    chain.getLogger().debug("丢弃已确认过交易,txHash:{}, - type:{}, - time:{}",tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    continue;
                }
                String txHex = null;
                try {
                    txHex = tx.hex();
                } catch (Exception e) {
                    clearInvalidTx(chain, tx);
                    chain.getLogger().warn(e.getMessage(), e);
                    chain.getLogger().debug("丢弃获取hex出错交易,txHash:{}, - type:{}, - time:{}",tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    continue;
                }
                long debugeVerifyStart = NetworkCall.getCurrentTimeMillis();
//                chain.getLogger().debug("########## 已花费时间:{} ", debugeVerifyStart - currentTimeMillis);
//                chain.getLogger().debug("########## 开始调用单个验证器, ");
                //交易业务验证tx
                if (!transactionManager.verify(chain, tx)) {
                    clearInvalidTx(chain, tx);
                    chain.getLogger().debug("丢弃验证器未验证通过交易,txHash:{}, - type:{}, - time:{}",tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    continue;
                }
                long debugeVerifyCoinDataStart = NetworkCall.getCurrentTimeMillis();
//                chain.getLogger().debug("########## 单个验证器花费时间:{} ", debugeVerifyCoinDataStart - debugeVerifyStart);
                //批量验证coinData, 单个发送
                VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, txHex, true);
                if (!verifyTxResult.success()) {
                    //-----debug 打印第一个coinfrom 的nonce
                    String nonce = HexUtil.encode(TxUtil.getCoinData(tx).getFrom().get(0).getNonce());
                    chain.getLogger().debug("丢弃批量验证coinData未通过交易 coinData not success - code: {}, - reason:{}, - type:{}, - first coinFrom nonce:{} - txhash:{}",
                            verifyTxResult.getCode(),  verifyTxResult.getDesc(), tx.getType(), nonce, tx.getHash().getDigestHex());
                    continue;
                }
                long debugeMap = NetworkCall.getCurrentTimeMillis();
//                chain.getLogger().debug("########## 单个VerifyCoinData花费时间:{} ", debugeMap - debugeVerifyCoinDataStart);
                /*if (tx.getType() == 2) {
                    chain.getLogger().debug("**************************** 测试未确认垃圾交易回收,对转账交易不打包");
                    continue;
                }*/
                packingTxList.add(tx);
                totalSize += txSize;
                //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
                TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
                if (moduleVerifyMap.containsKey(txRegister)) {
                    moduleVerifyMap.get(txRegister).add(txHex);
                } else {
                    List<String> txHexs = new ArrayList<>();
                    txHexs.add(txHex);
                    moduleVerifyMap.put(txRegister, txHexs);
                }
                //如果有接收新区块,把取出的交易放回到打包队列
                if(chain.getRePackage().get()){
                    for (Transaction transaction : packingTxList) {
                        packablePool.addInFirst(chain, transaction, false);
                    }
                    return getPackableTxs(chain, endtimestamp, maxTxDataSize);
                }
                long loopOnce = NetworkCall.getCurrentTimeMillis() - currentTimeMillis;
//                chain.getLogger().debug("########## 分组花费时间:{} ",  NetworkCall.getCurrentTimeMillis() - debugeMap);
//                chain.getLogger().debug("########## 成功取一个交易花费时间(一次循环):{} ", loopOnce);
                loopDebug += (loopOnce - currentTimeMillis);
                chain.getLogger().debug("");
            }
            chain.getLogger().debug("--------------while end----花费时间:{}毫秒-------", loopDebug);
            chain.getLogger().debug("取出的交易packableTxs - Start:");

            try {
                for(int i = 0; i < packingTxList.size();i++){
                    chain.getLogger().debug(i + ": " + ((Transaction) packingTxList.get(i)).hex());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            chain.getLogger().debug("***");
            chain.getLogger().debug("");
            long debugeBatch = NetworkCall.getCurrentTimeMillis();
            //统一验证以及之后的再次验证过滤掉的交易集合
            List<Transaction> filterList = new ArrayList<>();
            txModuleValidatorPackable(chain, moduleVerifyMap, filterList);
            //过滤未通过验证的交易
            filterTx(packingTxList, filterList);
            //清除被过滤掉的交易
            clearInvalidTx(chain, filterList);
            packableTxs = new ArrayList<>();
            Iterator<Transaction> iterator = packingTxList.iterator();
            while (iterator.hasNext()){
                Transaction tx = iterator.next();
                try {
                    packableTxs.add(tx.hex());
                } catch (Exception e) {
                    clearInvalidTx(chain, tx);
                    iterator.remove();
                    throw new NulsException(e);
                }
            }
            //如果有接收新区块,把取出的交易放回到打包队列
            if(chain.getRePackage().get()){
                for (Transaction transaction : packingTxList) {
                    packablePool.addInFirst(chain, transaction, false);
                }
                return getPackableTxs(chain, endtimestamp, maxTxDataSize);
            }
            chain.getLogger().debug("---##########--- 批量验证花费时间:{} ",  NetworkCall.getCurrentTimeMillis() - debugeBatch);
        } catch (NulsException e) {
            //可打包交易,全加回去
            for(Transaction tx : packingTxList){
                packablePool.addInFirst(chain, tx, false);
            }
            chain.getLogger().error(e);
            throw new NulsException(e);
        }

        Log.info("提供给共识的可打包交易packableTxs - Rs:");
        chain.getLogger().debug("***");
        for(int i = 0; i < packableTxs.size();i++){
            chain.getLogger().debug(i + ": " + packableTxs.get(i));
        }
        chain.getLogger().debug("***");
        chain.getLogger().debug("");
        chain.getLogger().debug("获取打包交易结束,当前待打包队列交易数: {} ", packablePool.getPoolSize(chain));
        chain.getLogger().debug("=================================================");

        return packableTxs;
    }

    /**
     * 1.统一验证
     * 2a:如果没有不通过的验证的交易则结束!!
     * 2b.有不通过的验证时，moduleVerifyMap过滤掉不通过的交易.
     * 3.重新验证同一个模块中不通过交易后面的交易(包括单个verify和coinData)，再执行1.递归？
     *
     * @param moduleVerifyMap
     * @param filterList
     */
    private boolean txModuleValidatorPackable(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, List<Transaction> filterList) throws NulsException {
        Iterator<Map.Entry<TxRegister, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<TxRegister, List<String>> entry = it.next();
            List<String> txhashList = null;
            if(entry.getKey().getModuleCode().equals(ModuleE.TX.abbr)){
                //模块统一验证,交易模块,不用调RPC接口
                txhashList = transactionModuleValidator(chain, entry.getValue());
            }else {
                txhashList = TransactionCall.txModuleValidator(chain, entry.getKey().getModuleValidator(), entry.getKey().getModuleCode(), entry.getValue());
            }
            if (null == txhashList || txhashList.size() == 0) {
                //模块统一验证没有冲突的，从map中干掉
                it.remove();
                break;
            }
            //记录冲突的交易，以及对应的索引
            int startIndex = filter(entry.getValue(), txhashList, filterList);
            chain.getLogger().debug("模块统一验证返回的冲突交易,txHashs:{}", Arrays.toString(txhashList.toArray()));
            if (startIndex >= 0) {
                //从模块验证集合中，删除冲突交易,以便重新验证剩下的交易
                chain.getLogger().debug("=========过滤前==========");
                for (String s : entry.getValue()){
                    chain.getLogger().debug(s);
                }
                entry.getValue().remove(startIndex);
                chain.getLogger().debug("=========过滤后==========");
                for (String s : entry.getValue()){
                    chain.getLogger().debug(s);
                }
            }
        }
        if (moduleVerifyMap.isEmpty()) {
            return true;
        }
        verifyAgain(chain, moduleVerifyMap, filterList);
        return txModuleValidatorPackable(chain, moduleVerifyMap, filterList);
    }

    private void verifyAgain(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, List<Transaction> filterList) throws NulsException {
        //已经按模块分组的集合
        Iterator<Map.Entry<TxRegister, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        //向账本模块发送要批量验证coinData的标识
        if(!LedgerCall.coinDataBatchNotify(chain)){
            chain.getLogger().error("Call ledger bathValidateBegin interface failed");
            throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
        }
        while (it.hasNext()) {
            Map.Entry<TxRegister, List<String>> entry = it.next();
            Iterator<String> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                String txHex = iterator.next();
                Transaction tx = TxUtil.getTransaction(txHex);
                //验证tx
                if (!transactionManager.verify(chain, tx)) {
                    filterList.add(tx);
                    iterator.remove();
                    continue;
                }
                //验证coinData
                VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, txHex, true);
                if (!verifyTxResult.success()) {
                    chain.getLogger().debug("*** Debug *** [verifyAgain] " +
                            "coinData not success - code: {}, - reason:{}, type:{} - txhash:{}",
                            verifyTxResult.getCode(),  verifyTxResult.getDesc(), tx.getType(), tx.getHash().getDigestHex());
                    filterList.add(tx);
                    iterator.remove();
                    continue;
                }
            }
        }
    }

    private int filter(List<String> txHexList, List<String> txhashList, List<Transaction> filterList) throws NulsException {
        int startIndex = -1;
        for (int i = 0; i < txHexList.size(); i++) {
            String txHex = txHexList.get(i);
            Transaction tx = TxUtil.getTransaction(txHex);
            for (String txHash : txhashList) {
                if (tx.getHash().equals(NulsDigestData.fromDigestHex(txHash))) {
                    filterList.add(tx);
                    if (startIndex == -1) {
                        startIndex = i;
                    }
                }
            }
        }
        return startIndex;
    }

    /**
     * 从最终要返回的集合中过滤被没通过的交易
     *
     * @param packingTxList
     * @param filterList
     */
    private void filterTx(List<Transaction> packingTxList, List<Transaction> filterList) {
        Iterator<Transaction> it = packingTxList.iterator();
        while (it.hasNext()) {
            Transaction tx = it.next();
            if (filterList.contains(tx)) {
                it.remove();
            }
        }
    }

    @Override
    public VerifyTxResult batchVerify(Chain chain, List<String> txHexList) throws NulsException {
        chain.getLogger().debug("开始区块交易批量验证......");
        VerifyTxResult verifyTxResult = new VerifyTxResult(VerifyTxResult.OTHER_EXCEPTION);
        List<Transaction> txList = new ArrayList<>();
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>();
        for (String txHex : txHexList) {
            //将txHex转换为Transaction对象
            Transaction tx = TxUtil.getTransaction(txHex);
            Transaction transaction = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
            if(null != transaction){
                //交易已存在于已确认块中
                chain.getLogger().debug("batchVerify failed, tx is existed. hash:{}, -type:{}",tx.getHash().getDigestHex(), tx.getType());
                return verifyTxResult;
            }
            txList.add(tx);
            if (tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                CrossTxData crossTxData = TxUtil.getInstance(tx.getTxData(), CrossTxData.class);
                if (crossTxData.getChainId() != chain.getChainId()) {
                    //如果是跨链交易，发起链不是当前链，则核对(跨链验证的结果)
                    CrossTx crossTx = ctxStorageService.getTx(crossTxData.getChainId(), tx.getHash());
                    //todo
                    /**
                     * 核对(跨链验证的结果)
                     */
                    chain.getLogger().debug("batchVerify failed, ctx. hash:{}, -type:{}",tx.getHash().getDigestHex(), tx.getType());
                    return verifyTxResult;
                }
            }
            //验证单个交易
            if (!transactionManager.verify(chain, tx)) {
                chain.getLogger().debug("batchVerify failed, single tx verify failed. hash:{}, -type:{}",tx.getHash().getDigestHex(), tx.getType());
                return verifyTxResult;
            }
            //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
            TxRegister txRegister = transactionManager.getTxRegister(chain, tx.getType());
            if (moduleVerifyMap.containsKey(txRegister)) {
                moduleVerifyMap.get(txRegister).add(txHex);
            } else {
                List<String> txHexs = new ArrayList<>();
                txHexs.add(txHex);
                moduleVerifyMap.put(txRegister, txHexs);
            }
        }
        LedgerCall.coinDataBatchNotify(chain);
        //todo 批量验证coinData，接口和单个的区别？
        for(Transaction tx : txList) {
            verifyTxResult = LedgerCall.verifyCoinData(chain, tx, true);
            if (!verifyTxResult.success()) {
                chain.getLogger().debug("batchVerify failed, batch verifyCoinData failed. hash:{}, -type:{}",tx.getHash().getDigestHex(), tx.getType());
                return verifyTxResult;
            }
        }

        //统一验证
        Iterator<Map.Entry<TxRegister, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        boolean rs = true;
        while (it.hasNext()) {
            Map.Entry<TxRegister, List<String>> entry = it.next();
            List<String> txhashList = null;
            if (entry.getKey().getModuleCode().equals(ModuleE.TX.abbr)) {
                //模块统一验证,交易模块,不用调RPC接口
                txhashList = transactionModuleValidator(chain, entry.getValue());
            } else {
                txhashList = TransactionCall.txModuleValidator(chain, entry.getKey().getModuleValidator(), entry.getKey().getModuleCode(), entry.getValue());
            }
            if(txhashList != null && txhashList.size() > 0){
                rs = false;
                break;
            }
        }

        if(rs){
            for(Transaction tx : txList){
                //如果该交易不在交易管理待打包库中，则进行保存
                if(null == unconfirmedTxStorageService.getTx(chain.getChainId(), tx.getHash())){
                    unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
                    //保存到h2数据库
                    transactionH2Service.saveTxs(TxUtil.tx2PO(tx));
                }
            }
            verifyTxResult.setCode(VerifyTxResult.SUCCESS);
        }
        return verifyTxResult;
    }

    @Override
    public void clearInvalidTx(Chain chain, List<Transaction> txList) {
        for (Transaction tx : txList) {
            clearInvalidTx(chain, tx);
        }
    }

    @Override
    public void clearInvalidTx(Chain chain, Transaction tx) {
        chain.getLogger().debug("---------------------- rollbackClear txHash: " + tx.getHash().getDigestHex());
        unconfirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
        //移除H2交易记录
        chain.getLogger().debug("---------------------- clear H2 -----------------------");
        transactionH2Service.deleteTx(tx);
        chain.getLogger().debug("---------------------- rollbackTxLedger -----------------------\n");
        try {
            //通知账本回滚nonce
            List<String> txHexList = new ArrayList<>();
            txHexList.add(tx.hex());
            LedgerCall.rollBackUnconfirmTx(chain, txHexList);
        } catch (NulsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
