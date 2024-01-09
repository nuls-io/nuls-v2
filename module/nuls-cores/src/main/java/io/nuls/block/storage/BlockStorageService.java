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

package io.nuls.block.storage;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.po.BlockHeaderPo;

import java.util.List;

/**
 * Blockstorage services
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 morning10:08
 */
public interface BlockStorageService {

    /**
     * Store a{@link BlockHeaderPo}
     *
     * @param chainId chainId/chain id
     * @param blockHeader
     * @return
     * @throws Exception
     */
    boolean save(int chainId, BlockHeaderPo blockHeader);

    /**
     * According to the chainIDQuery one with block height{@link BlockHeaderPo}
     *
     * @param chainId chainId/chain id
     * @param height
     * @return
     */
    BlockHeaderPo query(int chainId, long height);

    /**
     * According to the chainIDAnd blockshashQuery a{@link BlockHeaderPo}
     *
     * @param chainId chainId/chain id
     * @param hash
     * @return
     */
    BlockHeaderPo query(int chainId, NulsHash hash);

    /**
     * According to the chainIDAnd height interval query{@link BlockHeaderPo}list
     *
     * @param chainId chainId/chain id
     * @param startHeight
     * @param endHeight
     * @return
     */
    List<BlockHeader> query(int chainId, long startHeight, long endHeight);

    /**
     * According to the chainIDRemove from height range{@link BlockHeaderPo}
     *
     * @param chainId chainId/chain id
     * @param height
     * @return
     * @throws Exception
     */
    boolean remove(int chainId, long height);

    /**
     * According to the chainIDDestroy database files
     *
     * @param chainId chainId/chain id
     * @return
     * @throws Exception
     */
    boolean destroy(int chainId);

    /**
     * According to the chainIDQuery the latest height of the chain
     *
     * @param chainId chainId/chain id
     * @return
     */
    long queryLatestHeight(int chainId);

    /**
     * According to the chainIDSet the latest height of the chain
     *
     * @param chainId chainId/chain id
     * @param height
     * @return
     */
    boolean setLatestHeight(int chainId, long height);

}
