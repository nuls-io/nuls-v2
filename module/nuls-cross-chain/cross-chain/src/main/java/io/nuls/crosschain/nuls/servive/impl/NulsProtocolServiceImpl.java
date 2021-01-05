package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.*;
import io.nuls.crosschain.base.model.bo.Circulation;
import io.nuls.crosschain.base.service.ProtocolService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.CtxStateEnum;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.model.bo.message.WaitBroadSignMessage;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.LedgerCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.srorage.*;
import io.nuls.crosschain.nuls.utils.CommonUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * 跨链模块协议处理实现类
 *
 * @author tag
 * @date 2019/4/9
 */
@Component
public class NulsProtocolServiceImpl implements ProtocolService {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private NulsCrossChainConfig config;
    @Autowired
    private ConvertCtxService convertCtxService;
    @Autowired
    private ConvertHashService convertHashService;

    @Autowired
    private CtxStatusService ctxStatusService;

    @Autowired
    private CommitedOtherCtxService otherCtxService;

    @Autowired
    private CtxStateService ctxStateService;

    @Override
    /**
     * 查询交易处理状态
     * */
    public void getCtxState(int chainId, String nodeId, GetCtxStateMessage messageBody) {
        CtxStateMessage responseMessage = new CtxStateMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        responseMessage.setHandleResult(CtxStateEnum.UNCONFIRM.getStatus());
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String hashHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("收到节点{}发送过来的查询跨链交易处理结果信息,交易Hash:{}", nodeId, hashHex);
        NulsHash realCtxHash = messageBody.getRequestHash();
        Transaction ctx = otherCtxService.get(realCtxHash, handleChainId);
        try {
            if(ctx != null){
                int toChainId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getTo().get(0).getAddress());
                if(handleChainId == toChainId){
                    responseMessage.setHandleResult(CtxStateEnum.CONFIRMED.getStatus());
                }else{
                    if(ctxStateService.get(messageBody.getRequestHash().getBytes(), handleChainId)){
                        responseMessage.setHandleResult(CtxStateEnum.CONFIRMED.getStatus());
                    }else{
                        responseMessage.setHandleResult(CtxStateEnum.MAINCONFIRMED.getStatus());
                        UntreatedMessage untreatedMessage = new UntreatedMessage(chainId,nodeId,messageBody,messageBody.getRequestHash());
                        chain.getGetCtxStateQueue().offer(untreatedMessage);
                    }
                }
            }
            chain.getLogger().info("跨链交易处理结果为：{}(0:确认中，1:主网已确认，2:确认完成)，Hash:{}\n\n", responseMessage.getHandleResult(),hashHex);
        }catch(NulsException e){
            chain.getLogger().error(e);
        }
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.CTX_STATE_MESSAGE);
    }

    @Override
    /**
     * 获取其他链发送过来的跨链交易处理状态
     * */
    public void receiveCtxState(int chainId, String nodeId, CtxStateMessage messageBody) {
        //将返回结果放到指定缓存中，等待其他线程处理
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getLogger().debug("收到节点{}发送过来的交易处理结果消息，交易hash:{},处理结果:{}\n\n", nodeId, messageBody.getRequestHash().toHex(), messageBody.getHandleResult());
        NulsHash requestHash = messageBody.getRequestHash();
        if (!chain.getCtxStateMap().keySet().contains(requestHash)) {
            chain.getCtxStateMap().put(requestHash, new ArrayList<>());
        }
        chain.getCtxStateMap().get(messageBody.getRequestHash()).add(messageBody.getHandleResult());
    }

    @Override
    /**
     * 接收链内节点广播过来的跨链交易Hash和签名
     * */
    @SuppressWarnings("unchecked")
    public void receiveCtxSign(int chainId, String nodeId, BroadCtxSignMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsHash localHash = messageBody.getLocalHash();
        String nativeHex = localHash.toHex();
        String signHex = "";
        if (messageBody.getSignature() != null) {
            signHex = HexUtil.encode(messageBody.getSignature());
        }
        chain.getLogger().debug("接收到链内节点{}广播过来的跨链交易Hash和签名,Hash:{},签名:{}", nodeId, nativeHex, signHex);
        //如果为第一次收到该交易，则向广播过来的节点获取完整跨链交易
        CtxStatusPO ctxStatusPO = ctxStatusService.get(localHash, handleChainId);
        if (ctxStatusPO == null) {
            UntreatedMessage untreatedSignMessage = new UntreatedMessage(chainId,nodeId,messageBody,localHash);
            chain.getFutureMessageMap().putIfAbsent(localHash, new ArrayList<>());
            chain.getFutureMessageMap().get(localHash).add(untreatedSignMessage);
            chain.getLogger().info("当前节点还未确认该跨链交易，缓存签名消息");
            return;
        }
        //如果交易在本节点已确认则无需再签名处理
        if (ctxStatusPO.getStatus() != TxStatusEnum.UNCONFIRM.getStatus() || messageBody.getSignature() == null) {
            chain.getLogger().debug("跨链交易在本节点已经处理完成,Hash:{}\n\n", nativeHex);
            return;
        }
        try {
            UntreatedMessage untreatedSignMessage = new UntreatedMessage(chainId,nodeId,messageBody,localHash);
            chain.getSignMessageByzantineQueue().offer(untreatedSignMessage);
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
    }

    @Override
    /**
     * 接收其他链广播过来的跨链交易Hash
     * */
    public void receiveCtxHash(int chainId, String nodeId, BroadCtxHashMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        //跨链传输的都是主网协议交易HASH
        NulsHash mainHash = messageBody.getConvertHash();
        String mainHex = mainHash.toHex();
        chain.getLogger().debug("收到其他链节点{}广播过来的跨链交易,Hash：{}", nodeId, mainHex);
        //判断是否接收过该交易,如果收到过则直接返回，如果没有收到过则向广播过来的节点发送获取完整跨链交易消息
        if (convertHashService.get(mainHash, handleChainId) != null) {
            chain.getLogger().debug("本节点已经收到过该跨链交易，Hash：{}\n\n", mainHex);
            return;
        }
        if (chain.getOtherCtxStageMap().get(mainHash) == null && chain.getOtherCtxStageMap().putIfAbsent(mainHash, NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) == null) {
            chain.getLogger().info("第一次收到跨链交易Hash广播信息,Hash:{}", mainHex);
            GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
            responseMessage.setRequestHash(mainHash);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
            chain.getLogger().info("向发送链节点{}获取完整跨链交易，Hash:{}", nodeId, mainHex);
        } else {
            UntreatedMessage untreatedSignMessage = new UntreatedMessage(chainId,nodeId,messageBody,mainHash);
            chain.getHashMessageQueue().offer(untreatedSignMessage);
        }
        chain.getLogger().debug("其他链广播的跨链交易Hash消息接收完成，Hash：{}\n\n", mainHex);
    }

    @Override
    /**
     * 其他链节点向当前节点要完整跨链交易
     * */
    public void getOtherCtx(int chainId, String nodeId, GetOtherCtxMessage messageBody) {
        NewOtherCtxMessage responseMessage = new NewOtherCtxMessage();
        //主网中传输的都是主网协议交易HASH
        NulsHash mainHash = messageBody.getRequestHash();
        responseMessage.setRequestHash(mainHash);
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("接收到请求链节点{}发送的获取完整跨链交易信息,Hash:{}", nodeId, nativeHex);
        //查到对应的跨链交易
        NulsHash localHash = mainHash;
        if(!config.isMainNet()){
            localHash = convertHashService.get(mainHash,handleChainId);
        }
        CtxStatusPO ctxStatusPO = ctxStatusService.get(localHash, handleChainId);
        if(ctxStatusPO == null){
            chain.getLogger().error("交易不存在,hash:{}",nativeHex);
            return;
        }
        if (ctxStatusPO.getTx().getType() != TxType.VERIFIER_INIT) {
            List<String> packAddressList = chain.getVerifierList();
            int byzantineCount = CommonUtil.getByzantineCount(chain, packAddressList.size());
            TransactionSignature transactionSignature = new TransactionSignature();
            try {
                transactionSignature.parse(ctxStatusPO.getTx().getTransactionSignature(),0);
            } catch (NulsException e) {
                Log.error("解析交易签名失败");
                return;
            }
            if(transactionSignature.getP2PHKSignatures().size() < byzantineCount && ctxStatusPO.getStatus() == TxStatusEnum.UNCONFIRM.getStatus()){
                chain.getLogger().info("The cross chain transaction obtained has not been confirmed at this node,hash:{}",nativeHex);
                return;
            }
        }
        Transaction localCtx = ctxStatusPO.getTx();
        /*
        如果为主网向友链获取完整跨链交易，则需要将本链协议跨链交易转换为对应主网协议跨链交易返回给主网节点
        */
        if(!config.isMainNet() && localCtx.getType() == config.getCrossCtxType()){
            try {
                localCtx = TxUtil.friendConvertToMain(chain, localCtx, TxType.CROSS_CHAIN, true);
            }catch (Exception e){
                chain.getLogger().error("将本链协议跨链交易转为主网协议跨链交易错误,Hash:{}\n\n",nativeHex);
                chain.getLogger().error(e);
                return;
            }
        }
        responseMessage.setCtx(localCtx);
        //把完整跨链交易发送给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.NEW_OTHER_CTX_MESSAGE);
        chain.getLogger().info("将完整跨链交易发送给请求连节点{},Hash:{}\n\n", nodeId, nativeHex);
    }

    @Override
    /**
     * 接收其他链节点发送过来的跨链交易
     * */
    public void receiveOtherCtx(int chainId, String nodeId, NewOtherCtxMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsHash ctxHash = messageBody.getCtx().getHash();
        String ctxHashHex = ctxHash.toHex();
        chain.getLogger().info("收到其他链节点{}发送过来的完整跨链交易,Hash:{},交易类型：{}", nodeId, ctxHashHex, messageBody.getCtx().getType());
        //判断本节点是否已经收到过该跨链交易，如果已收到过直接忽略
        if (convertHashService.get(ctxHash, handleChainId) != null) {
            chain.getLogger().debug("本节点已收到并处理过该跨链交易,Hash:{}\n\n", ctxHashHex);
            return;
        }
        /*
         * 修改跨链交易状态为已接收，处理中
         * */
        if (NulsCrossChainConstant.CTX_STATE_PROCESSING.equals(chain.getOtherCtxStageMap().put(ctxHash, NulsCrossChainConstant.CTX_STATE_PROCESSING))) {
            chain.getLogger().debug("该跨链交易正在处理中,Hash:{}\n\n", ctxHashHex);
            return;
        }
        UntreatedMessage untreatedCtxMessage = new UntreatedMessage(chainId,nodeId,messageBody,ctxHash);
        chain.getOtherCtxMessageQueue().offer(untreatedCtxMessage);
        chain.getLogger().debug("其他链节点{}发送过来的完整跨链交易消息接收完成,Hash:{}", nodeId,ctxHashHex);
    }

    @Override
    public void getCirculation(int chainId, String nodeId, GetCirculationMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getLogger().info("主网节点{}本节点查询本链资产流通量,查询的资产ID为：{}\n\n", nodeId, messageBody.getAssetIds());
        //调用账本模块接口获取查询资产的流通量
        CirculationMessage message = new CirculationMessage();
        try {
            List<Circulation> circulationList = LedgerCall.getAssetsById(chain, messageBody.getAssetIds());
            message.setCirculationList(circulationList);
            //将结果返回给请求节点
            NetWorkCall.sendToNode(chainId, message, nodeId, CommandConstant.CIRCULATION_MESSAGE);
        } catch (NulsException e) {
            chain.getLogger().error(e);
        }
    }
}
