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

package io.nuls.block.service;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.po.BlockHeaderPo;

import java.util.List;

/**
 * Blockchain services
 *
 * @author captain
 * @version 1.0
 * @date 18-11-6 afternoon4:57
 */
public interface BlockService {

    /**
     * Obtain the Genesis Block
     *
     * @param chainId chainId/chain id
     * @return
     */
    Block getGenesisBlock(int chainId);

    /**
     * Get the latest block header
     *
     * @param chainId chainId/chain id
     * @return
     */
    BlockHeader getLatestBlockHeader(int chainId);

    /**
     * Get the latest block headerPO
     *
     * @param chainId chainId/chain id
     * @return
     */
    BlockHeaderPo getLatestBlockHeaderPo(int chainId);

    /**
     * Get the latest blocks
     *
     * @param chainId chainId/chain id
     * @return
     */
    Block getLatestBlock(int chainId);

    /**
     * Obtain block headers based on block height
     *
     * @param chainId chainId/chain id
     * @param height  block height
     * @return
     */
    BlockHeader getBlockHeader(int chainId, long height);

    /**
     * Obtain block headers based on block height
     *
     * @param chainId chainId/chain id
     * @param height  block height
     * @return
     */
    BlockHeaderPo getBlockHeaderPo(int chainId, long height);

    /**
     * Obtain blocks based on block height
     *
     * @param chainId chainId/chain id
     * @param height  block height
     * @return
     */
    Block getBlock(int chainId, long height);

    /**
     * Obtain block heads based on block height intervals
     *
     * @param chainId chainId/chain id
     * @param startHeight Starting height
     * @param endHeight   End height
     * @return
     */
    List<BlockHeader> getBlockHeader(int chainId, long startHeight, long endHeight);

    /**
     * Obtain several rounds of block heads(POCConsensus specific)
     *
     * @param chainId chainId/chain id
     * @param height  Obtain from this height forward
     * @param round   Round
     * @return
     */
    List<BlockHeader> getBlockHeaderByRound(int chainId, long height, int round);

    /**
     * Based on blockshashGet block header
     *
     * @param chainId chainId/chain id
     * @param hash    blockhash
     * @return
     */
    BlockHeader getBlockHeader(int chainId, NulsHash hash);

    /**
     * Based on blockshashGet block headerPO
     *
     * @param chainId chainId/chain id
     * @param hash    blockhash
     * @return
     */
    BlockHeaderPo getBlockHeaderPo(int chainId, NulsHash hash);

    /**
     * Based on blockshashGet blocks
     *
     * @param chainId chainId/chain id
     * @param hash    blockhash
     * @return
     */
    Block getBlock(int chainId, NulsHash hash);

    /**
     * Obtain blocks based on their height intervals
     *
     * @param chainId chainId/chain id
     * @param startHeight Starting height
     * @param endHeight   End height
     * @return
     */
    List<Block> getBlock(int chainId, long startHeight, long endHeight);

    /**
     * Save Block,Failed rollback operation has been considered,Without throwing any exceptions,There won't be any junk data
     *
     * @param chainId chainId/chain id
     * @param block    Block to be saved
     * @param needLock Do you need to add a lock
     * @return
     */
    boolean saveBlock(int chainId, Block block, boolean needLock);

    /**
     * Save Block,Failed rollback operation has been considered,Without throwing any exceptions,There won't be any junk data
     *
     * @param chainId chainId/chain id
     * @param block    Block to be saved
     * @param download Is it the latest block,Latest Block-1,Not the latest block-0
     * @param needLock Do you need to add a synchronization lock
     * @param broadcast Do you need to broadcast this block
     * @param forward Do you need to forward this block
     * @return
     */
    boolean saveBlock(int chainId, Block block, int download, boolean needLock, boolean broadcast, boolean forward);

    /**
     * Rolling back blocks,Failed rollback operation has been considered,Without throwing any exceptions,There won't be any junk data
     *
     * @param chainId chainId/chain id
     * @param blockHeaderPo Pending rollback block header
     * @param needLock Do you need to add a synchronization lock
     * @return
     */
    boolean rollbackBlock(int chainId, BlockHeaderPo blockHeaderPo, boolean needLock);

    /**
     * Rolling back blocks,Failed rollback operation has been considered,Without throwing any exceptions,There won't be any junk data
     *
     * @param chainId chainId/chain id
     * @param height  Block height to be rolled back
     * @param needLock Do you need to add a synchronization lock
     * @return
     */
    boolean rollbackBlock(int chainId, long height, boolean needLock);

    /**
     * Forwarding blocks to other connected peer nodes,Allow an exception（Do not forward to it）
     *
     * @param chainId chainId/chain id
     * @param hash        block
     * @param excludeNode Nodes that need to be excluded,Because the block received from this node
     * @return
     */
    boolean forwardBlock(int chainId, NulsHash hash, String excludeNode);

    /**
     * Broadcast blocks to other connected peer nodes
     *
     * @param chainId chainId/chain id
     * @param block
     * @return
     */
    boolean broadcastBlock(int chainId, Block block);

    /**
     * Initialization method
     *
     * @param chainId chainId/chain id
     */
    void init(int chainId);

    /**
     * Obtain blocks based on heighthash
     *
     * @param chainId chainId/chain id
     * @param height block height
     * @return
     */
    NulsHash getBlockHash(int chainId, long height);


}
