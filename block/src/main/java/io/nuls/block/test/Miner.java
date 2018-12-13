/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2018 nuls.io
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

package io.nuls.block.test;

import io.nuls.base.data.Block;
import io.nuls.block.constant.RunningStatusEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.service.BlockService;
import lombok.Data;

import java.util.Random;

import static io.nuls.block.constant.Constant.CHAIN_ID;

/**
 * 测试用生产区块的矿工
 *
 * @author captain
 * @version 1.0
 * @date 18-12-13 下午3:00
 */
@Data
public class Miner extends Thread {

    private static final long TOTAL = Long.MAX_VALUE;
    private String symbol;
    private boolean slow;
    private Block startBlock;
    private Block previousBlock;

    public Miner(String symbol, Block startBlock, boolean slow) {
        this.slow = slow;
        this.symbol = symbol;
        this.startBlock = startBlock;
    }

    @Override
    public void run() {
        int i = 0;
        BlockService blockService = ContextManager.getServiceBean(BlockService.class);
        while (i < TOTAL) {
            try {
                if (i % 20 == 0) {
                    slow = !slow;
                }
                Thread.sleep((long) (new Random().nextInt(5) * 1000));
                if (!ContextManager.getContext(CHAIN_ID).getStatus().equals(RunningStatusEnum.RUNNING)) {
                    return;
                }
                i++;
                Block block;
                if (previousBlock == null) {
                    block = BlockGenerator.generate(startBlock, i, symbol);
                } else {
                    block = BlockGenerator.generate(previousBlock, i, symbol);
                }
                previousBlock = block;
                blockService.saveBlock(CHAIN_ID, block);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}