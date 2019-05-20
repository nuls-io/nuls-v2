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
package io.nuls.chain.rpc.call;

import io.nuls.base.data.Transaction;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.ChainAssetTotalCirculate;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;

import java.util.List;

/**
 * 调用外部接口
 *
 * @author lan
 */
public interface RpcService {
    /**
     * 跨链种子节点获取
     *
     * @return
     */
    String getCrossChainSeeds();

    long getMainNetMagicNumber();

    /**
     * 注册交易验证器
     *
     * @return
     */
    boolean regTx();

    ErrorCode newTx(Transaction tx);

    boolean createCrossGroup(BlockChain blockChain);

    boolean destroyCrossGroup(BlockChain blockChain);

    boolean requestCrossIssuingAssets(int chainId, String assetIds);

    /**
     * 获取账户余额
     *
     * @param address
     * @param accountBalance
     * @return
     */
    ErrorCode getCoinData(String address, AccountBalance accountBalance);

    List<ChainAssetTotalCirculate> getLgAssetsById(int chainId, String assetIds);

    /**
     * 交易签名
     * transaction signature
     *
     * @param chainId
     * @param address
     * @param password
     * @param tx
     */
    ErrorCode transactionSignature(int chainId, String address, String password, Transaction tx) throws NulsException;

    /**
     * @return
     */
    long getTime();

}

