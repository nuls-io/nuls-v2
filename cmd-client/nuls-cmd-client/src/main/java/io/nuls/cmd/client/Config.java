package io.nuls.cmd.client;

import io.nuls.api.provider.Provider;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.ConfigLoader;
import lombok.Getter;

import java.io.IOException;
import java.util.Properties;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:56
 * @Description: 功能描述
 */
@Component
@Getter
public class Config implements InitializingBean {

    private int chainId;

    @Override
    public void afterPropertiesSet() throws NulsException {
        Integer defaultChainId;
        try {
            Properties prop = ConfigLoader.loadProperties("module.properties");
            if(prop.getProperty("chain-id") == null){
                throw new RuntimeException("api provider init fail, must be set chain-id in module.properties");
            }
            defaultChainId = Integer.parseInt(prop.getProperty("chain-id"));
            this.chainId = defaultChainId;
        } catch (IOException e) {
            throw new RuntimeException("api provider init fail, load module.properties fail");
        }
    }
}
