package io.nuls.ledger.datasource;

/**
 * Defines configurable database settings
 * Created by wangkun23 on 2018/11/19.
 */
public class DbSettings {

    public static final DbSettings DEFAULT = new DbSettings()
            .withMaxThreads(1)
            .withMaxOpenFiles(32);

    int maxOpenFiles;
    int maxThreads;

    private DbSettings() {
    }

    public static DbSettings newInstance() {
        DbSettings settings = new DbSettings();
        settings.maxOpenFiles = DEFAULT.maxOpenFiles;
        settings.maxThreads = DEFAULT.maxThreads;
        return settings;
    }

    public int getMaxOpenFiles() {
        return maxOpenFiles;
    }

    public DbSettings withMaxOpenFiles(int maxOpenFiles) {
        this.maxOpenFiles = maxOpenFiles;
        return this;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public DbSettings withMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        return this;
    }
}
