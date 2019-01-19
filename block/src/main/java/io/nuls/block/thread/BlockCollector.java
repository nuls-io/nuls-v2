/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2018 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.block.thread;

import io.nuls.base.data.Block;
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.model.Node;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import static io.nuls.block.constant.Constant.BLOCK_COMPARATOR;
import static io.nuls.block.utils.LoggerUtil.Log;

/**
 * 区块收集器,收集下载器下载到的区块,排序后放入共享队列
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午4:25
 */
public class BlockCollector implements Runnable {

    /**
     * 区块下载参数
     */
    private BlockDownloaderParams params;
    private BlockingQueue<Block> queue;
    private ThreadPoolExecutor executor;
    private BlockingQueue<Future<BlockDownLoadResult>> futures;
    private int chainId;

    public BlockCollector(int chainId, BlockingQueue<Future<BlockDownLoadResult>> futures, ThreadPoolExecutor executor, BlockDownloaderParams params, BlockingQueue<Block> queue) {
        this.params = params;
        this.executor = executor;
        this.futures = futures;
        this.chainId = chainId;
        this.queue = queue;
    }

    @Override
    public void run() {
        BlockDownLoadResult result;
        try {
            long netLatestHeight = params.getNetLatestHeight();
            long startHeight = params.getLocalLatestHeight() + 1;
            Log.info("BlockCollector start work");
            while (startHeight <= netLatestHeight) {
                result = futures.take().get();
                int size = result.getSize();
                if (result.isSuccess()) {
                    Node node = result.getNode();
                    long endHeight = startHeight + size - 1;
                    Log.info("get " + size + " blocks:" + startHeight + "->" + endHeight + " ,from:" + node.getId() + ", success");
                    node.adjustCredit(true);
                    params.getNodes().offer(node);
                    List<Block> blockList = CacheHandler.getBlockList(chainId, result.getMessageHash());
                    blockList.sort(BLOCK_COMPARATOR);
                    queue.addAll(blockList);
                } else {
                    retryDownload(result);
                }
                startHeight += size;
            }
            Log.info("BlockCollector stop work");
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
        }
    }


    /**
     * 下载失败重试,直到成功为止
     *
     * @param result
     * @return
     */
    private void retryDownload(BlockDownLoadResult result) {
        //归还下载失败的节点
        Node node = result.getNode();
        node.adjustCredit(false);
        params.getNodes().offer(node);
        Log.info("retry download blocks, fail node:" + node + ", start:" + result.getStartHeight());
        PriorityBlockingQueue<Node> nodes = params.getNodes();
        try {
            result.setNode(nodes.take());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Log.error(e);
        }

        if (downloadBlockFromNode(result)) {
            return;
        }
        retryDownload(result);
    }

    private boolean downloadBlockFromNode(BlockDownLoadResult result) {
        BlockWorker worker = new BlockWorker(result.getStartHeight(), result.getSize(), chainId, result.getNode());
        Future<BlockDownLoadResult> submit = executor.submit(worker);
        try {
            return submit.get().isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e);
        }
        return false;
    }

}
