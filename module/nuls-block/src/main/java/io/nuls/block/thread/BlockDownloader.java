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

import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HeightRangeMessage;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.model.Node;
import io.nuls.core.log.logback.NulsLogger;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 区块下载管理器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午4:25
 */
public class BlockDownloader implements Callable<Boolean> {

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

    BlockDownloader(int chainId, BlockingQueue<Future<BlockDownLoadResult>> futures, ThreadPoolExecutor executor) {
        this.chainId = chainId;
        this.futures = futures;
        this.executor = executor;
    }

    @Override
    public Boolean call() {
        ChainContext context = ContextManager.getContext(chainId);
        BlockDownloaderParams params = context.getDownloaderParams();
        PriorityBlockingQueue<Node> nodes = params.getNodes();
        long netLatestHeight = params.getNetLatestHeight();
        long startHeight = params.getLocalLatestHeight() + 1;
        NulsLogger logger = context.getLogger();
        try {
            logger.info("BlockDownloader start work from " + startHeight + " to " + netLatestHeight + ", nodes-" + nodes);
            ChainParameters chainParameters = context.getParameters();
            long cachedBlockSizeLimit = chainParameters.getCachedBlockSizeLimit();
            int downloadNumber = chainParameters.getDownloadNumber();
            AtomicInteger cachedBlockSize = context.getCachedBlockSize();
            long limit = context.getParameters().getCachedBlockSizeLimit() * 80 / 100;
            while (startHeight <= netLatestHeight && context.isDoSyn()) {
                int cachedSize = cachedBlockSize.get();
                while (cachedSize > cachedBlockSizeLimit) {
                    logger.info("BlockDownloader wait! cached block:" + context.getBlockMap().size() + ", total block size:" + cachedSize);
                    nodes.forEach(e -> e.setCredit(20));
                    Thread.sleep(3000L);
                    cachedSize = cachedBlockSize.get();
                }
                //下载的区块字节数达到缓存阈值的80%时，降慢下载速度
                if (cachedSize > limit) {
                    params.getList().forEach(e -> e.setCredit(e.getCredit() / 2));
                }
                int credit;
                Node node;
                do {
                    node = nodes.take();
                    credit = node.getCredit();
                    if (credit == 0) {
                        params.getList().remove(node);
                        logger.warn("remove unstable node:" + node);
                    }
                } while (credit == 0);
                int size = downloadNumber * credit / 100;
                size = size <= 0 ? 1 : size;
                if (startHeight + size > netLatestHeight) {
                    size = (int) (netLatestHeight - startHeight + 1);
                }
                BlockWorker worker = new BlockWorker(startHeight, size, chainId, node);
                Future<BlockDownLoadResult> future = executor.submit(worker);
                futures.offer(future);
                startHeight += size;
            }
            logger.info("BlockDownloader stop work, flag-" + context.isDoSyn());
        } catch (Exception e) {
            logger.error("", e);
            context.setDoSyn(false);
            return false;
        }
        return context.isDoSyn();
    }

}
