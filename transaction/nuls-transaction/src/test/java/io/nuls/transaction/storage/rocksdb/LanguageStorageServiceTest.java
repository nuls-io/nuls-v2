package io.nuls.transaction.storage.rocksdb;

import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.transaction.TestConstant;
import io.nuls.transaction.TransactionBootStrap;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LanguageStorageServiceTest {

    protected static LanguageStorageService languageStorageService;

    @BeforeClass
    public static void beforeTest() throws Exception {
        //初始化数据库配置文件
        new TransactionBootStrap().initDB();
        //初始化上下文
        SpringLiteContext.init(TestConstant.CONTEXT_PATH);
        languageStorageService = SpringLiteContext.getBean(LanguageStorageService.class);
    }

    @Test
    public void saveLanguage() throws Exception {
        languageStorageService.saveLanguage("zh-CHS");
        String language = languageStorageService.getLanguage();
        Assert.assertEquals("zh-CHS", language);
    }
}