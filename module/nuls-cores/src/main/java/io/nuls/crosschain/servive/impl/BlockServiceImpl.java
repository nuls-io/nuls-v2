package io.nuls.crosschain.servive.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.constant.ParamConstant;
import io.nuls.crosschain.model.bo.BroadFailFlag;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.model.po.SendCtxHashPO;
import io.nuls.crosschain.rpc.call.ConsensusCall;
import io.nuls.crosschain.servive.BlockService;
import io.nuls.crosschain.srorage.*;
import io.nuls.crosschain.utils.BroadCtxUtil;
import io.nuls.crosschain.utils.TxUtil;
import io.nuls.crosschain.utils.manager.ChainManager;
import io.nuls.crosschain.utils.thread.VerifierChangeTxHandler;

import java.util.*;

/**
 * Interface implementation class provided for block module calls
 *
 * @author tag
 * @date 2019/4/25
 */
@Component
public class BlockServiceImpl implements BlockService {
    @Autowired
    private ChainManager chainManager;

    @Autowired
    private SendHeightService sendHeightService;

    @Autowired
    private SendedHeightService sendedHeightService;

    @Autowired
    private NulsCoresConfig config;

    @Autowired
    private ConvertCtxService convertCtxService;

    @Autowired
    private CtxStatusService ctxStatusService;

    @Autowired
    private VerifierChangeBroadFailedService verifierChangeBroadFailedService;

    @Override
    @SuppressWarnings("unchecked")
    public Result syncStatusUpdate(Map<String, Object> params) {
        if (params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.SYNC_STATUS) == null) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        int chainId = (int) params.get(ParamConstant.CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(NulsCrossChainErrorCode.CHAIN_NOT_EXIST);
        }
        int syncStatus = (int) params.get(ParamConstant.SYNC_STATUS);
        chain.setSyncStatus(syncStatus);
        chain.getLogger().info("Node synchronization status change,syncStatus:{}", syncStatus);
        return Result.getSuccess(CommonCodeConstanst.SUCCESS);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result newBlockHeight(Map<String, Object> params) {
        Result result = paramValid(params);
        if (result.isFailed()) {
            return result;
        }
        int chainId = (int) params.get(ParamConstant.CHAIN_ID);
        Chain chain = chainManager.getChainMap().get(chainId);
        long height = Long.valueOf(params.get(ParamConstant.NEW_BLOCK_HEIGHT).toString());
        chain.getLogger().debug("Received block height update information, the latest block height is：{}", height);
        //Check if there are cross chain transactions waiting to be broadcasted
        Map<Long, SendCtxHashPO> sendHeightMap = sendHeightService.getList(chainId);
        if (sendHeightMap != null && sendHeightMap.size() > 0) {
            Set<Long> sortSet = new TreeSet<>(sendHeightMap.keySet());
            //Cache the status of each chain
            Map<Integer, Byte> crossStatusMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
            //Broadcast to cache various transaction types that have failed on each chain to avoid broadcasting out of order
            Map<Integer, BroadFailFlag> broadFailMap = new HashMap<>(NulsCrossChainConstant.INIT_CAPACITY_16);
            for (long cacheHeight : sortSet) {
                if (height >= cacheHeight && height - cacheHeight < 2000) {
                    chain.getLogger().debug("The height of the broadcast block is{}Cross chain transactions to other chains", cacheHeight);
                    SendCtxHashPO po = sendHeightMap.get(cacheHeight);
                    List<NulsHash> broadSuccessCtxHash = new ArrayList<>();
                    List<NulsHash> broadFailCtxHash = new ArrayList<>();
                    for (NulsHash ctxHash : po.getHashList()) {
                        if (BroadCtxUtil.broadCtxHash(chain, ctxHash, cacheHeight, crossStatusMap, broadFailMap)) {
                            broadSuccessCtxHash.add(ctxHash);
                        } else {
                            broadFailCtxHash.add(ctxHash);
                        }
                    }
                    if (broadSuccessCtxHash.size() > 0) {
                        SendCtxHashPO sendedPo = sendedHeightService.get(cacheHeight, chainId);
                        if (sendedPo != null) {
                            sendedPo.getHashList().addAll(broadSuccessCtxHash);
                        } else {
                            sendedPo = new SendCtxHashPO(broadSuccessCtxHash);
                        }
                        if (!sendedHeightService.save(cacheHeight, sendedPo, chainId)) {
                            continue;
                        }
                    }
                    if (broadFailCtxHash.size() > 0) {
                        int ONE_DAY_HEIGHT = 360 * 24;
                        if (height - cacheHeight < ONE_DAY_HEIGHT) {
                            po.setHashList(broadFailCtxHash);
                            sendHeightService.save(cacheHeight, po, chainId);
                            chain.getLogger().error("The block height is{}Cross chain transaction broadcast failed for", cacheHeight);
                        }
                    } else {
                        sendHeightService.delete(cacheHeight, chainId);
                        chain.getLogger().info("The block height is{}Cross chain transaction broadcast successful", cacheHeight);
                    }
                } else {
                    continue;
                }
            }
        }
        chain.getLogger().debug("Block height update message processing completed,Height:{}\n\n", height);
        return Result.getSuccess(CommonCodeConstanst.SUCCESS);
    }

