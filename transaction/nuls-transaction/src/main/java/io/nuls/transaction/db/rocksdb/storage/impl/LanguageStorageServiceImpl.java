package io.nuls.transaction.db.rocksdb.storage.impl;

import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.db.rocksdb.storage.LanguageStorageService;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
@Service
public class LanguageStorageServiceImpl implements LanguageStorageService {

    @Override
    public boolean saveLanguage(String language) throws Exception {
        return RocksDBService.put(TxDBConstant.DB_TX_LANGUAGE, TxDBConstant.DB_TX_LANGUAGE.getBytes(), language.getBytes());
    }

    @Override
    public String getLanguage() {
        byte[] languageByte = RocksDBService.get(TxDBConstant.DB_TX_LANGUAGE, TxDBConstant.DB_TX_LANGUAGE.getBytes());
        if (languageByte == null) {
            return null;
        }
        return ByteUtils.asString(languageByte);
    }


}
