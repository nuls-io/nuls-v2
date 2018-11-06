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

import java.util.List;
import java.util.Set;

/**
 *
 * 网络参数
 * @author  lan
 *
 */
public class NetworkParam {

    private static NetworkParam instance = new NetworkParam();

    public static NetworkParam getInstance() {
        return instance;
    }

    private NetworkParam() {
    }
    private int chainId;

    private int port;

    private long packetMagic;

    private int maxInCount;

    private int maxOutCount;

    private List<String> seedIpList;

    private int crossPort;

    private int moonMaxInCount;

    private int moonMaxOutCount;

    private List<String> moonSeedIpList;

    private boolean moonNode;



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

    public int getMoonMaxInCount() {
        return moonMaxInCount;
    }

    public void setMoonMaxInCount(int moonMaxInCount) {
        this.moonMaxInCount = moonMaxInCount;
    }

    public int getMoonMaxOutCount() {
        return moonMaxOutCount;
    }

    public void setMoonMaxOutCount(int moonMaxOutCount) {
        this.moonMaxOutCount = moonMaxOutCount;
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
}
