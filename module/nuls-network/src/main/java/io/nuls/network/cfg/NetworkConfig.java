package io.nuls.network.cfg;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.rpc.model.ModuleE;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: lanjinsheng
 * @Time: 2019-03-14 14:11
 * @Description: 配置文件
 */
@Component
@Configuration(domain = ModuleE.Constant.NETWORK)
public class NetworkConfig implements ModuleConfig {
    private String logLevel = "DEBUG";
    private int chainId;
    private int mainChainId;
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

    private int corssMaxInSameIp;
    private String moonSeedIps;
    private List<String> moonSeedIpList;

    private boolean moonNode;

    private String language;
    private String encoding;

    private List<String> localIps = new ArrayList<>();
    private int updatePeerInfoType = 0;
    /**
     * ROCK DB 数据库文件存储路径
     */
    private String dataPath;

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

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
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

    public int getCorssMaxInSameIp() {
        return corssMaxInSameIp;
    }

    public void setCorssMaxInSameIp(int corssMaxInSameIp) {
        this.corssMaxInSameIp = corssMaxInSameIp;
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

    public void setLocalIps(List<String> localIps) {
        this.localIps = localIps;
    }

    public int getUpdatePeerInfoType() {
        return updatePeerInfoType;
    }

    public void setUpdatePeerInfoType(int updatePeerInfoType) {
        this.updatePeerInfoType = updatePeerInfoType;
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
}
