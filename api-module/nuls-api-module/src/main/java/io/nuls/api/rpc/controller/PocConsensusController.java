/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.api.rpc.controller;

import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.*;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.CurrentRound;
import io.nuls.api.model.po.db.PocRoundItem;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;

import java.util.List;

/**
 * @author Niels
 */
@Controller
public class PocConsensusController {

    @Autowired
    private RoundManager roundManager;
    @Autowired
    private AgentService agentService;
    @Autowired
    private PunishService punishService;
    @Autowired
    private DepositService depositService;
    @Autowired
    private RoundService roundService;
//    @Autowired
//    private StatisticalService statisticalService;

    @Autowired
    private BlockService headerService;

    @Autowired
    private AliasService aliasService;

    @RpcMethod("getBestRoundItemList")
    public RpcResult getBestRoundItemList(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId = (int) params.get(0);

        ApiCache apiCache = CacheManager.getCache(chainId);
        List<PocRoundItem> itemList = apiCache.getCurrentRound().getItemList();
        RpcResult rpcResult = new RpcResult();
        itemList.addAll(itemList);
        rpcResult.setResult(itemList);
        return rpcResult;
    }
//
//    @RpcMethod("getConsensusNodeCount")
//    public RpcResult getConsensusNodeCount(List<Object> params) {
//        Map<String, Long> resultMap = new HashMap<>();
//        resultMap.put("seedsCount", (long) ApiContext.SEED_NODE_ADDRESS.size());
//        resultMap.put("consensusCount", (long) (roundManager.getCurrentRound().getMemberCount() - ApiContext.SEED_NODE_ADDRESS.size()));
//        long count = agentService.agentsCount(ApiContext.bestHeight);
//        resultMap.put("agentCount", count);
//        resultMap.put("totalCount", count + ApiContext.SEED_NODE_ADDRESS.size());
//        RpcResult result = new RpcResult();
//        result.setResult(resultMap);
//        return result;
//    }
//
//    @RpcMethod("getConsensusStatistical")
//    public RpcResult getConsensusStatistical(List<Object> params) {
//        VerifyUtils.verifyParams(params, 1);
//        int type = (int) params.get(0);
//        List list = this.statisticalService.getStatisticalList(type, "consensusLocked");
//        return new RpcResult().setResult(list);
//    }
//
//    @RpcMethod("getConsensusNodes")
//    public RpcResult getConsensusNodes(List<Object> params) {
//        VerifyUtils.verifyParams(params, 3);
//        int pageIndex = (int) params.get(0);
//        int pageSize = (int) params.get(1);
//        int type = (int) params.get(2);
//        if (pageIndex <= 0) {
//            pageIndex = 1;
//        }
//        if (pageSize <= 0 || pageSize > 200) {
//            pageSize = 10;
//        }
////        Map<String, Integer> map = new HashMap<>();
////        List<PocRoundItem> itemList = roundManager.getCurrentRound().getItemList();
////        for (PocRoundItem item : itemList) {
////            map.put(item.getPackingAddress(), 1);
////        }
//
//        PageInfo<AgentInfo> list = agentService.getAgentList(type, pageIndex, pageSize);
//        for (AgentInfo agentInfo : list.getList()) {
//            RpcClientResult<AgentInfo> clientResult = walletRPCHandler.getAgent(agentInfo.getTxHash());
//            if (clientResult.isSuccess()) {
//                agentInfo.setCreditValue(clientResult.getData().getCreditValue());
//                agentInfo.setDepositCount(clientResult.getData().getDepositCount());
//                agentInfo.setStatus(clientResult.getData().getStatus());
//                if (agentInfo.getAgentAlias() == null) {
//                    AliasInfo info = aliasService.getAliasByAddress(agentInfo.getAgentAddress());
//                    if (null != info) {
//                        agentInfo.setAgentAlias(info.getAlias());
//                    }
//                }
//            }
//        }
//
//        Collections.sort(list.getList(), AgentComparator.getInstance());
//
//        return new RpcResult().setResult(list);
//    }
//
//    @RpcMethod("getConsensusNode")
//    public RpcResult getConsensusNode(List<Object> params) {
//        VerifyUtils.verifyParams(params, 1);
//        String agentHash = (String) params.get(0);
//
//        AgentInfo agentInfo = agentService.getAgentByAgentHash(agentHash);
//
//        long count = punishService.getYellowCount(agentInfo.getAgentAddress());
//        if (agentInfo.getTotalPackingCount() != 0) {
//            agentInfo.setLostRate(DoubleUtils.div(count, count + agentInfo.getTotalPackingCount()));
//        }
//
//        List<PocRoundItem> itemList = roundManager.getCurrentRound().getItemList();
//
//        PocRoundItem roundItem = null;
//        if (null != itemList) {
//            for (PocRoundItem item : itemList) {
//                if (item.getPackingAddress().equals(agentInfo.getPackingAddress())) {
//                    roundItem = item;
//                    break;
//                }
//            }
//        }
//        if (null == roundItem) {
//            agentInfo.setStatus(0);
//        } else {
//            agentInfo.setRoundPackingTime(roundManager.getCurrentRound().getStartTime() + roundItem.getOrder() * 10000);
//            agentInfo.setStatus(1);
//        }
//
//        RpcClientResult<AgentInfo> result = walletRPCHandler.getAgent(agentHash);
//        if (result.isSuccess()) {
//            AgentInfo agent = result.getData();
//            agentInfo.setCreditValue(agent.getCreditValue());
//            agentInfo.setDepositCount(agent.getDepositCount());
//            if (agentInfo.getAgentAlias() == null) {
//                AliasInfo info = aliasService.getAliasByAddress(agentInfo.getAgentAddress());
//                if (null != info) {
//                    agentInfo.setAgentAlias(info.getAlias());
//                }
//            }
//        }
//
//        return new RpcResult().setResult(agentInfo);
//    }
//
//    @RpcMethod("getConsensusNodeStatistical")
//    public RpcResult getConsensusNodeStatistical(List<Object> params) {
//        VerifyUtils.verifyParams(params, 1);
//        int type = (int) params.get(0);
//        List list = this.statisticalService.getStatisticalList(type, "nodeCount");
//        return new RpcResult().setResult(list);
//    }
//
//    @RpcMethod("getAnnulizedRewardStatistical")
//    public RpcResult getAnnulizedRewardStatistical(List<Object> params) {
//        VerifyUtils.verifyParams(params, 1);
//        int type = (int) params.get(0);
//        List list = this.statisticalService.getStatisticalList(type, "annualizedReward");
//        return new RpcResult().setResult(list);
//    }
//
//    @RpcMethod("getPunishList")
//    public RpcResult getPunishList(List<Object> params) {
//        VerifyUtils.verifyParams(params, 4);
//        int pageIndex = (int) params.get(0);
//        int pageSize = (int) params.get(1);
//        int type = (int) params.get(2);
//        String agentAddress = (String) params.get(3);
//        if (!AddressTool.validAddress(agentAddress)) {
//            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] is inValid"));
//        }
//        if (pageIndex <= 0) {
//            pageIndex = 1;
//        }
//        if (pageSize <= 0 || pageSize > 100) {
//            pageSize = 10;
//        }
//        PageInfo<PunishLog> list = punishService.getPunishLogList(type, agentAddress, pageIndex, pageSize);
//        return new RpcResult().setResult(list);
//    }
//
//    @RpcMethod("getConsensusDeposit")
//    public RpcResult getConsensusDeposit(List<Object> params) {
//        VerifyUtils.verifyParams(params, 3);
//        int pageIndex = (int) params.get(0);
//        int pageSize = (int) params.get(1);
//        String agentHash = (String) params.get(2);
//        if (StringUtils.isBlank(agentHash)) {
//            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[agentHash] is inValid"));
//        }
//        if (pageIndex <= 0) {
//            pageIndex = 1;
//        }
//        if (pageSize <= 0 || pageSize > 100) {
//            pageSize = 10;
//        }
//        PageInfo<DepositInfo> list = this.depositService.getDepositListByAgentHash(agentHash, pageIndex, pageSize);
//        return new RpcResult().setResult(list);
//    }
//
//    @RpcMethod("getBestRoundInfo")
//    public RpcResult getBestRoundInfo(List<Object> params) {
//        VerifyUtils.verifyParams(params, 0);
//        return new RpcResult().setResult(roundManager.getCurrentRound());
//    }
//
//    @RpcMethod("getAllConsensusDeposit")
//    public RpcResult getAllConsensusDeposit(List<Object> params) {
//        VerifyUtils.verifyParams(params, 4);
//        int pageIndex = (int) params.get(0);
//        int pageSize = (int) params.get(1);
//        String agentHash = (String) params.get(2);
//        int type = (int) params.get(3);
//        if (StringUtils.isBlank(agentHash)) {
//            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[agentHash] is inValid"));
//        }
//        if (pageIndex <= 0) {
//            pageIndex = 1;
//        }
//        if (pageSize <= 0 || pageSize > 100) {
//            pageSize = 10;
//        }
//        PageInfo<DepositInfo> list = this.depositService.getCancelDepositListByAgentHash(agentHash, type, pageIndex, pageSize);
//        return new RpcResult().setResult(list);
//    }
//
//    @RpcMethod("getRoundList")
//    public RpcResult getRoundList(List<Object> params) {
//        VerifyUtils.verifyParams(params, 2);
//        int pageIndex = (int) params.get(0);
//        int pageSize = (int) params.get(1);
//        if (pageIndex <= 0) {
//            pageIndex = 1;
//        }
//        if (pageSize <= 0 || pageSize > 100) {
//            pageSize = 10;
//        }
//        long count = roundService.getTotalCount();
//        List<PocRound> roundList = roundService.getRoundList(pageIndex, pageSize);
//        PageInfo<PocRound> pageInfo = new PageInfo<>();
//        pageInfo.setPageNumber(pageIndex);
//        pageInfo.setPageSize(pageSize);
//        pageInfo.setTotalCount(count);
//        pageInfo.setList(roundList);
//        return new RpcResult().setResult(pageInfo);
//    }
//
//    @RpcMethod("getRoundInfo")
//    public RpcResult getRoundInfo(List<Object> params) {
//        VerifyUtils.verifyParams(params, 1);
//        long roundIndex = Long.parseLong(params.get(0) + "");
//        if (roundIndex == 1) {
//            return getFirstRound();
//        }
//
//        CurrentRound round = new CurrentRound();
//        PocRound pocRound = roundService.getRound(roundIndex);
//        if (pocRound == null) {
//            throw new JsonRpcException(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
//        }
//        List<PocRoundItem> itemList = roundService.getRoundItemList(roundIndex);
//        round.setItemList(itemList);
//        round.initByPocRound(pocRound);
//        return new RpcResult().setResult(round);
//    }
//
//    private RpcResult getFirstRound() {
//        BlockHeaderInfo headerInfo = headerService.getBlockHeaderInfoByHeight(0);
//        if (null == headerInfo) {
//            return new RpcResult();
//        }
//        CurrentRound round = new CurrentRound();
//        round.setStartTime(headerInfo.getRoundStartTime());
//        round.setStartHeight(0);
//        round.setProducedBlockCount(1);
//        round.setMemberCount(1);
//        round.setIndex(1);
//        round.setEndTime(headerInfo.getCreateTime());
//        round.setEndHeight(0);
//        List<PocRoundItem> itemList = new ArrayList<>();
//        PocRoundItem item = new PocRoundItem();
//        itemList.add(item);
//        item.setTime(headerInfo.getCreateTime());
//        item.setTxCount(1);
//        item.setBlockHash(headerInfo.getHash());
//        item.setBlockHeight(0);
//        item.setPackingAddress(headerInfo.getPackingAddress());
//        item.setRoundIndex(1);
//        item.setOrder(1);
//        round.setItemList(itemList);
//        return new RpcResult().setResult(round);
//    }

}
