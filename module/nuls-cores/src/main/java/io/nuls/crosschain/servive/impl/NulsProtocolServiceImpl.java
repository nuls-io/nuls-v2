package io.nuls.crosschain.servive.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
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
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.CtxStateEnum;
import io.nuls.crosschain.model.bo.message.UntreatedMessage;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.rpc.call.LedgerCall;
import io.nuls.crosschain.rpc.call.NetWorkCall;
import io.nuls.crosschain.srorage.*;
import io.nuls.crosschain.utils.CommonUtil;
import io.nuls.crosschain.utils.TxUtil;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Cross chain module protocol processing implementation class
 *
 * @author tag
 * @date 2019/4/9
 */
@Component
public class NulsProtocolServiceImpl implements ProtocolService {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private NulsCoresConfig config;
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
     * Query transaction processing status
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
        chain.getLogger().info("Received node{}The query cross chain transaction processing result information sent over,transactionHash:{}", nodeId, hashHex);
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
            chain.getLogger().info("The result of cross chain transaction processing is：{}(0:Confirming,1:The main network has been confirmed,2:Confirm completion),Hash:{}\n\n", responseMessage.getHandleResult(),hashHex);
        }catch(NulsException e){
            chain.getLogger().error(e);
        }
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.CTX_STATE_MESSAGE);
    }

    @Override
    /**
     * Obtain the processing status of cross chain transactions sent by other chains
     * */
    public void receiveCtxState(int chainId, String nodeId, CtxStateMessage messageBody) {
        //Place the returned result in the specified cache and wait for other threads to process it
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getLogger().debug("Received node{}Transaction processing result message sent, transactionhash:{},Processing results:{}\n\n", nodeId, messageBody.getRequestHash().toHex(), messageBody.getHandleResult());
        NulsHash requestHash = messageBody.getRequestHash();
        if (!chain.getCtxStateMap().keySet().contains(requestHash)) {
            chain.getCtxStateMap().put(requestHash, new ArrayList<>());
        }
        chain.getCtxStateMap().get(messageBody.getRequestHash()).add(messageBody.getHandleResult());
    }

    @Override
    /**
     * Receive cross chain transactions broadcasted by nodes within the chainHashAnd signature
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
        chain.getLogger().debug("Received in chain node{}Cross chain transactions broadcasted overHashAnd signature,Hash:{},autograph:{}", nodeId, nativeHex, signHex);
        //If the transaction is received for the first time, obtain the complete cross chain transaction from the broadcast node
        CtxStatusPO ctxStatusPO = ctxStatusService.get(localHash, handleChainId);
        if (ctxStatusPO == null) {
            UntreatedMessage untreatedSignMessage = new UntreatedMessage(chainId,nodeId,messageBody,localHash);
            chain.getFutureMessageMap().putIfAbsent(localHash, new ArrayList<>());
            chain.getFutureMessageMap().get(localHash).add(untreatedSignMessage);
            //TODO pierre test
            chain.getLogger().debug("The current node has not yet confirmed the cross chain transaction, caching signature messages");
            return;
        }
        //If the transaction has been confirmed at this node, there is no need for further signature processing
        if (ctxStatusPO.getStatus() != TxStatusEnum.UNCONFIRM.getStatus() || messageBody.getSignature() == null) {
            chain.getLogger().debug("Cross chain transactions have been processed at this node,Hash:{}\n\n", nativeHex);
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
     * Receive cross chain transactions broadcasted from other chainsHash
     * */
    public void receiveCtxHash(int chainId, String nodeId, BroadCtxHashMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        //Cross chain transmission involves main network protocol transactionsHASH
        NulsHash mainHash = messageBody.getConvertHash();
        String mainHex = mainHash.toHex();
        chain.getLogger().debug("Received other chain nodes{}Cross chain transactions broadcasted over,Hash：{}", nodeId, mainHex);
        //Determine if the transaction has been received,If it has been received, it will be returned directly. If it has not been received, it will be sent to the broadcast node to obtain the complete cross chain transaction message
        if (convertHashService.get(mainHash, handleChainId) != null) {
            chain.getLogger().debug("This node has already received the cross chain transaction,Hash：{}\n\n", mainHex);
            return;
        }
        if (chain.getOtherCtxStageMap().get(mainHash) == null && chain.getOtherCtxStageMap().putIfAbsent(mainHash, NulsCrossChainConstant.CTX_STAGE_WAIT_RECEIVE) == null) {
            chain.getLogger().info("First time receiving cross chain transactionHashBroadcast information,Hash:{}", mainHex);
            GetOtherCtxMessage responseMessage = new GetOtherCtxMessage();
            responseMessage.setRequestHash(mainHash);
            NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.GET_OTHER_CTX_MESSAGE);
            chain.getLogger().info("To send chain nodes{}Obtain complete cross chain transactions,Hash:{}", nodeId, mainHex);
        } else {
            UntreatedMessage untreatedSignMessage = new UntreatedMessage(chainId,nodeId,messageBody,mainHash);
            chain.getHashMessageQueue().offer(untreatedSignMessage);
        }
        chain.getLogger().debug("Cross chain transactions of other chain broadcastsHashMessage reception completed,Hash：{}\n\n", mainHex);
    }

    @Override
    /**
     * Other chain nodes request complete cross chain transactions from the current node
     * */
    public void getOtherCtx(int chainId, String nodeId, GetOtherCtxMessage messageBody) {
        NewOtherCtxMessage responseMessage = new NewOtherCtxMessage();
        //All transactions transmitted in the main network are based on the main network protocolHASH
        NulsHash mainHash = messageBody.getRequestHash();
        responseMessage.setRequestHash(mainHash);
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        String nativeHex = messageBody.getRequestHash().toHex();
        chain.getLogger().info("Received request chain node{}Obtaining complete cross chain transaction information sent,Hash:{}", nodeId, nativeHex);
        //Found corresponding cross chain transactions
        NulsHash localHash = mainHash;
        if(!config.isMainNet()){
            localHash = convertHashService.get(mainHash,handleChainId);
        }
        CtxStatusPO ctxStatusPO = ctxStatusService.get(localHash, handleChainId);
        if(ctxStatusPO == null){
            chain.getLogger().error("Transaction does not exist,hash:{}",nativeHex);
            return;
        }
        if (ctxStatusPO.getTx().getType() != TxType.VERIFIER_INIT) {
            List<String> packAddressList = chain.getVerifierList();
            int byzantineCount = CommonUtil.getByzantineCount(chain, packAddressList.size());
            TransactionSignature transactionSignature = new TransactionSignature();
            try {
                transactionSignature.parse(ctxStatusPO.getTx().getTransactionSignature(),0);
            } catch (NulsException e) {
                Log.error("Failed to parse transaction signature");
                return;
            }
            if(transactionSignature.getP2PHKSignatures().size() < byzantineCount && ctxStatusPO.getStatus() == TxStatusEnum.UNCONFIRM.getStatus()){
                chain.getLogger().info("The cross chain transaction obtained has not been confirmed at this node,hash:{}",nativeHex);
                return;
            }
        }
        Transaction localCtx = ctxStatusPO.getTx();
        /*
        If the main network obtains complete cross chain transactions from the friend chain, it is necessary to convert the cross chain transactions of this chain protocol into corresponding main network protocol cross chain transactions and return them to the main network node
        */
        if(!config.isMainNet() && localCtx.getType() == config.getCrossCtxType()){
            try {
                localCtx = TxUtil.friendConvertToMain(chain, localCtx, TxType.CROSS_CHAIN, true);
            }catch (Exception e){
                chain.getLogger().error("Error converting cross chain transaction of this chain protocol to cross chain transaction of main network protocol,Hash:{}\n\n",nativeHex);
                chain.getLogger().error(e);
                return;
            }
        }
        responseMessage.setCtx(localCtx);
        //Send the complete cross chain transaction to the requesting node
        NetWorkCall.sendToNode(chainId, responseMessage, nodeId, CommandConstant.NEW_OTHER_CTX_MESSAGE);
        chain.getLogger().info("Send the complete cross chain transaction to the requesting connection node{},Hash:{}\n\n", nodeId, nativeHex);
    }

    @Override
    /**
     * Receive cross chain transactions sent by other chain nodes
     * */
    public void receiveOtherCtx(int chainId, String nodeId, NewOtherCtxMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        NulsHash ctxHash = messageBody.getCtx().getHash();
        String ctxHashHex = ctxHash.toHex();
        chain.getLogger().info("Received other chain nodes{}The complete cross chain transaction sent over,Hash:{},Transaction type：{}", nodeId, ctxHashHex, messageBody.getCtx().getType());
        //Determine whether this node has received the cross chain transaction. If it has, ignore it directly
        if (convertHashService.get(ctxHash, handleChainId) != null) {
            chain.getLogger().debug("This node has received and processed the cross chain transaction,Hash:{}\n\n", ctxHashHex);
            return;
        }
        /*
         * Modify the cross chain transaction status to Received, Processing
         * */
        if (NulsCrossChainConstant.CTX_STATE_PROCESSING.equals(chain.getOtherCtxStageMap().put(ctxHash, NulsCrossChainConstant.CTX_STATE_PROCESSING))) {
            chain.getLogger().debug("The cross chain transaction is currently being processed,Hash:{}\n\n", ctxHashHex);
            return;
        }
        UntreatedMessage untreatedCtxMessage = new UntreatedMessage(chainId,nodeId,messageBody,ctxHash);
        chain.getOtherCtxMessageQueue().offer(untreatedCtxMessage);
        chain.getLogger().debug("Other chain nodes{}The complete cross chain transaction message sent has been received,Hash:{}", nodeId,ctxHashHex);
    }

    @Override
    public void getCirculation(int chainId, String nodeId, GetCirculationMessage messageBody) {
        int handleChainId = chainId;
        if (config.isMainNet()) {
            handleChainId = config.getMainChainId();
        }
        Chain chain = chainManager.getChainMap().get(handleChainId);
        chain.getLogger().info("Main network node{}This node queries the circulation of assets in this chain,Assets queriedIDby：{}\n\n", nodeId, messageBody.getAssetIds());
        //Calling the ledger module interface to obtain the circulation volume of queried assets
        CirculationMessage message = new CirculationMessage();
        try {
            List<Circulation> circulationList = LedgerCall.getAssetsById(chain, messageBody.getAssetIds());
            message.setCirculationList(circulationList);
            //Return the result to the requesting node
            NetWorkCall.sendToNode(chainId, message, nodeId, CommandConstant.CIRCULATION_MESSAGE);
        } catch (NulsException e) {
            chain.getLogger().error(e);
        }
    }
}
