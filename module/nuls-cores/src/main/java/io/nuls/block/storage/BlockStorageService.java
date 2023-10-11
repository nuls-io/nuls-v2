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
 * 区块存储服务
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 上午10:08
 */
public interface BlockStorageService {

    /**
     * 存储一个{@link BlockHeaderPo}
     *
     * @param chainId 链Id/chain id
     * @param blockHeader
     * @return
     * @throws Exception
     */
    boolean save(int chainId, BlockHeaderPo blockHeader);

    /**
     * 根据链ID和区块高度查询一个{@link BlockHeaderPo}
     *
     * @param chainId 链Id/chain id
     * @param height
     * @return
     */
    BlockHeaderPo query(int chainId, long height);

    /**
     * 根据链ID和区块hash查询一个{@link BlockHeaderPo}
     *
     * @param chainId 链Id/chain id
     * @param hash
     * @return
     */
    BlockHeaderPo query(int chainId, NulsHash hash);

    /**
     * 根据链ID和高度区间查询{@link BlockHeaderPo}列表
     *
     * @param chainId 链Id/chain id
     * @param startHeight
     * @param endHeight
     * @return
     */
    List<BlockHeader> query(int chainId, long startHeight, long endHeight);

    /**
     * 根据链ID和高度区间移除{@link BlockHeaderPo}
     *
     * @param chainId 链Id/chain id
     * @param height
     * @return
     * @throws Exception
     */
    boolean remove(int chainId, long height);

    /**
     * 根据链ID销毁数据库文件
     *
     * @param chainId 链Id/chain id
     * @return
     * @throws Exception
     */
    boolean destroy(int chainId);

    /**
     * 根据链ID查询该链的最新高度
     *
     * @param chainId 链Id/chain id
     * @return
     */
    long queryLatestHeight(int chainId);

    /**
     * 根据链ID设置该链的最新高度
     *
     * @param chainId 链Id/chain id
     * @param height
     * @return
     */
    boolean setLatestHeight(int chainId, long height);

}
