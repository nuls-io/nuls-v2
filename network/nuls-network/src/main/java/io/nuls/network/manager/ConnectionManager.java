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
package io.nuls.network.manager;


import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;
import io.nuls.network.constant.ManagerStatusEnum;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.loker.Lockers;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.netty.NettyServer;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * 连接管理器,连接的启动，停止，连接引用缓存管理
 * Connection manager, connection start, stop, connection reference cache management
 * @author lan
 * @date 2018/11/01
 *
 */
public class ConnectionManager extends BaseManager{
    TaskManager taskManager = TaskManager.getInstance();
    private static ConnectionManager instance = new ConnectionManager();
    /**
     *作为Server 被动连接的peer
     * Passer as a server passive connection
     */
    private Map<String, Node> cacheConnectNodeInMap=new ConcurrentHashMap<>();
    /**
     * 作为client 主动连接的peer
     *As the client actively connected peer
     */
    private  Map<String, Node> cacheConnectNodeOutMap=new ConcurrentHashMap<>();


    public static ConnectionManager getInstance() {
        return instance;
    }
    private ConnectionManager() {

    }

    /**
     * Server所有被动连接的IP,通过这个集合判断是否存在过载
     * As the Server all passively connected IP, through this set to determine whether there is overload
     * Key:ip+"_"+magicNumber  value: connectNumber
     */
    private  Map<String, Integer> cacheConnectGroupIpInMap=new ConcurrentHashMap<>();
    StorageManager storageManager = StorageManager.getInstance();
    LocalInfoManager localInfoManager = LocalInfoManager.getInstance();
    private ManagerStatusEnum status=ManagerStatusEnum.UNINITIALIZED;

    public  boolean  isRunning(){
        return  instance.status==ManagerStatusEnum.RUNNING;
    }

    /**
     * 加载种子节点
     * @param magicNumber
     */
    public void loadSeedsNode(long magicNumber){
        NetworkParam networkParam=NetworkParam.getInstance();
        List<String> list=networkParam.getSeedIpList();
        NodeGroup nodeGroup= NodeGroupManager.getInstance().getNodeGroupByMagic(networkParam.getPacketMagic());
        for(String seed:list){
            String []peer=seed.split(NetworkConstant.COLON);
            if(localInfoManager.isSelfIp(peer[0])){
                continue;
            }
            Node node=new Node(peer[0],Integer.valueOf(peer[1]),Node.OUT,false);
            nodeGroup.addDisConnetNode(node,false);
        }
    }
    public boolean isPeerSingleGroup(Channel channel){
        SocketChannel socketChannel = (SocketChannel) channel;
        String remoteIP = socketChannel.remoteAddress().getHostString();
        int port = socketChannel.remoteAddress().getPort();
        String nodeId = remoteIP+NetworkConstant.COLON+port;
        Node node = ConnectionManager.getInstance().getNodeByCache(nodeId);
        if(null != node){
            if(null != node.getNodeGroupConnectors() && node.getNodeGroupConnectors().size() > 1){
                return false;
            }
        }
        return true;
    }

    /**
     * 在物理连接断开时时候进行调用
     * Called when the physical connection is broken
     * @param nodeKey
     * @param nodeType
     */
    public void removeCacheConnectNodeMap(String nodeKey,int nodeType){
        Lockers.NODE_ESTABLISH_CONNECT_LOCK.lock();
        try {
            Node node = null;
            if (Node.OUT == nodeType) {
                node = cacheConnectNodeOutMap.get(nodeKey);
                cacheConnectNodeOutMap.remove(nodeKey);
            } else {
                node = cacheConnectNodeInMap.get(nodeKey);
                cacheConnectNodeInMap.remove(nodeKey);
                List<NodeGroupConnector> list = node.getNodeGroupConnectors();
                for (NodeGroupConnector nodeGroupConnector : list) {
                    subGroupMaxInIp(node, nodeGroupConnector.getMagicNumber(), true);
                }
            }
            if(null != node) {
                node.disConnectNodeChannel();
            }
        }finally {
            Lockers.NODE_ESTABLISH_CONNECT_LOCK.unlock();
        }
    }

    /**
     * 通过连接类型来获取peer连接信息
     * Get peer connection information by connection type
     * @param nodeId
     * @param nodeType
     * @return
     */
    public Node getNodeByCache(String nodeId,int nodeType)
    {
        if(Node.OUT == nodeType){
            return cacheConnectNodeOutMap.get(nodeId);
        }else{
            return cacheConnectNodeInMap.get(nodeId);
        }
    }

    /**
     * 通过nodeId来获取peer连接信息，从主动与被动连接缓存中查找
     *Get peer connection information through nodeId,
     * find from active and passive connection cache
     * @param nodeId
     * @return
     */
    public Node getNodeByCache(String nodeId)
    {
        if(null != cacheConnectNodeOutMap.get(nodeId)){
            return cacheConnectNodeOutMap.get(nodeId);
        }else{
            return cacheConnectNodeInMap.get(nodeId);
        }
    }

