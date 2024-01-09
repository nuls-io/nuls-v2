package io.nuls.crosschain.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxHashMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainChangeData;
import io.nuls.crosschain.base.model.bo.txdata.VerifierInitData;
import io.nuls.crosschain.base.utils.enumeration.ChainInfoChangeType;
import io.nuls.crosschain.model.bo.BroadFailFlag;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.po.VerifierChangeSendFailPO;
import io.nuls.crosschain.rpc.call.NetWorkCall;
import io.nuls.crosschain.srorage.*;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class BroadCtxUtil {
    @Autowired
    private static ChainManager chainManager;

    @Autowired
    private static SendHeightService sendHeightService;
    @Autowired
    private static NulsCoresConfig config;

    @Autowired
    private static ConvertCtxService convertCtxService;

    @Autowired
    private static CtxStatusService ctxStatusService;

    @Autowired
    private static VerifierChangeBroadFailedService verifierChangeBroadFailedService;

    @Autowired
    private static CrossChangeBroadFailService crossChangeBroadFailService;

    /**
     * After the height of the block changes, rebroadcast the failed cross chain transactions previously broadcasted
     * After the block height changes, broadcast the failed cross chain transactions before rebroadcasting
     */
    public static boolean broadCtxHash(Chain chain, NulsHash ctxHash, long cacheHeight, Map<Integer, Byte> crossStatusMap, Map<Integer, BroadFailFlag> broadFailMap) {
        int chainId = chain.getChainId();
        BroadCtxHashMessage message = new BroadCtxHashMessage();
        message.setConvertHash(ctxHash);
        Transaction ctx = ctxStatusService.get(ctxHash, chainId).getTx();
        try {
            if (ctx.getType() == config.getCrossCtxType() || ctx.getType() == TxType.CONTRACT_TOKEN_CROSS_TRANSFER) {
                return broadCrossTransferTx(chain, ctx, message, crossStatusMap, broadFailMap);
            } else if (ctx.getType() == TxType.VERIFIER_CHANGE) {
                return broadVerifierChangeTx(chain, message, cacheHeight, crossStatusMap, broadFailMap);
            } else if (ctx.getType() == TxType.VERIFIER_INIT) {
                VerifierInitData verifierInitData = new VerifierInitData();
                verifierInitData.parse(ctx.getTxData(), 0);
                if (!NetWorkCall.broadcast(verifierInitData.getRegisterChainId(), message, CommandConstant.BROAD_CTX_HASH_MESSAGE, true)) {
                    BroadFailFlag broadFailFlag = broadFailMap.get(verifierInitData.getRegisterChainId());
                    if (broadFailFlag == null) {
                        broadFailFlag = new BroadFailFlag();
                    }
                    broadFailFlag.setVerifierInitFlag(true);
                    broadFailMap.put(verifierInitData.getRegisterChainId(), broadFailFlag);
                    return false;
                }
                chain.getLogger().info("Verifier initialized transaction broadcast successfully,chainId:{}",verifierInitData.getRegisterChainId());
            }else if(ctx.getType() == TxType.REGISTERED_CHAIN_CHANGE){
                return broadCrossChainChangeTx(chain, message, cacheHeight, crossStatusMap, broadFailMap, ctx);
            }
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * Broadcast Cross Chain Transfer Transactions
     * Broadcast cross chain transfer transaction
     *
     * @param chain          Chain information
     * @param ctx            transaction
     * @param message        news
     * @param crossStatusMap Cross chain state cache, if empty, no need to check
     */
    private static boolean broadCrossTransferTx(Chain chain, Transaction ctx, BroadCtxHashMessage message, Map<Integer, Byte> crossStatusMap, Map<Integer, BroadFailFlag> broadFailMap) throws NulsException, IOException {
        int toId = chain.getChainId();
        if (config.isMainNet()) {
            toId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getTo().get(0).getAddress());
        }
        //Determine if there is a verification person change handover for broadcast failure in the current chain/Verifier initializes transaction, if any, wait
        BroadFailFlag broadFailFlag = broadFailMap.get(toId);
        boolean haveConflict = broadFailFlag != null && (broadFailFlag.isVerifierInitFlag() || broadFailFlag.isVerifierChangeFlag());
        if (haveConflict) {
            return false;
        }
        byte broadStatus = getBroadStatus(chain, toId, crossStatusMap);
        boolean broadResult;
        if (broadStatus == 0) {
            return true;
        } else if (broadStatus == 1) {
            broadResult = false;
        } else {
            if (!config.isMainNet()) {
                NulsHash convertHash = TxUtil.friendConvertToMain(chain, ctx, TxType.CROSS_CHAIN).getHash();
                message.setConvertHash(convertHash);
                chain.getLogger().info("Broadcast cross chain transfer transactions to the main network, this chain protocolhash:{}Corresponding main network protocolhash:{}", ctx.getHash().toHex(), convertHash.toHex());
            }
            broadResult = NetWorkCall.broadcast(toId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE, true);
        }
        if (!broadResult) {
            if (broadFailFlag == null) {
                broadFailFlag = new BroadFailFlag();
            }
            broadFailFlag.setCrossChainTransferFlag(true);
            broadFailMap.put(toId, broadFailFlag);
            return false;
        }
        return true;
    }

    /**
     * Change of broadcast verifier
     * Change of broadcast verifier
     *
     * @param chain          Chain information
     * @param message        news
     * @param crossStatusMap Cross chain state cache, if empty, no need to check
     */
    private static boolean broadVerifierChangeTx(Chain chain, BroadCtxHashMessage message, long cacheHeight, Map<Integer, Byte> crossStatusMap, Map<Integer, BroadFailFlag> broadFailMap) {
        int chainId = chain.getChainId();
        BroadFailFlag broadFailFlag;
        //If it is a flat chain, only broadcast to the main network
        if (!chain.isMainChain()) {
            broadFailFlag = broadFailMap.get(chainId);
            boolean haveConflict = broadFailFlag != null && (broadFailFlag.isVerifierInitFlag() || broadFailFlag.isVerifierChangeFlag() || broadFailFlag.isCrossChainTransferFlag() || broadFailFlag.isCrossChainChangeFlag());
            if (haveConflict) {
                return false;
            }
            byte broadStatus = getBroadStatus(chain, chainId, crossStatusMap);
            if (broadStatus == 0) {
                return true;
            }
            if (broadStatus == 1 || !NetWorkCall.broadcast(chainId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE, true)) {
                if (broadFailFlag == null) {
                    broadFailFlag = new BroadFailFlag();
                }
                broadFailFlag.setVerifierChangeFlag(true);
                broadFailMap.put(chainId, broadFailFlag);
                return false;
            }
            return true;
        } else {
            boolean broadResult = true;
            if (chainManager.getRegisteredCrossChainList() == null || chainManager.getRegisteredCrossChainList().isEmpty() || chainManager.getRegisteredCrossChainList().size() == 1) {
                chain.getLogger().info("No registration chain information available");
                return true;
            }
            VerifierChangeSendFailPO po = verifierChangeBroadFailedService.get(cacheHeight, chainId);
            Set<Integer> broadFailChains = new HashSet<>();
            //If it is a previously broadcasted transaction, it only needs to be broadcasted to the chain that failed the previous broadcast, otherwise it needs to be broadcasted to all parallel chains
            if (po != null) {
                for (Integer toChainId : po.getChains()) {
                    broadFailFlag = broadFailMap.get(toChainId);
                    boolean haveConflict = broadFailFlag != null && (broadFailFlag.isVerifierInitFlag() || broadFailFlag.isVerifierChangeFlag() || broadFailFlag.isCrossChainTransferFlag()|| broadFailFlag.isCrossChainChangeFlag());
                    if (haveConflict) {
                        broadFailChains.add(toChainId);
                        continue;
                    }
                    byte broadStatus = getBroadStatus(chain, toChainId, crossStatusMap);
                    if (broadStatus == 0) {
                        continue;
                    }
                    if (broadStatus == 1 || !NetWorkCall.broadcast(toChainId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE, true)) {
                        broadResult = false;
                        broadFailChains.add(toChainId);
                        if (broadFailFlag == null) {
                            broadFailFlag = new BroadFailFlag();
                        }
                        broadFailFlag.setVerifierChangeFlag(true);
                        broadFailMap.put(chainId, broadFailFlag);
                    }
                }
            } else {
                for (ChainInfo chainInfo : chainManager.getRegisteredCrossChainList()) {
                    int toChainId = chainInfo.getChainId();
                    if (toChainId == chainId) {
                        continue;
                    }
                    broadFailFlag = broadFailMap.get(toChainId);
                    boolean haveConflict = broadFailFlag != null && (broadFailFlag.isVerifierInitFlag() || broadFailFlag.isVerifierChangeFlag() || broadFailFlag.isCrossChainTransferFlag());
                    if (haveConflict) {
                        broadFailChains.add(toChainId);
                        continue;
                    }
                    byte broadStatus = getBroadStatus(chain, toChainId, crossStatusMap);
                    if (broadStatus == 0) {
                        continue;
                    }
                    if (broadStatus == 1 || !NetWorkCall.broadcast(toChainId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE, true)) {
                        broadResult = false;
                        broadFailChains.add(toChainId);
                        if (broadFailFlag == null) {
                            broadFailFlag = new BroadFailFlag();
                        }
                        broadFailFlag.setVerifierChangeFlag(true);
                        broadFailMap.put(chainId, broadFailFlag);
                    }
                }
            }
            if (broadFailChains.isEmpty()) {
                verifierChangeBroadFailedService.delete(cacheHeight, chainId);
            } else {
                VerifierChangeSendFailPO failPO = new VerifierChangeSendFailPO(broadFailChains);
                verifierChangeBroadFailedService.save(cacheHeight, failPO, chainId);
            }
            return broadResult;
        }
    }


    /**
     * Change of broadcast verifier
     * Change of broadcast verifier
     *
     * @param chain          Chain information
     * @param message        news
     * @param crossStatusMap Cross chain state cache, if empty, no need to check
     */
    private static boolean broadCrossChainChangeTx(Chain chain, BroadCtxHashMessage message, long cacheHeight, Map<Integer, Byte> crossStatusMap, Map<Integer, BroadFailFlag> broadFailMap, Transaction ctx) {
        int chainId = chain.getChainId();
        //If it is a flat chain, only broadcast to the main network
        if(!chain.isMainChain()){
            chain.getLogger().error("The current chain is not the main network, and there should be no cross chain registration change transaction");
            return true;
        }
        RegisteredChainChangeData txData = new RegisteredChainChangeData();
        try {
            txData.parse(ctx.getTxData(),0);
        }catch (Exception e){
            chain.getLogger().error(e);
            return false;
        }
        int toChainId = txData.getRegisterChainId();
        BroadFailFlag broadFailFlag;
        if(txData.getType() == ChainInfoChangeType.INIT_REGISTER_CHAIN.getType()){
            broadFailFlag = broadFailMap.get(toChainId);
            boolean haveConflict = broadFailFlag != null && (broadFailFlag.isVerifierInitFlag() || broadFailFlag.isVerifierChangeFlag());
            if (haveConflict) {
                return false;
            }
            byte broadStatus = getBroadStatus(chain, toChainId, crossStatusMap);
            if (broadStatus == 0) {
                chain.getLogger().warn("The message receiving chain does not exist,toChainId:{}",toChainId);
                return true;
            }
            if (broadStatus == 1 || !NetWorkCall.broadcast(toChainId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE, true)) {
                if (broadFailFlag == null) {
                    broadFailFlag = new BroadFailFlag();
                }
                broadFailFlag.setCrossChainChangeFlag(true);
                broadFailMap.put(toChainId, broadFailFlag);
                return false;
            }
            chain.getLogger().info("Cross chain change message broadcast successful：toChainId:{}",toChainId);
            return true;
        }else{
            VerifierChangeSendFailPO po = crossChangeBroadFailService.get(cacheHeight, chainId);
            Set<Integer> broadFailChains = new HashSet<>();
            Set<Integer> broadChains = new HashSet<>();
            if (po != null) {
                broadChains = po.getChains();
            }else{
                for (ChainInfo chainInfo : chainManager.getRegisteredCrossChainList()){
                    broadChains.add(chainInfo.getChainId());
                }
                if(txData.getType() == ChainInfoChangeType.NEW_REGISTER_CHAIN.getType()){
                    broadChains.remove(toChainId);
                }
                broadChains.remove(config.getMainChainId());
            }
            broadChains.remove(config.getMainChainId());
            chain.getLogger().info("Cross chain transactions need to be broadcasted tobroadChains:{}",broadChains);
            boolean broadResult = true;
            for (Integer broadChainId : broadChains){
                broadFailFlag = broadFailMap.get(broadChainId);
                boolean haveConflict = broadFailFlag != null && (broadFailFlag.isVerifierInitFlag() || broadFailFlag.isVerifierChangeFlag());
                if (haveConflict) {
                    broadFailChains.add(broadChainId);
                    continue;
                }
                byte broadStatus = getBroadStatus(chain, broadChainId, crossStatusMap);
                if (broadStatus == 0) {
                    chain.getLogger().warn("The message receiving chain does not exist,toChainId:{}",broadChainId);
                    continue;
                }
                if (broadStatus == 1 || !NetWorkCall.broadcast(broadChainId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE, true)) {
                    broadResult = false;
                    broadFailChains.add(broadChainId);
                    if (broadFailFlag == null) {
                        broadFailFlag = new BroadFailFlag();
                    }
                    broadFailFlag.setVerifierChangeFlag(true);
                    broadFailMap.put(chainId, broadFailFlag);
                }
            }
            if (broadFailChains.isEmpty()) {
                crossChangeBroadFailService.delete(cacheHeight, chainId);
            } else {
                VerifierChangeSendFailPO failPO = new VerifierChangeSendFailPO(broadFailChains);
                crossChangeBroadFailService.save(cacheHeight, failPO, chainId);
                chain.getLogger().warn("Cross chain change message broadcast failed chainsbroadFailChains：{}",broadFailChains);
            }
            return broadResult;
        }
    }

    private static byte getBroadStatus(Chain chain, int chainId, Map<Integer, Byte> crossStatusMap) {
        byte broadStatus;
        if (crossStatusMap == null) {
            broadStatus = MessageUtil.canSendMessage(chain, chainId);
        } else {
            if (crossStatusMap.containsKey(chainId)) {
                broadStatus = crossStatusMap.get(chainId);
            } else {
                broadStatus = MessageUtil.canSendMessage(chain, chainId);
                crossStatusMap.put(chainId, broadStatus);
            }
        }
        return broadStatus;
    }
}
