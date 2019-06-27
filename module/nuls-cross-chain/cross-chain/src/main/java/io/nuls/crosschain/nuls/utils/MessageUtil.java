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
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.message.GetCtxMessage;
import io.nuls.crosschain.base.message.GetOtherCtxMessage;
import io.nuls.crosschain.base.message.VerifyCtxMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.bo.NodeType;
import io.nuls.crosschain.nuls.model.po.CtxStatusPO;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPo;
import io.nuls.crosschain.nuls.rpc.call.AccountCall;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.rpc.call.TransactionCall;
import io.nuls.crosschain.nuls.srorage.*;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import org.checkerframework.checker.units.qual.A;

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
     * @param chain     本链信息
     * @param cacheHash 交易缓存HASH
     * @param chainId   发送链ID
     * @param nodeId    发送节点ID
     * @param hashHex   交易Hash字符串（用于日志打印）
     */
    public static void handleSignMessage(Chain chain, NulsHash cacheHash, int chainId, String nodeId,String hashHex) {
        int tryCount = 0;
        while (chain.getCtxStageMap().get(cacheHash) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE && tryCount < NulsCrossChainConstant.BYZANTINE_TRY_COUNT) {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
            tryCount++;
        }
        if (chain.getCtxStageMap().get(cacheHash) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) {
            GetCtxMessage responseMessage = new GetCtxMessage();
            responseMessage.setRequestHash(cacheHash);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_CTX_MESSAGE);
            chain.getLogger().info("向链内节点{}获取完整跨链交易，Hash:{}", nodeId, hashHex);
        } else {
            chain.getHashNodeIdMap().putIfAbsent(cacheHash, new ArrayList<>());
            chain.getHashNodeIdMap().get(cacheHash).add(new NodeType(nodeId, 2));
        }
        chain.getLogger().info("链内节点{}广播过来的跨链交易Hash或签名消息处理完成,Hash：{}\n\n", nodeId, hashHex);
    }

    /**
     * 对其他链广播的的交易进行处理
     * @param chain     本链信息
     * @param cacheHash 交易缓存HASH
     * @param chainId   发送链ID
     * @param nodeId    发送节点ID
     * @param hashHex   交易Hash字符串（用于日志打印）
     */
    public static void handleNewHashMessage(Chain chain, NulsHash cacheHash, int chainId, String nodeId,String hashHex) {
        int tryCount = 0;
        while (chain.getOtherCtxStageMap().get(cacheHash) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE && tryCount < NulsCrossChainConstant.BYZANTINE_TRY_COUNT) {
            try {
                Thread.sleep(2000);
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
            tryCount++;
        }
        if (chain.getOtherCtxStageMap().get(cacheHash) == NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) {
            GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
            responseMessage.setRequestHash(cacheHash);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
            chain.getLogger().info("向跨链节点{}获取完整跨链交易，Hash:{}", nodeId, hashHex);
        } else {
            chain.getOtherHashNodeIdMap().putIfAbsent(cacheHash, new ArrayList<>());
            chain.getOtherHashNodeIdMap().get(cacheHash).add(new NodeType(nodeId, 1));
        }
        chain.getLogger().info("跨链节点{}广播过来的跨链交易Hash或签名消息处理完成,Hash：{}\n\n", nodeId, hashHex);
    }

    /**
     * 交易签名拜占庭处理
     * @param chain        本链信息
     * @param chainId      发送链ID
     * @param realHash     本链协议跨链交易Hash
     * @param ctx          跨链交易
     * @param messageBody  消息
     * @param nativeHex    交易Hash字符串
     * @param signHex      交易签名字符串
     */
    public static void signByzantine(Chain chain, int chainId, NulsHash realHash, Transaction ctx, BroadCtxSignMessage messageBody, String nativeHex, String signHex) throws NulsException, IOException {
        //判断节点是否已经收到并广播过该签名，如果已经广播过则不需要再广播
        int handleChainId = chain.getChainId();
        TransactionSignature signature = new TransactionSignature();
        if (ctx.getTransactionSignature() != null) {
            signature.parse(ctx.getTransactionSignature(), 0);
            for (P2PHKSignature sign : signature.getP2PHKSignatures()) {
                if (Arrays.equals(messageBody.getSignature(), sign.serialize())) {
                    chain.getLogger().info("本节点已经收到过该跨链交易的该签名,Hash:{},签名:{}\n\n", nativeHex, signHex);
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
        List<String> packAddressList = CommonUtil.getCurrentPackAddressList(chain);
        if(signByzantineInChain(chain, ctx, signature, packAddressList)){
            CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx,TxStatusEnum.CONFIRMED.getStatus());
            ctxStatusService.save(realHash, ctxStatusPO, handleChainId);
        }
        NetWorkCall.broadcast(chainId, messageBody, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
        chain.getLogger().info("将收到的跨链交易签名广播给链接到的其他节点,Hash:{},签名:{}\n\n", nativeHex, signHex);
    }

    /**
     * 交易签名拜占庭验证
     * @param chain              本链信息
     * @param ctx                跨链交易
     * @param signature          签名列表
     * @param packAddressList    验证账户列表
     * @return                   拜占庭验证是否通过
     */
    public static boolean signByzantineInChain(Chain chain,Transaction ctx,TransactionSignature signature,List<String>packAddressList)throws NulsException,IOException{
        //交易签名拜占庭
        int byzantineCount = CommonUtil.getByzantineCount(packAddressList, chain);
        //如果为友链中跨链转账交易，则需要减掉本链协议交易签名
        if(!config.isMainNet() && ctx.getType() == config.getCrossCtxType()){
            Set<String> fromAddressList = ctx.getCoinDataInstance().getFromAddressList();
            for (String address:fromAddressList) {
                if(packAddressList.contains(address)){
                    byzantineCount += 1;
                }else{
                    packAddressList.add(address);
                    byzantineCount += 2;
                }
            }
        }
        int signCount = signature.getP2PHKSignatures().size();
        if (signCount >= byzantineCount) {
            List<P2PHKSignature> misMatchSignList = CommonUtil.getMisMatchSigns(chain, signature, packAddressList);
            signCount -= misMatchSignList.size();
            if (signCount >= byzantineCount) {
                ctx.setTransactionSignature(signature.serialize());
                //如果本链为发起链则发送交易模块处理，否则直接放入待广播队列
                if(ctx.getType() == config.getCrossCtxType()){
                    int fromChainId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getFrom().get(0).getAddress());
                    if(fromChainId == chain.getChainId()){
                        TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                        chain.getLogger().info("签名拜占庭验证通过,将跨链交易广播给交易模块处理，最小验证人数:{}，签名数量为：{}", byzantineCount,signCount);
                    }else{
                        long sendHeight = chainManager.getChainHeaderMap().get(chain.getChainId()).getHeight() + config.getSendHeight();
                        SendCtxHashPo sendCtxHashPo = sendHeightService.get(sendHeight, chain.getChainId());
                        if(sendCtxHashPo == null){
                            List<NulsHash> hashList = new ArrayList<>();
                            hashList.add(ctx.getHash());
                            sendCtxHashPo = new SendCtxHashPo(hashList);
                        }else{
                            sendCtxHashPo.getHashList().add(ctx.getHash());
                        }
                        sendHeightService.save(sendHeight, sendCtxHashPo, chain.getChainId());
                        chain.getLogger().info("签名拜占庭验证通过,将交易广播给其他链节点，最小验证人数:{}，签名数量为：{}", byzantineCount,signCount);
                    }
                }else{
                    long sendHeight = chainManager.getChainHeaderMap().get(chain.getChainId()).getHeight() + config.getSendHeight();
                    SendCtxHashPo sendCtxHashPo = sendHeightService.get(sendHeight, chain.getChainId());
                    if(sendCtxHashPo == null){
                        List<NulsHash> hashList = new ArrayList<>();
                        hashList.add(ctx.getHash());
                        sendCtxHashPo = new SendCtxHashPo(hashList);
                    }else{
                        sendCtxHashPo.getHashList().add(ctx.getHash());
                    }
                    sendHeightService.save(sendHeight, sendCtxHashPo, chain.getChainId());
                    chain.getLogger().info("签名拜占庭验证通过,将交易广播给其他链节点，最小验证人数:{}，签名数量为：{}", byzantineCount,signCount);
                }
                return true;
            } else {
                signature.getP2PHKSignatures().addAll(misMatchSignList);
                ctx.setTransactionSignature(signature.serialize());
            }
        } else {
            ctx.setTransactionSignature(signature.serialize());
        }
        return false;
    }


    /**
     * 处理接收到的链内节点广播过来的跨链交易
     * @param chain        本链信息
     * @param ctx          跨链交易
     */
    @SuppressWarnings("unchecked")
    public static boolean handleInChainCtx(Transaction ctx, Chain chain){
        try {
            NulsHash nativeHash = ctx.getHash();
            String nativeHex = nativeHash.toHex();
            //验证交易签名正确性
            BroadCtxSignMessage message = new BroadCtxSignMessage();
            message.setLocalHash(nativeHash);
            Transaction realTransaction = ctx;
            if(!config.isMainNet() && ctx.getType() == config.getCrossCtxType()){
                if(!SignatureUtil.validateTransactionSignture(ctx)){
                    chain.getLogger().error("交易签名验证失败,hash:{}",nativeHex);
                }
                realTransaction = TxUtil.friendConvertToMain(chain, ctx, null, config.getCrossCtxType());
            }
            if(!SignatureUtil.validateCtxSignture(realTransaction)){
                chain.getLogger().error("交易签名验证失败,hash:{}",nativeHex);
                return false;
            }

            Map packerInfo = ConsensusCall.getPackerInfo(chain);
            List<String>packAddressList = (List<String>) packerInfo.get("packAddressList");
            CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx,TxStatusEnum.UNCONFIRM.getStatus());
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.parse(ctx.getTransactionSignature(),0 );
            //先判断新交易是否已经达到拜占庭比例，如果已经达到拜占庭比例则本节点不需要再签名,否则需判断本节点是否为共识节点，然后再做拜占庭
            if(signByzantineInChain(chain, ctx, transactionSignature, packAddressList)){
                ctxStatusPO.setStatus(TxStatusEnum.CONFIRMED.getStatus());
            }else{
                String password = (String) packerInfo.get("password");
                String address = (String) packerInfo.get("address");
                if (!StringUtils.isBlank(address)) {
                    chain.getLogger().info("本节点为共识节点，对跨链交易签名,hash:{}", nativeHex);
                    P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, realTransaction.getHash().getBytes());
                    transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
                    message.setSignature(p2PHKSignature.serialize());
                    transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
                    //将收到的消息放入缓存中，等到收到交易后再广播该签名给其他节点
                    if (!chain.getWaitBroadSignMap().keySet().contains(nativeHash)) {
                        chain.getWaitBroadSignMap().put(nativeHash, new HashSet<>());
                    }
                    chain.getWaitBroadSignMap().get(nativeHash).add(message);
                }
                if(signByzantineInChain(chain, ctx, transactionSignature, packAddressList)){
                    ctxStatusPO.setStatus(TxStatusEnum.CONFIRMED.getStatus());
                }
            }
            //保存交易
            ctxStatusService.save(nativeHash, ctxStatusPO, chain.getChainId());
            if(!config.isMainNet()){
                if(ctx.getType() == config.getCrossCtxType()){
                    convertCtxService.save(nativeHash, realTransaction, chain.getChainId());
                }
                convertHashService.save(realTransaction.getHash(), nativeHash, chain.getChainId());
            }

            //广播交易
            broadcastCtx(chain, nativeHash, chain.getChainId(),nativeHex);
        }catch (Exception e){
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    /**
     * 处理接收到的其他链节点节点广播过来的跨链交易
     * @param chain        本链信息
     * @param ctx          跨链交易
     * @param fromChainId  发送交易的链ID
     */
    public static boolean handleOtherChainCtx(Transaction ctx, Chain chain,int fromChainId){
        NulsHash ctxHash = ctx.getHash();
        String otherHashHex = ctxHash.toHex();
        try {
            TransactionSignature signature = new TransactionSignature();
            signature.parse(ctx.getTransactionSignature(),0);
            if(!otherCtxSignValidate(chain, ctx, signature, fromChainId)){
                chain.getLogger().error("跨链交易签名验证失败,hash:{}",otherHashHex);
                return false;
            }
            //跨链间传输的交易都是主网协议交易
            if(ctx.getType() == TxType.CROSS_CHAIN){
                CoinData coinData = ctx.getCoinDataInstance();
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                Transaction packCtx = ctx;
                //如果本链为接收链则直接发送交易模块打包
                if(chain.getChainId() == toChainId){
                    if(!config.isMainNet() && ctx.getType() == config.getCrossCtxType() ){
                        packCtx = TxUtil.mainConvertToFriend(ctx, config.getCrossCtxType());
                        packCtx.setTransactionSignature(signature.serialize());
                        convertCtxService.save(packCtx.getHash(), ctx, chain.getChainId());
                    }
                    TransactionCall.sendTx(chain, RPCUtil.encode(packCtx.serialize()));
                }else{
                    if(!config.isMainNet()){
                        chain.getLogger().error("跨链交易验证失败，hash:{}",otherHashHex);
                        return false;
                    }
                    TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                    ctx.setTransactionSignature(null);
                    TxUtil.handleNewCtx(ctx, chain);
                }
            }else if(ctx.getType() == TxType.VERIFIER_CHANGE){
                VerifierChangeData verifierChangeData = new VerifierChangeData();
                verifierChangeData.parse(ctx.getTxData(),0);
                boolean isCorrect = (config.isMainNet() && verifierChangeData.getChainId() != config.getMainChainId())
                        || (!config.isMainNet() && verifierChangeData.getChainId() == config.getMainChainId());
                if(!isCorrect){
                    chain.getLogger().error("验证人变更信息无效,hash:{}",otherHashHex);
                }
                TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
            }
        }catch (Exception e){
            chain.getLogger().error("跨链交易处理失败，hash:{}",otherHashHex);
            chain.getLogger().error(e);
            return false;
        }
        convertHashService.save(ctxHash, ctxHash, chain.getChainId());
        return true;
    }

    /**
     * 其他链协议跨链交易签名验证
     * Signature Verification of Cross-Chain Transactions in Other Chain Protocols
     * @param chain          当前链信息
     * @param signature      交易签名
     * @param ctx            交易
     * @param fromChainId    发送链ID
     * */
    private static boolean otherCtxSignValidate(Chain chain,Transaction ctx,TransactionSignature signature,int fromChainId)throws NulsException{
        //验证交易签名正确性
        int verifierChainId = fromChainId;
        if(!config.isMainNet()){
            verifierChainId = config.getMainChainId();
        }
        ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
        if(chainInfo == null){
            chain.getLogger().error("链未注册,chainId:{}",verifierChainId);
            return false;
        }
        Set<String> verifierList = chainInfo.getVerifierList();
        if(verifierList == null || verifierList.isEmpty()){
            chain.getLogger().error("链还未注册验证人,chainId:{}",verifierChainId);
            return  false;
        }
        int minPassCount = chainInfo.getMinPassCount();
        int passCount = 0;
        List<P2PHKSignature> signatureList = signature.getP2PHKSignatures();
        if(signatureList == null || signatureList.size() < minPassCount){
            return false;
        }
        byte[] hashByte = ctx.getHash().getBytes();
        Set<String> passedAddress  = new HashSet<>();
        for (P2PHKSignature sign:signatureList) {
            if(!SignatureUtil.validateSignture(hashByte, sign)){
                continue;
            }
            for (String verifier:verifierList) {
                if(passedAddress.contains(verifier)){
                    continue;
                }
                if(Arrays.equals(AddressTool.getAddress(sign.getPublicKey(), verifierChainId), AddressTool.getAddress(verifier))){
                    passedAddress.add(verifier);
                    passCount++;
                    break;
                }
            }
        }
        if(passCount < minPassCount){
            return false;
        }
        return true;
    }


    /**
     * 从广播交易hash或签名消息的节点中获取完整跨链交易处理
     * @param chain           本链信息
     * @param chainId         发送链ID
     * @param cacheHash       缓存的交易Hash
     * @param isLocal         是否为链内节点广播的交易
     * */
    public static void regainCtx(Chain chain, int chainId, NulsHash cacheHash,String nativeHex,boolean isLocal) {
        if(isLocal){
            NodeType nodeType = chain.getHashNodeIdMap().get(cacheHash).remove(0);
            if (chain.getHashNodeIdMap().get(cacheHash).isEmpty()) {
                chain.getHashNodeIdMap().remove(cacheHash);
            }
            GetCtxMessage responseMessage = new GetCtxMessage();
            responseMessage.setRequestHash(cacheHash);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeType.getNodeId(), CommandConstant.GET_CTX_MESSAGE);
            chain.getLogger().info("跨链交易处理失败，向链内节点：{}重新获取跨链交易,Hash:{}", nodeType.getNodeId(), nativeHex);
        }else{
            NodeType nodeType = chain.getOtherHashNodeIdMap().get(cacheHash).remove(0);
            if (chain.getOtherHashNodeIdMap().get(cacheHash).isEmpty()) {
                chain.getHashNodeIdMap().remove(cacheHash);
            }
            GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
            responseMessage.setRequestHash(cacheHash);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeType.getNodeId(), CommandConstant.GET_OTHER_CTX_MESSAGE);
            chain.getLogger().info("跨链交易处理失败，向其他链节点：{}重新获取跨链交易，Hash:{}", nodeType.getNodeId(), nativeHex);
        }
    }

    /**
     * 统计交易验证结果
     * @param chain        本链信息
     * @param fromChainId  发送链ID
     * @param requestHash  交易Hash
     * @return  验证是否成功
     */
    private static boolean verifyResult(Chain chain, int fromChainId, NulsHash requestHash) {
        try {
            int linkedNode = NetWorkCall.getAvailableNodeAmount(fromChainId, true);
            int verifySuccessCount = linkedNode * chain.getConfig().getByzantineRatio() / NulsCrossChainConstant.MAGIC_NUM_100;
            chain.getLogger().info("当前链接到的跨链节点数为：{}，拜占庭比例为:{},最少需要验证通过数量:{}", linkedNode, chain.getConfig().getByzantineRatio(), verifySuccessCount);
            int tryCount = 0;
            boolean validResult = false;
            while (tryCount < NulsCrossChainConstant.BYZANTINE_TRY_COUNT) {
                if (chain.getVerifyCtxResultMap().get(requestHash).size() < verifySuccessCount) {
                    Thread.sleep(2000);
                    tryCount++;
                    continue;
                }
                validResult = chain.verifyResult(requestHash, verifySuccessCount);
                if (validResult || chain.getVerifyCtxResultMap().get(requestHash).size() >= linkedNode) {
                    break;
                }
                Thread.sleep(2000);
                tryCount++;
            }
            chain.getLogger().info("跨链交易拜占庭验证完成，验证结果为：{}", validResult);
            return validResult;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        } finally {
            chain.getVerifyCtxResultMap().remove(requestHash);
        }
    }

    /**
     * 处理接收到的新交易
     *
     * @param ctx            本链协议跨链交易
     * @param chain          本链信息
     * @param originalHash   发送连协议跨链交易Hash
     * @param nativeHex      Hash字符串
     * @param originalHex    Hash字符串
     * @return               保存是否成功
     */
    private static boolean saveNewCtx(Transaction ctx, Chain chain, NulsHash originalHash, String nativeHex, String originalHex) {
        int handleChainId = chain.getChainId();
        /*
         * 主网中传输的都是主网协议的跨链交易所以不用做处理，如果是友链接收到主网发送来的跨链主网协议跨链交易需要生成对应的本链协议跨链交易
         * 如果友链收到本链节点广播的跨链交易，需要找到该交易对应的主网协议跨链交易的Hash（txData中保存）然后保存
         * */
        if (convertHashService.save(originalHash, ctx.getHash(), handleChainId)) {
            if (!ctxStatusService.save(ctx.getHash(), null, handleChainId)) {
                convertHashService.delete(originalHash, handleChainId);
                chain.getLogger().error("新跨链交易保存失败,originalHash:{},localHash:{}", originalHex, nativeHex);
                return false;
            }
        } else {
            return false;
        }
        chain.getLogger().info("新跨链交易保存成功,originalHash:{},localHash:{}", originalHex, nativeHex);
        return true;
    }

    /**
     * 验证完成的跨链交易签名并广播给链内其他节点
     * @param chain                本链信息
     * @param ctx                  本链协议跨链交易
     * @param nativeHash           本链协议跨链交易Hash
     * @param nativeHex            本链协议跨链交易Hash字符串
     * @param transactionSignature 本链协议跨链交易签名
     * @return                     签名处理是否成功
     */
    @SuppressWarnings("unchecked")
    private static boolean signCtx(Chain chain, Transaction ctx, NulsHash nativeHash, String nativeHex, TransactionSignature transactionSignature) throws NulsException, IOException {
        /*
         * 如果本地缓存有该跨链交易未广播的签名，需要把签名加入到交易的签名列表中
         * */
        if (transactionSignature.getP2PHKSignatures() == null) {
            transactionSignature.setP2PHKSignatures(new ArrayList<>());
        }
        if (chain.getWaitBroadSignMap().keySet().contains(nativeHash)) {
            for (BroadCtxSignMessage message : chain.getWaitBroadSignMap().get(nativeHash)) {
                for (P2PHKSignature sign : transactionSignature.getP2PHKSignatures()) {
                    if (Arrays.equals(message.getSignature(), sign.serialize())) {
                        break;
                    }
                }
                P2PHKSignature p2PHKSignature = new P2PHKSignature();
                p2PHKSignature.parse(message.getSignature(), 0);
                transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
            }
        }
        BroadCtxSignMessage message = new BroadCtxSignMessage();
        //判断本节点是否为共识节点，如果为共识节点则对该交易签名
        Map packerInfo = ConsensusCall.getPackerInfo(chain);
        List<String> packAddressList = (List<String>) packerInfo.get("packAddressList");
        int agentCount = packAddressList.size();
        int signCount = transactionSignature.getP2PHKSignatures().size();
        int minPassCount = agentCount * chain.getConfig().getByzantineRatio() / NulsCrossChainConstant.MAGIC_NUM_100;
        if (minPassCount == 0) {
            minPassCount = 1;
        }
        boolean isDuplicate = false;
        //判断交易签名与当前轮次共识节点出块账户是否匹配
        List<P2PHKSignature> misMatchSignList = null;
        if (signCount >= minPassCount) {
            misMatchSignList = CommonUtil.getMisMatchSigns(chain, transactionSignature, packAddressList);
            signCount -= misMatchSignList.size();
            isDuplicate = true;
            if (signCount >= minPassCount) {
                ctx.setTransactionSignature(transactionSignature.serialize());
                TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                chain.getLogger().info("跨链交易签名数量达到拜占庭比例，将该跨链交易发送给交易模块处理,hash:{}", nativeHex);
                return true;
            }
        }
        String password = (String) packerInfo.get("password");
        String address = (String) packerInfo.get("address");
        if (!StringUtils.isBlank(address)) {
            chain.getLogger().info("本节点为共识节点，对跨链交易签名,hash:{}", nativeHex);
            P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, ctx.getHash().getBytes());
            message.setSignature(p2PHKSignature.serialize());
            signCount++;
            transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
            if (signCount >= minPassCount) {
                if (!isDuplicate) {
                    misMatchSignList = CommonUtil.getMisMatchSigns(chain, transactionSignature, packAddressList);
                    signCount -= misMatchSignList.size();
                }
                if (signCount >= minPassCount) {
                    ctx.setTransactionSignature(transactionSignature.serialize());
                    TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                    chain.getLogger().info("跨链交易签名数量达到拜占庭比例，将该跨链交易发送给交易模块处理,hash:{}", nativeHex);
                }
            }
            if (misMatchSignList != null && misMatchSignList.size() > 0 && signCount < minPassCount) {
                transactionSignature.getP2PHKSignatures().addAll(misMatchSignList);
                ctx.setTransactionSignature(transactionSignature.serialize());
            }
        }
        //将收到的消息放入缓存中，等到收到交易后再广播该签名给其他节点
        if (!chain.getWaitBroadSignMap().keySet().contains(nativeHash)) {
            chain.getWaitBroadSignMap().put(nativeHash, new HashSet<>());
        }
        chain.getWaitBroadSignMap().get(nativeHash).add(message);
        return true;
    }

    /**
     * 广播签名
     * @param chain         本链信息
     * @param hash          要广播的交易hash
     * @param chainId       接收链ID
     * @param nativeHex     本链协议交易Hash
     */
    public static void broadcastCtx(Chain chain, NulsHash hash, int chainId, String nativeHex) {
        if (chain.getWaitBroadSignMap().get(hash) != null) {
            Iterator<BroadCtxSignMessage> iterator = chain.getWaitBroadSignMap().get(hash).iterator();
            Set<BroadCtxSignMessage> broadMessageSet = new HashSet<>();
            while (iterator.hasNext()) {
                BroadCtxSignMessage message = iterator.next();
                if(!broadMessageSet.contains(message)){
                    if (NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false)) {
                        iterator.remove();
                        String signStr = "";
                        if (message.getSignature() != null) {
                            signStr = HexUtil.encode(message.getSignature());
                        }
                        chain.getLogger().info("将跨链交易签名广播给链内其他节点,hash:{},sign:{}", nativeHex, signStr);
                        broadMessageSet.add(message);
                    }
                }else{
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
     * @param chain      本链信息
     * @param toChainId  接收链ID
     * @return           是否可发送跨链消息
     * */
    public static boolean canSendMessage(Chain chain,int toChainId) {
        try {
            int linkedNode = NetWorkCall.getAvailableNodeAmount(toChainId, true);
            int minNodeAmount = chain.getConfig().getMinNodeAmount();
            if(config.isMainNet()){
                for (ChainInfo chainInfo:chainManager.getRegisteredCrossChainList()) {
                    if(chainInfo.getChainId() == toChainId){
                        minNodeAmount = chainInfo.getMinAvailableNodeNum();
                    }
                }
            }
            if(linkedNode >= minNodeAmount){
                return true;
            } else {
                chain.getLogger().info("当前节点链接到的跨链节点数小于最小链接数,crossChainId:{},linkedNodeCount:{},minLinkedCount:{}", toChainId, linkedNode, minNodeAmount);
            }
        }catch (NulsException e){
            chain.getLogger().error(e);
        }
        return false;
    }
}
