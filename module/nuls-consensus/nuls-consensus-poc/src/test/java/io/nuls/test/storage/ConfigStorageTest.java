package io.nuls.test.storage;

import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.storage.ConfigService;
import io.nuls.test.TestUtil;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.parse.ConfigLoader;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;

public class ConfigStorageTest {
    private ConfigService configService;
    @Before
    public void init(){
        try {
            Properties properties = ConfigLoader.loadProperties(ConsensusConstant.DB_CONFIG_NAME);
            String path = properties.getProperty(ConsensusConstant.DB_DATA_PATH, ConsensusConstant.DB_DATA_DEFAULT_PATH);
            RocksDBService.init(path);
            TestUtil.initTable(1);
        }catch (Exception e){
            Log.error(e);
        }
        SpringLiteContext.init(ConsensusConstant.CONTEXT_PATH);
        configService = SpringLiteContext.getBean(ConfigService.class);
    }

    @Test
    public void saveConfig()throws Exception{
        ConfigBean configBean = new ConfigBean();
        configBean.setAssetId(1);
        //configBean.setChainId(1);
        configBean.setChainId(2);
        configBean.setBlockMaxSize(5242880);
        configBean.setPackingInterval(10);
        System.out.println(configService.save(configBean,1));
        getConfig();
        getConfigList();
    }

    @Test
    public void getConfig(){
        ConfigBean configBean = configService.get(1);
        assertNotNull(configBean);
        System.out.println(configBean.getChainId());
    }

    @Test
    public void deleteConfig(){
        System.out.println(configService.delete(1));
        getConfig();
    }

    @Test
    public void getConfigList(){
        Map<Integer,ConfigBean> configBeanMap=configService.getList();
        assertNotNull(configBeanMap);
        System.out.println(configBeanMap.size());
    }
}
