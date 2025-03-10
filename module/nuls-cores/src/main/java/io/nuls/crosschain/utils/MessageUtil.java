package io.nuls.crosschain.utils;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
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
import io.nuls.crosschain.base.message.CtxFullSignMessage;
import io.nuls.crosschain.base.message.GetOtherCtxMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.base.service.ResetLocalVerifierService;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.ParamConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.NodeType;
import io.nuls.crosschain.model.bo.message.WaitBroadSignMessage;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.model.po.SendCtxHashPO;
import io.nuls.crosschain.rpc.call.ConsensusCall;
import io.nuls.crosschain.rpc.call.NetWorkCall;
import io.nuls.crosschain.rpc.call.TransactionCall;
import io.nuls.crosschain.srorage.ConvertCtxService;
import io.nuls.crosschain.srorage.ConvertHashService;
import io.nuls.crosschain.srorage.CtxStatusService;
import io.nuls.crosschain.srorage.SendHeightService;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.io.IOException;
import java.util.*;

/**
 * Message tool class
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
    private static NulsCoresConfig config;

    @Autowired
    private static ChainManager chainManager;

    @Autowired
    private static ConvertCtxService convertCtxService;

    @Autowired
    private static CtxStatusService ctxStatusService;

    @Autowired
    private static SendHeightService sendHeightService;

    @Autowired
    private static ResetLocalVerifierService resetLocalVerifierService;

    /**
     * Process transactions broadcasted on this chain
     *
     * @param chain   This chain information
     * @param hash    Transaction cacheHASH
     * @param chainId Sending ChainID
     * @param nodeId  Sending nodeID
     * @param hashHex transactionHashcharacter string（Used for log printing）
     */
    public static void handleSignMessage(Chain chain, NulsHash hash, int chainId, String nodeId, BroadCtxSignMessage messageBody, String hashHex) {
        try {
            int handleChainId = chain.getChainId();
            CtxStatusPO ctxStatusPO = ctxStatusService.get(hash, handleChainId);
            //If the transaction has been confirmed at this node, there is no need for further signature processing
            if (ctxStatusPO.getStatus() != TxStatusEnum.UNCONFIRM.getStatus() || messageBody.getSignature() == null) {
                chain.getLogger().info("Cross chain transactions have been processed at this node,Hash:{}\n\n", hashHex);
                return;
            }
            String signHex = HexUtil.encode(messageBody.getSignature());
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            p2PHKSignature.parse(messageBody.getSignature(), 0);
            Transaction convertCtx = ctxStatusPO.getTx();

            if (config.getCrossTxDropTime() > convertCtx.getTime()) {
                chain.getLogger().warn("The cross-chain transaction has expired and is no longer processed");
                return;
            }

            if (!config.isMainNet() && convertCtx.getType() == config.getCrossCtxType()) {
                convertCtx = convertCtxService.get(hash, handleChainId);
            }
            //Verify if the signature is correct. If it is a cross chain transfer transaction, the signature corresponds to the main network protocol signature
            if (!ECKey.verify(convertCtx.getHash().getBytes(), p2PHKSignature.getSignData().getSignBytes(), p2PHKSignature.getPublicKey())) {
                chain.getLogger().info("Signature verification error,hash:{},autograph:{}\n\n", hashHex, signHex);
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
     * Process transactions broadcasted on other chains
     *
     * @param chain     This chain information
     * @param cacheHash Transaction cacheHASH
     * @param chainId   Sending ChainID
     * @param nodeId    Sending nodeID
     * @param hashHex   transactionHashcharacter string（Used for log printing）
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
            chain.getLogger().info("Get transaction timeout, send to cross chain nodes{}Retrieve complete cross chain transactions again,Hash:{}", nodeId, hashHex);
        } else {
            chain.getOtherHashNodeIdMap().putIfAbsent(cacheHash, new ArrayList<>());
            chain.getOtherHashNodeIdMap().get(cacheHash).add(new NodeType(nodeId, 1));
        }
        chain.getLogger().debug("Cross chain nodes{}Cross chain transactions broadcasted overHashOr signature message processing completed,Hash：{}\n\n", nodeId, hashHex);
    }

    /**
     * Transaction Signature Byzantine Processing
     *
     * @param chain       This chain information
     * @param chainId     Sending ChainID
     * @param realHash    Cross chain transactions under this chain protocolHash
     * @param ctx         Cross chain transactions
     * @param messageBody news
     * @param nativeHex   transactionHashcharacter string
     * @param signHex     Transaction signature string
     */
    @SuppressWarnings("unchecked")
    public static void signByzantine(Chain chain, int chainId, NulsHash realHash, Transaction ctx, BroadCtxSignMessage messageBody, String nativeHex, String signHex, String excludeNodes) throws NulsException, IOException {
        //Check if the node has received and broadcasted the signature. If it has already been broadcasted, there is no need to broadcast it again
        TransactionSignature signature = new TransactionSignature();
        if (ctx.getTransactionSignature() != null) {
            signature.parse(ctx.getTransactionSignature(), 0);
            for (P2PHKSignature sign : signature.getP2PHKSignatures()) {
                if (Arrays.equals(messageBody.getSignature(), sign.serialize())) {
                    chain.getLogger().debug("This node has already received the signature for the cross chain transaction,Hash:{},autograph:{}\n\n", nativeHex, signHex);
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
        //Transaction Signature Byzantine
        List<String> packAddressList;
        //Byzantine signature saturation increase 0To avoid floating upwards
        Float signCountOverflow = 0F;
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            String txHash = realHash.toHex();
            //This is a special initialization validator transaction, where the user resets the main network validator list stored on the parallel chain
            if (resetLocalVerifierService.isResetOtherVerifierTx(txHash)) {
                packAddressList = chain.getVerifierList();
                //1To float up to all
                signCountOverflow = 1F;
            } else {
                packAddressList = (List<String>) ConsensusCall.getSeedNodeList(chain).get(ParamConstant.PARAM_PACK_ADDRESS_LIST);
            }
        } else {
            packAddressList = chain.getVerifierList();
        }
        boolean byzantineSignIsDone = signByzantineInChain(chain, ctx, signature, packAddressList, realHash, signCountOverflow);
        if (byzantineSignIsDone) {
            chain.getLogger().debug("The local Byzantine signature collection is complete and the signature is passed back to the node that sent the signature,to node:{}", excludeNodes);
            CtxFullSignMessage ctxFullSignMessage = new CtxFullSignMessage();
            ctxFullSignMessage.setLocalTxHash(realHash);
            ctxFullSignMessage.setTransactionSignature(ctx.getTransactionSignature());
            NetWorkCall.sendToNode(chainId, ctxFullSignMessage, excludeNodes, CommandConstant.CROSS_CTX_FULL_SIGN_MESSAGE);
        }
        NetWorkCall.broadcast(chainId, messageBody, excludeNodes, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
        chain.getLogger().info("Broadcast newly received cross chain transaction signatures to other nodes linked to them,Hash:{} ,autograph:{} ,node: {}", nativeHex, signHex, excludeNodes);
    }

    /**
     * Transaction signature Byzantine verification
     *
     * @param chain           This chain information
     * @param ctx             Cross chain transactions
     * @param signature       Signature List
     * @param packAddressList Verify account list
     * @return Byzantine verification passed
     */
    public static boolean signByzantineInChain(
            Chain chain,
            Transaction ctx,
            TransactionSignature signature,
            List<String> packAddressList,
            NulsHash realHash,
            Float signCountOverflow) throws NulsException, IOException {
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            return verifierInitLocalByzantine(chain, ctx, signature, packAddressList, realHash, signCountOverflow);
        } else if (ctx.getType() == TxType.VERIFIER_CHANGE) {
            return verifierChangeLocalByzantine(chain, ctx, signature, realHash);
        } else {
            return crossTransferLocalByzantine(chain, ctx, signature, realHash);
        }
    }

    /**
     * Transaction signature Byzantine verification
     *
     * @param chain           This chain information
     * @param ctx             Cross chain transactions
     * @param signature       Signature List
     * @param packAddressList Verify account list
     * @return Byzantine verification passed
     */
    public static boolean signByzantineInChain(
            Chain chain,
            Transaction ctx,
            TransactionSignature signature,
            List<String> packAddressList,
            NulsHash realHash) throws NulsException, IOException {
        return signByzantineInChain(chain, ctx, signature, packAddressList, realHash, 0F);
    }

    /**
     * Verifier initializes transaction local Byzantine signature
     *
     * @param chain
     * @param ctx
     * @param signature
     * @param packAddressList
     * @param realHash
     * @param signCountOverflow The amplitude of saturation signature float 0.3 Increase the number of signatures after reaching the minimum number（The number of floats up is equal to The percentage after subtracting the minimum number of signatures from the total number of signatures）
     *                          Example： Total number of signatures100Minimum number of signatures60Upward floating 0.3 It's equivalent to （100 - 60）* 0.3 = 12 Then the number of saturated signatures is 72.
     *                          When the number of signatures reaches60Afterwards, other chain broadcast transactions will be considered, and when the number of signatures reaches72Afterwards, stop processing signatures.
     * @return
     * @throws IOException
     */
    public static boolean verifierInitLocalByzantine(
            Chain chain,
            Transaction ctx,
            TransactionSignature signature,
            List<String> packAddressList,
            NulsHash realHash,
            Float signCountOverflow) throws IOException {
        List<String> handleAddressList = new ArrayList<>(packAddressList);
        int agentCount = handleAddressList.size();
        //Transaction Signature Byzantine
        int byzantineCount = CommonUtil.getByzantineCount(chain, agentCount);
        int signCount = signature.getSignersCount();
        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        if (signCount >= byzantineCount) {
            //Remove signatures and duplicate signatures that are not the current verifier
            List<P2PHKSignature> misMatchSignList = CommonUtil.getMisMatchSigns(chain, signature, handleAddressList);
            signCount = signature.getSignersCount();
            if (signCount >= byzantineCount) {
                ctx.setTransactionSignature(signature.serialize());
                long sendHeight = config.getSendHeight();
                if (chainManager.getChainHeaderMap().get(chain.getChainId()) != null) {
                    sendHeight = chainManager.getChainHeaderMap().get(chain.getChainId()).getHeight();
                }
                saveCtxSendHeight(chain, sendHeight, ctx);
                chain.getLogger().info("Initial verifier transaction signature Byzantine verification passed,Save the height change of the verifier and wait for broadcast,Hash{},Broadcasting height{}", ctx.getHash().toHex(), sendHeight);
                if (signCountOverflow == null) {
                    signCountOverflow = 0F;
                }
                int fullByzantineCount = byzantineCount + (int) ((agentCount - byzantineCount) * signCountOverflow);
                if (signCount >= fullByzantineCount) {
                    chain.getLogger().info("The number of initial verifier transaction signatures has reached saturation:{},ctxSet toCONFIRMEDStatus, this node will no longer process this transaction", signCount);
                    ctxStatusPO.setStatus(TxStatusEnum.CONFIRMED.getStatus());
                    resetLocalVerifierService.finishResetOtherVerifierTx(realHash.toHex());
                } else {
                    chain.getLogger().debug("The number of initial verifier transaction signatures has reached the minimum number of signatures:{}, but to reach saturation signature count:{}This section will continue to process this transaction", signCount, fullByzantineCount);
                }
                ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
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
        //Transaction Signature Byzantine
        int byzantineCount = CommonUtil.getByzantineCount(chain, agentCount);
        int signCount = signature.getSignersCount();
        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        if (signCount >= byzantineCount) {
            //Remove signatures and duplicate signatures that are not the current verifier
            List<P2PHKSignature> misMatchSignList = CommonUtil.getMisMatchSigns(chain, signature, handleAddressList);
            signCount = signature.getSignersCount();
            if (signCount >= byzantineCount) {
                ctx.setTransactionSignature(signature.serialize());
                ctxStatusPO.setStatus(TxStatusEnum.BYZANTINE_COMPLETE.getStatus());
                ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
                TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
                chain.getLogger().info("Verifier changes transaction signature Byzantine verification passed,Broadcast cross chain transactions to the transaction module for processing,Hash{}", ctx.getHash().toHex());
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
     * Cross chain transactions
     *
     * @param chain
     * @param ctx
     * @param signature
     * @param realHash
     * @return
     * @throws NulsException
     * @throws IOException
     */
    private static boolean crossTransferLocalByzantine(
            Chain chain,
            Transaction ctx,
            TransactionSignature signature,
            NulsHash realHash) throws NulsException, IOException {
        List<String> handleAddressList;
        BlockHeader blockheader = chainManager.getChainHeaderMap().get(chain.getChainId());
        if (null == blockheader) {
            chain.getLogger().info("ChainHeaderMap get failed : {} -of- {}", chain.getChainId(), chainManager.getChainHeaderMap().size());
        }
        long broadHeight = blockheader.getHeight();
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
        //Transaction Signature Byzantine
        int byzantineCount = CommonUtil.getByzantineCount(chain, agentCount);
        int signCount = signature.getSignersCount();
        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        if (signCount >= byzantineCount) {
            //Remove signatures and duplicate signatures that are not the current verifier
            List<P2PHKSignature> misMatchSignList = CommonUtil.getMisMatchSigns(chain, signature, handleAddressList);
            signCount = signature.getSignersCount();
            if (signCount >= byzantineCount) {
                ctx.setTransactionSignature(signature.serialize());
                saveCtxSendHeight(chain, broadHeight, ctx);
                chain.getLogger().info("Cross chain transaction completed by Byzantium, placed in the waiting queue for packaging, waiting for broadcast,Hash:{},sendHeight:{},txType:{}", ctx.getHash().toHex(), broadHeight, ctx.getType());
                //Number of saturated signatures, floating up from the minimum number of signatures5%
                float overflow = (agentCount - byzantineCount) * .05F;
                int fullByzantineCount = byzantineCount + (int) (Math.ceil(overflow));
                if (fullByzantineCount > agentCount) {
                    fullByzantineCount = agentCount;
                }
                if (signCount >= fullByzantineCount) {
                    chain.getLogger().info("Cross chain transaction signature count reaches saturation signature count:{},ctxSet toCONFIRMEDStatus, this node will no longer process this transaction", signCount);
                    ctxStatusPO.setStatus(TxStatusEnum.CONFIRMED.getStatus());
                } else {
                    chain.getLogger().debug("Cross chain transaction signatures have reached the minimum number of signatures:{}, but to reach saturation signature count:{}This section will continue to process this transaction", signCount, fullByzantineCount);
                }
                ctxStatusService.save(realHash, ctxStatusPO, chain.getChainId());
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
     * Handle cross chain transactions broadcasted by other chain node nodes received
     *
     * @param chain       This chain information
     * @param ctx         Cross chain transactions
     * @param fromChainId Chain for sending transactionsID
     */
    public static boolean handleOtherChainCtx(Transaction ctx, Chain chain, int fromChainId) {
        NulsHash ctxHash = ctx.getHash();
        try {
            if (ctx.getType() == TxType.REGISTERED_CHAIN_CHANGE && config.isMainNet()) {
                return false;
            }
            TransactionSignature signature = new TransactionSignature();
            signature.parse(ctx.getTransactionSignature(), 0);
            int verifierChainId = fromChainId;
            if (!config.isMainNet()) {
                verifierChainId = config.getMainChainId();
            }
            //Transactions transmitted across chains are all main network protocol transactions
            if (ctx.getType() == TxType.CROSS_CHAIN || ctx.getType() == TxType.CONTRACT_TOKEN_CROSS_TRANSFER) {
                if (!handleOtherChainCrossTransferTx(chain, ctx, signature, verifierChainId)) {
                    return false;
                }
            } else if (ctx.getType() == TxType.VERIFIER_CHANGE) {
                if (!handleOtherChainVerifierChangeTx(chain, ctx, signature, verifierChainId)) {
                    return false;
                }
            } else if (ctx.getType() == TxType.VERIFIER_INIT) {
                if (!handleOtherChainVerifierInitTx(chain, ctx, signature, verifierChainId)) {
                    return false;
                }
            } else {
                if (!handleOtherChainCrossTx(chain, ctx, signature, verifierChainId)) {
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
                chain.getLogger().error("Chain not registered,chainId:{}", verifierChainId);
                return false;
            }
            verifierList = chainInfo.getVerifierList();
            minPassCount = chainInfo.getMinPassCount();
        }
        try {
            if (!otherCtxSignValidate(chain, ctx, signature, verifierChainId, verifierList, minPassCount)) {
                chain.getLogger().error("Verifier initialization transaction signature Byzantine verification failed,hash:{}", ctx.getHash().toHex());
                return false;
            }
            TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
            chain.getLogger().debug("The receiving chain initialization validator completes the transaction verification and sends it to the transaction module for processing,hash:{}", ctx.getHash().toHex());
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    private static boolean handleOtherChainVerifierChangeTx(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId) {
        ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
        if (chainInfo == null) {
            chain.getLogger().error("Chain not registered,chainId:{}", verifierChainId);
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
            //The maximum number of exits for a validator change transaction30%Verified by
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
                chain.getLogger().error("Verifier changed transaction signature Byzantine verification failed,hash:{}", ctx.getHash().toHex());
                return false;
            }
            TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
            chain.getLogger().debug("Verifier change transaction verification completed, sent to the transaction module for processing,hash:{}", ctx.getHash().toHex());
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    private static boolean handleOtherChainCrossTransferTx(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId) {
        ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
        if (chainInfo == null) {
            chain.getLogger().error("Chain not registered,chainId:{}", verifierChainId);
            return false;
        }
        Set<String> verifierList = chainInfo.getVerifierList();
        int minPassCount = chainInfo.getMinPassCount();
        try {
            if (!otherCtxSignValidate(chain, ctx, signature, verifierChainId, verifierList, minPassCount)) {
                chain.getLogger().error("Cross chain transfer transaction signature Byzantine verification failed,hash:{}", ctx.getHash().toHex());
                return false;
            }
            CoinData coinData = ctx.getCoinDataInstance();
            int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
            Transaction packCtx = ctx;
            String crossTxHashHex = ctx.getHash().toHex();
            //If this chain is a receiving chain, directly send the transaction module for packaging
            if (chain.getChainId() == toChainId) {
                if (!config.isMainNet()) {
                    packCtx = TxUtil.mainConvertToFriend(ctx, config.getCrossCtxType());
                    packCtx.setTransactionSignature(signature.serialize());
                    convertCtxService.save(packCtx.getHash(), ctx, chain.getChainId());
                    chain.getLogger().info("Received main network protocol cross chain transactionshash：{}Corresponding cross chain transactions of this chain protocolhash:{}", crossTxHashHex, packCtx.getHash().toHex());
                }
            } else {
                if (!config.isMainNet()) {
                    chain.getLogger().error("Cross chain transaction verification failed,hash:{}", crossTxHashHex);
                    return false;
                }
            }
            TransactionCall.sendTx(chain, RPCUtil.encode(packCtx.serialize()));
            chain.getLogger().info("The cross chain transfer transaction verification is completed and sent to the transaction module for processing,hash:{}", crossTxHashHex);
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    private static boolean handleOtherChainCrossTx(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId) {
        ChainInfo chainInfo = chainManager.getChainInfo(verifierChainId);
        if (chainInfo == null) {
            chain.getLogger().error("Chain not registered,chainId:{}", verifierChainId);
            return false;
        }
        Set<String> verifierList = chainInfo.getVerifierList();
        int minPassCount = chainInfo.getMinPassCount();
        try {
            String crossTxHashHex = ctx.getHash().toHex();
            if (!otherCtxSignValidate(chain, ctx, signature, verifierChainId, verifierList, minPassCount)) {
                chain.getLogger().error("Cross chain transaction verification failed for other broadcasts,hash:{},txType:{}", crossTxHashHex, ctx.getType());
                return false;
            }
            TransactionCall.sendTx(chain, RPCUtil.encode(ctx.serialize()));
            chain.getLogger().debug("The cross chain transaction verification for other broadcasts is completed and sent to the transaction module for processing,hash:{},txType:{}", crossTxHashHex, ctx.getType());
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
            return false;
        }
        return true;
    }

    /**
     * Cross chain transaction signature verification for other chain protocols
     * Signature Verification of Cross-Chain Transactions in Other Chain Protocols
     *
     * @param chain           Current Chain Information
     * @param signature       Transaction signature
     * @param ctx             transaction
     * @param verifierChainId Sending ChainID
     * @param verifierList    Verifier List
     * @param minPassCount    Minimum number of signatures
     */
    private static boolean otherCtxSignValidate(Chain chain, Transaction ctx, TransactionSignature signature, int verifierChainId, Set<String> verifierList, int minPassCount) throws NulsException {
        if (verifierList == null || verifierList.isEmpty()) {
            chain.getLogger().error("The chain has not registered a verifier yet,chainId:{}", verifierChainId);
            return false;
        }
        int passCount = 0;
        List<P2PHKSignature> signatureList = signature.getP2PHKSignatures();
        if (signatureList == null || signatureList.size() < minPassCount) {
            chain.getLogger().error("The number of cross chain transaction signatures is less than the minimum number of Byzantine verifications,signCount{},minPassCount{}", signatureList == null ? 0 : signatureList.size(), minPassCount);
            try {
                chain.getLogger().error(HexUtil.encode(ctx.serialize()));
            } catch (IOException e) {
            }
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
            chain.getLogger().error("The number of signature verifications passed is less than the minimum number of Byzantine verifications,passCount:{},minPassCount:{}", passCount, minPassCount);
            return false;
        }
        chain.getLogger().info("Signature verification passed,passCount:{},minPassCount:{}, of: {}", passCount, minPassCount, ctx.getHash().toHex());
        return true;
    }


    /**
     * Broadcast signature
     *
     * @param chain     This chain information
     * @param hash      Transactions to be broadcastedhash
     * @param chainId   Receiving ChainID
     * @param nativeHex This chain protocol transactionHash
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
                        chain.getLogger().info("Broadcast cross chain transaction signatures to other nodes in the chain,hash:{},sign:{}", nativeHex, signStr);
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
     * Can cross chain related messages be sent
     *
     * @param chain     This chain information
     * @param toChainId Receiving ChainID
     * @return Current cross chain network status 0Chain has been deregistered1Cannot be broadcasted2Broadcastable
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
                chain.getLogger().debug("The number of cross chain nodes that the current node is linked to is less than the minimum number of links,crossChainId:{},linkedNodeCount:{},minLinkedCount:{}", toChainId, linkedNode, minNodeAmount);
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
