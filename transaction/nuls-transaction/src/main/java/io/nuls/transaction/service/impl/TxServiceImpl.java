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
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.constant.TxStatusEnum;
import io.nuls.base.data.*;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.model.BigIntegerUtils;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.*;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.rpc.call.*;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.CtxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.h2.TransactionH2Service;
import io.nuls.transaction.storage.rocksdb.ConfirmedTxStorageService;
import io.nuls.transaction.storage.rocksdb.CtxStorageService;
import io.nuls.transaction.storage.rocksdb.UnconfirmedTxStorageService;
import io.nuls.transaction.storage.rocksdb.UnverifiedTxStorageService;
import io.nuls.transaction.utils.TxUtil;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.transaction.utils.LoggerUtil.Log;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
@Service
public class TxServiceImpl implements TxService {

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

    @Autowired
    private TxConfig txConfig;

    @Override
    public boolean register(Chain chain, TxRegister txRegister) {
        return TxManager.register(chain, txRegister);
    }

    @Override
    public void newTx(Chain chain, Transaction tx) throws NulsException {
        TransactionConfirmedPO txExist = getTransaction(chain, tx.getHash());
        if (null == txExist) {
            unverifiedTxStorageService.putTx(chain, tx);
        }
    }

    @Override
    public TransactionConfirmedPO getTransaction(Chain chain, NulsDigestData hash) {
        Transaction tx = unconfirmedTxStorageService.getTx(chain.getChainId(), hash);
        if (null != tx) {
            return new TransactionConfirmedPO(tx, -1L, TxStatusEnum.UNCONFIRM.getStatus());
        }else{
            return confirmedTxService.getConfirmedTransaction(chain, hash);
        }
    }


