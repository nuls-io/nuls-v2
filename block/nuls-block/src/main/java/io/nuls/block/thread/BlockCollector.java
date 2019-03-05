/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.Node;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

import static io.nuls.block.constant.Constant.BLOCK_COMPARATOR;

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
    private NulsLogger commonLog;

    BlockCollector(int chainId, BlockingQueue<Future<BlockDownLoadResult>> futures, ThreadPoolExecutor executor, BlockDownloaderParams params, BlockingQueue<Block> queue) {
        this.params = params;
        this.executor = executor;
        this.futures = futures;
        this.chainId = chainId;
        this.queue = queue;
        this.commonLog = ContextManager.getContext(chainId).getCommonLog();
    }

    @Override
    public void run() {
        BlockDownLoadResult result;
        ChainContext context = ContextManager.getContext(chainId);
        try {
            long netLatestHeight = params.getNetLatestHeight();
            long startHeight = params.getLocalLatestHeight() + 1;
            commonLog.info("BlockCollector start work");
            while (startHeight <= netLatestHeight && context.isDoSyn()) {
                result = futures.take().get();
                int size = result.getSize();
                Node node = result.getNode();
                long endHeight = startHeight + size - 1;
                PriorityBlockingQueue<Node> nodes = params.getNodes();
                if (result.isSuccess()) {
                    commonLog.info("get " + size + " blocks:" + startHeight + "->" + endHeight + " ,from:" + node.getId() + ", success");
                    node.adjustCredit(true, result.getDuration());
                    nodes.offer(node);
                    List<Block> blockList = CacheHandler.getBlockList(chainId, result.getMessageHash());
                    blockList.sort(BLOCK_COMPARATOR);
                    queue.addAll(blockList);
                } else {
                    //归还下载失败的节点
                    node.adjustCredit(false, result.getDuration());
                    nodes.offer(node);
                    commonLog.info("get " + size + " blocks:" + startHeight + "->" + endHeight + " ,from:" + node.getId() + ", fail");
                    retryDownload(startHeight, size, 0);
                }
                startHeight += size;
            }
            commonLog.info("BlockCollector stop work, flag-" + context.isDoSyn());
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error("BlockCollector stop work abnormally-" + e);
        }
    }


    /**
     * 下载失败重试,直到成功为止
     *
     * @param startHeight
     * @param size
     * @param index
     * @return
     */
    private void retryDownload(long startHeight, int size, int index) {
        List<Node> nodeList = params.getList();
        BlockDownLoadResult result = downloadBlockFromNode(startHeight, size, nodeList.get(index % nodeList.size()));
        if (result.isSuccess()) {
            Node node = result.getNode();
            long endHeight = startHeight + size - 1;
            commonLog.info("get " + size + " blocks:" + startHeight + "->" + endHeight + " ,from:" + node.getId() + ", success");
            List<Block> blockList = CacheHandler.getBlockList(chainId, result.getMessageHash());
            blockList.sort(BLOCK_COMPARATOR);
            queue.addAll(blockList);
            return;
        }
        retryDownload(startHeight, size, ++index);
    }

    private BlockDownLoadResult downloadBlockFromNode(long startHeight, int size, Node node) {
        commonLog.info("retry download blocks, node:" + node + ", start:" + startHeight);
        BlockWorker worker = new BlockWorker(startHeight, size, chainId, node);
        Future<BlockDownLoadResult> submit = executor.submit(worker);
        try {
            return submit.get();
        } catch (Exception e) {
            return new BlockDownLoadResult();
        }
    }

}
