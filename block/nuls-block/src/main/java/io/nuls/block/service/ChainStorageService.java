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
import io.nuls.base.data.NulsDigestData;
import io.nuls.tools.exception.NulsException;

import java.util.List;

/**
 * 链存储服务
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 上午10:08
 */
public interface ChainStorageService {

    /**
     * 存储一条链
     *
     * @param chainId
     * @param blocks
     * @return
     * @throws Exception
     */
    boolean save(int chainId, List<Block> blocks);

    /**
     * 存储一个区块
     *
     * @param chainId
     * @param block
     * @return
     * @throws Exception
     */
    boolean save(int chainId, Block block);

    /**
     * 查询一个区块
     *
     * @param chainId
     * @param hash
     * @return
     * @throws NulsException
     */
    Block query(int chainId, NulsDigestData hash);

    /**
     * 查询一条链
     *
     * @param chainId
     * @param hashList
     * @return
     * @throws NulsException
     */
    List<Block> query(int chainId, List<NulsDigestData> hashList);

    /**
     * 移除一条链
     *
     * @param chainId
     * @param hashList
     * @return
     * @throws Exception
     */
    boolean remove(int chainId, List<NulsDigestData> hashList);

    /**
     * 移除一个区块
     *
     * @param chainId
     * @param hash
     * @return
     * @throws Exception
     */
    boolean remove(int chainId, NulsDigestData hash);

    /**
     * 销毁链存储
     *
     * @param chainId
     * @return
     * @throws Exception
     */
    boolean destroy(int chainId);

}
