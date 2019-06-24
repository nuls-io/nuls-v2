package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.message.GetCtxStateMessage;
import io.nuls.crosschain.base.service.CrossChainService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.dto.input.CoinDTO;
import io.nuls.crosschain.nuls.model.dto.input.CrossTxTransferDTO;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPo;
import io.nuls.crosschain.nuls.rpc.call.AccountCall;
import io.nuls.crosschain.nuls.rpc.call.ChainManagerCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.srorage.*;
import io.nuls.crosschain.nuls.utils.MessageUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import io.nuls.crosschain.nuls.utils.manager.CoinDataManager;
import io.nuls.crosschain.nuls.utils.validator.CrossTxValidator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.crosschain.nuls.constant.NulsCrossChainConstant.*;
import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.*;
import static io.nuls.crosschain.nuls.constant.ParamConstant.*;

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
    private NulsCrossChainConfig config;

    @Autowired
    private CoinDataManager coinDataManager;

    @Autowired
    private CrossTxValidator txValidator;

    @Autowired
    private NewCtxService newCtxService;

    @Autowired
    private CommitedCtxService commitedCtxService;

    @Autowired
    private SendHeightService sendHeightService;

    @Autowired
    private CompletedCtxService completedCtxService;

    @Autowired
    private ConvertHashService convertHashService;

    @Autowired
    private CtxStateService ctxStateService;

    @Autowired
    private ConvertCtxService convertCtxService;

    @Override
    @SuppressWarnings("unchecked")
    public Result createCrossTx(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        CrossTxTransferDTO crossTxTransferDTO = JSONUtils.map2pojo(params, CrossTxTransferDTO.class);
        int chainId = crossTxTransferDTO.getChainId();
        if (chainId <= CHAIN_ID_MIN) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        if(!chainManager.isCrossNetUseAble()){
            chain.getLogger().info("跨链网络组网异常！");
            return Result.getFailed(CROSS_CHAIN_NETWORK_UNAVAILABLE);
        }
        Transaction tx = new Transaction(config.getCrossCtxType());
        try {
            tx.setRemark(StringUtils.bytes(crossTxTransferDTO.getRemark()));
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            List<CoinFrom> coinFromList = coinDataManager.assemblyCoinFrom(chain, crossTxTransferDTO.getListFrom(), false);
            List<CoinTo> coinToList = coinDataManager.assemblyCoinTo(crossTxTransferDTO.getListTo(), chain);
            coinDataManager.verifyCoin(coinFromList, coinToList, chain);
            int txSize = tx.size();
            //如果为主链跨链交易中只存在原始跨链交易签名，如果不为主链，跨链交易签名列表中会包含主网协议跨链交易的签名列表
            if (config.isMainNet()) {
                txSize += coinDataManager.getSignatureSize(coinFromList);
            } else {
                txSize += coinDataManager.getSignatureSize(coinFromList) * 2;
            }
            CoinData coinData = coinDataManager.getCoinData(chain, coinFromList, coinToList, txSize, true);
            //如果不是主网需计算主网协议跨链交易手续费
            if (!config.isMainNet()) {
                coinData = coinDataManager.getCoinData(chain, coinData.getFrom(), coinData.getTo(), txSize, false);
            }
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
            //签名
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            List<String> signedAddressList = new ArrayList<>();
            Map<String, String> signedAddressMap = new HashMap<>(INIT_CAPACITY_8);
            for (CoinDTO coinDTO : crossTxTransferDTO.getListFrom()) {
                if (!signedAddressList.contains(coinDTO.getAddress())) {
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(coinDTO.getAddress(), coinDTO.getPassword(), tx.getHash().getBytes());
                    p2PHKSignatures.add(p2PHKSignature);
                    signedAddressList.add(coinDTO.getAddress());
                    signedAddressMap.put(coinDTO.getAddress(), coinDTO.getPassword());
                }
            }
            if (!txValidator.coinDataValid(chain, coinData, tx.size())) {
                chain.getLogger().error("跨链交易CoinData验证失败！\n\n");
                return Result.getFailed(COINDATA_VERIFY_FAIL);
            }
            NulsHash txHash = tx.getHash();
            BroadCtxSignMessage message = new BroadCtxSignMessage();
            //判断本链是友链还是主网，如果是友链则需要生成对应的主网协议跨链交易，如果为主网则直接将跨链交易发送给交易模块处理
            if (!config.isMainNet()) {
                Transaction mainCtx = TxUtil.friendConvertToMain(chain, tx, signedAddressMap, TxType.CROSS_CHAIN);
                NulsHash convertHash = mainCtx.getHash();
                TransactionSignature mTransactionSignature = new TransactionSignature();
                mTransactionSignature.parse(mainCtx.getTransactionSignature(), 0);
                p2PHKSignatures.addAll(mTransactionSignature.getP2PHKSignatures());
                if (!txValidator.coinDataValid(chain, mainCtx.getCoinDataInstance(), mainCtx.size(), false)) {
                    chain.getLogger().error("生成的主网协议跨链交易CoinData验证失败！\n\n");
                    return Result.getFailed(COINDATA_VERIFY_FAIL);
                }
                message.setSignature(mTransactionSignature.getP2PHKSignatures().get(0).serialize());
                convertCtxService.save(txHash, mainCtx, chainId);
                convertHashService.save(convertHash, txHash, chainId);
            }
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            //如果本链为主网，则创建的交易就是主网协议交易
            if (config.isMainNet()) {
                message.setSignature(p2PHKSignatures.get(0).serialize());
                convertCtxService.save(txHash, tx, chainId);
            }
            message.setLocalHash(txHash);
            newCtxService.save(txHash, tx, chainId);
            NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
            Map<String, Object> result = new HashMap<>(2);
            result.put(TX_HASH, tx.getHash().toHex());
            return Result.getSuccess(SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(SERIALIZE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result validCrossTx(Map<String, Object> params) {
        if (params.get(CHAIN_ID) == null || params.get(TX) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        String txStr = (String) params.get(TX);
        try {
            Transaction transaction = new Transaction();
            transaction.parse(RPCUtil.decode(txStr), 0);
            if (!txValidator.validateTx(chain, transaction, null)) {
                chain.getLogger().error("跨链交易验证失败,Hash:{}\n", transaction.getHash().toHex());
                return Result.getFailed(TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> validResult = new HashMap<>(2);
            validResult.put(VALUE, true);
            chain.getLogger().info("跨链交易验证成功，Hash:{}\n", transaction.getHash().toHex());
            return Result.getSuccess(SUCCESS).setData(validResult);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            Log.error(e);
            return Result.getFailed(SERIALIZE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean commitCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        try {
            Map<NulsHash, Transaction> waitSendMap = new HashMap<>(INIT_CAPACITY_16);
            Map<NulsHash, Transaction> finishedMap = new HashMap<>(INIT_CAPACITY_16);
            List<NulsHash> hashList = new ArrayList<>();
            for (Transaction ctx:txs) {
                CoinData coinData = ctx.getCoinDataInstance();
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                NulsHash realCtxHash = ctx.getHash();
                //如果为接收链直接保存到已处理完成表中，否则保存到待广播表中
                if (chainId == toChainId) {
                    if (!completedCtxService.save(realCtxHash, ctx, chainId) || !newCtxService.delete(realCtxHash, chainId)) {
                        rollbackCtx(waitSendMap, finishedMap, chainId);
                        return false;
                    }
                    finishedMap.put(realCtxHash, ctx);
                } else {
                    hashList.add(realCtxHash);
                    //如果保存失败，则需要回滚已保存交易，直接返回
                    if (!commitedCtxService.save(realCtxHash, ctx, chainId) || !newCtxService.delete(realCtxHash, chainId)) {
                        rollbackCtx(waitSendMap, finishedMap, chainId);
                        return false;
                    }
                    waitSendMap.put(realCtxHash, ctx);
                }
                chain.getLogger().info("跨链交易提交成功，Hash:{}", ctx.getHash().toHex());
            }
            if (!hashList.isEmpty()) {
                //跨链交易被打包的高度
                long sendHeight = blockHeader.getHeight() + chain.getConfig().getSendHeight();
                SendCtxHashPo sendCtxHashPo = new SendCtxHashPo(hashList);
                if (!sendHeightService.save(sendHeight, sendCtxHashPo, chainId)) {
                    rollbackCtx(waitSendMap, finishedMap, chainId);
                    return false;
                }
            }
            //如果本链为主网通知跨链管理模块发起链与接收链资产变更
            if (config.isMainNet()) {
                List<String> txStrList = new ArrayList<>();
                for (Transaction tx:txs) {
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
            Map<NulsHash, Transaction> waitSendMap = new HashMap<>(INIT_CAPACITY_16);
            Map<NulsHash, Transaction> finishedMap = new HashMap<>(INIT_CAPACITY_16);
            for (Transaction ctx:txs) {
                CoinData coinData = ctx.getCoinDataInstance();
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                NulsHash realCtxHash = ctx.getHash();
                if (chainId == toChainId) {
                    if (!completedCtxService.delete(realCtxHash, chainId) || !newCtxService.save(realCtxHash, ctx, chainId)) {
                        commitCtx(waitSendMap, finishedMap, chainId);
                        chain.getLogger().error("跨链交易状态修改失败！\n\n");
                        return false;
                    }
                    finishedMap.put(realCtxHash, ctx);
                } else {
                    if (!commitedCtxService.delete(realCtxHash, chainId) || !newCtxService.save(realCtxHash, ctx, chainId)) {
                        commitCtx(waitSendMap, finishedMap, chainId);
                        chain.getLogger().error("跨链交易状态修改失败！\n\n");
                        return false;
                    }
                    waitSendMap.put(realCtxHash, ctx);
                }
            }
            //需要被清理的跨链交易高度
            long sendHeight = blockHeader.getHeight() + chain.getConfig().getSendHeight();
            if (!sendHeightService.delete(sendHeight, chainId)) {
                chain.getLogger().error("已广播跨链高度清除失败！\n\n");
                return false;
            }
            //如果为主网通知跨链管理模块发起链与接收链资产变更
            if (config.isMainNet()) {
                List<String> txStrList = new ArrayList<>();
                for (Transaction tx:txs) {
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
    public List<Transaction> crossTxBatchValid(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return null;
        }
        List<Transaction> invalidCtxList = new ArrayList<>();
        for (Transaction ctx:txs) {
            try {
                if(!txValidator.validateTx(chain, ctx, blockHeader)){
                    invalidCtxList.add(ctx);
                }
            }catch (Exception e){
                chain.getLogger().error(e);
                invalidCtxList.add(ctx);
            }
        }
        return invalidCtxList;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getCrossTxState(Map<String, Object> params) {
        if (params.get(CHAIN_ID) == null || params.get(TX_HASH) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (Integer) params.get(CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        String hashStr = (String) params.get(TX_HASH);
        Map<String, Object> result = new HashMap<>(2);
        NulsHash requestHash = NulsHash.fromHex(hashStr);
        //查看本交易是否已经存在查询处理成功记录，如果有直接返回，否则需向主网节点验证
        if (ctxStateService.get(requestHash.getBytes(), chainId)) {
            result.put(VALUE, true);
            return Result.getSuccess(SUCCESS).setData(result);
        }
        GetCtxStateMessage message = new GetCtxStateMessage();
        message.setRequestHash(requestHash);
        int linkedChainId = chainId;
        try {
            if (config.isMainNet()) {
                Transaction ctx = completedCtxService.get(requestHash, chainId);
                if (ctx == null) {
                    chain.getLogger().info("跨链交易不存在！\n\n");
                    result.put(VALUE, false);
                    return Result.getSuccess(SUCCESS).setData(result);
                }
                CoinData coinData = ctx.getCoinDataInstance();
                linkedChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
            }
            if(MessageUtil.canSendMessage(chain,linkedChainId)){
                result.put(VALUE, false);
                return Result.getSuccess(SUCCESS).setData(result);
            }
            NetWorkCall.broadcast(linkedChainId, message, CommandConstant.GET_CTX_STATE_MESSAGE, true);
            if (!chain.getCtxStateMap().containsKey(requestHash)) {
                chain.getCtxStateMap().put(requestHash, new ArrayList<>());
            }
            boolean statisticsResult = statisticsCtxState(chain, linkedChainId, requestHash);
            if (statisticsResult) {
                ctxStateService.save(requestHash.getBytes(), chainId);
            }
            result.put(VALUE, statisticsResult);
            return Result.getSuccess(SUCCESS).setData(result);
        } catch (NulsException e) {
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getRegisteredChainInfoList(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>(2);
        result.put(LIST, chainManager.getRegisteredCrossChainList());
        return Result.getSuccess(SUCCESS).setData(result);
    }

    @Override
    public int getCrossChainTxType() {
        return config.getCrossCtxType();
    }

    private void commitCtx(Map<NulsHash, Transaction> waitSendMap, Map<NulsHash, Transaction> finishedMap, int chainId) {
        for (Map.Entry<NulsHash, Transaction> entry : waitSendMap.entrySet()) {
            newCtxService.delete(entry.getKey(), chainId);
            commitedCtxService.save(entry.getKey(), entry.getValue(), chainId);
        }
        for (Map.Entry<NulsHash, Transaction> entry : finishedMap.entrySet()) {
            newCtxService.delete(entry.getKey(), chainId);
            completedCtxService.save(entry.getKey(), entry.getValue(), chainId);
        }
    }

    private void rollbackCtx(Map<NulsHash, Transaction> waitSendMap, Map<NulsHash, Transaction> finishedMap, int chainId) {
        for (Map.Entry<NulsHash, Transaction> entry : waitSendMap.entrySet()) {
            commitedCtxService.delete(entry.getKey(), chainId);
            newCtxService.save(entry.getKey(), entry.getValue(), chainId);
        }
        for (Map.Entry<NulsHash, Transaction> entry : finishedMap.entrySet()) {
            completedCtxService.delete(entry.getKey(), chainId);
            newCtxService.save(entry.getKey(), entry.getValue(), chainId);
        }
    }

    private boolean statisticsCtxState(Chain chain, int fromChainId, NulsHash requestHash) {
        try {
            int linkedNode = NetWorkCall.getAvailableNodeAmount(fromChainId, true);
            int needSuccessCount = linkedNode * chain.getConfig().getByzantineRatio() / NulsCrossChainConstant.MAGIC_NUM_100;
            int tryCount = 0;
            boolean statisticsResult = false;
            while (tryCount < NulsCrossChainConstant.BYZANTINE_TRY_COUNT) {
                if (chain.getCtxStateMap().get(requestHash).size() < needSuccessCount) {
                    Thread.sleep(2000);
                    tryCount++;
                    continue;
                }
                statisticsResult = chain.statisticsCtxState(requestHash, needSuccessCount);
                if (statisticsResult || chain.getCtxStateMap().get(requestHash).size() >= linkedNode) {
                    break;
                }
                Thread.sleep(2000);
                tryCount++;
            }
            return statisticsResult;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        } finally {
            chain.getCtxStateMap().remove(requestHash);
        }
    }
}
