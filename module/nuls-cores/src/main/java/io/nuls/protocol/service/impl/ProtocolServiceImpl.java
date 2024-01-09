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

import io.nuls.base.basic.ProtocolVersion;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.common.ConfigBean;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.po.ProtocolVersionPo;
import io.nuls.protocol.model.po.StatisticsInfo;
import io.nuls.protocol.rpc.call.BlockCall;
import io.nuls.protocol.rpc.call.VersionChangeNotifier;
import io.nuls.protocol.service.ProtocolService;
import io.nuls.protocol.storage.ProtocolVersionStorageService;
import io.nuls.protocol.storage.StatisticsStorageService;
import io.nuls.protocol.utils.PoUtil;

import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.base.data.BlockHeader.BLOCK_HEADER_COMPARATOR;

/**
 * Block service implementation class
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 morning11:09
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
        NulsLogger logger = context.getLogger();
        try {
            context.setLatestHeight(BlockCall.getLatestHeight(chainId));
            List<ProtocolVersionPo> list = protocolService.getList(chainId);
            list.sort(ProtocolVersionPo.COMPARATOR.reversed());
            ProtocolVersionPo protocolVersionPo = list.get(0);
            ProtocolVersion protocolVersion = PoUtil.getProtocolVersion(protocolVersionPo);
            System.out.println("---------init currentVersion----------," + protocolVersion.getVersion());
            context.setCurrentProtocolVersion(protocolVersion);
            VersionChangeNotifier.notify(chainId, protocolVersion.getVersion());
            VersionChangeNotifier.reRegister(chainId, context, protocolVersion.getVersion());
            var stack = list.stream().map(PoUtil::getProtocolVersion).collect(Collectors.toCollection(ArrayDeque::new));
            context.setProtocolVersionHistory(stack);
            long latestHeight = BlockCall.getLatestHeight(chainId);
            context.setLatestHeight(latestHeight);
            long l = latestHeight % context.getParameters().getInterval();
            context.setLastValidStatisticsInfo(service.get(chainId, latestHeight - l));
            context.setCount((int) l);
            context.setCurrentProtocolVersionCount(protocolService.getCurrentProtocolVersionCount(chainId));
            List<BlockHeader> blockHeaders = BlockCall.getBlockHeaders(chainId, context.getParameters().getInterval());
            context.setProportionMap(initMap(blockHeaders));
            logger.info("cached protocol version-" + protocolVersionPo);
        } catch (Exception e) {
            logger.error(e);
        }
    }

    private Map<ProtocolVersion, Integer> initMap(List<BlockHeader> blockHeaders) {
        if (blockHeaders.isEmpty()) {
            return new HashMap<>();
        }
        blockHeaders.sort(BLOCK_HEADER_COMPARATOR);
        Map<ProtocolVersion, Integer> proportionMap = new HashMap<>();
        for (BlockHeader blockHeader : blockHeaders) {
            BlockExtendsData data = blockHeader.getExtendsData();
            ProtocolVersion newProtocolVersion = new ProtocolVersion();
            newProtocolVersion.setVersion(data.getBlockVersion());
            newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            //Recalculate statistics
            proportionMap.merge(newProtocolVersion, 1, Integer::sum);

        }
        return proportionMap;
    }

    /**
     * Save the Genesis Block, Special Treatment
     *
     * @param chainId
     * @param blockHeader
     */
    private boolean saveGenesisBlock(int chainId, BlockHeader blockHeader) {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        BlockExtendsData data = blockHeader.getExtendsData();
        context.setLatestHeight(0);

        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        ProtocolVersion genesisProtocolVersion = new ProtocolVersion();
        genesisProtocolVersion.setVersion(data.getBlockVersion());
        genesisProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
        genesisProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
        logger.debug("save block, height-0, data-" + data);
        //Calculate statistical information
        proportionMap.put(genesisProtocolVersion, 1);

        //Initialize a new protocol statistics information,Highly bound to blocks,Coexist in database
        StatisticsInfo statisticsInfo = new StatisticsInfo();
        statisticsInfo.setHeight(0);
        statisticsInfo.setProtocolVersion(genesisProtocolVersion);
        statisticsInfo.setProtocolVersionMap(proportionMap);
        statisticsInfo.setCount((short) 1);

        boolean b = service.save(chainId, statisticsInfo);
        logger.info("height-0, save-" + b + ", new statisticsInfo-" + statisticsInfo);
        //Set new protocol version
        context.setCurrentProtocolVersion(genesisProtocolVersion);
        System.out.println("---------genesisProtocolVersion----------," + genesisProtocolVersion.getVersion());
        context.setCurrentProtocolVersionCount(statisticsInfo.getCount());
        protocolService.saveCurrentProtocolVersionCount(chainId, context.getCurrentProtocolVersionCount());
        VersionChangeNotifier.notify(chainId, genesisProtocolVersion.getVersion());
        //Save new agreement
        protocolService.save(chainId, PoUtil.getProtocolVersionPo(genesisProtocolVersion, 0, 0));
        logger.info("height-0, new protocol version available-" + genesisProtocolVersion);
        context.setCount(0);
        context.setLastValidStatisticsInfo(statisticsInfo);
        //Clear old statistical data
        proportionMap.clear();
        return true;
    }

    @Override
    public boolean save(int chainId, BlockHeader blockHeader) {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        StatisticsInfo lastValidStatisticsInfo = context.getLastValidStatisticsInfo();
        BlockExtendsData data = blockHeader.getExtendsData();
        long height = blockHeader.getHeight();
        if (height == 0) {
            return saveGenesisBlock(chainId, blockHeader);
        }
        //Total cache statistics+1
        int count = context.getCount();
        count++;
        context.setCount(count);
        context.setLatestHeight(height);

        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
        if (!validate(data, context)) {
            logger.error("invalid block header-" + height);
            logger.error("currentProtocolVersion-" + currentProtocolVersion);
            logger.error("data-" + data);
            return false;
        } else {
            ProtocolVersion newProtocolVersion = new ProtocolVersion();
            newProtocolVersion.setVersion(data.getBlockVersion());
            newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            logger.info("save block, height-" + height + ", data-" + data);
            //Recalculate statistics
            proportionMap.merge(newProtocolVersion, 1, Integer::sum);
        }
        ConfigBean parameters = context.getParameters();
        short interval = parameters.getInterval();
        //each1000Perform a block count once
        if (count == interval) {
            int already = 0;
            Map<Short, ProtocolVersion> localVersionMap = getLocalVersionMap(context);
            for (Map.Entry<ProtocolVersion, Integer> entry : proportionMap.entrySet()) {
                //This is the protocol configuration object contained in the blocks transmitted over the network,IflocalProtocolVersion==null,When using this object for statistics, it indicates that there is no corresponding configuration for the block version number sent from the network locally
                ProtocolVersion netProtocolVersion = entry.getKey();
                //This is the protocol configuration object read from the local configuration file based on the version number,Prioritize using this object for statistics, indicating that the block version number sent from the network has a corresponding configuration locally
                ProtocolVersion localProtocolVersion = localVersionMap.get(netProtocolVersion.getVersion());
                //The configuration information that is truly used for statistics
                ProtocolVersion statictisProtocolVersion = localProtocolVersion == null ? netProtocolVersion : localProtocolVersion;
                int real = entry.getValue();
                already += real;
                int expect = interval * statictisProtocolVersion.getEffectiveRatio() / 100;
                //The proportion exceeds the threshold,Save a new protocol statistics record to the database
                if (!statictisProtocolVersion.equals(currentProtocolVersion) && real >= expect) {
                    //Initialize a new protocol statistics information,Highly bound to blocks,Coexist in database
                    StatisticsInfo statisticsInfo = new StatisticsInfo();
                    statisticsInfo.setHeight(height);
                    statisticsInfo.setProtocolVersion(statictisProtocolVersion);
                    statisticsInfo.setProtocolVersionMap(proportionMap);
                    //Count statistics
                    if (lastValidStatisticsInfo.getProtocolVersion().equals(statictisProtocolVersion)) {
                        statisticsInfo.setCount((short) (lastValidStatisticsInfo.getCount() + 1));
                    } else {
                        statisticsInfo.setCount((short) 1);
                    }
                    boolean b = service.save(chainId, statisticsInfo);
                    logger.info("height-" + height + ", save-" + b + ", new statisticsInfo-" + statisticsInfo);
                    //If the number of consecutive confirmation counts for a certain protocol version exceeds the threshold,Upgrade the version accordingly
                    if (statisticsInfo.getCount() >= statictisProtocolVersion.getContinuousIntervalCount() && statictisProtocolVersion.getVersion() > currentProtocolVersion.getVersion()) {
                        short localVersion = context.getLocalProtocolVersion().getVersion();
                        if (statictisProtocolVersion.getVersion() > localVersion) {
                            logger.error("localVersion-" + localVersion);
                            logger.error("newVersion-" + statictisProtocolVersion.getVersion());
                            logger.error("Older versions of the wallet automatically stop working, Please upgrade the latest version of the wallet!");
                            System.exit(1);
                        }
                        //Set new protocol version
                        context.setCurrentProtocolVersion(statictisProtocolVersion);
                        context.setCurrentProtocolVersionCount(statisticsInfo.getCount());
                        protocolService.saveCurrentProtocolVersionCount(chainId, context.getCurrentProtocolVersionCount());
                        context.getProtocolVersionHistory().push(statictisProtocolVersion);
                        VersionChangeNotifier.notify(chainId, statictisProtocolVersion.getVersion());
                        VersionChangeNotifier.reRegister(chainId, context, statictisProtocolVersion.getVersion());
                        ProtocolVersionPo oldProtocolVersionPo = protocolService.get(chainId, currentProtocolVersion.getVersion());
                        //Old protocol version invalid,Update the termination height of the agreement,And update
                        oldProtocolVersionPo.setEndHeight(height);
                        protocolService.save(chainId, oldProtocolVersionPo);
                        //Save new agreement
                        protocolService.save(chainId, PoUtil.getProtocolVersionPo(statictisProtocolVersion, height + 1, 0));
                        logger.info("height-" + height + ", new protocol version available-" + statictisProtocolVersion);
                    }
                    context.setCount(0);
                    context.setLastValidStatisticsInfo(statisticsInfo);
                    //Clear old statistical data
                    proportionMap.clear();
                    return true;
                }
                //It has been counted1000In blocks400individual,But the new agreement has not yet come into effect,The rest doesn't need to be counted anymore
                if (already > interval - (interval * parameters.getEffectiveRatioMinimum() / 100)) {
                    break;
                }
            }
            //Initialize an old statistical information,Highly bound to blocks,Coexist in database
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setHeight(height);
            statisticsInfo.setProtocolVersion(currentProtocolVersion);
            statisticsInfo.setProtocolVersionMap(proportionMap);
            //Count statistics
            statisticsInfo.setCount((short) (context.getCurrentProtocolVersionCount() + 1));
            context.setCurrentProtocolVersionCount(context.getCurrentProtocolVersionCount() + 1);
            protocolService.saveCurrentProtocolVersionCount(chainId, context.getCurrentProtocolVersionCount());
            boolean b = service.save(chainId, statisticsInfo);
            logger.info("height-" + height + ", save-" + b + ", new statisticsInfo-" + statisticsInfo);
            context.setCount(0);
            context.setLastValidStatisticsInfo(statisticsInfo);
            //Clear old statistical data
            proportionMap.clear();
        }
        return true;
    }

    private Map<Short, ProtocolVersion> getLocalVersionMap(ProtocolContext context) {
        Map<Short, ProtocolVersion> map = new HashMap<>();
        context.getLocalVersionList().forEach(e -> map.put(e.getVersion(), e));
        return map;
    }

    @Override
    public boolean rollback(int chainId, BlockHeader blockHeader) {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        BlockExtendsData data = blockHeader.getExtendsData();
        long height = blockHeader.getHeight();
        //Total cache statistics-1
        int count = context.getCount();
        count--;
        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        ProtocolVersion newProtocolVersion = new ProtocolVersion();
        if (!validate(data, context)) {
            logger.error("invalid block header-" + height);
            logger.error("currentProtocolVersion-" + context.getCurrentProtocolVersion());
            logger.error("data-" + data);
            return false;
        } else {
            newProtocolVersion.setVersion(data.getBlockVersion());
            newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            logger.info("rollback block, height-" + height + ", protocol-" + newProtocolVersion);
            //Recalculate statistics
            proportionMap.merge(newProtocolVersion, 1, (a, b) -> a - b);
        }
        //Total cache statistics==0Time,Load the previous statistical record from the database
        ConfigBean parameters = context.getParameters();
        short interval = parameters.getInterval();
        //Block height reaches threshold,Delete a statistical record from the database
        if (count < 0) {
            StatisticsInfo oldValidStatisticsInfo = service.get(chainId, height);
            boolean b = service.delete(chainId, height);
            logger.info("height-" + height + ", delete-" + b);
            count = interval - 1;
            StatisticsInfo newValidStatisticsInfo = service.get(chainId, height - interval);
            context.setLastValidStatisticsInfo(newValidStatisticsInfo);
            context.setProportionMap(oldValidStatisticsInfo.getProtocolVersionMap());
            context.getProportionMap().merge(newProtocolVersion, 1, (x, y) -> x - y);
            context.setCurrentProtocolVersionCount(context.getCurrentProtocolVersionCount() - 1);
            protocolService.saveCurrentProtocolVersionCount(chainId, context.getCurrentProtocolVersionCount());
            ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
            if (newValidStatisticsInfo.getProtocolVersion().equals(currentProtocolVersion) && newValidStatisticsInfo.getCount() < currentProtocolVersion.getContinuousIntervalCount()) {
                //Set new protocol version
                Deque<ProtocolVersion> history = context.getProtocolVersionHistory();
                if (history.size() > 1) {
                    ProtocolVersion pop = history.pop();
                    ProtocolVersion protocolVersion = history.peek();
                    context.setCurrentProtocolVersion(protocolVersion);
                    VersionChangeNotifier.notify(chainId, protocolVersion.getVersion());
                    VersionChangeNotifier.reRegister(chainId, context, protocolVersion.getVersion());
                    //Delete Invalid Protocol
                    protocolService.delete(chainId, pop.getVersion());
                    //Update the end height of the previous agreement
                    ProtocolVersionPo protocolVersionPo = protocolService.get(chainId, protocolVersion.getVersion());
                    protocolVersionPo.setEndHeight(0);
                    protocolService.save(chainId, protocolVersionPo);
                    logger.info("height-" + height + ", protocol version rollback-" + pop + ", new protocol version available-" + protocolVersion);
                }
            }
        }
        context.setCount(count);
        context.setLatestHeight(height - 1);
        return true;
    }

    /**
     * Verify the correctness of block header protocol information
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
        ConfigBean parameters = context.getParameters();
        byte effectiveRatio = data.getEffectiveRatio();
        if (effectiveRatio < parameters.getEffectiveRatioMinimum()) {
            return false;
        }
        short continuousIntervalCount = data.getContinuousIntervalCount();
        return continuousIntervalCount >= parameters.getContinuousIntervalCountMinimum();
    }

}
