package io.nuls.cmd.client;

import io.nuls.api.provider.Provider;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Persist;
import io.nuls.tools.core.annotation.Value;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.config.ConfigItem;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.Properties;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:56
 * @Description: 功能描述
 */
@Configuration(persistDomain = "cmd_client")
@Getter
@Setter
public class Config {

    @Persist
    @Value.NotNull
    private Integer chainId;

    @Value.NotNull
    private Integer assetsId;

    @Value.NotNull
    private Provider.ProviderType providerType;

}
