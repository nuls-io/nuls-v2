package io.nuls.common;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.rpc.model.ModuleE;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2023/7/27
 */
@Component
@Configuration(domain = ModuleE.Constant.NULS_CORES)
public class NulsCoresConfig extends ConfigBean implements ModuleConfig {

    /*-------------------------[Common]-----------------------------*/

    private String dataPath;

    /** 模块code*/
    private String moduleCode;

    /** 主链链ID*/
    private int mainChainId;

    /** 主链主资产ID*/
    private int mainAssetId;

    /** 语言*/
    private String language;

    /** 编码*/
    private String encoding;
    private String blackHolePublicKey;

    /**
     * 链ID
     */
    private int chainId;
    private int assetId;
    private int decimals = 8;
    private String  symbol;
    private String addressPrefix;
    private String logLevel = "DEBUG";

    /*-------------------------[Transaction]-----------------------------*/
    /** 未确认交易过期时间秒 */
    private long unconfirmedTxExpire;


    private String blackListPath;
    private String accountBlockManagerPublicKeys;
    /**
     *  是否已连接智能合约模块
     */
    private volatile boolean collectedSmartContractModule;

    /*-------------------------[Protocol]-----------------------------*/

    /*-------------------------[Network]-----------------------------*/

    private int port;

    private long packetMagic;

    private int maxInCount;

    private int maxOutCount;

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
     * 种子节点列表
     */
    private Set<String> seedNodeList;
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

    private String blockAccountManager;

    /*-------------------------[Consensus]-----------------------------*/


    /**
     * 跨链交易手续费主链收取手续费比例
     * Cross-Chain Transaction Fee Proportion of Main Chain Fee Collection
     * */
    private int mainChainCommissionRatio;

    /*-------------------------[SmartContract]-----------------------------*/

    private long maxViewGas;

    private String packageLogPackages;

    private String packageLogLevels;

    private String crossTokenSystemContract;

    /*-------------------------[Chain Manager]-----------------------------*/
    /**
     * 初始配置参数
     */
    private String chainNameMax;
    private String assetSymbolMax;
    private String assetNameMax;
    private BigInteger assetDepositNuls;
    private BigInteger assetDestroyNuls;
    private String assetDepositNulsDestroyRate;
    private String assetDepositNulsLockRate;

    private String assetInitNumberMin;
    private String assetInitNumberMax;
    private String assetDecimalPlacesMin;
    private String assetDecimalPlacesMax;
    private String assetRecoveryRate;

    private String chainName;
    private String nulsAssetInitNumberMax;
    private String mainSymbol;
    private String nulsFeeMainNetRate = "0.6";
    private int nulsFeeMainNetPercent = 60;
    private int nulsFeeOtherNetPercent = 40;
    private String defaultDecimalPlaces = "8";

    private int chainAssetsTaskIntervalMinute;
    /*----------------------------------------------------------------------*/

    public boolean isCollectedSmartContractModule() {
        return collectedSmartContractModule;
    }

