package io.nuls.ledger.storage;

import io.nuls.tools.exception.NulsException;

/**
 * 数据初始化接口
 * @author lanjinsheng
 */
public interface InitDB {
    /**
     * 初始化表
     * @throws NulsException
     */
    void initTableName() throws NulsException;
}
