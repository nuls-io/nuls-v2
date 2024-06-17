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

package io.nuls.block.utils;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.Chain;
import io.nuls.block.service.BlockService;
import io.nuls.common.ConfigBean;

import java.util.ArrayDeque;

/**
 * Chain Generator
 *
 * @author captain
 * @version 1.0
 * @date 18-11-29 afternoon2:52
 */
public class ChainGenerator {

    /**
     * Test specific
     * Generate a chain
     *
     * @param startHeight  Chain starting height(Include within the chain)
     * @param endHeight    Chain termination height(Include within the chain)
     * @param symbol       Chain marker,GeneratedhashListRelated to this field,Judgment test results
     * @param parent       Parent Chain
     * @param parentSymbol Parent Chain Flag
     * @param chainId chainId/chain id
     * @return
     */
    public static Chain newChain(long startHeight, long endHeight, String symbol, Chain parent, String parentSymbol, int chainId, ChainTypeEnum type) {
        Chain chain = new Chain();
        chain.setType(type);
        chain.setChainId(chainId);
        chain.setStartHeight(startHeight);
        chain.setEndHeight(endHeight);
        chain.setParent(parent);
        ArrayDeque<NulsHash> hashList = new ArrayDeque<>();
        for (long i = startHeight; i <= endHeight; i++) {
            hashList.add(NulsHash.calcHash((symbol + i).getBytes()));
        }
        chain.setHashList(hashList);
        if (parent != null) {
            parent.getSons().add(chain);
        }
        chain.setStartHashCode(hashList.getFirst().hashCode());
        chain.setPreviousHash(NulsHash.calcHash((parentSymbol + (startHeight - 1)).getBytes()));
        return chain;
    }

    /**
     * Test specific
     * Generate a main chain
     *
     * @param endHeight Chain termination height(Include within the chain)
     * @param symbol    Chain marker,GeneratedhashListRelated to this field,Judgment test results
     * @param chainId chainId/chain id
     * @return
     */
    public static Chain newMasterChain(long endHeight, String symbol, int chainId) {
        Chain chain = new Chain();
        chain.setType(ChainTypeEnum.MASTER);
        chain.setChainId(chainId);
        chain.setEndHeight(endHeight);
        ArrayDeque<NulsHash> hashList = new ArrayDeque<>();
        for (long i = 0; i <= endHeight; i++) {
            hashList.add(NulsHash.calcHash((symbol + i).getBytes()));
        }
        chain.setHashList(hashList);
        chain.setStartHashCode(hashList.getFirst().hashCode());
        chain.setPreviousHash(NulsHash.calcHash((symbol + (0 - 1)).getBytes()));
        return chain;
    }

    /**
     * Generate a chain using a block
     *
     * @param chainId chainId/chain id
     * @param block
     * @param parent  When generating a forked chain, pass on the parent chain,Transferred when generating orphan chainsnull
     * @return
     */
    public static Chain generate(int chainId, Block block, Chain parent, ChainTypeEnum type) {
        BlockHeader header = block.getHeader();
        long height = header.getHeight();
        NulsHash hash = header.getHash();
        NulsHash preHash = header.getPreHash();
        Chain chain = new Chain();
        ArrayDeque<NulsHash> hashs = new ArrayDeque<>();
        hashs.add(hash);
        chain.setChainId(chainId);
        chain.setStartHeight(height);
        chain.setEndHeight(height);
        chain.setHashList(hashs);
        chain.setPreviousHash(preHash);
        chain.setParent(parent);
        chain.setType(type);
        chain.setStartHashCode(header.getHash().hashCode());
        if (parent != null) {
            parent.getSons().add(chain);
        }
        return chain;
    }

    /**
     * During system initialization,Generate the main chain from the latest local blocks
     *
     * @param chainId chainId/chain id
     * @param block
     * @param blockService
     * @return
     */
    public static Chain generateMasterChain(int chainId, Block block, BlockService blockService) {
        BlockHeader header = block.getHeader();
        long height = header.getHeight();
        Chain chain = new Chain();
        chain.setChainId(chainId);
        chain.setStartHeight(0L);
        chain.setEndHeight(height);
        chain.setType(ChainTypeEnum.MASTER);
        chain.setParent(null);
        chain.setPreviousHash(header.getPreHash());
        chain.setStartHashCode(header.getHash().hashCode());
        ArrayDeque<NulsHash> hashs = new ArrayDeque<>();
        ConfigBean parameters = ContextManager.getContext(chainId).getParameters();
        int heightRange = parameters.getHeightRange();
        long start = height - heightRange + 1;
        start = start >= 0 ? start : 0;
        //Load blocks on the main chainhash
        for (long i = start; i <= height; i++) {
            hashs.add(blockService.getBlockHash(chainId, i));
        }
        chain.setHashList(hashs);
        return chain;
    }

}
