package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.*;
import io.nuls.crosschain.base.model.bo.Circulation;
import io.nuls.crosschain.base.service.ProtocolService;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.NodeType;
import io.nuls.crosschain.nuls.rpc.call.AccountCall;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.rpc.call.TransactionCall;
import io.nuls.crosschain.nuls.srorage.*;
import io.nuls.crosschain.nuls.utils.CommonUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import java.io.IOException;
import java.util.*;

/**
 * 跨链模块协议处理实现类
 *
 * @author tag
 * @date 2019/4/9
 */
@Component
public class NulsProtocolServiceImpl implements ProtocolService {
    @Autowired
    private CompletedCtxService completedCtxService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private CommitedCtxService commitedCtxService;
    @Autowired
    private NulsCrossChainConfig config;
    @Autowired
    private ConvertToCtxService convertToCtxService;
    @Autowired
    private ConvertFromCtxService convertFromCtxService;
    @Autowired
    private NewCtxService newCtxService;


    @Override
    /**
     * 其他链节点向本节点验证跨链交易正确性
     * */
    public void verifyCtx(int chainId, String nodeId, VerifyCtxMessage messageBody) {
        //验证跨链交易
        VerifyCtxResultMessage responseMessage = new VerifyCtxResultMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        int handleChainId = chainId;
        NulsDigestData ctxHash = messageBody.getRequestHash();
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
            ctxHash = messageBody.getOriginalCtxHash();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String originalHex = messageBody.getOriginalCtxHash().getDigestHex();
        String nativeHex = messageBody.getRequestHash().getDigestHex();
        chain.getMessageLog().info("收到节点{}发送过来的验证跨链交易信息,Hash：{}",nodeId,nativeHex);
        //如果是友链向主网验证，则只需查看本地是否存在该跨链交易
        Transaction mainCtx = completedCtxService.get(ctxHash, handleChainId);
        if (mainCtx == null) {
            mainCtx = commitedCtxService.get(ctxHash, handleChainId);
        }
        if (mainCtx == null) {
            responseMessage.setVerifyResult(false);
            chain.getMessageLog().info("本节点不存在该跨链交易，Hash：{}",nativeHex);
        } else {
            //如果为主网向友链发起验证，则需验证主网协议跨链交易中存的原始跨链交易Hash与友链中存储的是否匹配
            if(!config.isMainNet()){
                NulsDigestData originalHash = new NulsDigestData();
                try {
                    originalHash.parse(mainCtx.getTxData(),0);
                }catch (NulsException e){
                    chain.getMessageLog().error(e);
                }
                if(originalHash.equals(messageBody.getOriginalCtxHash())){
                    responseMessage.setVerifyResult(true);
                }else {
                    responseMessage.setVerifyResult(false);
                    chain.getMessageLog().info("本地存在该交易，但该交易对应的本链协议跨链交易Hash不匹配，链内Hash：{}"+";接收的本链协议Hash：{}",originalHash.getDigestHex(),originalHex);
                }
            }else {
                responseMessage.setVerifyResult(true);
            }
        }
        //将验证结果返回给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.CTX_VERIFY_RESULT_MESSAGE);
        chain.getMessageLog().info("将跨链交易验证结果返回给节点{},Hash：{},验证结果：{}\n\n",nodeId,nativeHex,responseMessage.isVerifyResult());
    }

