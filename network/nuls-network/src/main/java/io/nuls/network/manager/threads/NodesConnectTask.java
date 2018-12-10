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
import io.nuls.tools.log.Log;

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
public class NodesConnectTask implements Runnable  {
    ConnectionManager connectionManager = ConnectionManager.getInstance();
    StorageManager storageManager = StorageManager.getInstance();
    private void connectPeer(Collection<Node> nodes,long magicNumber){
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
                /**
                 * 判断是否被动连接里已经存在此链的 连接,如果业务已经存在则跳过
                 */
                if(connectionManager.isPeerConnectExceedMaxIn(node.getIp(),magicNumber,1)){
                    continue;
                }

                /**
                 * 由于被拒绝过连接，会存在锁定名单，判断peer连接是否还在锁定时间内
                 */
                if(nodeGroup.isInLockTime(node.getId())){
                    continue;
                }
                /**
                 * 判断节点是否处于闲置状态
                 */
                if(node.isIdle()) {
                    node.addGroupConnector(magicNumber);
                    connectionManager.connectionNode(node);
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
                        }
                    }else{
                        Log.error(node.getId()+" not in connect pool.");
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
        if(!connectionManager.isRunning()){
           return;
        }
            Collection<NodeGroup> nodeGroups = NodeGroupManager.getInstance().getNodeGroupCollection();
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
                    connectPeer(nodes,nodeGroup.getMagicNumber());
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
                    connectPeer(nodesList,nodeGroup.getMagicNumber());
                }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

