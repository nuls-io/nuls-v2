package io.nuls.ledger.config;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;
import lombok.Data;

/**
 * @Author: lanjinsheng
 * @Time: 2019-03-14 14:11
 * @Description: 配置文件
 */
@Configuration(persistDomain = "ledger")
@Data
public class LedgerConfig {
    private String language;
    private String encoding;
    private int unconfirmedTxExpired;

    /**
     * ROCK DB 数据库文件存储路径
     */
    @Value("DataPath")
    private String dataPath;

}
