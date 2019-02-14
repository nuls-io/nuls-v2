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
package io.nuls.ledger.db;

import io.nuls.ledger.model.ChainHeight;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.BlockSnapshotAccounts;

import java.util.List;

public interface Repository {


    /**
     * put accountState to rocksdb
     *
     * @param key
     * @param accountState
     */
    void createAccountState(byte[] key, AccountState accountState);

    /**
     * @param chainId
     * @param key
     * @return AccountState
     */
    AccountState getAccountState(int chainId, byte[] key);

    /**
     * @param key
     * @param nowAccountState
     */
    void updateAccountState(byte[] key, AccountState nowAccountState) throws Exception;

    /**
     *
     * @param chainId
     * @param height
     */
    void delBlockSnapshot(int chainId,long height) throws Exception;
    /**
     *
     * @param chainId
     * @param height
     */
    void saveBlockSnapshot(int chainId,long height,BlockSnapshotAccounts blockSnapshotAccounts) throws Exception;
    /**
     *
     * @param chainId
     * @param height
     * @return BlockSnapshotAccounts
     */
    BlockSnapshotAccounts getBlockSnapshot(int chainId, long height);


    /**
     *
     * @param chainId
     * @return
     */
    long getBlockHeight(int chainId);

    /**
     *
     * @param chainId
     * @param height
     */
    void saveOrUpdateBlockHeight(int chainId,long height);

    /**
     *
     * @return
     */
    List<ChainHeight> getChainsBlockHeight();
}