    @Override
    /**
     * 接收向其他链验证跨链交易的验证结果
     * */
    public void recvVerifyRs(int chainId, String nodeId, VerifyCtxResultMessage messageBody) {
        //将验证结果放入缓存，等待其他线程处理
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getMessageLog().info("收到节点{}发送过来的交易验证结果,交易Hash:{},验证结果:{}\n\n",nodeId,messageBody.getRequestHash().getDigestHex(),messageBody.isVerifyResult());
        NulsDigestData requestHash = messageBody.getRequestHash();
        if(!chain.getVerifyCtxResultMap().keySet().contains(requestHash)){
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
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String hashHex = messageBody.getRequestHash().getDigestHex();
        chain.getMessageLog().info("收到节点{}发送过来的查询跨链交易处理结果信息,交易Hash:{}",nodeId,hashHex);
        NulsDigestData realCtxHash = convertToCtxService.get(messageBody.getRequestHash(), handleChainId);
        Transaction ctx = completedCtxService.get(realCtxHash, handleChainId);
        if(ctx == null){
            ctx = commitedCtxService.get(realCtxHash, handleChainId);
        }
        if (ctx == null) {
            responseMessage.setHandleResult(false);
        }else{
            responseMessage.setHandleResult(true);
        }
        //将验证结果返回给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.CTX_STATE_MESSAGE);
        chain.getMessageLog().info("将跨链交易在本节点的处理结果返回给节点{}，Hash:{},处理结果：{}\n\n",nodeId,hashHex,responseMessage.isHandleResult());
    }

    @Override
    /**
     * 获取其他链发送过来的跨链交易处理状态
     * */
    public void recvCtxState(int chainId, String nodeId, CtxStateMessage messageBody) {
        //将返回结果放到指定缓存中，等待其他线程处理
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getMessageLog().info("收到节点{}发送过来的交易处理结果消息，交易hash:{},处理结果:{}\n\n",nodeId,messageBody.getRequestHash().getDigestHex(),messageBody.isHandleResult());
        NulsDigestData requestHash = messageBody.getRequestHash();
        if(!chain.getCtxStateMap().keySet().contains(requestHash)){
            chain.getCtxStateMap().put(requestHash, new ArrayList<>());
        }
        chain.getCtxStateMap().get(messageBody.getRequestHash()).add(messageBody.isHandleResult());
    }

    @Override
    /**
     * 接收链内节点广播过来的跨链交易Hash和签名
     * */
    @SuppressWarnings("unchecked")
    public void recvCtxSign(int chainId, String nodeId, BroadCtxSignMessage messageBody) {
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().getDigestHex();
        String signHex = "";
        if(messageBody.getSignature() != null){
            signHex = HexUtil.encode(messageBody.getSignature());
        }

        chain.getMessageLog().info("接收到链内节点{}广播过来的跨链交易Hash和签名，originalHash:{},Hash:{},签名:{}",nodeId,messageBody.getOriginalHash().getDigestHex(),nativeHex,signHex);
        //如果为第一次收到该交易，则向广播过来的节点获取完整跨链交易
        NulsDigestData ctxHash = convertToCtxService.get(messageBody.getOriginalHash(), handleChainId);
        if(ctxHash == null){
            //将收到的消息放入缓存中，等到收到交易后再广播该签名给其他节点
            if(messageBody.getSignature() != null){
                if(!chain.getWaitBroadSignMap().keySet().contains(messageBody.getRequestHash())){
                    chain.getWaitBroadSignMap().put(messageBody.getRequestHash(), new HashSet<>());
                }
                chain.getWaitBroadSignMap().get(messageBody.getRequestHash()).add(messageBody);
            }
            GetCtxMessage responseMessage = new GetCtxMessage();
            responseMessage.setRequestHash(messageBody.getRequestHash());

            NulsDigestData cacheHash;
            if(config.isMainNet()){
                cacheHash = messageBody.getRequestHash();
            }else{
                cacheHash = messageBody.getOriginalHash();
            }

            if(chain.getCtxStageMap().get(cacheHash) == null && chain.getCtxStageMap().putIfAbsent(cacheHash, NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) == null){
                chain.getMessageLog().info("第一次收到跨链交易Hash广播信息,Hash:{}",nativeHex);
                NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_CTX_MESSAGE);
            }else{
                int tryCount = 0;
                while (chain.getCtxStageMap().get(cacheHash) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE && tryCount <= NulsCrossChainConstant.BYZANTINE_TRY_COUNT ){
                    try{
                        Thread.sleep(2000);
                    }catch (Exception e){
                        chain.getMessageLog().error(e);
                    }
                    tryCount++;
                }
                if(chain.getCtxStageMap().get(cacheHash) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE){
                    NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_CTX_MESSAGE);
                }else{
                    chain.getHashNodeIdMap().putIfAbsent(cacheHash, new ArrayList<>());
                    chain.getHashNodeIdMap().get(cacheHash).add(new NodeType(nodeId,1));
                }
            }
            return;
        }
        Transaction ctx = newCtxService.get(ctxHash, handleChainId);
        //如果最新区块表中不存在该交易，则表示该交易已经被打包了，所以不需要再广播该交易的签名
        if(ctx == null || messageBody.getSignature() == null){
            chain.getMessageLog().info("跨链交易在本节点已经处理完成,Hash:{}",nativeHex);
            return;
        }
        try {
            //判断节点是否已经收到并广播过该签名，如果已经广播过则不需要再广播
            TransactionSignature signature = new TransactionSignature();
            if(ctx.getTransactionSignature() != null){
                signature.parse(ctx.getTransactionSignature(),0);
                for (P2PHKSignature sign:signature.getP2PHKSignatures()) {
                    if(Arrays.equals(messageBody.getSignature(), sign.serialize())){
                        chain.getMessageLog().info("本节点已经收到过该跨链交易的该签名,Hash:{},签名:{}",nativeHex,signHex);
                        return;
                    }
                }
            }else{
                List<P2PHKSignature> p2PHKSignatureList = new ArrayList<>();
                signature.setP2PHKSignatures(p2PHKSignatureList);
            }
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            p2PHKSignature.parse(messageBody.getSignature(),0);
            signature.getP2PHKSignatures().add(p2PHKSignature);
            //交易签名拜占庭
            List<String> packAddressList = CommonUtil.getCurrentPackAddresList(chain);
            int byzantineCount = CommonUtil.getByzantineCount(packAddressList, chain);
            int signCount = signature.getP2PHKSignatures().size();
            if(signCount >= byzantineCount){
                List<P2PHKSignature>misMatchSignList = CommonUtil.getMisMatchSigns(chain, signature, packAddressList);
                signCount -= misMatchSignList.size();
                if(signCount >= byzantineCount){
                    ctx.setTransactionSignature(signature.serialize());
                    newCtxService.save(ctxHash, ctx, handleChainId);
                    TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                    chain.getMessageLog().info("签名拜占庭验证通过，签名数量为：{}",signature.getP2PHKSignatures().size() );
                    return;
                }else{
                    signature.getP2PHKSignatures().addAll(misMatchSignList);
                    ctx.setTransactionSignature(signature.serialize());
                }
            }else{
                ctx.setTransactionSignature(signature.serialize());
            }
            newCtxService.save(ctxHash, ctx, handleChainId);
            NetWorkCall.broadcast(chainId, messageBody , CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
            chain.getMessageLog().info("将收到的跨链交易签名广播给链接到的其他节点,Hash:{},签名:{}\n\n",nativeHex,signHex);
        }catch (Exception e){
            chain.getMessageLog().error("链内节点广播过来的跨链交易签名消息处理失败,Hash:{},签名:{}\n\n",nativeHex,signHex);
            chain.getMessageLog().error(e);
        }
    }


