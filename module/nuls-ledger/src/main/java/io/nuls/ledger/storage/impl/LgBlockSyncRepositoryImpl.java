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
package io.nuls.ledger.storage.impl;

import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.BlockSnapshotTxs;
import io.nuls.ledger.storage.DataBaseArea;
import io.nuls.ledger.storage.LgBlockSyncRepository;
import io.nuls.ledger.utils.LoggerUtil;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * @author lanjinsheng
 */
@Service
public class LgBlockSyncRepositoryImpl implements LgBlockSyncRepository, InitializingBean {

    public LgBlockSyncRepositoryImpl() {

    }


    @Override
    public void saveOrUpdateSyncBlockHeight(int chainId, long height) {
        try {
            RocksDBService.put(getSyncBlockTableName(chainId), ByteUtils.toBytes("height-" + chainId, LedgerConstant.DEFAULT_ENCODING), ByteUtils.longToBytes(height));
        } catch (Exception e) {
            logger(chainId).error("saveOrUpdateSyncBlockHeight serialize error.", e);
        }
    }

    @Override
    public void bakBlockInfosByHeight(int chainId, long height, BlockSnapshotTxs blockSnapshotTxs) {
        try {
            RocksDBService.put(getSyncBlockTableName(chainId), ByteUtils.toBytes("bak-" + height, LedgerConstant.DEFAULT_ENCODING), blockSnapshotTxs.serialize());
        } catch (Exception e) {
            logger(chainId).error("bakBlockInfosByHeight serialize error.", e);
        }
    }

    @Override
    public BlockSnapshotTxs getBlockSnapshotTxs(int chainId, long height) {
        byte[] snapshot = RocksDBService.get(getSyncBlockTableName(chainId), ByteUtils.toBytes("bak-" + height, LedgerConstant.DEFAULT_ENCODING));
        if (null != snapshot) {
            try {
                BlockSnapshotTxs blockSnapshotTxs = new BlockSnapshotTxs();
                blockSnapshotTxs.parse(snapshot, 0);
                return blockSnapshotTxs;
            } catch (NulsException e) {
                LoggerUtil.COMMON_LOG.error(e);
            }
        }
        return null;
    }

    @Override
    public void delBlockSnapshotTxs(int chainId, long height) {
        try {
            RocksDBService.delete(getSyncBlockTableName(chainId), ByteUtils.toBytes("bak-" + height, LedgerConstant.DEFAULT_ENCODING));
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
        }
    }

    @Override
    public void saveBlockHashByHeight(int chainId, long height, String hash) {
        try {
            RocksDBService.put(getSyncBlockTableName(chainId), ByteUtils.toBytes("bHash-" + height, LedgerConstant.DEFAULT_ENCODING), ByteUtils.toBytes(hash, LedgerConstant.DEFAULT_ENCODING));
        } catch (Exception e) {
            logger(chainId).error("saveBlockHashByHeight serialize error.", e);
        }
    }

    @Override
    public String getBlockHash(int chainId, long height) {
        String table = getSyncBlockTableName(chainId);
        byte[] hashBytes = RocksDBService.get(table, ByteUtils.toBytes("bHash-" + height, LedgerConstant.DEFAULT_ENCODING));
        if (null != hashBytes) {
            try {
                return new String(hashBytes, LedgerConstant.DEFAULT_ENCODING);
            } catch (UnsupportedEncodingException e) {
                logger(chainId).error("getBlockHash serialize error.", e);
            }
        }
        return null;
    }

    @Override
    public void delBlockHash(int chainId, long height) {
        try {
            RocksDBService.delete(getSyncBlockTableName(chainId), ByteUtils.toBytes("bHash-" + height, LedgerConstant.DEFAULT_ENCODING));
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
        }
    }

    @Override
    public long getSyncBlockHeight(int chainId) {
        String table = getSyncBlockTableName(chainId);
        byte[] heightBytes = RocksDBService.get(table, ByteUtils.toBytes("height-" + chainId, LedgerConstant.DEFAULT_ENCODING));
        if (null == heightBytes) {
            return -1;
        }
        return ByteUtils.byteToLong(heightBytes);
    }

    String getChainTableName(String tableName, int chainId) {
        return tableName + "_" + chainId;
    }


    public String getSyncBlockTableName(int chainId) {
        return getChainTableName(DataBaseArea.TB_SYNC_BLOCK, chainId);
    }

