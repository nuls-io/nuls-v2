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
 * Store interface commands provided externally
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 afternoon2:15
 */
public interface CommandConstant {

    //Corresponding to network messagesRPCcommand
    /**
     * End message returned at the end of bulk block retrieval
     */
    String COMPLETE_MESSAGE = "complete";
    /**
     * Complete block message
     */
    String BLOCK_MESSAGE = "block";
    /**
     * Obtain blocks based on block height
     */
    String GET_BLOCK_BY_HEIGHT_MESSAGE = "getBlockH";
    /**
     * Based on blocksHASHGet blocks
     */
    String GET_BLOCK_MESSAGE = "getBlock";
    /**
     * Forwarding blocks
     */
    String FORWARD_SMALL_BLOCK_MESSAGE = "forward";
    /**
     * Batch acquisition of community block messages
     */
    String GET_BLOCKS_BY_HEIGHT_MESSAGE = "getBlocks";
    /**
     * Batch acquisition of transactions
     */
    String GET_TXGROUP_MESSAGE = "getTxs";
    /**
     * Community block messages
     */
    String SMALL_BLOCK_MESSAGE = "sBlock";
    /**
     * Get community block messages
     */
    String GET_SMALL_BLOCK_MESSAGE = "getsBlock";
    /**
     * Batch transaction messages
     */
    String TXGROUP_MESSAGE = "txs";

    //Corresponding to ordinary servicesRPCcommand
    /**
     * Get some information
     */
    String INFO = "info";
    /**
     * Get the latest altitude
     */
    String LATEST_HEIGHT = "latestHeight";
    /**
     * Get the latest block header
     */
    String LATEST_BLOCK_HEADER = "latestBlockHeader";
    /**
     * Get the latest block headerPO
     */
    String LATEST_BLOCK_HEADER_PO = "latestBlockHeaderPo";
    /**
     * Get the latest blocks
     */
    String LATEST_BLOCK = "latestBlock";
    /**
     * Obtain block headers based on block height
     */
    String GET_BLOCK_HEADER_BY_HEIGHT = "getBlockHeaderByHeight";
    /**
     * Obtain block headers based on block heightPO
     */
    String GET_BLOCK_HEADER_PO_BY_HEIGHT = "getBlockHeaderPoByHeight";
    /**
     * Obtain blocks based on block height
     */
    String GET_BLOCK_BY_HEIGHT = "getBlockByHeight";
    /**
     * Based on blocksHASHGet block header
     */
    String GET_BLOCK_HEADER_BY_HASH = "getBlockHeaderByHash";
    /**
     * Based on blocksHASHGet block headerPO
     */
    String GET_BLOCK_HEADER_PO_BY_HASH = "getBlockHeaderPoByHash";
    /**
     * Based on blocksHASHGet blocks
     */
    String GET_BLOCK_BY_HASH = "getBlockByHash";
    /**
     * Get the latest block headers
     */
    String GET_LATEST_BLOCK_HEADERS = "getLatestBlockHeaders";
    /**
     * Get the latest block heads for several rounds
     */
    String GET_LATEST_ROUND_BLOCK_HEADERS = "getLatestRoundBlockHeaders";
    /**
     * Obtain several rounds of block heads
     */
    String GET_ROUND_BLOCK_HEADERS = "getRoundBlockHeaders";
    /**
     * Receive newly packaged blocks from local nodes
     */
    String RECEIVE_PACKING_BLOCK = "receivePackingBlock";
    /**
     * According to the block height interval
     */
    String GET_BLOCK_HEADERS_BY_HEIGHT_RANGE = "getBlockHeadersByHeightRange";
    /**
     * Provide batch interfaces for protocol upgrade modules
     */
    String GET_BLOCK_HEADERS_FOR_PROTOCOL = "getBlockHeadersForProtocol";
    /**
     * Receive newly packaged blocks from local nodes
     */
    String GET_STATUS = "getStatus";
}
