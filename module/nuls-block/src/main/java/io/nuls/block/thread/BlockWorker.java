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
import io.nuls.base.data.NulsHash;
import io.nuls.block.cache.BlockCacher;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.message.HeightRangeMessage;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.Node;
import io.nuls.block.rpc.call.NetworkUtil;
import io.nuls.core.log.logback.NulsLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static io.nuls.block.constant.CommandConstant.GET_BLOCKS_BY_HEIGHT_MESSAGE;

/**
 * 区块下载器
 *
 * @author captain
 * @version 1.0
 * @date 18-12-4 下午8:29
 */
public class BlockWorker implements Callable<BlockDownLoadResult> {

    private long startHeight;
    private int size;
    private int chainId;
    private Node node;
    private HeightRangeMessage message;

    BlockWorker(long startHeight, int size, int chainId, Node node, HeightRangeMessage message) {
        this.startHeight = startHeight;
        this.size = size;
        this.chainId = chainId;
        this.node = node;
        this.message = message;
    }

    @Override
    public BlockDownLoadResult call() {
        boolean b = false;
        //计算本次请求hash,用来跟踪本次异步请求
        NulsHash messageHash = message.getMsgHash();
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getLogger();
        long duration = 0;
        try {
            Future<CompleteMessage> future = BlockCacher.addBatchBlockRequest(chainId, messageHash);
            //发送消息给目标节点
            long begin = System.currentTimeMillis();
            boolean result = NetworkUtil.sendToNode(chainId, message, node.getId(), GET_BLOCKS_BY_HEIGHT_MESSAGE);
            //发送失败清空数据
            if (!result) {
                BlockCacher.removeBatchBlockRequest(chainId, messageHash);
                return new BlockDownLoadResult(messageHash, startHeight, size, node, false, 0);
            }
            int batchDownloadTimeout = context.getParameters().getBatchDownloadTimeout();
            CompleteMessage completeMessage = future.get(batchDownloadTimeout, TimeUnit.MILLISECONDS);
            List<Block> blockList = BlockCacher.getBlockList(chainId, messageHash);
            int real = blockList.size();
            long interval = (long) context.getParameters().getWaitInterval();
            int maxLoop = context.getParameters().getMaxLoop();
            int count = 0;
            while (real < size && count < maxLoop) {
                commonLog.debug("#start-" + message.getStartHeight() + ",end-" + message.getEndHeight() + "#real-" + real + ",expect-" + size + ",count-" + count + ",node-" +node.getId());
                Thread.sleep(interval * (size - real));
                blockList = BlockCacher.getBlockList(chainId, messageHash);
                real = blockList.size();
                count++;
            }
            if (real != size) {
                return new BlockDownLoadResult(messageHash, startHeight, size, node, false, 0);
            }
            List<Long> heightList = new ArrayList<>();
            for (Block block : blockList) {
                heightList.add(block.getHeader().getHeight());
            }
            for (long i = message.getStartHeight(); i <= message.getEndHeight(); i++) {
                if (!heightList.contains(i)) {
                    return new BlockDownLoadResult(messageHash, startHeight, size, node, false, 0);
                }
            }
            b = completeMessage.isSuccess();
            long end = System.currentTimeMillis();
            duration = end - begin;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            commonLog.error("", e);
        } catch (TimeoutException | ExecutionException e) {
            commonLog.error("", e);
        }
        return new BlockDownLoadResult(messageHash, startHeight, size, node, b, duration);
    }
}