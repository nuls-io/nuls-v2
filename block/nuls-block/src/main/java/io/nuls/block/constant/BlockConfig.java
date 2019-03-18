package io.nuls.block.constant;

import io.nuls.block.model.ChainParameters;
import io.nuls.tools.core.annotation.Configuration;
import lombok.Getter;
import lombok.Setter;

/**
 * 配置信息
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:01
 */
@Configuration(persistDomain = "block")
public class BlockConfig {

    /**
     * db文件存放目录
     */
    @Setter
    @Getter
    private String dataPath;

    /**
     * 默认链配置
     */
    @Setter
    @Getter
    private ChainParameters defaultChainParameter;
}
