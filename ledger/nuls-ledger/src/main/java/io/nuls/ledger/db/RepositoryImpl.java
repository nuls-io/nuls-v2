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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.utils.LedgerUtils;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Service
public class RepositoryImpl implements Repository {
    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * create accountState to rocksdb
     *
     * @param key
     * @param accountState
     */
    @Override
    public void createAccountState(byte[] key, AccountState accountState) {
        try {
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, key, accountState.serialize());
        } catch (Exception e) {
            logger.error("createAccountState serialize error.", e);
        }
    }

    /**
     * update accountState to rocksdb
     * @param key
     * @param preAccountState
     * @param nowAccountState
     */
    @Override
    public void updateAccountStateAndSnapshot(String key,AccountState preAccountState,AccountState nowAccountState){
        try {
            //bak  account Snapshot
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT_SNAPSHOT, LedgerUtils.getSnapshotKey(key,nowAccountState.getTxHash(),nowAccountState.getHeight()), preAccountState.serialize());
            //清除过期的snapshot数据,height = height - 100以前的数据
            RocksDBService.delete(DataBaseArea.TB_LEDGER_ACCOUNT_SNAPSHOT, LedgerUtils.getSnapshotKey(key,nowAccountState.getTxHash(),nowAccountState.getHeight()-100));
            //update account
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, key.getBytes(LedgerConstant.DEFAULT_ENCODING), nowAccountState.serialize());
        } catch (Exception e) {
            logger.error("updateAccountState serialize error.", e);
        }
    }
    /**
     * update accountState to rocksdb
     * @param key
     * @param nowAccountState
     */
    @Override
    public void updateAccountState(byte[] key,AccountState nowAccountState){
        try {
            //update account
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, key, nowAccountState.serialize());
        } catch (Exception e) {
            logger.error("updateAccountState serialize error.", e);
        }
    }

    @Override
    public AccountState getSnapshotAccountState(byte[] key) {
        byte[] stream = RocksDBService.get(DataBaseArea.TB_LEDGER_ACCOUNT_SNAPSHOT, key);
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
    public void delSnapshotAccountState(byte[] key) {
        try {
        RocksDBService.delete(DataBaseArea.TB_LEDGER_ACCOUNT_SNAPSHOT,key);
        } catch (Exception e) {
            logger.error("delSnapshotAccountState error.", e);
        }
    }

    /**
     * get accountState from rocksdb
     *
     * @param key
     * @return
     */
    @Override
    public AccountState getAccountState(byte[] key) {
        byte[] stream = RocksDBService.get(DataBaseArea.TB_LEDGER_ACCOUNT, key);
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
    public long getBlockHeight() {
        //TODO:
        return 0;
    }

}
