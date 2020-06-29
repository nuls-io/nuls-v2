package io.nuls.crosschain.nuls.utils;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.constant.CrossChainConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.message.GetOtherCtxMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.NodeType;
import io.nuls.crosschain.nuls.model.bo.message.WaitBroadSignMessage;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPO;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.rpc.call.TransactionCall;
import io.nuls.crosschain.nuls.srorage.ConvertCtxService;
import io.nuls.crosschain.nuls.srorage.ConvertHashService;
import io.nuls.crosschain.nuls.srorage.CtxStatusService;
import io.nuls.crosschain.nuls.srorage.SendHeightService;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.io.IOException;
import java.util.*;

/**
 * 消息工具类
 * Message Tool Class
 *
 * @author tag
 * 2019/5/20
 */
@Component
public class MessageUtil {
    @Autowired
    private static ConvertHashService convertHashService;

    @Autowired
    private static NulsCrossChainConfig config;

    @Autowired
    private static ChainManager chainManager;

    @Autowired
    private static ConvertCtxService convertCtxService;

    @Autowired
    private static CtxStatusService ctxStatusService;

    @Autowired
    private static SendHeightService sendHeightService;

    /**
     * 对本链广播的交易进行处理
     *
     * @param chain   本链信息
     * @param hash    交易缓存HASH
     * @param chainId 发送链ID
     * @param nodeId  发送节点ID
     * @param hashHex 交易Hash字符串（用于日志打印）
     */
    public static void handleSignMessage(Chain chain, NulsHash hash, int chainId, String nodeId, BroadCtxSignMessage messageBody, String hashHex) {
        try {
            int handleChainId = chain.getChainId();
            CtxStatusPO ctxStatusPO = ctxStatusService.get(hash, handleChainId);
            //如果交易在本节点已确认则无需再签名处理
            if (ctxStatusPO.getStatus() != TxStatusEnum.UNCONFIRM.getStatus() || messageBody.getSignature() == null) {
                chain.getLogger().info("跨链交易在本节点已经处理完成,Hash:{}\n\n", hashHex);
                return;
            }
            String signHex = HexUtil.encode(messageBody.getSignature());
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            p2PHKSignature.parse(messageBody.getSignature(), 0);
            Transaction convertCtx = ctxStatusPO.getTx();
            if (!config.isMainNet() && convertCtx.getType() == config.getCrossCtxType()) {
                convertCtx = convertCtxService.get(hash, handleChainId);
            }
            //验证签名是否正确，如果是跨链转账交易，这签名对应的主网协议签名
            if (!ECKey.verify(convertCtx.getHash().getBytes(), p2PHKSignature.getSignData().getSignBytes(), p2PHKSignature.getPublicKey())) {
                chain.getLogger().info("签名验证错误，hash:{},签名:{}\n\n", hashHex, signHex);
                return;
            }
            MessageUtil.signByzantine(chain, chainId, hash, ctxStatusPO.getTx(), messageBody, hashHex, signHex, nodeId);
        } catch (NulsException e) {
            chain.getLogger().error(e);
        } catch (IOException io) {
            chain.getLogger().error(io);
        }

    }

    /**
     * 对其他链广播的的交易进行处理
     *
     * @param chain     本链信息
     * @param cacheHash 交易缓存HASH
     * @param chainId   发送链ID
     * @param nodeId    发送节点ID
     * @param hashHex   交易Hash字符串（用于日志打印）
     */
    public static void handleNewHashMessage(Chain chain, NulsHash cacheHash, int chainId, String nodeId, String hashHex) {
        int tryCount = 0;
        while (chain.getOtherCtxStageMap().get(cacheHash) != null && chain.getOtherCtxStageMap().get(cacheHash) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE && tryCount < NulsCrossChainConstant.BYZANTINE_TRY_COUNT) {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
            tryCount++;
        }
        if (chain.getOtherCtxStageMap().get(cacheHash) != null && chain.getOtherCtxStageMap().get(cacheHash) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) {
            GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
            responseMessage.setRequestHash(cacheHash);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
            chain.getLogger().info("获取交易超时，向跨链节点{}重新获取完整跨链交易，Hash:{}", nodeId, hashHex);
        } else {
            chain.getOtherHashNodeIdMap().putIfAbsent(cacheHash, new ArrayList<>());
            chain.getOtherHashNodeIdMap().get(cacheHash).add(new NodeType(nodeId, 1));
        }
        chain.getLogger().debug("跨链节点{}广播过来的跨链交易Hash或签名消息处理完成,Hash：{}\n\n", nodeId, hashHex);
    }

