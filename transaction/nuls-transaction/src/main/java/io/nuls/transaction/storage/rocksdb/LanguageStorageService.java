package io.nuls.transaction.storage.rocksdb;

/**
 * 系统语言设置数据存储服务接口
 *
 * @author: Charlie
 */
public interface LanguageStorageService {

    /**
     * 保存当前系统语言环境
     */
    boolean saveLanguage(String language) throws Exception;

    /**
     * 获取当前系统语言环境
     */
    String getLanguage();

}
