package io.nuls.ledger.datasource.rocksdb;

import io.nuls.ledger.datasource.DbSettings;
import io.nuls.ledger.datasource.DbSource;

import io.nuls.ledger.datasource.NodeKeyCompositor;
import io.nuls.ledger.utils.FileUtil;
import org.rocksdb.*;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.System.arraycopy;

/**
 * rocksdb implements
 * Created by wangkun23 on 2018/11/19.
 */
//@Service
public class RocksDbDataSource implements DbSource<byte[]> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger("db");

    private String databaseDir;
    private String name;
    RocksDB db;
    ReadOptions readOpts;
    boolean alive;

    DbSettings settings = DbSettings.DEFAULT;

    // The native RocksDB insert/update/delete are normally thread-safe
    // However close operation is not thread-safe.
    // This ReadWriteLock still permits concurrent execution of insert/delete/update operations
    // however blocks them on init/close/delete operations
    private ReadWriteLock resetDbLock = new ReentrantReadWriteLock();

    static {
        RocksDB.loadLibrary();
    }

    public RocksDbDataSource() {

    }

    public String getDatabaseDir() {
        return databaseDir;
    }

    public void setDatabaseDir(String databaseDir) {
        this.databaseDir = databaseDir;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void init() {
        init(DbSettings.DEFAULT);
    }

    @Override
    public void init(DbSettings settings) {
        this.settings = settings;
        resetDbLock.writeLock().lock();
        try {
            logger.debug("~> RocksDbDataSource.init(): " + name);

            if (isAlive()) {
                return;
            }
            if (name == null) {
                throw new NullPointerException("no name set to the db");
            }
            try (Options options = new Options()) {
                // most of these options are suggested by https://github.com/facebook/rocksdb/wiki/Set-Up-Options
                // general options
                options.setCreateIfMissing(true);
                options.setCompressionType(CompressionType.LZ4_COMPRESSION);
                options.setBottommostCompressionType(CompressionType.ZSTD_COMPRESSION);
                options.setLevelCompactionDynamicLevelBytes(true);
                options.setMaxOpenFiles(settings.getMaxOpenFiles());
                options.setIncreaseParallelism(settings.getMaxThreads());

                // key prefix for state node lookups
                options.useFixedLengthPrefixExtractor(NodeKeyCompositor.PREFIX_BYTES);

                // table options
                final BlockBasedTableConfig tableCfg;
                options.setTableFormatConfig(tableCfg = new BlockBasedTableConfig());
                tableCfg.setBlockSize(16 * 1024);
                tableCfg.setBlockCacheSize(32 * 1024 * 1024);
                tableCfg.setCacheIndexAndFilterBlocks(true);
                tableCfg.setPinL0FilterAndIndexBlocksInCache(true);
                tableCfg.setFilter(new BloomFilter(10, false));

                // read options
                readOpts = new ReadOptions();
                readOpts = readOpts.setPrefixSameAsStart(true)
                        .setVerifyChecksums(false);

                try {
                    logger.debug("Opening database");
                    final Path dbPath = getPath();
                    if (!Files.isSymbolicLink(dbPath.getParent())) {
                        Files.createDirectories(dbPath.getParent());
                    }

//                    if (config.databaseFromBackup() && backupPath().toFile().canWrite()) {
//                        logger.debug("Restoring database from backup: '{}'", name);
//                        try (BackupableDBOptions backupOptions = new BackupableDBOptions(backupPath().toString());
//                             RestoreOptions restoreOptions = new RestoreOptions(false);
//                             BackupEngine backups = BackupEngine.open(Env.getDefault(), backupOptions)) {
//
//                            if (!backups.getBackupInfo().isEmpty()) {
//                                backups.restoreDbFromLatestBackup(getPath().toString(), getPath().toString(),
//                                        restoreOptions);
//                            }
//
//                        } catch (RocksDBException e) {
//                            logger.error("Failed to restore database '{}' from backup", name, e);
//                        }
//                    }

                    logger.debug("Initializing new or existing database: '{}'", name);
                    try {
                        db = RocksDB.open(options, dbPath.toString());
                    } catch (RocksDBException e) {
                        logger.error(e.getMessage(), e);
                        throw new RuntimeException("Failed to initialize database", e);
                    }

                    alive = true;

                } catch (IOException ioe) {
                    logger.error(ioe.getMessage(), ioe);
                    throw new RuntimeException("Failed to initialize database", ioe);
                }

                logger.debug("<~ RocksDbDataSource.init(): " + name);
            }
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }


    @Override
    public boolean isAlive() {
        return alive;
    }

    @Override
    public void close() {
        resetDbLock.writeLock().lock();
        try {
            if (!isAlive()) {
                return;
            }
            logger.debug("Close db: {}", name);
            db.close();
            readOpts.close();

            alive = false;

        } catch (Exception e) {
            logger.error("Error closing db '{}'", name, e);
        } finally {
            resetDbLock.writeLock().unlock();
        }
    }

    @Override
    public Set<byte[]> keys() throws RuntimeException {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.keys(): " + name);
            }
            try (RocksIterator iterator = db.newIterator()) {
                Set<byte[]> result = new HashSet<>();
                for (iterator.seekToFirst(); iterator.isValid(); iterator.next()) {
                    result.add(iterator.key());
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("<~ RocksDbDataSource.keys(): " + name + ", " + result.size());
                }
                return result;
            } catch (Exception e) {
                logger.error("Error iterating db '{}'", name, e);
                hintOnTooManyOpenFiles(e);
                throw new RuntimeException(e);
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void reset() {
        close();
        FileUtil.recursiveDelete(getPath().toString());
        init(settings);
    }

    private Path getPath() {
        return Paths.get(this.databaseDir, name);
    }

    @Override
    public void updateBatch(Map<byte[], byte[]> rows) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
            }
            try {

                try (WriteBatch batch = new WriteBatch();
                     WriteOptions writeOptions = new WriteOptions()) {
                    for (Map.Entry<byte[], byte[]> entry : rows.entrySet()) {
                        if (entry.getValue() == null) {
                            batch.remove(entry.getKey());
                        } else {
                            batch.put(entry.getKey(), entry.getValue());
                        }
                    }
                    db.write(writeOptions, batch);
                }

                if (logger.isTraceEnabled()) {
                    logger.trace("<~ RocksDbDataSource.updateBatch(): " + name + ", " + rows.size());
                }
            } catch (RocksDBException e) {
                logger.error("Error in batch update on db '{}'", name, e);
                hintOnTooManyOpenFiles(e);
                throw new RuntimeException(e);
            }
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void put(byte[] key, byte[] val) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.put(): " + name + ", key: " + Hex.toHexString(key) + ", " + (val == null ? "null" : val.length));
            }
            if (val != null) {
                db.put(key, val);
            } else {
                db.delete(key);
            }
            if (logger.isTraceEnabled()) {
                logger.trace("<~ RocksDbDataSource.put(): " + name + ", key: " + Hex.toHexString(key) + ", " + (val == null ? "null" : val.length));
            }
        } catch (RocksDBException e) {
            logger.error("Failed to put into db '{}'", name, e);
            hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public byte[] get(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key));
            }
            byte[] ret = db.get(readOpts, key);
            if (logger.isTraceEnabled()) {
                logger.trace("<~ RocksDbDataSource.get(): " + name + ", key: " + Hex.toHexString(key) + ", " + (ret == null ? "null" : ret.length));
            }
            return ret;
        } catch (RocksDBException e) {
            logger.error("Failed to get from db '{}'", name, e);
            hintOnTooManyOpenFiles(e);
            throw new RuntimeException(e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public void delete(byte[] key) {
        resetDbLock.readLock().lock();
        try {
            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
            }
            db.delete(key);
            if (logger.isTraceEnabled()) {
                logger.trace("<~ RocksDbDataSource.delete(): " + name + ", key: " + Hex.toHexString(key));
            }
        } catch (RocksDBException e) {
            logger.error("Failed to delete from db '{}'", name, e);
            throw new RuntimeException(e);
        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public byte[] prefixLookup(byte[] key, int prefixBytes) {

        if (prefixBytes != NodeKeyCompositor.PREFIX_BYTES) {
            throw new RuntimeException("RocksDbDataSource.prefixLookup() supports only " + prefixBytes + "-bytes prefix");
        }

        resetDbLock.readLock().lock();
        try {

            if (logger.isTraceEnabled()) {
                logger.trace("~> RocksDbDataSource.prefixLookup(): " + name + ", key: " + Hex.toHexString(key));
            }

            // RocksDB sets initial position of iterator to the first key which is greater or equal to the seek key
            // since keys in RocksDB are ordered in asc order iterator must be initiated with the lowest key
            // thus bytes with indexes greater than PREFIX_BYTES must be nullified
            byte[] prefix = new byte[NodeKeyCompositor.PREFIX_BYTES];
            arraycopy(key, 0, prefix, 0, NodeKeyCompositor.PREFIX_BYTES);

            byte[] ret = null;
            try (RocksIterator it = db.newIterator(readOpts)) {

                it.seek(prefix);
                if (it.isValid()) {
                    ret = it.value();
                }

            } catch (Exception e) {
                logger.error("Failed to seek by prefix in db '{}'", name, e);
                hintOnTooManyOpenFiles(e);
                throw new RuntimeException(e);
            }

            if (logger.isTraceEnabled()) {
                logger.trace("<~ RocksDbDataSource.prefixLookup(): " + name + ", key: " + Hex.toHexString(key) + ", " + (ret == null ? "null" : ret.length));
            }

            return ret;

        } finally {
            resetDbLock.readLock().unlock();
        }
    }

    @Override
    public boolean flush() {
        return false;
    }

    private void hintOnTooManyOpenFiles(Exception e) {
        if (e.getMessage() != null && e.getMessage().toLowerCase().contains("too many open files")) {
            logger.info("");
            logger.info("       Mitigating 'Too many open files':");
            logger.info("       either decrease value of database.maxOpenFiles parameter in ethereumj.conf");
            logger.info("       or set higher limit by using 'ulimit -n' command in command line");
            logger.info("");
        }
    }
}
