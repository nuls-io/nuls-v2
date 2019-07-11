package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.*;
import io.nuls.crosschain.base.model.bo.Circulation;
import io.nuls.crosschain.base.service.ProtocolService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.CtxStateEnum;
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.model.bo.message.WaitBroadSignMessage;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.rpc.call.LedgerCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.srorage.*;
import io.nuls.crosschain.nuls.utils.MessageUtil;
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
     * 其他链节点向本节点验证跨链交易正确性
     * */
    public void verifyCtx(int chainId, String nodeId, VerifyCtxMessage messageBody) {
        //验证跨链交易
        VerifyCtxResultMessage responseMessage = new VerifyCtxResultMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        int handleChainId = chainId;
        NulsHash ctxHash = messageBody.getRequestHash();
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
            ctxHash = messageBody.getOriginalCtxHash();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String originalHex = messageBody.getOriginalCtxHash().toHex();
        String nativeHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("收到节点{}发送过来的验证跨链交易信息,Hash：{}", nodeId, nativeHex);
        //如果是友链向主网验证，则只需查看本地是否存在该跨链交易
        Transaction mainCtx = ctxStatusService.get(ctxHash, handleChainId).getTx();
        if (mainCtx == null) {
            responseMessage.setVerifyResult(false);
            chain.getLogger().info("本节点不存在该跨链交易，Hash：{}", nativeHex);
        } else {
            //如果为主网向友链发起验证，则需验证主网协议跨链交易中存的原始跨链交易Hash与友链中存储的是否匹配
            if (!config.isMainNet()) {
                NulsHash originalHash = new NulsHash(mainCtx.getTxData());
                if (originalHash.equals(messageBody.getOriginalCtxHash())) {
                    responseMessage.setVerifyResult(true);
                } else {
                    responseMessage.setVerifyResult(false);
                    chain.getLogger().info("本地存在该交易，但该交易对应的本链协议跨链交易Hash不匹配，链内Hash：{}" + ";接收的本链协议Hash：{}", originalHash.toHex(), originalHex);
                }
            } else {
                responseMessage.setVerifyResult(true);
            }
        }
        //将验证结果返回给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.CTX_VERIFY_RESULT_MESSAGE);
        chain.getLogger().info("将跨链交易验证结果返回给节点{},Hash：{},验证结果：{}\n\n", nodeId, nativeHex, responseMessage.isVerifyResult());
    }

    @Override
    /**
     * 接收向其他链验证跨链交易的验证结果
     * */
    public void receiveVerifyRs(int chainId, String nodeId, VerifyCtxResultMessage messageBody) {
        //将验证结果放入缓存，等待其他线程处理
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getLogger().info("收到节点{}发送过来的交易验证结果,交易Hash:{},验证结果:{}\n\n", nodeId, messageBody.getRequestHash().toHex(), messageBody.isVerifyResult());
        NulsHash requestHash = messageBody.getRequestHash();
        if (!chain.getVerifyCtxResultMap().keySet().contains(requestHash)) {
            chain.getVerifyCtxResultMap().put(requestHash, new ArrayList<>());
        }
        chain.getVerifyCtxResultMap().get(requestHash).add(messageBody.isVerifyResult());
    }

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
                    chain.getLogger().info("跨链交易已确认完成，Hash:{},处理结果：{}\n\n", hashHex, responseMessage.getHandleResult());
                }else{
                    if(ctxStateService.get(messageBody.getRequestHash().getBytes(), handleChainId)){
                        responseMessage.setHandleResult(CtxStateEnum.CONFIRMED.getStatus());
                        chain.getLogger().info("跨链交易已确认完成，Hash:{},处理结果：{}\n\n", hashHex, responseMessage.getHandleResult());
                    }else{
                        responseMessage.setHandleResult(CtxStateEnum.MAINCONFIRMED.getStatus());
                        chain.getLogger().info("跨链交易主网已确认完成,接收链还未确认，Hash:{},处理结果：{}\n\n", hashHex, responseMessage.getHandleResult());
                        UntreatedMessage untreatedMessage = new UntreatedMessage(chainId,nodeId,messageBody,messageBody.getRequestHash());
                        chain.getGetCtxStateQueue().offer(untreatedMessage);
                    }
                }
            }
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
        chain.getLogger().info("收到节点{}发送过来的交易处理结果消息，交易hash:{},处理结果:{}\n\n", nodeId, messageBody.getRequestHash().toHex(), messageBody.getHandleResult());
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

        chain.getLogger().info("接收到链内节点{}广播过来的跨链交易Hash和签名,Hash:{},签名:{}", nodeId, nativeHex, signHex);
        //如果为第一次收到该交易，则向广播过来的节点获取完整跨链交易
        CtxStatusPO ctxStatusPO = ctxStatusService.get(localHash, handleChainId);
        if (ctxStatusPO == null) {
            //将收到的消息放入缓存中，等到收到交易后再广播该签名给其他节点
            if (messageBody.getSignature() != null) {
                if (!chain.getWaitBroadSignMap().keySet().contains(localHash)) {
                    chain.getWaitBroadSignMap().put(localHash, new HashSet<>());
                }
                chain.getWaitBroadSignMap().get(localHash).add(new WaitBroadSignMessage(nodeId, messageBody));
            }
            GetCtxMessage responseMessage = new GetCtxMessage();
            responseMessage.setRequestHash(localHash);
            if (chain.getCtxStageMap().get(localHash) == null && chain.getCtxStageMap().putIfAbsent(localHash, NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) == null) {
                NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_CTX_MESSAGE);
                chain.getLogger().info("第一次收到跨链交易Hash广播信息,向链内节点{}获取完整跨链交易,Hash:{}\n\n", nodeId, nativeHex);
            } else {
                UntreatedMessage untreatedSignMessage = new UntreatedMessage(chainId,nodeId,messageBody,localHash);
                chain.getSignMessageQueue().offer(untreatedSignMessage);
            }
            chain.getLogger().info("链内节点{}广播过来的跨链交易签名消息接收完成，Hash:{},签名:{}\n\n", nodeId, nativeHex, signHex);
            return;
        }
        //如果最新区块表中不存在该交易，则表示该交易已经被打包了，所以不需要再广播该交易的签名
        if (ctxStatusPO.getStatus() != TxStatusEnum.UNCONFIRM.getStatus() || messageBody.getSignature() == null) {
            chain.getLogger().info("跨链交易在本节点已经处理完成,Hash:{}\n\n", nativeHex);
            return;
        }
        try {
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            p2PHKSignature.parse(messageBody.getSignature(), 0);
            Transaction convertCtx = ctxStatusPO.getTx();
            if(!config.isMainNet() && convertCtx.getType() == config.getCrossCtxType()){
                convertCtx = convertCtxService.get(localHash, handleChainId);
            }
            //验证签名是否正确，如果是跨链转账交易，这签名为
            if(!ECKey.verify(convertCtx.getHash().getBytes(), p2PHKSignature.getSignData().getSignBytes(), p2PHKSignature.getPublicKey())){
                chain.getLogger().info("签名验证错误，hash:{},签名:{}\n\n",nativeHex,signHex);
                return;
            }
            MessageUtil.signByzantine(chain, chainId, localHash, ctxStatusPO.getTx(), messageBody, nativeHex, signHex, nodeId);
        } catch (Exception e) {
            chain.getLogger().error("链内节点广播过来的跨链交易签名消息处理失败,Hash:{},签名:{}\n\n", nativeHex, signHex);
            chain.getLogger().error(e);
        }
    }


    @Override
    /**
     * 链内其他节点向当前节点要完整跨链交易
     * */
    public void getCtx(int chainId, String nodeId, GetCtxMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsHash localHash = messageBody.getRequestHash();
        String nativeHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("链内节点{},向本节点获取完整的跨链交易，Hash:{}", nodeId, nativeHex);
        //查到对应的跨链交易
        CtxStatusPO ctxStatusPO = ctxStatusService.get(localHash, handleChainId);
        if(ctxStatusPO == null){
            chain.getLogger().info("当前节点不存在该跨链交易,Hash:{}",nativeHex);
            return;
        }
        NewCtxMessage responseMessage = new NewCtxMessage();
        responseMessage.setRequestHash(localHash);
        responseMessage.setCtx(ctxStatusPO.getTx());
        //把完整跨链交易发送给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.NEW_CTX_MESSAGE);
        chain.getLogger().info("将完整的跨链交易发送给链内节点{},Hash:{}\n\n", nodeId, nativeHex);
    }

    @Override
    /**
     * 接收链内节点发送过来的跨链交易
     * */
    public void receiveCtx(int chainId, String nodeId, NewCtxMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsHash localHash = messageBody.getCtx().getHash();
        String localHashHex = localHash.toHex();
        try {
            chain.getLogger().info("收到链内节点:{}发送过来的完整跨链交易信息,Hash:{}", nodeId, localHashHex);
            //判断本节点是否已经收到过该跨链交易，如果已收到过直接忽略
            if (ctxStatusService.get(localHash, handleChainId) != null) {
                chain.getLogger().info("本节点已收到并处理过该跨链交易,Hash:{}\n\n", localHashHex);
                return;
            }
            if (NulsCrossChainConstant.CTX_STATE_PROCESSING.equals(chain.getCtxStageMap().put(localHash, NulsCrossChainConstant.CTX_STATE_PROCESSING))) {
                chain.getLogger().info("该跨链交易正在处理中,Hash:{}\n\n",  localHashHex);
                return;
            }
            UntreatedMessage untreatedCtxMessage = new UntreatedMessage(chainId,nodeId,messageBody,localHash);
            chain.getCtxMessageQueue().offer(untreatedCtxMessage);
            chain.getLogger().info("链内节点:{}发送过来的完整跨链接收完成,Hash:{}\n\n", nodeId, localHashHex);
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
        chain.getLogger().info("收到其他链节点{}广播过来的跨链交易,Hash：{}", nodeId, mainHex);
        //判断是否接收过该交易,如果收到过则直接返回，如果没有收到过则向广播过来的节点发送获取完整跨链交易消息
        if (convertHashService.get(mainHash, handleChainId) != null) {
            chain.getLogger().info("本节点已经收到过该跨链交易，Hash：{}\n\n", mainHex);
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
        chain.getLogger().info("其他链广播的跨链交易Hash消息接收完成，Hash：{}\n\n", mainHex);
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
        Transaction localCtx = ctxStatusPO.getTx();
        /*
        如果为主网向友链获取完整跨链交易，则需要将本链协议跨链交易转换为对应主网协议跨链交易返回给主网节点
        */
        if(!config.isMainNet() && localCtx.getType() == config.getCrossCtxType()){
            try {
                localCtx = TxUtil.friendConvertToMain(chain, localCtx, null, TxType.CROSS_CHAIN);
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
        chain.getLogger().info("收到其他链节点{}发送过来的完整跨链交易,Hash:{}", nodeId, ctxHashHex);
        //判断本节点是否已经收到过该跨链交易，如果已收到过直接忽略
        if (convertHashService.get(ctxHash, handleChainId) != null) {
            chain.getLogger().info("本节点已收到并处理过该跨链交易,Hash:{}\n\n", ctxHashHex);
            return;
        }
        /*
         * 修改跨链交易状态为已接收，处理中
         * */
        if (NulsCrossChainConstant.CTX_STATE_PROCESSING.equals(chain.getOtherCtxStageMap().put(ctxHash, NulsCrossChainConstant.CTX_STATE_PROCESSING))) {
            chain.getLogger().info("该跨链交易正在处理中,Hash:{}\n\n", ctxHashHex);
            return;
        }
        UntreatedMessage untreatedCtxMessage = new UntreatedMessage(chainId,nodeId,messageBody,ctxHash);
        chain.getOtherCtxMessageQueue().offer(untreatedCtxMessage);
        chain.getLogger().info("其他链节点{}发送过来的完整跨链交易消息接收完成,Hash:{}", nodeId,ctxHashHex);
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

    @Override
    public void receiveRegisteredChainInfo(int chainId, String nodeId, RegisteredChainMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getLogger().info("收到主网节点{}发送的已注册链信息,注册跨链的链数量：{}",nodeId,messageBody.getChainInfoList().size());
        chainManager.getRegisteredChainMessageList().add(messageBody);
    }
}
