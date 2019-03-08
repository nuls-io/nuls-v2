package io.nuls.contract.storage;

/**
 * @author: PierreLuo
 * @date: 2019-02-26
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