    private Result paramValid(Map<String, Object> params) {
        if (params.get(ParamConstant.CHAIN_ID) == null || params.get(ParamConstant.NEW_BLOCK_HEIGHT) == null || params.get(ParamConstant.PARAM_BLOCK_HEADER) == null) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        int chainId = (int) params.get(ParamConstant.CHAIN_ID);
        int download = (int) params.get("download");
        if (chainId <= 0) {
            return Result.getFailed(CommonCodeConstanst.PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(NulsCrossChainErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            BlockHeader blockHeader = new BlockHeader();
            String headerHex = (String) params.get(ParamConstant.PARAM_BLOCK_HEADER);
            blockHeader.parse(RPCUtil.decode(headerHex), 0);
            if (!chainManager.isCrossNetUseAble()) {
                chainManager.getChainHeaderMap().put(chainId, blockHeader);
                chain.getLogger().info("Waiting for consensus network networking completion");
                return Result.getSuccess(CommonCodeConstanst.SUCCESS);
            }
            if (config.isMainNet() && chainManager.getRegisteredCrossChainList().size() <= 1) {
                chain.getLogger().info("There is currently no registration chain");
                chainManager.getChainHeaderMap().put(chainId, blockHeader);
                return Result.getSuccess(CommonCodeConstanst.SUCCESS);
            }
            /*
            Blockchain in running state(download 0Blocking download in progress,1Received the latest block)Check for round changes. If there are round changes, query the consensus module for any changes in the consensus node. If there are changes, create a validator change transaction(This operation needs to be done after the validator initializes the transaction)
            */
            if (download == 1 && chain.getVerifierList() != null && !chain.getVerifierList().isEmpty()) {
                Map<String, List<String>> agentChangeMap;
                BlockHeader localHeader = chainManager.getChainHeaderMap().get(chainId);
                if (localHeader != null) {
                    BlockExtendsData blockExtendsData = blockHeader.getExtendsData();
                    BlockExtendsData localExtendsData = localHeader.getExtendsData();
                    if (blockExtendsData.getRoundIndex() == localExtendsData.getRoundIndex()) {
                        chainManager.getChainHeaderMap().put(chainId, blockHeader);
                        return Result.getSuccess(CommonCodeConstanst.SUCCESS);
                    }
                    agentChangeMap = ConsensusCall.getAgentChangeInfo(chain, localHeader.getExtend(), blockHeader.getExtend());
                } else {
                    agentChangeMap = ConsensusCall.getAgentChangeInfo(chain, null, blockHeader.getExtend());
                }
                if (agentChangeMap != null) {
                    List<String> registerAgentList = agentChangeMap.get(ParamConstant.PARAM_REGISTER_AGENT_LIST);
                    List<String> cancelAgentList = agentChangeMap.get(ParamConstant.PARAM_CANCEL_AGENT_LIST);
                    //Special processing for the first block,Check if the list of validators for the obtained changes is correct
                    if (localHeader == null) {
                        if (registerAgentList != null) {
                            registerAgentList.removeAll(chain.getVerifierList());
                        }
                    }
                    boolean verifierChange = (registerAgentList != null && !registerAgentList.isEmpty()) || (cancelAgentList != null && !cancelAgentList.isEmpty());
                    if (verifierChange) {
                        chain.getLogger().info("There is a change in verifier, create a transaction with a change in verifier, and the latest round shares a block address with the previous round：{},New Verifier List：{},Reduced list of validators：{}", chain.getVerifierList().toString(), registerAgentList, cancelAgentList);
                        Transaction verifierChangeTx = TxUtil.createVerifierChangeTx(registerAgentList, cancelAgentList, blockHeader.getExtendsData().getRoundStartTime(), chainId);
                        chain.getCrossTxThreadPool().execute(new VerifierChangeTxHandler(chain, verifierChangeTx, blockHeader.getHeight()));
                    }
                }
            }
            chainManager.getChainHeaderMap().put(chainId, blockHeader);
        } catch (Exception e) {
            chain.getLogger().error(e);
            return Result.getFailed(CommonCodeConstanst.DATA_PARSE_ERROR);
        }
        return Result.getSuccess(CommonCodeConstanst.SUCCESS);
    }
}
