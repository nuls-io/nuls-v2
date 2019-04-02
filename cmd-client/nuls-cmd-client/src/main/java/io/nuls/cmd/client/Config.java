package io.nuls.cmd.client;

import io.nuls.api.provider.Provider;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Persist;
import io.nuls.tools.core.annotation.Value;
import lombok.Getter;
import lombok.Setter;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 16:56
 * @Description:
 */
@Configuration(domain = "cmd_client")
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
