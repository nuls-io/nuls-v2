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
import io.nuls.tools.log.logback.NulsLogger;
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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    private Lock packageLock = new ReentrantLock();

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
        } else {
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

        return verify(chain, tx, true);
    }

    @Override
    public boolean verify(Chain chain, Transaction tx, boolean incloudBasic) {
        try {
            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
            if (incloudBasic) {
                baseValidateTx(chain, tx, txRegister);
            }
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
        //coinBase交易/智能合约退还gas交易没有from
        if (type == TxConstant.TX_TYPE_COINBASE || type == TxConstant.TX_TYPE_CONTRACT_RETURN_GAS) {
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
    private void validateCoinToBase(Chain chain, List<CoinTo> listTo, int type) throws NulsException {
        if (type != TxConstant.TX_TYPE_COINBASE && !TxManager.isSmartContract(chain, type)) {
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
            if (TxUtil.isLegalContractAddress(coinTo.getAddress(), chain)) {
                if (type != TxConstant.TX_TYPE_COINBASE && type != TxConstant.TX_TYPE_CALL_CONTRACT) {
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
        // TODO: 2019/3/22 有效代码 暂时没有启动chain模块
        /** try {
         if (!ChainCall.verifyCtxAsset(chain, tx.hex())) {
         return false;
         }
         } catch (Exception e) {
         chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
         return false;
         }*/
        return true;
    }

    @Override
    public List<String> transactionModuleValidator(Chain chain, List<String> txHexList) throws NulsException {
        //验证不同的交易是否含有相同的CrossTxData(原始交易hash),如果有则不通过
        Map<String, CrossTxData> map = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        List<String> list = new ArrayList<>();
        //todo 有测试代码
        /*int test = 0;*///测试代码 主动制造不通过的情况, 会造成不通过交易之后含有相同地址资产的交易不通过
        for (String hex : txHexList) {
            Transaction tx = TxUtil.getTransaction(hex);
            CrossTxData crossTxData = TxUtil.getInstance(tx.getTxData(), CrossTxData.class);
            if (map.containsValue(crossTxData)) {
                list.add(tx.getHash().getDigestHex());
            }
          /*  if(test == 2){
                list.add(tx.getHash().getDigestHex());
            }
            test++;*/

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
            /**有效代码 占时未使用
             *  if (!ChainCall.ctxAssetCirculateCommit(chain, txHexList, blockHeaderHex)) {
             rs = false;
             break;
             }*/
            successedCoinDataHexs.add(coinDataHex);
        }
        if (rs) {
            //保存生效高度
            BlockHeader blockHeader = TxUtil.getInstance(blockHeaderHex, BlockHeader.class);
            long effectHeight = blockHeader.getHeight() + txConfig.getMainAssetId();
            return confirmedTxStorageService.saveCrossTxEffectList(chain.getChainId(), effectHeight, txHash);
        } else {
            for (String coinDataHex : successedCoinDataHexs) {
                /**有效代码 占时未使用
                 * if (!ChainCall.ctxAssetCirculateRollback(chain, txHexList, blockHeaderHex)) {
                 throw new NulsException(TxErrorCode.FAILED);
                 }*/
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
            /**有效代码 占时未使用
             *  if (!ChainCall.ctxAssetCirculateRollback(chain, txHexList, blockHeaderHex)) {
             rs = false;
             break;
             }*/
            successedCoinDataHexs.add(coinDataHex);
        }
        if (rs) {
            BlockHeader blockHeader = TxUtil.getInstance(blockHeaderHex, BlockHeader.class);
            long effectHeight = blockHeader.getHeight() + txConfig.getMainAssetId();
            return confirmedTxStorageService.removeCrossTxEffectList(chain.getChainId(), effectHeight);
        } else {
            for (String coinDataHex : successedCoinDataHexs) {
                /**有效代码 占时未使用
                 * if (!ChainCall.ctxAssetCirculateCommit(chain, txHexList, blockHeaderHex)) {
                 throw new NulsException(TxErrorCode.FAILED);
                 }*/
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
    public TxPackage getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize, long blockHeight, long blockTime, String packingAddress, String preStateRoot) {
        packageLock.lock();
        NulsLogger nulsLogger = chain.getLoggerMap().get(TxConstant.LOG_TX);
        nulsLogger.debug("");
        nulsLogger.debug("============");
        nulsLogger.debug("========================");
        nulsLogger.debug("=================================================");
        nulsLogger.info("%%%%%%%%% TX开始打包 %%%%%%%%%%%% height:{}", blockHeight);

        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        List<Transaction> packingTxList = new ArrayList<>();
        //记录账本的孤儿交易,返回给共识的时候给过滤出去,因为在因高度变化而导致重新打包的时候,需要还原到待打包队列
        List<Transaction> orphanTxList = new ArrayList<>();
        long totalSize = 0L;
        List<String> packableTxs = null;
        /**
         * 智能合约通知标识
         * 当本次打包过程中,出现的第一个智能合约交易并且调用验证器通过时,
         * 就对智能合约模块进行调用合约的通知,本次打包之后再出现智能合约交易则不会再次通知.
         * 打包时没有智能合约交易则不通知, 有则只第一次时通知.
         */
        boolean contractNotify = false;
        try {
            long startTime = NetworkCall.getCurrentTimeMillis();
            if (!LedgerCall.coinDataBatchNotify(chain)) {
                nulsLogger.error("Call ledger bathValidateBegin interface failed");
                throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
            }
            nulsLogger.info("获取打包交易开始,当前待打包队列交易数: {} , height:{}", packablePool.getPoolSize(chain), blockHeight);
            nulsLogger.debug("--------------while-----------");
            while (true) {
                long currentTimeMillis = NetworkCall.getCurrentTimeMillis();

                //如果本地最新区块+1 大于当前在打包区块的高度, 说明本地最新区块已更新,需要重新打包,把取出的交易放回到打包队列
                if (blockHeight < chain.getBestBlockHeight() + 1) {
                    nulsLogger.debug("获取交易过程中最新区块高度已增长,把取出的交易放回到打包队列, 重新打包...");
                    for (int i = packingTxList.size() - 1; i >= 0; i--) {
                        Transaction transaction = packingTxList.get(i);
                        packablePool.addInFirst(chain, transaction);
                    }
                    currentTimeMillis = NetworkCall.getCurrentTimeMillis();
                    if (endtimestamp - currentTimeMillis <= chain.getConfig().getModuleVerifyOffset()) {
                        nulsLogger.debug("########## 打包时间到: {}, -endtimestamp:{} , -offset:{}",
                                currentTimeMillis, endtimestamp, chain.getConfig().getModuleVerifyOffset());
                        break;
                    }
                    return getPackableTxs(chain, endtimestamp, maxTxDataSize, chain.getBestBlockHeight() + 1, blockTime, packingAddress, preStateRoot);
                }

//                nulsLogger.debug("########## (循环开始)当前网络时间: {} ", currentTimeMillis);
//                nulsLogger.debug("########## 预留的[获取打包交易]结束时间: {}, 还剩{}秒 ", endtimestamp, (endtimestamp - currentTimeMillis)/1000.0);
                if (endtimestamp - currentTimeMillis <= chain.getConfig().getModuleVerifyOffset()) {
                    nulsLogger.debug("########## 打包时间到: {}, -endtimestamp:{} , -offset:{}",
                            currentTimeMillis, endtimestamp, chain.getConfig().getModuleVerifyOffset());
                    break;
                }
                Transaction tx = packablePool.get(chain);
                if (tx == null) {
                    try {
                        //nulsLogger.debug("************* [获取交易等待]");
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        nulsLogger.error("packaging error ", e);
                    }
                    continue;
                }
                nulsLogger.debug("@@@@@ 打包取出交易 hash:{}", tx.getHash().getDigestHex());
                long txSize = tx.size();
                if ((totalSize + txSize) > maxTxDataSize) {
                    packablePool.addInFirst(chain, tx);
                    nulsLogger.debug("交易已达最大容量, 实际值: {} - 预定最大值maxTxDataSize:{}", totalSize + txSize, maxTxDataSize);
                    break;
                }

                String txHex = null;
                try {
                    txHex = tx.hex();
                } catch (Exception e) {
                    nulsLogger.warn(e.getMessage(), e);
                    nulsLogger.info("丢弃获取hex出错交易,txHash:{}, - type:{}, - time:{}", tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    clearInvalidTx(chain, tx);
                    continue;
                }
                //交易业务验证tx
                if (!this.verify(chain, tx, false)) {
                    nulsLogger.info("丢弃验证器未验证通过交易,txHash:{}, - type:{}, - time:{}", tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    clearInvalidTx(chain, tx);
                    continue;
                }
                //批量验证coinData, 单个发送
                VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, txHex, true);
                if (!verifyTxResult.success()) {
                    String nonce = HexUtil.encode(TxUtil.getCoinData(tx).getFrom().get(0).getNonce());
                    nulsLogger.info("coinData打包批量验证未通过 coinData not success - code: {}, - reason:{}, - type:{}, - first coinFrom nonce:{} - txhash:{}",
                            verifyTxResult.getCode(), verifyTxResult.getDesc(), tx.getType(), nonce, tx.getHash().getDigestHex());
                    if (verifyTxResult.getCode() == VerifyTxResult.ORPHAN) {
                        orphanTxList.add(tx);
                    }else{
                        continue;
                    }
                }
                //从已确认的交易中进行重复交易判断
                TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
                if (txConfirmed != null) {
                    nulsLogger.info("丢弃已确认过交易,txHash:{}, - type:{}, - time:{}", tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                    continue;
                }
                packingTxList.add(tx);
                totalSize += txSize;
                /** 智能合约*/
                if (TxManager.isSmartContract(chain, tx.getType())) {
                    /** 出现智能合约,且通知标识为false,则先调用通知 */
                    if (!contractNotify) {
                        ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot);
                        contractNotify = true;
                    }
                    ContractCall.invokeContract(chain, txHex);
                }
                //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
                TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
                nulsLogger.debug("");
            }
            nulsLogger.debug("--------------while end----取出的交易 - size:{}", packingTxList.size());
            long whileTime = NetworkCall.getCurrentTimeMillis() - startTime;
            long batchStart = NetworkCall.getCurrentTimeMillis();
            txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList);

            packableTxs = new ArrayList<>();
            Iterator<Transaction> iterator = packingTxList.iterator();
            while (iterator.hasNext()) {
                Transaction tx = iterator.next();
                if (orphanTxList.contains(tx)) {
                    continue;
                }
                try {
                    packableTxs.add(tx.hex());
                } catch (Exception e) {
                    clearInvalidTx(chain, tx);
                    iterator.remove();
                    throw new NulsException(e);
                }
            }
            String stateRoot = preStateRoot;
            long batchTime = NetworkCall.getCurrentTimeMillis() - batchStart;

            long contractStart = NetworkCall.getCurrentTimeMillis();
            /** 智能合约 当通知标识为true, 则表明有智能合约被调用执行*/
            if (contractNotify) {
                Map<String, Object> map = ContractCall.contractBatchEnd(chain, blockHeight);
                if (null != map) {
                    List<String> scNewList = (List<String>) map.get("txHexList");
                    if (null != scNewList) {
                        packableTxs.addAll(scNewList);
                    }
                    String sr = (String) map.get("stateRoot");
                    if (null != sr) {
                        stateRoot = sr;
                    }
                }
            }
            long contractTime = NetworkCall.getCurrentTimeMillis() - contractStart;
            long totalTime = NetworkCall.getCurrentTimeMillis() - startTime;
            nulsLogger.debug("打包交易时间统计, 开始时间戳:{}, 获取交易(循环)执行时间:{}, 模块统一验证执行时间:{}, 合约执行时间:{}, 总执行时间:{}, 剩余时间:{}",
                    startTime, whileTime, batchTime, contractTime, totalTime, endtimestamp - NetworkCall.getCurrentTimeMillis());
            //检测最新高度
            if (blockHeight < chain.getBestBlockHeight() + 1) {
                //这个阶段已经不够时间再打包,所以直接超时异常处理交易回滚至待打包队列,打空块
                nulsLogger.debug("获取交易完整时,当前最新高度已增长,不够时间重新打包,直接超时异常处理交易回滚至待打包队列,打空块");
                throw new NulsException(TxErrorCode.HEIGHT_UPDATE_UNABLE_TO_REPACKAGE);
            }
            //检测预留传输时间
            long current = NetworkCall.getCurrentTimeMillis();
            if (endtimestamp - current < chain.getConfig().getPackageRpcReserveTime()) {
                //超时,留给最后数据组装和RPC传输时间不足
                nulsLogger.error("getPackableTxs time out, endtimestamp:{}, current:{}, endtimestamp-current:{}, reserveTime:{}",
                        endtimestamp, current, endtimestamp - current, chain.getConfig().getPackageRpcReserveTime());
                throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
            }

            //孤儿交易,全加回待打包队列去
            for (int i = orphanTxList.size() - 1; i >= 0; i--) {
                Transaction tx = orphanTxList.get(i);
                packablePool.addInFirst(chain, tx);
                nulsLogger.debug("获取交易完整时,当前最新高度已增长,不够时间重新打包,直接超时异常处理交易回滚至待打包队列,打空块");
            }
            TxPackage txPackage = new TxPackage(packableTxs, stateRoot, blockHeight);
            nulsLogger.debug("提供给共识的可打包交易packableTxs - size:{}", packableTxs.size());
            nulsLogger.debug("获取打包交易结束,当前待打包队列交易数: {} ", packablePool.getPoolSize(chain));

            nulsLogger.debug("%%%%%%%%% 打包完成 %%%%%%%%%%%% height:{}", blockHeight);
            nulsLogger.debug("=================================================");
            nulsLogger.debug("========================");
            nulsLogger.debug("============");
            nulsLogger.debug("");

            return txPackage;
        } catch (Exception e) {
            nulsLogger.error(e);
            //可打包交易,全加回去
            for (int i = packingTxList.size() - 1; i >= 0; i--) {
                Transaction tx = packingTxList.get(i);
                packablePool.addInFirst(chain, tx);
            }
            return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
        } finally {
            packageLock.unlock();
        }
    }


    /**
     * 1.统一验证
     * 2a:如果没有不通过的验证的交易则结束!!
     * 2b.有不通过的验证时，moduleVerifyMap过滤掉不通过的交易.
     * 3.重新验证同一个模块中不通过交易后面的交易(包括单个verify和coinData)，再执行1.递归？
     *
     * @param moduleVerifyMap
     */
    private boolean txModuleValidatorPackable(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, List<Transaction> packingTxList) throws NulsException {
        Iterator<Map.Entry<TxRegister, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<TxRegister, List<String>> entry = it.next();
            List<String> moduleList = entry.getValue();
            if (moduleList.size() == 0) {
                //当递归中途模块交易被过滤完后会造成list为空,这时不需要再调用模块统一验证器
                it.remove();
                continue;
            }
            List<String> txHashList = null;
            TxRegister txRegister = entry.getKey();
            if (txRegister.getModuleCode().equals(ModuleE.TX.abbr)) {
                //模块统一验证,交易模块,不用调RPC接口
                txHashList = transactionModuleValidator(chain, moduleList);
            } else {
                txHashList = TransactionCall.txModuleValidator(chain, txRegister.getModuleValidator(), txRegister.getModuleCode(), moduleList);
            }
            if (null == txHashList || txHashList.size() == 0) {
                //模块统一验证没有冲突的，从map中干掉
                it.remove();
                break;
            }

            /**冲突检测有不通过的, 执行清除和未确认回滚 从packingTxList删除, 放弃分组?*/
            for (int i = 0; i < txHashList.size(); i++) {
                String hash = txHashList.get(i);
                Iterator<Transaction> its = packingTxList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next();
                    if (hash.equals(tx.getHash().getDigestHex())) {
                        clearInvalidTx(chain, tx);
                        its.remove();
                    }
                }
            }
        }
        if (moduleVerifyMap.isEmpty()) {
            return true;
        }
        moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        verifyAgain(chain, moduleVerifyMap, packingTxList);
        return txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList);
    }


    private void verifyAgain(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, List<Transaction> packingTxList) throws NulsException {
        //向账本模块发送要批量验证coinData的标识
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("%%%%%%%%% verifyAgain 打包再次批量校验通知 %%%%%%%%%%%%");
        if (!LedgerCall.coinDataBatchNotify(chain)) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error("Call ledger bathValidateBegin interface failed");
            throw new NulsException(TxErrorCode.CALLING_REMOTE_INTERFACE_FAILED);
        }
        Iterator<Transaction> it = packingTxList.iterator();
        while (it.hasNext()) {
            Transaction tx = it.next();
            //交易业务验证tx
            if (!this.verify(chain, tx)) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).info("丢弃验证器未验证通过交易,txHash:{}, - type:{}, - time:{}", tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                clearInvalidTx(chain, tx);
                it.remove();
                continue;
            }
            //批量验证coinData, 单个发送
            String txHex = null;
            try {
                txHex = tx.hex();
            } catch (Exception e) {
                throw new NulsException(e);
            }
            VerifyTxResult verifyTxResult = LedgerCall.verifyCoinData(chain, txHex, true);
            if (!verifyTxResult.success()) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).debug("*** Debug *** [verifyAgain] " +
                                "coinData not success - code: {}, - reason:{}, type:{} - txhash:{}",
                        verifyTxResult.getCode(), verifyTxResult.getDesc(), tx.getType(), tx.getHash().getDigestHex());
                clearInvalidTx(chain, tx);
                it.remove();
                continue;
            }
            //从已确认的交易中进行重复交易判断
            TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
            if (txConfirmed != null) {
                chain.getLoggerMap().get(TxConstant.LOG_TX).info("丢弃已确认过交易,txHash:{}, - type:{}, - time:{}", tx.getHash().getDigestHex(), tx.getType(), tx.getTime());
                it.remove();
                continue;
            }
            TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
        }
    }

    @Override
    public VerifyTxResult batchVerify(Chain chain, List<String> txHexList, long blockHeight) throws NulsException {
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("");
        chain.getLoggerMap().get(TxConstant.LOG_TX).debug("开始区块交易批量验证......");
        long s1 = NetworkCall.getCurrentTimeMillis();
        Log.debug("[验区块交易] 高度:{} -开始------------------------------------", blockHeight);//----
        Log.debug("[验区块交易] -开始时间:{}", s1);//----
        VerifyTxResult verifyTxResult = new VerifyTxResult(VerifyTxResult.OTHER_EXCEPTION);
        List<Transaction> txList = new ArrayList<>();
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        /**
         * 智能合约通知标识
         * 当本次打包过程中,出现的第一个智能合约交易并且调用验证器通过时,
         * 就对智能合约模块进行调用合约的通知,本次打包之后再出现智能合约交易则不会再次通知.
         * 打包时没有智能合约交易则不通知, 有则只第一次时通知.
         */
        boolean contractNotify = false;
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

            /** 智能合约*/
//            if (TxManager.isSmartContract(chain, tx.getType())) {
//                /** 出现智能合约,且通知标识为false,则先调用通知 */
//                if (!contractNotify) {
//                    ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot);
//                    contractNotify = true;
//                }
//                ContractCall.invokeContract(chain, txHex);
//            }





            //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
            TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
        }
        Log.debug("[验区块交易] 单个 -(距方法开始的)时间:{}", NetworkCall.getCurrentTimeMillis() - s1);//----
        Log.debug("");//----

        long coinDataV = NetworkCall.getCurrentTimeMillis();//-----
        if (!LedgerCall.verifyBlockTxsCoinData(chain, txHexList, blockHeight)) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("batch verifyCoinData failed.");
            return verifyTxResult;
        }
        Log.debug("[验区块交易] coinData验证时间:{}", NetworkCall.getCurrentTimeMillis() - coinDataV);//----
        Log.debug("[验区块交易] coinData -距方法开始的时间:{}", NetworkCall.getCurrentTimeMillis() - s1);//----
        Log.debug("");//----

        //统一验证
        long moduleV = NetworkCall.getCurrentTimeMillis();//-----
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
        Log.debug("[验区块交易] 模块统一验证时间:{}", NetworkCall.getCurrentTimeMillis() - moduleV);//----
        Log.debug("[验区块交易] 模块统一验证 -距方法开始的时间:{}", NetworkCall.getCurrentTimeMillis() - s1);//----
        Log.debug("");//----
        if (rs) {
            long save = NetworkCall.getCurrentTimeMillis();//-----
            for (Transaction tx : txList) {
                //如果该交易不在交易管理待打包库中，则进行保存
                if (null == unconfirmedTxStorageService.getTx(chain.getChainId(), tx.getHash())) {
                    unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
                    //保存到h2数据库
                    transactionH2Service.saveTxs(TxUtil.tx2PO(chain, tx));
                }
            }
            verifyTxResult.setCode(VerifyTxResult.SUCCESS);
            Log.debug("[验区块交易] 本地不存在的交易保存数据时间:{}", NetworkCall.getCurrentTimeMillis() - save);//----
            Log.debug("[验区块交易] 本地不存在的交易保存数据 -距方法开始的时间:{}", NetworkCall.getCurrentTimeMillis() - s1);//----
            Log.debug("");//----
        }
        if (verifyTxResult.success()) {
            Log.debug("[验区块交易] 通过 -结束时间:{}", NetworkCall.getCurrentTimeMillis() - s1);//----
        } else {
            Log.debug("[验区块交易] 未通过 -结束时间:{}", NetworkCall.getCurrentTimeMillis() - s1);//----
        }
        return verifyTxResult;
    }

    @Override
    public void clearInvalidTx(Chain chain, List<Transaction> txList) {
        if (txList.size() > 0) {// TODO: 2019/3/18 测试代码
            chain.getLoggerMap().get(TxConstant.LOG_TX).info("打包集中清理统一验证过程中未通过交易..");
        }
        for (Transaction tx : txList) {
            clearInvalidTx(chain, tx);
        }
    }

    @Override
    public void clearInvalidTx(Chain chain, Transaction tx) {
        clearInvalidTx(chain, tx, false);
    }

    @Override
    public void clearInvalidTx(Chain chain, Transaction tx, boolean cleanLedgerUfmTx) {
        //判断如果交易已被确认就不用清理了!!
        TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
        if (txConfirmed != null) {
            return;
        }
        unconfirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
        try {
            transactionH2Service.deleteTx(chain, tx);
            if (cleanLedgerUfmTx) {
                //如果是清理机制调用, 则调用账本未确认回滚
                LedgerCall.rollBackUnconfirmTx(chain, tx.hex());
            } else {
                //通知账本状态变更
                LedgerCall.rollbackTxValidateStatus(chain, tx.hex());
            }
        } catch (NulsException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