    @Override
    /**
     * 链内其他节点向当前节点要完整跨链交易
     * */
    public void getCtx(int chainId, String nodeId, GetCtxMessage messageBody) {
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().getDigestHex();
        chain.getMessageLog().info("链内节点{},向本节点获取完整的跨链交易，Hash:{}",nodeId,nativeHex);
        //查到对应的跨链交易
        Transaction ctx = newCtxService.get(messageBody.getRequestHash(), handleChainId);
        if(ctx == null){
            ctx = commitedCtxService.get(messageBody.getRequestHash(), handleChainId);
        }
        if(ctx == null ){
            ctx = completedCtxService.get(messageBody.getRequestHash(), handleChainId);
        }
        NewCtxMessage responseMessage = new NewCtxMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        responseMessage.setCtx(ctx);
        //把完整跨链交易发送给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.NEW_CTX_MESSAGE);
        chain.getMessageLog().info("将完整的跨链交易发送给链内节点{},Hash:{}\n\n",nodeId,nativeHex);
    }

    @Override
    /**
     * 接收链内节点发送过来的跨链交易
     * */
    public void recvCtx(int chainId, String nodeId, NewCtxMessage messageBody) {
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsDigestData originalHash = new NulsDigestData();
        NulsDigestData nativeHash = messageBody.getRequestHash();
        String originalHex;
        String nativeHex = nativeHash.getDigestHex();
        try {
            originalHash.parse(messageBody.getCtx().getTxData(),0);
            originalHex = originalHash.getDigestHex();
            /*
             * 修改跨链交易状态为已接收，处理中
             * */
            NulsDigestData cacheHash;
            if(config.isMainNet()){
                cacheHash = nativeHash;
            }else{
                cacheHash = originalHash;
            }
            if(chain.getCtxStageMap().putIfAbsent(cacheHash,NulsCrossChainConstant.CTX_STATE_PROCESSING) != null){
                chain.getMessageLog().info("该跨链交易正在处理中,originalHash:{},Hash:{}",originalHex,nativeHex);
                return;
            }
            chain.getMessageLog().info("收到链内节点:{}发送过来的完整跨链交易信息,originalHash:{},Hash:{}",nodeId,originalHex,nativeHex);
            //判断本节点是否已经收到过该跨链交易，如果已收到过直接忽略
            if(convertToCtxService.get(originalHash, handleChainId) != null){
                chain.getMessageLog().info("本节点已收到并处理过该跨链交易，originalHash:{},Hash:{}",originalHex,nativeHex);
                return;
            }
            boolean handleResult = handleNewCtx(messageBody.getCtx(), originalHash, nativeHash, chain, chainId,nativeHex,originalHex,true);

            if(!handleResult && chain.getHashNodeIdMap().get(cacheHash)!= null && !chain.getHashNodeIdMap().get(cacheHash).isEmpty()){
                NodeType nodeType = chain.getHashNodeIdMap().get(cacheHash).remove(0);
                if(chain.getHashNodeIdMap().get(cacheHash).isEmpty()){
                    chain.getHashNodeIdMap().remove(cacheHash);
                }
                //如果为链内节点，则发送获取链内交易消息，否则获取链外交易消息
                if(nodeType.getNodeType() == NulsCrossChainConstant.NODE_TYPE_CURRENT_CHAIN){
                    GetCtxMessage responseMessage = new GetCtxMessage();
                    responseMessage.setRequestHash(nativeHash);
                    NetWorkCall.sendToNode(chainId, responseMessage, nodeType.getNodeId(), CommandConstant.GET_CTX_MESSAGE);
                    chain.getMessageLog().info("跨链交易处理失败，向链内节点：{}重新获取跨链交易，originalHash:{},Hash:{}",nodeType.getNodeId(),originalHex,nativeHex);
                }else{
                    GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
                    responseMessage.setRequestHash(originalHash);
                    NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
                    chain.getMessageLog().info("跨链交易处理失败，向发起链节点：{}重新获取跨链交易，originalHash:{},Hash:{}",nodeType.getNodeId(),originalHex,nativeHex);
                }
            }
            chain.getMessageLog().info("新交易处理完成,originalHash:{},Hash:{}\n\n",originalHex,nativeHex);
        }catch (Exception e){
            chain.getMessageLog().error(e);
        }finally {
            chain.clearCache(nativeHash,originalHash);
        }
    }


