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

import io.nuls.ledger.model.ChainHeight;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.BlockSnapshotAccounts;
import io.nuls.core.exception.NulsException;

import java.util.List;
import java.util.Map;

/**
 * 数据存储接口
 *
 * @author lanjinsheng
 */
public interface Repository {


    /**
     * put accountState to rocksdb
     *
     * @param key
     * @param accountState
     */
    void createAccountState(byte[] key, AccountState accountState);

    /**
     * 获取账号账本信息
     * Getting Account Book Information
     *
     * @param chainId
     * @param key
     * @return AccountState
     */
    AccountState getAccountState(int chainId, byte[] key);

    /**
     * 更新账号账本信息
     * update Account ledger Information
     *
     * @param key
     * @param nowAccountState
     * @throws Exception
     */
    void updateAccountState(byte[] key, AccountState nowAccountState) throws Exception;

    /**
     * 批量更新账号账本信息
     * batch update Account ledger Information
     *
     * @param addressChainId
     * @param accountStateMap
     * @throws Exception
     */
    void batchUpdateAccountState(int addressChainId, Map<byte[], byte[]> accountStateMap) throws Exception;

    /**
     * 删除区块快照
     *
     * @param chainId
     * @param height
     * @throws Exception
     */
    void delBlockSnapshot(int chainId, long height) throws Exception;

    /**
     * 存储区块快照
     *
     * @param chainId
     * @param height
     * @param blockSnapshotAccounts
     * @throws Exception
     */
    void saveBlockSnapshot(int chainId, long height, BlockSnapshotAccounts blockSnapshotAccounts) throws Exception;

    /**
     * 获取区块快照
     *
     * @param chainId
     * @param height
     * @return BlockSnapshotAccounts
     */
    BlockSnapshotAccounts getBlockSnapshot(int chainId, long height);


    /**
     * 获取区块高度
     *
     * @param chainId
     * @return
     */
    long getBlockHeight(int chainId);

    /**
     * 保存或更新区块高度
     *
     * @param chainId
     * @param height
     */
    void saveOrUpdateBlockHeight(int chainId, long height);

    /**
     * 获取区块高度对象
     *
     * @return
     */
    List<ChainHeight> getChainsBlockHeight();


    /**
     * 保存账本使用过的nonce
     *
     * @param chainId
     * @param noncesMap
     * @throws Exception
     */
    void saveAccountNonces(int chainId, Map<String, Integer> noncesMap) throws Exception;

    /**
     * 删除账本存储的nonce
     *
     * @param chainId
     * @param accountNonceKey
     * @throws Exception
     */
    void deleteAccountNonces(int chainId, String accountNonceKey) throws Exception;

    /**
     * 判断账号的nonce是否已被使用
     *
     * @param chainId
     * @param accountNonceKey
     * @return
     * @throws Exception
     */
    boolean existAccountNonce(int chainId, String accountNonceKey) throws Exception;


    /**
     * 保存账本使用过的hash
     *
     * @param chainId
     * @param hashMap
     * @throws Exception
     */
    void saveAccountHash(int chainId, Map<String, Integer> hashMap) throws Exception;

    /**
     *
     * @param chainId
     * @param hashMap
     * @throws Exception
     */
    void batchDeleteAccountHash(int chainId, Map<String, Integer> hashMap) throws Exception;
    /**
     *
     * @param chainId
     * @param noncesMap
     * @throws Exception
     */
    public void batchDeleteAccountNonces(int chainId, Map<String, Integer> noncesMap) throws Exception;
    /**
     * 删除账本存储的hash
     *
     * @param chainId
     * @param hash
     * @throws Exception
     */
    void deleteAccountHash(int chainId, String hash) throws Exception;

    /**
     * 判断账号的hash是否已被使用
     *
     * @param chainId
     * @param hash
     * @return
     * @throws Exception
     */
    boolean existAccountHash(int chainId, String hash) throws Exception;

    /**
     * 初始化数据表
     *
     * @throws NulsException
     */
    void initTableName() throws NulsException;
}
