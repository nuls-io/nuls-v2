package io.nuls.contract.storage.impl;

import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.storage.LanguageStorageService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;

/**
 * @author: PierreLuo
 * @date: 2019-02-26
 */
@Service
public class LanguageStorageServiceImpl implements LanguageStorageService, InitializingBean {

    @Override
    public void afterPropertiesSet() throws NulsException {
        /**
         * 一个节点共用，不区分chain
         */
        ContractUtil.createTable(ContractDBConstant.DB_NAME_LANGUAGE);
    }

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
