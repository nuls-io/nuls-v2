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

package io.nuls.block.constant;

import io.nuls.tools.log.Log;

/**
 * 存储对外提供的接口命令
 * @author captain
 * @date 18-11-9 下午2:15
 * @version 1.0
 */
public interface CommandConstant {

    //网络消息对应的RPC命令
    String COMPLETE_MESSAGE = "complete";
    String BLOCK_MESSAGE = "block";
    String GET_BLOCK_MESSAGE = "getBlock";
    String FORWARD_SMALL_BLOCK_MESSAGE = "forward";
    String GET_BLOCKS_BY_HEIGHT_MESSAGE = "getBlocks";
    String GET_TXGROUP_MESSAGE = "getTxs";
    String SMALL_BLOCK_MESSAGE = "sBlock";
    String GET_SMALL_BLOCK_MESSAGE = "getsBlock";
    String TXGROUP_MESSAGE = "txs";

    //普通服务对应的RPC命令
    String BEST_HEIGHT = "bestHeight";
    String BEST_BLOCK_HEADER = "bestBlockHeader";
    String BEST_BLOCK = "bestBlock";
    String GET_BLOCK_HEADER_BY_HEIGHT = "getBlockHeaderByHeight";
    String GET_BLOCK_BY_HEIGHT = "getBlockByHeight";
    String GET_BLOCK_HEADER_BY_HASH = "getBlockHeaderByHash";
    String GET_BLOCK_BY_HASH = "getBlockByHash";
    String GET_LATEST_BLOCK_HEADERS = "getLatestBlockHeaders";
    String RECEIVE_PACKING_BLOCK = "receivePackingBlock";
}
