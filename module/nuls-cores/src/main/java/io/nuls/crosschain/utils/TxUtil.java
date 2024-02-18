package io.nuls.crosschain.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.message.GetCtxStateMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.CrossTransferData;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainChangeData;
import io.nuls.crosschain.base.model.bo.txdata.VerifierChangeData;
import io.nuls.crosschain.base.model.bo.txdata.VerifierInitData;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.ParamConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.bo.CtxStateEnum;
import io.nuls.crosschain.model.bo.message.WaitBroadSignMessage;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.rpc.call.AccountCall;
import io.nuls.crosschain.rpc.call.ConsensusCall;
import io.nuls.crosschain.rpc.call.NetWorkCall;
import io.nuls.crosschain.srorage.ConvertCtxService;
import io.nuls.crosschain.srorage.ConvertHashService;
import io.nuls.crosschain.srorage.CtxStateService;
import io.nuls.crosschain.srorage.CtxStatusService;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Trading tools
 * Transaction Tool Class
 *
 * @author tag
 * 2019/4/15
 */
@Component
public class TxUtil {
    @Autowired
    private static NulsCoresConfig config;
    @Autowired
    private static ConvertCtxService convertCtxService;
    @Autowired
    private static CtxStatusService ctxStatusService;
    @Autowired
    private static ConvertHashService convertHashService;
    @Autowired
    private static CtxStateService ctxStateService;
    @Autowired
    private static ChainManager chainManager;

    /**
     * Friendly chain protocol cross chain transaction to main network protocol cross chain transaction
     * Friendly Chain Protocol Cross-Chain Transaction to Main Network Protocol Cross-Chain Transaction
     */
    public static Transaction friendConvertToMain(Chain chain, Transaction friendCtx, int ctxType) throws NulsException, IOException {
        return friendConvertToMain(chain,friendCtx,ctxType,false);
    }

    /**
     * Friendly chain protocol cross chain transaction to main network protocol cross chain transaction
     * Friendly Chain Protocol Cross-Chain Transaction to Main Network Protocol Cross-Chain Transaction
     */
    public static Transaction friendConvertToMain(Chain chain, Transaction friendCtx, int ctxType, boolean needSign) throws NulsException, IOException {
        Transaction mainCtx = new Transaction(ctxType);
        mainCtx.setRemark(friendCtx.getRemark());
        mainCtx.setTime(friendCtx.getTime());
        mainCtx.setTxData(friendCtx.getTxData());
        //Restore and RechargeCoinData
        CoinData realCoinData = friendCtx.getCoinDataInstance();
        restoreCoinData(realCoinData);
        mainCtx.setCoinData(realCoinData.serialize());
        int fromChainId = AddressTool.getChainIdByAddress(realCoinData.getFrom().get(0).getAddress());
        //If it is the initiating chain, it needs to be refactoredtxDataInitiate chain transactionshashSet totxDatain
        if(chain.getChainId() == fromChainId){
            CrossTransferData crossTransferData = new CrossTransferData();
            crossTransferData.parse(friendCtx.getTxData(),0);
            crossTransferData.setSourceHash(friendCtx.getHash().getBytes());
            mainCtx.setTxData(crossTransferData.serialize());
        }else{
            mainCtx.setTxData(friendCtx.getTxData());
        }
        if(needSign){
            mainCtx.setTransactionSignature(friendCtx.getTransactionSignature());
        }
        /*
        //If creating a new cross chain transaction, directly sign with account information; otherwise, obtain the signature from the original signature
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        int fromChainId = AddressTool.getChainIdByAddress(realCoinData.getFrom().get(0).getAddress());
        if (fromChainId == chain.getChainId()) {
            TransactionSignature originalSignature = new TransactionSignature();
            originalSignature.parse(friendCtx.getTransactionSignature(), 0);
            int signCount = realCoinData.getFromAddressCount();
            int size = originalSignature.getP2PHKSignatures().size();
            for (int index = signCount; index < size; index++) {
                p2PHKSignatures.add(originalSignature.getP2PHKSignatures().get(index));
            }
            if (!p2PHKSignatures.isEmpty()) {
                transactionSignature.setP2PHKSignatures(p2PHKSignatures);
                mainCtx.setTransactionSignature(transactionSignature.serialize());
            }
        } else {
            mainCtx.setTransactionSignature(friendCtx.getTransactionSignature());
        }*/

        chain.getLogger().debug("The cross chain transaction of this chain protocol is transferred to the main network protocol, and the cross chain transaction is completed!");
        return mainCtx;
    }

