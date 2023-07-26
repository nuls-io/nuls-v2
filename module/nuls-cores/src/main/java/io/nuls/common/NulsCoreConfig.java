package io.nuls.common;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.rpc.model.ModuleE;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Transaction module setting
 * @author: Charlie
 * @date: 2019/03/14
 */
@Component
@Configuration(domain = ModuleE.Constant.NULS_CORES)
public class NulsCoreConfig extends ConfigBean implements ModuleConfig {

    /*-------------------------[Common]-----------------------------*/
    /** ROCK DB 数据库文件存储路径*/
    private String dataPath;
    /** 主链链ID*/
    private int mainChainId;
    /** 主链主资产ID*/
    private int mainAssetId;
    /** 编码*/
    private String encoding;
    /**
     * 国际化
     */
    private String language;
    /**
     * 链ID
     */
    private int chainId;
    private int assetId;
    private int decimals = 8;
    private String  symbol;
    private String addressPrefix;
    /**
     * 日志级别
     */
    private String logLevel;
    /*-------------------------[Transaction]-----------------------------*/
    /** 黑洞公钥*/
    private String blackHolePublicKey;
    /** 交易时间所在区块时间的默认范围值(在区块时间±本值范围内)*/
    private long blockTxTimeRangeSec;
    /** 孤儿交易生命时间,超过会被清理**/
    private int orphanLifeTimeSec;
    /** 未确认交易过期时间秒 */
    private long unconfirmedTxExpireSec;
    /** 单个交易数据最大值(B)*/
    private long txMaxSize;
    /** coinTo 不支持金额等于0 的协议生效高度*/
    private long coinToPtlHeightFirst;
    /** coinTo 支持金额等于0, 只禁止金额为0的锁定 的协议生效高度*/
    private long coinToPtlHeightSecond;

    private String blackListPath;
    private String accountBlockManagerPublicKeys;

    /*-------------------------[Protocol]-----------------------------*/

    /*-------------------------[Network]-----------------------------*/
    private int port;

    private long packetMagic;

    private int maxInCount;

    private int maxOutCount;

    private byte reverseCheck = 1;

    private int maxInSameIp;

    private String selfSeedIps;

    private List<String> seedIpList;

    private int crossPort;

    private int crossMaxInCount;

    private int crossMaxOutCount;

    private int crossMaxInSameIp;

    private String moonSeedIps;

    private List<String> moonSeedIpList;

    private boolean moonNode;


    private List<String> localIps = new ArrayList<>();

    private int updatePeerInfoType = 0;
    /**
     * 中心化网络服务接口
     */
    private String timeServers;
    /*-------------------------[Ledger]-----------------------------*/
    private int unconfirmedTxExpired;
    private int assetRegDestroyAmount = 200;

    /*-------------------------[CrossChain]-----------------------------*/

    private int crossCtxType;

    private boolean mainNet;

    /**默认链接到的跨链节点*/
    private String crossSeedIps;
    /**
     * 本链种子节点地址
     */
    private Set<String> seedNodeSet;

    private Long version1_6_0_height;
    /*-------------------------[Block]-----------------------------*/
    /**
     * 分叉链监视线程执行间隔
     */
    private int forkChainsMonitorInterval;

    /**
     * 孤儿链监视线程执行间隔
     */
    private int orphanChainsMonitorInterval;

    /**
     * 孤儿链维护线程执行间隔
     */
    private int orphanChainsMaintainerInterval;

    /**
     * 数据库监视线程执行间隔
     */
    private int storageSizeMonitorInterval;

    /**
     * 网络监视线程执行间隔
     */
    private int networkResetMonitorInterval;

    /**
     * 节点数量监控线程执行间隔
     */
    private int nodesMonitorInterval;
    /**
     * BZT缓存数据清理监控线程执行间隔
     */
    private int blockBZTClearMonitorInterval;

    /**
     * TxGroup请求器线程执行间隔
     */
    private int txGroupRequestorInterval;

    /**
     * TxGroup请求器任务执行延时
     */
    private int txGroupTaskDelay;

    /**
     * 启动后自动回滚多少个区块
     */
    private int testAutoRollbackAmount;

    /**
     * 回滚到指定高度
     */
    private int rollbackHeight;
    /*-------------------------[Account]-----------------------------*/
    /**
     * key store 存储文件夹
     */
    private String keystoreFolder;

