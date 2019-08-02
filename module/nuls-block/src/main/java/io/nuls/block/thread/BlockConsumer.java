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

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 消费共享队列中的区块
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午5:45
 */
public class BlockConsumer implements Callable<Boolean> {

    private int chainId;
    private BlockService blockService;

    BlockConsumer(int chainId) {
        this.chainId = chainId;
        this.blockService = SpringLiteContext.getBean(BlockService.class);
    }

    @Override
    public Boolean call() {
        ChainContext context = ContextManager.getContext(chainId);
        BlockDownloaderParams params = context.getDownloaderParams();
        long netLatestHeight = params.getNetLatestHeight();
        long startHeight = params.getLocalLatestHeight() + 1;
        NulsLogger logger = context.getLogger();
        Block block;
        logger.info("BlockConsumer start work");
        try {
            Map<Long, Block> blockMap = context.getBlockMap();
            while (startHeight <= netLatestHeight && context.isDoSyn()) {
                block = blockMap.remove(startHeight);
                if (block != null) {
                    boolean saveBlock = blockService.saveBlock(chainId, block, true);
                    if (!saveBlock) {
                        logger.error("error occur when saving downloaded blocks, height-" + startHeight + ", hash-" + block.getHeader().getHash());
                        context.setDoSyn(false);
                        return false;
                    }
                    startHeight++;
                    context.getCachedBlockSize().addAndGet(-block.size());
                    continue;
                }
                Thread.sleep(10);
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