    /**
     * Main network protocol cross chain transaction to friend chain protocol cross chain transaction
     * Main Network Protocol Cross-Chain Transaction Transfer Chain Protocol Cross-Chain Transaction
     */
    public static Transaction mainConvertToFriend(Transaction mainCtx, int ctxType) {
        Transaction friendCtx = new Transaction(ctxType);
        friendCtx.setRemark(mainCtx.getRemark());
        friendCtx.setTime(mainCtx.getTime());
        friendCtx.setTxData(mainCtx.getTxData());
        friendCtx.setCoinData(mainCtx.getCoinData());
        return friendCtx;
    }

    /**
     * Assembly Verifier Change Transaction
     * Assemble Verifier Change Transaction
     */
    public static Transaction createVerifierChangeTx(List<String> registerAgentList, List<String> cancelAgentList, long time, int chainId) throws IOException {
        Transaction verifierChangeTx = new Transaction(TxType.VERIFIER_CHANGE);
        verifierChangeTx.setTime(time);
        if(registerAgentList != null){
            registerAgentList.sort(Comparator.naturalOrder());
        }
        if(cancelAgentList != null){
            cancelAgentList.sort(Comparator.naturalOrder());
        }
        VerifierChangeData verifierChangeData = new VerifierChangeData(registerAgentList, cancelAgentList, chainId);
        verifierChangeTx.setTxData(verifierChangeData.serialize());
        return verifierChangeTx;
    }

    /**
     * Assembly validator initializes transaction
     * Assemble Verifier Change Transaction
     */
    public static Transaction createVerifierInitTx(List<String> verifierList, long time, int registerChainId) throws IOException {
        Transaction verifierInitTx = new Transaction(TxType.VERIFIER_INIT);
        verifierInitTx.setTime(time);
        VerifierInitData verifierInitData = new VerifierInitData(registerChainId, verifierList);
        verifierInitTx.setTxData(verifierInitData.serialize());
        return verifierInitTx;
    }

    /**
     * Assembly registration cross chain change transaction
     * Assemble Verifier Change Transaction
     */
    public static Transaction createCrossChainChangeTx(long time, int registerChainId, int type) throws IOException {
        Transaction crossChainChangeTx = new Transaction(TxType.REGISTERED_CHAIN_CHANGE);
        crossChainChangeTx.setTime(time);
        List<ChainInfo> chainInfoList = new ArrayList<>();
        RegisteredChainChangeData txData = new RegisteredChainChangeData(registerChainId, type, chainInfoList);
        crossChainChangeTx.setTxData(txData.serialize());
        return crossChainChangeTx;
    }

    /**
     * Assembly registration cross chain change transaction
     * Assemble Verifier Change Transaction
     */
    public static Transaction createCrossChainChangeTx(ChainInfo chainInfo, long time, int registerChainId, int type) throws IOException {
        Transaction crossChainChangeTx = new Transaction(TxType.REGISTERED_CHAIN_CHANGE);
        crossChainChangeTx.setTime(time);
        List<ChainInfo> chainInfoList = new ArrayList<>();
        chainInfoList.add(chainInfo);
        RegisteredChainChangeData txData = new RegisteredChainChangeData(registerChainId, type, chainInfoList);

        crossChainChangeTx.setTxData(txData.serialize());
        return crossChainChangeTx;
    }

