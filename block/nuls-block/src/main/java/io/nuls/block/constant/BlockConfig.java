package io.nuls.block.constant;

import io.nuls.block.model.ChainParameters;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

/**
 * 配置信息,所有时间配置默认单位为毫秒
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
    private String dataFolder;

    /**
     * 国际化
     */
    @Setter
    @Getter
    private String language;

    /**
     * 分叉链监视线程执行间隔
     */
    @Setter
    @Getter
    private int forkChainsMonitorInterval;

    /**
     * 孤儿链监视线程执行间隔
     */
    @Setter
    @Getter
    private int orphanChainsMonitorInterval;

    /**
     * 孤儿链维护线程执行间隔
     */
    @Setter
    @Getter
    private int orphanChainsMaintainerInterval;

    /**
     * 数据库监视线程执行间隔
     */
    @Setter
    @Getter
    private int storageSizeMonitorInterval;

    /**
     * 网络监视线程执行间隔
     */
    @Setter
    @Getter
    private int networkResetMonitorInterval;

    /**
     * TxGroup请求器线程执行间隔
     */
    @Setter
    @Getter
    private int txGroupRequestorInterval;

    /**
     * TxGroup请求器任务执行延时
     */
    @Setter
    @Getter
    private int txGroupTaskDelay;

    @Value("DataPath")
    @Setter
    @Getter
    private String dataPath;

    /**
     * 启动后自动回滚多少个区块
     */
    @Setter
    @Getter
    private int testAutoRollbackAmount;

    /**
     * 默认链配置
     */
    @Setter
    @Getter
    private ChainParameters defaultChainParameter;

    public String getDataFolder() {
        return dataPath + File.separator + dataFolder;
    }

}
