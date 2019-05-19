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
package io.nuls.ledger.storage;

import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;

import java.math.BigInteger;
import java.util.Map;

/**
 * @author lanjinsheng
 */
public interface UnconfirmedRepository {

    AccountStateUnconfirmed getMemAccountStateUnconfirmed(int chainId, String accountKey);

    void delMemAccountStateUnconfirmed(int chainId, String accountKey);

    void saveMemAccountStateUnconfirmed(int chainId, String accountKey, AccountStateUnconfirmed accountStateUnconfirmed);

    TxUnconfirmed getMemUnconfirmedTx(int chainId, String accountKey, String nonceKey);

    void delMemUnconfirmedTx(int chainId, String accountKey, String nonceKey);

    void saveMemUnconfirmedTxs(int chainId, String accountKey, Map<String, TxUnconfirmed> map);

    void saveMemUnconfirmedTx(int chainId, String accountKey, String nonce, TxUnconfirmed txUnconfirmed);

    void addUncfd2Cfd(int chainId, String accountKey, BigInteger addAmount);

    void clearMemUnconfirmedTxs(int chainId, String accountKey, TxUnconfirmed txUnconfirmed);

    void clearMemUnconfirmedTxs(int chainId, String accountKey);
}
