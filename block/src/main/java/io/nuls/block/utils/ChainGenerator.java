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

package io.nuls.block.utils;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.block.constant.ChainTypeEnum;
import io.nuls.block.model.Chain;

import java.util.LinkedList;

/**
 * 链生成器
 * @author captain
 * @date 18-11-29 下午2:52
 * @version 1.0
 */
public class ChainGenerator {

    /**
     * 测试专用
     * 生成一条链
     * @param startHeight       链起始高度(包括在链内)
     * @param endHeight         链终止高度(包括在链内)
     * @param symbol            链标志，生成的hashList与这个字段有关，便与判断测试结果
     * @param parent            父链
     * @param parentSymbol      父链标志
     * @param chainId
     * @return
     */
    public static Chain newChain(long startHeight, long endHeight, String symbol, Chain parent, String parentSymbol, int chainId, ChainTypeEnum type){
        Chain chain = new Chain();
        chain.setType(type);
        chain.setChainId(chainId);
        chain.setStartHeight(startHeight);
        chain.setEndHeight(endHeight);
        chain.setParent(parent);
        LinkedList<NulsDigestData> hashList = new LinkedList<>();
        for (long i = startHeight; i <= endHeight; i++) {
            hashList.add(NulsDigestData.calcDigestData((symbol + i).getBytes()));
        }
        chain.setHashList(hashList);
        if (parent != null) {
            parent.getSons().add(chain);
        }
        chain.setStartHashCode(hashList.getFirst().hashCode());
        chain.setPreviousHash(NulsDigestData.calcDigestData((parentSymbol + (startHeight-1)).getBytes()));
        return chain;
    }

    /**
     * 测试专用
     * 生成一条主链
     * @param endHeight         链终止高度(包括在链内)
     * @param symbol            链标志，生成的hashList与这个字段有关，便与判断测试结果
     * @param chainId
     * @return
     */
    public static Chain newMasterChain(long endHeight, String symbol, int chainId){
        Chain chain = new Chain();
        chain.setType(ChainTypeEnum.MASTER);
        chain.setChainId(chainId);
        chain.setEndHeight(endHeight);
        LinkedList<NulsDigestData> hashList = new LinkedList<>();
        for (long i = 0; i <= endHeight; i++) {
            hashList.add(NulsDigestData.calcDigestData((symbol + i).getBytes()));
        }
        chain.setHashList(hashList);
        chain.setStartHashCode(hashList.getFirst().hashCode());
        chain.setPreviousHash(NulsDigestData.calcDigestData((symbol + (0-1)).getBytes()));
        return chain;
    }

    /**
     * 使用一个区块生成一条链
     *
     * @param chainId
     * @param block
     * @param parent  生成分叉链时传父链，生成孤儿链时传null
     * @return
     */
    public static Chain generate(int chainId, Block block, Chain parent, ChainTypeEnum type) {
        BlockHeader header = block.getHeader();
        long height = header.getHeight();
        NulsDigestData hash = header.getHash();
        NulsDigestData preHash = header.getPreHash();
        Chain chain = new Chain();
        LinkedList<NulsDigestData> hashs = new LinkedList();
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
     * 系统初始化时，由本地的最新区块生成主链
     *
     * @param chainId
     * @param block
     * @return
     */
    public static Chain generateMasterChain(int chainId, Block block) {
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
        LinkedList<NulsDigestData> hashs = new LinkedList();
        NulsDigestData hash = header.getHash();
        hashs.add(hash);
        chain.setHashList(hashs);
        return chain;
    }

}
