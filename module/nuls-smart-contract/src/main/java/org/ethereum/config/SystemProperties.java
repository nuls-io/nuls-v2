/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.ethereum.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import io.nuls.core.crypto.HexUtil;
import org.ethereum.util.ByteUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.util.*;
import java.util.function.Function;

import static org.ethereum.util.ByteUtil.toHexString;

/**
 * Utility class to retrieve property values from the ethereumj.conf files
 * <p>
 * The properties are taken from different sources and merged in the following order
 * (the config option from the next source overrides option from previous):
 * - resource ethereumj.conf : normally used as a reference config with default values
 * and shouldn't be changed
 * - system property : each config entry might be altered via -D VM option
 * - [user dir]/config/ethereumj.conf
 * - config specified with the -Dethereumj.conf.file=[file.conf] VM option
 * - CLI options
 *
 * @author Roman Mandeleil
 * @since 22.05.2014
 */
public class SystemProperties {
    private static Logger logger = LoggerFactory.getLogger("general");

    public final static String PROPERTY_DB_DIR = "database.dir";
    public final static String PROPERTY_LISTEN_PORT = "peer.listen.port";
    public final static String PROPERTY_PEER_ACTIVE = "peer.active";
    public final static String PROPERTY_DB_RESET = "database.reset";
    public final static String PROPERTY_PEER_DISCOVERY_ENABLED = "peer.discovery.enabled";

    /* Testing */
    private final static Boolean DEFAULT_VMTEST_LOAD_LOCAL = false;
    private final static String DEFAULT_BLOCKS_LOADER = "";

    private static SystemProperties CONFIG;
    private static boolean useOnlySpringConfig = false;
    private String generatedNodePrivateKey;

    /**
     * Returns the static config instance. If the config is passed
     * as a Spring bean by the application this instance shouldn't
     * be used
     * This method is mainly used for testing purposes
     * (Autowired fields are initialized with this static instance
     * but when running within Spring context they replaced with the
     * bean config instance)
     */
    public static SystemProperties getDefault() {
        return useOnlySpringConfig ? null : getSpringDefault();
    }

    static SystemProperties getSpringDefault() {
        if (CONFIG == null) {
            CONFIG = new SystemProperties();
        }
        return CONFIG;
    }

    public static void resetToDefault() {
        CONFIG = null;
    }

    /**
     * Used mostly for testing purposes to ensure the application
     * refers only to the config passed as a Spring bean.
     * If this property is set to true {@link #getDefault()} returns null
     */
    public static void setUseOnlySpringConfig(boolean useOnlySpringConfig) {
        SystemProperties.useOnlySpringConfig = useOnlySpringConfig;
    }

    static boolean isUseOnlySpringConfig() {
        return useOnlySpringConfig;
    }

    /**
     * Marks config accessor methods which need to be called (for value validation)
     * upon config creation or modification
     */
    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    private @interface ValidateMe {
    }

    private Config config;

    // mutable options for tests
    private String databaseDir = null;
    private Boolean databaseReset = null;
    private String projectVersion = null;
    private String projectVersionModifier = null;
    protected Integer databaseVersion = null;

    private String genesisInfo = null;

    private String bindIp = null;
    private String externalIp = null;

    private Boolean syncEnabled = null;
    private Boolean discoveryEnabled = null;

    private BlockchainNetConfig blockchainConfig;
    private Boolean vmTrace;
    private Boolean recordInternalTransactionsData;

    public SystemProperties() {
        Map<String, Object> values = new HashMap<>();
        values.put("cache.flush.writeCacheSize", 64);
        values.put("cache.flush.blocks", 0);
        values.put("cache.flush.shortSyncFlush", true);
        values.put("cache.stateCacheSize", 384);
        values.put("crypto.providerName", "BC");
        values.put("crypto.hash.alg256", "ETH-KECCAK-256");
        values.put("crypto.hash.alg512", "ETH-KECCAK-512");
        values.put("database.maxOpenFiles", 2048);
        values.put("database.prune.enabled", false);
        values.put("database.prune.maxDepth", 192);
        values.put("keyvalue.datasource", "");
        config = ConfigFactory.parseMap(values);
    }

