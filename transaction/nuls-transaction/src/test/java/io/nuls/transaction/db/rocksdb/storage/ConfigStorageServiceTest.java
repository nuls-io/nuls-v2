package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.init.TransactionBootStrap;
import io.nuls.transaction.model.bo.config.ConfigBean;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Map;

public class ConfigStorageServiceTest {

    protected static ConfigStorageService configStorageService;
    protected int chainId = 12345;
    protected int assetsId = 1;

    @BeforeClass
    public static void beforeTest() {
        //初始化数据库配置文件
        TransactionBootStrap.initDB();
        //初始化上下文
        SpringLiteContext.init(TxConstant.CONTEXT_PATH);
        configStorageService = SpringLiteContext.getBean(ConfigStorageService.class);
    }

    @Test
    public void save() throws Exception {
        ConfigBean bean = new ConfigBean();
        bean.setChainId(chainId);
        bean.setAssetsId(assetsId);
        boolean result = configStorageService.save(bean, chainId);
        Assert.assertTrue(result);
    }

    @Test
    public void get() {
        ConfigBean config = configStorageService.get(chainId);
        Assert.assertEquals(chainId, config.getChainId());
    }

    @Test
    public void delete() {
        boolean result = configStorageService.delete(chainId);
        Assert.assertTrue(result);
        ConfigBean config = configStorageService.get(chainId);
        Assert.assertNull(config);
    }

    @Test
    public void getList() {
        Map<Integer, ConfigBean> configMap = configStorageService.getList();
        if (configMap != null) {
            for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
                System.out.println(entry.getKey() + "===" + entry.getValue().getChainId());
            }
        }
    }

}