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

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.ApiConstant;
import io.nuls.api.db.*;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.utils.AgentComparator;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.model.StringUtils;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.api.constant.DBTableConstant.CONSENSUS_LOCKED;

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
    @Autowired
    private StatisticalService statisticalService;

    @Autowired
    private BlockService headerService;

    @Autowired
    private AliasService aliasService;

    @RpcMethod("getBestRoundItemList")
    public RpcResult getBestRoundItemList(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            ApiCache apiCache = CacheManager.getCache(chainId);
            List<PocRoundItem> itemList = apiCache.getCurrentRound().getItemList();
            RpcResult rpcResult = new RpcResult();
            itemList.addAll(itemList);
            rpcResult.setResult(itemList);
            return rpcResult;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getConsensusNodeCount")
    public RpcResult getConsensusNodeCount(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            ApiCache apiCache = CacheManager.getCache(chainId);
            if (apiCache == null) {
                return RpcResult.dataNotFound();
            }
            Map<String, Long> resultMap = new HashMap<>();
            resultMap.put("seedsCount", (long) apiCache.getChainInfo().getSeeds().size());
            int consensusCount = apiCache.getCurrentRound().getMemberCount() - apiCache.getChainInfo().getSeeds().size();
            if (consensusCount < 0) {
                consensusCount = 0;
            }
            resultMap.put("consensusCount", (long) consensusCount);
            long count = agentService.agentsCount(chainId, apiCache.getBestHeader().getHeight());
            resultMap.put("agentCount", count);
            resultMap.put("totalCount", count + apiCache.getChainInfo().getSeeds().size());
            RpcResult result = new RpcResult();
            result.setResult(resultMap);
            return result;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getConsensusNodes")
    public RpcResult getConsensusNodes(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageIndex, pageSize, type;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            type = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (type < 0 || type > 3) {
            return RpcResult.paramError("[type] is invalid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<AgentInfo> pageInfo;
            if (!CacheManager.isChainExist(chainId)) {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
                return new RpcResult().setResult(pageInfo);
            }

            pageInfo = agentService.getAgentList(chainId, type, pageIndex, pageSize);
            for (AgentInfo agentInfo : pageInfo.getList()) {
                Result<AgentInfo> clientResult = WalletRpcHandler.getAgentInfo(chainId, agentInfo.getTxHash());
                if (clientResult.isSuccess()) {
                    agentInfo.setCreditValue(clientResult.getData().getCreditValue());
                    agentInfo.setDepositCount(clientResult.getData().getDepositCount());
                    agentInfo.setStatus(clientResult.getData().getStatus());
                    if (agentInfo.getAgentAlias() == null) {
                        AliasInfo info = aliasService.getAliasByAddress(chainId, agentInfo.getAgentAddress());
                        if (null != info) {
                            agentInfo.setAgentAlias(info.getAlias());
                        }
                    }
                }
            }
            Collections.sort(pageInfo.getList(), AgentComparator.getInstance());
            return new RpcResult().setResult(pageInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getConsensusNode")
    public RpcResult getConsensusNode(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String agentHash;
        try {
            chainId = (int) params.get(0);
            agentHash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            AgentInfo agentInfo = agentService.getAgentByHash(chainId, agentHash);
            if (agentInfo == null) {
                return RpcResult.dataNotFound();
            }
            long count = punishService.getYellowCount(chainId, agentInfo.getAgentAddress());
            if (agentInfo.getTotalPackingCount() != 0) {
                agentInfo.setLostRate(DoubleUtils.div(count, count + agentInfo.getTotalPackingCount()));
            }
            agentInfo.setYellowCardCount((int) count);
            ApiCache apiCache = CacheManager.getCache(chainId);
            List<PocRoundItem> itemList = apiCache.getCurrentRound().getItemList();
            PocRoundItem roundItem = null;
            if (null != itemList) {
                for (PocRoundItem item : itemList) {
                    if (item.getPackingAddress().equals(agentInfo.getPackingAddress())) {
                        roundItem = item;
                        break;
                    }
                }
            }
            if (agentInfo.getStatus() != ApiConstant.STOP_AGENT) {
                if (null == roundItem) {
                    agentInfo.setStatus(0);
                } else {
                    agentInfo.setRoundPackingTime(apiCache.getCurrentRound().getStartTime() + roundItem.getOrder() * 10000);
                    agentInfo.setStatus(1);
                }
            }

            Result<AgentInfo> result = WalletRpcHandler.getAgentInfo(chainId, agentHash);
            if (result.isSuccess()) {
                AgentInfo agent = result.getData();
                agentInfo.setCreditValue(agent.getCreditValue());
                agentInfo.setDepositCount(agent.getDepositCount());
                if (agentInfo.getAgentAlias() == null) {
                    AliasInfo info = aliasService.getAliasByAddress(chainId, agentInfo.getAgentAddress());
                    if (null != info) {
                        agentInfo.setAgentAlias(info.getAlias());
                    }
                }
            }
            return RpcResult.success(agentInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAccountConsensusNode")
    public RpcResult getAccountConsensusNode(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is invalid");
        }
        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            AgentInfo agentInfo = agentService.getAgentByAgentAddress(chainId, address);
            return RpcResult.success(agentInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getConsensusStatistical")
    public RpcResult getConsensusStatistical(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId, type;
        try {
            chainId = (int) params.get(0);
            type = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (type < 0 || type > 4) {
            return RpcResult.paramError("[type] is invalid");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.success(new ArrayList<>());
            }
            List list = this.statisticalService.getStatisticalList(chainId, type, CONSENSUS_LOCKED);
            return new RpcResult().setResult(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getConsensusNodeStatistical")
    public RpcResult getConsensusNodeStatistical(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId, type;
        try {
            chainId = (int) params.get(0);
            type = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (type < 0 || type > 4) {
            return RpcResult.paramError("[type] is invalid");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.success(new ArrayList<>());
            }
            List list = this.statisticalService.getStatisticalList(chainId, type, "nodeCount");
            return new RpcResult().setResult(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAnnulizedRewardStatistical")
    public RpcResult getAnnulizedRewardStatistical(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId, type;
        try {
            chainId = (int) params.get(0);
            type = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (type < 0 || type > 4) {
            return RpcResult.paramError("[type] is invalid");
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.success(new ArrayList<>());
            }
            List list = this.statisticalService.getStatisticalList(chainId, type, "annualizedReward");
            return new RpcResult().setResult(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getPunishList")
    public RpcResult getPunishList(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, pageIndex, pageSize, type;
        String agentAddress;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            type = (int) params.get(3);
            agentAddress = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (type < 0 || type > 2) {
            return RpcResult.paramError("[type] is invalid");
        }
        if (!AddressTool.validAddress(chainId, agentAddress)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<PunishLogInfo> list;
            if (!CacheManager.isChainExist(chainId)) {
                list = new PageInfo<>(pageIndex, pageSize);
            } else {
                list = punishService.getPunishLogList(chainId, type, agentAddress, pageIndex, pageSize);
            }
            return new RpcResult().setResult(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getConsensusDeposit")
    public RpcResult getConsensusDeposit(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageIndex, pageSize;
        String agentHash;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            agentHash = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (StringUtils.isBlank(agentHash)) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<DepositInfo> list;
            if (!CacheManager.isChainExist(chainId)) {
                list = new PageInfo<>(pageIndex, pageSize);
            } else {
                list = this.depositService.getDepositListByAgentHash(chainId, agentHash, pageIndex, pageSize);
            }
            return new RpcResult().setResult(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAllConsensusDeposit")
    public RpcResult getAllConsensusDeposit(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, pageIndex, pageSize, type;
        String agentHash;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            agentHash = (String) params.get(3);
            type = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (type < 0 || type > 2) {
            return RpcResult.paramError("[type] is invalid");
        }
        if (StringUtils.isBlank(agentHash)) {
            return RpcResult.paramError("[agentHash] is inValid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<DepositInfo> list;
            if (!CacheManager.isChainExist(chainId)) {
                list = new PageInfo<>(pageIndex, pageSize);
            } else {
                list = this.depositService.getCancelDepositListByAgentHash(chainId, agentHash, type, pageIndex, pageSize);
            }
            return new RpcResult().setResult(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAccountConsensus")
    public RpcResult getAccountConsensus(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageIndex, pageSize;
        String address;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is invalid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<AgentInfo> pageInfo;
            if (!CacheManager.isChainExist(chainId)) {
                pageInfo = new PageInfo(pageIndex, pageSize);
                return RpcResult.success(pageInfo);
            }
            List<String> hashList = depositService.getAgentHashList(chainId, address);
            AgentInfo agentInfo = agentService.getAliveAgentByAgentAddress(chainId, address);
            if (agentInfo != null && !hashList.contains(agentInfo.getTxHash())) {
                hashList.add(agentInfo.getTxHash());
            }

            pageInfo = agentService.getAgentByHashList(chainId, pageIndex, pageSize, hashList);
            for (AgentInfo info : pageInfo.getList()) {
                Result<AgentInfo> clientResult = WalletRpcHandler.getAgentInfo(chainId, info.getTxHash());
                if (clientResult.isSuccess()) {
                    info.setCreditValue(clientResult.getData().getCreditValue());
                    info.setDepositCount(clientResult.getData().getDepositCount());
                    info.setStatus(clientResult.getData().getStatus());
                    if (info.getAgentAlias() == null) {
                        AliasInfo aliasInfo = aliasService.getAliasByAddress(chainId, info.getAgentAddress());
                        if (null != aliasInfo) {
                            info.setAgentAlias(aliasInfo.getAlias());
                        }
                    }
                }
            }
            return RpcResult.success(pageInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAccountDeposit")
    public RpcResult getAccountDeposit(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, pageIndex, pageSize;
        String address, agentHash;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            address = (String) params.get(3);
            agentHash = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is invalid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<DepositInfo> list;
            if (!CacheManager.isChainExist(chainId)) {
                list = new PageInfo<>(pageIndex, pageSize);
            } else {
                list = this.depositService.getDepositListByAddress(chainId, agentHash, address, pageIndex, pageSize);
            }
            return new RpcResult().setResult(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAccountDepositValue")
    public RpcResult getAccountDepositValue(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address, agentHash;
        try {
            chainId = (int) params.get(0);
            address = (String) params.get(1);
            agentHash = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            BigInteger value = depositService.getDepositAmount(chainId, address, agentHash);
            return new RpcResult().setResult(value);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getBestRoundInfo")
    public RpcResult getBestRoundInfo(List<Object> params) {
        try {
            VerifyUtils.verifyParams(params, 1);
            int chainId = (int) params.get(0);
            ApiCache apiCache = CacheManager.getCache(chainId);
            if (apiCache == null) {
                return RpcResult.dataNotFound();
            }
            return new RpcResult().setResult(apiCache.getCurrentRound());
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getRoundList")
    public RpcResult getRoundList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId = (int) params.get(0);
        int pageIndex = (int) params.get(1);
        int pageSize = (int) params.get(2);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.success(new PageInfo<>(pageIndex, pageSize));
            }
            long count = roundService.getTotalCount(chainId);
            List<PocRound> roundList = roundService.getRoundList(chainId, pageIndex, pageSize);
            PageInfo<PocRound> pageInfo = new PageInfo<>();
            pageInfo.setPageNumber(pageIndex);
            pageInfo.setPageSize(pageSize);
            pageInfo.setTotalCount(count);
            pageInfo.setList(roundList);
            return new RpcResult().setResult(pageInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getRoundInfo")
    public RpcResult getRoundInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId = (int) params.get(0);
        long roundIndex = Long.parseLong(params.get(1) + "");

        try {
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            if (roundIndex == 1) {
                return getFirstRound(chainId);
            }
            CurrentRound round = new CurrentRound();
            PocRound pocRound = roundService.getRound(chainId, roundIndex);
            if (pocRound == null) {
                return RpcResult.dataNotFound();
            }
            List<PocRoundItem> itemList = roundService.getRoundItemList(chainId, roundIndex);
            round.setItemList(itemList);
            round.initByPocRound(pocRound);
            return new RpcResult().setResult(round);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    private RpcResult getFirstRound(int chainId) {
        BlockHeaderInfo headerInfo = headerService.getBlockHeader(chainId, 0);
        if (null == headerInfo) {
            return new RpcResult();
        }
        CurrentRound round = new CurrentRound();
        round.setStartTime(headerInfo.getRoundStartTime());
        round.setStartHeight(0);
        round.setProducedBlockCount(1);
        round.setMemberCount(1);
        round.setIndex(1);
        round.setEndTime(headerInfo.getCreateTime());
        round.setEndHeight(0);
        List<PocRoundItem> itemList = new ArrayList<>();
        PocRoundItem item = new PocRoundItem();
        itemList.add(item);
        item.setTime(headerInfo.getCreateTime());
        item.setTxCount(1);
        item.setBlockHash(headerInfo.getHash());
        item.setBlockHeight(0);
        item.setPackingAddress(headerInfo.getPackingAddress());
        item.setRoundIndex(1);
        item.setOrder(1);
        round.setItemList(itemList);
        return new RpcResult().setResult(round);
    }

}
