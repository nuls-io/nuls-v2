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
package io.nuls.ledger.service.processor;

import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

/**
 * 普通交易处理
 * Created by lanjinsheng on 2018/12/29.
 * @author lanjinsheng
 */
@Component
public class CommontTransactionProcessor implements TxCommonProcessor {

    @Autowired
    AccountStateService accountStateService;


    @Override
    public boolean processFromCoinData(CoinFrom coin, byte[] nonce,AccountState accountState) {
        accountState.addTotalFromAmount(coin.getAmount());
        accountState.setNonce(nonce);
        return true;
    }

    @Override
    public boolean processToCoinData(CoinTo coin,AccountState accountState) {
        accountState.addTotalToAmount(coin.getAmount());
        return true;
    }
}