    public void setCollectedSmartContractModule(boolean collectedSmartContractModule) {
        this.collectedSmartContractModule = collectedSmartContractModule;
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

    public String getDataPath() {
        return dataPath;
    }

    public void setDataPath(String dataPath) {
        this.dataPath = dataPath;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
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

    public String getBlackHolePublicKey() {
        return blackHolePublicKey;
    }

    public void setBlackHolePublicKey(String blackHolePublicKey) {
        this.blackHolePublicKey = blackHolePublicKey;
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

    public long getUnconfirmedTxExpire() {
        return unconfirmedTxExpire;
    }

    public void setUnconfirmedTxExpire(long unconfirmedTxExpire) {
        this.unconfirmedTxExpire = unconfirmedTxExpire;
    }

    public String getBlackListPath() {
        return blackListPath;
    }

    public void setBlackListPath(String blackListPath) {
        this.blackListPath = blackListPath;
    }

    public String getAccountBlockManagerPublicKeys() {
        return accountBlockManagerPublicKeys;
    }

    public void setAccountBlockManagerPublicKeys(String accountBlockManagerPublicKeys) {
        this.accountBlockManagerPublicKeys = accountBlockManagerPublicKeys;
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

    public Set<String> getSeedNodeList() {
        return seedNodeList;
    }

    public void setSeedNodeList(Set<String> seedNodeList) {
        this.seedNodeList = seedNodeList;
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

    public String getBlockAccountManager() {
        return blockAccountManager;
    }

    public void setBlockAccountManager(String blockAccountManager) {
        this.blockAccountManager = blockAccountManager;
    }

    public int getMainChainCommissionRatio() {
        return mainChainCommissionRatio;
    }

    public void setMainChainCommissionRatio(int mainChainCommissionRatio) {
        this.mainChainCommissionRatio = mainChainCommissionRatio;
    }

    @Override
    public long getMaxViewGas() {
        return maxViewGas;
    }

    @Override
    public void setMaxViewGas(long maxViewGas) {
        this.maxViewGas = maxViewGas;
    }

    public String getPackageLogPackages() {
        return packageLogPackages;
    }

    public void setPackageLogPackages(String packageLogPackages) {
        this.packageLogPackages = packageLogPackages;
    }

    public String getPackageLogLevels() {
        return packageLogLevels;
    }

    public void setPackageLogLevels(String packageLogLevels) {
        this.packageLogLevels = packageLogLevels;
    }

    public String getCrossTokenSystemContract() {
        return crossTokenSystemContract;
    }

    public void setCrossTokenSystemContract(String crossTokenSystemContract) {
        this.crossTokenSystemContract = crossTokenSystemContract;
    }

    public String getChainNameMax() {
        return chainNameMax;
    }

    public void setChainNameMax(String chainNameMax) {
        this.chainNameMax = chainNameMax;
    }

    public String getAssetSymbolMax() {
        return assetSymbolMax;
    }

    public void setAssetSymbolMax(String assetSymbolMax) {
        this.assetSymbolMax = assetSymbolMax;
    }

    public String getAssetNameMax() {
        return assetNameMax;
    }

    public void setAssetNameMax(String assetNameMax) {
        this.assetNameMax = assetNameMax;
    }

    public BigInteger getAssetDepositNuls() {
        return assetDepositNuls;
    }

    public void setAssetDepositNuls(BigInteger assetDepositNuls) {
        this.assetDepositNuls = assetDepositNuls;
    }

    public BigInteger getAssetDestroyNuls() {
        return assetDestroyNuls;
    }

    public void setAssetDestroyNuls(BigInteger assetDestroyNuls) {
        this.assetDestroyNuls = assetDestroyNuls;
    }

    public String getAssetDepositNulsDestroyRate() {
        return assetDepositNulsDestroyRate;
    }

    public void setAssetDepositNulsDestroyRate(String assetDepositNulsDestroyRate) {
        this.assetDepositNulsDestroyRate = assetDepositNulsDestroyRate;
    }

    public String getAssetDepositNulsLockRate() {
        return assetDepositNulsLockRate;
    }

    public void setAssetDepositNulsLockRate(String assetDepositNulsLockRate) {
        this.assetDepositNulsLockRate = assetDepositNulsLockRate;
    }

    public String getAssetInitNumberMin() {
        return assetInitNumberMin;
    }

    public void setAssetInitNumberMin(String assetInitNumberMin) {
        this.assetInitNumberMin = assetInitNumberMin;
    }

    public String getAssetInitNumberMax() {
        return assetInitNumberMax;
    }

    public void setAssetInitNumberMax(String assetInitNumberMax) {
        this.assetInitNumberMax = assetInitNumberMax;
    }

    public String getAssetDecimalPlacesMin() {
        return assetDecimalPlacesMin;
    }

    public void setAssetDecimalPlacesMin(String assetDecimalPlacesMin) {
        this.assetDecimalPlacesMin = assetDecimalPlacesMin;
    }

    public String getAssetDecimalPlacesMax() {
        return assetDecimalPlacesMax;
    }

    public void setAssetDecimalPlacesMax(String assetDecimalPlacesMax) {
        this.assetDecimalPlacesMax = assetDecimalPlacesMax;
    }

    public String getAssetRecoveryRate() {
        return assetRecoveryRate;
    }

    public void setAssetRecoveryRate(String assetRecoveryRate) {
        this.assetRecoveryRate = assetRecoveryRate;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public String getNulsAssetInitNumberMax() {
        return nulsAssetInitNumberMax;
    }

    public void setNulsAssetInitNumberMax(String nulsAssetInitNumberMax) {
        this.nulsAssetInitNumberMax = nulsAssetInitNumberMax;
    }

    public String getMainSymbol() {
        return mainSymbol;
    }

    public void setMainSymbol(String mainSymbol) {
        this.mainSymbol = mainSymbol;
    }

    public String getNulsFeeMainNetRate() {
        return nulsFeeMainNetRate;
    }

    public void setNulsFeeMainNetRate(String nulsFeeMainNetRate) {
        this.nulsFeeMainNetRate = nulsFeeMainNetRate;
    }

    public int getNulsFeeMainNetPercent() {
        return nulsFeeMainNetPercent;
    }

    public void setNulsFeeMainNetPercent(int nulsFeeMainNetPercent) {
        this.nulsFeeMainNetPercent = nulsFeeMainNetPercent;
    }

    public int getNulsFeeOtherNetPercent() {
        return nulsFeeOtherNetPercent;
    }

    public void setNulsFeeOtherNetPercent(int nulsFeeOtherNetPercent) {
        this.nulsFeeOtherNetPercent = nulsFeeOtherNetPercent;
    }

    public String getDefaultDecimalPlaces() {
        return defaultDecimalPlaces;
    }

    public void setDefaultDecimalPlaces(String defaultDecimalPlaces) {
        this.defaultDecimalPlaces = defaultDecimalPlaces;
    }

    public int getChainAssetsTaskIntervalMinute() {
        return chainAssetsTaskIntervalMinute;
    }

    public void setChainAssetsTaskIntervalMinute(int chainAssetsTaskIntervalMinute) {
        this.chainAssetsTaskIntervalMinute = chainAssetsTaskIntervalMinute;
    }

    @Override
    public VersionChangeInvoker getVersionChangeInvoker() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return CommonVersionChangeInvoker.instance();
    }
}
