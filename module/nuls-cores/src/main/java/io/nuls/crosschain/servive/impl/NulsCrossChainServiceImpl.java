package io.nuls.crosschain.servive.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.crosschain.base.model.dto.input.CoinDTO;
import io.nuls.crosschain.base.model.dto.input.CrossTxTransferDTO;
import io.nuls.crosschain.base.service.CrossChainService;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.constant.ParamConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.rpc.call.AccountCall;
import io.nuls.crosschain.rpc.call.BlockCall;
import io.nuls.crosschain.rpc.call.ChainManagerCall;
import io.nuls.crosschain.rpc.call.TransactionCall;
import io.nuls.crosschain.srorage.*;
import io.nuls.crosschain.utils.CommonUtil;
import io.nuls.crosschain.utils.LoggerUtil;
import io.nuls.crosschain.utils.TxUtil;
import io.nuls.crosschain.utils.manager.ChainManager;
import io.nuls.crosschain.utils.manager.CoinDataManager;
import io.nuls.crosschain.utils.thread.CrossTxHandler;
import io.nuls.crosschain.utils.validator.CrossTxValidator;

import java.io.IOException;
import java.util.*;

/**
 * 跨链模块默认接口实现类
 *
 * @author tag
 * @date 2019/4/9
 */
@Component
public class NulsCrossChainServiceImpl implements CrossChainService {
    @Autowired
    private ChainManager chainManager;

    @Autowired
    private NulsCoresConfig config;

    @Autowired
    private CoinDataManager coinDataManager;

    @Autowired
    private CrossTxValidator txValidator;

    @Autowired
    private SendHeightService sendHeightService;

    @Autowired
    private ConvertHashService convertHashService;

    @Autowired
    private CtxStateService ctxStateService;

    @Autowired
    private ConvertCtxService convertCtxService;

    @Autowired
    private CtxStatusService ctxStatusService;

    @Autowired
    private CommitedOtherCtxService otherCtxService;

    private Map<Integer, Set<NulsHash>> verifiedCtxMap = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public Result createCrossTx(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        CrossTxTransferDTO crossTxTransferDTO = JSONUtils.map2pojo(params, CrossTxTransferDTO.class);
        int chainId = crossTxTransferDTO.getChainId();
        if (chainId <= NulsCrossChainConstant.CHAIN_ID_MIN) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(NulsCrossChainErrorCode.CHAIN_NOT_EXIST);
        }
        if (!chainManager.isCrossNetUseAble()) {
            chain.getLogger().info("跨链网络组网异常！");
            return Result.getFailed(NulsCrossChainErrorCode.CROSS_CHAIN_NETWORK_UNAVAILABLE);
        }
        Transaction tx = new Transaction(config.getCrossCtxType());
        try {
            tx.setRemark(StringUtils.bytes(crossTxTransferDTO.getRemark()));
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            List<CoinFrom> coinFromList = coinDataManager.assemblyCoinFrom(chain, crossTxTransferDTO.getListFrom(), false);
            List<CoinTo> coinToList = coinDataManager.assemblyCoinTo(crossTxTransferDTO.getListTo(), chain);
            coinDataManager.verifyCoin(coinFromList, coinToList, chain);
            int txSize = tx.size();
//            txSize += P2PHKSignature.SERIALIZE_LENGTH;
            CoinData coinData = coinDataManager.getCrossCoinData(chain, coinFromList, coinToList, txSize, config.isMainNet());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            //签名
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            List<String> signedAddressList = new ArrayList<>();
            for (CoinDTO coinDTO : crossTxTransferDTO.getListFrom()) {
                if (!signedAddressList.contains(coinDTO.getAddress())) {
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(coinDTO.getAddress(), coinDTO.getPassword(), tx.getHash().getBytes());
                    p2PHKSignatures.add(p2PHKSignature);
                    signedAddressList.add(coinDTO.getAddress());
                }
            }
            if (!txValidator.coinDataValid(chain, coinData, tx.size())) {
                chain.getLogger().error("跨链交易CoinData验证失败！\n\n");
                return Result.getFailed(NulsCrossChainErrorCode.COINDATA_VERIFY_FAIL);
            }
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());