    /**
     * Assembly registration cross chain change transaction
     * Assemble Verifier Change Transaction
     */
    public static Transaction createCrossChainChangeTx(List<ChainInfo> chainInfoList, long time, int registerChainId, int type) throws IOException {
        Transaction crossChainChangeTx = new Transaction(TxType.REGISTERED_CHAIN_CHANGE);
        crossChainChangeTx.setTime(time);
        RegisteredChainChangeData txData = new RegisteredChainChangeData(registerChainId, type, chainInfoList);
        crossChainChangeTx.setTxData(txData.serialize());
        return crossChainChangeTx;
    }

    /**
     * Verifier change transaction processing requires waiting for high-level changes
     * When the verifier changes the transaction processing, it needs to wait for the height change
     */
    public static void verifierChangeWait(Chain chain, long height) {
        while (chainManager.getChainHeaderMap().get(chain.getChainId()).getHeight() < height - 1) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                chain.getLogger().error(e);
            }
        }
    }

    /**
     * After the cross chain transactions initiated by this chain are packaged, Byzantine verification is initiated
     * After the cross chain transaction initiated by this chain is packaged, Byzantine verification is initiated
     */
    @SuppressWarnings("unchecked")
    public static void localCtxByzantine(Transaction ctx, Chain chain) {
        int chainId = chain.getChainId();
        NulsHash hash = ctx.getHash();
        try {
            Map packerInfo = ConsensusCall.getPackerInfo(chain);
            String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
            List<String> localPackers = (List<String>) packerInfo.get(ParamConstant.PARAM_ADDRESS + "es");

            List<String> packAddressList = (List<String>) packerInfo.get(ParamConstant.PARAM_PACK_ADDRESS_LIST);


            NulsHash convertHash = hash;
            if (!config.isMainNet()) {
                //txDataIntermediate storage source chain transactionshashandnulsMain chain transactionshashIf the initiating chain isnulsMain chain, source chainhashandnulsMain chainhashSame.
                Transaction mainCtx = TxUtil.friendConvertToMain(chain, ctx, TxType.CROSS_CHAIN);
                convertHash = mainCtx.getHash();
                convertCtxService.save(hash, mainCtx, chainId);
            }
            CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
            //If this node is a consensus node, it needs to be signed and Byzantine, otherwise only the locally collected signature information needs to be broadcasted
            if (!localPackers.isEmpty()) {
                BroadCtxSignMessage message = new BroadCtxSignMessage();
                message.setLocalHash(hash);
                TransactionSignature transactionSignature = new TransactionSignature();
                if (ctx.getTransactionSignature() != null) {
                    transactionSignature.parse(ctx.getTransactionSignature(), 0);
                } else {
                    List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
                    transactionSignature.setP2PHKSignatures(p2PHKSignatures);
                }
                //Loop all local packaging addresses
                for (String packerAddress : localPackers) {
                    if (!chain.getVerifierList().contains(packerAddress)) {
                        continue;
                    }
                    if (config.isMainNet()) {
                        if (ctx.getType() == TxType.CROSS_CHAIN && ctx.getCoinDataInstance().getFromAddressList().contains(packerAddress)) {
                            message.setSignature(transactionSignature.getP2PHKSignatures().get(0).serialize());
                        } else {
                            P2PHKSignature p2PHKSignature = AccountCall.signDigest(packerAddress, password, hash.getBytes());
                            transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
                            message.setSignature(p2PHKSignature.serialize());
                        }
                    } else {
                        P2PHKSignature p2PHKSignature = AccountCall.signDigest(packerAddress, password, convertHash.getBytes());
                        transactionSignature.getP2PHKSignatures().add(p2PHKSignature);
                        message.setSignature(p2PHKSignature.serialize());
                    }
                    NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
                }
                MessageUtil.signByzantineInChain(chain, ctx, transactionSignature, packAddressList, hash);
            } else {
                ctxStatusService.save(hash, ctxStatusPO, chainId);
            }
            //Add the received signed message to the message queue
            if (chain.getFutureMessageMap().containsKey(hash)) {
                chain.getLogger().debug("Transfer this cross chain transaction:{}Received signature placed in message queue", hash.toHex());
                chain.getSignMessageByzantineQueue().addAll(chain.getFutureMessageMap().remove(hash));
            }
        } catch (NulsException | IOException e) {
            chain.getLogger().error(e);
        }
    }


    /**
     * Reset the main chain validator list for parallel chain storage
     * Cross-Chain Transaction Processing
     */
    @SuppressWarnings("unchecked")
    public static void handleResetOtherVerifierListCtx(Transaction ctx, Chain chain) {
        int chainId = chain.getChainId();
        NulsHash hash = ctx.getHash();
        String hashHex = hash.toHex();
        /*
        Determine whether this node is a consensus node. If it is a consensus node, sign it. If it is not a consensus node, broadcast the transaction
        */
        Map packerInfo;
        List<String> verifierList = chain.getVerifierList();
        packerInfo = ConsensusCall.getPackerInfo(chain);
        String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
        String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);
        BroadCtxSignMessage message = new BroadCtxSignMessage();
        message.setLocalHash(hash);
        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        boolean byzantinePass = false;
        //Verifier changes, reduced verifiers do not sign
        boolean sign = verifierList.contains(address);
        if (sign) {
            chain.getLogger().info("This node is a consensus node that signs cross chain transactions,Hash:{}", hashHex);
            P2PHKSignature p2PHKSignature;
            try {
                p2PHKSignature = AccountCall.signDigest(address, password, hash.getBytes());
                message.setSignature(p2PHKSignature.serialize());
                TransactionSignature signature = new TransactionSignature();
                List<P2PHKSignature> p2PHKSignatureList = new ArrayList<>();
                p2PHKSignatureList.add(p2PHKSignature);
                signature.setP2PHKSignatures(p2PHKSignatureList);
                ctx.setTransactionSignature(signature.serialize());
                byzantinePass = MessageUtil.verifierInitLocalByzantine(chain, ctx, signature, verifierList,hash,1F);
            } catch (Exception e) {
                chain.getLogger().error(e);
                chain.getLogger().error("Signature error!,hash:{}", hashHex);
                return;
            }
            if (!chain.getWaitBroadSignMap().keySet().contains(hash)) {
                chain.getWaitBroadSignMap().put(hash, new HashSet<>());
            }
            /*
            Save and broadcast the transaction
            */
            chain.getWaitBroadSignMap().get(hash).add(new WaitBroadSignMessage(null, message));
        }else{
            chain.getLogger().debug("This node is not a consensus node and will not sign this transaction,Hash:{}",hashHex);
            ctxStatusService.save(hash, ctxStatusPO, chainId);
        }
        if (byzantinePass) {
            chain.getFutureMessageMap().remove(hash);
        } else {
            if (chain.getFutureMessageMap().containsKey(hash)) {
                chain.getSignMessageByzantineQueue().addAll(chain.getFutureMessageMap().remove(hash));
            }
        }
        MessageUtil.broadcastCtx(chain, hash, chainId, hashHex);
    }


    /**
     * Cross chain transaction processing
     * Cross-Chain Transaction Processing
     */
    @SuppressWarnings("unchecked")
    public static void handleNewCtx(Transaction ctx, Chain chain, List<String> cancelList) {
        int chainId = chain.getChainId();
        NulsHash hash = ctx.getHash();
        String hashHex = hash.toHex();
        /*
        Determine whether this node is a consensus node. If it is a consensus node, sign it. If it is not a consensus node, broadcast the transaction
        */
        Map packerInfo = ConsensusCall.getPackerInfo(chain);
        List<String> localPackers = (List<String>) packerInfo.get(ParamConstant.PARAM_ADDRESS + "es");
        List<String> verifierList = chain.getVerifierList();
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            packerInfo = ConsensusCall.getSeedNodeList(chain);
            verifierList = (List<String>) packerInfo.get(ParamConstant.PARAM_PACK_ADDRESS_LIST);
        } else {
            packerInfo = ConsensusCall.getPackerInfo(chain);
        }
        String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
        String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);

        CtxStatusPO ctxStatusPO = new CtxStatusPO(ctx, TxStatusEnum.UNCONFIRM.getStatus());
        boolean byzantinePass = false;
        //Verifier changes, reduced verifiers do not sign
        boolean sign = !StringUtils.isBlank(address) && verifierList.contains(address);
        if (sign && cancelList != null) {
            sign = !cancelList.contains(address);
        }
        if (sign) {
            chain.getLogger().info("This node is a consensus node that signs cross chain transactions,Hash:{}", hashHex);
            TransactionSignature signature = new TransactionSignature();
            HashSet<WaitBroadSignMessage> messageList = new HashSet<>();
            List<P2PHKSignature> p2PHKSignatureList = new ArrayList<>();
            for (var packageAddress : localPackers){
                P2PHKSignature p2PHKSignature;
                try {
                    p2PHKSignature = AccountCall.signDigest(packageAddress, password, hash.getBytes());
                    BroadCtxSignMessage message = new BroadCtxSignMessage();
                    message.setLocalHash(hash);
                    message.setSignature(p2PHKSignature.serialize());
                    messageList.add(new WaitBroadSignMessage(null, message));
                    p2PHKSignatureList.add(p2PHKSignature);
                } catch (Exception e) {
                    chain.getLogger().error(e);
                    chain.getLogger().error("Signature error!,hash:{}", hashHex);
                    return;
                }
            }
            try{
                signature.setP2PHKSignatures(p2PHKSignatureList);
                ctx.setTransactionSignature(signature.serialize());
                byzantinePass = MessageUtil.signByzantineInChain(chain, ctx, signature, verifierList, hash);
            } catch (Exception e) {
                chain.getLogger().error(e);
                chain.getLogger().error("Signature error!,hash:{}", hashHex);
                return;
            }
            /*
            Save and broadcast the transaction
            */
            if (!chain.getWaitBroadSignMap().keySet().contains(hash)) {
                chain.getWaitBroadSignMap().put(hash, messageList);
            }else{
                chain.getWaitBroadSignMap().get(hash).addAll(messageList);
            }
        } else {
            ctxStatusService.save(hash, ctxStatusPO, chainId);
        }
        if (!config.isMainNet()) {
            convertHashService.save(hash, hash, chainId);
        }

        if (byzantinePass) {
            chain.getFutureMessageMap().remove(hash);
        } else {
            if (chain.getFutureMessageMap().containsKey(hash)) {
                chain.getSignMessageByzantineQueue().addAll(chain.getFutureMessageMap().remove(hash));
            }
        }
        MessageUtil.broadcastCtx(chain, hash, chainId, hashHex);
    }

    /**
     * Sign and broadcast the transaction（Cross chain transactions during synchronization are only signed and broadcasted without any other processing）
     * Sign and broadcast transactions
     *
     * @param chain Chain information
     * @param ctx   Cross chain transactions
     */
    @SuppressWarnings("unchecked")
    public static void signAndBroad(Chain chain, Transaction ctx) {
        Map packerInfo;
        List<String> verifierList = chain.getVerifierList();
        if (ctx.getType() == TxType.VERIFIER_INIT) {
            packerInfo = ConsensusCall.getSeedNodeList(chain);
            verifierList = (List<String>) packerInfo.get(ParamConstant.PARAM_PACK_ADDRESS_LIST);
        } else {
            packerInfo = ConsensusCall.getPackerInfo(chain);
        }
        String password = (String) packerInfo.get(ParamConstant.PARAM_PASSWORD);
        String address = (String) packerInfo.get(ParamConstant.PARAM_ADDRESS);
        boolean sign = !StringUtils.isBlank(address) && verifierList.contains(address);
        if (!sign) {
            return;
        }
        BroadCtxSignMessage message = new BroadCtxSignMessage();
        message.setLocalHash(ctx.getHash());
        Transaction realTx = ctx;
        try {
            if (ctx.getType() == TxType.CROSS_CHAIN) {
                //If it is not the main network, switch to the main network protocol for cross chain transactions
                if (!config.isMainNet()) {
                    realTx = TxUtil.friendConvertToMain(chain, ctx, TxType.CROSS_CHAIN);
                }
            }
            P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, realTx.getHash().getBytes());
            message.setSignature(p2PHKSignature.serialize());
            NetWorkCall.broadcast(chain.getChainId(), message, CommandConstant.BROAD_CTX_SIGN_MESSAGE, false);
        } catch (IOException | NulsException e) {
            chain.getLogger().error(e);
        }
    }


    /**
     * Query the status of cross chain transaction processing
     */
    public static byte getCtxState(Chain chain, NulsHash ctxHash) {
        int chainId = chain.getChainId();
        //Check if there is already a record of successful query processing in this transaction. If so, return it directly. Otherwise, verify with the main network node
        if (ctxStateService.get(ctxHash.getBytes(), chainId)) {
            return CtxStateEnum.CONFIRMED.getStatus();
        }
        try {
            CtxStatusPO ctxStatusPO = ctxStatusService.get(ctxHash, chainId);
            int fromChainId = AddressTool.getChainIdByAddress(ctxStatusPO.getTx().getCoinDataInstance().getFrom().get(0).getAddress());
            if (chainId == fromChainId && ctxStatusPO.getStatus() != TxStatusEnum.CONFIRMED.getStatus()) {
                return CtxStateEnum.UNCONFIRM.getStatus();
            }
            GetCtxStateMessage message = new GetCtxStateMessage();
            NulsHash requestHash = ctxHash;
            int linkedChainId = chainId;
            if (!config.isMainNet()) {
                requestHash = friendConvertToMain(chain, ctxStatusPO.getTx(), TxType.CROSS_CHAIN).getHash();
            } else {
                linkedChainId = AddressTool.getChainIdByAddress(ctxStatusPO.getTx().getCoinDataInstance().getTo().get(0).getAddress());
            }
            if (MessageUtil.canSendMessage(chain, linkedChainId) != 2) {
                return CtxStateEnum.UNCONFIRM.getStatus();
            }
            message.setRequestHash(requestHash);
            NetWorkCall.broadcast(linkedChainId, message, CommandConstant.GET_CTX_STATE_MESSAGE, true);
            if (!chain.getCtxStateMap().containsKey(requestHash)) {
                chain.getCtxStateMap().put(requestHash, new ArrayList<>());
            }
            //Statistical processing results
            byte result = statisticsCtxState(chain, linkedChainId, requestHash);
            if (result == CtxStateEnum.CONFIRMED.getStatus()) {
                ctxStateService.save(ctxHash.getBytes(), chainId);
            }
            return result;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return CtxStateEnum.UNCONFIRM.getStatus();
        }
    }

    private static byte statisticsCtxState(Chain chain, int linkedChainId, NulsHash requestHash) {
        byte ctxState = CtxStateEnum.UNCONFIRM.getStatus();
        try {
            int tryCount = 0;
            int linkedNode = NetWorkCall.getAvailableNodeAmount(linkedChainId, true);
            Map<Byte, Integer> ctxStateMap = new HashMap<>(4);
            while (tryCount < NulsCrossChainConstant.BYZANTINE_TRY_COUNT) {
                for (Byte state : chain.getCtxStateMap().get(requestHash)) {
                    if (ctxStateMap.containsKey(state)) {
                        int count = ctxStateMap.get(state);
                        count++;
                        if (count >= linkedNode / 2) {
                            return state;
                        }
                    } else {
                        ctxStateMap.put(state, 1);
                    }
                }
                if (chain.getCtxStateMap().get(requestHash).size() >= linkedNode) {
                    break;
                }
                Thread.sleep(2000);
                tryCount++;
            }
            int maxCount = 0;
            for (Map.Entry<Byte, Integer> entry : ctxStateMap.entrySet()) {
                int value = entry.getValue();
                if (value > maxCount) {
                    maxCount = value;
                    ctxState = entry.getKey();
                }
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
            return ctxState;
        } finally {
            chain.getCtxStateMap().remove(requestHash);
        }
        return ctxState;
    }


    /**
     * Restore this chain protocolCoinData
     * Restore the Chain Protocol CoinData
     */
    private static void restoreCoinData(CoinData coinData) {
        //Assets and handling fees key:assetChainId_assetId   value:fromIn this asset - toThe total amount of this asset
        Map<String, BigInteger> assetMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
        String key;
        String mainKey = config.getMainChainId() + "_" + config.getMainAssetId();
        for (Coin coin : coinData.getFrom()) {
            key = coin.getAssetsChainId() + "_" + coin.getAssetsId();
            if (assetMap.containsKey(key)) {
                BigInteger amount = assetMap.get(key).add(coin.getAmount());
                assetMap.put(key, amount);
            } else {
                assetMap.put(key, coin.getAmount());
            }
        }
        for (Coin coin : coinData.getTo()) {
            key = coin.getAssetsChainId() + "_" + coin.getAssetsId();
            BigInteger amount = assetMap.get(key).subtract(coin.getAmount());
            assetMap.put(key, amount);
        }
        for (Map.Entry<String, BigInteger> entry : assetMap.entrySet()) {
            String entryKey = entry.getKey();
            if (entryKey.equals(mainKey)) {
                continue;
            }
            BigInteger entryValue = entry.getValue();
            Iterator<CoinFrom> it = coinData.getFrom().iterator();
            while (it.hasNext()) {
                Coin coin = it.next();
                key = coin.getAssetsChainId() + "_" + coin.getAssetsId();
                if (entryKey.equals(key)) {
                    if (coin.getAmount().compareTo(entryValue) > 0) {
                        coin.setAmount(coin.getAmount().subtract(entryValue));
                        break;
                    } else {
                        it.remove();
                        entryValue = entryValue.subtract(coin.getAmount());
                    }
                }
            }
        }
    }

    /**
     * Cross chain transaction signature Byzantine verification
     * Byzantine Verification of Cross-Chain Transaction Signature
     */
    public static boolean signByzantineVerify(Chain chain, Transaction ctx, List<String> verifierList, int byzantineCount, int verifierChainId) throws NulsException {
        TransactionSignature transactionSignature = new TransactionSignature();
        try {
            transactionSignature.parse(ctx.getTransactionSignature(), 0);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            throw e;
        }
        //Due to the presence of3505754Verified that the list of people was lost before the height was reachedbugSo before this height, as long as there is5A transaction signed by a seed node can be verified as successful
        if (ctx.getBlockHeight() > 3505754 && transactionSignature.getP2PHKSignatures().size() < byzantineCount) {
            chain.getLogger().error("The number of cross chain transaction signatures is less than the number of Byzantine signatures,Hash:{},signCount:{},byzantineCount:{}", ctx.getHash().toHex(), transactionSignature.getP2PHKSignatures().size(), byzantineCount);
            return false;
        }
        chain.getLogger().debug("Current Verifier List：{}", verifierList.toString());
        Iterator<P2PHKSignature> iterator = transactionSignature.getP2PHKSignatures().iterator();
        int passCount = 0;
        Set<String> passedAddress = new HashSet<>();
        while (iterator.hasNext()) {
            P2PHKSignature signature = iterator.next();
            for (String verifier : verifierList) {
                if (passedAddress.contains(verifier)) {
                    continue;
                }
                if (Arrays.equals(AddressTool.getAddress(signature.getPublicKey(), verifierChainId), AddressTool.getAddress(verifier))) {
                    passedAddress.add(verifier);
                    passCount++;
                    break;
                }
            }
        }
        //Due to the presence of3505754Verified that the list of people was lost before the height was reachedbugSo before this height, as long as there is5A transaction signed by a seed node can be verified as successful
        if (ctx.getBlockHeight() <= 3505754 && passCount == 5) {
            return true;
        }
        if (passCount < byzantineCount ) {
            chain.getLogger().error("The number of cross chain transaction signature verifications passed is less than the Byzantine number,Hash:{},passCount:{},byzantineCount:{}", ctx.getHash().toHex(), passCount, byzantineCount);
            return false;
        }
        return true;
    }
}
