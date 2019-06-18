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


import io.nuls.contract.model.po.ContractTokenTransferInfoPo;
import io.nuls.core.basic.Result;

import java.math.BigInteger;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
public interface ContractTxService {

    Result contractCreateTx(int chainId, String sender, String alias, Long gasLimit, Long price,
                            byte[] contractCode, String[][] args, String password, String remark);

    Result validateContractCreateTx(int chainId, byte[] sender, Long gasLimit, Long price, byte[] contractCode, String[][] args);

    Result contractCallTx(int chainId, String sender, BigInteger value, Long gasLimit, Long price, String contractAddress,
                          String methodName, String methodDesc, String[][] args, String password, String remark);

    Result validateContractCallTx(int chainId, byte[] senderBytes, BigInteger value, Long gasLimit, Long price, byte[] contractAddressBytes,
                                  String methodName, String methodDesc, String[][] args);

    Result callTxFee(int chainId, String sender, BigInteger value, Long gasLimit, Long price, String contractAddress,
                     String methodName, String methodDesc, String[][] args, String remark);

    Result contractDeleteTx(int chainId, String sender, String contractAddress, String password, String remark);

    Result validateContractDeleteTx(int chainId, String sender, String contractAddress);

    Result<List<ContractTokenTransferInfoPo>> getTokenTransferInfoList(int chainId, String address);
}