            if (!TransactionCall.sendTx(chain, RPCUtil.encode(tx.serialize()))) {
                chain.getLogger().error("跨链交易发送交易模块失败\n\n");
                throw new NulsException(NulsCrossChainErrorCode.INTERFACE_CALL_FAILED);
            }
            Map<String, Object> result = new HashMap<>(2);
            result.put(ParamConstant.TX_HASH, tx.getHash().toHex());
            return Result.getSuccess(CommonCodeConstanst.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(CommonCodeConstanst.SERIALIZE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result newApiModuleCrossTx(Map<String, Object> params) {
        if (params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.TX) == null) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(ParamConstant.CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(NulsCrossChainErrorCode.CHAIN_NOT_EXIST);
        }
        String txStr = (String) params.get(ParamConstant.TX);
        try {
            Transaction tx = new Transaction();
            tx.parse(RPCUtil.decode(txStr), 0);
            CoinData coinData = tx.getCoinDataInstance();
            if (!txValidator.coinDataValid(chain, coinData, tx.size())) {
                chain.getLogger().error("跨链交易CoinData验证失败！\n\n");
                return Result.getFailed(NulsCrossChainErrorCode.COINDATA_VERIFY_FAIL);
            }

            if (!TransactionCall.sendTx(chain, RPCUtil.encode(tx.serialize()))) {
                chain.getLogger().error("跨链交易发送交易模块失败\n\n");
                throw new NulsException(NulsCrossChainErrorCode.INTERFACE_CALL_FAILED);
            }

            Map<String, Object> result = new HashMap<>(2);
            result.put(ParamConstant.TX_HASH, tx.getHash().toHex());
            result.put("success", true);
            return Result.getSuccess(CommonCodeConstanst.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(CommonCodeConstanst.SERIALIZE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result validCrossTx(Map<String, Object> params) {
        if (params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.TX) == null) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(ParamConstant.CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(NulsCrossChainErrorCode.CHAIN_NOT_EXIST);
        }
        String txStr = (String) params.get(ParamConstant.TX);
        try {
            Transaction transaction = new Transaction();
            transaction.parse(RPCUtil.decode(txStr), 0);
            if (!txValidator.validateTx(chain, transaction, null)) {
                chain.getLogger().error("跨链交易验证失败,Hash:{}\n", transaction.getHash().toHex());
                return Result.getFailed(NulsCrossChainErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> validResult = new HashMap<>(2);
            validResult.put(ParamConstant.VALUE, true);
            chain.getLogger().info("跨链交易验证成功，Hash:{}\n", transaction.getHash().toHex());
            return Result.getSuccess(CommonCodeConstanst.SUCCESS).setData(validResult);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(CommonCodeConstanst.SERIALIZE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean commitCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        int syncStatus = BlockCall.getBlockStatus(chain);
        try {
            List<NulsHash> convertHashList = new ArrayList<>();
            List<NulsHash> ctxStatusList = new ArrayList<>();
            List<NulsHash> otherCtxList = new ArrayList<>();
            for (Transaction ctx : txs) {
                NulsHash ctxHash = ctx.getHash();
                if (verifiedCtxMap.get(chainId) != null) {
                    verifiedCtxMap.get(chainId).remove(ctxHash);
                }
                CoinData coinData = ctx.getCoinDataInstance();
                int fromChainId = AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                if (chainId == toChainId) {
                    //跨入
                    NulsHash convertHash = ctxHash;
                    if (!config.isMainNet()) {
                        convertHash = TxUtil.friendConvertToMain(chain, ctx, TxType.CROSS_CHAIN).getHash();
                    }
                    if (!convertHashService.save(convertHash, ctxHash, chainId)) {
                        rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                        return false;
                    }
                    convertHashList.add(convertHash);
                    if (!otherCtxService.save(convertHash, ctx, chainId)) {
                        rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                        return false;
                    }
                    otherCtxList.add(convertHash);
                } else {
                    //跨出
                    if (!config.isMainNet()) {
                        NulsHash convertHash = TxUtil.friendConvertToMain(chain, ctx, TxType.CROSS_CHAIN).getHash();
                        if (!convertHashService.save(convertHash, ctxHash, chainId)) {
                            rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                            return false;
                        }
                        convertHashList.add(convertHash);
                    }
                    //如果当前链不为发起链，则本链为主网中转链需清空签名在对交易对签名拜占庭，避免其他链向本链获取交易时失败
                    if (chainId != fromChainId) {
                        ctx.setTransactionSignature(null);
                        if (!otherCtxService.save(ctxHash, ctx, chainId)) {
                            rollbackCtx(convertHashList, ctxStatusList, otherCtxList, chainId);
                            return false;
                        }
                        otherCtxList.add(ctxHash);
                    }
                    //回滚重新打包，如果当前跨链交易已处理完成，则不需要重复处理
                    CtxStatusPO ctxStatusPO = ctxStatusService.get(ctxHash, chainId);
                    if(ctxStatusPO != null){
                        if(ctxStatusPO.getStatus() == TxStatusEnum.CONFIRMED.getStatus()){
                            chain.getLogger().info("该跨链转账交易之前已处理完成，不需重复处理：{}",ctxHash.toHex() );
                            continue;
                        }
                    }
                    ctxStatusList.add(ctxHash);
                    chain.getLogger().debug("跨链交易提交完成，对跨链转账交易做拜占庭验证：{}", ctxHash.toHex());
                    //发起拜占庭验证
                    ctx.setTransactionSignature(null);
                    chain.getCrossTxThreadPool().execute(new CrossTxHandler(chain, ctx, syncStatus));
                }
            }
            //如果本链为主网通知跨链管理模块发起链与接收链资产变更
            if(config.isMainNet()){
                List<String> txStrList = new ArrayList<>();
                for (Transaction tx : txs) {
                    txStrList.add(RPCUtil.encode(tx.serialize()));
                }
                String headerStr = RPCUtil.encode(blockHeader.serialize());
                ChainManagerCall.ctxAssetCirculateCommit(chainId, txStrList, headerStr);
            }
            chain.getLogger().info("高度：{} 的跨链交易提交完成\n", blockHeader.getHeight());
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean rollbackCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        try {
            for (Transaction ctx : txs) {
                CoinData coinData = ctx.getCoinDataInstance();
                int fromChainId = AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                NulsHash ctxHash = ctx.getHash();
                if (chainId == fromChainId) {
                    CtxStatusPO ctxStatusPO = ctxStatusService.get(ctxHash, chainId);
                    if(ctxStatusPO != null){
                        if(ctxStatusPO.getStatus() == TxStatusEnum.CONFIRMED.getStatus()){
                            chain.getLogger().info("该跨链转账交易已处理完成，不需回滚：{}",ctxHash.toHex() );
                            continue;
                        }
                    }
                    if (!ctxStatusService.delete(ctxHash, chainId) || !convertHashService.delete(ctxHash, chainId)) {
                        return false;
                    }
                } else if (chainId == toChainId) {
                    NulsHash convertHash = ctxHash;
                    if (!config.isMainNet() && ctx.getType() == config.getCrossCtxType()) {
                        convertHash = TxUtil.friendConvertToMain(chain, ctx, TxType.CROSS_CHAIN).getHash();
                    }
                    if (!convertHashService.delete(convertHash, chainId)) {
                        return false;
                    }
                } else {
                    CtxStatusPO ctxStatusPO = ctxStatusService.get(ctxHash, chainId);
                    if(ctxStatusPO != null){
                        if(ctxStatusPO.getStatus() == TxStatusEnum.CONFIRMED.getStatus()){
                            chain.getLogger().info("该跨链转账交易已处理完成，不需回滚：{}",ctxHash.toHex() );
                            continue;
                        }
                    }
                    if (!ctxStatusService.delete(ctxHash, chainId) || !convertHashService.delete(ctxHash, chainId)) {
                        return false;
                    }
                }
            }
            //如果为主网通知跨链管理模块发起链与接收链资产变更
            if (config.isMainNet()) {
                List<String> txStrList = new ArrayList<>();
                for (Transaction tx : txs) {
                    txStrList.add(RPCUtil.encode(tx.serialize()));
                }
                String headerStr = RPCUtil.encode(blockHeader.serialize());
                ChainManagerCall.ctxAssetCirculateRollback(chainId, txStrList, headerStr);
            }
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> crossTxBatchValid(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if (chain == null) {
            result.put("txList", txs);
            result.put("errorCode", NulsCrossChainErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        if (!verifiedCtxMap.keySet().contains(chainId)) {
            verifiedCtxMap.put(chainId, new HashSet<>());
        }
        Set<NulsHash> verifiedCtxSet = verifiedCtxMap.get(chainId);
        List<Transaction> invalidCtxList = new ArrayList<>();
        String errorCode = null;
        for (Transaction ctx : txs) {
            NulsHash ctxHash = ctx.getHash();
            try {
                if (!verifiedCtxSet.contains(ctxHash)) {
                    if (!txValidator.validateTx(chain, ctx, blockHeader)) {
                        invalidCtxList.add(ctx);
                    } else {
                        verifiedCtxSet.add(ctxHash);
                    }
                }
            } catch (NulsException e) {
                invalidCtxList.add(ctx);
                chain.getLogger().error("Cross-Chain Transaction Verification Failure");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            } catch (IOException io) {
                invalidCtxList.add(ctx);
                chain.getLogger().error("Cross-Chain Transaction Verification Failure");
                chain.getLogger().error(io);
                errorCode = NulsCrossChainErrorCode.SERIALIZE_ERROR.getCode();
            }
        }
        result.put("txList", invalidCtxList);
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getCrossTxState(Map<String, Object> params) {
        if (params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.TX_HASH) == null) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(ParamConstant.CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(NulsCrossChainErrorCode.CHAIN_NOT_EXIST);
        }
        String hashStr = (String) params.get(ParamConstant.TX_HASH);
        Map<String, Object> result = new HashMap<>(2);
        NulsHash requestHash = NulsHash.fromHex(hashStr);
        byte statisticsResult = TxUtil.getCtxState(chain, requestHash);
        result.put(ParamConstant.VALUE, statisticsResult);
        return Result.getSuccess(CommonCodeConstanst.SUCCESS).setData(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getRegisteredChainInfoList(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>(2);
        LoggerUtil.commonLog.info("------获取跨链资产信息---总长度：" + chainManager.getRegisteredCrossChainList().size());
        result.put(ParamConstant.LIST, chainManager.getRegisteredCrossChainList());
        return Result.getSuccess(CommonCodeConstanst.SUCCESS).setData(result);
    }

    @Override
    public int getCrossChainTxType() {
        return config.getCrossCtxType();
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getByzantineCount(Map<String, Object> params) {
        if (params.get(ParamConstant.CHAIN_ID) == null) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(ParamConstant.CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(NulsCrossChainErrorCode.CHAIN_NOT_EXIST);
        }
        Map<String, Object> result = new HashMap<>(2);
        result.put(ParamConstant.VALUE, config.getByzantineRatio() * CommonUtil.getCurrentPackAddressList(chain).size() / NulsCrossChainConstant.MAGIC_NUM_100);
        return Result.getSuccess(CommonCodeConstanst.SUCCESS).setData(result);
    }

    private void rollbackCtx(List<NulsHash> convertHashList, List<NulsHash> ctxStatusList, List<NulsHash> otherCtxList, int chainId) {
        for (NulsHash convertHash : convertHashList) {
            convertHashService.delete(convertHash, chainId);
        }
        for (NulsHash otherHash : otherCtxList) {
            otherCtxService.delete(otherHash, chainId);
        }
        for (NulsHash ctxStatusHash : ctxStatusList) {
            CtxStatusPO ctxStatusPO = ctxStatusService.get(ctxStatusHash, chainId);
            ctxStatusPO.setStatus(TxStatusEnum.UNCONFIRM.getStatus());
            ctxStatusService.save(ctxStatusHash, ctxStatusPO, chainId);
        }
    }
}
