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
import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.storage.DataBaseArea;
import io.nuls.ledger.storage.InitDB;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 *
 * @author lanjinsheng
 * @date 2018/11/19
 */
@Service
public class UnconfirmedRepositoryImpl implements UnconfirmedRepository, InitDB, InitializingBean {

    public UnconfirmedRepositoryImpl() {

    }
    String getLedgerUnconfirmedTableName(int chainId) {
        return DataBaseArea.TB_LEDGER_ACCOUNT_UNCONFIRMED + chainId;
    }
    /**
     * create AccountStateUnconfirmed to rocksdb
     *
     * @param key
     * @param accountStateUnconfirmed
     */
    @Override
    public void createAccountStateUnconfirmed(byte[] key, AccountStateUnconfirmed accountStateUnconfirmed) {
        try {
            RocksDBService.put(getLedgerUnconfirmedTableName(accountStateUnconfirmed.getAddressChainId()), key, accountStateUnconfirmed.serialize());
        } catch (Exception e) {
            logger(accountStateUnconfirmed.getAddressChainId()).error("createAccountStateUnconfirmed serialize error.", e);
        }
    }

    /**
     * update AccountStateUnconfirmed to rocksdb
     *
     * @param key
     * @param nowAccountState
     */
    @Override
    public void updateAccountStateUnconfirmed(byte[] key, AccountStateUnconfirmed nowAccountState) throws Exception {
        //update account
          RocksDBService.put(getLedgerUnconfirmedTableName(nowAccountState.getAddressChainId()), key, nowAccountState.serialize());
    }



    @Override
    public AccountStateUnconfirmed getAccountStateUnconfirmed(int chainId, byte[] key) {
        byte[] stream = RocksDBService.get(getLedgerUnconfirmedTableName(chainId), key);
        if (stream == null) {
            return null;
        }
        AccountStateUnconfirmed accountState = new AccountStateUnconfirmed();
        try {
            accountState.parse(new NulsByteBuffer(stream));
        } catch (NulsException e) {
            logger(chainId).error("getAccountStateUnconfirmed serialize error.", e);
        }
        return accountState;
    }


    /**
     * 初始化数据库
     */
    public void initChainDb(int addressChainId) {
        try {
            if (!RocksDBService.existTable(getLedgerUnconfirmedTableName(addressChainId))) {
                RocksDBService.createTable(getLedgerUnconfirmedTableName(addressChainId));
            }
        } catch (Exception e) {
            logger(addressChainId).error(e);
        }
    }

    @Override
    public void initTableName() throws NulsException {

    }

    @Override
    public void afterPropertiesSet() throws NulsException {

    }
}
