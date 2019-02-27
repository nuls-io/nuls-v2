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

import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.network.netty.container.NodesContainer;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * Group event monitor
 * 测试 定时打印连接信息
 *
 * @author lan
 * @create 2018/11/14
 */
public class DataShowMonitorTest implements Runnable {
    @Override
    public void run() {
        //test
        printlnPeer();
        printlnMem();
        printlnProtocolMap();
    }

    private void printlnProtocolMap() {
        Collection<Map<String, ProtocolRoleHandler>> values = MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap().values();
        for (Map<String, ProtocolRoleHandler> map : values) {
            Collection<ProtocolRoleHandler> list = map.values();
            for (ProtocolRoleHandler protocolRoleHandler : list) {
                Log.debug("protocolRoleHandler =================={}==={}", protocolRoleHandler.getRole(), protocolRoleHandler.getHandler());
            }
        }

    }

    private void printlnMem() {
//       byte[] bys = new byte[1024*1024];//申请1M内存
//       Log.debug("Java进程可以向操作系统申请到的最大内存:"+(Runtime.getRuntime().maxMemory())/(1024*1024)+"M");
//       Log.debug("Java进程空闲内存:"+(Runtime.getRuntime().freeMemory())/(1024*1024)+"M");
//       Log.debug("Java进程现在从操作系统那里已经申请了内存:"+(Runtime.getRuntime().totalMemory())/(1024*1024)+"M");
    }

    private void printlnPeer() {

        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
        for (NodeGroup nodeGroup : nodeGroupList) {
            Log.info("chainId={},magicNumber={},isLocalChain={}", nodeGroup.getChainId(), nodeGroup.getMagicNumber(), !(nodeGroup.isMoonCrossGroup()));
            NodesContainer localNodesContainer = nodeGroup.getLocalNetNodeContainer();
            Collection<Node> c1 = localNodesContainer.getConnectedNodes().values();
            Collection<Node> c2 = localNodesContainer.getCanConnectNodes().values();
            Collection<Node> c3 = localNodesContainer.getDisconnectNodes().values();
            Collection<Node> c4 = localNodesContainer.getUncheckNodes().values();
            Collection<Node> c5 = localNodesContainer.getFailNodes().values();
            Log.info("begin============================printlnPeer :SelfConnectNodes=============");
            for (Node n : c1) {
                Log.info("*****connected:{},info:blockHash={},blockHeight={},version={},connStatus={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion(),n.getConnectStatus());
            }
            for (Node n : c2) {
                Log.info("*****canConnect:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            for (Node n : c3) {
                Log.info("*****disConnect:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            for (Node n : c4) {
                Log.info("*****uncheck:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            for (Node n : c5) {
                Log.info("*****failed:{},FailCount = {},info:blockHash={},blockHeight={},version={}", n.getId(), n.getFailCount(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            Log.info("end============================printlnPeer :SelfConnectNodes=============");

            NodesContainer crossNodesContainer = nodeGroup.getCrossNodeContainer();
            Collection<Node> d1 = crossNodesContainer.getConnectedNodes().values();
            Collection<Node> d2 = crossNodesContainer.getCanConnectNodes().values();
            Collection<Node> d3 = crossNodesContainer.getDisconnectNodes().values();
            Collection<Node> d4 = crossNodesContainer.getUncheckNodes().values();
            Collection<Node> d5 = crossNodesContainer.getFailNodes().values();
            Log.info("begin============================printlnPeer :crossConnectNodes=============");
            for (Node n : d1) {
                Log.info("*****connected:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            for (Node n : d2) {
                Log.info("*****canConnect:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            for (Node n : d3) {
                Log.info("*****disConnect:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            for (Node n : d4) {
                Log.info("*****uncheck:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            for (Node n : d5) {
                Log.info("*****failed:{},FailCount = {},info:blockHash={},blockHeight={},version={}", n.getId(), n.getFailCount(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            Log.info("end============================printlnPeer :crossDisConnectNodes=============");
        }
    }
}
