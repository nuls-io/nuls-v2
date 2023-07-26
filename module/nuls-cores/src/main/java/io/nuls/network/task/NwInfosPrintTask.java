/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.network.cfg.NetworkConfig;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.utils.LoggerUtil;

import java.util.Collection;
import java.util.List;

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
        printlnPeer();
    }

    private void printlnPeer() {
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
        NetworkConfig networkConfig = SpringLiteContext.getBean(NetworkConfig.class);
        if (networkConfig.isMoonNode()) {
            for (NodeGroup nodeGroup : nodeGroupList) {
                if (nodeGroup.isMoonCrossGroup()) {
                    printCross(nodeGroup);
                } else {
                    printLocalNet(nodeGroup);
                }
            }
        } else {
            for (NodeGroup nodeGroup : nodeGroupList) {
                printLocalNet(nodeGroup);
                printCross(nodeGroup);
            }
        }

    }

    private void printCross(NodeGroup nodeGroup) {
        NodesContainer crossNodesContainer = nodeGroup.getCrossNodeContainer();
        Collection<Node> d1 = crossNodesContainer.getConnectedNodes().values();
        Collection<Node> d2 = crossNodesContainer.getCanConnectNodes().values();
        Collection<Node> d3 = crossNodesContainer.getDisconnectNodes().values();
        Collection<Node> d4 = crossNodesContainer.getUncheckNodes().values();
        Collection<Node> d5 = crossNodesContainer.getFailNodes().values();
        LoggerUtil.logger(nodeGroup.getChainId()).info("");
        LoggerUtil.logger(nodeGroup.getChainId()).info("BEGIN @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        LoggerUtil.logger(nodeGroup.getChainId()).info("(CrossNet)begin printlnPeer :CrossConnectNodes-NetTime = {},offset={}", TimeManager.currentTimeMillis(), TimeManager.netTimeOffset);
        StringBuilder sb1 = new StringBuilder();
        sb1.append("\n@@@@@@@@@@@CrossNet chainId=").append(nodeGroup.getChainId()).append(",magicNumber=").append(nodeGroup.getMagicNumber()).append(",crossNetStatus(CrossNet)=").append(nodeGroup.getCrossStatus());
        sb1.append("\n*****(connected)******************************\n");
        for (Node n : d1) {
            sb1.append("(connected):").append(n.getId()).append(",channelId=")
                    .append(n.getChannel().id().asShortText()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        sb1.append("\n*****(canConnect)*******************************\n");
        for (Node n : d2) {
            sb1.append("(canConnect):").append(n.getId()).append(",tryCount=")
                    .append(n.getFailCount()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        sb1.append("\n*****(disConnect)*****************************\n");
        for (Node n : d3) {
            sb1.append("(disConnect):").append(n.getId()).append(",failCount=")
                    .append(n.getFailCount()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        sb1.append("\n*****(uncheck)******************************\n");
        for (Node n : d4) {
            sb1.append("(uncheck):").append(n.getId()).append(",failCount=")
                    .append(n.getFailCount()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        sb1.append("\n*****(failed)**********************************\n");
        for (Node n : d5) {
            sb1.append("(failed):").append(n.getId()).append(",failCount=")
                    .append(n.getFailCount()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        LoggerUtil.logger(nodeGroup.getChainId()).info(sb1.toString());
        LoggerUtil.logger(nodeGroup.getChainId()).info("END @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        LoggerUtil.logger(nodeGroup.getChainId()).info("");
    }

    private void printLocalNet(NodeGroup nodeGroup) {
        NodesContainer localNodesContainer = nodeGroup.getLocalNetNodeContainer();
        Collection<Node> c1 = localNodesContainer.getConnectedNodes().values();
        Collection<Node> c2 = localNodesContainer.getCanConnectNodes().values();
        Collection<Node> c3 = localNodesContainer.getDisconnectNodes().values();
        Collection<Node> c4 = localNodesContainer.getUncheckNodes().values();
        Collection<Node> c5 = localNodesContainer.getFailNodes().values();
        LoggerUtil.logger(nodeGroup.getChainId()).info("");
        LoggerUtil.logger(nodeGroup.getChainId()).info("BEGIN @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        LoggerUtil.logger(nodeGroup.getChainId()).info("(LocalNet)begin printlnPeer :SelfConnectNodes-NetTime = {},offset={}", TimeManager.currentTimeMillis(), TimeManager.netTimeOffset);
        StringBuilder sb1 = new StringBuilder();
        sb1.append("\n@@@@@@@@@@@ LocalNet chainId=").append(nodeGroup.getChainId()).append(",magicNumber=").append(nodeGroup.getMagicNumber()).append(",localNetStatus(LocalNet)=").append(nodeGroup.getLocalStatus());
        sb1.append("\n*****(connected)******************************\n");
        for (Node n : c1) {
            sb1.append("(connected):").append(n.getId()).append(",blockHash=").append(n.getBlockHash()).append(",blockHeight=").append(n.getBlockHeight()).append(",channelId=")
                    .append(n.getChannel().id().asShortText()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        sb1.append("\n*****(canConnect)******************************\n");
        for (Node n : c2) {
            sb1.append("(canConnect):").append(n.getId()).append(",tryCount=")
                    .append(n.getFailCount()).append(",crossPort=").append(n.getRemoteCrossPort()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        sb1.append("\n*****(disConnect)******************************\n");
        for (Node n : c3) {
            sb1.append("(disConnect):").append(n.getId()).append(",tryCount=")
                    .append(n.getFailCount()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        sb1.append("\n*****(uncheck)******************************\n");
        for (Node n : c4) {
            sb1.append("(uncheck):").append(n.getId()).append(",failCount=")
                    .append(n.getFailCount()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        sb1.append("\n*****(failed)******************************\n");
        for (Node n : c5) {
            sb1.append("(failed):").append(n.getId()).append(",failCount=")
                    .append(n.getFailCount()).append(",connStatus=").append(n.getConnectStatus()).append("\n");
        }
        LoggerUtil.logger(nodeGroup.getChainId()).info(sb1.toString());
        LoggerUtil.logger(nodeGroup.getChainId()).info("END @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        LoggerUtil.logger(nodeGroup.getChainId()).info("");
    }
}
