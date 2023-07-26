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

import io.nuls.core.exception.NulsException;
import io.nuls.ledger.model.po.BlockSnapshotTxs;

import java.util.List;
import java.util.Map;

/**
 * 数据存储接口
 *
 * @author lanjinsheng
 */
public interface LgBlockSyncRepository {


    /**
     * 获取区块高度
     *
     * @param chainId
     * @return
     */
    long getSyncBlockHeight(int chainId);

    /**
     * 保存或更新区块高度
     *
     * @param chainId
     * @param height
     */
    void saveOrUpdateSyncBlockHeight(int chainId, long height);


    void bakBlockInfosByHeight(int chainId, long height, BlockSnapshotTxs blockSnapshotTxs);

    BlockSnapshotTxs getBlockSnapshotTxs(int chainId, long height);

    void delBlockSnapshotTxs(int chainId, long height);

    void saveBlockHashByHeight(int chainId, long height, String hash);

    String getBlockHash(int chainId, long height);

    void delBlockHash(int chainId, long height);

    /**
     * 保存账本使用过的nonce
     * @param chainId
     * @param noncesMap
     * @throws Exception
     */
    void saveAccountNonces(int chainId, Map<byte[], byte[]> noncesMap) throws Exception;
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
    void saveAccountHash(int chainId, Map<byte[], byte[]> hashMap) throws Exception;

    /**
     * @param chainId
     * @param hashList
     * @throws Exception
     */
    void batchDeleteAccountHash(int chainId,  List<String> hashList) throws Exception;

    /**
     * @param chainId
     * @param noncesList
     * @throws Exception
     */
    public void batchDeleteAccountNonces(int chainId, List<String> noncesList) throws Exception;

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
