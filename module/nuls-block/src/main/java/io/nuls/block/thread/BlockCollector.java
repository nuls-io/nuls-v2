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

import io.nuls.block.cache.BlockCacher;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.Node;
import io.nuls.core.log.logback.NulsLogger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 区块收集器,收集下载器下载到的区块,排序后放入共享队列
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午4:25
 */
public class BlockCollector implements Runnable {

    private BlockingQueue<Future<BlockDownLoadResult>> futures;
    private int chainId;
    private NulsLogger logger;

    BlockCollector(int chainId, BlockingQueue<Future<BlockDownLoadResult>> futures) {
        this.chainId = chainId;
        this.futures = futures;
        this.logger = ContextManager.getContext(chainId).getLogger();
    }

    @Override
    public void run() {
        BlockDownLoadResult result;
        ChainContext context = ContextManager.getContext(chainId);
        try {
            BlockDownloaderParams params = context.getDownloaderParams();
            long netLatestHeight = params.getNetLatestHeight();
            long startHeight = params.getLocalLatestHeight() + 1;
            logger.info("BlockCollector start work");
            while (startHeight <= netLatestHeight && context.isDoSyn()) {
                result = futures.take().get();
                int size = result.getSize();
                Node node = result.getNode();
                long endHeight = startHeight + size - 1;
                PriorityBlockingQueue<Node> nodes = params.getNodes();
                if (result.isSuccess()) {
                    logger.info("get " + size + " blocks:" + startHeight + "->" + endHeight + " ,from:" + node.getId() + ", success");
                    node.adjustCredit(true, result.getDuration());
                    nodes.offer(node);
                    BlockCacher.removeBatchBlockRequest(chainId, result.getMessageHash());
                } else {
                    //归还下载失败的节点
                    logger.warn("get " + size + " blocks:" + startHeight + "->" + endHeight + " ,from:" + node.getId() + ", fail");
                    node.adjustCredit(false, result.getDuration());
                    nodes.offer(node);
                }
                startHeight += size;
            }
            logger.info("BlockCollector stop work, flag-" + context.isDoSyn());
        } catch (Exception e) {
            context.setDoSyn(false);
            logger.error("BlockCollector stop work abnormally-", e);
        }
    }

}
