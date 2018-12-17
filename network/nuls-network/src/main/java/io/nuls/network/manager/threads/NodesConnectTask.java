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
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.message.VersionMessage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * 连接定时器，主要任务：
 *  1>询问种子节点要更多的地址
 *  2>补充未饱和的网络连接
 *  3>清除无效的网络连接
 * Connection timer, main tasks:
 * 1> Ask the seed node for more addresses
 * 2>Supply an unsaturated network connection
 * 3> Clear invalid network connections
 * ues add peer connect
 * @author lan
 * @date 2018/11/01
 *
 */
public class NodesConnectTask implements Runnable  {
    private ConnectionManager connectionManager = ConnectionManager.getInstance();
    private StorageManager storageManager = StorageManager.getInstance();
    private void connectPeer(Collection<Node> nodes,long magicNumber){
        if(null == nodes){
            return;
        }
        List<Node> nodesList = new ArrayList<>(nodes);
        Collections.shuffle(nodesList);
        List<String> eliminateNodes = new ArrayList<>();
       NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(magicNumber);
        for (Node node : nodesList) {
            //满足丢弃条件的nodes
            if (node.isEliminate()) {
                if(node.isCrossConnect()){
                    nodeGroup.getDisConnectCrossNodeMap().remove(node.getId());
                }else{
                    nodeGroup.getDisConnectNodeMap().remove(node.getId());
                }
                eliminateNodes.add(node.getId());
            } else {
                /*
                 * 判断是否被动连接里已经存在此链的 连接,如果业务已经存在则跳过
                 * Determine if the connection to this chain already exists in the passive connection, skip if the service already exists
                 */
                if(connectionManager.isPeerConnectExceedMaxIn(node.getIp(),magicNumber,1)){
                    continue;
                }

                /*
                 * 由于被拒绝过连接，会存在锁定名单，判断peer连接是否还在锁定时间内
                 * Since the connection is rejected, there will be a lock list to determine if the peer connection is still in the lock time.
                 */
                if(nodeGroup.isInLockTime(node.getId())){
                    continue;
                }
                /*
                 * 判断节点是否处于闲置状态
                 * Determine if the node is idle
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
                            BaseMeesageHandlerInf handler=MessageHandlerFactory.getInstance().getHandler(versionMessage);
                            handler.send(versionMessage, node, false,true);
                        }
                    }else{
                        Log.error(node.getId()+" not in connect pool.");
                    }

                }

            }
        }
        /*
         * 删除无效节点
         * Delete invalid node
         */
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
                    /*
                     * 连接不饱和，向种子节点寻求更多的地址
                     *Connection is not saturated, seek more addresses from the seed node
                     */
                    MessageManager.getInstance().sendGetAddrMessage(nodeGroup.getMagicNumber(),false,true);
                    Collection<Node> nodes = nodeGroup.getDisConnectNodes();
                    connectPeer(nodes,nodeGroup.getMagicNumber());
                }
                /*
                 * 跨链连接
                 * Cross-chain connection
                 */
                if(!nodeGroup.isHadCrossMaxOutFull()){
                    /*
                     * 连接不饱和，向种子节点寻求更多的地址
                     *Connection is not saturated, seek more addresses from the seed node
                     */
                    MessageManager.getInstance().sendGetAddrMessage(nodeGroup.getMagicNumber(),true,true);
                    Collection<Node> nodes = nodeGroup.getDisConnectCrossNodes();
                    connectPeer(nodes,nodeGroup.getMagicNumber());
                }


                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

