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
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.Node;
import io.nuls.block.service.BlockService;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.List;
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
            long begin = System.nanoTime();
            while (startHeight <= netLatestHeight && context.isNeedSyn()) {
                block = blockMap.remove(startHeight);
                if (block != null) {
                    begin = System.nanoTime();
                    boolean saveBlock = blockService.saveBlock(chainId, block, true);
                    if (!saveBlock) {
                        logger.error("error occur when saving downloaded blocks, height-" + startHeight + ", hash-" + block.getHeader().getHash());
                        context.setNeedSyn(false);
                        return false;
                    }
                    startHeight++;
                    context.getCachedBlockSize().addAndGet(-block.size());
                    continue;
                }
                Thread.sleep(10);
                long end = System.nanoTime();
                //超过10秒没有高度更新
                if ((end - begin) / 1000000 > 10000) {
                    retryDownload(startHeight, context);
                    begin = System.nanoTime();
                }
            }
            logger.info("BlockConsumer stop work normally");
            return context.isNeedSyn();
        } catch (Exception e) {
            logger.error("BlockConsumer stop work abnormally", e);
            context.setNeedSyn(false);
            return false;
        }
    }

    /**
     * 下载失败重试,直到成功为止(批量下载失败,重试就一个一个下载)
     *
     * @param height 已下载的区块
     * @return
     */
    private void retryDownload(long height, ChainContext context) throws NulsException {
        boolean download = false;
        BlockDownloaderParams downloaderParams = context.getDownloaderParams();
        List<Node> nodeList = downloaderParams.getNodes();
        for (Node node : nodeList) {
            Block block = BlockUtil.downloadBlockByHeight(chainId, node.getId(), height);
            if (block != null) {
                context.getLogger().info("retryDownload, get block from " + node.getId() + " success, height-" + height);
                download = true;
                context.getBlockMap().put(height, block);
                context.getCachedBlockSize().addAndGet(block.size());
                break;
            } else {
                node.adjustCredit(false, 0);
            }
        }
        if (!download) {
            //如果从所有节点下载这个高度的区块失败,就停止同步进程,等待下次同步
            throw new NulsException(BlockErrorCode.BLOCK_SYN_ERROR);
        }
    }

}
