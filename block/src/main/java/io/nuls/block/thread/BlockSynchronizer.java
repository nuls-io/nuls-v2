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
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.model.Node;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockDownloadUtils;
import io.nuls.block.utils.module.ConsensusUtil;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.StampedLock;

import static io.nuls.block.constant.Constant.CONSENSUS_WORKING;
import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * 区块同步主线程,管理多条链的区块同步
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午5:49
 */
public class BlockSynchronizer implements Runnable {

    private static final BlockSynchronizer INSTANCE = new BlockSynchronizer();
    /**
     * 保存多条链的区块同步状态
     */
    private BlockService blockService;

    private BlockSynchronizer() {this.blockService = SpringLiteContext.getBean(BlockService.class);}

    public static BlockSynchronizer getInstance() {
        return INSTANCE;
    }

    @Override
    public void run() {
        for (Integer chainId : ContextManager.chainIds) {
            try {
                while (true) {
                    if (synchronize(chainId)) {
                        break;
                    }
                    Thread.sleep(1000L);
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.error(e);
            }
        }
    }

    private boolean synchronize(int chainId) throws Exception {
        //1.调用网络模块接口获取当前chainID网络的可用节点
        List<Node> availableNodes = NetworkUtil.getAvailableNodes(chainId);

        //2.判断可用节点数是否满足最小配置
        ChainContext context = ContextManager.getContext(chainId);
        ChainParameters parameters = context.getParameters();
        int minNodeAmount = parameters.getMinNodeAmount();
        if (availableNodes.size() >= minNodeAmount) {
            //3.统计网络中可用节点的一致区块高度、区块hash
            BlockDownloaderParams params = statistics(availableNodes, context);
            int size = params.getNodes().size();
            //网络上没有可用节点
            if (size == 0) {
                Log.warn("chain-" + chainId + ", no consistent nodes");
                return false;
            }
            //网络上所有节点高度都是0,说明是该链第一次运行
            if (params.getNetLatestHeight() == 0 && size == availableNodes.size()) {
                Log.warn("chain-" + chainId + ", first start");
                context.setStatus(RunningStatusEnum.RUNNING);
                ConsensusUtil.notice(chainId, CONSENSUS_WORKING);
                return true;
            }
            //检查本地区块状态
            if (!checkLocalBlock(chainId, params)) {
                Log.warn("chain-" + chainId + ", local blocks is newest");
                context.setStatus(RunningStatusEnum.RUNNING);
                ConsensusUtil.notice(chainId, CONSENSUS_WORKING);
                return true;
            }
            context.setStatus(RunningStatusEnum.SYNCHRONIZING);
            PriorityBlockingQueue<Node> nodes = params.getNodes();
            int nodeCount = nodes.size();
            ThreadPoolExecutor executor = ThreadUtils.createThreadPool(nodeCount, 0, new NulsThreadFactory("worker-" + chainId));
            BlockingQueue<Block> queue = new LinkedBlockingQueue<>();
            BlockingQueue<Future<BlockDownLoadResult>> futures = new LinkedBlockingQueue<>();
            long netLatestHeight = params.getNetLatestHeight();
            long startHeight = params.getLocalLatestHeight() + 1;
            long total = netLatestHeight - startHeight + 1;
            long start = System.currentTimeMillis();
            //5.开启区块下载器BlockDownloader
            BlockDownloader downloader = new BlockDownloader(chainId, futures, executor, params, queue);
            Future<Boolean> downloadFutrue = ThreadUtils.asynExecuteCallable(downloader);

            //6.开启区块收集线程BlockCollector,收集BlockDownloader下载的区块
            BlockCollector collector = new BlockCollector(chainId, futures, executor, params, queue);
            ThreadUtils.createAndRunThread("block-collector-" + chainId, collector);

            //7.开启区块消费线程BlockConsumer,与上面的BlockDownloader共用一个队列blockQueue
            BlockConsumer consumer = new BlockConsumer(chainId, queue, params);
            Future<Boolean> consumerFuture = ThreadUtils.asynExecuteCallable(consumer);

            Boolean downResult = downloadFutrue.get();
            Boolean storageResult = consumerFuture.get();
            boolean success = downResult != null && downResult && storageResult != null && storageResult;
            long end = System.currentTimeMillis();
            Log.info("block syn complete, total download:" + total + ", total time:" + (end - start) + ", average time:" + (end - start) / total);
            if (success) {
                if (true) {
                    Log.info("block syn complete successfully, current height-" + params.getNetLatestHeight());
                    context.setStatus(RunningStatusEnum.RUNNING);
                    ConsensusUtil.notice(chainId, CONSENSUS_WORKING);
                    return true;
                } else {
                    Log.info("block syn complete but is not newest");
                }
            } else {
                Log.info("block syn fail, downResult:" + downResult + ", storageResult:" + storageResult);
            }
        } else {
            Log.warn("chain-" + chainId + ", available nodes not enough");
        }
        return false;
    }

    /**
     * 检查本地区块是否同步到最新高度,如果不是最新高度,变更同步状态为BlockSynStatusEnum.WAITING,等待下次同步
     *
     * @param chainId
     * @param params
     * @param context
     * @return
     * @throws Exception
     */
    private boolean checkIsNewest(int chainId, BlockDownloaderParams params, ChainContext context) throws Exception {

        long downloadBestHeight = params.getNetLatestHeight();
        long time = NetworkUtil.currentTime();
        long timeout = 60 * 1000L;
        long localBestHeight = 0L;

        while (true) {
            if (NetworkUtil.currentTime() - time > timeout) {
                break;
            }

            long bestHeight = blockService.getLatestBlock(chainId).getHeader().getHeight();
            if (bestHeight >= downloadBestHeight) {
                break;
            } else if (bestHeight != localBestHeight) {
                localBestHeight = bestHeight;
                time = NetworkUtil.currentTime();
            }
            Thread.sleep(100L);
        }

        BlockDownloaderParams newestParams = statistics(NetworkUtil.getAvailableNodes(chainId), context);
        if (newestParams.getNetLatestHeight() > blockService.getLatestBlock(chainId).getHeader().getHeight()) {
            return false;
        }
        return true;
    }

    /**
     * 统计网络中可用节点的一致区块高度、区块hash,构造下载参数
     *
     * @param
     * @param context
     * @return
     * @date 18-11-8 下午4:55
     */
    public BlockDownloaderParams statistics(List<Node> availableNodes, ChainContext context) {
        BlockDownloaderParams params = new BlockDownloaderParams();
        params.setAvailableNodesCount(availableNodes.size());
        PriorityBlockingQueue<Node> nodeQueue = new PriorityBlockingQueue<>(availableNodes.size(), Node.COMPARATOR);
        params.setNodes(nodeQueue);
        //每个节点的(最新HASH+最新高度)是key
        String key = "";
        int count = 0;
        //一个以key为主键记录持有该key的节点列表
        Map<String, List<Node>> nodeMap = new HashMap<>(availableNodes.size());
        //一个以key为主键统计次数
        Map<String, Integer> countMap = new HashMap<>(availableNodes.size());
        for (Node node : availableNodes) {
            String tempKey = node.getHash().getDigestHex() + node.getHeight();
            if (countMap.containsKey(tempKey)) {
                //tempKey已存在,统计次数加1
                countMap.put(tempKey, countMap.get(tempKey) + 1);
            } else {
                //tempKey不存在,初始化统计次数
                countMap.put(tempKey, 1);
            }

            if (nodeMap.containsKey(tempKey)) {
                //tempKey已存在,添加到持有节点列表中
                List<Node> nodes = nodeMap.get(tempKey);
                nodes.add(node);
            } else {
                //tempKey不存在,新增持有节点列表
                nodeMap.put(tempKey, Lists.newArrayList(node));
            }
        }

        //最终统计出出现频率最大的key,就获取到当前可信的最新高度与最新hash,以及可信的节点列表
        for (Map.Entry<String, Integer> entry : countMap.entrySet()) {
            Integer value = entry.getValue();
            if (value > count) {
                count = value;
                key = entry.getKey();
            }
        }

        ChainParameters parameters = context.getParameters();
        int config = availableNodes.size() * parameters.getConsistencyNodePercent() / 100;
        if (count >= config) {
            nodeQueue.addAll(nodeMap.get(key));
            Node node = nodeQueue.peek();
            params.setNetLatestHash(node.getHash());
            params.setNetLatestHeight(node.getHeight());
        }

        // a read-only method
        // upgrade from optimistic read to read lock
        StampedLock lock = context.getLock();
        long stamp = lock.tryOptimisticRead();
        try {
            for (; ; stamp = lock.readLock()) {
                if (stamp == 0L) {
                    continue;
                }
                // possibly racy reads
                params.setLocalLatestHeight(context.getLatestHeight());
                params.setLocalLatestHash(context.getLatestBlock().getHeader().getHash());
                if (!lock.validate(stamp)) {
                    continue;
                }
                return params;
            }
        } finally {
            if (StampedLock.isReadLockStamp(stamp)) {
                lock.unlockRead(stamp);
            }
        }
    }

    /**
     * 区块同步前,与网络区块作对比,检查本地区块是否需要回滚
     *
     * @param chainId
     * @param params
     * @return
     */
    private boolean checkLocalBlock(int chainId, BlockDownloaderParams params) {
        long localHeight = params.getLocalLatestHeight();
        long netHeight = params.getNetLatestHeight();
        //得到共同高度
        long commonHeight = Math.min(localHeight, netHeight);
        if (checkHashEquality(chainId, params)) {
            if (commonHeight < netHeight) {
                //commonHeight区块的hash一致,正常,比远程节点落后,下载区块
                return true;
            }
        } else {
            //需要回滚的场景,要满足可用节点数(10个)>配置,一致可用节点数(6个)占比超80%两个条件
            ChainParameters parameters = ContextManager.getContext(chainId).getParameters();
            if (params.getNodes().size() >= parameters.getMinNodeAmount()
                    && params.getAvailableNodesCount() >= params.getNodes().size() * parameters.getConsistencyNodePercent() / 100
            ) {
                return checkRollback(0, chainId, params);
            }
        }
        return false;
    }

    private boolean checkRollback(int rollbackCount, int chainId, BlockDownloaderParams params) {
        //每次最多回滚10个区块,等待下次同步,这样可以避免被恶意节点攻击,大量回滚正常区块.
        ChainParameters parameters = ContextManager.getContext(chainId).getParameters();
        if (params.getLocalLatestHeight() == 0 || rollbackCount >= parameters.getMaxRollback()) {
            return false;
        }
        blockService.rollbackBlock(chainId, params.getLocalLatestHeight(), true);
        BlockHeader latestBlockHeader = blockService.getLatestBlockHeader(chainId);
        params.setLocalLatestHeight(latestBlockHeader.getHeight());
        params.setLocalLatestHash(latestBlockHeader.getHash());
        if (checkHashEquality(chainId, params)) {
            return true;
        }
        return checkRollback(rollbackCount + 1, chainId, params);
    }

    /**
     * 根据传入的区块localBlock判断localBlock.hash与网络上同高度的区块hash是否一致
     *
     * @author captain
     * @date 18-11-9 下午6:13
     * @version 1.0
     */
    private boolean checkHashEquality(int chainId, BlockDownloaderParams params) {
        NulsDigestData localHash = params.getLocalLatestHash();
        long localHeight = params.getLocalLatestHeight();
        long netHeight = params.getNetLatestHeight();
        NulsDigestData netHash = params.getNetLatestHash();
        //得到共同高度
        long commonHeight = Math.min(localHeight, netHeight);
        //如果双方共同高度<网络高度,要进行hash判断,需要从网络上下载区块,因为params里只有最新的区块hash,没有旧的hash
        if (commonHeight < netHeight) {
            for (Node node : params.getNodes()) {
                Block remoteBlock = BlockDownloadUtils.getBlockByHash(chainId, localHash, node);
                if (remoteBlock != null) {
                    netHash = remoteBlock.getHeader().getHash();
                    return localHash.equals(netHash);
                }
            }
            //如果从网络上下载区块失败，返回false
            return false;
        }
        if (commonHeight < localHeight) {
            localHash = blockService.getBlockHash(chainId, commonHeight);
        }
        return localHash.equals(netHash);
    }
}
