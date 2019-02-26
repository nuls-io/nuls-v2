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
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.db.service.RocksDBService;
import io.nuls.protocol.constant.Constant;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolConfig;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.protocol.model.po.Statistics;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.protocol.service.StatisticsStorageService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static io.nuls.protocol.utils.LoggerUtil.commonLog;

/**
 * 区块服务实现类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:09
 */
@Service
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
            RocksDBService.createTable(Constant.STATISTICS + chainId);
            //初始化一条新协议统计信息，与区块高度绑定，并存到数据库
            Statistics statistics = new Statistics();
            statistics.setHeight(0);
            statistics.setLastHeight(0);
            statistics.setProtocolVersion(context.getCurrentProtocolVersion());
            Map<ProtocolVersion, Integer> proportionMap = new HashMap<>(1);
            proportionMap.put(context.getCurrentProtocolVersion(), 1);
            statistics.setProtocolVersionMap(proportionMap);
            statistics.setCount((short) 0);
            boolean b = service.save(chainId, statistics);
            commonLog.info("chainId-" + chainId + ", height-0, save-" + b + ", new statistics-" + statistics);
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

    @Override
    public short save(int chainId, BlockHeader blockHeader) throws NulsException {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        Statistics lastValidStatistics = context.getLastValidStatistics();
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
        ProtocolConfig config = context.getConfig();
        short interval = config.getInterval();
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
                    Statistics statistics = new Statistics();
                    statistics.setHeight(height);
                    statistics.setLastHeight(lastValidStatistics.getHeight());
                    statistics.setProtocolVersion(version);
                    statistics.setProtocolVersionMap(proportionMap);
                    //计数统计
                    if (lastValidStatistics.getProtocolVersion().equals(version)) {
                        statistics.setCount((short) (lastValidStatistics.getCount() + 1));
                    } else {
                        statistics.setCount((short) 1);
                    }
                    boolean b = service.save(chainId, statistics);
                    commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statistics-" + statistics);
                    //如果某协议版本连续统计确认数大于阈值，则进行版本升级
                    if (statistics.getCount() >= version.getContinuousIntervalCount()) {
                        //设置新协议版本
                        context.setCurrentProtocolVersion(version);
                        context.setCurrentProtocolVersionCount(statistics.getCount());
                        context.getProtocolVersionHistory().push(version);
                        commonLog.info("chainId-" + chainId + ", height-"+ height + ", new protocol version available-" + version);
                    }
                    context.setCount(0);
                    context.setLastValidStatistics(statistics);
                    //清除旧统计数据
                    proportionMap.clear();
                    return context.getCurrentProtocolVersion().getVersion();
                }
                //已经统计了1000个区块中的400个，但是还没有新协议生效，后面的就不需要统计了
                if (already > interval - (interval * config.getEffectiveRatioMinimum() / 100)) {
                    break;
                }
            }
            //初始化一条旧统计信息，与区块高度绑定，并存到数据库
            Statistics statistics = new Statistics();
            statistics.setHeight(height);
            statistics.setLastHeight(lastValidStatistics.getHeight());
            statistics.setProtocolVersion(currentProtocolVersion);
            statistics.setProtocolVersionMap(proportionMap);
            //计数统计
            statistics.setCount((short) (context.getCurrentProtocolVersionCount() + 1));
            boolean b = service.save(chainId, statistics);
            commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statistics-" + statistics);
            context.setCount(0);
            context.setLastValidStatistics(statistics);
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
        Statistics lastValidStatistics = context.getLastValidStatistics();
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
        ProtocolConfig config = context.getConfig();
        short interval = config.getInterval();
        //区块高度到达阈值，从数据库删除一条统计记录
        if (count < 0) {
            boolean b = service.delete(chainId, height);
            commonLog.info("chainId-" + chainId + ", height-" + height + ", delete-" + b);
            count = interval - 1;
            Statistics newValidStatistics = service.get(chainId, lastValidStatistics.getLastHeight());
            context.setLastValidStatistics(newValidStatistics);
            context.setProportionMap(newValidStatistics.getProtocolVersionMap());
            ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
            if (newValidStatistics.getProtocolVersion().equals(currentProtocolVersion) && newValidStatistics.getCount() < currentProtocolVersion.getContinuousIntervalCount()) {
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
        ProtocolConfig config = context.getConfig();
        byte effectiveRatio = data.getEffectiveRatio();
        if (effectiveRatio > config.getEffectiveRatioMaximum()) {
            return false;
        }
        if (effectiveRatio < config.getEffectiveRatioMinimum()) {
            return false;
        }
        short continuousIntervalCount = data.getContinuousIntervalCount();
        if (continuousIntervalCount > config.getContinuousIntervalCountMaximum()) {
            return false;
        }
        if (continuousIntervalCount < config.getContinuousIntervalCountMinimum()) {
            return false;
        }
        return true;
    }

}