    @Override
    /**
     * 接收其他链广播过来的跨链交易Hash
     * */
    public void recvCtxHash(int chainId, String nodeId, BroadCtxHashMessage messageBody) {
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().getDigestHex();
        chain.getMessageLog().info("收到其他链节点{}广播过来的跨链交易,Hash：{}" ,nodeId, nativeHex);
        //判断是否接收过该交易,如果收到过则直接返回，如果没有收到过则向广播过来的节点发送获取完整跨链交易消息
        if(convertToCtxService.get(messageBody.getRequestHash(), handleChainId) != null){
            chain.getMessageLog().info("本节点已经收到过该跨链交易，Hash：{}" , nativeHex);
            return;
        }
        GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        if(chain.getCtxStageMap().get(messageBody.getRequestHash()) == null && chain.getCtxStageMap().putIfAbsent(messageBody.getRequestHash(), NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) == null){
            chain.getMessageLog().info("第一次收到跨链交易Hash广播信息,Hash:{}",nativeHex);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
            chain.getMessageLog().info("向发送链节点{}获取完整跨链交易，Hash:{}\n\n",nodeId,nativeHex);
        }else{
            int tryCount = 0;
            while (chain.getCtxStageMap().get(messageBody.getRequestHash()) == 1 && tryCount <= NulsCrossChainConstant.BYZANTINE_TRY_COUNT ){
                try{
                    Thread.sleep(2000);
                }catch (Exception e){
                    chain.getMessageLog().error(e);
                }
                tryCount++;
            }
            if(chain.getCtxStageMap().get(messageBody.getRequestHash()) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE){
                NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
                chain.getMessageLog().info("向发送链节点{}获取完整跨链交易，Hash:{}\n\n",nodeId,nativeHex);
            }else {
                chain.getHashNodeIdMap().putIfAbsent(messageBody.getRequestHash(), new ArrayList<>());
                chain.getHashNodeIdMap().get(messageBody.getRequestHash()).add(new NodeType(nodeId, 2));
            }
        }
    }

