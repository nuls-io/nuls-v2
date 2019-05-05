/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.chain.service;

import io.nuls.base.data.Transaction;
import io.nuls.chain.model.po.BlockHeight;
import io.nuls.chain.model.po.CacheDatas;

import java.util.List;

/**
 * @author lan
 * @description
 * @date 2019/02/14
 **/
public interface CacheDataService {
    void initBlockDatas() throws Exception;
    void bakBlockTxs(int chainId, long preHeight, long height, List<Transaction> txList, boolean isCirculate) throws Exception;
    void rollBlockTxs(int chainId, long height) throws Exception;
    BlockHeight getBlockHeight(int chainId) throws Exception;
    void beginBakBlockHeight(int chainId, long blockHeight) throws Exception;
    void endBakBlockHeight(int chainId, long blockHeight) throws Exception;

    CacheDatas getCacheDatas(long height) throws Exception;
}
