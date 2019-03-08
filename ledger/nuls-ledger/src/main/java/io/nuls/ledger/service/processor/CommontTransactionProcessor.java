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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * 普通交易处理
 * Created by lanjinsheng on 2018/12/29.
 */
@Component
public class CommontTransactionProcessor implements TxProcessor {

    @Autowired
    AccountStateService accountStateService;


    @Override
    public boolean processFromCoinData(CoinFrom coin, String nonce, String hash, AccountState accountState) {
        logger.debug("processFromCoinData address={},amount={},oldNonce={},updateNonce={},hash={} ", AddressTool.getStringAddressByBytes(coin.getAddress()), coin.getAmount(), HexUtil.encode(coin.getNonce()), nonce, hash);
        accountState.addTotalFromAmount(coin.getAmount());
        accountState.setNonce(nonce);
        return true;
    }

    @Override
    public boolean processToCoinData(CoinTo coin, String nonce, String hash, AccountState accountState) {
        logger.debug("processToCoinData address={},amount={},lockedTime={},hash={} ", AddressTool.getStringAddressByBytes(coin.getAddress()), coin.getAmount(), coin.getLockTime(), hash);
        accountState.addTotalToAmount(coin.getAmount());
        return true;
    }
}
