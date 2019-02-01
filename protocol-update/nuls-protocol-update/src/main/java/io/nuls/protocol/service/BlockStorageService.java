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

package io.nuls.protocol.service;

import io.nuls.base.data.BlockHeader;

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
     * 存储一个{@link BlockHeader}
     *
     * @param chainId
     * @param blockHeader
     * @return
     * @throws Exception
     */
    boolean save(int chainId, BlockHeader blockHeader);

    /**
     * 根据链ID和高度区间查询{@link BlockHeader}列表
     *
     * @param chainId
     * @param startHeight
     * @param endHeight
     * @return
     */
    List<BlockHeader> query(int chainId, long startHeight, long endHeight);

    /**
     * 根据链ID和高度区间移除{@link BlockHeader}
     *
     * @param chainId
     * @param height
     * @return
     * @throws Exception
     */
    boolean remove(int chainId, long height);

}
