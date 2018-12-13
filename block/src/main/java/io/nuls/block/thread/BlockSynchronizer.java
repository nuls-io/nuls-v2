/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.block.thread;

import com.google.common.collect.Lists;
import io.nuls.base.data.Block;
import io.nuls.block.constant.BlockSynStatusEnum;
import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Node;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.TimeService;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 区块同步主线程，管理多条链的区块同步
 * @author captain
 * @date 18-11-8 下午5:49
 * @version 1.0
 */
@Component
@NoArgsConstructor
public class BlockSynchronizer implements Runnable {

    /**
     * 保存多条链的区块同步状态
     */
    private Map<Integer, BlockSynStatusEnum> statusEnumMap = new ConcurrentHashMap<>();

    private static final BlockSynchronizer INSTANCE = new BlockSynchronizer();

    @Autowired
    private BlockService blockService;

    public static BlockSynchronizer getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        for (Integer chainId : ContextManager.chainIds) {
            try {
                BlockSynStatusEnum synStatus = statusEnumMap.get(chainId);
                if (synStatus == null) {
                    statusEnumMap.put(chainId, synStatus = BlockSynStatusEnum.WAITING);
                }
                RunningStatusEnum runningStatus = ContextManager.getContext(chainId).getStatus();
                if (synStatus.equals(BlockSynStatusEnum.WAITING) && runningStatus.equals(RunningStatusEnum.RUNNING)) {
                    synchronize(chainId);
                } else {
                    Log.info("skip Block Synchronize, SynStatus:{}, RunningStatus:{}", synStatus, runningStatus);
                }
            } catch (Exception error) {
                Log.error(error);
            }
        }
    }

    private void synchronize(int chainId) {
        //1.调用网络模块接口获取当前chainID网络的可用节点
        List<Node> availableNodes = NetworkUtil.getAvailableNodes(chainId);

        //2.判断可用节点数是否满足最小配置
        String config = ConfigManager.getValue(chainId, ConfigConstant.MIN_NODE_AMOUNT);
        if (availableNodes.size() >= Integer.parseInt(config)) {
            //3.统计网络中可用节点的一致区块高度、区块hash
            BlockDownloaderParams params = statistics(availableNodes, chainId);
            int size = params.getNodes().size();
            if (size == 0) {
                statusEnumMap.put(chainId, BlockSynStatusEnum.FAIL);
                return;
            }
            //网络上所有节点高度都是0，说明是该链第一次运行
            if (params.getNetLatestHeight() == 0 && size == availableNodes.size()) {
                statusEnumMap.put(chainId, BlockSynStatusEnum.SUCCESS);
                return;
            }
            //4.更新下载状态为“下载中”
            statusEnumMap.put(chainId, BlockSynStatusEnum.RUNNING);
            BlockingQueue<Block> blockQueue = new LinkedBlockingQueue<>();

            //5.开启区块下载管理器BlockDownloaderManager
            BlockDownloaderManager downloadThreadManager = new BlockDownloaderManager(chainId, params, blockQueue);
            FutureTask<Boolean> threadManagerFuture = new FutureTask<>(downloadThreadManager);
            ThreadUtils.createAndRunThread("block-downloader-manager-" + chainId, threadManagerFuture);

            //6.开启区块消费线程BlockConsumer，与上面的BlockDownloaderManager共用一个队列blockQueue
            BlockConsumer blockConsumer = new BlockConsumer(chainId, blockQueue);
            FutureTask<Boolean> dataStorageFuture = new FutureTask<>(blockConsumer);
            ThreadUtils.createAndRunThread("block-consumer-" + chainId, dataStorageFuture);

            try {
                Boolean downResult = threadManagerFuture.get();
                blockQueue.offer(new Block());
                Boolean storageResult = dataStorageFuture.get();
                boolean success = downResult != null && downResult && storageResult != null && storageResult;

                if (success && checkIsNewest(chainId, params)) {
                    statusEnumMap.put(chainId, BlockSynStatusEnum.SUCCESS);
                } else if (BlockSynStatusEnum.WAITING.equals(statusEnumMap.get(chainId))) {
                    statusEnumMap.put(chainId, BlockSynStatusEnum.FAIL);
                }
            } catch (Exception e) {
                Log.error(e);
                statusEnumMap.put(chainId, BlockSynStatusEnum.FAIL);
            }

        }
    }

    /**
     * 检查本地区块是否同步到最新高度，如果不是最新高度，变更同步状态为BlockSynStatusEnum.WAITING，等待下次同步
     * @param chainId
     * @param params
     * @return
     * @throws Exception
     */
    private boolean checkIsNewest(int chainId, BlockDownloaderParams params) throws Exception {

        long downloadBestHeight = params.getNetLatestHeight();
        long time = TimeService.currentTimeMillis();
        long timeout = 60 * 1000L;
        long localBestHeight = 0L;

        while (true) {
            if (TimeService.currentTimeMillis() - time > timeout) {
                break;
            }

            long bestHeight = blockService.getLatestBlock(chainId).getHeader().getHeight();
            if (bestHeight >= downloadBestHeight) {
                break;
            } else if (bestHeight != localBestHeight) {
                localBestHeight = bestHeight;
                time = TimeService.currentTimeMillis();
            }
            Thread.sleep(100L);
        }

        BlockDownloaderParams newestParams = statistics(chainId);
        if (newestParams.getNetLatestHeight() > blockService.getLatestBlock(chainId).getHeader().getHeight()) {
            statusEnumMap.put(chainId, BlockSynStatusEnum.WAITING);
            return false;
        }
        return true;
    }

    public BlockDownloaderParams statistics(int chainId) {
        return statistics(NetworkUtil.getAvailableNodes(chainId), chainId);
    }

    /**
     * 统计网络中可用节点的一致区块高度、区块hash，构造下载参数
     * @date 18-11-8 下午4:55
     * @param
     * @return
     */
    public BlockDownloaderParams statistics(List<Node> availableNodes, int chainId) {
        BlockDownloaderParams params = new BlockDownloaderParams();
        params.setAvailableNodesCount(availableNodes.size());
        List<Node> nodeList = new ArrayList<>();
        params.setNodes(nodeList);
        //每个节点的(最新HASH+最新高度)是key
        String key = "";
        int count = 0;
        //一个以key为主键记录持有该key的节点列表
        Map<String, List<Node>> nodeMap = new HashMap<>(availableNodes.size());
        //一个以key为主键统计次数
        Map<String, Integer> countMap = new HashMap<>(availableNodes.size());
        for (Node node: availableNodes){
            String tempKey = node.getHash().getDigestHex() + node.getHeight();
            if (countMap.containsKey(tempKey)) {
                //tempKey已存在，统计次数加1
                countMap.put(tempKey, countMap.get(tempKey) + 1);
            } else {
                //tempKey不存在，初始化统计次数
                countMap.put(tempKey, 1);
            }

            if (nodeMap.containsKey(tempKey)) {
                //tempKey已存在，添加到持有节点列表中
                List<Node> nodes = nodeMap.get(tempKey);
                nodes.add(node);
            } else {
                //tempKey不存在，新增持有节点列表
                nodeMap.put(tempKey, Lists.newArrayList(node));
            }
        }

        //最终统计出出现频率最大的key，就获取到当前可信的最新高度与最新hash，以及可信的节点列表
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            Integer value = entry.getValue();
            if (value > count) {
                count = value;
                key = entry.getKey();
            }
        }

        int config = availableNodes.size() * Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.CONSISTENCY_NODE_PERCENT)) / 100;
        if (count >= config) {
            nodeList = nodeMap.get(key);
            params.setNetLatestHash(nodeList.get(0).getHash());
            params.setNetLatestHeight(nodeList.get(0).getHeight());
        }
        return params;
    }

}
