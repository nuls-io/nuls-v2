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
package io.nuls.ledger.service;

import io.nuls.base.data.Block;
import io.nuls.ledger.model.ChainHeight;

import java.util.List;

/**
 * @author lan
 * @description
 * @date 2019/02/14
 **/
public interface BlockDataService {
    /**
     * 初始化区块的数据清理
     *
     * @throws Exception
     */
    void initBlockDatas() throws Exception;

    /**
     * @throws Exception
     */
    void syncBlockHeight() throws Exception;

    //获取确认高度
    List<ChainHeight> getChainsBlockHeight() throws Exception;

    /**
     * 同步区块
     *
     * @param chainId
     * @param height
     * @param block
     * @throws Exception
     */
    void syncBlockDatas(int chainId, long height, Block block) throws Exception;

    /**
     * @param addressChainId
     * @param height
     */
    public void clearSurplusBakDatas(int addressChainId, long height);

    /**
     * 执行区块同步的数据回滚
     *
     * @param chainId
     * @param height
     * @throws Exception
     */
    void rollBackBlockDatas(int chainId, long height) throws Exception;

    /**
     * @param chainId
     * @param height
     * @return
     * @throws Exception
     */
    String getBlockHashByHeight(int chainId, long height) throws Exception;

    /**
     * @param chainId
     * @return
     * @throws Exception
     */
    long currentSyncHeight(int chainId) throws Exception;

}
