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

import java.util.concurrent.*;

/**
 * 消费同步到的区块
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午5:45
 */
public class BlockConsumer implements Callable<Boolean> {

    private int chainId;
    private BlockingQueue<Block> queue;
    private BlockService blockService = ContextManager.getServiceBean(BlockService.class);

    public BlockConsumer(int chainId, BlockingQueue<Block> queue) {
        this.chainId = chainId;
        this.queue = queue;
    }

    @Override
    public Boolean call() {
        try {
            Block block;
            while ((block = queue.take()) != null) {
                boolean saveBlock = blockService.saveBlock(chainId, block);
                if (!saveBlock) {
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

}
