package io.nuls.transaction.db.rocksdb.storage.impl;

import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.db.rocksdb.storage.LanguageStorageService;
import io.nuls.transaction.utils.DBUtil;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
@Service
public class LanguageStorageServiceImpl implements LanguageStorageService, InitializingBean {

    private static final String DB_NAME_TX_LANGUAGE = "language";

    @Override
    public void afterPropertiesSet() throws NulsException {
        DBUtil.createTable(DB_NAME_TX_LANGUAGE);
    }

    @Override
    public boolean saveLanguage(String language) throws Exception {
        return RocksDBService.put(DB_NAME_TX_LANGUAGE, DB_NAME_TX_LANGUAGE.getBytes(), language.getBytes());
    }

    @Override
    public String getLanguage() {
        byte[] languageByte = RocksDBService.get(DB_NAME_TX_LANGUAGE, DB_NAME_TX_LANGUAGE.getBytes());
        if (languageByte == null) {
            return null;
        }
        return ByteUtils.asString(languageByte);
    }


}