    /**
     * 交易签名拜占庭处理
     *
     * @param chain       本链信息
     * @param chainId     发送链ID
     * @param realHash    本链协议跨链交易Hash
     * @param ctx         跨链交易
     * @param messageBody 消息
     * @param nativeHex   交易Hash字符串
     * @param signHex     交易签名字符串
     */
    @SuppressWarnings("unchecked")
    public static void signByzantine(Chain chain, int chainId, NulsHash realHash, Transaction ctx, BroadCtxSignMessage messageBody, String nativeHex, String signHex, String excludeNodes) throws NulsException, IOException {
        //判断节点是否已经收到并广播过该签名，如果已经广播过则不需要再广播
        TransactionSignature signature = new TransactionSignature();
        if (ctx.getTransactionSignature() != null) {
            signature.parse(ctx.getTransactionSignature(), 0);
            for (P2PHKSignature sign : signature.getP2PHKSignatures()) {
                if (Arrays.equals(messageBody.getSignature(), sign.serialize())) {
                    chain.getLogger().debug("本节点已经收到过该跨链交易的该签名,Hash:{},签名:{}\n\n", nativeHex, signHex);
                    return;
                }
            }
        } else {
            List<P2PHKSignature> p2PHKSignatureList = new ArrayList<>();
            signature.setP2PHKSignatures(p2PHKSignatureList);
        }
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.parse(messageBody.getSignature(), 0);
        signature.getP2PHKSignatures().add(p2PHKSignature);
        //交易签名拜占庭
        List<String> packAddressList;
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            packAddressList = (List<String>) ConsensusCall.getSeedNodeList(chain).get(ParamConstant.PARAM_PACK_ADDRESS_LIST);
        } else {
            packAddressList = chain.getVerifierList();
        }
        signByzantineInChain(chain, ctx, signature, packAddressList, realHash);
        NetWorkCall.broadcast(chainId, messageBody, excludeNodes, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
        chain.getLogger().info("将新收到的跨链交易签名广播给链接到的其他节点,Hash:{},签名:{}\n\n", nativeHex, signHex);
    }