    @Override
    /**
     * 其他链节点向当前节点要完整跨链交易
     * */
    public void getOtherCtx(int chainId, String nodeId, GetOtherCtxMessage messageBody) {
        NewOtherCtxMessage responseMessage = new NewOtherCtxMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        NulsDigestData realCtxHash = messageBody.getRequestHash();
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().getDigestHex();
        chain.getMessageLog().info("接收到请求链节点{}发送的获取完整跨链交易信息,Hash:{}",nodeId,nativeHex);
        //查到对应的跨链交易
        Transaction mainCtx = completedCtxService.get(realCtxHash, handleChainId);
        responseMessage.setCtx(mainCtx);
        //把完整跨链交易发送给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.NEW_OTHER_CTX_MESSAGE);
        chain.getMessageLog().info("将完整跨链交易发送给请求连节点{},Hash:{}\n\n",nodeId,nativeHex);
    }

    @Override
    /**
     * 接收其他链节点发送过来的跨链交易
     * */
    public void recvOtherCtx(int chainId, String nodeId, NewOtherCtxMessage messageBody) {
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsDigestData originalHash = new NulsDigestData();
        NulsDigestData nativeHash = messageBody.getCtx().getHash();
        String originalHex;
        String nativeHex = nativeHash.getDigestHex();
        //如果是主网接收友链发送过来的跨链交易，则originalHash为跨链交易中txData数据，如果为友链接收主网发送的跨链交易originalHash与Hash一样都是主网协议跨链交易
        try {
            if(!chain.isMainChain()){
                originalHash = messageBody.getRequestHash();
                originalHex = nativeHex;
            }else{
                originalHash.parse(messageBody.getCtx().getTxData(),0);
                originalHex = originalHash.getDigestHex();
            }
        }catch (NulsException e){
            chain.getMessageLog().error(e);
            return;
        }
        chain.getMessageLog().info("收到发送链节点{}发送过来的完整跨链交易,originalHash:{},Hash:{}",nodeId,originalHex,nativeHex);
        //判断本节点是否已经收到过该跨链交易，如果已收到过直接忽略
        if(convertToCtxService.get(originalHash, handleChainId) != null){
            chain.getMessageLog().info("本节点已收到并处理过该跨链交易，originalHash:{},Hash:{}",originalHex,nativeHex);
            return;
        }
        try {
            NulsDigestData cacheHash;
            if(config.isMainNet()){
                cacheHash = nativeHash;
            }else{
                cacheHash = originalHash;
            }
            /*
             * 修改跨链交易状态为已接收，处理中
             * */
            if(chain.getCtxStageMap().putIfAbsent(cacheHash,NulsCrossChainConstant.CTX_STATE_PROCESSING) != null){
                chain.getMessageLog().info("该跨链交易正在处理中,originalHash:{},Hash:{}",originalHex,nativeHex);
                return;
            }

            boolean handleResult = handleNewCtx(messageBody.getCtx(), originalHash, nativeHash, chain, chainId,nativeHex,originalHex,false);

            if(!handleResult &&  chain.getHashNodeIdMap().get(cacheHash)!= null && !chain.getHashNodeIdMap().get(cacheHash).isEmpty()){
                NodeType nodeType = chain.getHashNodeIdMap().get(cacheHash).remove(0);
                if(chain.getHashNodeIdMap().get(cacheHash).isEmpty()){
                    chain.getHashNodeIdMap().remove(cacheHash);
                }
                //如果为链内节点，则发送获取链内交易消息，否则获取链外交易消息
                if(nodeType.getNodeType() == 1){
                    GetCtxMessage responseMessage = new GetCtxMessage();
                    responseMessage.setRequestHash(nativeHash);
                    NetWorkCall.sendToNode(chainId, responseMessage, nodeType.getNodeId(), CommandConstant.GET_CTX_MESSAGE);
                    chain.getMessageLog().info("跨链交易处理失败，向链内节点：{}重新获取跨链交易，originalHash:{},Hash:{}",nodeType.getNodeId(),originalHex,nativeHex);

                }else{
                    GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
                    responseMessage.setRequestHash(originalHash);
                    NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
                    chain.getMessageLog().info("跨链交易处理失败，向其他链节点：{}重新获取跨链交易，originalHash:{},Hash:{}",nodeType.getNodeId(),originalHex,nativeHex);

                }
            }
            chain.getMessageLog().info("新交易处理完成,originalHash:{},Hash:{}\n\n",originalHex,nativeHex);
        }catch (Exception e){
            chain.getMessageLog().error(e);
        }finally {
            chain.clearCache(nativeHash,originalHash);
        }
    }



