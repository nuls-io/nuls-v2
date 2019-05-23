package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
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
import io.nuls.crosschain.nuls.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.nuls.rpc.call.*;
import io.nuls.crosschain.nuls.srorage.*;
import io.nuls.crosschain.nuls.utils.CommonUtil;
import io.nuls.crosschain.nuls.utils.MessageUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.core.annotation.Autowired;
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
        Transaction mainCtx = completedCtxService.get(ctxHash, handleChainId);
        if (mainCtx == null) {
            mainCtx = commitedCtxService.get(ctxHash, handleChainId);
        }
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
    public void recvVerifyRs(int chainId, String nodeId, VerifyCtxResultMessage messageBody) {
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
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String hashHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("收到节点{}发送过来的查询跨链交易处理结果信息,交易Hash:{}", nodeId, hashHex);
        NulsHash realCtxHash = convertToCtxService.get(messageBody.getRequestHash(), handleChainId);
        Transaction ctx = completedCtxService.get(realCtxHash, handleChainId);
        if (ctx == null) {
            ctx = commitedCtxService.get(realCtxHash, handleChainId);
        }
        if (ctx == null) {
            responseMessage.setHandleResult(false);
        } else {
            responseMessage.setHandleResult(true);
        }
        //将验证结果返回给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.CTX_STATE_MESSAGE);
        chain.getLogger().info("将跨链交易在本节点的处理结果返回给节点{}，Hash:{},处理结果：{}\n\n", nodeId, hashHex, responseMessage.isHandleResult());
    }

    @Override
    /**
     * 获取其他链发送过来的跨链交易处理状态
     * */
    public void recvCtxState(int chainId, String nodeId, CtxStateMessage messageBody) {
        //将返回结果放到指定缓存中，等待其他线程处理
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getLogger().info("收到节点{}发送过来的交易处理结果消息，交易hash:{},处理结果:{}\n\n", nodeId, messageBody.getRequestHash().toHex(), messageBody.isHandleResult());
        NulsHash requestHash = messageBody.getRequestHash();
        if (!chain.getCtxStateMap().keySet().contains(requestHash)) {
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
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().toHex();
        String signHex = "";
        if (messageBody.getSignature() != null) {
            signHex = HexUtil.encode(messageBody.getSignature());
        }

        chain.getLogger().info("接收到链内节点{}广播过来的跨链交易Hash和签名，originalHash:{},Hash:{},签名:{}", nodeId, messageBody.getOriginalHash().toHex(), nativeHex, signHex);
        //如果为第一次收到该交易，则向广播过来的节点获取完整跨链交易
        NulsHash ctxHash = convertToCtxService.get(messageBody.getOriginalHash(), handleChainId);
        if (ctxHash == null) {
            //将收到的消息放入缓存中，等到收到交易后再广播该签名给其他节点
            if (messageBody.getSignature() != null) {
                if (!chain.getWaitBroadSignMap().keySet().contains(messageBody.getRequestHash())) {
                    chain.getWaitBroadSignMap().put(messageBody.getRequestHash(), new HashSet<>());
                }
                chain.getWaitBroadSignMap().get(messageBody.getRequestHash()).add(messageBody);
            }
            GetCtxMessage responseMessage = new GetCtxMessage();
            responseMessage.setRequestHash(messageBody.getRequestHash());

            NulsHash cacheHash;
            if (config.isMainNet()) {
                cacheHash = messageBody.getRequestHash();
            } else {
                cacheHash = messageBody.getOriginalHash();
            }
            if (chain.getCtxStageMap().get(cacheHash) == null && chain.getCtxStageMap().putIfAbsent(cacheHash, NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) == null) {
                NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_CTX_MESSAGE);
                chain.getLogger().info("第一次收到跨链交易Hash广播信息,向链内节点{}获取完整跨链交易,Hash:{}\n\n", nodeId, nativeHex);
            } else {
                UntreatedMessage  untreatedSignMessage = new UntreatedMessage(chainId,nodeId,messageBody,cacheHash);
                chain.getSignMessageQueue().offer(untreatedSignMessage);
            }
            chain.getLogger().info("链内节点{}广播过来的跨链交易签名消息接收完成，originalHash:{},Hash:{},签名:{}", nodeId, messageBody.getOriginalHash().toHex(), nativeHex, signHex);
            return;
        }
        Transaction ctx = newCtxService.get(ctxHash, handleChainId);
        //如果最新区块表中不存在该交易，则表示该交易已经被打包了，所以不需要再广播该交易的签名
        if (ctx == null || messageBody.getSignature() == null) {
            chain.getLogger().info("跨链交易在本节点已经处理完成,Hash:{}\n\n", nativeHex);
            return;
        }
        try {
            MessageUtil.signByzantine(chain, chainId, ctxHash, ctx, messageBody, nativeHex, signHex);
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
        String nativeHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("链内节点{},向本节点获取完整的跨链交易，Hash:{}", nodeId, nativeHex);
        //查到对应的跨链交易
        Transaction ctx = newCtxService.get(messageBody.getRequestHash(), handleChainId);
        if (ctx == null) {
            ctx = commitedCtxService.get(messageBody.getRequestHash(), handleChainId);
        }
        if (ctx == null) {
            ctx = completedCtxService.get(messageBody.getRequestHash(), handleChainId);
        }
        NewCtxMessage responseMessage = new NewCtxMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        responseMessage.setCtx(ctx);
        //把完整跨链交易发送给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.NEW_CTX_MESSAGE);
        chain.getLogger().info("将完整的跨链交易发送给链内节点{},Hash:{}\n\n", nodeId, nativeHex);
    }

    @Override
    /**
     * 接收链内节点发送过来的跨链交易
     * */
    public void recvCtx(int chainId, String nodeId, NewCtxMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsHash originalHash = new NulsHash(messageBody.getCtx().getTxData());
        NulsHash nativeHash = messageBody.getRequestHash();
        String originalHex = originalHash.toHex();
        String nativeHex = nativeHash.toHex();
        try {
            /*
             * 修改跨链交易状态为已接收，处理中
             * */
            NulsHash cacheHash;
            if (config.isMainNet()) {
                cacheHash = nativeHash;
            } else {
                cacheHash = originalHash;
            }

            chain.getLogger().info("收到链内节点:{}发送过来的完整跨链交易信息,originalHash:{},Hash:{}", nodeId, originalHex, nativeHex);
            //判断本节点是否已经收到过该跨链交易，如果已收到过直接忽略
            if (convertToCtxService.get(originalHash, handleChainId) != null) {
                chain.getLogger().info("本节点已收到并处理过该跨链交易，originalHash:{},Hash:{}\n\n", originalHex, nativeHex);
                return;
            }
            if (NulsCrossChainConstant.CTX_STATE_PROCESSING.equals(chain.getCtxStageMap().put(cacheHash, NulsCrossChainConstant.CTX_STATE_PROCESSING))) {
                chain.getLogger().info("该跨链交易正在处理中,originalHash:{},Hash:{}\n\n", originalHex, nativeHex);
                return;
            }
            UntreatedMessage untreatedCtxMessage = new UntreatedMessage(chainId,nodeId,messageBody,cacheHash);
            chain.getCtxMessageQueue().offer(untreatedCtxMessage);
            chain.getLogger().info("链内节点:{}发送过来的完整跨链接收完成,originalHash:{},Hash:{}", nodeId, originalHex, nativeHex);
        } catch (Exception e) {
            chain.getLogger().error(e);
        } finally {
            chain.clearCache(nativeHash, originalHash);
        }
    }


    @Override
    /**
     * 接收其他链广播过来的跨链交易Hash
     * */
    public void recvCtxHash(int chainId, String nodeId, BroadCtxHashMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("收到其他链节点{}广播过来的跨链交易,Hash：{}", nodeId, nativeHex);
        //判断是否接收过该交易,如果收到过则直接返回，如果没有收到过则向广播过来的节点发送获取完整跨链交易消息
        if (convertToCtxService.get(messageBody.getRequestHash(), handleChainId) != null) {
            chain.getLogger().info("本节点已经收到过该跨链交易，Hash：{}\n\n", nativeHex);
            return;
        }
        GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        if (chain.getCtxStageMap().get(messageBody.getRequestHash()) == null && chain.getCtxStageMap().putIfAbsent(messageBody.getRequestHash(), NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) == null) {
            chain.getLogger().info("第一次收到跨链交易Hash广播信息,Hash:{}", nativeHex);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
            chain.getLogger().info("向发送链节点{}获取完整跨链交易，Hash:{}", nodeId, nativeHex);
        } else {
            UntreatedMessage  untreatedSignMessage = new UntreatedMessage(chainId,nodeId,messageBody,messageBody.getRequestHash());
            chain.getSignMessageQueue().offer(untreatedSignMessage);
        }
        chain.getLogger().info("其他链广播的跨链交易Hash消息接收完成，Hash：{}\n\n", nativeHex);
    }

    @Override
    /**
     * 其他链节点向当前节点要完整跨链交易
     * */
    public void getOtherCtx(int chainId, String nodeId, GetOtherCtxMessage messageBody) {
        NewOtherCtxMessage responseMessage = new NewOtherCtxMessage();
        responseMessage.setRequestHash(messageBody.getRequestHash());
        NulsHash realCtxHash = messageBody.getRequestHash();
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("接收到请求链节点{}发送的获取完整跨链交易信息,Hash:{}", nodeId, nativeHex);
        //查到对应的跨链交易
        Transaction mainCtx = completedCtxService.get(realCtxHash, handleChainId);
        responseMessage.setCtx(mainCtx);
        //把完整跨链交易发送给请求节点
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.NEW_OTHER_CTX_MESSAGE);
        chain.getLogger().info("将完整跨链交易发送给请求连节点{},Hash:{}\n\n", nodeId, nativeHex);
    }

    @Override
    /**
     * 接收其他链节点发送过来的跨链交易
     * */
    public void recvOtherCtx(int chainId, String nodeId, NewOtherCtxMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsHash originalHash;
        String originalHex;

        NulsHash nativeHash = messageBody.getCtx().getHash();
        String nativeHex = nativeHash.toHex();
        //如果是主网接收友链发送过来的跨链交易，则originalHash为跨链交易中txData数据，如果为友链接收主网发送的跨链交易originalHash与Hash一样都是主网协议跨链交易
        if (!chain.isMainChain()) {
            originalHash = messageBody.getRequestHash();
            originalHex = nativeHex;
        } else {
            originalHash = new NulsHash(messageBody.getCtx().getTxData());
            originalHex = originalHash.toHex();
        }
        chain.getLogger().info("收到其他链节点{}发送过来的完整跨链交易,originalHash:{},Hash:{}", nodeId, originalHex, nativeHex);
        //判断本节点是否已经收到过该跨链交易，如果已收到过直接忽略
        if (convertToCtxService.get(originalHash, handleChainId) != null) {
            chain.getLogger().info("本节点已收到并处理过该跨链交易，originalHash:{},Hash:{}\n\n", originalHex, nativeHex);
            return;
        }
        try {
            NulsHash cacheHash;
            if (config.isMainNet()) {
                cacheHash = nativeHash;
            } else {
                cacheHash = originalHash;
            }
            /*
             * 修改跨链交易状态为已接收，处理中
             * */
            if (NulsCrossChainConstant.CTX_STATE_PROCESSING.equals(chain.getCtxStageMap().put(cacheHash, NulsCrossChainConstant.CTX_STATE_PROCESSING))) {
                chain.getLogger().info("该跨链交易正在处理中,originalHash:{},Hash:{}\n\n", originalHex, nativeHex);
                return;
            }
            UntreatedMessage untreatedCtxMessage = new UntreatedMessage(chainId,nodeId,messageBody,cacheHash);
            chain.getOtherCtxMessageQueue().offer(untreatedCtxMessage);
            chain.getLogger().info("其他链节点{}发送过来的完整跨链交易消息接收完成,originalHash:{},Hash:{}", nodeId, originalHex, nativeHex);
        } catch (Exception e) {
            chain.getLogger().error(e);
        } finally {
            chain.clearCache(nativeHash, originalHash);
        }
    }

    @Override
    public void getCirculat(int chainId, String nodeId, GetCirculationMessage messageBody) {
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
    public void recvCirculat(int chainId, String nodeId, CirculationMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getLogger().info("接收到友链:{}节点:{}发送的资产该链最新资产流通量信\n\n", chainId, nodeId);
        try {
            ChainManagerCall.sendCirculation(chainId, messageBody);
        } catch (NulsException e) {
            chain.getLogger().error(e);
        }
    }

    @Override
    public void recRegisteredChainInfo(int chainId, String nodeId, RegisteredChainMessage messageBody) {
        chainManager.getRegisteredChainMessageList().add(messageBody);
    }
}
