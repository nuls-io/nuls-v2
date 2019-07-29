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

package io.nuls.block.thread;

import io.nuls.base.data.Block;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.service.BlockService;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.logback.NulsLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消费共享队列中的区块
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
    private int chainId;
    private BlockingQueue<Block> queue;
    private BlockService blockService;
    private AtomicInteger cachedBlockSize;

    BlockConsumer(int chainId, BlockingQueue<Block> queue, BlockDownloaderParams params, AtomicInteger cachedBlockSize) {
        this.params = params;
        this.chainId = chainId;
        this.queue = queue;
        this.blockService = SpringLiteContext.getBean(BlockService.class);
        this.cachedBlockSize = cachedBlockSize;
    }

    @Override
    public Boolean call() {
        long netLatestHeight = params.getNetLatestHeight();
        long startHeight = params.getLocalLatestHeight() + 1;
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger logger = context.getLogger();
        Block block;
        logger.info("BlockConsumer start work");
        try {
            while (startHeight <= netLatestHeight && context.isDoSyn()) {
                block = queue.take();
                boolean saveBlock = blockService.saveBlock(chainId, block, true);
                if (!saveBlock) {
                    logger.error("error occur when saving downloaded blocks, height-" + startHeight + ", hash-" + block.getHeader().getHash());
                    context.setDoSyn(false);
                    return false;
                }
                startHeight++;
                cachedBlockSize.addAndGet(-block.size());
                if (queue.isEmpty()) {
                    logger.warn("block downloader's queue size is 0, size = " + cachedBlockSize.get() + ", BlockConsumer wait!");
                }
            }
            logger.info("BlockConsumer stop work normally");
            return context.isDoSyn();
        } catch (Exception e) {
            logger.error("BlockConsumer stop work abnormally", e);
            context.setDoSyn(false);
            return false;
        }
    }

}
