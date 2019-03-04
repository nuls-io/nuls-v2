package io.nuls.chain.storage;

import io.nuls.tools.exception.NulsException;

public interface InitDB {
    /**
     * 初始化表
     */
    void initTableName() throws NulsException;
}
