/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.network.constant;

import io.nuls.network.utils.IpUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 加载配置文件的参数放在这个bean对象中
 * The parameters for loading the configuration file are placed in this bean object.
 *
 * @author lan
 */
public class NetworkParam {

    private static NetworkParam instance = new NetworkParam();

    public static NetworkParam getInstance() {
        return instance;
    }

    private NetworkParam() {
        this.localIps.addAll(IpUtil.getIps());
    }

    private int chainId;

    private int port;

    private long packetMagic;

    private int maxInCount;

    private int maxOutCount;

    private int maxInSameIp;

    private List<String> seedIpList;

    private int crossPort;

    private int crossMaxInCount;

    private int crossMaxOutCount;

    private int corssMaxInSameIp;

    private List<String> moonSeedIpList;

    private boolean moonNode;

    private String language;
    private String encoding;
    private String dbPath;

    private List<String> localIps = new ArrayList<>();


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

    public int getMaxInSameIp() {
        return maxInSameIp;
    }

    public void setMaxInSameIp(int maxInSameIp) {
        this.maxInSameIp = maxInSameIp;
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

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
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

    public String getDbPath() {
        return dbPath;
    }

    public void setDbPath(String dbPath) {
        this.dbPath = dbPath;
    }

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