    /**
     * Loads resources using given ClassLoader assuming, there could be several resources
     * with the same name
     */
    public static List<InputStream> loadResources(
            final String name, final ClassLoader classLoader) throws IOException {
        final List<InputStream> list = new ArrayList<InputStream>();
        final Enumeration<URL> systemResources =
                (classLoader == null ? ClassLoader.getSystemClassLoader() : classLoader)
                        .getResources(name);
        while (systemResources.hasMoreElements()) {
            list.add(systemResources.nextElement().openStream());
        }
        return list;
    }

    public Config getConfig() {
        return config;
    }

    /**
     * Puts a new config atop of existing stack making the options
     * in the supplied config overriding existing options
     * Once put this config can't be removed
     *
     * @param overrideOptions - atop config
     */
    public void overrideParams(Config overrideOptions) {
        config = overrideOptions.withFallback(config);
        validateConfig();
    }

    /**
     * Puts a new config atop of existing stack making the options
     * in the supplied config overriding existing options
     * Once put this config can't be removed
     *
     * @param keyValuePairs [name] [value] [name] [value] ...
     */
    public void overrideParams(String... keyValuePairs) {
        if (keyValuePairs.length % 2 != 0) {
            throw new RuntimeException("Odd argument number");
        }
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < keyValuePairs.length; i += 2) {
            map.put(keyValuePairs[i], keyValuePairs[i + 1]);
        }
        overrideParams(map);
    }

    /**
     * Puts a new config atop of existing stack making the options
     * in the supplied config overriding existing options
     * Once put this config can't be removed
     *
     * @param cliOptions -  command line options to take presidency
     */
    public void overrideParams(Map<String, ?> cliOptions) {
        Config cliConf = ConfigFactory.parseMap(cliOptions);
        overrideParams(cliConf);
    }

    private void validateConfig() {
        for (Method method : getClass().getMethods()) {
            try {
                if (method.isAnnotationPresent(ValidateMe.class)) {
                    method.invoke(this);
                }
            } catch (Exception e) {
                throw new RuntimeException("Error validating config method: " + method, e);
            }
        }
    }

    /**
     * Builds config from the list of config references in string doing following actions:
     * 1) Splits input by "," to several strings
     * 2) Uses parserFunc to create config from each string reference
     * 3) Merges configs, applying them in the same order as in input, so last overrides first
     *
     * @param input      String with list of config references separated by ",", null or one reference works fine
     * @param parserFunc Function to apply to each reference, produces config from it
     * @return Merged config
     */
    protected Config mergeConfigs(String input, Function<String, Config> parserFunc) {
        Config config = ConfigFactory.empty();
        if (input != null && !input.isEmpty()) {
            String[] list = input.split(",");
            for (int i = list.length - 1; i >= 0; --i) {
                config = config.withFallback(parserFunc.apply(list[i]));
            }
        }

        return config;
    }

    public <T> T getProperty(String propName, T defaultValue) {
        if (!config.hasPath(propName)) {
            return defaultValue;
        }
        String string = config.getString(propName);
        if (string.trim().isEmpty()) {
            return defaultValue;
        }
        return (T) config.getAnyRef(propName);
    }

    public BlockchainNetConfig getBlockchainConfig() {
        if (blockchainConfig == null) {
            blockchainConfig = new BlockchainNetConfig() {
                @Override
                public Constants getCommonConstants() {
                    return new Constants();
                }
            };
        }
        return blockchainConfig;
    }

    public void setBlockchainConfig(BlockchainNetConfig config) {
        blockchainConfig = config;
    }

    @ValidateMe
    public boolean peerDiscovery() {
        return discoveryEnabled == null ? config.getBoolean("peer.discovery.enabled") : discoveryEnabled;
    }

    public void setDiscoveryEnabled(Boolean discoveryEnabled) {
        this.discoveryEnabled = discoveryEnabled;
    }

    @ValidateMe
    public boolean peerDiscoveryPersist() {
        return config.getBoolean("peer.discovery.persist");
    }

    @ValidateMe
    public int peerDiscoveryWorkers() {
        return config.getInt("peer.discovery.workers");
    }

    @ValidateMe
    public int peerDiscoveryTouchPeriod() {
        return config.getInt("peer.discovery.touchPeriod");
    }

    @ValidateMe
    public int peerDiscoveryTouchMaxNodes() {
        return config.getInt("peer.discovery.touchMaxNodes");
    }

    @ValidateMe
    public int peerConnectionTimeout() {
        return config.getInt("peer.connection.timeout") * 1000;
    }

    @ValidateMe
    public int transactionApproveTimeout() {
        return config.getInt("transaction.approve.timeout") * 1000;
    }

    @ValidateMe
    public List<String> peerDiscoveryIPList() {
        return config.getStringList("peer.discovery.ip.list");
    }

    @ValidateMe
    public boolean databaseReset() {
        return databaseReset == null ? config.getBoolean("database.reset") : databaseReset;
    }

    public void setDatabaseReset(Boolean reset) {
        databaseReset = reset;
    }

    @ValidateMe
    public long databaseResetBlock() {
        return config.getLong("database.resetBlock");
    }

    @ValidateMe
    public boolean databaseFromBackup() {
        return config.getBoolean("database.fromBackup");
    }

    @ValidateMe
    public int databasePruneDepth() {
        return config.getBoolean("database.prune.enabled") ? config.getInt("database.prune.maxDepth") : -1;
    }

    @ValidateMe
    public Integer blockQueueSize() {
        return config.getInt("cache.blockQueueSize") * 1024 * 1024;
    }

    @ValidateMe
    public Integer headerQueueSize() {
        return config.getInt("cache.headerQueueSize") * 1024 * 1024;
    }

    @ValidateMe
    public Integer peerChannelReadTimeout() {
        return config.getInt("peer.channel.read.timeout");
    }

    @ValidateMe
    public Integer traceStartBlock() {
        return config.getInt("trace.startblock");
    }

    @ValidateMe
    public boolean recordBlocks() {
        return config.getBoolean("record.blocks");
    }

    @ValidateMe
    public boolean dumpFull() {
        return config.getBoolean("dump.full");
    }

    @ValidateMe
    public String dumpDir() {
        return config.getString("dump.dir");
    }

    @ValidateMe
    public String dumpStyle() {
        return config.getString("dump.style");
    }

    @ValidateMe
    public int dumpBlock() {
        return config.getInt("dump.block");
    }

    @ValidateMe
    public String databaseDir() {
        return databaseDir == null ? config.getString("database.dir") : databaseDir;
    }

    public String ethashDir() {
        return config.hasPath("ethash.dir") ? config.getString("ethash.dir") : databaseDir();
    }

    public void setDataBaseDir(String dataBaseDir) {
        this.databaseDir = dataBaseDir;
    }

    @ValidateMe
    public boolean dumpCleanOnRestart() {
        return config.getBoolean("dump.clean.on.restart");
    }

    @ValidateMe
    public boolean playVM() {
        return config.getBoolean("play.vm");
    }

    @ValidateMe
    public boolean blockChainOnly() {
        return config.getBoolean("blockchain.only");
    }

    @ValidateMe
    public int syncPeerCount() {
        return config.getInt("sync.peer.count");
    }

    public Integer syncVersion() {
        if (!config.hasPath("sync.version")) {
            return null;
        }
        return config.getInt("sync.version");
    }

    @ValidateMe
    public boolean exitOnBlockConflict() {
        return config.getBoolean("sync.exitOnBlockConflict");
    }

    @ValidateMe
    public String projectVersion() {
        return projectVersion;
    }

    @ValidateMe
    public Integer databaseVersion() {
        return databaseVersion;
    }

    @ValidateMe
    public String projectVersionModifier() {
        return projectVersionModifier;
    }

    @ValidateMe
    public String helloPhrase() {
        return config.getString("hello.phrase");
    }

    @ValidateMe
    public String rootHashStart() {
        return config.hasPath("root.hash.start") ? config.getString("root.hash.start") : null;
    }

    @ValidateMe
    public List<String> peerCapabilities() {
        return config.getStringList("peer.capabilities");
    }

    @ValidateMe
    public boolean vmTrace() {
        return vmTrace == null ? (vmTrace = config.getBoolean("vm.structured.trace")) : vmTrace;
    }

    @ValidateMe
    public boolean vmTraceCompressed() {
        return config.getBoolean("vm.structured.compressed");
    }

    @ValidateMe
    public int vmTraceInitStorageLimit() {
        return config.getInt("vm.structured.initStorageLimit");
    }

    @ValidateMe
    public int cacheFlushBlocks() {
        return config.getInt("cache.flush.blocks");
    }

    @ValidateMe
    public String vmTraceDir() {
        return config.getString("vm.structured.dir");
    }

    public String customSolcPath() {
        return config.hasPath("solc.path") ? config.getString("solc.path") : null;
    }

    @ValidateMe
    public int networkId() {
        return config.getInt("peer.networkId");
    }

    @ValidateMe
    public int maxActivePeers() {
        return config.getInt("peer.maxActivePeers");
    }

    @ValidateMe
    public boolean eip8() {
        return config.getBoolean("peer.p2p.eip8");
    }

    @ValidateMe
    public int listenPort() {
        return config.getInt("peer.listen.port");
    }


    /**
     * This can be a blocking call with long timeout (thus no ValidateMe)
     */
    public String bindIp() {
        if (!config.hasPath("peer.discovery.bind.ip") || config.getString("peer.discovery.bind.ip").trim().isEmpty()) {
            if (bindIp == null) {
                logger.debug("Bind address wasn't set, Punching to identify it...");
                try (Socket s = new Socket("www.google.com", 80)) {
                    bindIp = s.getLocalAddress().getHostAddress();
                    logger.debug("UDP local bound to: {}", bindIp);
                } catch (IOException e) {
                    logger.warn("Can't get bind IP. Fall back to 0.0.0.0: " + e);
                    bindIp = "0.0.0.0";
                }
            }
            return bindIp;
        } else {
            return config.getString("peer.discovery.bind.ip").trim();
        }
    }

    /**
     * This can be a blocking call with long timeout (thus no ValidateMe)
     */
    public String externalIp() {
        if (!config.hasPath("peer.discovery.external.ip") || config.getString("peer.discovery.external.ip").trim().isEmpty()) {
            if (externalIp == null) {
                logger.debug("External IP wasn't set, using checkip.amazonaws.com to identify it...");
                try {
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            new URL("http://checkip.amazonaws.com").openStream()));
                    externalIp = in.readLine();
                    if (externalIp == null || externalIp.trim().isEmpty()) {
                        throw new IOException("Invalid address: '" + externalIp + "'");
                    }
                    try {
                        InetAddress.getByName(externalIp);
                    } catch (Exception e) {
                        throw new IOException("Invalid address: '" + externalIp + "'");
                    }
                    logger.debug("External address identified: {}", externalIp);
                } catch (IOException e) {
                    externalIp = bindIp();
                    logger.warn("Can't get external IP. Fall back to peer.bind.ip: " + externalIp + " :" + e);
                }
            }
            return externalIp;

        } else {
            return config.getString("peer.discovery.external.ip").trim();
        }
    }

    @ValidateMe
    public String getKeyValueDataSource() {
        return config.getString("keyvalue.datasource");
    }

    @ValidateMe
    public boolean isSyncEnabled() {
        return this.syncEnabled == null ? config.getBoolean("sync.enabled") : syncEnabled;
    }

    public void setSyncEnabled(Boolean syncEnabled) {
        this.syncEnabled = syncEnabled;
    }

    @ValidateMe
    public boolean isFastSyncEnabled() {
        return isSyncEnabled() && config.getBoolean("sync.fast.enabled");
    }

    @ValidateMe
    public byte[] getFastSyncPivotBlockHash() {
        if (!config.hasPath("sync.fast.pivotBlockHash")) {
            return null;
        }
        byte[] ret = HexUtil.decode(config.getString("sync.fast.pivotBlockHash"));
        if (ret.length != 32) {
            throw new RuntimeException("Invalid block hash length: " + toHexString(ret));
        }
        return ret;
    }

    @ValidateMe
    public boolean fastSyncBackupState() {
        return config.getBoolean("sync.fast.backupState");
    }

    @ValidateMe
    public boolean fastSyncSkipHistory() {
        return config.getBoolean("sync.fast.skipHistory");
    }

    @ValidateMe
    public int makeDoneByTimeout() {
        return config.getInt("sync.makeDoneByTimeout");
    }


    @ValidateMe
    public boolean isPublicHomeNode() {
        return config.getBoolean("peer.discovery.public.home.node");
    }

    @ValidateMe
    public String genesisInfo() {
        return genesisInfo == null ? config.getString("genesis") : genesisInfo;
    }

    @ValidateMe
    public int txOutdatedThreshold() {
        return config.getInt("transaction.outdated.threshold");
    }

    public void setGenesisInfo(String genesisInfo) {
        this.genesisInfo = genesisInfo;
    }

    @ValidateMe
    public boolean minerStart() {
        return config.getBoolean("mine.start");
    }

    @ValidateMe
    public byte[] getMinerCoinbase() {
        String sc = config.getString("mine.coinbase");
        byte[] c = ByteUtil.hexStringToBytes(sc);
        if (c.length != 20) {
            throw new RuntimeException("mine.coinbase has invalid value: '" + sc + "'");
        }
        return c;
    }

    @ValidateMe
    public byte[] getMineExtraData() {
        byte[] bytes;
        if (config.hasPath("mine.extraDataHex")) {
            bytes = HexUtil.decode(config.getString("mine.extraDataHex"));
        } else {
            bytes = config.getString("mine.extraData").getBytes();
        }
        if (bytes.length > 32) {
            throw new RuntimeException("mine.extraData exceed 32 bytes length: " + bytes.length);
        }
        return bytes;
    }

    @ValidateMe
    public BigInteger getMineMinGasPrice() {
        return new BigInteger(config.getString("mine.minGasPrice"));
    }

    @ValidateMe
    public long getMineMinBlockTimeoutMsec() {
        return config.getLong("mine.minBlockTimeoutMsec");
    }

    @ValidateMe
    public int getMineCpuThreads() {
        return config.getInt("mine.cpuMineThreads");
    }

    @ValidateMe
    public boolean isMineFullDataset() {
        return config.getBoolean("mine.fullDataSet");
    }

    @ValidateMe
    public String getCryptoProviderName() {
        return config.getString("crypto.providerName");
    }

    @ValidateMe
    public boolean recordInternalTransactionsData() {
        if (recordInternalTransactionsData == null) {
            recordInternalTransactionsData = config.getBoolean("record.internal.transactions.entity");
        }
        return recordInternalTransactionsData;
    }

    public void setRecordInternalTransactionsData(Boolean recordInternalTransactionsData) {
        this.recordInternalTransactionsData = recordInternalTransactionsData;
    }

    @ValidateMe
    public String getHash256AlgName() {
        return config.getString("crypto.hash.alg256");
    }

    @ValidateMe
    public String getHash512AlgName() {
        return config.getString("crypto.hash.alg512");
    }

    @ValidateMe
    public String getEthashMode() {
        return config.getString("sync.ethash");
    }


    public String dump() {
        return config.root().render(ConfigRenderOptions.defaults().setComments(false));
    }

    /*
     *
     * Testing
     *
     */
    public boolean vmTestLoadLocal() {
        return config.hasPath("GitHubTests.VMTest.loadLocal") ?
                config.getBoolean("GitHubTests.VMTest.loadLocal") : DEFAULT_VMTEST_LOAD_LOCAL;
    }

    public String blocksLoader() {
        return config.hasPath("blocks.loader") ?
                config.getString("blocks.loader") : DEFAULT_BLOCKS_LOADER;
    }

    public String githubTestsPath() {
        return config.hasPath("GitHubTests.testPath") ?
                config.getString("GitHubTests.testPath") : "";
    }

    public boolean githubTestsLoadLocal() {
        return config.hasPath("GitHubTests.testPath") &&
                !config.getString("GitHubTests.testPath").isEmpty();
    }

}
