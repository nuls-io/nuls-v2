package io.nuls.contract.storage.impl;

import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.storage.LanguageStorageService;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.model.ByteUtils;

/**
 * @author: PierreLuo
 * @date: 2019-02-26
 */
@Component
public class LanguageStorageServiceImpl implements LanguageStorageService {

    @Override
    public boolean saveLanguage(String language) throws Exception {
        return RocksDBService.put(ContractDBConstant.DB_NAME_LANGUAGE, ContractDBConstant.DB_NAME_LANGUAGE.getBytes(), language.getBytes());
    }

    @Override
    public String getLanguage() {
        byte[] languageByte = RocksDBService.get(ContractDBConstant.DB_NAME_LANGUAGE, ContractDBConstant.DB_NAME_LANGUAGE.getBytes());
        if (languageByte == null) {
            return null;
        }
        return ByteUtils.asString(languageByte);
    }


}