    @Override
    public void getCirculat(int chainId, String nodeId, GetCirculationMessage messageBody) {
        int handleChainId = chainId;
        if(config.isMainNet()){
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        //调用账本模块接口获取查询资产的流通量
        CirculationMessage message = new CirculationMessage();
        //todo 调用账本模块查询资产明细
        List<Circulation> circulationList = new ArrayList<>();
        message.setCirculationList(circulationList);
        //将结果返回给请求节点
        NetWorkCall.sendToNode(chainId, message, nodeId, CommandConstant.CIRCULATION_MESSAGE);
    }

    @Override
    public void recvCirculat(int chainId, String nodeId, CirculationMessage messageBody) {
        //todo 将接收到的资产明细发送给账本模块
    }

    /**
     * 处理收到的新交易
     *
     * @param ctx              收到的交易
     * @param originalHash     本地协议对应的原交易hash
     * @param nativeHash       本地交易Hash
     * @param chain            该交易所属链
     * @param fromChainId      跨链链接标志
     * @return                 处理是否成功
     * */
    private boolean handleNewCtx(Transaction ctx, NulsDigestData originalHash, NulsDigestData nativeHash, Chain chain, int fromChainId,String nativeHex,String originalHex,boolean isLocalCtx){
        TransactionSignature transactionSignature = new TransactionSignature();
        try {
            //如果是其他链发送过来的跨链交易一定需要验证签名，如果为本链节点发送的跨链交易，如果有签名则需验证签名，如果没有不用验证
            transactionSignature.parse(ctx.getTransactionSignature(),0);
            if(!isLocalCtx){
                if(!SignatureUtil.validateTransactionSignture(ctx)){
                    chain.getMessageLog().error("Signature verification error");
                    return false;
                }
                ctx.setTransactionSignature(null);
                transactionSignature.setP2PHKSignatures(null);
            }else{
                if(transactionSignature.getP2PHKSignatures() != null && transactionSignature.getP2PHKSignatures().size() > 0){
                    if(!SignatureUtil.validateTransactionSignture(ctx)){
                        chain.getMessageLog().error("Signature verification error");
                        return false;
                    }
                }
            }
        }catch (NulsException e){
            chain.getMessageLog().error(e);
            return false;
        }
        VerifyCtxMessage verifyCtxMessage = new VerifyCtxMessage();
        verifyCtxMessage.setOriginalCtxHash(originalHash);
        verifyCtxMessage.setRequestHash(nativeHash);
        NetWorkCall.broadcast(fromChainId, verifyCtxMessage, CommandConstant.VERIFY_CTX_MESSAGE,true);
        chain.getMessageLog().info("本节点第一次收到该跨链交易，需向连接到的发送链节点验证该跨链交易,originalHash:{},Hash:{}",originalHex,nativeHex);
        if(!chain.getVerifyCtxResultMap().containsKey(nativeHash)){
            chain.getVerifyCtxResultMap().put(nativeHash, new ArrayList<>());
        }
        //接收验证结果，统计结果并做拜占庭得到最终验证结果，如果验证结果为验证不通过则删除该消息
        boolean validResult = verifyResult(chain, fromChainId, nativeHash);
        //如果验证不通过，结束
        if(!validResult){
            chain.getMessageLog().info("该跨链交易拜占庭验证失败，originalHash:{},Hash:{}\n",originalHex,nativeHex);
            return false;
        }
        //如果不是链内协议交易，本链为接收链且不为主链则需要生成本链协议跨链交易
        Transaction localCtx = ctx;
        try {
            if(!isLocalCtx){
                int toChainId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getTo().get(0).getAddress());
                if(!chain.isMainChain() && toChainId == chain.getChainId()){
                    localCtx = TxUtil.mainConvertToFriend(ctx, config.getCrossCtxType());
                    nativeHash = localCtx.getHash();
                    nativeHex = nativeHash.getDigestHex();
                    chain.getMessageLog().info("主网协议跨链交易转换为本链协议完成，本链协议交易Hash为：{}",nativeHex );
                }
            }
        }catch (Exception e){
            chain.getMessageLog().error(e);
            return false;
        }

        //处理交易签名
        try {
            if(!signCtx(chain, localCtx,originalHash, nativeHash,nativeHex,originalHex,transactionSignature)){
                return false;
            }
        }catch (Exception e){
            chain.getMessageLog().error("跨链交易签名失败,originalHash:{},Hash:{}",originalHex,nativeHex);
            chain.getMessageLog().error(e);
            return false;
        }

        //保存跨链交易
        if(!saveNewCtx(localCtx, chain, originalHash,nativeHex,originalHex)){
            chain.getMessageLog().info("跨链交易保存失败，originalHash:{},Hash:{}",originalHex,nativeHex);
            return false;
        }
        //广播缓存中的签名
        broadcastCtx(chain, nativeHash, chain.getChainId(),originalHex,nativeHex);
        return true;
    }


