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

package io.nuls.protocol.service.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.ProtocolVersion;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ChainParameters;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.po.StatisticsInfo;
import io.nuls.protocol.rpc.call.VersionChangeNotifier;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.protocol.storage.StatisticsStorageService;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 区块服务实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Component
public class ProtocolServiceImpl implements ProtocolService {

    @Autowired
    private StatisticsStorageService service;

    @Override
    public boolean startChain(int chainId) {
        return false;
    }

    @Override
    public boolean stopChain(int chainId, boolean cleanData) {
        return false;
    }

    @Override
    public void init(int chainId) {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        try {
            //初始化一条新协议统计信息，与区块高度绑定，并存到数据库
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setHeight(0);
            statisticsInfo.setLastHeight(0);
            statisticsInfo.setProtocolVersion(context.getCurrentProtocolVersion());
            Map<ProtocolVersion, Integer> proportionMap = new HashMap<>(1);
            proportionMap.put(context.getCurrentProtocolVersion(), 1);
            statisticsInfo.setProtocolVersionMap(proportionMap);
            statisticsInfo.setCount((short) 0);
            boolean b = service.save(chainId, statisticsInfo);
            commonLog.info("chainId-" + chainId + ", height-0, save-" + b + ", new statisticsInfo-" + statisticsInfo);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

    @Override
    public short save(int chainId, BlockHeader blockHeader) throws NulsException {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        StatisticsInfo lastValidStatisticsInfo = context.getLastValidStatisticsInfo();
        byte[] extend = blockHeader.getExtend();
        BlockExtendsData data = new BlockExtendsData();
        data.parse(new NulsByteBuffer(extend));
        long height = blockHeader.getHeight();
        //缓存统计总数+1
        int count = context.getCount();
        count++;
        context.setCount(count);
        context.setLatestHeight(height);

        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
        if (!validate(data, context)) {
            commonLog.error("chainId-" + chainId + ", invalid blockheader-" + height);
        } else {
            ProtocolVersion newProtocolVersion = new ProtocolVersion();
            newProtocolVersion.setVersion(data.getBlockVersion());
            newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            commonLog.debug("chainId-" + chainId + ", save block, height-" + height + ", protocol-" + newProtocolVersion);
            //重新计算统计信息
            proportionMap.merge(newProtocolVersion, 1, (a, b) -> a + b);
        }
        ChainParameters parameters = context.getParameters();
        short interval = parameters.getInterval();
        //每1000块进行一次统计
        if (count == interval) {
            int already = 0;
            for (Map.Entry<ProtocolVersion, Integer> entry : proportionMap.entrySet()) {
                ProtocolVersion version = entry.getKey();
                int real = entry.getValue();
                already += real;
                int expect = interval * version.getEffectiveRatio() / 100;
                //占比超过阈值，保存一条新协议统计记录到数据库
                if (!version.equals(currentProtocolVersion) && real >= expect) {
                    //初始化一条新协议统计信息，与区块高度绑定，并存到数据库
                    StatisticsInfo statisticsInfo = new StatisticsInfo();
                    statisticsInfo.setHeight(height);
                    statisticsInfo.setLastHeight(lastValidStatisticsInfo.getHeight());
                    statisticsInfo.setProtocolVersion(version);
                    statisticsInfo.setProtocolVersionMap(proportionMap);
                    //计数统计
                    if (lastValidStatisticsInfo.getProtocolVersion().equals(version)) {
                        statisticsInfo.setCount((short) (lastValidStatisticsInfo.getCount() + 1));
                    } else {
                        statisticsInfo.setCount((short) 1);
                    }
                    boolean b = service.save(chainId, statisticsInfo);
                    commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statisticsInfo-" + statisticsInfo);
                    //如果某协议版本连续统计确认数大于阈值，则进行版本升级
                    if (statisticsInfo.getCount() >= version.getContinuousIntervalCount()) {
                        //设置新协议版本
                        context.setCurrentProtocolVersion(version);
                        context.setCurrentProtocolVersionCount(statisticsInfo.getCount());
                        context.getProtocolVersionHistory().push(version);
                        boolean notify = VersionChangeNotifier.notify(chainId, version.getVersion());
                        commonLog.info("chainId-" + chainId + ", height-"+ height + ", new protocol version available-" + version);
                    }
                    context.setCount(0);
                    context.setLastValidStatisticsInfo(statisticsInfo);
                    //清除旧统计数据
                    proportionMap.clear();
                    return context.getCurrentProtocolVersion().getVersion();
                }
                //已经统计了1000个区块中的400个，但是还没有新协议生效，后面的就不需要统计了
                if (already > interval - (interval * parameters.getEffectiveRatioMinimum() / 100)) {
                    break;
                }
            }
            //初始化一条旧统计信息，与区块高度绑定，并存到数据库
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setHeight(height);
            statisticsInfo.setLastHeight(lastValidStatisticsInfo.getHeight());
            statisticsInfo.setProtocolVersion(currentProtocolVersion);
            statisticsInfo.setProtocolVersionMap(proportionMap);
            //计数统计
            statisticsInfo.setCount((short) (context.getCurrentProtocolVersionCount() + 1));
            boolean b = service.save(chainId, statisticsInfo);
            commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statisticsInfo-" + statisticsInfo);
            context.setCount(0);
            context.setLastValidStatisticsInfo(statisticsInfo);
            context.setCurrentProtocolVersionCount(context.getCurrentProtocolVersionCount() + 1);
            //清除旧统计数据
            proportionMap.clear();
        }
        return context.getCurrentProtocolVersion().getVersion();
    }

    @Override
    public short rollback(int chainId, BlockHeader blockHeader) throws NulsException {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        StatisticsInfo lastValidStatisticsInfo = context.getLastValidStatisticsInfo();
        byte[] extend = blockHeader.getExtend();
        BlockExtendsData data = new BlockExtendsData();
        data.parse(new NulsByteBuffer(extend));
        long height = blockHeader.getHeight();
        //缓存统计总数-1
        int count = context.getCount();
        count--;
        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        if (!validate(data, context)) {
            commonLog.error("chainId-" + chainId + ", invalid blockheader-" + height);
        } else {
            ProtocolVersion newProtocolVersion = new ProtocolVersion();
            newProtocolVersion.setVersion(data.getBlockVersion());
            newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            commonLog.debug("chainId-" + chainId + ", rollback block, height-" + height + ", protocol-" + newProtocolVersion);
            //重新计算统计信息
            proportionMap.merge(newProtocolVersion, 1, (a, b) -> a - b);
        }
        //缓存统计总数==0时，从数据库加载上一条统计记录
        ChainParameters parameters = context.getParameters();
        short interval = parameters.getInterval();
        //区块高度到达阈值，从数据库删除一条统计记录
        if (count < 0) {
            boolean b = service.delete(chainId, height);
            commonLog.info("chainId-" + chainId + ", height-" + height + ", delete-" + b);
            count = interval - 1;
            StatisticsInfo newValidStatisticsInfo = service.get(chainId, lastValidStatisticsInfo.getLastHeight());
            context.setLastValidStatisticsInfo(newValidStatisticsInfo);
            context.setProportionMap(newValidStatisticsInfo.getProtocolVersionMap());
            ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
            if (newValidStatisticsInfo.getProtocolVersion().equals(currentProtocolVersion) && newValidStatisticsInfo.getCount() < currentProtocolVersion.getContinuousIntervalCount()) {
                //设置新协议版本
                Stack<ProtocolVersion> history = context.getProtocolVersionHistory();
                if (history.size() > 1) {
                    ProtocolVersion pop = history.pop();
                    ProtocolVersion protocolVersion = history.peek();
                    context.setCurrentProtocolVersion(protocolVersion);
                    commonLog.info("chainId-" + chainId + ", height-" + height + ", protocol version rollback-" + pop + ", new protocol version available-" + protocolVersion);
                }
            }
        }
        context.setCount(count);
        context.setLatestHeight(height - 1);
        return context.getCurrentProtocolVersion().getVersion();
    }

    /**
     * 验证区块头协议信息正确性
     *
     * @param data
     * @param context
     * @return
     */
    private boolean validate(BlockExtendsData data, ProtocolContext context){
        short blockVersion = data.getBlockVersion();
        ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
        if (currentProtocolVersion.getVersion() > blockVersion) {
            return false;
        }
        ChainParameters parameters = context.getParameters();
        byte effectiveRatio = data.getEffectiveRatio();
        if (effectiveRatio > parameters.getEffectiveRatioMaximum()) {
            return false;
        }
        if (effectiveRatio < parameters.getEffectiveRatioMinimum()) {
            return false;
        }
        short continuousIntervalCount = data.getContinuousIntervalCount();
        if (continuousIntervalCount > parameters.getContinuousIntervalCountMaximum()) {
            return false;
        }
        return continuousIntervalCount >= parameters.getContinuousIntervalCountMinimum();
    }

}
