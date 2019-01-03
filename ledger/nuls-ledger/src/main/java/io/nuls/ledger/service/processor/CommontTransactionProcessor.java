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
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 普通交易处理
 * Created by lanjinsheng on 2018/12/29.
 */
@Service
public class CommontTransactionProcessor implements TxProcessor {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AccountStateService accountStateService;


    @Override
    public void processFromCoinData(CoinFrom coin,String nonce,String hash) {
        //TODO:账户更新锁未使用
        String address = AddressTool.getStringAddressByBytes(coin.getAddress());
        int assetChainId = coin.getAssetsChainId();
        int assetId = coin.getAssetsId();
        AccountState accountState  = accountStateService.getAccountState(address,assetChainId,assetId);
        accountState.addTotalFromAmount(coin.getAmount());
        accountState.updateConfirmedNonce(nonce);
        //提交账号记录
        accountStateService.updateAccountState(address,assetChainId,assetId,accountState);
    }

    @Override
    public void processToCoinData(CoinTo coin,String nonce,String hash) {
        //TODO:账户更新锁未使用
        String address = AddressTool.getStringAddressByBytes(coin.getAddress());
        int assetChainId = coin.getAssetsChainId();
        int assetId = coin.getAssetsId();
        AccountState accountState  = accountStateService.getAccountState(address,assetChainId,assetId);
        accountState.addTotalToAmount(coin.getAmount());
        //提交账号记录
        accountStateService.updateAccountState(address,assetChainId,assetId,accountState);
    }
}
