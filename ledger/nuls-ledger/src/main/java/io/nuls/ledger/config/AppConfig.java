package io.nuls.ledger.config;

import io.nuls.ledger.model.ModuleConfig;
import io.nuls.tools.core.annotation.Component;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Component
public class AppConfig {

    @Setter
    @Getter
    private ModuleConfig moduleConfig;


}
