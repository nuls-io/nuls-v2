/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.service;


import io.nuls.base.data.NulsHash;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.model.bo.ContractTempTransaction;
import io.nuls.contract.model.po.ContractOfflineTxHashPo;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public interface ContractService {

    Result begin(int chainId, long blockHeight, long blockTime, String packingAddress, String preStateRoot);

    Result beginV8(int chainId, long blockHeight, long blockTime, String packingAddress, String preStateRoot);

    Result beforeEnd(int chainId, long blockHeight);

    Result end(int chainId, long blockHeight);

    Result endV8(int chainId, long blockHeight);

    Result packageEnd(int chainId, long blockHeight);

    Result packageEndV8(int chainId, long blockHeight);

    Result invokeContractOneByOne(int chainId, ContractTempTransaction tx);

    Result invokeContractOneByOneV8(int chainId, ContractTempTransaction tx);

    // add by pierre at 2022/6/2 p14
    Result invokeContractOneByOneV14(int chainId, ContractTempTransaction tx);

    Result saveContractExecuteResult(int chainId, NulsHash hash, ContractResult contractResult);

    Result deleteContractExecuteResult(int chainId, NulsHash hash);

    ContractResult getContractExecuteResult(int chainId, NulsHash hash);

    Result<ContractOfflineTxHashPo> getContractOfflineTxHashList(Integer chainId, String blockHash) throws NulsException;
}
