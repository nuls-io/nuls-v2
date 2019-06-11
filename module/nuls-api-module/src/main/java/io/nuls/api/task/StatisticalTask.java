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

package io.nuls.api.task;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.AgentService;
import io.nuls.api.db.BlockService;
import io.nuls.api.db.DepositService;
import io.nuls.api.db.StatisticalService;
import io.nuls.api.db.mongo.MongoAgentServiceImpl;
import io.nuls.api.db.mongo.MongoBlockServiceImpl;
import io.nuls.api.db.mongo.MongoDepositServiceImpl;
import io.nuls.api.db.mongo.MongoStatisticalServiceImpl;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AgentInfo;
import io.nuls.api.model.po.db.BlockHeaderInfo;
import io.nuls.api.model.po.db.DepositInfo;
import io.nuls.api.model.po.db.StatisticalInfo;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.DoubleUtils;

import java.math.BigInteger;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import io.nuls.core.rpc.util.NulsDateUtils;

/**
 * @author Niels
 */
public class StatisticalTask implements Runnable {

    private int chainId;

    private StatisticalService statisticalService;

    private BlockService blockService;

    private DepositService depositService;

    private AgentService agentService;

    public StatisticalTask(int chainId) {
        this.chainId = chainId;
        statisticalService = SpringLiteContext.getBean(MongoStatisticalServiceImpl.class);
        blockService = SpringLiteContext.getBean(MongoBlockServiceImpl.class);
        depositService = SpringLiteContext.getBean(MongoDepositServiceImpl.class);
        agentService = SpringLiteContext.getBean(MongoAgentServiceImpl.class);
    }

    @Override
    public void run() {
        try {
            this.doCalc();
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
    }

    private void doCalc() {
        long bestId = statisticalService.getBestId(chainId);
        BlockHeaderInfo header = blockService.getBestBlockHeader(chainId);
        if (null == header || header.getHeight() == 0) {
            return;
        }
        long day = 24 * 3600000;
        long start = bestId + 1;
        long end = 0;
        if (bestId == -1) {
            BlockHeaderInfo header0 = blockService.getBlockHeader(chainId, 1);
            start = header0.getCreateTime() - NulsDateUtils.SECOND_TIME * 10;
            end = start + day;
            this.statisticalService.saveBestId(chainId, start);
        } else {
            end = start + day - 1;
        }
        while (true) {
            if (end > header.getCreateTime()) {
                break;
            }
            statistical(start, end);
            start = end + 1;
            end = end + day;
            BlockHeaderInfo newBlockHeader = blockService.getBestBlockHeader(chainId);
            if (null != newBlockHeader) {
                header = newBlockHeader;
            }
        }
    }

    private void statistical(long start, long end) {
        long txCount = statisticalService.calcTxCount(chainId, start, end);
        BigInteger consensusLocked = BigInteger.ZERO;
        long height = blockService.getMaxHeight(chainId, end);
        List<AgentInfo> agentList = agentService.getAgentList(chainId, height);
        List<DepositInfo> depositList = depositService.getDepositList(chainId, height);
        int nodeCount = agentList.size();
        for (AgentInfo agent : agentList) {
            consensusLocked = consensusLocked.add(agent.getDeposit());
        }
        for (DepositInfo deposit : depositList) {
            consensusLocked = consensusLocked.add(deposit.getAmount());
        }
        double annualizedReward = 0L;
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (consensusLocked.compareTo(BigInteger.ZERO) != 0) {
            Result<Map> result = WalletRpcHandler.getConsensusConfig(chainId);
            Map map = result.getData();
            String inflationAmount = (String) map.get("inflationAmount");
            annualizedReward = DoubleUtils.mul(100, DoubleUtils.div(new BigInteger(inflationAmount), consensusLocked, 4), 2);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(end);
        StatisticalInfo info = new StatisticalInfo();
        info.setTime(end);
        info.setTxCount(txCount);
        info.setAnnualizedReward(annualizedReward);
        info.setNodeCount(nodeCount);
        info.setConsensusLocked(consensusLocked);
        info.setDate(calendar.get(Calendar.DATE));
        info.setMonth(calendar.get(Calendar.MONTH) + 1);
        info.setYear(calendar.get(Calendar.YEAR));
        try {
            this.statisticalService.insert(chainId, info);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
        }
        this.statisticalService.updateBestId(chainId, info.getTime());
    }

}
