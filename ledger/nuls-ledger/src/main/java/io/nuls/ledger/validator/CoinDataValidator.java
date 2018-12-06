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
package io.nuls.ledger.validator;

import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * validate Coin Data
 * Created by wangkun23 on 2018/11/22.
 */
@Component
public class CoinDataValidator {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountStateService accountStateService;

    /**
     * 验证coin data
     *
     * @param address
     * @param amount
     * @param nonce
     * @return
     */
    public boolean validate(String address, int chainId, int assetId, BigInteger amount, long nonce) {
        AccountState accountState = accountStateService.getAccountState(address, chainId, assetId);
        if (accountState == null) {
            return false;
        }
        if (accountState.getBalance().compareTo(amount) == -1) {
            logger.info("account {} balance lacked {}", address, amount);
            return false;
        }
        //TODO nonce String hash
        long targetNonce = accountState.getNonce() + 1;
        if (nonce != targetNonce) {
            logger.info("account {} nonce {} incorrect", address, nonce);
            return false;
        }
        return true;
    }
}