    /**
     * 统计交易验证结果
     * */
    private boolean verifyResult(Chain chain,int fromChainId,NulsDigestData requestHash){
        try {
            int linkedNode = NetWorkCall.getAvailableNodeAmount(fromChainId, true);
            int verifySuccessCount = linkedNode*chain.getConfig().getByzantineRatio()/NulsCrossChainConstant.MAGIC_NUM_100;
            chain.getMessageLog().info("当前链接到的跨链节点数为：{}，拜占庭比例为:{},最少需要验证通过数量:{}",linkedNode,chain.getConfig().getByzantineRatio(),verifySuccessCount);
            int tryCount = 0;
            boolean validResult = false;
            while (tryCount <= NulsCrossChainConstant.BYZANTINE_TRY_COUNT){
                if(chain.getVerifyCtxResultMap().get(requestHash).size() < verifySuccessCount){
                    Thread.sleep(2000);
                    tryCount++;
                    continue;
                }
                validResult = chain.verifyResult(requestHash, verifySuccessCount);
                if(validResult || chain.getVerifyCtxResultMap().get(requestHash).size() >= linkedNode){
                    break;
                }
                Thread.sleep(2000);
                tryCount++;
            }
            chain.getMessageLog().info("跨链交易拜占庭验证完成，验证结果为：{}",validResult );
            return validResult;
        }catch (Exception e){
            chain.getMessageLog().error(e);
            return false;
        }finally {
            chain.getVerifyCtxResultMap().remove(requestHash);
        }
    }

    /**
     * 处理接收到的新交易
     * @param ctx
     * @param chain
     * */
    private boolean saveNewCtx(Transaction ctx, Chain chain , NulsDigestData originalHash,String nativeHex,String originalHex){
        int handleChainId = chain.getChainId();
        /*
         * 主网中传输的都是主网协议的跨链交易所以不用做处理，如果是友链接收到主网发送来的跨链主网协议跨链交易需要生成对应的本链协议跨链交易
         * 如果友链收到本链节点广播的跨链交易，需要找到该交易对应的主网协议跨链交易的Hash（txData中保存）然后保存
         * */
        if(convertToCtxService.save(originalHash, ctx.getHash(), handleChainId)){
            if(!newCtxService.save(ctx.getHash(), ctx, handleChainId)){
                convertToCtxService.delete(originalHash, handleChainId);
                chain.getMessageLog().error("新跨链交易保存失败,originalHash:{},localHash:{}",originalHex,nativeHex);
                return false;
            }
        }else {
            return false;
        }
        chain.getMessageLog().info("新跨链交易保存成功,originalHash:{},localHash:{}",originalHex,nativeHex);
        return true;
    }

