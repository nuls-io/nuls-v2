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
    NodeManager nodeManager= NodeManager.getInstance();
    ConnectionManager connectionManager=ConnectionManager.getInstance();
    private void connectPeer(Collection<Node> nodes,long magicNumber,int leftCount){
        List<String> eliminateNodes = new ArrayList<String>();
        for (Node node : nodes) {
            //满足丢弃条件的nodes
            if (node.isEliminate()) {
                eliminateNodes.add(node.getId());
            } else {
                //判断peer是否已经存在
                if(ConnectionManager.getInstance().isPeerConnectExist(node.getIp(),Node.OUT)){
                    continue;
                }
                if(node.isCanConnect()) {
                    node.addGroupConnector(magicNumber);
                    connectionManager.connectionNode(node);
                    node.setCanConnect(false);
                    leftCount--;
                }else{
                    //去连接缓存中获取连接是否存在，如果存在，则直接进行业务握手
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
            if(0 == leftCount){
                break;
            }
        }
        //TODO:进行eliminateNodes的处理逻辑，删除连接缓存信息，删除库数据
    }
    @Override
    public void run() {
        Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
        if(!nodeManager.isRunning()){
           return;
        }
        try {
            Collection<NodeGroup> nodeGroups = NodeGroupManager.getNodeGroupCollection();
            for (NodeGroup nodeGroup : nodeGroups) {
                if (!nodeGroup.isHadMaxOutFull()) {
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
                    Collection<Node> nodes = nodeGroup.getDisConnectCrossNodes();
                    List <Node> nodesList=new ArrayList<>();
                    nodesList.addAll(nodes);
                    Collections.shuffle(nodesList);
                    int leftCount= nodeGroup.getMaxCrossOut()-nodeGroup.getHadCrossConnectOut();
                    connectPeer(nodesList,nodeGroup.getMagicNumber(),leftCount);
                }
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        }
    }

