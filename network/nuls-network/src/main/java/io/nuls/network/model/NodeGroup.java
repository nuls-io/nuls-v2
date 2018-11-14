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

import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.dto.Dto;
import io.nuls.network.model.message.ByeMessage;
import io.nuls.network.model.message.body.ByeMessageBody;
import io.nuls.network.model.po.BasePo;
import io.nuls.network.model.po.NodeGroupPo;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 节点组对象
 * @author lan
 * @date 2018/11/01
 *
 */
public class NodeGroup  implements Dto {
    private long magicNumber = 0;
    private int chainId = 0;;
    private int maxOut = 0;;
    private int maxIn = 0;;
    /**
     * 友链跨链最大连接数
     */
    private int maxCrossOut = 0;;
    private int maxCrossIn = 0;;

    private int minAvailableCount = 0;
    /**
     * 最大高度peer id
     */
    private volatile String hightestBlockNodeId = "";
    /**
     *   网络最大高度
     */
    private volatile long hightest = 0;


    /**
     * 跨链网络是否激活,卫星链上的默认跨链true,
     * 友链默认false，在跨链模块请求时候这个属性才为true
     */
    private boolean isCrossActive = false;

    /**
     * 卫星链注册的跨链Group时为false
     */
    private boolean isSelf = true;
    /**
     * 是否卫星网络,只要卫星链上的节点就是true，如果是友链节点为false
     * 节点是否是卫星节点
     */
    private boolean isMoonNet = false;
    /**
     * self net-未连接或等待握手的节点
     */
    private  Map<String, Node> disConnectNodeMap = new ConcurrentHashMap<>();
    /**
     * self net-已经连接的节点
     */
    private  Map<String, Node> connectNodeMap = new ConcurrentHashMap<>();
    /**
     * 跨链-未连接或等待握手的节点
     */
     private  Map<String, Node> disConnectCrossNodeMap = new ConcurrentHashMap<>();
    /**
     * 跨链-已经连接的节点
     */
    private  Map<String, Node> connectCrossNodeMap = new ConcurrentHashMap<>();

    /**
     * 最近一次握手成功时间
     */
    private volatile long latestHandshakeSuccTime = 0;
    /**
     *
     * 被锁定或拒绝的连接 key:nodeId value:System.getCurrentMillion+x
     */
    private Map<String ,Long> failConnectMap = new ConcurrentHashMap<>();

    private volatile int hadConnectOut=0;

    private volatile int hadConnectIn=0;

    private volatile int hadCrossConnectOut=0;

    private volatile  int hadCrossConnectIn=0;

    private static ReentrantLock NETWORK_GROUP_NODE_CONNECT_LOCK = new ReentrantLock();


    /**
     *   GROUP  STATUS
     *   初始创建时候不可用状态是WAIT1
     *   到达OK后震荡不可用状态是WAIT2
     *
     */
    public final static int WAIT1 = 1;
    public final static int WAIT2= 2;
    public final static int OK = 3;
    public final static int DESTROY = -1;
    public final static int RECONNECT = -2;
    private volatile int status;

    public NodeGroup(long  magicNumber,int chainId,int maxIn,int maxOut,int minAvailableCount,boolean isMoonNet) {
        this.magicNumber = magicNumber;
        this.chainId=chainId;
        this.maxIn=maxIn;
        this.maxOut=maxOut;
        this.minAvailableCount=minAvailableCount;
        this.isMoonNet=isMoonNet;
        this.status=WAIT1;
        if(isMoonNet){
            isCrossActive=true;
        }
    }



    public   Collection<Node> getConnectNodes(){

        return  connectNodeMap.values();
    }
    public   Collection<Node> getDisConnectNodes(){
        return  disConnectNodeMap.values();
    }

    public   Collection<Node> getDisConnectCrossNodes(){
        return  disConnectCrossNodeMap.values();
    }
    public   Collection<Node> getConnectCrossNodes(){
        return  connectCrossNodeMap.values();
    }

    public long getLatestHandshakeSuccTime() {
        return latestHandshakeSuccTime;
    }

