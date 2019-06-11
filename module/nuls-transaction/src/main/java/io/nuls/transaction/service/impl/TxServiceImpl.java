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

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.protocol.TxRegisterDetail;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.TxWrapper;
import io.nuls.transaction.model.bo.*;
import io.nuls.transaction.model.dto.ModuleTxRegisterDTO;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.model.po.TransactionUnconfirmedPO;
import io.nuls.transaction.rpc.call.*;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.ConfirmedTxStorageService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.threadpool.NetTxProcessJob;
import io.nuls.transaction.threadpool.NetTxThreadPoolExecutor;
import io.nuls.transaction.utils.TxDuplicateRemoval;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static io.nuls.transaction.constant.TxConstant.CACHED_SIZE;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
@Component
public class TxServiceImpl implements TxService {

    @Autowired
    private PackablePool packablePool;

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    @Autowired
    private ConfirmedTxService confirmedTxService;

    @Autowired
    private ConfirmedTxStorageService confirmedTxStorageService;

    @Autowired
    private TxConfig txConfig;


    private ExecutorService verifySignExecutor = ThreadUtils.createThreadPool(Runtime.getRuntime().availableProcessors(), CACHED_SIZE, new NulsThreadFactory(TxConstant.VERIFY_TX_SIGN_THREAD));
    private ExecutorService clearTxExecutor = ThreadUtils.createThreadPool(1, CACHED_SIZE, new NulsThreadFactory(TxConstant.CLEAN_INVALID_TX_THREAD));

