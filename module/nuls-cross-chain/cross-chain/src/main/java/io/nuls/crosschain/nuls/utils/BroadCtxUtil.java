package io.nuls.crosschain.nuls.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxHashMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.bo.txdata.VerifierInitData;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.model.bo.BroadFailFlag;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.po.VerifierChangeSendFailPO;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.srorage.ConvertCtxService;
import io.nuls.crosschain.nuls.srorage.CtxStatusService;
import io.nuls.crosschain.nuls.srorage.SendHeightService;
import io.nuls.crosschain.nuls.srorage.VerifierChangeBroadFailedService;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

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
    private static NulsCrossChainConfig config;

    @Autowired
    private static ConvertCtxService convertCtxService;

    @Autowired
    private static CtxStatusService ctxStatusService;

    @Autowired
    private static VerifierChangeBroadFailedService verifierChangeBroadFailedService;

    /**
     * 区块高度变更之后，重新广播之前广播失败的跨链交易
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
                BroadFailFlag broadFailFlag = broadFailMap.get(verifierInitData.getRegisterChainId());
                if (!NetWorkCall.broadcast(verifierInitData.getRegisterChainId(), message, CommandConstant.BROAD_CTX_HASH_MESSAGE, true)) {
                    if (broadFailFlag == null) {
                        broadFailFlag = new BroadFailFlag();
                    }
                    broadFailFlag.setCrossChainTransferFlag(true);
                    broadFailMap.put(verifierInitData.getRegisterChainId(), broadFailFlag);
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
            return false;
        }
    }

    /**
     * 广播跨链转账交易
     * Broadcast cross chain transfer transaction
     *
     * @param chain          链信息
     * @param ctx            交易
     * @param message        消息
     * @param crossStatusMap 跨链状态缓存，如果为空则无需判断
     */
    private static boolean broadCrossTransferTx(Chain chain, Transaction ctx, BroadCtxHashMessage message, Map<Integer, Byte> crossStatusMap, Map<Integer, BroadFailFlag> broadFailMap) throws NulsException, IOException {
        int toId = chain.getChainId();
        if (config.isMainNet()) {
            toId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getTo().get(0).getAddress());
        }
        //判断当前链是否存在广播失败的验证人变更交/验证人初始化交易，如果有则需等待
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
                chain.getLogger().info("广播跨链转账交易给主网，本链协议hash:{}对应的主网协议hash:{}", ctx.getHash().toHex(), convertHash.toHex());
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
     * 广播验证人变更
     * Change of broadcast verifier
     *
     * @param chain          链信息
     * @param message        消息
     * @param crossStatusMap 跨链状态缓存，如果为空则无需判断
     */
    private static boolean broadVerifierChangeTx(Chain chain, BroadCtxHashMessage message, long cacheHeight, Map<Integer, Byte> crossStatusMap, Map<Integer, BroadFailFlag> broadFailMap) {
        int chainId = chain.getChainId();
        BroadFailFlag broadFailFlag;
        //如果为平信链则只需广播给主网
        if (!chain.isMainChain()) {
            broadFailFlag = broadFailMap.get(chainId);
            boolean haveConflict = broadFailFlag != null && (broadFailFlag.isVerifierInitFlag() || broadFailFlag.isVerifierChangeFlag() || broadFailFlag.isCrossChainTransferFlag());
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
                chain.getLogger().info("没有注册链信息");
                return true;
            }
            VerifierChangeSendFailPO po = verifierChangeBroadFailedService.get(cacheHeight, chainId);
            Set<Integer> broadFailChains = new HashSet<>();
            //如果为之前已广播的交易，则只需要广播给之前广播失败的链，否则需要广播给所有平行链
            if (po != null) {
                for (Integer toChainId : po.getChains()) {
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
