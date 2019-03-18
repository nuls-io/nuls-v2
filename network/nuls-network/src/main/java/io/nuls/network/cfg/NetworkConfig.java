package io.nuls.network.cfg;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.annotation.Value;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: lanjinsheng
 * @Time: 2019-03-14 14:11
 * @Description:
 * 配置文件
 */
@Configuration(persistDomain = "network")
@Data
public class NetworkConfig {

    private int chainId;

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

    /**
     * ROCK DB 数据库文件存储路径
     */
    @Value("DataPath")
    private String dataPath;

    public List<String> getLocalIps() {

        return localIps;
    }

    public String getExternalIp() {
        if (localIps.size() > 0) {
            return localIps.get(localIps.size() - 1);
        }
        return null;
    }
}
