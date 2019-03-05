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
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.cache.CacheHandler;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.CompleteMessage;
import io.nuls.block.message.HeightRangeMessage;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.Node;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.tools.log.logback.NulsLogger;
import lombok.AllArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static io.nuls.block.constant.CommandConstant.GET_BLOCKS_BY_HEIGHT_MESSAGE;
import static io.nuls.block.constant.Constant.BATCH_DOWNLOAD_TIMEOUNT;
import static io.nuls.block.constant.Constant.MAX_LOOP;

/**
 * 区块下载器
 *
 * @author captain
 * @version 1.0
 * @date 18-12-4 下午8:29
 */
@AllArgsConstructor
public class BlockWorker implements Callable<BlockDownLoadResult> {

    private long startHeight;
    private int size;
    private int chainId;
    private Node node;

    @Override
    public BlockDownLoadResult call() {
        boolean b = false;
        long endHeight = startHeight + size - 1;
        //组装批量获取区块消息
        HeightRangeMessage message = new HeightRangeMessage(startHeight, endHeight);
        //计算本次请求hash,用来跟踪本次异步请求
        NulsDigestData messageHash = message.getHash();
        ChainContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        long duration = 0;
        try {
            Future<CompleteMessage> future = CacheHandler.addBatchBlockRequest(chainId, messageHash);
            //发送消息给目标节点
            long begin = System.currentTimeMillis();
            boolean result = NetworkUtil.sendToNode(chainId, message, node.getId(), GET_BLOCKS_BY_HEIGHT_MESSAGE);
            //发送失败清空数据
            if (!result) {
                CacheHandler.removeRequest(chainId, messageHash);
                return new BlockDownLoadResult(messageHash, startHeight, size, node, false, 0);
            }
            CompleteMessage completeMessage = future.get(BATCH_DOWNLOAD_TIMEOUNT, TimeUnit.SECONDS);
            List<Block> blockList = CacheHandler.getBlockList(chainId, messageHash);
            int size = blockList.size();
            long expect = message.getEndHeight() - message.getStartHeight();
            int interval = context.getParameters().getWaitInterval();
            int count = 0;
            while (size < expect && count < MAX_LOOP) {
                Thread.sleep(interval * (expect - size));
                blockList = CacheHandler.getBlockList(chainId, messageHash);
                size = blockList.size();
                count++;
            }
            if (size != expect) {
                return new BlockDownLoadResult(messageHash, startHeight, size, node, b, 0);
            }
            List<Long> heightList = new ArrayList<>();
            for (Block block : blockList) {
                heightList.add(block.getHeader().getHeight());
            }
            for (long i = message.getStartHeight(); i <= message.getEndHeight(); i++) {
                if (!heightList.contains(i)) {
                    return new BlockDownLoadResult(messageHash, startHeight, size, node, b, 0);
                }
            }
            b = completeMessage.isSuccess();
            long end = System.currentTimeMillis();
            duration = end - begin;
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
        return new BlockDownLoadResult(messageHash, startHeight, size, node, b, duration);
    }
}