    private int sigMode = 0;

    private String sigMacUrl ;
    private String sigMacApiKey ;

    private String sigMacAddress;

    private String blockAccountManager;

    /*-------------------------[Consensus]-----------------------------*/
    /**
     * 跨链交易手续费主链收取手续费比例
     * Cross-Chain Transaction Fee Proportion of Main Chain Fee Collection
     * */
    private int mainChainCommissionRatio;
    private int maxCoinToOfCoinbase;
    private long minRewardHeight;

    public int getMainChainCommissionRatio() {
        return mainChainCommissionRatio;
    }

    public void setMainChainCommissionRatio(int mainChainCommissionRatio) {
        this.mainChainCommissionRatio = mainChainCommissionRatio;
    }

    @Override
    public int getMaxCoinToOfCoinbase() {
        return maxCoinToOfCoinbase;
    }

    @Override
    public void setMaxCoinToOfCoinbase(int maxCoinToOfCoinbase) {
        this.maxCoinToOfCoinbase = maxCoinToOfCoinbase;
    }

    @Override
    public long getMinRewardHeight() {
        return minRewardHeight;
    }

    @Override
    public void setMinRewardHeight(long minRewardHeight) {
        this.minRewardHeight = minRewardHeight;
    }

    public List<String> getLocalIps() {

        return localIps;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }

    public String getExternalIp() {
        if (localIps.size() > 0) {
            return localIps.get(localIps.size() - 1);
        }
        return null;
    }

    public String getAccountBlockManagerPublicKeys() {
        return accountBlockManagerPublicKeys;
    }

    public void setAccountBlockManagerPublicKeys(String accountBlockManagerPublicKeys) {
        this.accountBlockManagerPublicKeys = accountBlockManagerPublicKeys;
    }

    public String getBlackListPath() {
        return blackListPath;
    }

    public void setBlackListPath(String blackListPath) {
        this.blackListPath = blackListPath;
    }

    public String getBlackHolePublicKey() {
        return blackHolePublicKey;
    }

    public void setBlackHolePublicKey(String blackHolePublicKey) {
        this.blackHolePublicKey = blackHolePublicKey;
    }

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public int getMainChainId() {
        return mainChainId;
    }

    public void setMainChainId(int mainChainId) {
        this.mainChainId = mainChainId;
    }

    public int getMainAssetId() {
        return mainAssetId;
    }

