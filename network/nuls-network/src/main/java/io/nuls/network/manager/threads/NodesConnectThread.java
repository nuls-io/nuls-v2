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
package io.nuls.network.manager.threads;

import io.nuls.network.manager.*;
import io.nuls.network.manager.handler.NetworkMessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.message.VersionMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 定时补充建立连接
 * ues add peer connect
 * @author lan
 * @date 2018/11/01
 *
 */
public class NodesConnectThread implements Runnable  {
    NodeManager nodeManager = NodeManager.getInstance();
    ConnectionManager connectionManager = ConnectionManager.getInstance();
    StorageManager storageManager = StorageManager.getInstance();
    private void connectPeer(Collection<Node> nodes,long magicNumber,int leftCount){
        List<String> eliminateNodes = new ArrayList<String>();
       NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(magicNumber);
        for (Node node : nodes) {
            //满足丢弃条件的nodes
            if (node.isEliminate()) {
                if(node.isCrossConnect()){
                    nodeGroup.getDisConnectCrossNodeMap().remove(node.getId());
                }else{
                    nodeGroup.getDisConnectNodeMap().remove(node.getId());
                }
                eliminateNodes.add(node.getId());
            } else {
                //判断peer是否已经存在
                if(ConnectionManager.getInstance().isPeerConnectExist(node.getIp())){
                    continue;
                }
                //不在重连时间内
                if(!nodeGroup.isFreedFailLockTime(node.getId())){
                    continue;
                }
                if(node.isCanConnect()) {
                    node.addGroupConnector(magicNumber);
                    connectionManager.connectionNode(node);
                    node.setCanConnect(false);
                    leftCount--;
                }else{
                    //去连接缓存中获取连接是否存在，如果存在，直接进行业务握手
                    Node activeNode=ConnectionManager.getInstance().getNodeByCache(node.getId(),node.getType());
                    if(null != activeNode) {
                        NodeGroupConnector nodeGroupConnector=node.getNodeGroupConnector(magicNumber);
                        if(null == nodeGroupConnector){
                            node.addGroupConnector(magicNumber);
                            VersionMessage versionMessage = MessageFactory.getInstance().buildVersionMessage(activeNode, magicNumber);
                            BaseMeesageHandlerInf handler=NetworkMessageHandlerFactory.getInstance().getHandler(versionMessage);
                            handler.send(versionMessage, node, false,true);
                            leftCount--;
                        }
                    }

                }

            }
        }
        //删除无效节点
        storageManager.delGroupNodes(eliminateNodes,nodeGroup.getChainId());
    }
    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        if(!nodeManager.isRunning()){
           return;
        }
            Collection<NodeGroup> nodeGroups = NodeGroupManager.getNodeGroupCollection();
            for (NodeGroup nodeGroup : nodeGroups) {
                try {
                if(nodeGroup.isLock()){
                    continue;
                }
                if (!nodeGroup.isHadMaxOutFull()) {
                    //地址请求
                    MessageManager.getInstance().sendGetAddrMessage(nodeGroup.getMagicNumber(),false,true);
                    Collection<Node> nodes = nodeGroup.getDisConnectNodes();
                    List <Node> nodesList=new ArrayList<>();
                    nodesList.addAll(nodes);
                    Collections.shuffle(nodesList);
                    int leftCount= nodeGroup.getMaxOut()-nodeGroup.getHadConnectOut();
                    connectPeer(nodes,nodeGroup.getMagicNumber(),leftCount);
                }
                /**
                 * 跨链连接
                 */
                if(!nodeGroup.isHadCrossMaxOutFull()){
                    //地址请求
                    MessageManager.getInstance().sendGetAddrMessage(nodeGroup.getMagicNumber(),true,true);
                    Collection<Node> nodes = nodeGroup.getDisConnectCrossNodes();
                    List <Node> nodesList=new ArrayList<>();
                    nodesList.addAll(nodes);
                    Collections.shuffle(nodesList);
                    int leftCount= nodeGroup.getMaxCrossOut()-nodeGroup.getHadCrossConnectOut();
                    connectPeer(nodesList,nodeGroup.getMagicNumber(),leftCount);
                }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

