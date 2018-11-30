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
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.LocalInfoManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.dto.Dto;
import io.nuls.network.model.po.BasePo;
import io.nuls.network.model.po.NodePo;
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
public class Node extends BaseNulsData  implements Dto {

    /**
     * 1: inNode SERVER,  2: outNode CLIENT
     */
    public final static int IN = 1;
    public final static int OUT = 2;


    private String id;

    private String ip;

    private int remotePort = 0;
    /**
     * 跨链节点才有这个port，存跨链port 普通链无
     */
    private int remoteCrossPort = 0;

    private Long lastFailTime=0L;

    private Integer failCount=0;

    private Channel channel;
    /**
     * 是否可连接状态
     */
    private boolean isIdle=true;

    /**
     * 是否跨链连接
     */
    private boolean isCrossConnect;

    private int type;

    private volatile  boolean isBad=false;


    /**
     *
     * 1:N 连接器,一条peer连接可以同时属于多个group
     */
    private Map<String,NodeGroupConnector> nodeGroupConnectors=new ConcurrentHashMap<>();



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

    public boolean isIdle() {
        return isIdle;
    }

    public void setIdle(boolean idle) {
        isIdle = idle;
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

    public boolean isBad() {
        return isBad;
    }

    public void setBad(boolean bad) {
        isBad = bad;
    }

    /**
     * 设置peer信息
     * @param magicNumber
     * @param version
     * @param blockHeight
     * @param blockHash
     */
    public void setVersionProtocolInfos(long magicNumber,long version,long blockHeight,String blockHash){
        NodeGroupConnector nodeGroupConnector=nodeGroupConnectors.get(String.valueOf(magicNumber));
        nodeGroupConnector.setBlockHeight(blockHeight);
        nodeGroupConnector.setVersion(version);
        nodeGroupConnector.setBlockHash(blockHash);
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(magicNumber);
        if(blockHeight > nodeGroup.getHightest()){
            nodeGroup.setHightestBlockNodeId(this.getId());
            nodeGroup.setHightest(blockHeight);
        }
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
        //自己节点Ip,移除(自己是种子节点的情况)
        if (LocalInfoManager.getInstance().isSelfIp(ip)) {
            return true;
        }
        //如果是种子节点，不去移除
        //not eliminate if seed node
        if(NetworkParam.getInstance().getSeedIpList().contains(id)){
            return false;
        }
        return isBad;
    }
    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {

    }


    @Override
    public BasePo parseToPo() {
        NodePo   nodePo = new NodePo(id, ip,remotePort,remoteCrossPort,isCrossConnect );
        return nodePo;
    }
}