    public void setMainAssetId(int mainAssetId) {
        this.mainAssetId = mainAssetId;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public long getUnconfirmedTxExpireSec() {
        return unconfirmedTxExpireSec;
    }

    public void setUnconfirmedTxExpireSec(long unconfirmedTxExpireSec) {
        this.unconfirmedTxExpireSec = unconfirmedTxExpireSec;
    }

    public long getBlockTxTimeRangeSec() {
        return blockTxTimeRangeSec;
    }

    public void setBlockTxTimeRangeSec(long blockTxTimeRangeSec) {
        this.blockTxTimeRangeSec = blockTxTimeRangeSec;
    }

    public int getOrphanLifeTimeSec() {
        return orphanLifeTimeSec;
    }

    public void setOrphanLifeTimeSec(int orphanLifeTimeSec) {
        this.orphanLifeTimeSec = orphanLifeTimeSec;
    }

    public long getTxMaxSize() {
        return txMaxSize;
    }

    public void setTxMaxSize(long txMaxSize) {
        this.txMaxSize = txMaxSize;
    }

    public long getCoinToPtlHeightFirst() {
        return coinToPtlHeightFirst;
    }

    public void setCoinToPtlHeightFirst(long coinToPtlHeightFirst) {
        this.coinToPtlHeightFirst = coinToPtlHeightFirst;
    }

    public long getCoinToPtlHeightSecond() {
        return coinToPtlHeightSecond;
    }

    public void setCoinToPtlHeightSecond(long coinToPtlHeightSecond) {
        this.coinToPtlHeightSecond = coinToPtlHeightSecond;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public int getChainId() {
        return chainId;
    }

    @Override
    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public int getAssetId() {
        return assetId;
    }

    @Override
    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public int getDecimals() {
        return decimals;
    }

    public void setDecimals(int decimals) {
        this.decimals = decimals;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getPacketMagic() {
        return packetMagic;
    }

    public void setPacketMagic(long packetMagic) {
        this.packetMagic = packetMagic;
    }

    public int getMaxInCount() {
        return maxInCount;
    }

    public void setMaxInCount(int maxInCount) {
        this.maxInCount = maxInCount;
    }

    public int getMaxOutCount() {
        return maxOutCount;
    }

    public void setMaxOutCount(int maxOutCount) {
        this.maxOutCount = maxOutCount;
    }

    public byte getReverseCheck() {
        return reverseCheck;
    }

    public void setReverseCheck(byte reverseCheck) {
        this.reverseCheck = reverseCheck;
    }

    public int getMaxInSameIp() {
        return maxInSameIp;
    }

    public void setMaxInSameIp(int maxInSameIp) {
        this.maxInSameIp = maxInSameIp;
    }

    public String getSelfSeedIps() {
        return selfSeedIps;
    }

    public void setSelfSeedIps(String selfSeedIps) {
        this.selfSeedIps = selfSeedIps;
    }

    public List<String> getSeedIpList() {
        return seedIpList;
    }

    public void setSeedIpList(List<String> seedIpList) {
        this.seedIpList = seedIpList;
    }

    public int getCrossPort() {
        return crossPort;
    }

    public void setCrossPort(int crossPort) {
        this.crossPort = crossPort;
    }

    public int getCrossMaxInCount() {
        return crossMaxInCount;
    }

    public void setCrossMaxInCount(int crossMaxInCount) {
        this.crossMaxInCount = crossMaxInCount;
    }

    public int getCrossMaxOutCount() {
        return crossMaxOutCount;
    }

    public void setCrossMaxOutCount(int crossMaxOutCount) {
        this.crossMaxOutCount = crossMaxOutCount;
    }

    public int getCrossMaxInSameIp() {
        return crossMaxInSameIp;
    }

    public void setCrossMaxInSameIp(int crossMaxInSameIp) {
        this.crossMaxInSameIp = crossMaxInSameIp;
    }

    public String getMoonSeedIps() {
        return moonSeedIps;
    }

    public void setMoonSeedIps(String moonSeedIps) {
        this.moonSeedIps = moonSeedIps;
    }

    public List<String> getMoonSeedIpList() {
        return moonSeedIpList;
    }

    public void setMoonSeedIpList(List<String> moonSeedIpList) {
        this.moonSeedIpList = moonSeedIpList;
    }

    public boolean isMoonNode() {
        return moonNode;
    }

    public void setMoonNode(boolean moonNode) {
        this.moonNode = moonNode;
    }

    public void setLocalIps(List<String> localIps) {
        this.localIps = localIps;
    }

    public int getUpdatePeerInfoType() {
        return updatePeerInfoType;
    }

    public void setUpdatePeerInfoType(int updatePeerInfoType) {
        this.updatePeerInfoType = updatePeerInfoType;
    }

    public String getTimeServers() {
        return timeServers;
    }

    public void setTimeServers(String timeServers) {
        this.timeServers = timeServers;
    }

    public int getUnconfirmedTxExpired() {
        return unconfirmedTxExpired;
    }

    public void setUnconfirmedTxExpired(int unconfirmedTxExpired) {
        this.unconfirmedTxExpired = unconfirmedTxExpired;
    }

    public int getAssetRegDestroyAmount() {
        return assetRegDestroyAmount;
    }

    public void setAssetRegDestroyAmount(int assetRegDestroyAmount) {
        this.assetRegDestroyAmount = assetRegDestroyAmount;
    }

    public int getCrossCtxType() {
        return crossCtxType;
    }

    public void setCrossCtxType(int crossCtxType) {
        this.crossCtxType = crossCtxType;
    }

    public boolean isMainNet() {
        return mainNet;
    }

    public void setMainNet(boolean mainNet) {
        this.mainNet = mainNet;
    }

    public String getCrossSeedIps() {
        return crossSeedIps;
    }

    public void setCrossSeedIps(String crossSeedIps) {
        this.crossSeedIps = crossSeedIps;
    }

    public Set<String> getSeedNodeSet() {
        return seedNodeSet;
    }

    public void setSeedNodeSet(Set<String> seedNodeSet) {
        this.seedNodeSet = seedNodeSet;
    }

    public Long getVersion1_6_0_height() {
        return version1_6_0_height;
    }

    public void setVersion1_6_0_height(Long version1_6_0_height) {
        this.version1_6_0_height = version1_6_0_height;
    }

    public int getForkChainsMonitorInterval() {
        return forkChainsMonitorInterval;
    }

    public void setForkChainsMonitorInterval(int forkChainsMonitorInterval) {
        this.forkChainsMonitorInterval = forkChainsMonitorInterval;
    }

    public int getOrphanChainsMonitorInterval() {
        return orphanChainsMonitorInterval;
    }

    public void setOrphanChainsMonitorInterval(int orphanChainsMonitorInterval) {
        this.orphanChainsMonitorInterval = orphanChainsMonitorInterval;
    }

    public int getOrphanChainsMaintainerInterval() {
        return orphanChainsMaintainerInterval;
    }

    public void setOrphanChainsMaintainerInterval(int orphanChainsMaintainerInterval) {
        this.orphanChainsMaintainerInterval = orphanChainsMaintainerInterval;
    }

    public int getStorageSizeMonitorInterval() {
        return storageSizeMonitorInterval;
    }

    public void setStorageSizeMonitorInterval(int storageSizeMonitorInterval) {
        this.storageSizeMonitorInterval = storageSizeMonitorInterval;
    }

    public int getNetworkResetMonitorInterval() {
        return networkResetMonitorInterval;
    }

    public void setNetworkResetMonitorInterval(int networkResetMonitorInterval) {
        this.networkResetMonitorInterval = networkResetMonitorInterval;
    }

    public int getNodesMonitorInterval() {
        return nodesMonitorInterval;
    }

    public void setNodesMonitorInterval(int nodesMonitorInterval) {
        this.nodesMonitorInterval = nodesMonitorInterval;
    }

    public int getBlockBZTClearMonitorInterval() {
        return blockBZTClearMonitorInterval;
    }

    public void setBlockBZTClearMonitorInterval(int blockBZTClearMonitorInterval) {
        this.blockBZTClearMonitorInterval = blockBZTClearMonitorInterval;
    }

    public int getTxGroupRequestorInterval() {
        return txGroupRequestorInterval;
    }

    public void setTxGroupRequestorInterval(int txGroupRequestorInterval) {
        this.txGroupRequestorInterval = txGroupRequestorInterval;
    }

    public int getTxGroupTaskDelay() {
        return txGroupTaskDelay;
    }

    public void setTxGroupTaskDelay(int txGroupTaskDelay) {
        this.txGroupTaskDelay = txGroupTaskDelay;
    }

    public int getTestAutoRollbackAmount() {
        return testAutoRollbackAmount;
    }

    public void setTestAutoRollbackAmount(int testAutoRollbackAmount) {
        this.testAutoRollbackAmount = testAutoRollbackAmount;
    }

    public int getRollbackHeight() {
        return rollbackHeight;
    }

    public void setRollbackHeight(int rollbackHeight) {
        this.rollbackHeight = rollbackHeight;
    }

    public String getKeystoreFolder() {
        return keystoreFolder;
    }

    public void setKeystoreFolder(String keystoreFolder) {
        this.keystoreFolder = keystoreFolder;
    }

    public int getSigMode() {
        return sigMode;
    }

    public void setSigMode(int sigMode) {
        this.sigMode = sigMode;
    }

    public String getSigMacUrl() {
        return sigMacUrl;
    }

    public void setSigMacUrl(String sigMacUrl) {
        this.sigMacUrl = sigMacUrl;
    }

    public String getSigMacApiKey() {
        return sigMacApiKey;
    }

    public void setSigMacApiKey(String sigMacApiKey) {
        this.sigMacApiKey = sigMacApiKey;
    }

    public String getSigMacAddress() {
        return sigMacAddress;
    }

    public void setSigMacAddress(String sigMacAddress) {
        this.sigMacAddress = sigMacAddress;
    }

    public String getBlockAccountManager() {
        return blockAccountManager;
    }

    public void setBlockAccountManager(String blockAccountManager) {
        this.blockAccountManager = blockAccountManager;
    }

    @Override
    public VersionChangeInvoker getVersionChangeInvoker() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> aClass = Class.forName("io.nuls.transaction.rpc.upgrade.TxVersionChangeInvoker");
        return (VersionChangeInvoker) aClass.getDeclaredConstructor().newInstance();
    }
}
