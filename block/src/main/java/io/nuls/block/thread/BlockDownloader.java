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
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.model.Node;
import io.nuls.tools.log.Log;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author: Niels Wang
 */
public class BlockDownloader implements Callable<BlockDownLoadResult> {

    private NulsDigestData startHash;
    private NulsDigestData endHash;
    private long startHeight;
    private int size;
    private int chainId;
    private Node node;

    public BlockDownloader(int chainId, NulsDigestData startHash, NulsDigestData endHash, long startHeight, int size, Node node) {
        this.chainId = chainId;
        this.startHash = startHash;
        this.endHash = endHash;
        this.startHeight = startHeight;
        this.size = size;
        this.node = node;
    }

    @Override
    public BlockDownLoadResult call() {
        List<Block> blockList = null;
        try {
            blockList = BlockDownloadUtils.getBlocks(chainId, node, startHeight, startHeight + size - 1);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        return new BlockDownLoadResult(startHash, endHash, startHeight, size, node, blockList);
    }

}
