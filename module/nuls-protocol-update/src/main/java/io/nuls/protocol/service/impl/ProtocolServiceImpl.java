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
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ChainParameters;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.po.ProtocolVersionPo;
import io.nuls.protocol.model.po.StatisticsInfo;
import io.nuls.protocol.rpc.call.BlockCall;
import io.nuls.protocol.rpc.call.VersionChangeNotifier;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.protocol.storage.ProtocolVersionStorageService;
import io.nuls.protocol.storage.StatisticsStorageService;
import io.nuls.protocol.utils.PoUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import static io.nuls.base.data.BlockHeader.BLOCK_HEADER_COMPARATOR;
import static io.nuls.protocol.utils.LoggerUtil.commonLog;

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
    @Autowired
    private ProtocolVersionStorageService protocolService;

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
            context.setLatestHeight(BlockCall.getLatestHeight(chainId));
            List<ProtocolVersionPo> list = protocolService.getList(chainId);
            if (list != null && list.size() > 0) {
                list.sort(ProtocolVersionPo.COMPARATOR.reversed());
                ProtocolVersionPo protocolVersionPo = list.get(0);
                ProtocolVersion protocolVersion = PoUtil.getProtocolVersion(protocolVersionPo);
                context.setCurrentProtocolVersion(protocolVersion);
                var stack = new Stack<ProtocolVersion>();
                stack.addAll(list.stream().map(PoUtil::getProtocolVersion).collect(Collectors.toList()));
                context.setProtocolVersionHistory(stack);
                List<BlockHeader> blockHeaders = BlockCall.getBlockHeaders(chainId, context.getParameters().getInterval());
                context.setProportionMap(initMap(blockHeaders, context, chainId));
                commonLog.info("chainId-" + chainId + ", cached protocol version-" + protocolVersionPo);
            } else {
                //初次启动,初始化一条新协议统计信息,与区块高度绑定,并存到数据库
                ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
                StatisticsInfo statisticsInfo = new StatisticsInfo();
                statisticsInfo.setHeight(0);
                statisticsInfo.setLastHeight(0);
                statisticsInfo.setProtocolVersion(currentProtocolVersion);
                Map<ProtocolVersion, Integer> proportionMap = new HashMap<>(1);
                proportionMap.put(currentProtocolVersion, 1);
                statisticsInfo.setProtocolVersionMap(proportionMap);
                statisticsInfo.setCount((short) 0);
                boolean b = service.save(chainId, statisticsInfo);
                //保存默认协议
                protocolService.save(chainId, PoUtil.getProtocolVersionPo(currentProtocolVersion, 0, 0));
                commonLog.info("chainId-" + chainId + ", height-0, save-" + b + ", new statisticsInfo-" + statisticsInfo);
            }
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

    private Map<ProtocolVersion, Integer> initMap(List<BlockHeader> blockHeaders, ProtocolContext context, int chainId) throws NulsException {
        if (blockHeaders.size() == 0) {
            return new HashMap<>();
        }
        blockHeaders.sort(BLOCK_HEADER_COMPARATOR);
        long latestHeight = blockHeaders.get(blockHeaders.size() - 1).getHeight();
        context.setLatestHeight(latestHeight);
        long l = latestHeight % context.getParameters().getInterval();
        context.setLastValidStatisticsInfo(service.get(chainId, latestHeight - l));
        context.setCount((int) l);
        context.setCurrentProtocolVersionCount(protocolService.getCurrentProtocolVersionCount(chainId));
        Map<ProtocolVersion, Integer> proportionMap = new HashMap<>();
        for (BlockHeader blockHeader : blockHeaders) {
            byte[] extend = blockHeader.getExtend();
            long height = blockHeader.getHeight();
            BlockExtendsData data = new BlockExtendsData();
            data.parse(new NulsByteBuffer(extend));
            if (!validate(data, context)) {
                commonLog.error("chainId-" + chainId + ", invalid block header-" + height);
            } else {
                ProtocolVersion newProtocolVersion = new ProtocolVersion();
                newProtocolVersion.setVersion(data.getBlockVersion());
                newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
                newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
                //重新计算统计信息
                proportionMap.merge(newProtocolVersion, 1, Integer::sum);
            }
        }
        return proportionMap;
    }

    /**
     * 保存创世块，特殊对待
     *
     * @param chainId
     * @param blockHeader
     * @throws NulsException
     */
    private void saveGenesisBlock(int chainId, BlockHeader blockHeader) throws NulsException {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        byte[] extend = blockHeader.getExtend();
        BlockExtendsData data = new BlockExtendsData();
        data.parse(new NulsByteBuffer(extend));
        context.setLatestHeight(0);

        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        ProtocolVersion genesisProtocolVersion = new ProtocolVersion();
        if (!validate(data, context)) {
            commonLog.error("chainId-" + chainId + ", invalid block header-0");
            System.exit(1);
        } else {
            genesisProtocolVersion.setVersion(data.getBlockVersion());
            genesisProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            genesisProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            commonLog.debug("chainId-" + chainId + ", save block, height-0, data-" + data);
            //计算统计信息
            proportionMap.put(genesisProtocolVersion, 1);
        }
        //初始化一条新协议统计信息,与区块高度绑定,并存到数据库
        StatisticsInfo statisticsInfo = new StatisticsInfo();
        statisticsInfo.setHeight(0);
        statisticsInfo.setLastHeight(-1);
        statisticsInfo.setProtocolVersion(genesisProtocolVersion);
        statisticsInfo.setProtocolVersionMap(proportionMap);
        statisticsInfo.setCount((short) 1);

        boolean b = service.save(chainId, statisticsInfo);
        commonLog.info("chainId-" + chainId + ", height-0, save-" + b + ", new statisticsInfo-" + statisticsInfo);
        //设置新协议版本
        context.setCurrentProtocolVersion(genesisProtocolVersion);
        context.setCurrentProtocolVersionCount(statisticsInfo.getCount());
        protocolService.saveCurrentProtocolVersionCount(chainId, context.getCurrentProtocolVersionCount());
        context.getProtocolVersionHistory().push(genesisProtocolVersion);
        VersionChangeNotifier.notify(chainId, genesisProtocolVersion.getVersion());
//        VersionChangeNotifier.reRegister(chainId, context, genesisProtocolVersion.getVersion());
        //保存新协议
        protocolService.save(chainId, PoUtil.getProtocolVersionPo(genesisProtocolVersion, 0, 0));
        commonLog.info("chainId-" + chainId + ", height-0, new protocol version available-" + genesisProtocolVersion);
        context.setCount(0);
        context.setLastValidStatisticsInfo(statisticsInfo);
        //清除旧统计数据
        proportionMap.clear();
    }

    @Override
    public void save(int chainId, BlockHeader blockHeader) throws NulsException {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        StatisticsInfo lastValidStatisticsInfo = context.getLastValidStatisticsInfo();
        byte[] extend = blockHeader.getExtend();
        BlockExtendsData data = new BlockExtendsData();
        data.parse(new NulsByteBuffer(extend));
        long height = blockHeader.getHeight();
        if (height == 0) {
            saveGenesisBlock(chainId, blockHeader);
            return;
        }
        //缓存统计总数+1
        int count = context.getCount();
        count++;
        context.setCount(count);
        context.setLatestHeight(height);

        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
        if (!validate(data, context)) {
            commonLog.error("chainId-" + chainId + ", invalid block header-" + height);
        } else {
            ProtocolVersion newProtocolVersion = new ProtocolVersion();
            newProtocolVersion.setVersion(data.getBlockVersion());
            newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            commonLog.debug("chainId-" + chainId + ", save block, height-" + height + ", data-" + data);
            //重新计算统计信息
            proportionMap.merge(newProtocolVersion, 1, Integer::sum);
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
                //占比超过阈值,保存一条新协议统计记录到数据库
                if (!version.equals(currentProtocolVersion) && real >= expect) {
                    //初始化一条新协议统计信息,与区块高度绑定,并存到数据库
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
                    //如果某协议版本连续统计确认数大于阈值,则进行版本升级
                    if (statisticsInfo.getCount() >= version.getContinuousIntervalCount()) {
                        //设置新协议版本
                        context.setCurrentProtocolVersion(version);
                        context.setCurrentProtocolVersionCount(statisticsInfo.getCount());
                        protocolService.saveCurrentProtocolVersionCount(chainId, context.getCurrentProtocolVersionCount());
                        context.getProtocolVersionHistory().push(version);
                        VersionChangeNotifier.notify(chainId, version.getVersion());
                        VersionChangeNotifier.reRegister(chainId, context, version.getVersion());
                        ProtocolVersionPo oldProtocolVersionPo = protocolService.get(chainId, currentProtocolVersion.getVersion());
                        //旧协议版本失效,更新协议终止高度,并更新
                        oldProtocolVersionPo.setEndHeight(height);
                        protocolService.save(chainId, oldProtocolVersionPo);
                        //保存新协议
                        protocolService.save(chainId, PoUtil.getProtocolVersionPo(version, height + 1, 0));
                        commonLog.info("chainId-" + chainId + ", height-"+ height + ", new protocol version available-" + version);
                    }
                    context.setCount(0);
                    context.setLastValidStatisticsInfo(statisticsInfo);
                    //清除旧统计数据
                    proportionMap.clear();
                    return;
                }
                //已经统计了1000个区块中的400个,但是还没有新协议生效,后面的就不需要统计了
                if (already > interval - (interval * parameters.getEffectiveRatioMinimum() / 100)) {
                    break;
                }
            }
            //初始化一条旧统计信息,与区块高度绑定,并存到数据库
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setHeight(height);
            statisticsInfo.setLastHeight(lastValidStatisticsInfo.getHeight());
            statisticsInfo.setProtocolVersion(currentProtocolVersion);
            statisticsInfo.setProtocolVersionMap(proportionMap);
            //计数统计
            statisticsInfo.setCount((short) (context.getCurrentProtocolVersionCount() + 1));
            context.setCurrentProtocolVersionCount(context.getCurrentProtocolVersionCount() + 1);
            protocolService.saveCurrentProtocolVersionCount(chainId, context.getCurrentProtocolVersionCount());
            boolean b = service.save(chainId, statisticsInfo);
            commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statisticsInfo-" + statisticsInfo);
            context.setCount(0);
            context.setLastValidStatisticsInfo(statisticsInfo);
            //清除旧统计数据
            proportionMap.clear();
        }
    }

    @Override
    public void rollback(int chainId, BlockHeader blockHeader) throws NulsException {
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
        //缓存统计总数==0时,从数据库加载上一条统计记录
        ChainParameters parameters = context.getParameters();
        short interval = parameters.getInterval();
        //区块高度到达阈值,从数据库删除一条统计记录
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
                    VersionChangeNotifier.notify(chainId, protocolVersion.getVersion());
                    VersionChangeNotifier.reRegister(chainId, context, protocolVersion.getVersion());
                    //删除失效协议
                    protocolService.delete(chainId, pop.getVersion());
                    //更新上一个协议的结束高度
                    ProtocolVersionPo protocolVersionPo = protocolService.get(chainId, protocolVersion.getVersion());
                    protocolVersionPo.setEndHeight(0);
                    protocolService.save(chainId, protocolVersionPo);
                    commonLog.info("chainId-" + chainId + ", height-" + height + ", protocol version rollback-" + pop + ", new protocol version available-" + protocolVersion);
                }
            }
        }
        context.setCount(count);
        context.setLatestHeight(height - 1);
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
