package io.nuls.account.config;

import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.config.ConfigItem;

import java.util.List;
import java.util.Set;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 20:10
 * @Description: 功能描述
 */
@Configuration
public class TestConfig implements InitializingBean {

    private String name;

    private int age;

    private List<ConfigItem> data;

    private ConfigItem item;

    @Override
    public void afterPropertiesSet() throws NulsException {
        System.out.println(this.name);
        System.out.println(age);
        data.forEach(d->{
            System.out.println(d.getName());
        });
        System.out.println(item.getValue());
    }
}
