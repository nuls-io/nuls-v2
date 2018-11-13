package io.nuls.transaction.storage.impl;

import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TransactionConstant;
import io.nuls.transaction.storage.LanguageStorageService;

/**
 * @author: Charlie
 * @date: 2018/11/12
 */
@Service
public class LanguageStorageServiceImpl implements LanguageStorageService, InitializingBean {

    @Override
    public void afterPropertiesSet() throws NulsException {
        try {
            RocksDBService.createTable(TransactionConstant.DB_NAME_CONSUME_LANGUAGE);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(e);
        }
    }

    @Override
    public boolean saveLanguage(String language) throws Exception {
        return RocksDBService.put(TransactionConstant.DB_NAME_CONSUME_LANGUAGE, TransactionConstant.DB_NAME_CONSUME_LANGUAGE.getBytes(), language.getBytes());
    }

    @Override
    public String getLanguage() {
        byte[] languageByte = RocksDBService.get(TransactionConstant.DB_NAME_CONSUME_LANGUAGE, TransactionConstant.DB_NAME_CONSUME_LANGUAGE.getBytes());
        if (languageByte == null) {
            return null;
        }
        return ByteUtils.asString(languageByte);
    }


}