    @Override
    public boolean register(Chain chain, ModuleTxRegisterDTO moduleTxRegisterDto) {
        try {
            for (TxRegisterDetail txRegisterDto : moduleTxRegisterDto.getList()) {
                TxRegister txRegister = new TxRegister();
                txRegister.setModuleCode(moduleTxRegisterDto.getModuleCode());
                txRegister.setTxType(txRegisterDto.getTxType());
                txRegister.setSystemTx(txRegisterDto.getSystemTx());
                txRegister.setUnlockTx(txRegisterDto.getUnlockTx());
                txRegister.setVerifySignature(txRegisterDto.getVerifySignature());
                txRegister.setVerifyFee(txRegisterDto.getVerifyFee());
                chain.getTxRegisterMap().put(txRegister.getTxType(), txRegister);
                chain.getLogger().info("register:{}", JSONUtils.obj2json(txRegister));
            }
            moduleTxRegisterDto.getDelList().forEach(e -> chain.getTxRegisterMap().remove(e));
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
        return false;
    }

    @Override
    public boolean unregister(Chain chain, String moduleCode) {
        try {
            Iterator<Map.Entry<Integer, TxRegister>> it = chain.getTxRegisterMap().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Integer, TxRegister> entry = it.next();
                if (moduleCode.equals(entry.getValue().getModuleCode())) {
                    it.remove();
                }
            }
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    @Override
    public void newBroadcastTx(Chain chain, TransactionNetPO txNet) {
        if (!isTxExists(chain, txNet.getTx().getHash())) {
            try {
                //chain.getUnverifiedQueue().addLast(txNet);
                NetTxProcessJob netTxProcessJob = new NetTxProcessJob(chain, txNet);
                NetTxThreadPoolExecutor threadPool = chain.getNetTxThreadPoolExecutor();
                threadPool.execute(netTxProcessJob);
            } catch (IllegalStateException e) {
                chain.getLogger().error("UnverifiedQueue full!");
            }
        }
    }


    @Override
    public void newTx(Chain chain, Transaction tx) throws NulsException {
        try {
            NulsHash hash = tx.getHash();
            TransactionConfirmedPO existTx = getTransaction(chain, hash);
            if (null == existTx) {
                VerifyResult verifyResult = verify(chain, tx);
                if (!verifyResult.getResult()) {
                    chain.getLogger().error("verify failed: type:{} - txhash:{}, code:{}",
                            tx.getType(), hash.toHex(), verifyResult.getErrorCode().getCode());
                    throw new NulsException(ErrorCode.init(verifyResult.getErrorCode().getCode()));
                }
                VerifyLedgerResult verifyLedgerResult = LedgerCall.commitUnconfirmedTx(chain, RPCUtil.encode(tx.serialize()));
                if (!verifyLedgerResult.businessSuccess()) {

                    String errorCode = verifyLedgerResult.getErrorCode() == null ? TxErrorCode.ORPHAN_TX.getCode() : verifyLedgerResult.getErrorCode().getCode();
                    chain.getLogger().error(
                            "coinData verify fail - orphan: {}, - code:{}, type:{} - txhash:{}", verifyLedgerResult.getOrphan(),
                            errorCode, tx.getType(), hash.toHex());
                    throw new NulsException(ErrorCode.init(errorCode));
                }
                if (chain.getPackaging().get()) {
                    packablePool.add(chain, tx);
                }
                unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
                //广播完整交易
                NetworkCall.broadcastTx(chain, tx);
                //加入去重过滤集合,防止其他节点转发回来再次处理该交易
                TxDuplicateRemoval.insertAndCheck(hash.toHex());
            }
        } catch (IOException e) {
            throw new NulsException(TxErrorCode.DESERIALIZE_ERROR);
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    @Override
    public TransactionConfirmedPO getTransaction(Chain chain, NulsHash hash) {
        TransactionUnconfirmedPO txPo = unconfirmedTxStorageService.getTx(chain.getChainId(), hash);
        if (null != txPo) {
            return new TransactionConfirmedPO(txPo.getTx(), -1L, TxStatusEnum.UNCONFIRM.getStatus(), txPo.getOriginalSendNanoTime());
        } else {
            return confirmedTxService.getConfirmedTransaction(chain, hash);
        }
    }

    @Override
    public boolean isTxExists(Chain chain, NulsHash hash) {
        boolean rs = unconfirmedTxStorageService.isExists(chain.getChainId(), hash);
        if (!rs) {
            rs = confirmedTxStorageService.isExists(chain.getChainId(), hash);
        }
        return rs;
    }

    /**
     * 验证交易
     *
     * @param chain
     * @param tx
     * @return
     */
    @Override
    public VerifyResult verify(Chain chain, Transaction tx) {
        return verify(chain, tx, true);
    }

    @Override
    public VerifyResult verify(Chain chain, Transaction tx, boolean incloudBasic) {
        try {
            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
            if (null == txRegister) {
                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
            }
            if (incloudBasic) {
                baseValidateTx(chain, tx, txRegister);
            }
            List<String> txHashList = TransactionCall.txModuleValidator(chain, txRegister.getModuleCode(), RPCUtil.encode(tx.serialize()));
            if (txHashList.isEmpty()) {
                return VerifyResult.success();
            } else {
                chain.getLogger().error("tx validator fail -type:{}, -hash:{} ", tx.getType(), tx.getHash().toHex());
                return VerifyResult.fail(TxErrorCode.SYS_UNKOWN_EXCEPTION);
            }
        } catch (IOException e) {
            return VerifyResult.fail(TxErrorCode.SERIALIZE_ERROR);
        } catch (NulsException e) {
            chain.getLogger().error("tx type: " + tx.getType(), e);
            return VerifyResult.fail(e.getErrorCode());
        } catch (Exception e) {
            return VerifyResult.fail(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @Override
    public void baseValidateTx(Chain chain, Transaction tx, TxRegister txRegister) throws NulsException {
        if (null == tx) {
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        if (tx.getHash() == null || !tx.getHash().verify()) {
            throw new NulsException(TxErrorCode.HASH_ERROR);
        }
        if (!TxManager.contains(chain, tx.getType())) {
            throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
        }
        if (tx.getTime() == 0L) {
            throw new NulsException(TxErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (tx.size() > chain.getConfig().getTxMaxSize()) {
            throw new NulsException(TxErrorCode.TX_SIZE_TOO_LARGE);
        }
        //验证签名
        validateTxSignature(tx, txRegister, chain);
        //如果有coinData, 则进行验证,有一些交易(黄牌)没有coinData数据
        if (tx.getType() == TxType.YELLOW_PUNISH) {
            return;
        }
        //coinData基础验证以及手续费 (from中所有的nuls资产-to中所有nuls资产)
        CoinData coinData = TxUtil.getCoinData(tx);
        validateCoinFromBase(chain, tx.getType(), coinData.getFrom());
        validateCoinToBase(chain, coinData.getTo(), tx.getType());
        if (txRegister.getVerifyFee()) {
            validateFee(chain, tx.getType(), tx.size(), coinData, txRegister);
        }
    }

    /**
     * 验证签名 只需要验证,需要验证签名的交易(一些系统交易不用签名)
     * 验证签名数据中的公钥和from中是否匹配, 验证签名正确性
     *
     * @param tx
     * @return
     * @throws NulsException
     */
    private void validateTxSignature(Transaction tx, TxRegister txRegister, Chain chain) throws NulsException {
        //只需要验证,需要验证签名的交易(一些系统交易不用签名)
        if (txRegister.getVerifySignature()) {
            Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chain.getChainId());
            CoinData coinData = TxUtil.getCoinData(tx);
            if (null == coinData || null == coinData.getFrom() || coinData.getFrom().size() <= 0) {
                throw new NulsException(TxErrorCode.COINDATA_NOT_FOUND);
            }
            if (!txRegister.getModuleCode().equals(ModuleE.CC.abbr)) {
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
                            && tx.getType() != TxType.STOP_AGENT) {
                        throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
                    }
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
     * 2.
     *
     * @param chain
     * @param listFrom
     * @return Result
     */
    //// TODO: 2019/4/19 多签地址交易是否只允许一个多签地址(from), 手续费可能导致两个from
    private void validateCoinFromBase(Chain chain, int type, List<CoinFrom> listFrom) throws NulsException {
        //coinBase交易/智能合约退还gas交易没有from
        if (type == TxType.COIN_BASE || type == TxType.CONTRACT_RETURN_GAS) {
            return;
        }
        if (null == listFrom || listFrom.size() == 0) {
            throw new NulsException(TxErrorCode.COINFROM_NOT_FOUND);
        }
        int chainId = chain.getConfig().getChainId();
        //验证支付方是不是属于同一条链
        Integer fromChainId = null;
        Set<String> uniqueCoin = new HashSet<>();
        for (CoinFrom coinFrom : listFrom) {
            byte[] addrBytes = coinFrom.getAddress();
            int addrChainId = AddressTool.getChainIdByAddress(addrBytes);
            int assetsId = coinFrom.getAssetsId();
            if (coinFrom.getAmount().compareTo(BigInteger.ZERO) < 0) {
                throw new NulsException(TxErrorCode.DATA_ERROR);
            }
            //所有from是否是同一条链的地址
            if (null == fromChainId) {
                fromChainId = addrChainId;
            } else if (fromChainId != addrChainId) {
                throw new NulsException(TxErrorCode.COINFROM_NOT_SAME_CHAINID);
            }
            //如果不是跨链交易，from中地址对应的链id必须发起链id，跨链交易在验证器中验证
            if (type != TxType.CROSS_CHAIN) {
                if (chainId != addrChainId) {
                    throw new NulsException(TxErrorCode.FROM_ADDRESS_NOT_MATCH_CHAIN);
                }
            }
            //todo 各个交易验证器自行验证 当交易不是转账以及跨链转账时，from的资产必须是该链主资产。(转账以及跨链交易，在验证器中验证资产)
            /*if (type != TxType.TRANSFER && type != TxType.CROSS_CHAIN) {
                if (chain.getConfig().getAssetId() != assetsId) {
                    throw new NulsException(TxErrorCode.ASSET_ERROR);
                }
            }*/
            //验证账户地址,资产链id,资产id的组合唯一性
            int assetsChainId = coinFrom.getAssetsChainId();
            boolean rs = uniqueCoin.add(AddressTool.getStringAddressByBytes(coinFrom.getAddress()) + "-" + assetsChainId + "-" + assetsId + "-" + HexUtil.encode(coinFrom.getNonce()));
            if (!rs) {
                throw new NulsException(TxErrorCode.COINFROM_HAS_DUPLICATE_COIN);
            }
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
        TxRegister txRegister = TxManager.getTxRegister(chain, type);
        String moduleCode = null;
        if (txRegister != null) {
            moduleCode = txRegister.getModuleCode();
        }
        if (type != TxType.COIN_BASE && !ModuleE.SC.abbr.equals(moduleCode)) {
            if (null == listTo || listTo.size() == 0) {
                throw new NulsException(TxErrorCode.COINTO_NOT_FOUND);
            }
        }
        //验证收款方是不是属于同一条链
        Integer addressChainId = null;
        int txChainId = chain.getChainId();
        Set<String> uniqueCoin = new HashSet<>();
        for (CoinTo coinTo : listTo) {
            int chainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            if (null == addressChainId) {
                addressChainId = chainId;
            } else if (addressChainId != chainId) {
                throw new NulsException(TxErrorCode.COINTO_NOT_SAME_CHAINID);
            }
            if (coinTo.getAmount().compareTo(BigInteger.ZERO) < 0) {
                throw new NulsException(TxErrorCode.DATA_ERROR);
            }
            //如果不是跨链交易，to中地址对应的链id必须发起交易的链id
            if (type != TxType.CROSS_CHAIN) {
                if (chainId != txChainId) {
                    throw new NulsException(TxErrorCode.TO_ADDRESS_NOT_MATCH_CHAIN);
                }
            }
            int assetsChainId = coinTo.getAssetsChainId();
            int assetsId = coinTo.getAssetsId();
            long lockTime = coinTo.getLockTime();
            //to里面地址、资产链id、资产id、锁定时间的组合不能重复
            boolean rs = uniqueCoin.add(AddressTool.getStringAddressByBytes(coinTo.getAddress()) + "-" + assetsChainId + "-" + assetsId + "-" + lockTime);
            if (!rs) {
                throw new NulsException(TxErrorCode.COINTO_HAS_DUPLICATE_COIN);
            }

            if (TxUtil.isLegalContractAddress(coinTo.getAddress(), chain)) {
                if (!txRegister.getSystemTx()
                        && type != TxType.COIN_BASE
                        && type != TxType.CALL_CONTRACT
                        && type != TxType.STOP_AGENT) {
                    chain.getLogger().error("contract data error: The contract does not accept transfers of this type[{}] of transaction.", type);
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
        BigInteger feeFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : coinData.getFrom()) {
            feeFrom = feeFrom.add(accrueFee(type, chain, coinFrom));
        }
        BigInteger feeTo = BigInteger.ZERO;
        for (CoinTo coinTo : coinData.getTo()) {
            feeTo = feeTo.add(accrueFee(type, chain, coinTo));
        }
        //交易中实际的手续费
        BigInteger fee = feeFrom.subtract(feeTo);
        if (BigIntegerUtils.isEqualOrLessThan(fee, BigInteger.ZERO)) {
            throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
        }
        //根据交易大小重新计算手续费，用来验证实际手续费
        BigInteger targetFee;
        if (type == TxType.CROSS_CHAIN) {
            targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
        } else {
            targetFee = TransactionFeeCalculator.getNormalTxFee(txSize);
        }
        if (BigIntegerUtils.isLessThan(fee, targetFee)) {
            throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
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
        BigInteger feeAsset = BigInteger.ZERO;
        if (type == TxType.CROSS_CHAIN && AddressTool.getChainIdByAddress(coin.getAddress()) != chain.getChainId()) {
            //为跨链交易时，只算nuls
            if (TxUtil.isNulsAsset(coin)) {
                feeAsset = feeAsset.add(coin.getAmount());
            }
        } else {
            //不为跨链交易时，只算发起链的主资产
            if (TxUtil.isChainAssetExist(chain, coin)) {
                feeAsset = feeAsset.add(coin.getAmount());
            }
        }
        return feeAsset;
    }

    /**
     * 1.按时间取出交易执行时间为endtimestamp-500，预留500毫秒给统一验证，
     * 2.取交易同时执行交易验证，然后coinData的验证(先发送开始验证的标识)
     * 3.冲突检测，模块统一验证，如果有没验证通过的交易，则将该交易之后的所有交易再从1.开始执行一次
     */
//    @Override
    public TxPackage getPackableTxs_TEMP(Chain chain, long endtimestamp, long maxTxDataSize, long blockHeight, long blockTime, String packingAddress, String preStateRoot) {
        chain.getPackageLock().lock();
        long startTime = NulsDateUtils.getCurrentTimeMillis();
        NulsLogger nulsLogger = chain.getLogger();
        nulsLogger.info("");
        nulsLogger.info("");
        nulsLogger.info("");
        nulsLogger.info("");
        nulsLogger.info("[Transaction Package start] -可打包时间：[{}], -可打包容量：[{}]B , - height:[{}], - 当前待打包队列交易数:[{}] ",
                endtimestamp - startTime, maxTxDataSize, blockHeight, packablePool.packableHashQueueSize(chain));
        //重置标志
        chain.setContractTxFail(false);
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        List<TxWrapper> packingTxList = new ArrayList<>();
        //记录账本的孤儿交易,返回给共识的时候给过滤出去,因为在因高度变化而导致重新打包的时候,需要还原到待打包队列
        Set<TxWrapper> orphanTxSet = new HashSet<>();
        long packingTime = endtimestamp - startTime;
        //统计待打包队列中取出的已确认的交易数量
        int confirmedTxCount = 0;
        int confirmedTxTime = 0;
        //统计总等待时间
        int allSleepTime = 0;
        //循环获取交易使用时间
        long whileTime;
        //验证账本总时间
        long totalLedgerTime = 0;
        //模块统一验证使用总时间
        long batchModuleTime;
        long totalSize = 0L;
        /**
         * 智能合约通知标识
         * 当本次打包过程中,出现的第一个智能合约交易并且调用验证器通过时,
         * 就对智能合约模块进行调用合约的通知,本次打包之后再出现智能合约交易则不会再次通知.
         * 打包时没有智能合约交易则不通知, 有则只第一次时通知.
         */
        boolean contractNotify = false;
        try {

            //通过配置的百分比，计算从总的打包时间中预留给批量验证的时间
            float batchValidReserveTemp = (chain.getConfig().getModuleVerifyPercent() / 100.0f) * (endtimestamp - startTime);
            long batchValidReserve = (long) batchValidReserveTemp;
            //向账本模块发送要批量验证coinData的标识
            LedgerCall.coinDataBatchNotify(chain);
            for (int index = 0; ; index++) {
                long currentTimeMillis = NulsDateUtils.getCurrentTimeMillis();
                //// TODO: 2019/6/5 临时处理 如果交易容量增大，统一验证预留时间需要加大
                if (totalSize >= ((Double) (maxTxDataSize / 2.5)).longValue()) {
                    batchValidReserve = (long) batchValidReserveTemp * 3;
                }
                if (endtimestamp - currentTimeMillis <= batchValidReserve) {
                    nulsLogger.debug("获取交易时间到,进入模块验证阶段: currentTimeMillis:[{}], -endtimestamp:[{}], -offset:[{}], -remaining:[{}]",
                            currentTimeMillis, endtimestamp, batchValidReserve, endtimestamp - currentTimeMillis);
                    break;
                }
                if (chain.getProtocolUpgrade().get()) {
                    nulsLogger.info("[Transaction Package start]  - Protocol Upgrade Package stop -chain:[{}] -best block height", chain.getChainId(), chain.getBestBlockHeight());
                    //放回可打包交易和孤儿
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    //直接打空块
                    return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
                }
                //如果本地最新区块+1 大于当前在打包区块的高度, 说明本地最新区块已更新,需要重新打包,把取出的交易放回到打包队列
                if (blockHeight < chain.getBestBlockHeight() + 1) {
                    nulsLogger.info("获取交易过程中最新区块高度已增长,把取出的交易以及孤儿放回到打包队列, 重新打包...");
                    //放回可打包交易和孤儿
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    return getPackableTxs(chain, endtimestamp, maxTxDataSize, chain.getBestBlockHeight() + 1, blockTime, packingAddress, preStateRoot);
                }
                Transaction tx = packablePool.poll(chain);
                if (tx == null) {
                    Thread.sleep(30L);
                    allSleepTime += 30;
                    continue;
                }
                TxWrapper txWrapper = new TxWrapper(tx, index);
                long txSize = tx.size();
                if ((totalSize + txSize) > maxTxDataSize) {
                    packablePool.offerFirst(chain, tx);
                    nulsLogger.info("交易已达最大容量, 实际值: {} - 预定最大值maxTxDataSize:{}", totalSize + txSize, maxTxDataSize);
                    break;
                }

                String txStr = null;
                try {
                    txStr = RPCUtil.encode(tx.serialize());
                } catch (Exception e) {
                    nulsLogger.warn(e.getMessage(), e);
                    nulsLogger.error("丢弃获取hex出错交易,txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                    clearInvalidTx(chain, tx);
                    continue;
                }
                long s2 = NulsDateUtils.getCurrentTimeMillis();
                //批量验证coinData, 单个发送
                VerifyLedgerResult verifyLedgerResult = LedgerCall.verifyCoinDataPackaged(chain, txStr);
                totalLedgerTime += NulsDateUtils.getCurrentTimeMillis() - s2;
                if (!verifyLedgerResult.businessSuccess()) {
                    String nonce = HexUtil.encode(TxUtil.getCoinData(tx).getFrom().get(0).getNonce());
                    nulsLogger.error("coinData 打包批量验证未通过 verify fail - orphan: {}, - code:{}, type:{}, - first coinFrom nonce:{}  - txhash:{}", verifyLedgerResult.getOrphan(),
                            verifyLedgerResult.getErrorCode() == null ? "" : verifyLedgerResult.getErrorCode().getCode(),
                            tx.getType(), nonce, tx.getHash().toHex());
                    if (verifyLedgerResult.getOrphan()) {
                        addOrphanTxSet(chain, orphanTxSet, txWrapper);
                    }

                    continue;
                }

                /** 智能合约*/
                if (TxManager.isSmartContract(chain, tx.getType())) {
                    /** 出现智能合约,且通知标识为false,则先调用通知 */
                    if (!contractNotify) {
                        ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot);
                        contractNotify = true;
                    }
                    if (!ContractCall.invokeContract(chain, txStr)) {
                        clearInvalidTx(chain, tx);
                        continue;
                    }
                }
                packingTxList.add(txWrapper);
                totalSize += txSize;
                //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
                TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
            }
            nulsLogger.debug("-取出的交易 - totalSize:{}", totalSize);

            boolean contractBefore = false;
            if (contractNotify) {
                contractBefore = ContractCall.contractBatchBeforeEnd(chain, blockHeight);
            }
            //循环获取交易使用时间
            whileTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            long batchStart = NulsDateUtils.getCurrentTimeMillis();
            txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList, orphanTxSet);
            //模块统一验证使用总时间
            batchModuleTime = NulsDateUtils.getCurrentTimeMillis() - batchStart;

            String stateRoot = preStateRoot;
            long contractStart = NulsDateUtils.getCurrentTimeMillis();
            /** 智能合约 当通知标识为true, 则表明有智能合约被调用执行*/
            List<String> contractGenerateTxs = new ArrayList<>();
            if (contractNotify && !chain.getContractTxFail()) {
                /**当contractBefore通知失败,或者contractBatchEnd失败则需要将智能合约交易还回待打包队列*/
                boolean isRollbackPackablePool = false;
                if (!contractBefore) {
                    isRollbackPackablePool = true;
                } else {
                    try {
                        Map<String, Object> map = ContractCall.contractBatchEnd(chain, blockHeight);
                        List<String> scNewList = (List<String>) map.get("txList");
                        if (null != scNewList) {
                            /**
                             * 1.共识验证 如果有
                             * 2.如果只有智能合约的共识交易失败，isRollbackPackablePool=true
                             * 3.如果只有其他共识交易失败，单独删掉
                             * 4.混合 执行2.
                             */
                            List<String> scNewConsensusList = new ArrayList<>();
                            for (String scNewTx : scNewList) {
                                int scNewTxType = TxUtil.extractTxTypeFromTx(scNewTx);
                                if (scNewTxType == TxType.CONTRACT_CREATE_AGENT
                                        || scNewTxType == TxType.CONTRACT_DEPOSIT
                                        || scNewTxType == TxType.CONTRACT_CANCEL_DEPOSIT
                                        || scNewTxType == TxType.CONTRACT_STOP_AGENT) {
                                    scNewConsensusList.add(scNewTx);
                                }
                            }
                            if (!scNewConsensusList.isEmpty()) {
                                //收集共识模块所有交易, 加上新产生的智能合约共识交易，一起再次进行模块统一验证
                                TxRegister consensusTxRegister = null;
                                List<String> consensusList = new ArrayList<>();
                                for (TxWrapper txWrapper : packingTxList) {
                                    Transaction tx = txWrapper.getTx();
                                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                                    if (txRegister.getModuleCode().equals(ModuleE.CS.abbr)) {

                                        consensusList.add(RPCUtil.encode(txWrapper.getTx().serialize()));
                                        if (null == consensusTxRegister) {
                                            consensusTxRegister = txRegister;
                                        }
                                    }
                                }
                                if (consensusTxRegister == null) {
                                    consensusTxRegister = TxManager.getTxRegister(chain, TxType.REGISTER_AGENT);
                                }
                                consensusList.addAll(scNewConsensusList);
                                isRollbackPackablePool = processContractConsensusTx(chain, consensusTxRegister, consensusList, packingTxList, false);

                            }
                            if (!isRollbackPackablePool) {
                                contractGenerateTxs.addAll(scNewList);
                            }

                        }
                        String sr = (String) map.get("stateRoot");
                        if (null != sr) {
                            stateRoot = sr;
                        }
                    } catch (NulsException e) {
                        nulsLogger.error(e);
                        isRollbackPackablePool = true;
                    }
                }
                if (isRollbackPackablePool) {
                    Iterator<TxWrapper> iterator = packingTxList.iterator();
                    while (iterator.hasNext()) {
                        TxWrapper txWrapper = iterator.next();
                        if (TxManager.isUnSystemSmartContract(chain, txWrapper.getTx().getType())) {
                            /**
                             * 智能合约出现需要加回待打包队列的情况,没有加回次数限制,
                             * 不需要比对TX_PACKAGE_ORPHAN_MAP的阈值,直接加入集合,可以与孤儿交易合用一个集合
                             */
                            orphanTxSet.add(txWrapper);
                            //从可打包集合中删除
                            iterator.remove();
                        }
                    }
                }
            }
            long contractTime = NulsDateUtils.getCurrentTimeMillis() - contractStart;

            List<String> packableTxs = new ArrayList<>();
            Iterator<TxWrapper> iterator = packingTxList.iterator();
            Map<NulsHash, Integer> txPackageOrphanMap = chain.getTxPackageOrphanMap();
            while (iterator.hasNext()) {
                TxWrapper txWrapper = iterator.next();
                Transaction tx = txWrapper.getTx();
                NulsHash hash = tx.getHash();
                if (txPackageOrphanMap.containsKey(hash)) {
                    txPackageOrphanMap.remove(hash);
                }
                try {
                    packableTxs.add(RPCUtil.encode(tx.serialize()));
                } catch (Exception e) {
                    clearInvalidTx(chain, tx);
                    iterator.remove();
                    throw new NulsException(e);
                }
            }
            //将智能合约生成的返还GAS的tx加到队尾
            if (contractGenerateTxs.size() > 0) {
                String csTxStr = contractGenerateTxs.get(contractGenerateTxs.size() - 1);
                if (TxUtil.extractTxTypeFromTx(csTxStr) == TxType.CONTRACT_RETURN_GAS) {
                    packableTxs.add(csTxStr);
                }

            }
            //检测最新高度
            if (blockHeight < chain.getBestBlockHeight() + 1) {
                //这个阶段已经不够时间再打包,所以直接超时异常处理交易回滚至待打包队列,打空块
                nulsLogger.info("获取交易完成时,当前最新高度已增长,不够时间重新打包,直接超时异常处理交易回滚至待打包队列,打空块");
                throw new NulsException(TxErrorCode.HEIGHT_UPDATE_UNABLE_TO_REPACKAGE);
            }


            //孤儿交易加回待打包队列去
            putBackPackablePool(chain, orphanTxSet);

            if (chain.getProtocolUpgrade().get()) {
                //协议升级直接打空块,取出的交易，倒序放入新交易处理队列
                int size = packingTxList.size();
                for (int i = size - 1; i >= 0; i--) {
                    TxWrapper txWrapper = packingTxList.get(i);
                    NetTxProcessJob netTxProcessJob = new NetTxProcessJob(chain, new TransactionNetPO(txWrapper.getTx()));
                    NetTxThreadPoolExecutor threadPool = chain.getNetTxThreadPoolExecutor();
                    threadPool.addFirst(netTxProcessJob);
                }
                return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
            }
            //检测预留传输时间
            long current = NulsDateUtils.getCurrentTimeMillis();
            if (endtimestamp - current < chain.getConfig().getPackageRpcReserveTime()) {
                //超时,留给最后数据组装和RPC传输时间不足
                nulsLogger.error("getPackableTxs time out, endtimestamp:[{}], current:[{}], endtimestamp-current:[{}], reserveTime:[{}]",
                        endtimestamp, current, endtimestamp - current, chain.getConfig().getPackageRpcReserveTime());
                throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
            }

            TxPackage txPackage = new TxPackage(packableTxs, stateRoot, blockHeight);
            long totalTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            nulsLogger.debug("[打包时间统计]  打包可用时间:[{}], 确认过的交易数量:[{}], 处理已确认交易花费时间：[{}], 获取交易(循环)总等待时间:[{}], " +
                            "获取交易(循环)执行时间:[{}], 获取交易(循环)验证账本总时间:[{}], 模块统一验证执行时间:[{}], " +
                            "合约执行时间:[{}], 总执行时间:[{}], 剩余时间:[{}]",
                    packingTime, confirmedTxCount, confirmedTxTime, allSleepTime, whileTime, totalLedgerTime, batchModuleTime,
                    contractTime, totalTime, endtimestamp - NulsDateUtils.getCurrentTimeMillis());

            nulsLogger.info("[Transaction Package end]  - height:[{}], - 待打包队列剩余交易数:[{}], - 本次打包交易数:[{}] ",
                    blockHeight, packablePool.packableHashQueueSize(chain), packableTxs.size());

            nulsLogger.info("");
            return txPackage;
        } catch (Exception e) {
            nulsLogger.error(e);
            //可打包交易,孤儿交易,全加回去
            putBackPackablePool(chain, packingTxList, orphanTxSet);
            return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
        } finally {
            chain.getPackageLock().unlock();
        }
    }


    /**
     * 处理智能合约的共识交易
     *
     * @param chain
     * @param consensusTxRegister
     * @param consensusList
     * @param packingTxList
     * @param batchVerify
     * @return
     * @throws NulsException
     */
    private boolean processContractConsensusTx(Chain chain, TxRegister consensusTxRegister, List<String> consensusList, List<TxWrapper> packingTxList, boolean batchVerify) throws NulsException {
        while (true) {
            List<String> txHashList = TransactionCall.txModuleValidator(chain, consensusTxRegister.getModuleCode(), consensusList);
            if (txHashList.isEmpty()) {
                //都执行通过
                return false;
            }
            if (batchVerify) {
                //如果是验证区块交易，有不通过的 直接返回
                return true;
            }
            Iterator<String> it = consensusList.iterator();
            while (it.hasNext()) {
                Transaction tx = TxUtil.getInstanceRpcStr(it.next(), Transaction.class);
                int type = tx.getType();
                for (String hash : txHashList) {
                    if (hash.equals(tx.getHash().toHex()) && (type == TxType.CONTRACT_CREATE_AGENT
                            || type == TxType.CONTRACT_DEPOSIT
                            || type == TxType.CONTRACT_CANCEL_DEPOSIT
                            || type == TxType.CONTRACT_STOP_AGENT)) {
                        //有智能合约交易不通过 则把所有智能合约交易返回待打包队列
                        return true;
                    }
                }
            }
            /**
             * 没有智能合约失败,只有普通共识交易失败的情况
             * 1.从待打包队列删除
             * 2.从模块统一验证集合中删除，再次验证，直到全部验证通过
             */
            for (int i = 0; i < txHashList.size(); i++) {
                String hash = txHashList.get(i);
                Iterator<TxWrapper> its = packingTxList.iterator();
                while (its.hasNext()) {
                    /**冲突检测有不通过的, 执行清除和未确认回滚 从packingTxList删除*/
                    Transaction tx = its.next().getTx();
                    if (hash.equals(tx.getHash().toHex())) {
                        clearInvalidTx(chain, tx);
                        its.remove();
                    }
                }
                Iterator<String> itcs = consensusList.iterator();
                while (its.hasNext()) {
                    Transaction tx = TxUtil.getInstanceRpcStr(itcs.next(), Transaction.class);
                    if (hash.equals(tx.getHash().toHex())) {
                        itcs.remove();
                    }

                }
            }
        }
    }

    /**
     * 将孤儿交易加回待打包队列时, 要判断加了几次(因为下次打包时又验证为孤儿交易会再次被加回), 达到阈值就不再加回了
     */
    private void addOrphanTxSet(Chain chain, Set<TxWrapper> orphanTxSet, TxWrapper txWrapper) {
        NulsHash hash = txWrapper.getTx().getHash();
        Integer count = chain.getTxPackageOrphanMap().get(hash);
        if (count == null || count < TxConstant.PACKAGE_ORPHAN_MAXCOUNT) {
            orphanTxSet.add(txWrapper);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            chain.getTxPackageOrphanMap().put(hash, count);
        } else {
            //不加回(丢弃),同时删除map中的key
            chain.getTxPackageOrphanMap().remove(hash);
        }
    }

    /**
     * 将交易加回到待打包队列
     * 将孤儿交易(如果有),加入到验证通过的交易集合中,按取出的顺序排倒序,再依次加入待打包队列的最前端
     *
     * @param chain
     * @param txList      验证通过的交易
     * @param orphanTxSet 孤儿交易
     */
    private void putBackPackablePool(Chain chain, List<TxWrapper> txList, Set<TxWrapper> orphanTxSet) {
        if (null == txList) {
            txList = new ArrayList<>();
        }
        if (null != orphanTxSet && !orphanTxSet.isEmpty()) {
            txList.addAll(orphanTxSet);
        }
        //孤儿交易排倒序,全加回待打包队列去
        txList.sort(new Comparator<TxWrapper>() {
            @Override
            public int compare(TxWrapper o1, TxWrapper o2) {
                return o1.compareTo(o2.getIndex());
            }
        });
        for (TxWrapper txWrapper : txList) {
            packablePool.offerFirst(chain, txWrapper.getTx());
        }
    }

    private void putBackPackablePool(Chain chain, Set<TxWrapper> orphanTxSet) {
        putBackPackablePool(chain, null, orphanTxSet);
    }

    /**
     * 1.统一验证
     * 2a:如果没有不通过的验证的交易则结束!!
     * 2b.有不通过的验证时，moduleVerifyMap过滤掉不通过的交易.
     * 3.重新验证同一个模块中不通过交易后面的交易(包括单个verify和coinData)，再执行1.递归？
     *
     * @param moduleVerifyMap
     */
    private boolean txModuleValidatorPackable(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, List<TxWrapper> packingTxList, Set<TxWrapper> orphanTxSet) throws NulsException {
        Iterator<Map.Entry<TxRegister, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<TxRegister, List<String>> entry = it.next();
            List<String> moduleList = entry.getValue();
            if (moduleList.size() == 0) {
                //当递归中途模块交易被过滤完后会造成list为空,这时不需要再调用模块统一验证器
                it.remove();
                continue;
            }
            TxRegister txRegister = entry.getKey();
            List<String> txHashList = TransactionCall.txModuleValidator(chain, txRegister.getModuleCode(), moduleList);
            if (!txHashList.isEmpty()) {
                chain.getLogger().debug("[模块统一验证器, 出现冲突交易] module:{}, module-code:{}, count:{} , return count:{}",
                        BaseConstant.TX_VALIDATOR, txRegister.getModuleCode(), moduleList.size(), txHashList.size());
            }
            if (null == txHashList || txHashList.size() == 0) {
                //模块统一验证没有冲突的，从map中干掉
                it.remove();
                continue;
            }

            /**冲突检测有不通过的, 执行清除和未确认回滚 从packingTxList删除*/
            for (int i = 0; i < txHashList.size(); i++) {
                String hash = txHashList.get(i);
                Iterator<TxWrapper> its = packingTxList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next().getTx();
                    if (hash.equals(tx.getHash().toHex())) {
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
        verifyAgain(chain, moduleVerifyMap, packingTxList, orphanTxSet);
        return txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList, orphanTxSet);
    }

    private void verifyAgain(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, List<TxWrapper> packingTxList, Set<TxWrapper> orphanTxSet) throws NulsException {
        chain.getLogger().debug("%%%%%%%%% verifyAgain 打包再次批量校验通知 %%%%%%%%%%%%");
        //向账本模块发送要批量验证coinData的标识
        LedgerCall.coinDataBatchNotify(chain);
        Iterator<TxWrapper> it = packingTxList.iterator();

        while (it.hasNext()) {
            TxWrapper txWrapper = it.next();
            Transaction tx = txWrapper.getTx();
            if (TxManager.isSystemSmartContract(chain, tx.getType())) {
                //智能合约系统交易不需要验证账本
                continue;
            }
            //批量验证coinData, 单个发送
            String txStr = null;
            try {
                txStr = RPCUtil.encode(tx.serialize());
            } catch (Exception e) {
                throw new NulsException(e);
            }
            VerifyLedgerResult verifyLedgerResult = LedgerCall.verifyCoinDataPackaged(chain, txStr);
            if (!verifyLedgerResult.businessSuccess()) {
                chain.getLogger().error("coinData 打包批量验证未通过 verify fail - orphan: {}, - code:{}, type:{}, - txhash:{}", verifyLedgerResult.getOrphan(),
                        verifyLedgerResult.getErrorCode() == null ? "" : verifyLedgerResult.getErrorCode().getCode(),
                        tx.getType(), tx.getHash().toHex());
                if (TxManager.isUnSystemSmartContract(chain, tx.getType())) {
                    //如果是智能合约的非系统交易,未验证通过,则放回待打包队列.
                    packablePool.offerFirst(chain, tx);
                    chain.setContractTxFail(true);
                } else if (verifyLedgerResult.getOrphan()) {
                    addOrphanTxSet(chain, orphanTxSet, txWrapper);
                } else {
                    clearInvalidTx(chain, tx);
                }
                it.remove();
                continue;
            }
            //从已确认的交易中进行重复交易判断
            TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
            if (txConfirmed != null) {
                chain.getLogger().info("[verifyAgain] 丢弃已确认过交易,txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                it.remove();
                continue;
            }
            TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
        }
    }

    @Override
    public Map<String, Object> batchVerify(Chain chain, List<String> txStrList, BlockHeader blockHeader, String blockHeaderStr, String preStateRoot) throws NulsException {
        Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
        resultMap.put("value", false);
        NulsLogger logger = chain.getLogger();
        long blockHeight = blockHeader.getHeight();
        long s1 = NulsDateUtils.getCurrentTimeMillis();
//        LOG.debug("[验区块交易] -开始-------------高度:{} ----------区块交易数:{} -------------", blockHeight, txStrList.size());
//        LOG.debug("[验区块交易] -开始时间:{}", s1);
//        LOG.debug("");
        //交易数据类型包装器
        class TxDataWrapper {
            private Transaction tx;
            private String txStr;

            public TxDataWrapper(Transaction tx, String txStr) {
                this.tx = tx;
                this.txStr = txStr;
            }
        }
        List<TxDataWrapper> txList = new ArrayList<>();

        /**
         * 智能合约通知标识
         * 当本次打包过程中,出现的第一个智能合约交易并且调用验证器通过时,
         * 就对智能合约模块进行调用合约的通知,本次打包之后再出现智能合约交易则不会再次通知.
         * 打包时没有智能合约交易则不通知, 有则只第一次时通知.
         */
        boolean contractNotify = false;
        int unSystemSmartContractCount = 0;//非系统智能合约交易的总数 计划beta版删除 todo
        List<Future<Boolean>> futures = new ArrayList<>();
        for (String txStr : txStrList) {
            Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
            txList.add(new TxDataWrapper(tx, txStr));
            //如果不是系统智能合约就继续单个验证
            if (TxManager.isSystemSmartContract(chain, tx.getType())) {
                continue;
            }
            unSystemSmartContractCount++;//非系统智能合约交易计数 计划beta版删除 todo
            //多线程处理单个交易
            Future<Boolean> res = verifySignExecutor.submit(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    NulsHash hash = tx.getHash();
                    String hashStr = hash.toHex();
                    int type = tx.getType();
                    TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, hash);
                    if (null != txConfirmed) {
                        //交易已存在于已确认块中
                        logger.debug("batchVerify failed, tx is existed. hash:{}, -type:{}", hashStr, type);
                        return false;
                    }
                    if (!unconfirmedTxStorageService.isExists(chain.getChainId(), hash)) {
                        //不在未确认中就进行基础验证
                        try {
                            //只验证单个交易的基础内容(TX模块本地验证)
                            TxRegister txRegister = TxManager.getTxRegister(chain, type);
                            if (null == txRegister) {
                                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
                            }
                            baseValidateTx(chain, tx, txRegister);
                        } catch (Exception e) {
                            logger.error("batchVerify failed, single tx verify failed. hash:{}, -type:{}", hashStr, type);
                            logger.error(e);
                            return false;
                        }
                    }
                    return true;
                }
            });
            futures.add(res);
        }

        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        long blockTime = blockHeader.getTime();

        for (TxDataWrapper txDataWrapper : txList) {
            Transaction tx = txDataWrapper.tx;
            /** 智能合约*/
            if (TxManager.isUnSystemSmartContract(chain, tx.getType())) {
                /** 出现智能合约,且通知标识为false,则先调用通知 */
                if (!contractNotify) {
                    String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress(chain.getChainId()));
                    ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot);
                    contractNotify = true;
                }
                try {
                    if (!ContractCall.invokeContract(chain, RPCUtil.encode(tx.serialize()))) {
                        logger.debug("batch verify failed. invokeContract fail");
                        return resultMap;
                    }
                } catch (IOException e) {
                    throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
                }
            }

            //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
            TxUtil.moduleGroups(chain, moduleVerifyMap, tx.getType(), txDataWrapper.txStr);
        }

        if (contractNotify) {
            if (!ContractCall.contractBatchBeforeEnd(chain, blockHeight)) {
                logger.debug("batch verify failed. contractBatchBeforeEnd fail");
                return resultMap;
            }
        }

//        long coinDataV = NulsDateUtils.getCurrentTimeMillis();//-----
        if (!LedgerCall.verifyBlockTxsCoinData(chain, txStrList, blockHeight)) {
            logger.debug("batch verifyCoinData failed.");
            return resultMap;
        }
//        LOG.debug("[验区块交易] coinData验证时间:{}", NulsDateUtils.getCurrentTimeMillis() - coinDataV);//----
//        LOG.debug("[验区块交易] coinData -距方法开始的时间:{}", NulsDateUtils.getCurrentTimeMillis() - s1);//----
//        LOG.debug("");//----

        //统一验证
//        long moduleV = NulsDateUtils.getCurrentTimeMillis();//-----
        Iterator<Map.Entry<TxRegister, List<String>>> it = moduleVerifyMap.entrySet().iterator();
//        boolean rs = true;
        while (it.hasNext()) {
            Map.Entry<TxRegister, List<String>> entry = it.next();
            List<String> txHashList = TransactionCall.txModuleValidator(chain,
                    entry.getKey().getModuleCode(), entry.getValue(), blockHeaderStr);
            if (txHashList != null && txHashList.size() > 0) {
                logger.debug("batch module verify fail:{}, module-code:{},  return count:{}", entry.getKey().getModuleCode(), txHashList.size());
//                rs = false;
                break;
            }
        }
//        LOG.debug("[验区块交易] 模块统一验证时间:{}", NulsDateUtils.getCurrentTimeMillis() - moduleV);//----
//        LOG.debug("[验区块交易] 模块统一验证 -距方法开始的时间:{}", NulsDateUtils.getCurrentTimeMillis() - s1);//----
//        LOG.debug("");//----

        /** 智能合约 当通知标识为true, 则表明有智能合约被调用执行*/
        List<String> scNewList = new ArrayList<>();
        String scStateRoot = preStateRoot;
        if (contractNotify) {
            Map<String, Object> map = null;
            try {
                map = ContractCall.contractBatchEnd(chain, blockHeight);
            } catch (NulsException e) {
                logger.error(e);
                return resultMap;
            }
            scStateRoot = (String) map.get("stateRoot");

            scNewList = (List<String>) map.get("txList");
            if (null == scNewList) {
                logger.error("contract new txs is null");
                return resultMap;
            }


            /**
             * 1.共识验证 如果有
             * 2.如果只有智能合约的共识交易失败，isRollbackPackablePool=true
             * 3.如果只有其他共识交易失败，单独删掉
             * 4.混合 执行2.
             */
            List<String> scNewConsensusList = new ArrayList<>();
            for (String scNewTx : scNewList) {
                int scNewTxType = TxUtil.extractTxTypeFromTx(scNewTx);
                if (scNewTxType == TxType.CONTRACT_CREATE_AGENT
                        || scNewTxType == TxType.CONTRACT_DEPOSIT
                        || scNewTxType == TxType.CONTRACT_CANCEL_DEPOSIT
                        || scNewTxType == TxType.CONTRACT_STOP_AGENT) {
                    scNewConsensusList.add(scNewTx);
                }
            }
            if (!scNewConsensusList.isEmpty()) {
                //收集共识模块所有交易, 加上新产生的智能合约共识交易，一起再次进行模块统一验证
                TxRegister consensusTxRegister = null;
                List<String> consensusList = new ArrayList<>();
                int txType;
                for (TxDataWrapper txDataWrapper : txList) {
                    Transaction tx = txDataWrapper.tx;
                    txType = tx.getType();
                    // 区块中的包含了智能合约生成的共识交易，不重复添加
                    if (txType == TxType.CONTRACT_CREATE_AGENT
                            || txType == TxType.CONTRACT_DEPOSIT
                            || txType == TxType.CONTRACT_CANCEL_DEPOSIT
                            || txType == TxType.CONTRACT_STOP_AGENT) {
                        continue;
                    }
                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                    if (txRegister.getModuleCode().equals(ModuleE.CS.abbr)) {
                        consensusList.add(txDataWrapper.txStr);
                        if (null == consensusTxRegister) {
                            consensusTxRegister = txRegister;
                        }
                    }
                }
                if (consensusTxRegister == null) {
                    consensusTxRegister = TxManager.getTxRegister(chain, TxType.REGISTER_AGENT);
                }
                consensusList.addAll(scNewConsensusList);
                boolean rsProcess = processContractConsensusTx(chain, consensusTxRegister, consensusList, null, false);
                if (rsProcess) {
                    logger.error("contract tx consensus module verify fail.");
                    return resultMap;
                }
            }

            //验证智能合约执行返回的交易hex 是否正确.打包时返回的交易是加入到区块交易的队尾
            int size = scNewList.size();
            if (size > 0) {
                int txSize = txStrList.size();
                if (!txStrList.get(txSize - 1).equals(scNewList.get(size - 1))) {
                    logger.error("contract error.");
                    logger.error("收到区块交易总数 size:{}, - tx hex：{}", txStrList.size(), txStrList.get(txSize - 1));
                    //计划beta版删除 todo
                    logger.error("收到除生成的系统智能合约以外的交易总数 + 生成智能合约交易数 size:{}, tx hex：{}",
                            unSystemSmartContractCount + scNewList.size(), scNewList.get(size - 1));
                    return resultMap;
                }
                //返回智能合约交易给区块
                if (TxUtil.extractTxTypeFromTx(scNewList.get(size - 1)) == TxType.CONTRACT_RETURN_GAS) {
                    scNewList.remove(size - 1);
                }
            }
        }

        //stateRoot发到共识,处理完再比较
        String coinBaseTx = null;
        for (TxDataWrapper txDataWrapper : txList) {
            Transaction tx = txDataWrapper.tx;
            if (tx.getType() == TxType.COIN_BASE) {
                coinBaseTx = txDataWrapper.txStr;
                break;
            }
        }
        String stateRootNew = ConsensusCall.triggerCoinBaseContract(chain, coinBaseTx, blockHeaderStr, scStateRoot);
        byte[] extend = blockHeader.getExtend();
        BlockExtendsData blockExtendsData = new BlockExtendsData();
        blockExtendsData.parse(extend, 0);
        String stateRoot = RPCUtil.encode(blockExtendsData.getStateRoot());
        if (!stateRoot.equals(stateRootNew)) {
            logger.warn("contract stateRoot error.");
            return resultMap;
        }

        try {
            //多线程处理结果
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    logger.error("batchVerify failed, single tx verify failed");
                    return resultMap;
                }
            }
        } catch (Exception e) {
            logger.error("batchVerify failed, single tx verify failed");
            logger.error(e);
            return resultMap;
        }
        logger.debug("[验区块交易] --合计执行时间:[{}], - 高度:[{}] - 区块交易数:[{}]",
                NulsDateUtils.getCurrentTimeMillis() - s1, blockHeight, txStrList.size());

        resultMap.put("value", true);
        resultMap.put("contractList", scNewList);
        return resultMap;

    }

    @Override
    public void clearInvalidTx(Chain chain, Transaction tx) {
        clearInvalidTx(chain, tx, false);
    }

    @Override
    public void clearInvalidTx(Chain chain, Transaction tx, boolean cleanLedgerUfmTx) {
        clearTxExecutor.submit(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                //判断如果交易已被确认就不用清理了!!
                TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
                if (txConfirmed != null) {
                    return true;
                }
                unconfirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
                try {
                    //如果是清理机制调用, 则调用账本未确认回滚
                    LedgerCall.rollBackUnconfirmTx(chain, RPCUtil.encode(tx.serialize()));
                    if (!cleanLedgerUfmTx) {
                        //通知账本状态变更
                        LedgerCall.rollbackTxValidateStatus(chain, RPCUtil.encode(tx.serialize()));
                    }
                    return true;
                } catch (NulsException e) {
                    chain.getLogger().error(e);
                } catch (Exception e) {
                    chain.getLogger().error(e);
                }
                return false;
            }
        });
    }


    /**
     * 1.按时间取出交易执行时间为endtimestamp-500，预留500毫秒给统一验证，
     * 2.取交易同时执行交易验证，然后coinData的验证(先发送开始验证的标识)
     * 3.冲突检测，模块统一验证，如果有没验证通过的交易，则将该交易之后的所有交易再从1.开始执行一次
     */
    @Override
    public TxPackage getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize, long blockHeight, long blockTime, String packingAddress, String preStateRoot) {
        chain.getPackageLock().lock();
        long startTime = NulsDateUtils.getCurrentTimeMillis();
        NulsLogger nulsLogger = chain.getLogger();
        nulsLogger.info("");
        nulsLogger.info("");
        nulsLogger.info("");
        nulsLogger.info("");
        nulsLogger.info("[Transaction Package start] -可打包时间：[{}], -可打包容量：[{}]B , - height:[{}], - 当前待打包队列交易数:[{}] ",
                endtimestamp - startTime, maxTxDataSize, blockHeight, packablePool.packableHashQueueSize(chain));
        //重置标志
        chain.setContractTxFail(false);
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<TxRegister, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        List<TxWrapper> packingTxList = new ArrayList<>();
        //记录账本的孤儿交易,返回给共识的时候给过滤出去,因为在因高度变化而导致重新打包的时候,需要还原到待打包队列
        Set<TxWrapper> orphanTxSet = new HashSet<>();
        long packingTime = endtimestamp - startTime;
        //统计总等待时间
        int allSleepTime = 0;
        //循环获取交易使用时间
        long whileTime;
        //验证账本总时间
        long totalLedgerTime = 0;
        //模块统一验证使用总时间
        long batchModuleTime;
        long totalSize = 0L;

        /**
         * 智能合约通知标识
         * 当本次打包过程中,出现的第一个智能合约交易并且调用验证器通过时,
         * 就对智能合约模块进行调用合约的通知,本次打包之后再出现智能合约交易则不会再次通知.
         * 打包时没有智能合约交易则不通知, 有则只第一次时通知.
         */
        boolean contractNotify = false;
        try {
            //通过配置的百分比，计算从总的打包时间中预留给批量验证的时间
            float batchValidReserveTemp = (chain.getConfig().getModuleVerifyPercent() / 100.0f) * (endtimestamp - startTime);
            long batchValidReserve = (long) batchValidReserveTemp;
            //向账本模块发送要批量验证coinData的标识
            LedgerCall.coinDataBatchNotify(chain);
            //当前批次从待打包交易队列获取交易的个数,达到处理数量或者待打包队列为空则开始处理该批次
            int batch = 0;
            List<String> batchProcessList = new ArrayList<>();
            List<TxWrapper> currentBatchPackableTxs = new ArrayList<>();
            for (int index = 0; ; index++) {
                long currentTimeMillis = NulsDateUtils.getCurrentTimeMillis();
                //// TODO: 2019/6/5 临时处理 如果交易容量增大，统一验证预留时间需要加大
                if (totalSize >= ((Double) (maxTxDataSize / 2.5)).longValue()) {
                    batchValidReserve = (long) batchValidReserveTemp * 3;
                }
                if (endtimestamp - currentTimeMillis <= batchValidReserve) {
                    nulsLogger.debug("获取交易时间到,进入模块验证阶段: currentTimeMillis:[{}], -endtimestamp:[{}], -offset:[{}], -remaining:[{}]",
                            currentTimeMillis, endtimestamp, batchValidReserve, endtimestamp - currentTimeMillis);
                    break;
                }
                if (chain.getProtocolUpgrade().get()) {
                    nulsLogger.info("[Transaction Package start]  - Protocol Upgrade Package stop -chain:[{}] -best block height", chain.getChainId(), chain.getBestBlockHeight());
                    //放回可打包交易和孤儿
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    //直接打空块
                    return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
                }
                //如果本地最新区块+1 大于当前在打包区块的高度, 说明本地最新区块已更新,需要重新打包,把取出的交易放回到打包队列
                if (blockHeight < chain.getBestBlockHeight() + 1) {
                    nulsLogger.info("获取交易过程中最新区块高度已增长,把取出的交易以及孤儿放回到打包队列, 重新打包...");
                    //放回可打包交易和孤儿
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    return getPackableTxs(chain, endtimestamp, maxTxDataSize, chain.getBestBlockHeight() + 1, blockTime, packingAddress, preStateRoot);
                }
                int batchProcessListSize = batchProcessList.size();
                boolean process = false;
                Transaction tx = packablePool.poll(chain);
                if (tx == null && batchProcessListSize == 0) {
                    //判断时间？
                    Thread.sleep(30L);
                    allSleepTime += 30;
                    continue;
                } else if (tx == null && batchProcessListSize > 0) {
                    //达到处理该批次的条件
                    process = true;
                } else if (tx != null) {
                    long txSize = tx.size();
                    if ((totalSize + txSize) > maxTxDataSize) {
                        packablePool.offerFirst(chain, tx);
                        nulsLogger.info("交易已达最大容量, 实际值: {} - 预定最大值maxTxDataSize:{}", totalSize + txSize, maxTxDataSize);
                        if (batchProcessListSize > 0) {
                            //达到处理该批次的条件
                            process = true;
                        } else {
                            break;
                        }
                    } else {
                        String txHex;
                        try {
                            txHex = RPCUtil.encode(tx.serialize());
                        } catch (Exception e) {
                            nulsLogger.warn(e.getMessage(), e);
                            nulsLogger.error("丢弃获取hex出错交易, txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                            clearInvalidTx(chain, tx);
                            continue;
                        }
                        batchProcessList.add(txHex);
                        TxWrapper txWrapper = new TxWrapper(tx, index, txHex);
                        currentBatchPackableTxs.add(txWrapper);
                        if (batchProcessList.size() == TxConstant.PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS) {
                            //达到处理该批次的条件
                            process = true;
                        }
                    }
                }
                if (process) {
                    long verifyLedgerStart = NulsDateUtils.getCurrentTimeMillis();
                    verifyLedger(chain, batchProcessList, currentBatchPackableTxs, orphanTxSet);
                    totalLedgerTime += NulsDateUtils.getCurrentTimeMillis() - verifyLedgerStart;
                    for (TxWrapper txWrapper : currentBatchPackableTxs) {
                        Transaction transaction = txWrapper.getTx();
                        if (TxManager.isSmartContract(chain, transaction.getType())) {
                            /** 出现智能合约,且通知标识为false,则先调用通知 */
                            if (!contractNotify) {
                                ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot);
                                contractNotify = true;
                            }
                            if (!ContractCall.invokeContract(chain, txWrapper.getTxHex())) {
                                clearInvalidTx(chain, transaction);
                                continue;
                            }
                        }
                        totalSize += transaction.getSize();
                        //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
                        TxUtil.moduleGroups(chain, moduleVerifyMap, transaction);
                    }
                    packingTxList.addAll(currentBatchPackableTxs);
                }

            }
            //循环获取交易使用时间
            whileTime = NulsDateUtils.getCurrentTimeMillis() - startTime;

            nulsLogger.debug("-取出的交易 - totalSize:{}", totalSize);


            boolean contractBefore = false;
            if (contractNotify) {
                contractBefore = ContractCall.contractBatchBeforeEnd(chain, blockHeight);
            }

            long batchStart = NulsDateUtils.getCurrentTimeMillis();
            txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList, orphanTxSet);
            //模块统一验证使用总时间
            batchModuleTime = NulsDateUtils.getCurrentTimeMillis() - batchStart;

            String stateRoot = preStateRoot;
            long contractStart = NulsDateUtils.getCurrentTimeMillis();
            /** 智能合约 当通知标识为true, 则表明有智能合约被调用执行*/
            List<String> contractGenerateTxs = new ArrayList<>();
            if (contractNotify && !chain.getContractTxFail()) {
                //处理智能合约执行结果
                stateRoot = processContractResult(chain, packingTxList, orphanTxSet, contractGenerateTxs, blockHeight, contractBefore, stateRoot);
            }
            long contractTime = NulsDateUtils.getCurrentTimeMillis() - contractStart;

            List<String> packableTxs = new ArrayList<>();
            Iterator<TxWrapper> iterator = packingTxList.iterator();
            Map<NulsHash, Integer> txPackageOrphanMap = chain.getTxPackageOrphanMap();
            while (iterator.hasNext()) {
                TxWrapper txWrapper = iterator.next();
                Transaction tx = txWrapper.getTx();
                NulsHash hash = tx.getHash();
                if (txPackageOrphanMap.containsKey(hash)) {
                    txPackageOrphanMap.remove(hash);
                }
                try {
                    packableTxs.add(RPCUtil.encode(tx.serialize()));
                } catch (Exception e) {
                    clearInvalidTx(chain, tx);
                    iterator.remove();
                    throw new NulsException(e);
                }
            }
            //将智能合约生成的返还GAS的tx加到队尾
            if (contractGenerateTxs.size() > 0) {
                String csTxStr = contractGenerateTxs.get(contractGenerateTxs.size() - 1);
                if (TxUtil.extractTxTypeFromTx(csTxStr) == TxType.CONTRACT_RETURN_GAS) {
                    packableTxs.add(csTxStr);
                }
            }
            //检测最新高度
            if (blockHeight < chain.getBestBlockHeight() + 1) {
                //这个阶段已经不够时间再打包,所以直接超时异常处理交易回滚至待打包队列,打空块
                nulsLogger.info("获取交易完成时,当前最新高度已增长,不够时间重新打包,直接超时异常处理交易回滚至待打包队列,打空块");
                throw new NulsException(TxErrorCode.HEIGHT_UPDATE_UNABLE_TO_REPACKAGE);
            }

            //孤儿交易加回待打包队列去
            putBackPackablePool(chain, orphanTxSet);
            if (chain.getProtocolUpgrade().get()) {
                //协议升级直接打空块,取出的交易，倒序放入新交易处理队列
                int size = packingTxList.size();
                for (int i = size - 1; i >= 0; i--) {
                    TxWrapper txWrapper = packingTxList.get(i);
                    NetTxProcessJob netTxProcessJob = new NetTxProcessJob(chain, new TransactionNetPO(txWrapper.getTx()));
                    NetTxThreadPoolExecutor threadPool = chain.getNetTxThreadPoolExecutor();
                    threadPool.addFirst(netTxProcessJob);
                }
                return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
            }
            //检测预留传输时间
            long current = NulsDateUtils.getCurrentTimeMillis();
            if (endtimestamp - current < chain.getConfig().getPackageRpcReserveTime()) {
                //超时,留给最后数据组装和RPC传输时间不足
                nulsLogger.error("getPackableTxs time out, endtimestamp:[{}], current:[{}], endtimestamp-current:[{}], reserveTime:[{}]",
                        endtimestamp, current, endtimestamp - current, chain.getConfig().getPackageRpcReserveTime());
                throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
            }

            TxPackage txPackage = new TxPackage(packableTxs, stateRoot, blockHeight);
            long totalTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            nulsLogger.debug("[打包时间统计]  打包可用时间:[{}], 获取交易(循环)总等待时间:[{}], " +
                            "获取交易(循环)执行时间:[{}], 获取交易(循环)验证账本总时间:[{}], 模块统一验证执行时间:[{}], " +
                            "合约执行时间:[{}], 总执行时间:[{}], 剩余时间:[{}]",
                    packingTime, allSleepTime, whileTime, totalLedgerTime, batchModuleTime,
                    contractTime, totalTime, endtimestamp - NulsDateUtils.getCurrentTimeMillis());

            nulsLogger.info("[Transaction Package end]  - height:[{}], - 待打包队列剩余交易数:[{}], - 本次打包交易数:[{}] ",
                    blockHeight, packablePool.packableHashQueueSize(chain), packableTxs.size());

            nulsLogger.info("");
            return txPackage;
        } catch (Exception e) {
            nulsLogger.error(e);
            //可打包交易,孤儿交易,全加回去
            putBackPackablePool(chain, packingTxList, orphanTxSet);
            return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
        } finally {
            chain.getPackageLock().unlock();
        }
    }


    /**
     * packing verify ledger
     * @param chain
     * @param batchProcessList
     * @param currentBatchPackableTxs
     * @param orphanTxSet
     * @throws NulsException
     */
    private void verifyLedger(Chain chain, List<String> batchProcessList, List<TxWrapper> currentBatchPackableTxs, Set<TxWrapper> orphanTxSet) throws NulsException {
        NulsLogger nulsLogger = chain.getLogger();
        //开始处理
        Map verifyCoinDataResult = LedgerCall.verifyCoinDataBatchPackaged(chain, batchProcessList);
        List<String> failHashs = (List<String>) verifyCoinDataResult.get("fail");
        List<String> orphanHashs = (List<String>) verifyCoinDataResult.get("orphan");
        if (!failHashs.isEmpty() || !orphanHashs.isEmpty()) {
            Iterator<TxWrapper> it = currentBatchPackableTxs.iterator();
            removeAndGo:
            while (it.hasNext()) {
                TxWrapper txWrapper = it.next();
                Transaction transaction = txWrapper.getTx();
                //去除账本验证失败的交易
                for (String hash : failHashs) {
                    String hashStr = transaction.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        nulsLogger.error("Package - ledger verification failed - type:{}, - txhash:{}", transaction.getType(), transaction.getHash().toHex());
                        it.remove();
                        continue removeAndGo;
                    }
                }
                //去除孤儿交易, 同时把孤儿交易放入孤儿池
                for (String hash : orphanHashs) {
                    String hashStr = transaction.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        nulsLogger.error("Package - ledger verification orphan tx - type:{}, - txhash:{}", transaction.getType(), transaction.getHash().toHex());
                        //孤儿交易
                        addOrphanTxSet(chain, orphanTxSet, txWrapper);
                        it.remove();
                        continue removeAndGo;
                    }
                }
            }
        }
    }


    /**
     * 处理智能合约交易 执行结果
     *
     * @param chain
     * @param packingTxList
     * @param orphanTxSet
     * @param contractGenerateTxs
     * @param blockHeight
     * @param contractBefore
     * @param stateRoot
     * @return 返回新生成的stateRoot
     * @throws IOException
     */
    private String processContractResult(Chain chain, List<TxWrapper> packingTxList, Set<TxWrapper> orphanTxSet, List<String> contractGenerateTxs,
                                         long blockHeight, boolean contractBefore, String stateRoot) throws IOException {
        /**当contractBefore通知失败,或者contractBatchEnd失败则需要将智能合约交易还回待打包队列*/
        boolean isRollbackPackablePool = false;
        if (!contractBefore) {
            isRollbackPackablePool = true;
        } else {
            try {
                Map<String, Object> map = ContractCall.contractBatchEnd(chain, blockHeight);
                List<String> scNewList = (List<String>) map.get("txList");
                if (null != scNewList) {
                    /**
                     * 1.共识验证 如果有
                     * 2.如果只有智能合约的共识交易失败，isRollbackPackablePool=true
                     * 3.如果只有其他共识交易失败，单独删掉
                     * 4.混合 执行2.
                     */
                    List<String> scNewConsensusList = new ArrayList<>();
                    for (String scNewTx : scNewList) {
                        int scNewTxType = TxUtil.extractTxTypeFromTx(scNewTx);
                        if (scNewTxType == TxType.CONTRACT_CREATE_AGENT
                                || scNewTxType == TxType.CONTRACT_DEPOSIT
                                || scNewTxType == TxType.CONTRACT_CANCEL_DEPOSIT
                                || scNewTxType == TxType.CONTRACT_STOP_AGENT) {
                            scNewConsensusList.add(scNewTx);
                        }
                    }
                    if (!scNewConsensusList.isEmpty()) {
                        //收集共识模块所有交易, 加上新产生的智能合约共识交易，一起再次进行模块统一验证
                        TxRegister consensusTxRegister = null;
                        List<String> consensusList = new ArrayList<>();
                        for (TxWrapper txWrapper : packingTxList) {
                            Transaction tx = txWrapper.getTx();
                            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                            if (txRegister.getModuleCode().equals(ModuleE.CS.abbr)) {
                                consensusList.add(RPCUtil.encode(txWrapper.getTx().serialize()));
                                if (null == consensusTxRegister) {
                                    consensusTxRegister = txRegister;
                                }
                            }
                        }
                        if (consensusTxRegister == null) {
                            consensusTxRegister = TxManager.getTxRegister(chain, TxType.REGISTER_AGENT);
                        }
                        consensusList.addAll(scNewConsensusList);
                        isRollbackPackablePool = processContractConsensusTx(chain, consensusTxRegister, consensusList, packingTxList, false);
                    }
                    if (!isRollbackPackablePool) {
                        contractGenerateTxs.addAll(scNewList);
                    }
                }
                String sr = (String) map.get("stateRoot");
                if (null != sr) {
                    stateRoot = sr;
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                isRollbackPackablePool = true;
            }
        }
        if (isRollbackPackablePool) {
            Iterator<TxWrapper> iterator = packingTxList.iterator();
            while (iterator.hasNext()) {
                TxWrapper txWrapper = iterator.next();
                if (TxManager.isUnSystemSmartContract(chain, txWrapper.getTx().getType())) {
                    /**
                     * 智能合约出现需要加回待打包队列的情况,没有加回次数限制,
                     * 不需要比对TX_PACKAGE_ORPHAN_MAP的阈值,直接加入集合,可以与孤儿交易合用一个集合
                     */
                    orphanTxSet.add(txWrapper);
                    //从可打包集合中删除
                    iterator.remove();
                }
            }
        }
        return stateRoot;
    }

}
