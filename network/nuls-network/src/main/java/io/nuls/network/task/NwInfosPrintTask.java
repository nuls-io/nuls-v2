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
package io.nuls.network.task;

import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.ProtocolRoleHandler;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.utils.LoggerUtil;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Group event monitor
 * 测试 定时打印连接信息
 *
 * @author lan
 * @create 2018/11/14
 */
public class NwInfosPrintTask implements Runnable {
    @Override
    public void run() {
        //test
        LoggerUtil.NwInfosLog.info("");
        LoggerUtil.NwInfosLog.info("");
        LoggerUtil.NwInfosLog.info("BEGIN @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        printlnNwTime();
        LoggerUtil.NwInfosLog.info("");
        printlnPeer();
        LoggerUtil.NwInfosLog.info("");
        printlnMem();
        LoggerUtil.NwInfosLog.info("");
        printlnProtocolMap();
        LoggerUtil.NwInfosLog.info("");
        LoggerUtil.NwInfosLog.info("END @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    }
    private void printlnNwTime() {
        LoggerUtil.NwInfosLog.info("网络时间time = {},offset={}",TimeManager.currentTimeMillis(),TimeManager.netTimeOffset);
    }
    private void printlnProtocolMap() {
        Collection<Map<String, ProtocolRoleHandler>> values = MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap().values();
        LoggerUtil.NwInfosLog.info("protocolRoleHandler ==================");
        StringBuilder stringBuilder = new StringBuilder();
        for (Map<String, ProtocolRoleHandler> map : values) {
            Collection<ProtocolRoleHandler> list = map.values();
            for (ProtocolRoleHandler protocolRoleHandler : list) {
                stringBuilder.append("{role:").append(protocolRoleHandler.getRole()).append(",cmd:").append(protocolRoleHandler.getHandler()).append("}");

            }
        }
        LoggerUtil.NwInfosLog.info("protocolRoleHandler={}", stringBuilder.toString());

    }

    private void printlnMem() {
//       byte[] bys = new byte[1024*1024];//申请1M内存
        LoggerUtil.NwInfosLog.info("=================内存情况=============");
        LoggerUtil.NwInfosLog.info("Java进程可以向操作系统申请到的最大内存:"+(Runtime.getRuntime().maxMemory())/(1024*1024)+"M");
        LoggerUtil.NwInfosLog.info("Java进程空闲内存:"+(Runtime.getRuntime().freeMemory())/(1024*1024)+"M");
        LoggerUtil.NwInfosLog.info("Java进程现在从操作系统那里已经申请了内存:"+(Runtime.getRuntime().totalMemory())/(1024*1024)+"M");
    }

    private void printlnPeer() {
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
        for (NodeGroup nodeGroup : nodeGroupList) {
            NodesContainer localNodesContainer = nodeGroup.getLocalNetNodeContainer();
            NodesContainer crossNodesContainer = nodeGroup.getCrossNodeContainer();
            LoggerUtil.NwInfosLog.info("######################################################################");
            LoggerUtil.NwInfosLog.info("@@@@@@@@@@@ chainId={},magicNumber={},localNetStatus(本地网络)={},crossNetStatus(跨链)={}",
                    nodeGroup.getChainId(), nodeGroup.getMagicNumber(),nodeGroup.getLocalStatus(),nodeGroup.getCrossStatus());

            Collection<Node> c1 = localNodesContainer.getConnectedNodes().values();
            Collection<Node> c2 = localNodesContainer.getCanConnectNodes().values();
            Collection<Node> c3 = localNodesContainer.getDisconnectNodes().values();
            Collection<Node> c4 = localNodesContainer.getUncheckNodes().values();
            Collection<Node> c5 = localNodesContainer.getFailNodes().values();
            LoggerUtil.NwInfosLog.info("=================(自有网络)begin printlnPeer :SelfConnectNodes=============");
            LoggerUtil.NwInfosLog.info("*****(connected)已连接信息**********");
            for (Node n : c1) {
                LoggerUtil.NwInfosLog.info("**connected:{},info:blockHash={},blockHeight={},version={},connStatus={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion(),n.getConnectStatus());
            }
            LoggerUtil.NwInfosLog.info("*****(canConnect)可连接信息**********");
            for (Node n : c2) {
                LoggerUtil.NwInfosLog.info("**:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("*****(disConnect)断开连接信息**********");
            for (Node n : c3) {
                LoggerUtil.NwInfosLog.info("**{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("*****(uncheck)待检测连接信息**********");
            for (Node n : c4) {
                LoggerUtil.NwInfosLog.info("**{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("*****(failed)失败连接信息**********");
            for (Node n : c5) {
                LoggerUtil.NwInfosLog.info("**{},FailCount = {},info:blockHash={},blockHeight={},version={}", n.getId(), n.getFailCount(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("======================================================================");


            Collection<Node> d1 = crossNodesContainer.getConnectedNodes().values();
            Collection<Node> d2 = crossNodesContainer.getCanConnectNodes().values();
            Collection<Node> d3 = crossNodesContainer.getDisconnectNodes().values();
            Collection<Node> d4 = crossNodesContainer.getUncheckNodes().values();
            Collection<Node> d5 = crossNodesContainer.getFailNodes().values();
            LoggerUtil.NwInfosLog.info("=================(跨链网络)begin printlnPeer :CrossConnectNodes=============");
            LoggerUtil.NwInfosLog.info("*****(connected)已连接信息**********");
            for (Node n : d1) {
                LoggerUtil.NwInfosLog.info("**connected:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("*****(canConnect)可连接信息**********");
            for (Node n : d2) {
                LoggerUtil.NwInfosLog.info("*****canConnect:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("*****(disConnect)断开连接信息**********");
            for (Node n : d3) {
                LoggerUtil.NwInfosLog.info("*****disConnect:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("*****(uncheck)待检测连接信息**********");
            for (Node n : d4) {
                LoggerUtil.NwInfosLog.info("*****uncheck:{},info:blockHash={},blockHeight={},version={}", n.getId(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("*****(failed)失败连接信息**********");
            for (Node n : d5) {
                LoggerUtil.NwInfosLog.info("*****failed:{},FailCount = {},info:blockHash={},blockHeight={},version={}", n.getId(), n.getFailCount(), n.getBlockHash(), n.getBlockHeight(), n.getVersion());
            }
            LoggerUtil.NwInfosLog.info("@@@@@@@@@@@ @@@@@@@@@@@ @@@@@@@@@@@ @@@@@@@@@@@ end==============");
            LoggerUtil.NwInfosLog.info("#####################################################################");
            LoggerUtil.NwInfosLog.info("");
        }
    }
}
