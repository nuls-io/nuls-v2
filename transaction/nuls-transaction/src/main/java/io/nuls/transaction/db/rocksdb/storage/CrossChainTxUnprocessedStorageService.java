/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.transaction.model.bo.CrossChainTx;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2018-12-27
 */
public interface CrossChainTxUnprocessedStorageService {

    /**
     * 新增或修改跨链交易数据
     *
     * @param chainId
     * @param ctx
     * @return
     */
    boolean putTx(int chainId, CrossChainTx ctx);

    /**
     * 是否已存在交易
     * @param chainId
     * @param hash
     * @return
     */
    CrossChainTx getTx(int chainId, NulsDigestData hash);

    /**
     * 根据交易hash批量删除
     * @param chainId
     * @param ctxList
     * @return 删除是否成功
     */
    boolean removeTxList(int chainId, List<CrossChainTx> ctxList);


    /**
     * 查询指定链下所有跨链交易
     * Query all cross-chain transactions in the specified chain
     *
     * @param chainId
     * @return
     */
    List<CrossChainTx> getTxList(int chainId);
}
