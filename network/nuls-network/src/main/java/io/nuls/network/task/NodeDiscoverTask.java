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

import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.constant.NodeStatusEnum;
import io.nuls.network.manager.*;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.netty.container.NodesContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * 节点发现任务
 *
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeDiscoverTask implements Runnable {

    /**
     * 节点探测结果 -- 成功，能连接
     */
    private final static int PROBE_STATUS_SUCCESS = 1;
    /**
     * 节点探测结果 -- 失败，不能连接，节点不可用
     */
    private final static int PROBE_STATUS_FAIL = 2;
    /**
     * 节点探测结果 -- 忽略，当断网时，也就是本地节点一个都没有连接时，不确定是对方连不上，还是本地没网，这时忽略
     */
    private final static int PROBE_STATUS_IGNORE = 3;

    private final ConnectionManager connectionManager = ConnectionManager.getInstance();

    public NodeDiscoverTask() {
        new Thread(() -> processFailNodes()).start();
    }

    @Override
    public void run() {
        try {
            List<NodeGroup> list = NodeGroupManager.getInstance().getNodeGroups();
            Collections.shuffle(list);
            for (NodeGroup nodeGroup : list) {
                processNodes(nodeGroup.getLocalNetNodeContainer(), false);
                processNodes(nodeGroup.getCrossNodeContainer(), true);
            }

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void processNodes(NodesContainer nodesContainer, boolean isCross) {

        Map<String, Node> canConnectNodes = nodesContainer.getCanConnectNodes();

        Map<String, Node> uncheckNodes = nodesContainer.getUncheckNodes();
        Map<String, Node> disconnectNodes = nodesContainer.getDisconnectNodes();

        if (uncheckNodes.size() > 0) {
            probeNodes(uncheckNodes, canConnectNodes, nodesContainer, isCross);
        }

        if (disconnectNodes.size() > 0) {
            probeNodes(disconnectNodes, canConnectNodes, nodesContainer, isCross);
        }
    }

    private void processFailNodes() {
        try {
            List<NodeGroup> list = NodeGroupManager.getInstance().getNodeGroups();
            Collections.shuffle(list);
            for (NodeGroup nodeGroup : list) {
                processFailNodes(nodeGroup.getLocalNetNodeContainer(), false);
                processFailNodes(nodeGroup.getCrossNodeContainer(), true);
            }

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void processFailNodes(NodesContainer nodesContainer, boolean isCross) {
        try {
            Map<String, Node> canConnectNodes = nodesContainer.getCanConnectNodes();
            while (true) {
                Map<String, Node> failNodes = nodesContainer.getFailNodes();
                if (failNodes.size() > 0) {
                    probeNodes(failNodes, canConnectNodes, nodesContainer, isCross);
                }
                Thread.sleep(3000L);
            }
        } catch (Exception e) {
            Log.error(e);
            try {
                Thread.sleep(3000L);
            } catch (InterruptedException e1) {
                Log.error(e1);
            }
        }
    }

    private void probeNodes(Map<String, Node> verifyNodes, Map<String, Node> canConnectNodes, NodesContainer nodesContainer, boolean isCross) {
        for (Map.Entry<String, Node> nodeEntry : verifyNodes.entrySet()) {
            Node node = nodeEntry.getValue();
            boolean needProbeNow = checkNeedProbeNow(node, verifyNodes);
            if (!needProbeNow) {
                continue;
            }
            int status = doProbe(node, isCross);

            if (status == PROBE_STATUS_IGNORE/* && !node.isSeedNode()*/) {
                continue;
            }

            verifyNodes.remove(node.getId());
            if (status == PROBE_STATUS_SUCCESS) {
                node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
                node.setFailCount(0);
                if (nodesContainer.hadInConnection(node.getIp())) {
                    node.setStatus(NodeStatusEnum.AVAILABLE);
                } else {
                    node.setStatus(NodeStatusEnum.CONNECTABLE);
                }
                canConnectNodes.put(node.getId(), node);

                if (node.getLastProbeTime() == 0L) {
                    // 当lastProbeTime为0时，代表第一次探测且成功，只有在第一次探测成功时情况，才转发节点信息
                    doShare(node, isCross);
                }
            } else if (status == PROBE_STATUS_FAIL) {
                ConnectionManager.getInstance().nodeConnectFail(node);
                if (isCross) {
                    node.getNodeGroup().getCrossNodeContainer().getFailNodes().put(node.getId(), node);
                } else {
                    node.getNodeGroup().getLocalNetNodeContainer().getFailNodes().put(node.getId(), node);
                }
            }
            node.setLastProbeTime(TimeManager.currentTimeMillis());
        }
    }

    private boolean checkNeedProbeNow(Node node, Map<String, Node> verifyNodes) {
        // 探测间隔时间，根据失败的次数来决定，探测失败次数为failCount，探测间隔为probeInterval，定义分别如下：
        // failCount : 0-10 ，probeInterval = 60s
        // failCount : 11-20 ，probeInterval = 300s
        // failCount : 21-30 ，probeInterval = 600s
        // failCount : 31-50 ，probeInterval = 1800s
        // failCount : 51-100 ，probeInterval = 3600s
        // 当一个节点失败次数大于100时，将从节点列表中移除，除非再次收到该节点的分享，否则永远丢弃该节点

        long probeInterval;
        int failCount = node.getFailCount();

        if (failCount <= 10) {
            probeInterval = 60 * 1000L;
        } else if (failCount <= 20) {
            probeInterval = 300 * 1000L;
        } else if (failCount <= 30) {
            probeInterval = 600 * 1000L;
        } else if (failCount <= 50) {
            probeInterval = 1800 * 1000L;
        } else if (failCount <= 100) {
            probeInterval = 3600 * 1000L;
        } else {
            verifyNodes.remove(node.getId());
            return false;
        }

        return (TimeManager.currentTimeMillis() - node.getLastProbeTime()) > probeInterval;
    }

    /*
     * 执行探测
     * @param int 探测结果 ： PROBE_STATUS_SUCCESS,成功  PROBE_STATUS_FAIL,失败  PROBE_STATUS_IGNORE,跳过（当断网时，也就是本地节点一个都没有连接时，不确定是对方连不上，还是本地没网，这时忽略）
     */
    private int doProbe(Node node, boolean isCross) {

        if (node == null) {
            return PROBE_STATUS_FAIL;
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();

        node.setConnectedListener(() -> {
            node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
            node.getChannel().close();
        });

        node.setDisconnectListener(() -> {
            node.setChannel(null);
            int availableNodesCount = 0;
            if (isCross) {
                availableNodesCount = node.getNodeGroup().getCrossNodeContainer().getConnectedNodes().size();
            } else {
                availableNodesCount = node.getNodeGroup().getLocalNetNodeContainer().getConnectedNodes().size();
            }
            if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTED) {
                future.complete(PROBE_STATUS_SUCCESS);
            } else if (availableNodesCount == 0) {
                future.complete(PROBE_STATUS_IGNORE);
            } else {
                future.complete(PROBE_STATUS_FAIL);
            }
        });

        boolean result = connectionManager.connection(node);
        if (!result) {
            return PROBE_STATUS_FAIL;
        }
        try {
            return future.get();
        } catch (Exception e) {
            Log.error(e);
            return PROBE_STATUS_IGNORE;
        }
    }

    private void doShare(Node node, boolean isCross) {
        IpAddress ipAddress = new IpAddress(node.getIp(), node.getRemotePort());
        List<IpAddress> list = new ArrayList<>();
        list.add(ipAddress);
        AddrMessage addrMessage = MessageFactory.getInstance().buildAddrMessage(list, node.getMagicNumber());
        MessageManager.getInstance().broadcastToAllNode(addrMessage, null, isCross, true);
    }
}