    /**
     * 验证完成的跨链交易签名并广播给链内其他节点
     * */
    @SuppressWarnings("unchecked")
    private boolean signCtx(Chain chain, Transaction ctx,NulsDigestData originalHash, NulsDigestData nativeHash,String nativeHex,String originalHex,TransactionSignature transactionSignature)throws NulsException,IOException{
        /*
         * 如果本地缓存有该跨链交易未广播的签名，需要把签名加入到交易的签名列表中
         * */
        if(transactionSignature.getP2PHKSignatures() == null){
            transactionSignature.setP2PHKSignatures(new ArrayList<>());
        }
        if(chain.getWaitBroadSignMap().keySet().contains(nativeHash)){
            for (BroadCtxSignMessage message:chain.getWaitBroadSignMap().get(nativeHash)) {
                for (P2PHKSignature sign:transactionSignature.getP2PHKSignatures()) {
                    if(Arrays.equals(message.getSignature(), sign.serialize())){
                        break;
                    }
                }
                P2PHKSignature p2PHKSignature = new P2PHKSignature();
                p2PHKSignature.parse(message.getSignature(),0);
                transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
            }
        }
        BroadCtxSignMessage message = new BroadCtxSignMessage();
        message.setRequestHash(nativeHash);
        message.setOriginalHash(originalHash);
        //判断本节点是否为共识节点，如果为共识节点则对该交易签名
        Map packerInfo = ConsensusCall.getPackerInfo(chain);
        List<String> packAddressList = (List<String>) packerInfo.get("packAddressList");
        int agentCount = packAddressList.size();
        int signCount = transactionSignature.getP2PHKSignatures().size();
        int minPassCount = agentCount*chain.getConfig().getByzantineRatio()/NulsCrossChainConstant.MAGIC_NUM_100;
        if(minPassCount == 0){
            minPassCount = 1;
        }
        boolean isDuplicate = false;
        //判断交易签名与当前轮次共识节点出块账户是否匹配
        List<P2PHKSignature>misMatchSignList = null;
        if(signCount >= minPassCount){
            misMatchSignList = CommonUtil.getMisMatchSigns(chain, transactionSignature, packAddressList);
            signCount -= misMatchSignList.size();
            isDuplicate = true;
            if(signCount >= minPassCount){
                ctx.setTransactionSignature(transactionSignature.serialize());
                TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                chain.getMessageLog().info("跨链交易签名数量达到拜占庭比例，将该跨链交易发送给交易模块处理,originalHash:{},localHash:{}",originalHex,nativeHex);
                return true;
            }
        }
        String password = (String)packerInfo.get("password");
        String address = (String)packerInfo.get("address");
        if(!StringUtils.isBlank(address)){
            chain.getMessageLog().info("本节点为共识节点，对跨链交易签名,originalHash:{},localHash:{}",originalHex,nativeHex);
            P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, ctx.getHash().getDigestBytes());
            message.setSignature(p2PHKSignature.serialize());
            signCount++;
            transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
            if(signCount >= minPassCount){
                if(!isDuplicate){
                    misMatchSignList = CommonUtil.getMisMatchSigns(chain, transactionSignature, packAddressList);
                    signCount -= misMatchSignList.size();
                }
                if(signCount >= minPassCount){
                    ctx.setTransactionSignature(transactionSignature.serialize());
                    TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                    chain.getMessageLog().info("跨链交易签名数量达到拜占庭比例，将该跨链交易发送给交易模块处理,originalHash:{},localHash:{}",originalHex,nativeHex);
                }
            }
            if(misMatchSignList != null && misMatchSignList.size() > 0 && signCount < minPassCount){
                transactionSignature.getP2PHKSignatures().addAll(misMatchSignList);
                ctx.setTransactionSignature(transactionSignature.serialize());
            }
        }
        //将收到的消息放入缓存中，等到收到交易后再广播该签名给其他节点
        if(!chain.getWaitBroadSignMap().keySet().contains(nativeHash)){
            chain.getWaitBroadSignMap().put(nativeHash, new HashSet<>());
        }
        chain.getWaitBroadSignMap().get(nativeHash).add(message);
        return true;
    }

    /**
     * 广播签名
     * */
    private void broadcastCtx(Chain chain,NulsDigestData hash,int chainId,String originalHex,String nativeHex){
        if(chain.getWaitBroadSignMap().get(hash) != null){
            Iterator<BroadCtxSignMessage> iterator = chain.getWaitBroadSignMap().get(hash).iterator();
            while (iterator.hasNext()){
                BroadCtxSignMessage message = iterator.next();
                if(NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE,false)){
                    iterator.remove();
                    String signStr = "";
                    if(message.getSignature() != null){
                        signStr = HexUtil.encode(message.getSignature());
                    }
                    chain.getMessageLog().info("将跨链交易签名广播给链内其他节点,originalHash:{},localHash:{},sign:{}",originalHex,nativeHex,signStr);
                }
            }
            if(chain.getWaitBroadSignMap().get(hash).isEmpty()){
                chain.getWaitBroadSignMap().remove(hash);
            }
        }
    }
}
