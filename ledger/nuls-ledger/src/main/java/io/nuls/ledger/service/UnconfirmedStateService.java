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
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.UnconfirmedNonce;

import java.util.List;

/**
 * Created by lan on 2018/12/30
 * @author lanjinsheng
 */
public interface UnconfirmedStateService {

    /**
     * 重计算未确认账本信息
     * @param accountState
     * @return
     */
    AccountStateUnconfirmed getUnconfirmedInfoReCal(AccountState accountState);

    /**
     * 刷新未确认账本的nonce值
     * @param accountState
     * @return
     */
    AccountStateUnconfirmed getUnconfirmedNonceReCal(AccountState accountState);


    /**
     * 回滚时候进行未确认账本信息的合并
     * @param addressChainId
     * @param assetKey
     * @param accountState
     * @param txHashList
     */
    void mergeUnconfirmedNonce(int addressChainId, String assetKey,List<UnconfirmedNonce> accountState, List<String> txHashList);

    /**
     * 回滚未确认账本交易
     * @param addressChainId
     * @param assetKey
     * @param nonce
     * @param txHash
     * @return
     */
    boolean rollUnconfirmTx(int addressChainId, String assetKey, String nonce, String txHash);

}
