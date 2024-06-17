package io.nuls.network.storage;

import io.nuls.core.exception.NulsException;

public interface InitDB {
    /**
     * Initialize Table
     */
    void initTableName() throws NulsException;
}
