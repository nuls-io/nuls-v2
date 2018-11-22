/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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
import io.nuls.block.service.BlockService;
import io.nuls.tools.log.Log;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;

/**
 * 消费同步的区块
 * @author captain
 * @date 18-11-8 下午5:45
 * @version 1.0
 */
public class BlockConsumer implements Callable<Boolean> {

    private BlockingQueue<Block> blockQueue;
    private String queueName;
    private int chainId;

    private BlockService blockService = ContextManager.getServiceBean(BlockService.class);

    public BlockConsumer(int chainId, BlockingQueue<Block> blockQueue) {
        this.blockQueue = blockQueue;
        this.queueName = queueName;
    }

    @Override
    public Boolean call() throws Exception {
        try {
            Block block;
            while ((block = blockQueue.take()) != null) {
                if (block.getHeader() == null) {
                    break;
                }
                blockService.saveBlock(chainId, block);
            }
            return true;
        } catch (InterruptedException e) {
            Log.error(e);
            return false;
        }
    }

}
