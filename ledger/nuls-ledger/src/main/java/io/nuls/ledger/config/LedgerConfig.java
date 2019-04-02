package io.nuls.ledger.config;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;

/**
 * @Author: lanjinsheng
 * @Time: 2019-03-14 14:11
 * @Description: 配置文件
 */
@Configuration(domain = "ledger")
public class LedgerConfig {
    private String language;
    private String encoding;
    private int unconfirmedTxExpired;

    /**
     * ROCK DB 数据库文件存储路径
     */
    @Value("DataPath")
    private String dataPath;

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getUnconfirmedTxExpired() {
        return unconfirmedTxExpired;
    }

    public void setUnconfirmedTxExpired(int unconfirmedTxExpired) {
        this.unconfirmedTxExpired = unconfirmedTxExpired;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }
}