    /**
     * 交易签名拜占庭验证
     *
     * @param chain           本链信息
     * @param ctx             跨链交易
     * @param signature       签名列表
     * @param packAddressList 验证账户列表
     * @return 拜占庭验证是否通过
     */
    public static boolean signByzantineInChain(Chain chain, Transaction ctx, TransactionSignature signature, List<String> packAddressList, NulsHash realHash) throws NulsException, IOException {
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            return verifierInitLocalByzantine(chain, ctx, signature, packAddressList, realHash);
        } else if (ctx.getType() == TxType.VERIFIER_CHANGE) {
            return verifierChangeLocalByzantine(chain, ctx, signature, realHash);
        } else {
            return crossTransferLocalByzantine(chain, ctx, signature, realHash);
        }
    }

    private static boolean verifierInitLocalByzantine(Chain chain, Transaction ctx, TransactionSignature signature, List<String> packAddressList, NulsHash realHash) throws  IOException {
        List<String> handleAddressList = new ArrayList<>(packAddressList);
        int agentCount = handleAddressList.size();
        //交易签名拜占庭
        int byzantineCount = CommonUtil.getByzantineCount(chain, agentCount);
        int signCount = signature.getSignersCount();
        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        if (signCount >= byzantineCount) {
            //去掉不是当前验证人的签名和重复签名
            List<P2PHKSignature> misMatchSignList = CommonUtil.getMisMatchSigns(chain, signature, handleAddressList);
            signCount = signature.getSignersCount();
            if (signCount >= byzantineCount) {
                ctx.setTransactionSignature(signature.serialize());
                long sendHeight = config.getSendHeight();
                if (chainManager.getChainHeaderMap().get(chain.getChainId()) != null) {
                    sendHeight = chainManager.getChainHeaderMap().get(chain.getChainId()).getHeight();
                }
                ctxStatusPO.setStatus(TxStatusEnum.CONFIRMED.getStatus());
                ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
                saveCtxSendHeight(chain, sendHeight, ctx);
                chain.getLogger().info("初始化验证人交易签名拜占庭验证通过,保存验证人变更高度等待广播，Hash{},广播高度{}", ctx.getHash().toHex(), sendHeight);
                return true;
            } else {
                signature.getP2PHKSignatures().addAll(misMatchSignList);
                ctx.setTransactionSignature(signature.serialize());
            }
        } else {
            ctx.setTransactionSignature(signature.serialize());
        }
        ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
        return false;
    }

    private static boolean verifierChangeLocalByzantine(Chain chain, Transaction ctx, TransactionSignature signature, NulsHash realHash) throws NulsException, IOException {
        List<String> handleAddressList;
        try {
            chain.getSwitchVerifierLock().readLock().lock();
            handleAddressList = new ArrayList<>(chain.getVerifierList());
        } finally {
            chain.getSwitchVerifierLock().readLock().unlock();
        }
        VerifierChangeData txData = new VerifierChangeData();
        txData.parse(ctx.getTxData(), 0);
        if (txData.getCancelAgentList() != null && !txData.getCancelAgentList().isEmpty()) {
            handleAddressList.removeAll(txData.getCancelAgentList());
        }
        int agentCount = handleAddressList.size();
        //交易签名拜占庭
        int byzantineCount = CommonUtil.getByzantineCount(chain, agentCount);
        int signCount = signature.getSignersCount();
        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        if (signCount >= byzantineCount) {
            //去掉不是当前验证人的签名和重复签名
            List<P2PHKSignature> misMatchSignList = CommonUtil.getMisMatchSigns(chain, signature, handleAddressList);
            signCount = signature.getSignersCount();
            if (signCount >= byzantineCount) {
                ctx.setTransactionSignature(signature.serialize());
                ctxStatusPO.setStatus(TxStatusEnum.BYZANTINE_COMPLETE.getStatus());
                ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
                TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                chain.getLogger().info("验证人变更交易签名拜占庭验证通过,将跨链交易广播给交易模块处理，Hash{}", ctx.getHash().toHex());
                return true;
            } else {
                signature.getP2PHKSignatures().addAll(misMatchSignList);
                ctx.setTransactionSignature(signature.serialize());
            }
        } else {
            ctx.setTransactionSignature(signature.serialize());
        }
        ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
        return false;
    }

    private static boolean crossTransferLocalByzantine(Chain chain, Transaction ctx, TransactionSignature signature, NulsHash realHash) throws NulsException, IOException {
        List<String> handleAddressList;
        long broadHeight = chainManager.getChainHeaderMap().get(chain.getChainId()).getHeight();
        try {
            chain.getSwitchVerifierLock().readLock().lock();
            handleAddressList = new ArrayList<>(chain.getVerifierList());
            if (broadHeight < chain.getLastChangeHeight()) {
                broadHeight = chain.getLastChangeHeight();
            }
        } finally {
            chain.getSwitchVerifierLock().readLock().unlock();
        }
        int agentCount = handleAddressList.size();
        //交易签名拜占庭
        int byzantineCount = CommonUtil.getByzantineCount(chain, agentCount);
        int signCount = signature.getSignersCount();
        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        if (signCount >= byzantineCount) {
            //去掉不是当前验证人的签名和重复签名
            List<P2PHKSignature> misMatchSignList = CommonUtil.getMisMatchSigns(chain, signature, handleAddressList);
            signCount = signature.getSignersCount();
            if (signCount >= byzantineCount) {
                ctx.setTransactionSignature(signature.serialize());
                ctxStatusPO.setStatus(TxStatusEnum.CONFIRMED.getStatus());
                ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
                saveCtxSendHeight(chain, broadHeight, ctx);
                chain.getLogger().info("跨链交易拜占庭完成，放入待打包队列，等待广播，Hash:{},sendHeight:{},txType:{}",ctx.getHash().toHex(), broadHeight, ctx.getType());
                return true;
            } else {
                signature.getP2PHKSignatures().addAll(misMatchSignList);
                ctx.setTransactionSignature(signature.serialize());
            }
        } else {
            ctx.setTransactionSignature(signature.serialize());
        }
        ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
        return false;
    }

    /**
     * 处理接收到的其他链节点节点广播过来的跨链交易
     *
     * @param chain       本链信息
     * @param ctx         跨链交易
     * @param fromChainId 发送交易的链ID
     */
    public static boolean handleOtherChainCtx(Transaction ctx, Chain chain, int fromChainId) {
        NulsHash ctxHash = ctx.getHash();
        try {
            if(ctx.getType() == TxType.REGISTERED_CHAIN_CHANGE && config.isMainNet()){
                return false;
            }
            TransactionSignature signature = new TransactionSignature();
            signature.parse(ctx.getTransactionSignature(), 0);
            int verifierChainId = fromChainId;
            if (!config.isMainNet()) {
                verifierChainId = config.getMainChainId();
            }
            //跨链间传输的交易都是主网协议交易
            if (ctx.getType() == TxType.CROSS_CHAIN || ctx.getType() == TxType.CONTRACT_TOKEN_CROSS_TRANSFER) {
                if (!handleOtherChainCrossTransferTx(chain, ctx, signature, verifierChainId)) {
                    return false;
                }
            } else if (ctx.getType() == TxType.VERIFIER_CHANGE) {
                if (!handleOtherChainVerifierChangeTx(chain, ctx, signature, verifierChainId)) {
                    return false;
                }
            } else if(ctx.getType() == TxType.VERIFIER_INIT){
                if (!handleOtherChainVerifierInitTx(chain, ctx, signature, verifierChainId)) {
                    return false;
                }
            }else{
                if(!handleOtherChainCrossTx(chain, ctx, signature, verifierChainId)){
                    return false;
                }
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
        convertHashService.save(ctxHash, ctxHash, chain.getChainId());
        return true;
    }

    private static boolean handleOtherChainVerifierInitTx(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId) {
        Set<String> verifierList;
        int minPassCount;
        if (!config.isMainNet()) {
            verifierList = new HashSet<>(Arrays.asList(config.getVerifiers().split(NulsCrossChainConstant.VERIFIER_SPLIT)));
            minPassCount = verifierList.size() * config.getMainByzantineRatio() / CrossChainConstant.MAGIC_NUM_100;
            if (minPassCount == 0) {
                minPassCount = 1;
            }
        } else {
            ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
            if (chainInfo == null) {
                chain.getLogger().error("链未注册,chainId:{}", verifierChainId);
                return false;
            }
            verifierList = chainInfo.getVerifierList();
            minPassCount = chainInfo.getMinPassCount();
        }
        try {
            if (!otherCtxSignValidate(chain, ctx, signature, verifierChainId, verifierList, minPassCount)) {
                chain.getLogger().error("验证人初始化交易签名拜占庭验证失败，hash:{}", ctx.getHash().toHex());
                return false;
            }
            TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
            chain.getLogger().debug("接收链初始化验证人交易验证完成，发送给交易模块处理，hash:{}", ctx.getHash().toHex());
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    private static boolean handleOtherChainVerifierChangeTx(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId) {
        ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
        if (chainInfo == null) {
            chain.getLogger().error("链未注册,chainId:{}", verifierChainId);
            return false;
        }
        VerifierChangeData verifierChangeData = new VerifierChangeData();
        Set<String> verifierList = new HashSet<>(chainInfo.getVerifierList());
        try {
            verifierChangeData.parse(ctx.getTxData(), 0);
            boolean haveCancelVerifier = verifierChangeData.getCancelAgentList() != null && !verifierChangeData.getCancelAgentList().isEmpty();
            boolean dataValid = haveCancelVerifier || (verifierChangeData.getRegisterAgentList() != null && !verifierChangeData.getRegisterAgentList().isEmpty());
            if (!dataValid) {
                chain.getLogger().error("Abnormal change of transaction data of verifier: there is no changed verifier");
                return false;
            }
            //一笔验证人变更交易最多退出30%的验证人
            if (haveCancelVerifier) {
                int maxCancelCount = chainInfo.getVerifierList().size() * NulsCrossChainConstant.VERIFIER_CANCEL_MAX_RATE / NulsCrossChainConstant.MAGIC_NUM_100;
                if (verifierChangeData.getCancelAgentList().size() > maxCancelCount) {
                    chain.getLogger().error("Abnormal change of transaction data of verifier: the verifier who exits is more than 30%,cancelCount:{},maxCancelCount:{},totalCount:{}", verifierChangeData.getCancelAgentList().size(), maxCancelCount, chainInfo.getVerifierList().size());
                    return false;
                }
                verifierList.removeAll(verifierChangeData.getCancelAgentList());
            }
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return false;
        }
        int minPassCount = chainInfo.getMinPassCount(verifierList.size());
        try {
            if (!otherCtxSignValidate(chain, ctx, signature, verifierChainId, verifierList, minPassCount)) {
                chain.getLogger().error("验证人变更交易签名拜占庭验证失败，hash:{}", ctx.getHash().toHex());
                return false;
            }
            TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
            chain.getLogger().debug("验证人变更交易验证完成，发送给交易模块处理，hash:{}", ctx.getHash().toHex());
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    private static boolean handleOtherChainCrossTransferTx(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId) {
        ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
        if (chainInfo == null) {
            chain.getLogger().error("链未注册,chainId:{}", verifierChainId);
            return false;
        }
        Set<String> verifierList = chainInfo.getVerifierList();
        int minPassCount = chainInfo.getMinPassCount();
        try {
            if (!otherCtxSignValidate(chain, ctx, signature, verifierChainId, verifierList, minPassCount)) {
                chain.getLogger().error("跨链转账交易签名拜占庭验证失败，hash:{}", ctx.getHash().toHex());
                return false;
            }
            CoinData coinData = ctx.getCoinDataInstance();
            int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
            Transaction packCtx = ctx;
            String crossTxHashHex = ctx.getHash().toHex();
            //如果本链为接收链则直接发送交易模块打包
            if (chain.getChainId() == toChainId) {
                if (!config.isMainNet()) {
                    packCtx = TxUtil.mainConvertToFriend(ctx, config.getCrossCtxType());
                    packCtx.setTransactionSignature(signature.serialize());
                    convertCtxService.save(packCtx.getHash(), ctx, chain.getChainId());
                    chain.getLogger().info("接收到的主网协议跨链交易hash：{}对应的本链协议跨链交易hash:{}", crossTxHashHex, packCtx.getHash().toHex());
                }
            } else {
                if (!config.isMainNet()) {
                    chain.getLogger().error("跨链交易验证失败，hash:{}", crossTxHashHex);
                    return false;
                }
            }
            TransactionCall.sendTx(chain, RPCUtil.encode(packCtx.serialize()));
            chain.getLogger().debug("跨链转账交易验证完成，发送给交易模块处理，hash:{}", crossTxHashHex);
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    private static boolean handleOtherChainCrossTx(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId) {
        ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
        if (chainInfo == null) {
            chain.getLogger().error("链未注册,chainId:{}", verifierChainId);
            return false;
        }
        Set<String> verifierList = chainInfo.getVerifierList();
        int minPassCount = chainInfo.getMinPassCount();
        try {
            String crossTxHashHex = ctx.getHash().toHex();
            if (!otherCtxSignValidate(chain, ctx, signature, verifierChainId, verifierList, minPassCount)) {
                chain.getLogger().error("其他广播的跨链交易验证失败，hash:{},txType:{}", crossTxHashHex, ctx.getType());
                return false;
            }
            TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
            chain.getLogger().debug("其他广播的跨链交易验证完成，发送给交易模块处理，hash:{},txType:{}", crossTxHashHex, ctx.getType());
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    /**
     * 其他链协议跨链交易签名验证
     * Signature Verification of Cross-Chain Transactions in Other Chain Protocols
     *
     * @param chain           当前链信息
     * @param signature       交易签名
     * @param ctx             交易
     * @param verifierChainId 发送链ID
     * @param verifierList    验证人列表
     * @param minPassCount    最小签名数
     */
    private static boolean otherCtxSignValidate(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId, Set<String> verifierList, int minPassCount) throws NulsException {
        if (verifierList == null || verifierList.isEmpty()) {
            chain.getLogger().error("链还未注册验证人,chainId:{}", verifierChainId);
            return false;
        }
        int passCount = 0;
        List<P2PHKSignature> signatureList = signature.getP2PHKSignatures();
        if (signatureList == null || signatureList.size() < minPassCount) {
            chain.getLogger().error("跨链交易签名数量小于拜占庭验证最小数量，signCount{},minPassCount{}", signatureList == null ? 0 : signatureList.size(), minPassCount);
            return false;
        }
        byte[] hashByte = ctx.getHash().getBytes();
        Set<String> passedAddress = new HashSet<>();
        for (P2PHKSignature sign : signatureList) {
            if (!SignatureUtil.validateSignture(hashByte, sign)) {
                continue;
            }
            for (String verifier : verifierList) {
                if (passedAddress.contains(verifier)) {
                    continue;
                }
                if (Arrays.equals(AddressTool.getAddress(sign.getPublicKey(), verifierChainId), AddressTool.getAddress(verifier))) {
                    passedAddress.add(verifier);
                    passCount++;
                    break;
                }
            }
        }
        if (passCount < minPassCount) {
            chain.getLogger().error("签名验证通过数量小于拜占庭验证最小数量,passCount{},minPassCount{}", passCount, minPassCount);
            return false;
        }
        return true;
    }


    /**
     * 广播签名
     *
     * @param chain     本链信息
     * @param hash      要广播的交易hash
     * @param chainId   接收链ID
     * @param nativeHex 本链协议交易Hash
     */
    public static void broadcastCtx(Chain chain, NulsHash hash, int chainId, String nativeHex) {
        if (chain.getWaitBroadSignMap().get(hash) != null) {
            Iterator<WaitBroadSignMessage> iterator = chain.getWaitBroadSignMap().get(hash).iterator();
            Set<BroadCtxSignMessage> broadMessageSet = new HashSet<>();
            while (iterator.hasNext()) {
                WaitBroadSignMessage waitBroadSignMessage = iterator.next();
                BroadCtxSignMessage message = waitBroadSignMessage.getMessage();
                String node = waitBroadSignMessage.getNodeId();
                if (!broadMessageSet.contains(message)) {
                    if (NetWorkCall.broadcast(chainId, message, node, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false)) {
                        iterator.remove();
                        String signStr = "";
                        if (message.getSignature() != null) {
                            signStr = HexUtil.encode(message.getSignature());
                        }
                        chain.getLogger().info("将跨链交易签名广播给链内其他节点,hash:{},sign:{}", nativeHex, signStr);
                        broadMessageSet.add(message);
                    }
                } else {
                    iterator.remove();
                }
            }
            if (chain.getWaitBroadSignMap().get(hash).isEmpty()) {
                chain.getWaitBroadSignMap().remove(hash);
            }
        }
    }

    /**
     * 是否可以发送跨链相关消息
     *
     * @param chain     本链信息
     * @param toChainId 接收链ID
     * @return 当前跨链网络状态 0链已注销1不可广播2可广播
     */
    public static byte canSendMessage(Chain chain, int toChainId) {
        try {
            int minNodeAmount = chain.getConfig().getMinNodeAmount();
            boolean chainExist = false;
            for (ChainInfo chainInfo : chainManager.getRegisteredCrossChainList()) {
                if (chainInfo.getChainId() == toChainId) {
                    if (config.isMainNet()) {
                        minNodeAmount = chainInfo.getMinAvailableNodeNum();
                    }
                    chainExist = true;
                    break;
                }
            }
            if (!chainExist) {
                return 0;
            }
            int linkedNode = NetWorkCall.getAvailableNodeAmount(toChainId, true);
            if (linkedNode >= minNodeAmount) {
                return 2;
            } else {
                chain.getLogger().debug("当前节点链接到的跨链节点数小于最小链接数,crossChainId:{},linkedNodeCount:{},minLinkedCount:{}", toChainId, linkedNode, minNodeAmount);
            }
        } catch (NulsException e) {
            chain.getLogger().error(e);
        }
        return 1;
    }

    public static synchronized void saveCtxSendHeight(Chain chain, long sendHeight, Transaction ctx) {
        SendCtxHashPO sendCtxHashPo = sendHeightService.get(sendHeight, chain.getChainId());
        if (sendCtxHashPo == null) {
            List<NulsHash> hashList = new ArrayList<>();
            hashList.add(ctx.getHash());
            sendCtxHashPo = new SendCtxHashPO(hashList);
        } else {
            sendCtxHashPo.getHashList().add(ctx.getHash());
        }
        sendHeightService.save(sendHeight, sendCtxHashPo, chain.getChainId());
    }
}
