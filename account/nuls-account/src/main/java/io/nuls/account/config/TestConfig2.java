package io.nuls.account.config;

import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.annotation.Value;
import io.nuls.tools.exception.NulsException;

import java.net.Socket;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 20:48
 * @Description: 功能描述
 */
@Component
public class TestConfig2 implements InitializingBean {

    @Value("name")
    String name;

    @Value("version")
    Integer managed;

    @Override
    public void afterPropertiesSet() throws NulsException {
        System.out.println("value:"+managed);
    }
}
