/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.network.model;

import io.netty.channel.Channel;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.dto.Dto;
import io.nuls.network.model.po.BasePo;
import io.nuls.network.model.po.NodePo;
import io.nuls.network.netty.listener.EventListener;

/**
 * 一个peer节点可以同时为多条链使用，
 * 所以存在节点所属不同网络的状态维护
 *
 * @author lan
 */
public class Node implements Dto {

    /**
     * 1: inNode SERVER,  2: outNode CLIENT
     */
    public final static int IN = 1;
    public final static int OUT = 2;

    private long magicNumber;
    private String id;

    private String ip;

    private int remotePort = 0;
    /**
     * 跨链节点才有这个port，存跨链port 普通链无
     */
    private int remoteCrossPort = 0;

    private Long lastFailTime = 0L;

    private Integer failCount = 0;

    private Channel channel;

    /**
     * 节点外网Ip
     */
    private String externalIp;

    /**
     * 是否跨链连接
     */
    private boolean isCrossConnect;

    private int type;

    private long connectTime = 0;
    private long version = 0;
    private long blockHeight = 0;
    private String blockHash = "";
    /**
     * NodeStatusEnum
     */

    private int status;

    /**
     * NodeConnectStatusEnum
     */


    private int connectStatus;
    private boolean isSeedNode;

    private Long lastProbeTime = 0L;
    private EventListener registerListener;
    private EventListener connectedListener;
    private EventListener disconnectListener;


    public Node(long magicNumber, String ip, int remotePort, int type, boolean isCrossConnect) {
        this(ip + NetworkConstant.COLON + remotePort, magicNumber, ip, remotePort, type, isCrossConnect);
    }


    public Node(String id, long magicNumber, String ip, int remotePort, int type, boolean isCrossConnect) {
        this.ip = ip;
        this.magicNumber = magicNumber;
        this.remotePort = remotePort;
        this.type = type;
        this.id = id;
        this.isCrossConnect = isCrossConnect;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public void close() {
        this.channel.close();
        this.channel = null;
    }

    public EventListener getRegisterListener() {
        return registerListener;
    }

    public void setRegisterListener(EventListener registerListener) {
        this.registerListener = registerListener;
    }

    public EventListener getConnectedListener() {
        return connectedListener;
    }

    public void setConnectedListener(EventListener connectedListener) {
        this.connectedListener = connectedListener;
    }

    public EventListener getDisconnectListener() {
        return disconnectListener;
    }

    public void setDisconnectListener(EventListener disconnectListener) {
        this.disconnectListener = disconnectListener;
    }


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    public Integer getFailCount() {
        if (failCount == null) {
            failCount = 0;
        }
        return failCount;
    }

    public void setFailCount(Integer failCount) {
        this.failCount = failCount;
    }


    public Long getLastFailTime() {
        if (lastFailTime == null) {
            lastFailTime = 0L;
        }
        return lastFailTime;
    }

    public void setLastFailTime(Long lastFailTime) {
        this.lastFailTime = lastFailTime;
    }

    public String getId() {
        return ip + ":" + remotePort;
    }

    public String getPoId() {
        id = ip + ":" + remotePort;
        return id;
    }

    public boolean isSeedNode() {
        return isSeedNode;
    }

    public void setSeedNode(boolean seedNode) {
        isSeedNode = seedNode;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public int getRemoteCrossPort() {
        return remoteCrossPort;
    }

    public void setRemoteCrossPort(int remoteCrossPort) {
        this.remoteCrossPort = remoteCrossPort;
    }


    public boolean isCrossConnect() {
        return isCrossConnect;
    }

    public void setCrossConnect(boolean crossConnect) {
        isCrossConnect = crossConnect;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getConnectStatus() {
        return connectStatus;
    }

    public void setConnectStatus(int connectStatus) {
        this.connectStatus = connectStatus;
    }

    public Long getLastProbeTime() {
        return lastProbeTime;
    }

    public void setLastProbeTime(Long lastProbeTime) {
        this.lastProbeTime = lastProbeTime;
    }

    public String getExternalIp() {
        return externalIp;
    }

    public void setExternalIp(String externalIp) {
        this.externalIp = externalIp;
    }

    public boolean isServer() {
        if (IN == type) {
            return true;
        } else {
            return false;
        }
    }

    public long getConnectTime() {
        return connectTime;
    }

    public void setConnectTime(long connectTime) {
        this.connectTime = connectTime;
    }

    public NodeGroup getNodeGroup() {
        return NodeGroupManager.getInstance().getNodeGroupByMagic(magicNumber);
    }

    /**
     * 设置peer信息
     *
     * @param version     long
     * @param blockHeight long
     * @param blockHash   String
     */
    public void setVersionProtocolInfos(long version, long blockHeight, String blockHash) {
        this.version = version;
        this.blockHash = blockHash;
        this.blockHeight = blockHeight;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    @Override
    public BasePo parseToPo() {
        return new NodePo(magicNumber, id, ip, remotePort, remoteCrossPort, isCrossConnect);
    }
}
