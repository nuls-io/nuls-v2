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

import io.nuls.ledger.model.po.AccountStateUncfd2Cfd;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;

import java.util.List;
import java.util.Map;

/**
 * @author lanjinsheng
 */
public interface UnconfirmedRepository {

    /**
     * put accountState to rocksdb
     *
     * @param key
     * @param accountStateUnconfirmed
     */
    void createAccountStateUnconfirmed(byte[] key, AccountStateUnconfirmed accountStateUnconfirmed);

    /**
     * 获取账号中未确认交易账本状态
     *
     * @param chainId
     * @param key
     * @return AccountStateUnconfirmed
     */
    AccountStateUnconfirmed getAccountStateUnconfirmed(int chainId, byte[] key);

    /**
     * 更新未确认交易账本状态
     *
     * @param key
     * @param nowAccountState
     * @throws Exception
     */
    void updateAccountStateUnconfirmed(byte[] key, AccountStateUnconfirmed nowAccountState) throws Exception;

    /**
     * delete unconfirmed state
     *
     * @param chainId
     * @param key
     * @throws Exception
     */
    void deleteAccountStateUnconfirmed(int chainId, byte[] key) throws Exception;

    /**
     * save unconfirmed tx
     *
     * @param chainId
     * @param key
     * @param txUnconfirmed
     * @throws Exception
     */
    void saveTxUnconfirmed(int chainId, byte[] key, TxUnconfirmed txUnconfirmed) throws Exception;

    /**
     * batch Delete unconfirmed tx
     *
     * @param chainId
     * @param keys
     * @throws Exception
     */
    void batchDeleteTxsUnconfirmed(int chainId, List<byte[]> keys) throws Exception;

    /**
     * batch save unconfirmed tx
     *
     * @param chainId
     * @param map
     * @throws Exception
     */
    void batchSaveTxsUnconfirmed(int chainId, Map<byte[], byte[]> map) throws Exception;

    /**
     * get unconfirmed tx by key
     *
     * @param chainId
     * @param key
     * @return
     * @throws Exception
     */
    TxUnconfirmed getTxUnconfirmed(int chainId, byte[] key) throws Exception;

    /**
     * delete unconfirmed tx
     *
     * @param chainId
     * @param key
     * @throws Exception
     */
    void deleteTxUnconfirmed(int chainId, byte[] key) throws Exception;

    /**
     * save unconfirmed to confirmed tx amount  object
     *
     * @param chainId
     * @param key
     * @param accountStateUncfd2Cfd
     * @throws Exception
     */
    void saveAccountStateUncfd2Cfd(int chainId, byte[] key, AccountStateUncfd2Cfd accountStateUncfd2Cfd) throws Exception;

    /**
     * delete unconfirmed to confirmed amount object
     *
     * @param chainId
     * @param key
     * @throws Exception
     */
    void deleteAccountStateUncfd2Cfd(int chainId, byte[] key) throws Exception;

    /**
     * get unconfirmed to confirmed amount object
     *
     * @param chainId
     * @param key
     * @return
     * @throws Exception
     */
    AccountStateUncfd2Cfd getAccountStateUncfd2Cfd(int chainId, byte[] key) throws Exception;

}
