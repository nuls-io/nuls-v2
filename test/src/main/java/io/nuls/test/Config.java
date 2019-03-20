package io.nuls.test;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 14:31
 * @Description: 功能描述
 */
@Configuration(persistDomain = "test")
@Data
public class Config {

    @Value("testNodeExclude")
    String nodeExclude;

}
