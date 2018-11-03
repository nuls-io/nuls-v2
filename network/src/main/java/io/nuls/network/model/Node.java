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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.util.*;

import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 一个peer节点可以同时为多条链使用，
 * 所以存在节点所属不同网络的状态维护
 *
 * @author lan
 */
public class Node extends BaseNulsData {

    private String id;

    private String ip;

    private int remotePort = 0;
    /**
     * 卫星链节点才有这个port，普通链无
     */
    private int remoteCrossPort = 0;

    private Long lastFailTime=0L;

    private Integer failCount=0;

    private Channel channel;
    /**
     * 是否可连接状态
     */
    private boolean canConnect=true;
    private final static int MAX_FAIL_COUNT=100;

    private boolean isCrossConnect;

    /**
     * 一条peer连接可以同时属于多个group
     */
    private Map<String,NodeGroupConnector> nodeGroupConnectors=new ConcurrentHashMap<>();



    /**
     * 1: inNode SERVER,  2: outNode CLIENT
     */
    public final static int IN = 1;
    public final static int OUT = 2;
    private int type;

    /**
     * 0: wait 等待中 , 1: connecting,握手连接中 2: handshake 握手成功
     */
    public final static int WAIT = 0;
    public final static int CONNECTING = 1;
    public final static int HANDSHAKE = 2;
    public final static int BAD = 3;

    public Node(String ip, int remotePort, int type,boolean isCrossConnect) {
        this(ip+":"+remotePort,ip,remotePort,type,isCrossConnect);
    }


    public Node(String id, String ip,int remotePort, int type,boolean isCrossConnect) {
        this.ip = ip;
        this.remotePort = remotePort;
        this.type = type;
        this.id = id;
        this.isCrossConnect=isCrossConnect;
    }

    public void destroy() {
        this.lastFailTime = TimeService.currentTimeMillis();
        this.channel = null;
    }

    public boolean isCanConnect() {
        return canConnect;
    }

    public void setCanConnect(boolean canConnect) {
        this.canConnect = canConnect;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("id:" + getId() + ",");
        sb.append("type:" + type + ",");
        sb.append("failCount:" + failCount + "}");

        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        Node other = (Node) obj;
        if (StringUtils.isBlank(other.getId())) {
            return false;
        }
        return other.getId().equals(this.getId());
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

    public void setVersionProtocolInfos(long magicNumber,long version,long blockHeight,String blockHash){
        NodeGroupConnector nodeGroupConnector=nodeGroupConnectors.get(String.valueOf(magicNumber));
        nodeGroupConnector.setBlockHeight(blockHeight);
        nodeGroupConnector.setVersion(version);
        nodeGroupConnector.setBlockHash(blockHash);
    }

    /**
     * node节点断开连接后的处理逻辑
     */
    public void disConnectNodeChannel(){
        List<NodeGroupConnector> list=getNodeGroupConnectors();
        for(NodeGroupConnector nodeGroupConnector:list){
            NodeGroup nodeGroup=NodeGroupManager.getInstance().getNodeGroupByMagic(nodeGroupConnector.getMagicNumber());
            nodeGroup.addDisConnetNode(this,true);
        }
        nodeGroupConnectors.clear();
    }
    public List<NodeGroupConnector> getNodeGroupConnectors(){
        List<NodeGroupConnector> list=new ArrayList<>();
        list.addAll(nodeGroupConnectors.values());
        return list;
    }
    public void addGroupConnector(long magicNumber){
        if(null == nodeGroupConnectors.get(String.valueOf(magicNumber))){
            NodeGroupConnector nodeGroupConnector=new NodeGroupConnector(magicNumber);
            nodeGroupConnectors.put(String.valueOf(magicNumber),nodeGroupConnector);
        }else{
            //已经存在了====Already exist
            Log.error(id+" nodeGroupConnector already exist.magicNumber="+magicNumber);
        }
    }

    /**
     *
     * @param magicNumber
     * 移除网络节点
     */
    public void removeGroupConnector(long magicNumber){
        if(null == nodeGroupConnectors.get(String.valueOf(magicNumber))){
            Log.error(id+" nodeGroupConnector not exist.magicNumber="+magicNumber);
        }else{
            nodeGroupConnectors.remove(String.valueOf(magicNumber));
        }
    }

    public NodeGroupConnector getNodeGroupConnector(long magicNumber){
        return nodeGroupConnectors.get(String.valueOf(magicNumber));
    }
    public NodeGroupConnector getFirstNodeGroupConnector(){
        return nodeGroupConnectors.values().iterator().next();
    }
    @Override
    public int size() {
        int s = 0;
        return s;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {

    }

    public boolean isEliminate(){
        return failCount>=MAX_FAIL_COUNT;
    }
    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {

    }
}
