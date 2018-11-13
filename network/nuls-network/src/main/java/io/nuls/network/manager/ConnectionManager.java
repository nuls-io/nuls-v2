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


import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.threads.NetworkThreadPool;
import io.nuls.network.manager.threads.NodesConnectThread;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.netty.NettyServer;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
/**
 * 连接管理器
 * connection  manager
 * @author lan
 * @date 2018/11/01
 *
 */
public class ConnectionManager {

    private static ConnectionManager instance = new ConnectionManager();
    /**
     *作为Server 被动连接的peer
     */
    private Map<String, Node> cacheConnectNodeInMap=new ConcurrentHashMap<>();
    /**
     * 作为client 主动连接的peer
     */
    private  Map<String, Node> cacheConnectNodeOutMap=new ConcurrentHashMap<>();

    /**
     * Server所有in连接的IP,通过这个集合判断是否存在过载
     * Key:ip+"_"+magicNumber  value: connectNumber
     */
    private  Map<String, Integer> cacheConnectGroupIpInMap=new ConcurrentHashMap<>();

    /**
     * Client所有out连接的IP,通过这个集合判断是否存在相互连接
     * Key:ip  value: connectNumber
     */
    private  Map<String, Integer> cacheConnectIpMap=new ConcurrentHashMap<>();


    public void removeCacheConnectNodeMap(String nodeKey,int nodeType){
        //要加同步锁
        Node node=null;
        String ip=nodeKey.split(NetworkConstant.COLON)[0];
        cacheConnectIpMap.remove(ip);
        if(Node.OUT == nodeType) {
              node = cacheConnectNodeOutMap.get(nodeKey);
              cacheConnectNodeOutMap.remove(nodeKey);
        }else{
              node=cacheConnectNodeInMap.get(nodeKey);
              cacheConnectNodeInMap.remove(nodeKey);
            List<NodeGroupConnector> list=node.getNodeGroupConnectors();
            for(NodeGroupConnector nodeGroupConnector:list){
                    subGroupMaxInIp(node, nodeGroupConnector.getMagicNumber(),true);
            }
        }
        node.disConnectNodeChannel();
    }


    public Node getNodeByCache(String nodeId,int nodeType)
    {
        if(Node.OUT == nodeType){
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
     * 处理已经成功连接的节点
     */
    public boolean processConnectedServerNode(Node node) {
        String ip=node.getId().split(NetworkConstant.COLON)[0];
        cacheConnectIpMap.put(ip,1);
        cacheConnectNodeInMap.put(node.getId(),node);
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

    /**
     * 处理已经成功连接的节点
     */
    public boolean processConnectedClientNode(Node node) {
        cacheConnectNodeOutMap.put(node.getId(),node);
        String ip=node.getId().split(NetworkConstant.COLON)[0];
        cacheConnectIpMap.put(ip,1);
        return true;
    }

    /**
     * juge peer ip Exist
     */
    public boolean isPeerConnectExist(String peerIp){
        if(null != cacheConnectIpMap.get(peerIp)){
            //had connect
            return true;
        }
            return false;
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

    private ConnectionManager() {
    }

    public void nettyBoot(){
        serverStart();
        clientStart();
    }
    public static ConnectionManager getInstance() {
        return instance;
    }

    public void serverStart(){
        NettyServer server=new NettyServer(NetworkParam.getInstance().getPort());
        NettyServer serverCross=new NettyServer(NetworkParam.getInstance().getCrossPort());
        server.init();
        serverCross.init();
        TaskManager.createAndRunThread("node server start", new Runnable() {
            @Override
            public void run() {
                try {
                    server.start();
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
        }, false);
        TaskManager.createAndRunThread("node crossServer start", new Runnable() {
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

    public void clientStart() {
        ScheduledThreadPoolExecutor executor = TaskManager.createScheduledThreadPool(1, new NulsThreadFactory("NodesConnectThread"));
        executor.scheduleAtFixedRate(new NodesConnectThread(), 5, 1000, TimeUnit.SECONDS);
    }

    public void connectionNode(Node node) {
        //发起连接
        NetworkThreadPool.doConnect(node);
    }
    //自我连接
    public void selfConnection(){
        if(LocalInfoManager.getInstance().isConnectedMySelf()){
            return;
        }
        IpAddress ipAddress=LocalInfoManager.getInstance().getExternalAddress();
        Node node=new Node(ipAddress.getIp().getHostAddress(),ipAddress.getPort(),Node.OUT,false);
        NetworkThreadPool.doConnect(node);
        LocalInfoManager.getInstance().setConnectedMySelf(true);
    }
}
