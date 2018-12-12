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

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.model.Node;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockDownloadUtils;
import io.nuls.block.utils.BlockUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.DoubleUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 区块下载管理器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午4:25
 */
@Component
@NoArgsConstructor
public class BlockDownloaderManager implements Callable<Boolean> {

    /**
     * 区块下载参数
     */
    private BlockDownloaderParams params;
    /**
     * 用来保存下载到的区块
     */
    private Queue<Block> blockQueue;
    private int chainId;
    private int maxDowncount;
    @Autowired
    private BlockService blockService;
    private NulsThreadFactory factory = new NulsThreadFactory("download-" + chainId);

    public BlockDownloaderManager(int chainId, BlockDownloaderParams params, Queue<Block> blockQueue) {
        this.chainId = chainId;
        this.params = params;
        this.blockQueue = blockQueue;
        this.maxDowncount = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.DOWNLOAD_NUMBER));
    }

    @Override
    public Boolean call() {
        if (!checkLocalBlock()) {
            return false;
        }

        List<Node> nodes = params.getNodes();
        long netLatestHeight = params.getNetLatestHeight();
        Block latestBlock = blockService.getLatestBlock(chainId);
        long latestHeight = latestBlock.getHeader().getHeight();

        int nodeCount = nodes.size();
        ThreadPoolExecutor executor = ThreadUtils.createThreadPool(nodeCount, 0, new NulsThreadFactory("block-downloader-" + chainId));

        List<FutureTask<BlockDownLoadResult>> futures = new ArrayList<>();

        //总共需要下载多少区块
        long totalCount = netLatestHeight - latestHeight;
        int roundDownloads = maxDowncount * nodeCount;
        //需要下载多少轮
        long round = (long) Math.ceil((double) totalCount / (roundDownloads));
        for (long i = 0; i < round; i++) {
            long startHeight = (latestHeight + 1) + i * roundDownloads;
            for (int j = 0; j < nodeCount; j++) {
                long start = startHeight + j * maxDowncount;
                int size = maxDowncount;

                //最后一个节点的下载区块数，特殊计算
                boolean isEnd = false;
                if (start + size > netLatestHeight) {
                    size = (int) (netLatestHeight - start) + 1;
                    isEnd = true;
                }

                int blockCache = Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.BLOCK_CACHE));
                while (blockQueue.size() >= blockCache) {
                    try {
                        Thread.sleep(500L);
                    } catch (InterruptedException e) {
                        Log.error(e);
                    }
                }

                BlockDownloader downloadThread = new BlockDownloader(start, size, chainId, j, nodes.get(j));
                FutureTask<BlockDownLoadResult> downloadThreadFuture = new FutureTask<>(downloadThread);
                executor.execute(factory.newThread(downloadThreadFuture));
                futures.add(downloadThreadFuture);
                if (isEnd) {
                    break;
                }
            }

            for (FutureTask<BlockDownLoadResult> task : futures) {
                BlockDownLoadResult result = null;
                try {
                    result = task.get();
                } catch (Exception e) {
                    Log.error(e);
                }

                if (result == null || !result.isSuccess()) {
                    retryDownload(executor, result);
                }

                List<Block> list = result.getBlockList();
                if (list == null) {
                    executor.shutdown();
                    return true;
                }

                for (Block block : list) {
                    blockQueue.offer(block);
                }
            }
            futures.clear();
        }
        executor.shutdown();
        return true;
    }

    /**
     * 下载失败重试，直到成功为止
     *
     * @param executor
     * @param result
     * @return
     */
    private BlockDownLoadResult retryDownload(ThreadPoolExecutor executor, BlockDownLoadResult result) {
        Log.info("retry download blocks, fail node:{}, start:{}", result.getNode(), result.getStartHeight());
        List<Node> nodes = params.getNodes();
        int index = result.getIndex() + 1 % nodes.size();

        result.setNode(nodes.get(index));
        List<Block> blockList = downloadBlockFromNode(executor, result, index);
        if (blockList != null && blockList.size() > 0) {
            result.setBlockList(blockList);
        }
        return result.isSuccess() ? result : retryDownload(executor, result);
    }

    private List<Block> downloadBlockFromNode(ThreadPoolExecutor executor, BlockDownLoadResult result, int index) {
        BlockDownloader downloadThread = new BlockDownloader(result.getStartHeight(), result.getSize(), chainId, index, result.getNode());
        FutureTask<BlockDownLoadResult> downloadThreadFuture = new FutureTask<>(downloadThread);
        executor.execute(downloadThreadFuture);
        List<Block> blockList = null;
        try {
            blockList = downloadThreadFuture.get().getBlockList();
        } catch (Exception e) {
            Log.error(e);
        }
        return blockList;
    }

    /**
     * 区块同步前，与网络区块作对比，检查本地区块是否需要回滚
     *
     * @return
     */
    private boolean checkLocalBlock() {
        Block localBlock = blockService.getLatestBlock(chainId);
        long localHeight = localBlock.getHeader().getHeight();
        long netHeight = params.getNetLatestHeight();
        //得到共同高度
        long commonHeight = Math.min(localHeight, netHeight);
        if (checkHashEquality(localBlock)) {
            if (commonHeight < netHeight) {
                //commonHeight区块的hash一致，正常，比远程节点落后，下载区块
                return true;
            }
        } else {
            //需要回滚的场景，要满足可用节点数(10个)>配置，一致可用节点数(6个)占比超80%两个条件
            if (params.getNodes().size() >= Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.MIN_NODE_AMOUNT))
                    && DoubleUtils.div(params.getNodes().size(), params.getAvailableNodesCount(), 2) >= Double.parseDouble(ConfigManager.getValue(chainId, ConfigConstant.CONSISTENCY_NODE_PERCENT)) * 100
            ) {
                return checkRollback(localBlock, 0);
            }
        }
        return false;
    }

    private boolean checkRollback(Block localBestBlock, int rollbackCount) {
        //每次最多回滚10个区块，等待下次同步，这样可以避免被恶意节点攻击，大量回滚正常区块。
        if (rollbackCount >= Integer.parseInt(ConfigManager.getValue(chainId, ConfigConstant.MAX_ROLLBACK))) {
            return false;
        }

        blockService.rollbackBlock(chainId, BlockUtil.toBlockHeaderPo(localBestBlock));
        localBestBlock = blockService.getLatestBlock(chainId);
        if (checkHashEquality(localBestBlock)) {
            return true;
        }

        return checkRollback(localBestBlock, rollbackCount + 1);
    }

    /**
     * 根据传入的区块localBlock判断localBlock.hash与网络上同高度的区块hash是否一致
     *
     * @author captain
     * @date 18-11-9 下午6:13
     * @version 1.0
     */
    private boolean checkHashEquality(Block localBlock) {
        NulsDigestData localHash = localBlock.getHeader().getHash();
        long localHeight = localBlock.getHeader().getHeight();
        long netHeight = params.getNetLatestHeight();
        //得到共同高度
        long commonHeight = Math.min(localHeight, netHeight);
        NulsDigestData remoteHash = params.getNetLatestHash();
        //如果双方共同高度<网络高度，要进行hash判断，需要从网络上下载区块，因为params里只有最新的区块hash，没有旧的hash
        if (commonHeight < netHeight) {
            for (Node node : params.getNodes()) {
                Block remoteBlock = BlockDownloadUtils.getBlockByHash(chainId, localHash, node);
                remoteHash = remoteBlock.getHeader().getHash();
                break;
            }
        }
        return localHash.equals(remoteHash);
    }

}
