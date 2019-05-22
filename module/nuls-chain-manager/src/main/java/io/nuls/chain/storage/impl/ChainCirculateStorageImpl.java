package io.nuls.chain.storage.impl;

import io.nuls.chain.storage.ChainCirculateStorage;
import io.nuls.chain.storage.InitDB;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;

import java.math.BigInteger;


@Component
public class ChainCirculateStorageImpl extends BaseStorage implements ChainCirculateStorage, InitDB, InitializingBean {

    private final String TBL = "chain_circulate";

    /**
     * 该方法在所有属性被设置之后调用，用于辅助对象初始化
     * This method is invoked after all properties are set, and is used to assist object initialization.
     */
    @Override
    public void afterPropertiesSet() {

    }


    @Override
    public void initTableName() throws NulsException {
        super.initTableName(TBL);
    }

    @Override
    public BigInteger load(String key) throws Exception {
        byte[] bytes = RocksDBService.get(TBL, key.getBytes());
        if (bytes == null) {
            return null;
        }
        BigInteger amount = ByteUtils.bytesToBigInteger(bytes);
        return amount;
    }

    @Override
    public void save(String key, BigInteger amount) throws Exception {
        RocksDBService.put(TBL, key.getBytes(), amount.toByteArray());
    }
}