    public void setLatestHandshakeSuccTime(long latestHandshakeSuccTime) {
        this.latestHandshakeSuccTime = latestHandshakeSuccTime;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getMaxOut() {
        return maxOut;
    }

    public void setMaxOut(int maxOut) {
        this.maxOut = maxOut;
    }

    public int getMaxIn() {
        return maxIn;
    }

    public void setMaxIn(int maxIn) {
        this.maxIn = maxIn;
    }

    public int getMinAvailableCount() {
        return minAvailableCount;
    }

    public void setMinAvailableCount(int minAvailableCount) {
        this.minAvailableCount = minAvailableCount;
    }

    public boolean isMoonNet() {
        return isMoonNet;
    }

    public void setMoonNet(boolean moonNet) {
        isMoonNet = moonNet;
    }

    public String getHightestBlockNodeId() {
        return hightestBlockNodeId;
    }

    public void setHightestBlockNodeId(String hightestBlockNodeId) {
        this.hightestBlockNodeId = hightestBlockNodeId;
    }

    public long getHightest() {
        return hightest;
    }

    public void setHightest(long hightest) {
        this.hightest = hightest;
    }
    public boolean isHadMaxOutFull() {
        return maxOut <= hadConnectOut;
    }

    public Node getConnectNode(String nodeId){
        return  this.getConnectNodeMap().get(nodeId);
    }
    public Node getDisConnectNode(String nodeId){
        return  this.getConnectNodeMap().get(nodeId);
    }

    public Node getConnectCrossNode(String nodeId){
        return  this.getConnectCrossNodeMap().get(nodeId);
    }
    public boolean isHadMaxInFull() {
        return maxIn <= hadConnectIn;
    }


    public boolean isHadCrossMaxOutFull() {
        return maxCrossOut <= hadCrossConnectOut;
    }

    public boolean isHadCrossMaxInFull() {
        return maxCrossIn <= hadCrossConnectIn;
    }

    public Map<String, Node> getDisConnectNodeMap() {
        return disConnectNodeMap;
    }

    public void setDisConnectNodeMap(Map<String, Node> disConnectNodeMap) {
        this.disConnectNodeMap = disConnectNodeMap;
    }

    public Map<String, Node> getConnectNodeMap() {
        return connectNodeMap;
    }

    public void setConnectNodeMap(Map<String, Node> connectNodeMap) {
        this.connectNodeMap = connectNodeMap;
    }

    public Map<String, Node> getDisConnectCrossNodeMap() {
        return disConnectCrossNodeMap;
    }

    public void setDisConnectCrossNodeMap(Map<String, Node> disConnectCrossNodeMap) {
        this.disConnectCrossNodeMap = disConnectCrossNodeMap;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isCrossActive() {
        return isCrossActive;
    }

    public void setCrossActive(boolean crossActive) {
        isCrossActive = crossActive;
    }

    public Map<String, Node> getConnectCrossNodeMap() {
        return connectCrossNodeMap;
    }

    public void setConnectCrossNodeMap(Map<String, Node> connectCrossNodeMap) {
        this.connectCrossNodeMap = connectCrossNodeMap;
    }

    public int getMaxCrossOut() {
        return maxCrossOut;
    }

    public int getHadConnectOut() {
        return hadConnectOut;
    }

    public void setHadConnectOut(int hadConnectOut) {
        this.hadConnectOut = hadConnectOut;
    }

    public int getHadConnectIn() {
        return hadConnectIn;
    }

    public void setHadConnectIn(int hadConnectIn) {
        this.hadConnectIn = hadConnectIn;
    }

    public int getHadCrossConnectOut() {
        return hadCrossConnectOut;
    }

    public void setHadCrossConnectOut(int hadCrossConnectOut) {
        this.hadCrossConnectOut = hadCrossConnectOut;
    }

    public int getHadCrossConnectIn() {
        return hadCrossConnectIn;
    }

    public void setHadCrossConnectIn(int hadCrossConnectIn) {
        this.hadCrossConnectIn = hadCrossConnectIn;
    }

    public void setMaxCrossOut(int maxCrossOut) {
        this.maxCrossOut = maxCrossOut;
    }

    public int getMaxCrossIn() {
        return maxCrossIn;
    }

    public void setMaxCrossIn(int maxCrossIn) {
        this.maxCrossIn = maxCrossIn;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }


    public boolean existSelfGroupList(String nodeId){
        if(null == this.getConnectNodeMap().get(nodeId) && null == this.getDisConnectNodeMap().get(nodeId)){
            return false;
        }
        return true;
    }
    public boolean existCrossGroupList(String nodeId){
        if( null == this.getConnectCrossNodeMap().get(nodeId) && null == this.getDisConnectCrossNodeMap().get(nodeId)){
            return false;
        }
        return true;
    }

    /**
     * 移除节点,判断是否承载有多链业务
     * 删除peer，注销链，bye消息，重载链中调用
     * @param node
     * @param connectChange
     */
    public boolean removePeerNode(Node node,boolean connectChange,boolean sendBye){
        NodeGroupConnector connector = node.getNodeGroupConnector(magicNumber);
        if(null != connector && node.getNodeGroupConnectors().size() == 1){
            //channelInactive code all the remove logic
            node.getChannel().close();
        }else{
            //Just remove the node group relation
            if(sendBye){
                //sendBye
                if(null != connector && node.getNodeGroupConnectors().size() > 1){
                    //send the Bye message
                    ByeMessage byeMessage = MessageFactory.getInstance().buildByeMessage(node,magicNumber,ByeMessageBody.CODE_BYE);
                    MessageManager.getInstance().sendToNode(byeMessage,node,true);
                }
            }
            addDisConnetNode(node,connectChange);
            if(Node.IN ==  node.getType()) {
                ConnectionManager.getInstance().subGroupMaxInIp(node, magicNumber, false);
            }
            node.removeGroupConnector(magicNumber);
        }
        return true;
    }
    public  Map<String,Node> getNodeMapByType(boolean isCross,boolean isConnect){
        if(!isCross){
            if (isConnect) {
                return connectNodeMap;
            }else{
                return disConnectNodeMap;
            }
        }else{
            if (isConnect) {
                return connectCrossNodeMap;
            }else{
                return disConnectCrossNodeMap;
            }
        }
    }
    /**
     * 如果是连接的节点变更 connectChange=true，如果不是connectChange=false
     * @param node
     * @param connectChange
     */
    public void addDisConnetNode(Node node,boolean connectChange){
        NETWORK_GROUP_NODE_CONNECT_LOCK.lock();
        try {
            Map<String, Node> connectMap = getNodeMapByType(node.isCrossConnect(), true);
            Map<String, Node> disConnectMap = getNodeMapByType(node.isCrossConnect(), false);
            if (Node.OUT == node.getType()) {
                disConnectMap.put(node.getId(), node);
            }
            if (connectChange) {
                if (Node.OUT == node.getType()) {
                    if (node.isCrossConnect()) {
                        hadCrossConnectOut--;
                    } else {
                        hadConnectOut--;
                    }
                } else {
                    if (node.isCrossConnect()) {
                        hadCrossConnectIn--;
                    } else {
                        hadConnectIn--;
                    }
                }
            }
            if (connectMap.containsKey(node.getId())) {
                connectMap.remove(node.getId());
            }
        }finally {
            NETWORK_GROUP_NODE_CONNECT_LOCK.unlock();
        }
    }
    /**
     * 如果是连接的节点变更 connectChange=true，如果不是connectChange=false
     * @param node
     * @param connectChange
     */
    public void addConnetNode(Node node,boolean connectChange){
        NETWORK_GROUP_NODE_CONNECT_LOCK.lock();
        try {
            Map<String,Node> connectMap = getNodeMapByType(node.isCrossConnect(),true);
            Map<String,Node> disConnectMap = getNodeMapByType(node.isCrossConnect(),false);
            connectMap.put(node.getId(), node);
            if(connectChange) {
                if (Node.OUT == node.getType()) {
                    if(node.isCrossConnect()){
                        hadCrossConnectOut++;
                    }else {
                        hadConnectOut++;
                    }
                } else {
                    if(node.isCrossConnect()){
                        hadCrossConnectIn++;
                    }else{
                        hadConnectIn++;
                    }

                }
            }
            if (disConnectMap.containsKey(node.getId())) {
                disConnectMap.remove(node.getId());
            }
        }finally {
            NETWORK_GROUP_NODE_CONNECT_LOCK.unlock();
        }
    }


    /**
     *
     * 网络是否可使用 非自有网络满足跨链连接最小数
     * @return
     */
    public boolean isActive(){
        if(DESTROY == status || RECONNECT == status){
            return false;
        }

        int activeConnectNum=0;
        if(isSelf){
            activeConnectNum=connectNodeMap.size();
        }else{
            activeConnectNum=connectCrossNodeMap.size();
        }
        if(isMoonNet){
            if( activeConnectNum < minAvailableCount){
                return false;
            }
        }
        if(activeConnectNum > 0) {
            return true;
        }
        return false;
    }
    /**
     * 获取网络中最高区块连接器
     * @return
     */
    public NodeGroupConnector getHightestNodeGroupInfo(){
        Node node=connectNodeMap.get(hightestBlockNodeId);
        if(null != node){
            NodeGroupConnector nodeGroupConnector=node.getNodeGroupConnector(magicNumber);
            if(null != nodeGroupConnector){
                return nodeGroupConnector;
            }
        }
        //上面无法获取的最高网络信息，就只能遍历
        long blockHeight=0;
        NodeGroupConnector returnConnector=null;
        String nodeId=null;
        Collection<Node> nodes=connectNodeMap.values();
        for(Node cNode:nodes){
            NodeGroupConnector nodeGroupConnector=cNode.getNodeGroupConnector(magicNumber);
            if(null != nodeGroupConnector){
                if(nodeGroupConnector.getBlockHeight()> blockHeight){
                    returnConnector=nodeGroupConnector;
                    blockHeight = nodeGroupConnector.getBlockHeight();
                    nodeId = cNode.getId();
                }
            }
        }
        if(null != nodeId){
            hightestBlockNodeId =nodeId;
        }
        return returnConnector;
    }


    @Override
    public BasePo parseToPo(){
        NodeGroupPo po=new NodeGroupPo();
        po.setChainId(chainId);
        po.setCrossActive(isCrossActive);
        po.setMagicNumber(magicNumber);
        po.setMaxCrossIn(maxCrossIn);
        po.setMaxCrossOut(maxCrossOut);
        po.setMaxIn(maxIn);
        po.setMaxOut(maxOut);
        po.setMinAvailableCount(minAvailableCount);
        po.setMoonNet(isMoonNet);
        po.setSelf(isSelf);
        return po;
    }

    public void destroy(){
        this.status = DESTROY;
        NodeGroupManager.getInstance().removeNodeGroup(chainId);
        Collection<Node> nodes=  getConnectNodes();
        for(Node node:nodes){
            removePeerNode(node,true,true);
        }
        Collection<Node> crossNodes=  getConnectCrossNodes();
        for(Node node:crossNodes){
            removePeerNode(node,true,true);
        }
        this.getConnectNodeMap().clear();
        this.getDisConnectNodeMap().clear();
        this.getConnectCrossNodeMap().clear();
        this.getDisConnectCrossNodeMap().clear();
    }

    public void reconnect(){
        this.status = RECONNECT;
        NodeGroupManager.getInstance().removeNodeGroup(chainId);
        Collection<Node> nodes=  getConnectNodes();
        for(Node node:nodes){
            removePeerNode(node,true,true);
        }
        Collection<Node> crossNodes=  getConnectCrossNodes();
        for(Node node:crossNodes){
            removePeerNode(node,true,true);
        }
        this.status = WAIT1;
    }

    public boolean isLock(){
        if(DESTROY == status || RECONNECT == status){
            return true;
        }
        return false;
    }

    public void  addFailConnect(String nodeId,int addMinute){
        failConnectMap.put(nodeId,System.currentTimeMillis()+addMinute*60*1000);
    }
    public boolean isFreedFailLockTime(String nodeId){
        if(null != failConnectMap.get(nodeId)){
           if(failConnectMap.get(nodeId)> System.currentTimeMillis()){
                return false;
           }
        }
        return true;
    }
}
