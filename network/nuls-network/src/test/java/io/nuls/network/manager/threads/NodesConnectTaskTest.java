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

import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;

import java.util.Collection;

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
public class NodesConnectTaskTest implements Runnable  {
    private ConnectionManager connectionManager = ConnectionManager.getInstance();
    private void connectPeer(NodeGroup nodeGroup){
        Node node=new Node("47.89.245.0",8008,Node.OUT,false);
        nodeGroup.addDisConnetNode(node,false);
        node.addGroupConnector(nodeGroup.getMagicNumber());
        connectionManager.connectionNode(node);
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
                if (nodeGroup.isInCrossMaxOutNumber()) {

                    connectPeer(nodeGroup);
                }



                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

