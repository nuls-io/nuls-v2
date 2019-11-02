/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.protocol.TxRegisterDetail;
import io.nuls.base.signture.MultiSignTxSignature;
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
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.TxManager;
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
import io.nuls.transaction.utils.TxDuplicateRemoval;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
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
            List<Integer> delList = moduleTxRegisterDto.getDelList();
            if (!delList.isEmpty()) {
                delList.forEach(e -> chain.getTxRegisterMap().remove(e));
            }
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
        return false;
    }

    @Override
    public void newBroadcastTx(Chain chain, TransactionNetPO txNet) {
        Transaction tx = txNet.getTx();
        if (!isTxExists(chain, tx.getHash())) {
            try {
                //执行交易基础验证
                TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                if (null == txRegister) {
                    throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
                }
                baseValidateTx(chain, tx, txRegister);
                chain.getUnverifiedQueue().addLast(txNet);
            } catch (NulsException e) {
                chain.getLogger().error(e);
            } catch (IllegalStateException e) {
                chain.getLogger().error("UnverifiedQueue full!");
            }
        }
    }


    @Override
    public void newTx(Chain chain, Transaction tx) throws NulsException {
        try {
            if (!chain.getProcessTxStatus().get()) {
                //节点区块同步中或回滚中,暂停接纳新交易
                throw new NulsException(TxErrorCode.PAUSE_NEWTX);
            }
            NulsHash hash = tx.getHash();
            if (isTxExists(chain, hash)) {
                throw new NulsException(TxErrorCode.TX_ALREADY_EXISTS);
            }
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
                //如果map满了则不一定能加入待打包队列
                packablePool.add(chain, tx);
            }
            unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
            //广播完整交易
            boolean broadcastResult = false;
            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
            for (int i = 0; i < 3; i++) {
                if (txRegister.getModuleCode().equals(ModuleE.CC.abbr)) {
                    broadcastResult = NetworkCall.forwardTxHash(chain, tx.getHash());
                } else {
                    broadcastResult = NetworkCall.broadcastTx(chain, tx);
                }

                if (broadcastResult) {
                    break;
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    chain.getLogger().error(e);
                }
            }
            if (!broadcastResult) {
                throw new NulsException(TxErrorCode.TX_BROADCAST_FAIL);
            }
            //加入去重过滤集合,防止其他节点转发回来再次处理该交易
            TxDuplicateRemoval.insertAndCheck(hash.toHex());

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
            return new TransactionConfirmedPO(txPo.getTx(), -1L, TxStatusEnum.UNCONFIRM.getStatus());
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
            Map<String, Object> result = TransactionCall.txModuleValidator(chain, txRegister.getModuleCode(), RPCUtil.encode(tx.serialize()));
            List<String> txHashList = (List<String>) result.get("list");
            if (txHashList.isEmpty()) {
                return VerifyResult.success();
            } else {
                chain.getLogger().error("tx validator fail -type:{}, -hash:{} ", tx.getType(), tx.getHash().toHex());
                String errorCodeStr = (String) result.get("errorCode");
                ErrorCode errorCode = null == errorCodeStr ? TxErrorCode.SYS_UNKOWN_EXCEPTION : ErrorCode.init(errorCodeStr);
                return VerifyResult.fail(errorCode);
            }
        } catch (IOException e) {
            return VerifyResult.fail(TxErrorCode.SERIALIZE_ERROR);
        } catch (NulsException e) {
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
        if (tx.getType() == TxType.YELLOW_PUNISH || tx.getType() == TxType.VERIFIER_CHANGE || tx.getType() == TxType.VERIFIER_INIT) {
            return;
        }
        CoinData coinData = TxUtil.getCoinData(tx);
        validateCoinFromBase(chain, txRegister, coinData.getFrom());
        validateCoinToBase(chain, txRegister, coinData.getTo());
        if (txRegister.getVerifyFee()) {
            validateFee(chain, tx.getType(), tx.size(), coinData, txRegister);
        }
    }

    /**
     * 验证签名 只需要验证,需要验证签名的交易(一些系统交易不用签名)
     * 验证签名数据中的公钥和from中是否匹配, 验证签名正确性
     *
     * @param tx
     * @throws NulsException
     */
    private void validateTxSignature(Transaction tx, TxRegister txRegister, Chain chain) throws NulsException {
        //只需要验证,需要验证签名的交易(一些系统交易不用签名)
        if (txRegister.getVerifySignature()) {
            CoinData coinData = TxUtil.getCoinData(tx);
            if (null == coinData || null == coinData.getFrom() || coinData.getFrom().size() <= 0) {
                throw new NulsException(TxErrorCode.COINDATA_NOT_FOUND);
            }
            //获取交易签名者地址列表
            Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chain.getChainId());
            if (addressSet == null) {
                throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
            }
            int chainId = chain.getChainId();
            byte[] multiSignAddress = null;
            if (tx.isMultiSignTx()) {
                /**
                 * 如果是多签交易, 则先从签名对象中取出多签地址原始创建者的公钥列表和最小签名数,
                 * 生成一个新的多签地址,来与交易from中的多签地址匹配，匹配不上这验证不通过.
                 */
                MultiSignTxSignature multiSignTxSignature = new MultiSignTxSignature();
                multiSignTxSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
                //验证签名者够不够最小签名数
                if (addressSet.size() < multiSignTxSignature.getM()) {
                    throw new NulsException(TxErrorCode.INSUFFICIENT_SIGNATURES);
                }
                //签名者是否是多签账户创建者之一
                for (String address : addressSet) {
                    boolean rs = false;
                    for (byte[] bytes : multiSignTxSignature.getPubKeyList()) {
                        String addr = AddressTool.getStringAddressByBytes(AddressTool.getAddress(bytes, chainId));
                        if (address.equals(addr)) {
                            rs = true;
                        }
                    }
                    if (!rs) {
                        throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
                    }
                }
                //生成一个多签地址
                List<String> pubKeys = new ArrayList<>();
                for (byte[] pubkey : multiSignTxSignature.getPubKeyList()) {
                    pubKeys.add(HexUtil.encode(pubkey));
                }
                try {
                    byte[] hash160 = SerializeUtils.sha256hash160(AddressTool.createMultiSigAccountOriginBytes(chainId, multiSignTxSignature.getM(), pubKeys));
                    Address address = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, hash160);
                    multiSignAddress = address.getAddressBytes();
                } catch (Exception e) {
                    chain.getLogger().error(e);
                    throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
                }
            }
            if (!txRegister.getModuleCode().equals(ModuleE.CC.abbr)) {
                //判断from中地址和签名的地址是否匹配
                for (CoinFrom coinFrom : coinData.getFrom()) {
                    if (tx.isMultiSignTx()) {
                        if (!Arrays.equals(coinFrom.getAddress(), multiSignAddress)) {
                            throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
                        }
                    } else if (!addressSet.contains(AddressTool.getStringAddressByBytes(coinFrom.getAddress()))
                            && tx.getType() != TxType.STOP_AGENT) {
                        throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
                    }
                }
                if (!SignatureUtil.validateTransactionSignture(tx)) {
                    throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
                }
            }
        }
    }

    private void validateCoinFromBase(Chain chain, TxRegister txRegister, List<CoinFrom> listFrom) throws NulsException {
        int type = txRegister.getTxType();
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
        byte[] existMultiSignAddress = null;
        for (CoinFrom coinFrom : listFrom) {
            byte[] addrBytes = coinFrom.getAddress();
            String addr = AddressTool.getStringAddressByBytes(addrBytes);
            //验证交易地址合法性,跨链模块交易需要取地址中的原始链id来验证
            int validAddressChainId = chainId;
            if (ModuleE.CC.abbr.equals(txRegister.getModuleCode())) {
                validAddressChainId = AddressTool.getChainIdByAddress(addrBytes);
            }
            if (!AddressTool.validAddress(validAddressChainId, addr)) {
                throw new NulsException(TxErrorCode.INVALID_ADDRESS);
            }
            if (null == existMultiSignAddress && AddressTool.isMultiSignAddress(addrBytes)) {
                existMultiSignAddress = addrBytes;
            }
            int addrChainId = AddressTool.getChainIdByAddress(addrBytes);
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
            //验证账户地址,资产链id,资产id的组合唯一性
            int assetsChainId = coinFrom.getAssetsChainId();
            int assetsId = coinFrom.getAssetsId();
            boolean rs = uniqueCoin.add(addr + "-" + assetsChainId + "-" + assetsId + "-" + HexUtil.encode(coinFrom.getNonce()));
            if (!rs) {
                throw new NulsException(TxErrorCode.COINFROM_HAS_DUPLICATE_COIN);
            }
            //用户发出的[非停止节点,红牌]交易不允许from中有合约地址,如果from包含合约地址,那么这个交易一定是系统发出的,系统发出的交易不会走基础验证
            if (type != TxType.STOP_AGENT && type != TxType.RED_PUNISH && TxUtil.isLegalContractAddress(coinFrom.getAddress(), chain)) {
                chain.getLogger().error("Tx from cannot have contract address ");
                throw new NulsException(TxErrorCode.TX_FROM_CANNOT_HAS_CONTRACT_ADDRESS);
            }
        }
        if (null != existMultiSignAddress) {
            //如果from中含有多签地址,则表示该交易是多签交易,则必须满足,froms中只存在这一个多签地址
            for (CoinFrom coinFrom : listFrom) {
                if (!Arrays.equals(existMultiSignAddress, coinFrom.getAddress())) {
                    throw new NulsException(TxErrorCode.MULTI_SIGN_TX_ONLY_SAME_ADDRESS);
                }
            }
        }
    }

    private void validateCoinToBase(Chain chain, TxRegister txRegister, List<CoinTo> listTo) throws NulsException {
        String moduleCode = txRegister.getModuleCode();
        int type = txRegister.getTxType();
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
            String addr = AddressTool.getStringAddressByBytes(coinTo.getAddress());

            //验证交易地址合法性,跨链模块交易需要取地址中的原始链id来验证
            int validAddressChainId = txChainId;
            if (ModuleE.CC.abbr.equals(txRegister.getModuleCode())) {
                validAddressChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            }
            if (!AddressTool.validAddress(validAddressChainId, addr)) {
                throw new NulsException(TxErrorCode.INVALID_ADDRESS);
            }

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
            boolean rs = uniqueCoin.add(addr + "-" + assetsChainId + "-" + assetsId + "-" + lockTime);
            if (!rs) {
                throw new NulsException(TxErrorCode.COINTO_HAS_DUPLICATE_COIN);
            }
            //合约地址接受NULS的交易只能是coinBase交易,调用合约交易,普通停止节点(合约停止节点交易是系统交易,不走基础验证)
            if (TxUtil.isLegalContractAddress(coinTo.getAddress(), chain)) {
                boolean sysTx = txRegister.getSystemTx();
                if (!sysTx && type != TxType.COIN_BASE
                        && type != TxType.CALL_CONTRACT
                        && type != TxType.STOP_AGENT) {
                    chain.getLogger().error("contract data error: The contract does not accept transfers of this type{} of transaction.", type);
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
        int feeAssetChainId;
        int feeAssetId;
        if (type == TxType.CROSS_CHAIN && AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress()) != chain.getChainId()) {
            //为跨链交易并且不是交易发起链时,计算主网主资产为手续费NULS
            feeAssetChainId = txConfig.getMainChainId();
            feeAssetId = txConfig.getMainAssetId();
        } else {
            //计算主资产为手续费
            feeAssetChainId = chain.getConfig().getChainId();
            feeAssetId = chain.getConfig().getAssetId();
        }
        BigInteger fee = coinData.getFeeByAsset(feeAssetChainId, feeAssetId);
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
     * 打包时,从待打包队列获取交易阶段,会产生临时交易列表,在中断获取交易时需要把遗留的临时交易还回待打包队列
     */
    private void backTempPackablePool(Chain chain, List<TxPackageWrapper> listTx){
        for(int i = listTx.size() - 1; i >= 0 ;i--){
            packablePool.offerFirstOnlyHash(chain, listTx.get(i).getTx());
        }
    }

    @Override
    public TxPackage getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize, long blockTime, String packingAddress, String preStateRoot) {
        chain.getPackageLock().lock();
        long startTime = NulsDateUtils.getCurrentTimeMillis();
        List<TxPackageWrapper> packingTxList = new ArrayList<>();
        //记录账本的孤儿交易,返回给共识的时候给过滤出去,因为在因高度变化而导致重新打包的时候,需要还原到待打包队列
        Set<TxPackageWrapper> orphanTxSet = new HashSet<>();
        NulsLogger nulsLogger = chain.getLogger();
        try {
            //本次打包高度
            long blockHeight = chain.getBestBlockHeight() + 1;

            long packableTime = endtimestamp - startTime;
            nulsLogger.info("[Package start] -可打包时间：{}, -可打包容量：{}B , - height:{}, - 当前待打包队列交易hash数:{}, - 待打包队列实际交易数:{}",
                    packableTime, maxTxDataSize, blockHeight, packablePool.packableHashQueueSize(chain), packablePool.packableTxMapSize(chain));
            long batchValidReserve = TxConstant.PACKAGE_MODULE_VALIDATOR_RESERVE_TIME;
            if (packableTime <= batchValidReserve) {
                //直接打空块
                return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
            }
            //重置标志
            chain.setContractTxFail(false);
            //组装统一验证参数数据,key为各模块统一验证器cmd
            Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);

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
            //获取交易时计算区块总size大小临时值
            long totalSizeTemp = 0L;
            int maxCount = TxConstant.PACKAGE_TX_MAX_COUNT - TxConstant.PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS;
            //通过配置的百分比，计算从总的打包时间中预留给批量验证的时间
            //            long batchValidReserve = packagingReservationTime(chain, packingTime);
            long packageRpcReserveTime = chain.getConfig().getPackageRpcReserveTime();

            //智能合约通知标识,出现的第一个智能合约交易并且调用验证器通过时,有则只第一次时通知.
            boolean contractNotify = false;

            //向账本模块发送要批量验证coinData的标识
            LedgerCall.coinDataBatchNotify(chain);
            //取出的交易集合(需要发送给账本验证)
            List<String> batchProcessList = new ArrayList<>();
            Set<String> duplicatesVerify = new HashSet<>();
            //取出的交易集合
            List<TxPackageWrapper> currentBatchPackableTxs = new ArrayList<>();
            //本次打包包含跨链交易个数
            int corssTxCount = 0;
            //一批次处理，包含跨链交易个数
            int batchCorssTxCount = 0;
            //本次打包包含合约交易个数
            int contractTxCount = 0;
            //一批次处理，包含合约交易个数
            int batchContractTxCount = 0;
            //是否停止执行职能合约,如果位true,则取出的智能合约本次打包不再处理,需要还回待打包队列
            boolean stopInvokeContract = false;
            for (int index = 0; ; index++) {
                long currentTimeMillis = NulsDateUtils.getCurrentTimeMillis();
                long currentReserve = endtimestamp - currentTimeMillis;
                if (currentReserve <= batchValidReserve) {
                    if(nulsLogger.isDebugEnabled()) {
                        nulsLogger.debug("获取交易时间到,进入模块验证阶段: currentTimeMillis:{}, -endtimestamp:{}, -offset:{}, -remaining:{}",
                                currentTimeMillis, endtimestamp, batchValidReserve, currentReserve);
                    }
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    break;
                }
                if (currentReserve < packageRpcReserveTime) {
                    //超时,留给最后数据组装和RPC传输时间不足
                    nulsLogger.error("getPackableTxs time out, endtimestamp:{}, current:{}, endtimestamp-current:{}, reserveTime:{}",
                            endtimestamp, currentTimeMillis, currentReserve, packageRpcReserveTime);
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
                }
                if (chain.getProtocolUpgrade().get()) {
                    nulsLogger.info("Protocol Upgrade Package stop -chain:{} -best block height", chain.getChainId(), chain.getBestBlockHeight());
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    //放回可打包交易和孤儿
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    //直接打空块
                    return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
                }
                //如果本地最新区块+1 大于当前在打包区块的高度, 说明本地最新区块已更新,需要重新打包,把取出的交易放回到打包队列
                if (blockHeight < chain.getBestBlockHeight() + 1) {
                    nulsLogger.info("获取交易过程中最新区块高度已增长,把取出的交易以及孤儿放回到打包队列, 重新打包...");
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    //放回可打包交易和孤儿
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    return getPackableTxs(chain, endtimestamp, maxTxDataSize, blockTime, packingAddress, preStateRoot);
                }
                if (packingTxList.size() > maxCount) {
                    if(nulsLogger.isDebugEnabled()) {
                        nulsLogger.debug("获取交易已达max count,进入模块验证阶段: currentTimeMillis:{}, -endtimestamp:{}, -offset:{}, -remaining:{}",
                                currentTimeMillis, endtimestamp, batchValidReserve, endtimestamp - currentTimeMillis);
                    }
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    break;
                }
                int batchProcessListSize = batchProcessList.size();
                boolean process = false;
                Transaction tx = null;
                boolean maxDataSize = false;
                try {
                    tx = packablePool.poll(chain);
                    if (tx == null && batchProcessListSize == 0) {
                        Thread.sleep(10L);
                        allSleepTime += 10;
                        continue;
                    } else if (tx == null && batchProcessListSize > 0) {
                        //达到处理该批次的条件
                        process = true;
                    } else if (tx != null) {
                        if (!duplicatesVerify.add(tx.getHash().toHex())) {
                            //加入不进去表示已存在
                            continue;
                        }
                        long txSize = tx.size();
                        if ((totalSizeTemp + txSize) > maxTxDataSize) {
                            packablePool.offerFirstOnlyHash(chain, tx);
                            nulsLogger.info("交易已达最大容量, 实际值: {}, totalSizeTemp:{}, 当前交易size：{} - 预定最大值maxTxDataSize:{}, txhash:{}", totalSize, totalSizeTemp, txSize, maxTxDataSize ,tx.getHash().toHex());
                            maxDataSize = true;
                            if (batchProcessListSize > 0) {
                                //达到处理该批次的条件
                                process = true;
                            } else {
                                break;
                            }
                        } else {
                            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                            //限制跨链交易数量
                            if (txRegister.getModuleCode().equals(ModuleE.CC.abbr)) {
                                if (corssTxCount + (++batchCorssTxCount) >= TxConstant.PACKAGE_CROSS_TX_MAX_COUNT) {
                                    //限制单个区块包含的跨链交易总数，超过跨链交易最大个数，放回去, 然后停止获取交易
                                    packablePool.add(chain, tx);
                                    if (batchProcessListSize > 0) {
                                        //达到处理该批次的条件
                                        process = true;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            //限制智能合约交易数量
                            boolean isContract = txRegister.getModuleCode().equals(ModuleE.SC.abbr);
                            if (isContract) {
                                if (contractTxCount + (++batchContractTxCount) >= TxConstant.PACKAGE_CONTRACT_TX_MAX_COUNT) {
                                    //限制单个区块包含的跨链交易总数，超过跨链交易最大个数，放回去, 然后停止获取交易
                                    packablePool.add(chain, tx);
                                    if (batchProcessListSize > 0) {
                                        //达到处理该批次的条件
                                        process = true;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            String txHex;
                            try {
                                txHex = RPCUtil.encode(tx.serialize());
                            } catch (Exception e) {
                                nulsLogger.warn(e.getMessage(), e);
                                nulsLogger.error("丢弃获取hex出错交易, txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                                clearInvalidTx(chain, tx);
                                continue;
                            }
                            TxPackageWrapper txPackageWrapper = new TxPackageWrapper(tx, index, txHex);
                            batchProcessList.add(txHex);
                            currentBatchPackableTxs.add(txPackageWrapper);
                            if (batchProcessList.size() == TxConstant.PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS) {
                                //达到处理该批次的条件
                                process = true;
                            }
                        }
                        //总大小加上当前批次各笔交易大小
                        totalSizeTemp += txSize;
                    }
                    if (process) {
                        long verifyLedgerStart = NulsDateUtils.getCurrentTimeMillis();
                        if (!chain.getPackableState().get()) {
                            nulsLogger.info("获取交易过程中保存或回滚区块触发账本提交或回滚, 重新打包...");
                            //放回可打包交易和孤儿
                            packingTxList.addAll(currentBatchPackableTxs);
                            putBackPackablePool(chain, packingTxList, orphanTxSet);
                            Thread.sleep(30L);
                            return getPackableTxs(chain, endtimestamp, maxTxDataSize, blockTime, packingAddress, preStateRoot);
                        }
                        verifyLedger(chain, batchProcessList, currentBatchPackableTxs, orphanTxSet, false, false);
                        totalLedgerTime += NulsDateUtils.getCurrentTimeMillis() - verifyLedgerStart;

                        Iterator<TxPackageWrapper> it = currentBatchPackableTxs.iterator();
                        while (it.hasNext()) {
                            TxPackageWrapper txPackageWrapper = it.next();
                            Transaction transaction = txPackageWrapper.getTx();
                            TxRegister txRegister = TxManager.getTxRegister(chain, transaction.getType());
                            String moduleCode = txRegister.getModuleCode();
                            boolean isSmartContractTx = moduleCode.equals(ModuleE.SC.abbr);
                            boolean isCrossTx = moduleCode.equals(ModuleE.CC.abbr);
                            // add by pierre at 2019-11-02 需要协议升级
                            boolean isCrossTransferTx = TxType.CROSS_CHAIN == transaction.getType();
                            // end code by pierre
                            // add by pierre at 2019-10-22 跨链转账交易发送到智能合约模块进行解析，是否为合约资产跨链转账
                            if (isSmartContractTx || isCrossTransferTx) {
                                if(stopInvokeContract){
                                    //该标志true,表示不再处理智能合约交易,需要暂存交易,统一还回待打包队列
                                    orphanTxSet.add(txPackageWrapper);
                                    it.remove();
                                    continue;
                                }
                                // 出现智能合约,且通知标识为false,则先调用通知
                                if (!contractNotify) {
                                    ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot, 0);
                                    contractNotify = true;
                                }
                                try {
                                    //调用执行智能合约,返回false.则不再处理智能合约
                                    boolean invokeContractRs = ContractCall.invokeContract(chain, txPackageWrapper.getTxHex(), 0);
                                    if(!invokeContractRs){
                                        //不再发invoke
                                        stopInvokeContract = true;
                                        orphanTxSet.add(txPackageWrapper);
                                        it.remove();
                                        continue;
                                    }
                                } catch (NulsException e) {
                                    chain.getLogger().error(e);
                                    clearInvalidTx(chain, transaction);
                                    continue;
                                }
                            }
                            totalSize += transaction.getSize();

                            //计算跨链交易的数量
                            if (isCrossTx) {
                                corssTxCount++;
                            }
                            //计算合约交易的数量
                            if (isSmartContractTx) {
                                contractTxCount++;
                            }
                            //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
                            TxUtil.moduleGroups(moduleVerifyMap, txRegister, RPCUtil.encode(transaction.serialize()));
                        }
                        //更新到当前最新区块交易大小总值
                        totalSizeTemp = totalSize;
                        packingTxList.addAll(currentBatchPackableTxs);

                        //批次结束重置数据
                        batchProcessList.clear();
                        currentBatchPackableTxs.clear();
                        batchCorssTxCount = 0;
                        batchContractTxCount = 0;
                        if(maxDataSize){
                            break;
                        }
                    }
                } catch (Exception e) {
                    currentBatchPackableTxs.clear();
                    nulsLogger.error("打包交易异常, txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                    nulsLogger.error(e);
                    continue;
                }

            }
            //循环获取交易使用时间
            whileTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            if(nulsLogger.isDebugEnabled()) {
                nulsLogger.debug("-取出的交易 -count:{} - data size:{}", packingTxList.size(), totalSize);
            }

            boolean contractBefore = false;
            if (contractNotify) {
                contractBefore = ContractCall.contractBatchBeforeEnd(chain, blockHeight, 0);
            }
            //处理智能合约
            String stateRoot = preStateRoot;
            boolean hasTxbackPackablePool = false;
            long contractStart = NulsDateUtils.getCurrentTimeMillis();
            /** 智能合约 当通知标识为true, 则表明有智能合约被调用执行*/
            List<String> contractGenerateTxs = new ArrayList<>();
            if (contractNotify && !chain.getContractTxFail()) {
                //处理智能合约执行结果
                Map map = processContractResult(chain, packingTxList, orphanTxSet, contractGenerateTxs, blockHeight, contractBefore, stateRoot);
                stateRoot = (String) map.get("stateRoot");
                hasTxbackPackablePool = (boolean) map.get("hasTxbackPackablePool");
            }
            //如果合约invoke时有需要还回去的合约交易,或者合约执行结果有还回去的交易,都需要重新验证账本
            if(stopInvokeContract || hasTxbackPackablePool){
                //如果智能合约有退回或者验证不通过的交易 则需要再次账本验证
                verifyAgain(chain, moduleVerifyMap, packingTxList, orphanTxSet, true);
            }
            long contractTime = NulsDateUtils.getCurrentTimeMillis() - contractStart;

            //模块统一验证器
            long batchStart = NulsDateUtils.getCurrentTimeMillis();
            txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList, orphanTxSet);
            //模块统一验证使用总时间
            batchModuleTime = NulsDateUtils.getCurrentTimeMillis() - batchStart;

            List<String> packableTxs = new ArrayList<>();
            Iterator<TxPackageWrapper> iterator = packingTxList.iterator();
            Map<NulsHash, Integer> txPackageOrphanMap = chain.getTxPackageOrphanMap();
            while (iterator.hasNext()) {
                TxPackageWrapper txPackageWrapper = iterator.next();
                Transaction tx = txPackageWrapper.getTx();
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
                    TxPackageWrapper txPackageWrapper = packingTxList.get(i);
                    Transaction tx = txPackageWrapper.getTx();
                    //执行交易基础验证
                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                    if (null == txRegister) {
                        throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
                    }
                    baseValidateTx(chain, tx, txRegister);
                    chain.getUnverifiedQueue().addLast(new TransactionNetPO(txPackageWrapper.getTx()));
                }
                return new TxPackage(new ArrayList<>(), preStateRoot, chain.getBestBlockHeight() + 1);
            }
            //检测预留传输时间
            long current = NulsDateUtils.getCurrentTimeMillis();
            if (endtimestamp - current < packageRpcReserveTime) {
                //超时,留给最后数据组装和RPC传输时间不足
                nulsLogger.error("getPackableTxs time out, endtimestamp:{}, current:{}, endtimestamp-current:{}, reserveTime:{}",
                        endtimestamp, current, endtimestamp - current, packageRpcReserveTime);
                throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
            }

            TxPackage txPackage = new TxPackage(packableTxs, stateRoot, blockHeight);

            long totalTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            nulsLogger.info("[打包时间统计]  总执行时间:{}, 剩余时间:{}, 打包可用时间:{}, 获取交易(循环)总等待时间:{}, " +
                            "获取交易(循环)执行时间:{}, 获取交易(循环)验证账本总时间:{}, 模块统一验证执行时间:{}, " +
                            "合约执行时间:{},", totalTime, endtimestamp - NulsDateUtils.getCurrentTimeMillis(),
                    packingTime, allSleepTime, whileTime, totalLedgerTime, batchModuleTime,
                    contractTime);

            nulsLogger.info("[Package end] - height:{} - 本次打包交易数:{} - 当前待打包队列交易hash数:{}, - 待打包队列实际交易数:{}" + TxUtil.nextLine(),
                    blockHeight, packableTxs.size(), packablePool.packableHashQueueSize(chain), packablePool.packableTxMapSize(chain));

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
     *
     * @param chain
     * @param batchProcessList
     * @param currentBatchPackableTxs
     * @param orphanTxSet
     * @param proccessContract 是否处理智能合约
     * @param orphanNoCount (是否因为合约还回去而再次验证账本)孤儿交易还回去的时候 不计算还回去的次数
     * @throws NulsException
     */
    private void verifyLedger(Chain chain, List<String> batchProcessList, List<TxPackageWrapper> currentBatchPackableTxs,
                              Set<TxPackageWrapper> orphanTxSet, boolean proccessContract, boolean orphanNoCount) throws NulsException {
        //开始处理
        Map verifyCoinDataResult = LedgerCall.verifyCoinDataBatchPackaged(chain, batchProcessList);
        List<String> failHashs = (List<String>) verifyCoinDataResult.get("fail");
        List<String> orphanHashs = (List<String>) verifyCoinDataResult.get("orphan");
        if (!failHashs.isEmpty() || !orphanHashs.isEmpty()) {
            chain.getLogger().error("Package verify Ledger fail tx count:{}", failHashs.size());
            chain.getLogger().error("Package verify Ledger orphan tx count:{}", orphanHashs.size());

            Iterator<TxPackageWrapper> it = currentBatchPackableTxs.iterator();
            boolean backContract = false;
            removeAndGo:
            while (it.hasNext()) {
                TxPackageWrapper txPackageWrapper = it.next();
                Transaction transaction = txPackageWrapper.getTx();
                //去除账本验证失败的交易
                for (String hash : failHashs) {
                    String hashStr = transaction.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        if (!backContract && proccessContract && TxManager.isUnSystemSmartContract(chain, transaction.getType())) {
                            //设置标志,如果是智能合约的非系统交易,未验证通过,则需要将所有非系统智能合约交易还回待打包队列.
                            backContract = true;
                        } else {
                            clearInvalidTx(chain, transaction);
                        }
                        it.remove();
                        continue removeAndGo;
                    }
                }
                //去除孤儿交易, 同时把孤儿交易放入孤儿池
                for (String hash : orphanHashs) {
                    String hashStr = transaction.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        if (!backContract && proccessContract && TxManager.isUnSystemSmartContract(chain, transaction.getType())) {
                            //设置标志, 如果是智能合约的非系统交易,未验证通过,则需要将所有非系统智能合约交易还回待打包队列.
                            backContract = true;
                        } else {
                            //孤儿交易
                            if(orphanNoCount){
                                //如果是因为合约还回去之后,验证账本为孤儿交易则不需要计数 直接还回
                                orphanTxSet.add(txPackageWrapper);
                            }else {
                                addOrphanTxSet(chain, orphanTxSet, txPackageWrapper);
                            }
                        }
                        it.remove();
                        continue removeAndGo;
                    }
                }
            }
            //如果有智能合约的非系统交易未验证通过,则需要将所有非系统智能合约交易还回待打包队列.
            if (backContract && proccessContract) {
                Iterator<TxPackageWrapper> its = currentBatchPackableTxs.iterator();
                while (its.hasNext()) {
                    TxPackageWrapper txPackageWrapper = it.next();
                    Transaction transaction = txPackageWrapper.getTx();
                    if (TxManager.isUnSystemSmartContract(chain, transaction.getType())) {
                        //如果是智能合约的非系统交易,未验证通过,则需要将所有非系统智能合约交易还回待打包队列.
                        packablePool.offerFirstOnlyHash(chain, transaction);
                        chain.setContractTxFail(true);
                        it.remove();
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
    private Map processContractResult(Chain chain, List<TxPackageWrapper> packingTxList, Set<TxPackageWrapper> orphanTxSet, List<String> contractGenerateTxs,
                                      long blockHeight, boolean contractBefore, String stateRoot) throws IOException {

        boolean hasTxbackPackablePool = false;
        /**当contractBefore通知失败,或者contractBatchEnd失败则需要将智能合约交易还回待打包队列*/
        boolean isRollbackPackablePool = false;
        if (!contractBefore) {
            isRollbackPackablePool = true;
        } else {
            try {
                Map<String, Object> map = ContractCall.contractPackageBatchEnd(chain, blockHeight);
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
                        for (TxPackageWrapper txPackageWrapper : packingTxList) {
                            Transaction tx = txPackageWrapper.getTx();
                            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                            if (txRegister.getModuleCode().equals(ModuleE.CS.abbr)) {
                                consensusList.add(RPCUtil.encode(txPackageWrapper.getTx().serialize()));
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
                if (!isRollbackPackablePool) {
                    //如果合约交易不需要全部放回待打包队列,就检查如果存在未执行的智能合约,则放回待打包队列,下次执行。
                    List<String> nonexecutionList = (List<String>) map.get("pendingTxHashList");
                    if (null != nonexecutionList && !nonexecutionList.isEmpty()) {
                        chain.getLogger().debug("contract pending tx count:{} ", nonexecutionList.size());
                        Iterator<TxPackageWrapper> iterator = packingTxList.iterator();
                        while (iterator.hasNext()) {
                            TxPackageWrapper txPackageWrapper = iterator.next();
                            for (String hash : nonexecutionList) {
                                if (hash.equals(txPackageWrapper.getTx().getHash().toHex())) {
                                    orphanTxSet.add(txPackageWrapper);
                                    //从可打包集合中删除
                                    iterator.remove();
                                    if(!hasTxbackPackablePool){
                                        hasTxbackPackablePool = true;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                isRollbackPackablePool = true;
            }
        }
        if (isRollbackPackablePool) {
            Iterator<TxPackageWrapper> iterator = packingTxList.iterator();
            while (iterator.hasNext()) {
                TxPackageWrapper txPackageWrapper = iterator.next();
                if (TxManager.isUnSystemSmartContract(chain, txPackageWrapper.getTx().getType())) {
                    /**
                     * 智能合约出现需要加回待打包队列的情况,没有加回次数限制,
                     * 不需要比对TX_PACKAGE_ORPHAN_MAP的阈值,直接加入集合,可以与孤儿交易合用一个集合
                     */
                    orphanTxSet.add(txPackageWrapper);
                    //从可打包集合中删除
                    iterator.remove();
                    if(!hasTxbackPackablePool){
                        hasTxbackPackablePool = true;
                    }
                }
            }
        }
        Map rs = new HashMap();
        rs.put("stateRoot", stateRoot);
        rs.put("hasTxbackPackablePool", hasTxbackPackablePool);
        return rs;
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
    private boolean processContractConsensusTx(Chain chain, TxRegister consensusTxRegister, List<String> consensusList, List<TxPackageWrapper> packingTxList, boolean batchVerify) throws NulsException {
        while (true) {
            List<String> txHashList = null;
            try {
                txHashList = TransactionCall.txModuleValidator(chain, consensusTxRegister.getModuleCode(), consensusList);
            } catch (NulsException e) {
                chain.getLogger().error("Package module verify failed -txModuleValidator Exception:{}, module-code:{}, count:{} , return count:{}",
                        BaseConstant.TX_VALIDATOR, consensusTxRegister.getModuleCode(), consensusList.size(), txHashList.size());
                txHashList = new ArrayList<>(consensusList.size());
                for (String txStr : consensusList) {
                    Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
                    txHashList.add(tx.getHash().toHex());
                }
            }
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
                Iterator<TxPackageWrapper> its = packingTxList.iterator();
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
    private void addOrphanTxSet(Chain chain, Set<TxPackageWrapper> orphanTxSet, TxPackageWrapper txPackageWrapper) {
        NulsHash hash = txPackageWrapper.getTx().getHash();
        Integer count = chain.getTxPackageOrphanMap().get(hash);
        if (count == null || count < TxConstant.PACKAGE_ORPHAN_MAXCOUNT) {
            orphanTxSet.add(txPackageWrapper);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            if(chain.getTxPackageOrphanMap().size() > TxConstant.PACKAGE_ORPHAN_MAP_MAXCOUNT){
                chain.getTxPackageOrphanMap().clear();
            }
            chain.getTxPackageOrphanMap().put(hash, count);
        } else {
            //不加回(丢弃), 同时删除map中的key,并清理
            chain.getLogger().debug("超过5次孤儿交易 hash:{}", hash.toHex());
            clearInvalidTx(chain, txPackageWrapper.getTx());
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
    private void putBackPackablePool(Chain chain, List<TxPackageWrapper> txList, Set<TxPackageWrapper> orphanTxSet) {
        if (null == txList) {
            txList = new ArrayList<>();
        }
        if (null != orphanTxSet && !orphanTxSet.isEmpty()) {
            txList.addAll(orphanTxSet);
        }
        if (txList.isEmpty()) {
            return;
        }
        //孤儿交易排倒序,全加回待打包队列去
        txList.sort(new Comparator<TxPackageWrapper>() {
            @Override
            public int compare(TxPackageWrapper o1, TxPackageWrapper o2) {
                return o1.compareTo(o2.getIndex());
            }
        });
        for (TxPackageWrapper txPackageWrapper : txList) {
            packablePool.offerFirstOnlyHash(chain, txPackageWrapper.getTx());
        }
        chain.getLogger().info("putBackPackablePool count:{}", txList.size());
    }

    private void putBackPackablePool(Chain chain, Set<TxPackageWrapper> orphanTxSet) {
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
    private boolean txModuleValidatorPackable(Chain chain, Map<String, List<String>> moduleVerifyMap, List<TxPackageWrapper> packingTxList, Set<TxPackageWrapper> orphanTxSet) throws NulsException {
        Iterator<Map.Entry<String, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> moduleList = entry.getValue();
            if (moduleList.size() == 0) {
                //当递归中途模块交易被过滤完后会造成list为空,这时不需要再调用模块统一验证器
                it.remove();
                continue;
            }
            String moduleCode = entry.getKey();
            List<String> txHashList = null;
            try {
                txHashList = TransactionCall.txModuleValidator(chain, moduleCode, moduleList);
            } catch (NulsException e) {
                chain.getLogger().error("Package module verify failed -txModuleValidator Exception:{}, module-code:{}, count:{} , return count:{}",
                        BaseConstant.TX_VALIDATOR, moduleCode, moduleList.size(), txHashList.size());
                //出错则删掉整个模块的交易
                Iterator<TxPackageWrapper> its = packingTxList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next().getTx();
                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                    if (txRegister.getModuleCode().equals(moduleCode)) {
                        clearInvalidTx(chain, tx);
                        its.remove();
                    }
                }
                continue;
            }
            if (null == txHashList || txHashList.isEmpty()) {
                //模块统一验证没有冲突的，从map中干掉
                it.remove();
                continue;
            }
            chain.getLogger().debug("[Package module verify failed] module:{}, module-code:{}, count:{} , return count:{}",
                    BaseConstant.TX_VALIDATOR, moduleCode, moduleList.size(), txHashList.size());
            /**冲突检测有不通过的, 执行清除和未确认回滚 从packingTxList删除*/
            for (int i = 0; i < txHashList.size(); i++) {
                String hash = txHashList.get(i);
                Iterator<TxPackageWrapper> its = packingTxList.iterator();
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
        verifyAgain(chain, moduleVerifyMap, packingTxList, orphanTxSet, false);
        return txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList, orphanTxSet);
    }

    /**
     *
     * @param chain
     * @param moduleVerifyMap
     * @param packingTxList
     * @param orphanTxSet
     * @param orphanNoCount (是否因为合约还回去而再次验证账本)孤儿交易还回去的时候 不计算还回去的次数
     * @throws NulsException
     */
    private void verifyAgain(Chain chain, Map<String, List<String>> moduleVerifyMap, List<TxPackageWrapper> packingTxList, Set<TxPackageWrapper> orphanTxSet, boolean orphanNoCount) throws NulsException {
        chain.getLogger().debug("------ verifyAgain 打包再次批量校验通知 ------");
        //向账本模块发送要批量验证coinData的标识
        LedgerCall.coinDataBatchNotify(chain);
        List<String> batchProcessList = new ArrayList<>();
        for (TxPackageWrapper txPackageWrapper : packingTxList) {
            if (TxManager.isSystemSmartContract(chain, txPackageWrapper.getTx().getType())) {
                //智能合约系统交易不需要验证账本
                continue;
            }
            batchProcessList.add(txPackageWrapper.getTxHex());
        }
        verifyLedger(chain, batchProcessList, packingTxList, orphanTxSet, true, orphanNoCount);

        for (TxPackageWrapper txPackageWrapper : packingTxList) {
            Transaction tx = txPackageWrapper.getTx();
            TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
        }
    }

    @Override
    public Map<String, Object> batchVerify(Chain chain, List<String> txStrList, BlockHeader blockHeader, String blockHeaderStr, String preStateRoot) throws NulsException {
        NulsLogger logger = chain.getLogger();
        long s1 = NulsDateUtils.getCurrentTimeMillis();
        long blockHeight = blockHeader.getHeight();
        if(logger.isDebugEnabled()) {
            logger.debug("[验区块交易] 开始 -----高度:{} -----区块交易数:{}", blockHeight, txStrList.size());
        }

        List<TxVerifyWrapper> txList = new ArrayList<>();
        //智能合约通知标识,出现的第一个智能合约交易并且调用验证器通过时,有则只第一次时通知.
        boolean contractNotify = false;
        long blockTime = blockHeader.getTime();
        List<Future<Boolean>> futures = new ArrayList<>();
        //组装统一验证参数数据,key为各模块统一验证器cmd
        Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        int chainId = chain.getChainId();
        long timeF1 = 0L;
        long timeF2 = 0L;
        long timeF3 = 0L;
        long timeF4 = 0L;
        List<byte[]> keys = new ArrayList<>();

        long f1 = System.currentTimeMillis();
        for (String txStr : txStrList) {
            Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
            txList.add(new TxVerifyWrapper(tx, txStr));
            int type = tx.getType();

            TxRegister txRegister = TxManager.getTxRegister(chain, type);
            if (null == txRegister) {
                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
            }
            // add by pierre at 2019-11-02 需要协议升级
            // add by pierre at 2019-10-22 跨链转账交易发送到智能合约模块进行解析，是否为合约资产跨链转账
            boolean isCrossTransferTx = TxType.CROSS_CHAIN == type;
            // end code by pierre
            /** 智能合约*/
            if (TxManager.isUnSystemSmartContract(txRegister) || isCrossTransferTx) {
                /** 出现智能合约,且通知标识为false,则先调用通知 */
                if (!contractNotify) {
                    String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress(chain.getChainId()));
                    ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot, 1);
                    contractNotify = true;
                }
                try {
                    if (!ContractCall.invokeContract(chain, RPCUtil.encode(tx.serialize()), 1)) {
                        if(logger.isDebugEnabled()) {
                            logger.debug("batch verify failed. invokeContract fail");
                        }
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                } catch (IOException e) {
                    throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
                }
            }
            //如果不是系统智能合约就继续单个验证
            if (TxManager.isSystemSmartContract(txRegister)) {
                continue;
            }

            keys.add(tx.getHash().getBytes());
            //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
            TxUtil.moduleGroups(moduleVerifyMap, txRegister, txStr);
        }

        long f2 = System.currentTimeMillis();
        timeF1 = f2 - f1;
        //验证交易是否已确认过
        List<byte[]> confirmedList = confirmedTxStorageService.getExistTxs(chainId, keys);
        if (!confirmedList.isEmpty()) {
            logger.error("There are confirmed transactions");
            try {
                for (byte[] cfmtx : confirmedList) {
                    logger.error("confirmed hash:{}", TxUtil.getTransaction(cfmtx).getHash().toHex());
                }
            } finally {
                logger.error("Show confirmed transaction deserialize fail");
                throw new NulsException(TxErrorCode.TX_CONFIRMED);
            }
        }
        long f3 = System.currentTimeMillis();
        timeF2 = f3 - f2;

        //验证本地没有的交易
        List<String> unconfirmedList = unconfirmedTxStorageService.getExistKeysStr(chainId, keys);

        long f4 = System.currentTimeMillis();
        timeF3 = f4 - f3;

        Set<String> set = new HashSet<>();
        set.addAll(unconfirmedList);
        unconfirmedList = null;
        long d = 0L;
        for (TxVerifyWrapper txVerifyWrapper : txList) {
            Transaction tx = txVerifyWrapper.getTx();
            //能加入表明未确认中没有,则需要处理
            if (set.add(tx.getHash().toHex())) {
                long d1 = System.currentTimeMillis();
                //不在未确认中就进行基础验证
                //多线程处理单个交易
                Future<Boolean> res = verifySignExecutor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            //只验证单个交易的基础内容(TX模块本地验证)
                            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                            if (null == txRegister) {
                                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
                            }
                            //logger.debug("验证区块时本地没有的交易, 需要进行基础验证 hash:{}", tx.getHash().toHex());
                            baseValidateTx(chain, tx, txRegister);
                        } catch (Exception e) {
                            logger.error("batchVerify failed, single tx verify failed. hash:{}, -type:{}", tx.getHash().toHex(), tx.getType());
                            logger.error(e);
                            return false;
                        }
                        return true;
                    }
                });
                futures.add(res);
                d += (System.currentTimeMillis() - d1);
            }
        }

        if(logger.isDebugEnabled()) {
            timeF4 = System.currentTimeMillis() - f4;
            logger.debug("[验区块交易] 反序列化,合约,分组:{} -是否确认过:{} -是否在未确认中:{}, -单个验证:{} -单内部处理:{} -合计时间:{}",
                    timeF1, timeF2, timeF3, d, timeF4, NulsDateUtils.getCurrentTimeMillis() - s1);
        }

        if (contractNotify) {
            if (!ContractCall.contractBatchBeforeEnd(chain, blockHeight, 1)) {
                if(logger.isDebugEnabled()) {
                    logger.debug("batch verify failed. contractBatchBeforeEnd fail");
                }
                throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
            }
        }

        long coinDataV = NulsDateUtils.getCurrentTimeMillis();
        //账本验证
        if (!LedgerCall.verifyBlockTxsCoinData(chain, txStrList, blockHeight)) {
            if(logger.isDebugEnabled()) {
                logger.debug("batch verifyCoinData failed.");
            }
            throw new NulsException(TxErrorCode.TX_LEDGER_VERIFY_FAIL);
        }
        if(logger.isDebugEnabled()) {
            logger.debug("[验区块交易] coinData -距方法开始的时间:{}，-验证时间:{}",
                    NulsDateUtils.getCurrentTimeMillis() - s1, NulsDateUtils.getCurrentTimeMillis() - coinDataV);
        }

        //模块统一验证器
        long moduleV = NulsDateUtils.getCurrentTimeMillis();
        Iterator<Map.Entry<String, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> txHashList = TransactionCall.txModuleValidator(chain,
                    entry.getKey(), entry.getValue(), blockHeaderStr);
            if (txHashList != null && txHashList.size() > 0) {
                if(logger.isDebugEnabled()) {
                    logger.debug("batch module verify fail, module-code:{},  return count:{}", entry.getKey(), txHashList.size());
                }
                throw new NulsException(TxErrorCode.TX_VERIFY_FAIL);
            }
        }
        if(logger.isDebugEnabled()) {
            logger.debug("[验区块交易] 模块统一验证时间:{}", NulsDateUtils.getCurrentTimeMillis() - moduleV);
            logger.debug("[验区块交易] 模块统一验证 -距方法开始的时间:{}", NulsDateUtils.getCurrentTimeMillis() - s1);
        }

        /** 智能合约 当通知标识为true, 则表明有智能合约被调用执行*/
        List<String> scNewList = new ArrayList<>();
        String scStateRoot = preStateRoot;
        if (contractNotify) {
            Map<String, Object> map = null;
            try {
                map = ContractCall.contractBatchEnd(chain, blockHeight);
            } catch (NulsException e) {
                logger.error(e);
                throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
            }
            scStateRoot = (String) map.get("stateRoot");

            scNewList = (List<String>) map.get("txList");
            if (null == scNewList) {
                logger.error("contract new txs is null");
                throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
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
                for (TxVerifyWrapper txVerifyWrapper : txList) {
                    Transaction tx = txVerifyWrapper.getTx();
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
                        consensusList.add(txVerifyWrapper.getTxStr());
                        if (null == consensusTxRegister) {
                            consensusTxRegister = txRegister;
                        }
                    }
                }
                if (consensusTxRegister == null) {
                    consensusTxRegister = TxManager.getTxRegister(chain, TxType.REGISTER_AGENT);
                }
                consensusList.addAll(scNewConsensusList);
                boolean rsProcess = processContractConsensusTx(chain, consensusTxRegister, consensusList, null, true);
                if (rsProcess) {
                    logger.error("contract tx consensus module verify fail.");
                    throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                }
            }
            //验证智能合约gas返回的交易hex 是否正确.打包时返回的交易是加入到区块交易的队尾
            int size = scNewList.size();
            if (size > 0) {
                int txSize = txStrList.size();
                String scNewTxHex = null;
                for (int i = size - 1; i >= 0; i--) {
                    String hex = scNewList.get(i);
                    int txType = TxUtil.extractTxTypeFromTx(hex);
                    if (txType == TxType.CONTRACT_RETURN_GAS) {
                        scNewTxHex = hex;
                        break;
                    }
                }
                if (scNewTxHex != null) {
                    String receivedScNewTxHex = null;
                    boolean rs = false;
                    for (int i = txSize - 1; i >= 0; i--) {
                        String txHex = txStrList.get(i);
                        int txType = TxUtil.extractTxTypeFromTx(txHex);
                        if (txType == TxType.CONTRACT_RETURN_GAS) {
                            receivedScNewTxHex = txHex;
                            if (txHex.equals(scNewTxHex)) {
                                rs = true;
                            }
                            break;
                        }
                    }
                    if (!rs) {
                        logger.error("contract error.生成的合约gas返还交易:{}, - 收到的合约gas返还交易：{}", scNewTxHex, receivedScNewTxHex);
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                    //返回智能合约交易给区块
                    scNewList.remove(scNewTxHex);
                }
            }
        }
        //stateRoot发到共识,处理完再比较
        String coinBaseTx = null;
        for (TxVerifyWrapper txVerifyWrapper : txList) {
            Transaction tx = txVerifyWrapper.getTx();
            if (tx.getType() == TxType.COIN_BASE) {
                coinBaseTx = txVerifyWrapper.getTxStr();
                break;
            }
        }
        String stateRootNew = ConsensusCall.triggerCoinBaseContract(chain, coinBaseTx, blockHeaderStr, scStateRoot);
        String stateRoot = RPCUtil.encode(blockHeader.getExtendsData().getStateRoot());
        if (!stateRoot.equals(stateRootNew)) {
            logger.warn("contract stateRoot error.");
            throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
        }

        //多线程处理结果
        try {
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    logger.error("batchVerify failed, single tx verify failed");
                    throw new NulsException(TxErrorCode.TX_VERIFY_FAIL);
                }
            }
        } catch (InterruptedException e) {
            logger.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        } catch (ExecutionException e) {
            logger.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        if(logger.isDebugEnabled()) {
            logger.debug("[验区块交易] 合计执行时间:{}, - 高度:{} - 区块交易数:{}" + TxUtil.nextLine(),
                    NulsDateUtils.getCurrentTimeMillis() - s1, blockHeight, txStrList.size());
        }
        Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
        resultMap.put("value", true);
        resultMap.put("contractList", scNewList);
        return resultMap;
    }


    @Override
    public void clearInvalidTx(Chain chain, Transaction tx) {
        clearInvalidTx(chain, tx, true);
    }

    @Override
    public void clearInvalidTx(Chain chain, Transaction tx, boolean changeStatus) {
        unconfirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
        //从待打包队里存交易的map中移除
        ByteArrayWrapper wrapper = new ByteArrayWrapper(tx.getHash().getBytes());
        chain.getPackableTxMap().remove(wrapper);
        //从待打包队列中存实际交易的的map中移除该笔交易
        packablePool.removeInvalidTxFromMap(chain, tx);
        //判断如果交易已被确认就不用调用账本清理了!!
        TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
        if (txConfirmed == null) {
            try {
                //如果是清理机制调用, 则调用账本未确认回滚
                LedgerCall.rollBackUnconfirmTx(chain, RPCUtil.encode(tx.serialize()));
                if (changeStatus) {
                    //通知账本状态变更
                    LedgerCall.rollbackTxValidateStatus(chain, RPCUtil.encode(tx.serialize()));
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
    }

}
