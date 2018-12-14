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
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Node;
import io.nuls.block.service.BlockService;
import io.nuls.tools.log.Log;

import java.util.List;
import java.util.concurrent.*;

/**
 * 消费同步到的区块
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午5:45
 */
public class BlockConsumer implements Callable<Boolean> {

    /**
     * 区块下载参数
     */
    private BlockDownloaderParams params;
    private ThreadPoolExecutor executor;
    private List<Future<BlockDownLoadResult>> futures;
    private BlockingQueue<Block> blockQueue;
    private int chainId;

    private BlockService blockService = ContextManager.getServiceBean(BlockService.class);

    public BlockConsumer(int chainId, List<Future<BlockDownLoadResult>> futures, ThreadPoolExecutor executor, BlockDownloaderParams params) {
        this.params = params;
        this.executor = executor;
        this.futures = futures;
        this.blockQueue = CacheHandler.getBlockQueue(chainId);
        this.chainId = chainId;
    }

    @Override
    public Boolean call() {
        try {

            for (Future<BlockDownLoadResult> task : futures) {
                BlockDownLoadResult result = null;
                try {
                    result = task.get();
                } catch (Exception e) {
                    Log.error(e);
                }

                if (result == null || !result.isSuccess()) {
                    retryDownload(result);
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

            Block block;
            while ((block = blockQueue.take()) != null) {
                boolean saveBlock = blockService.saveBlock(chainId, block);
                if (!saveBlock) {
                    return false;
                }
            }
            return true;
        } catch (InterruptedException e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 下载失败重试，直到成功为止
     *
     * @param result
     * @return
     */
    private BlockDownLoadResult retryDownload(BlockDownLoadResult result) {
        Log.info("retry download blocks, fail node:{}, start:{}", result.getNode(), result.getStartHeight());
        PriorityBlockingQueue<Node> nodes = params.getNodes();
        try {
            result.setNode(nodes.take());
        } catch (InterruptedException e) {
            Log.error(e);
        }

        List<Block> blockList = downloadBlockFromNode(result);
        if (blockList != null && blockList.size() > 0) {
            result.setBlockList(blockList);
        }
        return result.isSuccess() ? result : retryDownload(result);
    }

    private List<Block> downloadBlockFromNode(BlockDownLoadResult result) {
        BlockDownloader.Worker worker = new BlockDownloader.Worker(result.getStartHeight(), result.getSize(), chainId, result.getNode());
        FutureTask<BlockDownLoadResult> downloadThreadFuture = new FutureTask<>(worker);
        executor.execute(downloadThreadFuture);
        List<Block> blockList = null;
        try {
            blockList = downloadThreadFuture.get().getBlockList();
        } catch (Exception e) {
            Log.error(e);
        }
        return blockList;
    }

}
