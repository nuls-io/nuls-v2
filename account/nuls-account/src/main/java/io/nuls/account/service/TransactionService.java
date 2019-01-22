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
 */

package io.nuls.account.service;

import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.base.data.Transaction;
import io.nuls.tools.basic.Result;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 账户相关交易接口定义
 * account service definition
 *
 * @author: qinyifeng
 */
public interface TransactionService {

    /**
     * 多地址转账
     *
     * @param currentChainId 当前链ID
     * @param fromList       从指定账户转出
     * @param toList         转出到指定账户
     * @param remark         备注
     * @return transfer transaction hash
     * @throws NulsException
     */
    String multipleAddressTransfer(int currentChainId, List<CoinDto> fromList, List<CoinDto> toList, String remark) throws NulsException, IOException, Exception;

    Transaction transferByAlias(int chainId, CoinDto from, CoinDto toList, String remark);

    /**
     * 校验该链是否有该资产
     *
     * @param chainId
     * @param assetId
     * @return
     */
    boolean assetExist(int chainId, int assetId);

}
