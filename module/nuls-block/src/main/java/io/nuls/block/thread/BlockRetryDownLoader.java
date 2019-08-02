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
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.Node;
import io.nuls.block.utils.BlockUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;

import java.util.List;

/**
 * 区块收集器,收集下载器下载到的区块,排序后放入共享队列
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午4:25
 */
public class BlockRetryDownLoader implements Runnable {

    /**
     * 区块下载参数
     */
    private int chainId;
    private NulsLogger logger;

    BlockRetryDownLoader(int chainId) {
        this.chainId = chainId;
        this.logger = ContextManager.getContext(chainId).getLogger();
    }

    @Override
    public void run() {
        ChainContext context = ContextManager.getContext(chainId);
        while (context.isDoSyn()) {
            try {
                long h1 = context.getLatestHeight();
                Thread.sleep(5000);
                long h2 = context.getLatestHeight();
                if (h2 == h1) {
                    retryDownload(h2 + 1, context);
                }
            } catch (InterruptedException | NulsException e) {
                logger.error(e);
            }
        }
    }

    /**
     * 下载失败重试,直到成功为止(批量下载失败,重试就一个一个下载)
     *
     * @param height 已下载的区块
     * @return
     */
    private void retryDownload(long height, ChainContext context) throws NulsException, InterruptedException {
        boolean download = false;
        BlockDownloaderParams downloaderParams = context.getDownloaderParams();
        List<Node> nodeList = downloaderParams.getList();
        for (Node node : nodeList) {
            Block block = BlockUtil.downloadBlockByHeight(chainId, node.getId(), height);
            if (block != null) {
                logger.info("retryDownload, get block from " + node.getId() + " success, height-" + height);
                download = true;
                context.getBlockMap().put(height, block);
                context.getCachedBlockSize().addAndGet(block.size());
                break;
            } else {
                node.adjustCredit(false, 0);
            }
        }
        if (!download) {
            //如果从所有节点下载这个高度的区块失败，就停止同步进程
            throw new NulsException(BlockErrorCode.BLOCK_SYN_ERROR);
        }
        Thread.sleep(1000);
        if (context.getBlockMap().get(height + 1) == null) {
            retryDownload(height + 1, context);
        }
    }

}