    public List<Node> getCacheAllNodeList(){
        List<Node> nodesList=new ArrayList<>();
        nodesList.addAll(cacheConnectNodeInMap.values());
        nodesList.addAll(cacheConnectNodeOutMap.values());
        return nodesList;
    }
    /**
     * 处理已经成功建立socket物理连接的节点
     */
    public boolean processConnectNode(Node node) {
        Lockers.NODE_ESTABLISH_CONNECT_LOCK.lock();
        try {
            if (Node.IN == node.getType()) {
                cacheConnectNodeInMap.put(node.getId(), node);
            } else {
                cacheConnectNodeOutMap.put(node.getId(), node);
            }
        }finally {
            Lockers.NODE_ESTABLISH_CONNECT_LOCK.unlock();
        }
        return true;
    }

    public boolean addGroupMaxInIp(Node node,long magicNum){
        String ip=node.getId().split(NetworkConstant.COLON)[0];
        String key=ip+"_"+magicNum;
        if(null != cacheConnectGroupIpInMap.get(key)){
            cacheConnectGroupIpInMap.put(key,cacheConnectGroupIpInMap.get(key)+1);
        }else{
            cacheConnectGroupIpInMap.put(key,1);
        }
        return true;
    }

    /**
     * 减少链接入地址
     * sub chain max Ip
     * @param node
     * @param magicNum
     * @param isAll
     */
    public void subGroupMaxInIp (Node node,long magicNum,boolean isAll){
        String ip=node.getId().split(NetworkConstant.COLON)[0];
        String key=ip+"_"+magicNum;
        if(isAll){
            cacheConnectGroupIpInMap.remove(key);
            return;
        }
        if(null != cacheConnectGroupIpInMap.get(key) && cacheConnectGroupIpInMap.get(key)>1){
            cacheConnectGroupIpInMap.put(key,cacheConnectGroupIpInMap.get(key)-1);
        }else{
            cacheConnectGroupIpInMap.remove(key);
        }
    }


    public boolean isPeerConnectExceedMaxIn(String peerIp,long macgicNumber,int maxInSameIp){
        String key = peerIp+"_"+macgicNumber;
        if(null != cacheConnectGroupIpInMap.get(key)) {
           if(cacheConnectGroupIpInMap.get(key)>= maxInSameIp){
               return true;
           }else{
               return false;
            }
        }else{
            return false;
        }
    }


    public void nettyBoot(){
        serverStart();
        clientStart();
        Log.info("==========================NettyBoot");
    }

    private void serverStart(){
        NettyServer server=new NettyServer(NetworkParam.getInstance().getPort());
        NettyServer serverCross=new NettyServer(NetworkParam.getInstance().getCrossPort());
        server.init();
        serverCross.init();
        ThreadUtils.createAndRunThread("node server start", new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
        }, false);
        ThreadUtils.createAndRunThread("node crossServer start", new Runnable() {
            @Override
            public void run() {
                try {
                    serverCross.start();
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
        }, false);

    }

    private void clientStart() {
        taskManager.clientConnectThreadStart();
    }

    /**
     * connect peer
     * @param node
     */
    public  void connectionNode(Node node) {
        //发起连接
        Lockers.NODE_LAUNCH_CONNECT_LOCK.lock();
        try {
            if(node.isIdle()) {
                node.setIdle(false);
                taskManager.doConnect(node);
            }
        }finally {
            Lockers.NODE_LAUNCH_CONNECT_LOCK.unlock();
        }
    }
    //自我连接
    public void selfConnection(){
        if(LocalInfoManager.getInstance().isConnectedMySelf()){
            return;
        }
        if(LocalInfoManager.getInstance().isSelfNetSeed()){
            return;
        }
        IpAddress ipAddress=LocalInfoManager.getInstance().getExternalAddress();
        Node node=new Node(ipAddress.getIp().getHostAddress(),ipAddress.getPort(),Node.OUT,false);
        connectionNode(node);
        LocalInfoManager.getInstance().setConnectedMySelf(true);
    }

    @Override
    public void init() {
        status=ManagerStatusEnum.INITIALIZED;
        Collection<NodeGroup> nodeGroups=NodeGroupManager.getInstance().getNodeGroupCollection();
        for(NodeGroup nodeGroup:nodeGroups){
            if(nodeGroup.isSelf()){
                //自有网络组，增加种子节点的加载，跨链网络组，则无此步骤
                loadSeedsNode(nodeGroup.getMagicNumber());
            }
            //数据库获取node
            List<Node> nodes=storageManager.getNodesByChainId(nodeGroup.getChainId());
            for(Node node:nodes){
                nodeGroup.addDisConnetNode(node,false);
            }
        }
    }

    @Override
    public void start() {
        nettyBoot();
        status=ManagerStatusEnum.RUNNING;
    }



}
