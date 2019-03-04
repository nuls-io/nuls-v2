package io.nuls.ledger.storage;

import io.nuls.tools.exception.NulsException;

public interface InitDB {
    /**
     * 初始化表
     * @throws NulsException
     */
    void initTableName() throws NulsException;
}
