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
package io.nuls.contract.storage;


import io.nuls.base.data.CoinTo;
import io.nuls.contract.model.po.ContractOfflineTxHashPo;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2019-05-24
 */
public interface ContractRewardLogByConsensusStorageService {

    Set<String> getAssets(int chainId, byte[] address);
    Map<String, String> getAssetsMap(int chainId, byte[] address);
    BigInteger getAssetAmount(int chainId, byte[] address, int assetChainId, int assetId);
    Result save(int chainId, List<CoinTo> tos) throws Exception;
    Result delete(int chainId, List<CoinTo> tos) throws Exception;

}