    /**
     * 验证交易
     *
     * @param chain
     * @param tx
     * @return
     */
    @Override
    public boolean verify(Chain chain, Transaction tx) {
        try {
            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
            baseValidateTx(chain, tx, txRegister);
            //由于跨链交易直接调模块内部验证器接口，可不通过RPC接口
            if (tx.getType() == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                return this.crossTransactionValidator(chain, tx);
            }
            return TransactionCall.txValidatorProcess(chain, txRegister, tx.hex());
        } catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error("tx type: " + tx.getType(), e);
            return false;
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(TxErrorCode.IO_ERROR.getMsg());
            return false;
        }

    }

    /**
     * 交易基础验证
     * 基础字段
     * 交易size
     * 交易类型
     * 交易签名
     * from的地址必须全部是发起链(本链or相同链）地址
     * from里面的资产是否存在
     * to里面的地址必须是相同链的地址
     * 交易手续费
     *
     * @param chain
     * @param tx
     * @return Result
     */
    private void baseValidateTx(Chain chain, Transaction tx, TxRegister txRegister) throws NulsException {
        if (null == tx) {
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        if (tx.getHash() == null || tx.getHash().size() == 0 || tx.getHash().size() > TxConstant.TX_HASH_DIGEST_BYTE_MAX_LEN) {
            throw new NulsException(TxErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (!TxManager.contain(chain, tx.getType())) {
            throw new NulsException(TxErrorCode.TX_NOT_EFFECTIVE);
        }
        if (tx.getTime() == 0L) {
            throw new NulsException(TxErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (tx.size() > chain.getConfig().getTxMaxSize()) {
            throw new NulsException(TxErrorCode.TX_SIZE_TOO_LARGE);
        }
        //验证签名
        validateTxSignature(tx, txRegister, chain);
        //如果有coinData, 则进行验证,有一些交易没有coinData数据
        if (null != tx.getCoinData() && tx.getCoinData().length > 0) {
            //coinData基础验证以及手续费 (from中所有的nuls资产-to中所有nuls资产)
            CoinData coinData = TxUtil.getCoinData(tx);
            validateCoinFromBase(chain, tx.getType(), coinData.getFrom());
            validateCoinToBase(chain, coinData.getTo(), tx.getType());
            validateFee(chain, tx.getType(), tx.size(), coinData, txRegister);
        } else if (tx.getType() != TxConstant.TX_TYPE_YELLOW_PUNISH && tx.getType() != TxConstant.TX_TYPE_RED_PUNISH) {
            //不是
            // 红黄牌,必有coinData
            throw new NulsException(TxErrorCode.TX_DATA_VALIDATION_ERROR);
        }
    }

    /**
     * 验证签名 只需要验证,需要验证签名的交易(一些系统交易不用签名)
     *
     * @param tx
     * @return
     * @throws NulsException
     */
    private void validateTxSignature(Transaction tx, TxRegister txRegister, Chain chain) throws NulsException {
        //只需要验证,需要验证签名的交易(一些系统交易不用签名)
        if (txRegister.verifySignature) {
            Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chain.getChainId());
            CoinData coinData = TxUtil.getCoinData(tx);
            if (null == coinData || null == coinData.getFrom() || coinData.getFrom().size() <= 0) {
                throw new NulsException(TxErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            //判断from中地址和签名的地址是否匹配
            for (CoinFrom coinFrom : coinData.getFrom()) {
                if (tx.isMultiSignTx()) {
                    MultiSigAccount multiSigAccount = AccountCall.getMultiSigAccount(coinFrom.getAddress());
                    if (null == multiSigAccount) {
                        throw new NulsException(TxErrorCode.ACCOUNT_NOT_EXIST);
                    }
                    for (byte[] bytes : multiSigAccount.getPubKeyList()) {
                        String addr = AddressTool.getStringAddressByBytes(AddressTool.getAddress(bytes, chain.getChainId()));
                        if (!addressSet.contains(addr)) {
                            throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
                        }
                    }
                } else if (!addressSet.contains(AddressTool.getStringAddressByBytes(coinFrom.getAddress()))
                        && tx.getType() != TxConstant.TX_TYPE_STOP_AGENT) {
                    throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
                }
            }
            if (!SignatureUtil.validateTransactionSignture(tx)) {
                throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
            }
        }
    }

    /**
     * 验证交易的付款方数据
     * 1.from中地址对应的链id是否是发起链id
     * 2.验证资产是否存在
     * 3.验证签名数据中的公钥和from中是否匹配, 验证签名正确性
     *
     * @param chain
     * @param listFrom
     * @return Result
     */
    private void validateCoinFromBase(Chain chain, int type, List<CoinFrom> listFrom) throws NulsException {
        //coinBase交易没有from
        if (type == TxConstant.TX_TYPE_COINBASE) {
            return;
        }
        if (null == listFrom || listFrom.size() == 0) {
            throw new NulsException(TxErrorCode.COINFROM_NOT_FOUND);
        }
        int chainId = chain.getConfig().getChainId();
        for (CoinFrom coinFrom : listFrom) {
            byte[] addrBytes = coinFrom.getAddress();
            int addrChainId = AddressTool.getChainIdByAddress(addrBytes);
            int assetsId = coinFrom.getAssetsId();

            //如果不是跨链交易，from中地址对应的链id必须发起链id，跨链交易在验证器中验证
            if (type != TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                if (chainId != addrChainId) {
                    throw new NulsException(TxErrorCode.CHAINID_ERROR);
                }

            }
            //当交易不是转账以及跨链转账时，from的资产必须是该链主资产。(转账以及跨链交易，在验证器中验证资产)
            if (type != TxConstant.TX_TYPE_TRANSFER && type != TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
                if (chain.getConfig().getAssetId() != assetsId) {
                    throw new NulsException(TxErrorCode.ASSETID_ERROR);
                }
            }
            if (chainId == txConfig.getMainChainId()) {
                //如果chainId是主网则通过连管理验证资产是否存在
                //todo
               /* if (!ChainCall.verifyAssetExist(assetsChainId, assetsId)) {
                    throw new NulsException(TxErrorCode.ASSET_NOT_EXIST);
                }*/
            }
            /* 1.没有进行链内转账交易的资产合法性验证(因为可能出现链外资产)，
               2.跨链交易(非主网发起)from地址与发起链匹配的验证，需各验证器进行验证
             */
        }
    }

    /**
     * 验证交易的收款方数据(coinTo是不是属于同一条链)
     * 1.收款方所有地址是不是属于同一条链
     *
     * @param listTo
     * @return Result
     */
    private void validateCoinToBase(Chain chain,List<CoinTo> listTo, int type) throws NulsException {
        if (type != TxConstant.TX_TYPE_COINBASE) {
            if (null == listTo || listTo.size() == 0) {
                throw new NulsException(TxErrorCode.COINTO_NOT_FOUND);
            }
        }
        //验证收款方是不是属于同一条链
        Integer addressChainId = null;
        for (CoinTo coinTo : listTo) {
            int chainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            if (null == addressChainId) {
                addressChainId = chainId;
                continue;
            } else if (addressChainId != chainId) {
                throw new NulsException(TxErrorCode.CROSS_TX_PAYER_CHAINID_MISMATCH);
            }
            if(TxUtil.isLegalContractAddress(coinTo.getAddress(), chain)) {
                if(type != TxConstant.TX_TYPE_COINBASE && type != TxConstant.TX_TYPE_CALL_CONTRACT) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).error("contract data error: The contract does not accept transfers of this type[{}] of transaction.", type);
                    throw new NulsException(TxErrorCode.TX_DATA_VALIDATION_ERROR);
                }
            }
        }

    }


    /**
     * 验证交易手续费是否正确
     *
     * @param chain    链id
     * @param type     tx type
     * @param txSize   tx size
     * @param coinData
     * @return Result
     */
    private void validateFee(Chain chain, int type, int txSize, CoinData coinData, TxRegister txRegister) throws NulsException {
        if (txRegister.getSystemTx()) {
            //系统交易没有手续费
            return;
        }
        //int chainId = chain.getConfig().getChainId();
        BigInteger feeFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : coinData.getFrom()) {
            feeFrom = feeFrom.add(accrueFee(type, chain, coinFrom));
        }
        BigInteger feeTo = BigInteger.ZERO;
        for (CoinTo coinTo : coinData.getTo()) {
            feeFrom = feeFrom.add(accrueFee(type, chain, coinTo));
        }
        //交易中实际的手续费
        BigInteger fee = feeFrom.subtract(feeTo);
        if (BigIntegerUtils.isEqualOrLessThan(fee, BigInteger.ZERO)) {
            Result.getFailed(TxErrorCode.INSUFFICIENT_FEE);
        }
        //根据交易大小重新计算手续费，用来验证实际手续费
        BigInteger targetFee;
        if (type == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
            targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
        } else {
            targetFee = TransactionFeeCalculator.getNormalTxFee(txSize);
        }
        if (BigIntegerUtils.isLessThan(fee, targetFee)) {
            Result.getFailed(TxErrorCode.INSUFFICIENT_FEE);
        }
    }

    /**
     * 累积计算当前coinfrom中可用于计算手续费的资产
     *
     * @param type  tx type
     * @param chain chain id
     * @param coin  coinfrom
     * @return BigInteger
     */
    private BigInteger accrueFee(int type, Chain chain, Coin coin) {
        BigInteger fee = BigInteger.ZERO;
        if (type == TxConstant.TX_TYPE_CROSS_CHAIN_TRANSFER) {
            //为跨链交易时，只算nuls
            if (TxUtil.isNulsAsset(coin)) {
                fee = fee.add(coin.getAmount());
            }
        } else {
            //不为跨链交易时，只算发起链的主资产
            if (TxUtil.isChainAssetExist(chain, coin)) {
                fee = fee.add(coin.getAmount());
            }
        }
        return fee;
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
            if (!ChainCall.verifyCtxAsset(chain, tx.hex())) {
                return false;
            }
        } catch (Exception e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
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
        for (String hex : txHexList) {
            Transaction tx = TxUtil.getTransaction(hex);
            CrossTxData crossTxData = TxUtil.getInstance(tx.getTxData(), CrossTxData.class);
            if (map.containsValue(crossTxData)) {
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
        for (String txHex : txHexList) {
            Transaction tx = TxUtil.getTransaction(txHex);
            txHash.add(tx.getHash());
            String coinDataHex = HexUtil.encode(tx.getCoinData());
            if (!ChainCall.ctxAssetCirculateCommit(chain, txHexList, blockHeaderHex)) {
                rs = false;
                break;
            }
            successedCoinDataHexs.add(coinDataHex);
        }
        if (rs) {
            //保存生效高度
            BlockHeader blockHeader = TxUtil.getInstance(blockHeaderHex, BlockHeader.class);
            long effectHeight = blockHeader.getHeight() + txConfig.getMainAssetId();
            return confirmedTxStorageService.saveCrossTxEffectList(chain.getChainId(), effectHeight, txHash);
        } else {
            for (String coinDataHex : successedCoinDataHexs) {
                if (!ChainCall.ctxAssetCirculateRollback(chain, txHexList, blockHeaderHex)) {
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
        for (String txHex : txHexList) {
            Transaction tx = TxUtil.getTransaction(txHex);
            String coinDataHex = HexUtil.encode(tx.getCoinData());
            if (!ChainCall.ctxAssetCirculateRollback(chain, txHexList, blockHeaderHex)) {
                rs = false;
                break;
            }
            successedCoinDataHexs.add(coinDataHex);
        }
        if (rs) {
            BlockHeader blockHeader = TxUtil.getInstance(blockHeaderHex, BlockHeader.class);
            long effectHeight = blockHeader.getHeight() + txConfig.getMainAssetId();
            return confirmedTxStorageService.removeCrossTxEffectList(chain.getChainId(), effectHeight);
        } else {
            for (String coinDataHex : successedCoinDataHexs) {
                if (!ChainCall.ctxAssetCirculateCommit(chain, txHexList, blockHeaderHex)) {
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
    public TxPackage getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize, long height, long blockTime, String packingAddress, String preStateRoot) throws NulsException {
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("%%%%%%%%% TX开始打包 %%%%%%%%%%%%");
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("%%%%%%%%% getPackableTxs 打包第一次批量校验通知 %%%%%%%%%%%%");
        //重置重新打包标识为false
        chain.getRePackage().set(false);
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        List<Transaction> packingTxList = new ArrayList<>();
        long totalSize = 0L;
        List<String> packableTxs = null;
        try {
            //向账本模块发送要批量验证coinData的标识
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("%%%%%%%%% getPackableTxs 打包第一次批量校验通知 %%%%%%%%%%%%");
            if (!LedgerCall.coinDataBatchNotify(chain)) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).error("Call ledger bathValidateBegin interface failed");
                throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
            }
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("=================================================");
            chain.getLoggerMap().get(TxConstant.LOG_TX).info("获取打包交易开始,当前待打包队列交易数: {} ", packablePool.getPoolSize(chain));
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("交易最大容量: {} ", maxTxDataSize);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("--------------while-----------");
            long loopDebug = NetworkCall.getCurrentTimeMillis();
            while (true) {
                long currentTimeMillis = NetworkCall.getCurrentTimeMillis();
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("");
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## (循环开始)当前网络时间: {} ", currentTimeMillis);
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 预留的[获取打包交易]结束时间: {}, 还剩{}秒 ", endtimestamp, (endtimestamp - currentTimeMillis)/1000.0);
                if (endtimestamp - currentTimeMillis <= chain.getConfig().getModuleVerifyOffset()) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 打包时间到: {}, -endtimestamp:{} , -offset:{}",
                            currentTimeMillis, endtimestamp, chain.getConfig().getModuleVerifyOffset());
                    break;
                }
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 开始获取交易");
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
                    chain.getLoggerMap().get(TxConstant.LOG_TX).debug("交易已达最大容量, 实际值: {} - 预定最大值maxTxDataSize:{}", totalSize + txSize, maxTxDataSize);
                    break;
                }
                //从已确认的交易中进行重复交易判断
                TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
                if (txConfirmed != null) {
                    clearInvalidTx(chain, tx);
                    chain.getLoggerMap().get(TxConstant.LOG_TX).info("丢弃已确认过交易,txHash:{}, - type:{}, - time:{}", tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    continue;
                }
                String txHex = null;
                try {
                    txHex = tx.hex();
                } catch (Exception e) {
                    clearInvalidTx(chain, tx);
                    chain.getLoggerMap().get(TxConstant.LOG_TX).warn(e.getMessage(), e);
                    chain.getLoggerMap().get(TxConstant.LOG_TX).info("丢弃获取hex出错交易,txHash:{}, - type:{}, - time:{}", tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    continue;
                }
                long debugeVerifyStart = NetworkCall.getCurrentTimeMillis();
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 已花费时间:{} ", debugeVerifyStart - currentTimeMillis);
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 开始调用单个验证器, ");
                //交易业务验证tx
                if (!this.verify(chain, tx)) {
                    clearInvalidTx(chain, tx);
                    chain.getLoggerMap().get(TxConstant.LOG_TX).info("丢弃验证器未验证通过交易,txHash:{}, - type:{}, - time:{}", tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    continue;
                }
                long debugeVerifyCoinDataStart = NetworkCall.getCurrentTimeMillis();
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 单个验证器花费时间:{} ", debugeVerifyCoinDataStart - debugeVerifyStart);
                //批量验证coinData, 单个发送
                VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, txHex, true);
                if (!verifyTxResult.success()) {
                    //-----debug 打印第一个coinfrom 的nonce
                    String nonce = HexUtil.encode(TxUtil.getCoinData(tx).getFrom().get(0).getNonce());
                    chain.getLoggerMap().get(TxConstant.LOG_TX).info("丢弃批量验证coinData未通过交易 coinData not success - code: {}, - reason:{}, - type:{}, - first coinFrom nonce:{} - txhash:{}",
                            verifyTxResult.getCode(), verifyTxResult.getDesc(), tx.getType(), nonce, tx.getHash().getDigestHex());
                    continue;
                }
                long debugeMap = NetworkCall.getCurrentTimeMillis();
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 单个VerifyCoinData花费时间:{} ", debugeMap - debugeVerifyCoinDataStart);
                /*if (tx.getType() == 2) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).debug("**************************** 测试未确认垃圾交易回收,对转账交易不打包");
                    continue;
                }*/
                packingTxList.add(tx);
                totalSize += txSize;
                //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
                TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                if (moduleVerifyMap.containsKey(txRegister)) {
                    moduleVerifyMap.get(txRegister).add(txHex);
                } else {
                    List<String> txHexs = new ArrayList<>();
                    txHexs.add(txHex);
                    moduleVerifyMap.put(txRegister, txHexs);
                }
                //如果有接收新区块,把取出的交易放回到打包队列
                if (chain.getRePackage().get()) {
                    for (Transaction transaction : packingTxList) {
                        packablePool.addInFirst(chain, transaction, false);
                    }
                    return getPackableTxs(chain, endtimestamp, maxTxDataSize, height, blockTime, packingAddress, preStateRoot);
                }
                long loopOnce = NetworkCall.getCurrentTimeMillis() - currentTimeMillis;
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 分组花费时间:{} ",  NetworkCall.getCurrentTimeMillis() - debugeMap);
//                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("########## 成功取一个交易花费时间(一次循环):{} ", loopOnce);
                loopDebug += (loopOnce - currentTimeMillis);
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("");
            }
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("--------------while end----花费时间:{}毫秒-------", loopDebug);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("取出的交易packableTxs - Start:");

            try {
                for (int i = 0; i < packingTxList.size(); i++) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).debug(i + ": " + ((Transaction) packingTxList.get(i)).hex());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("***");
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("");
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
            while (iterator.hasNext()) {
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
            if (chain.getRePackage().get()) {
                for (Transaction transaction : packingTxList) {
                    packablePool.addInFirst(chain, transaction, false);
                }
                return getPackableTxs(chain, endtimestamp, maxTxDataSize, height, blockTime, packingAddress, preStateRoot);
            }
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("---##########--- 批量验证花费时间:{} ", NetworkCall.getCurrentTimeMillis() - debugeBatch);

            /* 处理智能合约部分*/
            List<String> scTxHexList = new ArrayList<>();
            for(Transaction tx : packingTxList) {
                if(TxManager.isSmartContract(chain, tx.getType())){
                    try {
                        scTxHexList.add(tx.hex());
                    } catch (Exception e) {
                        clearInvalidTx(chain, tx);
                        iterator.remove();
                        throw new NulsException(e);
                    }
                }
            }
            String stateRoot = null;
            if(scTxHexList.size() > 0) {
                Map<String, Object> map = ContractCall.invokeContract(chain, scTxHexList);
                List<String> scNewList = (List<String>) map.get("txHexList");
                packableTxs.addAll(scNewList);
                stateRoot = (String) map.get("txHexList");
            }
            TxPackage txPackage = new TxPackage(packableTxs, stateRoot);

            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("提供给共识的可打包交易packableTxs - Rs:");
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("***");
            for (int i = 0; i < packableTxs.size(); i++) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug(i + ": " + packableTxs.get(i));
            }
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("***");
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("");
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("获取打包交易结束,当前待打包队列交易数: {} ", packablePool.getPoolSize(chain));
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("=================================================");
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("%%%%%%%%% 打包完成 %%%%%%%%%%%%");
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("");

            return txPackage;
        } catch (NulsException e) {
            //可打包交易,全加回去
            for (Transaction tx : packingTxList) {
                packablePool.addInFirst(chain, tx, false);
            }
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            throw new NulsException(e);
        }

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
            if (entry.getValue().size() == 0) {
                //当递归中途模块交易被过滤完后会造成list为空,这时不需要再调用模块同意验证器
                continue;
            }
            List<String> txhashList = null;
            if (entry.getKey().getModuleCode().equals(ModuleE.TX.abbr)) {
                //模块统一验证,交易模块,不用调RPC接口
                txhashList = transactionModuleValidator(chain, entry.getValue());
            } else {
                txhashList = TransactionCall.txModuleValidator(chain, entry.getKey().getModuleValidator(), entry.getKey().getModuleCode(), entry.getValue());
            }
            if (null == txhashList || txhashList.size() == 0) {
                //模块统一验证没有冲突的，从map中干掉
                it.remove();
                break;
            }
            //记录冲突的交易，以及对应的索引
            int startIndex = filter(entry.getValue(), txhashList, filterList);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("模块统一验证返回的冲突交易,txHashs:{}", Arrays.toString(txhashList.toArray()));
            if (startIndex >= 0) {
                //从模块验证集合中，删除冲突交易,以便重新验证剩下的交易
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("=========过滤前==========");
                for (String s : entry.getValue()) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).debug(s);
                }
                entry.getValue().remove(startIndex);
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("=========过滤后==========");
                for (String s : entry.getValue()) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).debug(s);
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
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("%%%%%%%%% verifyAgain 打包再次批量校验通知 %%%%%%%%%%%%");
        if (!LedgerCall.coinDataBatchNotify(chain)) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error("Call ledger bathValidateBegin interface failed");
            throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
        }
        while (it.hasNext()) {
            Map.Entry<TxRegister, List<String>> entry = it.next();
            Iterator<String> iterator = entry.getValue().iterator();
            while (iterator.hasNext()) {
                String txHex = iterator.next();
                Transaction tx = TxUtil.getTransaction(txHex);
                //验证tx
                if (!this.verify(chain, tx)) {
                    filterList.add(tx);
                    iterator.remove();
                    continue;
                }
                //验证coinData
                VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, txHex, true);
                if (!verifyTxResult.success()) {
                    chain.getLoggerMap().get(TxConstant.LOG_TX).debug("*** Debug *** [verifyAgain] " +
                                    "coinData not success - code: {}, - reason:{}, type:{} - txhash:{}",
                            verifyTxResult.getCode(), verifyTxResult.getDesc(), tx.getType(), tx.getHash().getDigestHex());
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
    public VerifyTxResult batchVerify(Chain chain, List<String> txHexList, long blockHeight) throws NulsException {
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("");
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("开始区块交易批量验证......");
        VerifyTxResult verifyTxResult = new VerifyTxResult(VerifyTxResult.OTHER_EXCEPTION);
        List<Transaction> txList = new ArrayList<>();
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>();
        for (String txHex : txHexList) {
            //将txHex转换为Transaction对象
            Transaction tx = TxUtil.getTransaction(txHex);
            TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
            if (null != txConfirmed) {
                //交易已存在于已确认块中
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("batchVerify failed, tx is existed. hash:{}, -type:{}", tx.getHash().getDigestHex(), tx.getType());
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
                    chain.getLoggerMap().get(TxConstant.LOG_TX).debug("batchVerify failed, ctx. hash:{}, -type:{}", tx.getHash().getDigestHex(), tx.getType());
                    return verifyTxResult;
                }
            }
            //验证单个交易
            if (!this.verify(chain, tx)) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("batchVerify failed, single tx verify failed. hash:{}, -type:{}", tx.getHash().getDigestHex(), tx.getType());
                return verifyTxResult;
            }
            //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
            if (moduleVerifyMap.containsKey(txRegister)) {
                moduleVerifyMap.get(txRegister).add(txHex);
            } else {
                List<String> txHexs = new ArrayList<>();
                txHexs.add(txHex);
                moduleVerifyMap.put(txRegister, txHexs);
            }
        }
        if(!LedgerCall.verifyBlockTxsCoinData(chain, txHexList, blockHeight)){
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("batch verifyCoinData failed.");
            return verifyTxResult;
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
            if (txhashList != null && txhashList.size() > 0) {
                rs = false;
                break;
            }
        }

        if (rs) {
            for (Transaction tx : txList) {
                //如果该交易不在交易管理待打包库中，则进行保存
                if (null == unconfirmedTxStorageService.getTx(chain.getChainId(), tx.getHash())) {
                    unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
                    //保存到h2数据库
                    transactionH2Service.saveTxs(TxUtil.tx2PO(chain,tx));
//                    TxUtil.txInformationDebugPrint(chain, tx);
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
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("---------------------- rollbackClear txHash: " + tx.getHash().getDigestHex());
        unconfirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
        //移除H2交易记录
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("---------------------- clear H2 -----------------------");
        try {
            transactionH2Service.deleteTx(chain, tx);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("---------------------- rollbackTxLedger -----------------------\n");
            //通知账本回滚nonce
            LedgerCall.rollBackUnconfirmTx(chain, tx.hex());
        } catch (NulsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
