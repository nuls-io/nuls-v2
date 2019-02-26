/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
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

/**
 * 存储对外提供的接口命令
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午2:15
 */
public interface CommandConstant {

    //网络消息对应的RPC命令
    /**
     * 批量获取区块结束时返回的结束消息
     */
    String COMPLETE_MESSAGE = "complete";
    /**
     * 完整的区块消息
     */
    String BLOCK_MESSAGE = "block";
    /**
     * 根据区块HASH获取区块
     */
    String GET_BLOCK_MESSAGE = "getBlock";
    /**
     * 转发区块
     */
    String FORWARD_SMALL_BLOCK_MESSAGE = "forward";
    /**
     * 批量获取小区块消息
     */
    String GET_BLOCKS_BY_HEIGHT_MESSAGE = "getBlocks";
    /**
     * 批量获取交易
     */
    String GET_TXGROUP_MESSAGE = "getTxs";
    /**
     * 小区块消息
     */
    String SMALL_BLOCK_MESSAGE = "sBlock";
    /**
     * 获取小区块消息
     */
    String GET_SMALL_BLOCK_MESSAGE = "getsBlock";
    /**
     * 批量交易消息
     */
    String TXGROUP_MESSAGE = "txs";

    //普通服务对应的RPC命令
    /**
     * 获取最新高度
     */
    String LATEST_HEIGHT = "latestHeight";
    /**
     * 获取最新区块头
     */
    String LATEST_BLOCK_HEADER = "latestBlockHeader";
    /**
     * 获取最新区块
     */
    String LATEST_BLOCK = "latestBlock";
    /**
     * 根据区块高度获取区块头
     */
    String GET_BLOCK_HEADER_BY_HEIGHT = "getBlockHeaderByHeight";
    /**
     * 根据区块高度获取区块
     */
    String GET_BLOCK_BY_HEIGHT = "getBlockByHeight";
    /**
     * 根据区块HASH获取区块头
     */
    String GET_BLOCK_HEADER_BY_HASH = "getBlockHeaderByHash";
    /**
     * 根据区块HASH获取区块
     */
    String GET_BLOCK_BY_HASH = "downloadBlockByHash";
    /**
     * 获取最新若干区块头
     */
    String GET_LATEST_BLOCK_HEADERS = "getLatestBlockHeaders";
    /**
     * 接收本地节点新打包的区块
     */
    String RECEIVE_PACKING_BLOCK = "receivePackingBlock";
    /**
     * 根据区块高度区间
     */
    String GET_BLOCK_HEADERS_BY_HEIGHT_RANGE = "getBlockHeadersByHeightRange";
}
