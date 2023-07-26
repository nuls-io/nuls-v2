/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.service;

import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.sub.AccountStateSnapshot;

import java.util.List;

/**
 * 账本信息业务功能处理
 * Created by wangkun23 on 2018/11/29.
 *
 * @author lanjinsheng
 */
public interface AccountStateService {

    /**
     * 不用同步锁获取账户信息
     *
     * @param address        address
     * @param addressChainId addressChainId
     * @param assetChainId   assetChainId
     * @param assetId        assetId
     * @return AccountState
     */
    AccountState getAccountState(String address, int addressChainId, int assetChainId, int assetId);


    /**
     * 获取账本信息，并重新计算冻结的金额的信息
     *
     * @param address
     * @param addressChainId
     * @param assetChainId
     * @param assetId
     * @return
     */
    AccountState getAccountStateReCal(String address, int addressChainId, int assetChainId, int assetId);

    /**
     * 回滚账户信息
     *
     * @param chainId
     * @param preAccountStates
     * @throws Exception
     */
    void rollAccountState(int chainId, List<AccountStateSnapshot> preAccountStates) throws Exception;

}
