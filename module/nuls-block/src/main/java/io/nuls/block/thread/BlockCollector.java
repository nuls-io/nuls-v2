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
import io.nuls.block.cache.BlockCacher;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.Node;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

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
    private BlockingQueue<Future<BlockDownLoadResult>> futures;
    private int chainId;
    private NulsLogger commonLog;
    private AtomicInteger cachedBlockSize;

    BlockCollector(int chainId, BlockingQueue<Future<BlockDownLoadResult>> futures, BlockDownloaderParams params, BlockingQueue<Block> queue, AtomicInteger cachedBlockSize) {
        this.params = params;
        this.futures = futures;
        this.chainId = chainId;
        this.queue = queue;
        this.commonLog = ContextManager.getContext(chainId).getLogger();
        this.cachedBlockSize = cachedBlockSize;
    }

    @Override
    public void run() {
        BlockDownLoadResult result;
        ChainContext context = ContextManager.getContext(chainId);
        try {
            //下载的区块字节数达到缓存阈值的80%时，降慢下载速度
            int limit = context.getParameters().getCachedBlockSizeLimit() * 80 / 100;
            long netLatestHeight = params.getNetLatestHeight();
            long startHeight = params.getLocalLatestHeight() + 1;
            commonLog.info("BlockCollector start work");
            while (startHeight <= netLatestHeight && context.isDoSyn()) {
                result = futures.take().get();
                int size = result.getSize();
                Node node = result.getNode();
                long endHeight = startHeight + size - 1;
                PriorityBlockingQueue<Node> nodes = params.getNodes();
                List<Block> blockList = result.getBlockList();
                if (result.isSuccess()) {
                    commonLog.info("get " + size + " blocks:" + startHeight + "->" + endHeight + " ,from:" + node.getId() + ", success");
                    node.adjustCredit(true, result.getDuration());
                    nodes.offer(node);
                    blockList.sort(BLOCK_COMPARATOR);
                    int sum = blockList.stream().mapToInt(Block::size).sum();
                    cachedBlockSize.addAndGet(sum);
                    if (cachedBlockSize.get() > limit) {
                        params.getList().forEach(e -> e.setCredit(e.getCredit() / 2));
                    }
                    queue.addAll(blockList);
                    BlockCacher.removeBatchBlockRequest(chainId, result.getMessageHash());
                } else {
                    //归还下载失败的节点
                    node.adjustCredit(false, result.getDuration());
                    nodes.offer(node);
                    commonLog.info("get " + size + " blocks:" + startHeight + "->" + endHeight + " ,from:" + node.getId() + ", fail");
                    retryDownload(blockList, result, limit);
                }
                startHeight += size;
            }
            commonLog.info("BlockCollector stop work, flag-" + context.isDoSyn());
        } catch (Exception e) {
            context.setDoSyn(false);
            commonLog.error("BlockCollector stop work abnormally-", e);
        }
    }

    /**
     * 下载失败重试,直到成功为止(批量下载失败,重试就一个一个下载)
     *
     * @param blockList                   已下载的区块
     * @param result                      失败的下载结果
     * @param limit
     * @return
     */
    private void retryDownload(List<Block> blockList, BlockDownLoadResult result, int limit) throws NulsException {
        if (blockList == null) {
            blockList = new ArrayList<>();
        }
        List<Long> missingHeightList = result.getMissingHeightList();
        if (missingHeightList == null) {
            missingHeightList = new ArrayList<>();
            long startHeight = result.getStartHeight();
            for (int i = 0; i < result.getSize(); i++) {
                missingHeightList.add(startHeight);
                startHeight++;
            }
        }
        List<Node> nodeList = params.getList();
        for (long height : missingHeightList) {
            boolean download = false;
            for (Node node : nodeList) {
                Block block = BlockUtil.downloadBlockByHeight(chainId, node.getId(), height);
                if (block != null) {
                    commonLog.info("retryDownload, get block from " + node.getId() + " success, height-" + height);
                    blockList.add(block);
                    download = true;
                    break;
                }
            }
            if (!download) {
                //如果从所有节点下载这个高度的区块失败，就停止同步进程
                throw new NulsException(BlockErrorCode.BLOCK_SYN_ERROR);
            }
        }
        blockList.sort(BLOCK_COMPARATOR);
        int sum = blockList.stream().mapToInt(Block::size).sum();
        cachedBlockSize.addAndGet(sum);
        if (cachedBlockSize.get() > limit) {
            params.getList().forEach(e -> e.setCredit(e.getCredit() / 2));
        }
        queue.addAll(blockList);
    }

}