    String getLedgerNonceTableName(int chainId) {
        return getChainTableName(DataBaseArea.TB_LEDGER_NONCES, chainId);
    }

    String getLedgerHashTableName(int chainId) {
        return getChainTableName(DataBaseArea.TB_LEDGER_HASH, chainId);
    }

    /**
     * 初始化数据库
     */
    public void initChainDb(int addressChainId) {
        try {
            if (!RocksDBService.existTable(getLedgerNonceTableName(addressChainId))) {
                RocksDBService.createTable(getLedgerNonceTableName(addressChainId));
            }
            if (!RocksDBService.existTable(getLedgerHashTableName(addressChainId))) {
                RocksDBService.createTable(getLedgerHashTableName(addressChainId));
            }
            if (!RocksDBService.existTable(getSyncBlockTableName(addressChainId))) {
                RocksDBService.createTable(getSyncBlockTableName(addressChainId));
            }
        } catch (Exception e) {
            logger(addressChainId).error(e);
        }
    }

    @Override
    public void afterPropertiesSet() throws NulsException {

    }

    @Override
    public void initTableName() throws NulsException {

    }

    @Override
    public void saveAccountNonces(int chainId, Map<String, Integer> noncesMap) throws Exception {
        String table = getLedgerNonceTableName(chainId);
        if (!RocksDBService.existTable(table)) {
            RocksDBService.createTable(table);
        }
        Map<byte[], byte[]> saveMap = new HashMap<>(1024);
        for (Map.Entry<String, Integer> m : noncesMap.entrySet()) {
            saveMap.put(ByteUtils.toBytes(m.getKey(), LedgerConstant.DEFAULT_ENCODING), ByteUtils.intToBytes(m.getValue()));
        }
        if (saveMap.size() > 0) {
            RocksDBService.batchPut(table, saveMap);
        }
    }

    @Override
    public void deleteAccountNonces(int chainId, String accountNonceKey) throws Exception {
        RocksDBService.delete(getLedgerNonceTableName(chainId), ByteUtils.toBytes(accountNonceKey, LedgerConstant.DEFAULT_ENCODING));
    }

    @Override
    public boolean existAccountNonce(int chainId, String accountNonceKey) throws Exception {
        return RocksDBService.keyMayExist(getLedgerNonceTableName(chainId), ByteUtils.toBytes(accountNonceKey, LedgerConstant.DEFAULT_ENCODING));
//        return (null != RocksDBService.get(getLedgerNonceTableName(chainId), ByteUtils.toBytes(accountNonceKey, LedgerConstant.DEFAULT_ENCODING)));
    }


    @Override
    public void saveAccountHash(int chainId, Map<byte[], byte[]> hashMap) throws Exception {
        String table = getLedgerHashTableName(chainId);
        if (hashMap.size() > 0) {
            RocksDBService.batchPut(table, hashMap);
        }
    }

    @Override
    public void batchDeleteAccountHash(int chainId, List<String> hashList) throws Exception {
        String table = getLedgerHashTableName(chainId);

        List<byte[]> list = new ArrayList<>();
        for (String hash : hashList) {
            list.add(ByteUtils.toBytes(hash, LedgerConstant.DEFAULT_ENCODING));
        }
        if (list.size() > 0) {
            RocksDBService.deleteKeys(table, list);
        }
    }

    @Override
    public void deleteAccountHash(int chainId, String hash) throws Exception {
        RocksDBService.delete(getLedgerHashTableName(chainId), ByteUtils.toBytes(hash, LedgerConstant.DEFAULT_ENCODING));
    }

    @Override
    public void batchDeleteAccountNonces(int chainId, List<String> noncesList) throws Exception {
        String table = getLedgerNonceTableName(chainId);
        List<byte[]> list = new ArrayList<>();
        for (String addressNonce : noncesList) {
            list.add(ByteUtils.toBytes(addressNonce, LedgerConstant.DEFAULT_ENCODING));
        }
        if (list.size() > 0) {
            RocksDBService.deleteKeys(table, list);
        }
    }

    @Override
    public boolean existAccountHash(int chainId, String hash) throws Exception {
        return RocksDBService.keyMayExist(getLedgerHashTableName(chainId),ByteUtils.toBytes(hash, LedgerConstant.DEFAULT_ENCODING));
//        return (null != RocksDBService.get(getLedgerHashTableName(chainId), ByteUtils.toBytes(hash, LedgerConstant.DEFAULT_ENCODING)));
    }
}
