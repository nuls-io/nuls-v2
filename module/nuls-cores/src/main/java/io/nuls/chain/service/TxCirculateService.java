/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.dto.CoinDataAssets;
import io.nuls.chain.model.po.ChainAsset;
import io.nuls.core.exception.NulsException;

import java.util.List;

/**
 * 资产流通接口
 *
 * @author lan
 * @date 2019/02/21
 **/
public interface TxCirculateService {
    ChainEventResult circulateCommit(List<Transaction> txs) throws Exception;

    /**
     * 获取资产列表
     * @param coinDataByte
     * @return
     * @throws NulsException
     */
    List<CoinDataAssets> getChainAssetList(byte[] coinDataByte) throws NulsException;

    /**
     * 查询链上资产流通量
     * @param circulateChainId
     * @param assetChainId
     * @param assetId
     * @return
     * @throws NulsException
     */
    ChainAsset getCirculateChainAsset(int circulateChainId,int assetChainId,int assetId) throws Exception;

}
