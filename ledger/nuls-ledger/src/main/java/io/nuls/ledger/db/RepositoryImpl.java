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

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.serializers.AccountStateSerializer;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Service
public class RepositoryImpl implements Repository {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AccountStateSerializer accountStateSerializer;

    /**
     * put accountState to rocksdb
     *
     * @param key
     * @param accountState
     */
    @Override
    public void putAccountState(byte[] key, AccountState accountState) {
        try {
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, key, accountStateSerializer.serialize(accountState));
        } catch (Exception e) {
            logger.error("putAccountState serialize error.", e);
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
        AccountState state = accountStateSerializer.deserialize(stream);
        return state;
    }
}
