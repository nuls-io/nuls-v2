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
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.model.Node;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.concurrent.*;

/**
 * 区块下载管理器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午4:25
 */
public class BlockDownloader implements Callable<Boolean> {

    /**
     * 区块下载参数
     */
    private BlockDownloaderParams params;
    /**
     * 执行下载任务的线程池
     */
    private ThreadPoolExecutor executor;
    /**
     * 缓存下载结果
     */
    private BlockingQueue<Future<BlockDownLoadResult>> futures;
    /**
     * 链ID
     */
    private int chainId;
    /**
     * 下载到的区块最终放入此队列，由消费线程取出进行保存
     */
    private BlockingQueue<Block> queue;

    public BlockDownloader(int chainId, BlockingQueue<Future<BlockDownLoadResult>> futures, ThreadPoolExecutor executor, BlockDownloaderParams params, BlockingQueue<Block> queue) {
        this.params = params;
        this.executor = executor;
        this.futures = futures;
        this.chainId = chainId;
        this.queue = queue;
    }

    @Override
    public Boolean call() {
        PriorityBlockingQueue<Node> nodes = params.getNodes();
        long netLatestHeight = params.getNetLatestHeight();
        long startHeight = params.getLocalLatestHeight() + 1;
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        try {
            commonLog.info("BlockDownloader start work from " + startHeight + " to " + netLatestHeight);
            ChainParameters chainParameters = context.getParameters();
            int blockCache = chainParameters.getBlockCache();
            int maxDowncount = chainParameters.getDownloadNumber();
            while (startHeight <= netLatestHeight && context.isDoSyn()) {
                while (queue.size() > blockCache) {
                    commonLog.info("BlockDownloader wait！ cached queue size:" + queue.size());
                    Thread.sleep(1000L);
                }
                int credit;
                Node node;
                do {
                    node = nodes.take();
                    credit = node.getCredit();
                } while (credit == 0);
                int size = maxDowncount * credit / 100;
                size = size <= 0 ? 1 : size;
                if (startHeight + size > netLatestHeight) {
                    size = (int) (netLatestHeight - startHeight + 1);
                }
                BlockWorker worker = new BlockWorker(startHeight, size, chainId, node);
                Future<BlockDownLoadResult> future = executor.submit(worker);
                futures.offer(future);
                startHeight += size;
            }
            commonLog.info("BlockDownloader stop work, flag-" + context.isDoSyn());
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
            context.setDoSyn(false);
            return false;
        }
        executor.shutdown();
        return context.isDoSyn();
    }

}
