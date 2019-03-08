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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.ChainHeight;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.BlockSnapshotAccounts;
import io.nuls.ledger.model.po.BlockTxs;
import io.nuls.ledger.storage.DataBaseArea;
import io.nuls.ledger.storage.InitDB;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.List;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Service
public class RepositoryImpl implements Repository, InitDB, InitializingBean {

    public RepositoryImpl() {

    }

    /**
     * create accountState to rocksdb
     *
     * @param key
     * @param accountState
     */
    @Override
    public void createAccountState(byte[] key, AccountState accountState) {
        try {
            initChainDb(accountState.getAddressChainId());
            RocksDBService.put(getLedgerAccountTableName(accountState.getAddressChainId()), key, accountState.serialize());
        } catch (Exception e) {
            logger.error("createAccountState serialize error.", e);
        }
    }

    /**
     * update accountState to rocksdb
     *
     * @param key
     * @param nowAccountState
     */
    @Override
    public void updateAccountState(byte[] key, AccountState nowAccountState) throws Exception {
        //update account
        LoggerUtil.logger.debug("updateAccountState hash={},nonce={},UnconfirmedNoncesNum={}",nowAccountState.getTxHash(),nowAccountState.getNonce(),nowAccountState.getUnconfirmedNonces().size());
        LoggerUtil.logger.debug("updateAccountState address={},addressChainId={},assetChainId={},assetId={},getAvailableAmount={}," +
                        "getFreezeTotal={},getUnconfirmedAmount={},getUnconfirmedFreezeAmount={}",
                nowAccountState.getAddress(),nowAccountState.getAddressChainId(),nowAccountState.getAssetChainId(),nowAccountState.getAssetId(),
                nowAccountState.getAvailableAmount(),nowAccountState.getFreezeTotal(),nowAccountState.getUnconfirmedAmount(),nowAccountState.getUnconfirmedFreezeAmount());
        RocksDBService.put(getLedgerAccountTableName(nowAccountState.getAddressChainId()), key, nowAccountState.serialize());

    }


    @Override
    public void delBlockSnapshot(int chainId, long height) throws Exception {
        RocksDBService.delete(getBlockSnapshotTableName(chainId), ByteUtils.longToBytes(height));
    }

    @Override
    public void saveBlockSnapshot(int chainId, long height, BlockSnapshotAccounts blockSnapshotAccounts) throws Exception {
        RocksDBService.put(getBlockSnapshotTableName(chainId), ByteUtils.longToBytes(height), blockSnapshotAccounts.serialize());

    }

    @Override
    public BlockSnapshotAccounts getBlockSnapshot(int chainId, long height) {
        byte[] stream = RocksDBService.get(getBlockSnapshotTableName(chainId), ByteUtils.longToBytes(height));
        if (stream == null) {
            return null;
        }
        BlockSnapshotAccounts blockSnapshotAccounts = new BlockSnapshotAccounts();
        try {
            blockSnapshotAccounts.parse(new NulsByteBuffer(stream));
        } catch (NulsException e) {
            logger.error("getAccountState serialize error.", e);
        }
        return blockSnapshotAccounts;
    }


    /**
     * get accountState from rocksdb
     *
     * @param key
     * @return
     */
    @Override
    public AccountState getAccountState(int chainId, byte[] key) {
        byte[] stream = RocksDBService.get(getLedgerAccountTableName(chainId), key);
        if (stream == null) {
            return null;
        }
        AccountState accountState = new AccountState();
        try {
            accountState.parse(new NulsByteBuffer(stream));
        } catch (NulsException e) {
            logger.error("getAccountState serialize error.", e);
        }
        return accountState;
    }

    @Override
    public long getBlockHeight(int chainId) {
        byte[] stream = RocksDBService.get(getChainsHeightTableName(), ByteUtils.intToBytes(chainId));
        if (stream == null) {
            return -1;
        }
        try {
            long height = ByteUtils.byteToLong(stream);
            return height;
        } catch (Exception e) {
            logger.error("getBlockHeight serialize error.", e);
        }
        return -1;
    }

    @Override
    public void saveOrUpdateBlockHeight(int chainId, long height) {
        try {
            RocksDBService.put(getChainsHeightTableName(), ByteUtils.intToBytes(chainId), ByteUtils.longToBytes(height));
        } catch (Exception e) {
            logger.error("saveBlockHeight serialize error.", e);
        }

    }

    @Override
    public List<ChainHeight> getChainsBlockHeight() {
        List<Entry<byte[], byte[]>> list = RocksDBService.entryList(getChainsHeightTableName());
        List<ChainHeight> rtList = new ArrayList<>();
        if (null == list || 0 == list.size()) {
            return null;
        }
        for (Entry<byte[], byte[]> entry : list) {
            ChainHeight chainHeight = new ChainHeight();
            chainHeight.setChainId(ByteUtils.bytesToInt(entry.getKey()));
            chainHeight.setBlockHeight(ByteUtils.byteToLong(entry.getValue()));
            rtList.add(chainHeight);
        }
        return rtList;
    }


    String getLedgerAccountTableName(int chainId) {
        return DataBaseArea.TB_LEDGER_ACCOUNT + chainId;
    }

    String getBlockSnapshotTableName(int chainId) {
        return DataBaseArea.TB_LEDGER_ACCOUNT_BLOCK_SNAPSHOT + chainId;
    }

    public String getChainsHeightTableName() {
        return DataBaseArea.TB_LEDGER_BLOCK_HEIGHT;
    }

    String getBlockTableName(int chainId) {
        return DataBaseArea.TB_LEDGER_BLOCKS + chainId;
    }

    /**
     * 初始化数据库
     */
    public void initChainDb(int addressChainId) {
        try {
            if (!RocksDBService.existTable(getLedgerAccountTableName(addressChainId))) {
                RocksDBService.createTable(getLedgerAccountTableName(addressChainId));
            }
            if (!RocksDBService.existTable(getBlockSnapshotTableName(addressChainId))) {
                RocksDBService.createTable(getBlockSnapshotTableName(addressChainId));
            }

        } catch (Exception e) {
            logger.error(e);
        }
    }


    @Override
    public void saveBlock(int chainId, long height, BlockTxs blockTxs) {
        try {
            String table = getBlockTableName(chainId);
            if (!RocksDBService.existTable(table)) {
                RocksDBService.createTable(table);
            }
            RocksDBService.put(table, ByteUtils.longToBytes(height), blockTxs.serialize());
            RocksDBService.delete(table, ByteUtils.longToBytes(height - LedgerConstant.CACHE_ACCOUNT_BLOCK));
        } catch (Exception e) {
            logger.error("saveBlock serialize error.", e);
        }

    }

    @Override
    public BlockTxs getBlock(int chainId, long height) {
        try {

            byte[] stream = RocksDBService.get(getBlockTableName(chainId), ByteUtils.longToBytes(height));
            if (stream == null) {
                return null;
            }
            BlockTxs blockTxs = new BlockTxs();
            blockTxs.parse(new NulsByteBuffer(stream));
            return blockTxs;
        } catch (Exception e) {
            logger.error("getBlock serialize error.", e);
        }
        return null;
    }

    @Override
    public void afterPropertiesSet() throws NulsException {

    }

    @Override
    public void initTableName() throws NulsException {
        try {
            if (!RocksDBService.existTable(getChainsHeightTableName())) {
                RocksDBService.createTable(getChainsHeightTableName());
            } else {
                logger.info("table {} exist.", getChainsHeightTableName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NulsException(e);
        }
    }
}
