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

import io.nuls.core.log.Log;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.constant.NodeStatusEnum;
import io.nuls.network.manager.*;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddressShare;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.utils.LoggerUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

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
        new Thread().start();
    }

    @Override
    public void run() {
        try {
            List<NodeGroup> list = NodeGroupManager.getInstance().getNodeGroups();
            Collections.shuffle(list);
            for (NodeGroup nodeGroup : list) {
                /**
                 * 本地网络与跨链组网探测
                 */
                processNodes(nodeGroup.getLocalNetNodeContainer());
                processNodes(nodeGroup.getCrossNodeContainer());
                /**
                 * 本地探测可用的跨链连接，成功则分享给跨链组
                 */
                processLocalCrossNodes(nodeGroup.getLocalShareToCrossUncheckNodes(), nodeGroup.getLocalShareToCrossCanConnectNodes());
                /**
                 *失败节点探测
                 */
                processFailNodes(nodeGroup.getLocalNetNodeContainer());
                processFailNodes(nodeGroup.getCrossNodeContainer());
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void processNodes(NodesContainer nodesContainer) {

        Map<String, Node> canConnectNodes = nodesContainer.getCanConnectNodes();

        Map<String, Node> uncheckNodes = nodesContainer.getUncheckNodes();
        Map<String, Node> disconnectNodes = nodesContainer.getDisconnectNodes();

        if (uncheckNodes.size() > 0) {
            probeNodes(uncheckNodes, canConnectNodes, nodesContainer);
        }

        if (disconnectNodes.size() > 0) {
            probeNodes(disconnectNodes, canConnectNodes, nodesContainer);
        }
    }

    private void processLocalCrossNodes(Map<String, Node> verifyNodes, Map<String, Node> canConnectNodes) {
        if (verifyNodes.size() > 0) {
            for (Map.Entry<String, Node> nodeEntry : verifyNodes.entrySet()) {
                Node node = nodeEntry.getValue();
                boolean needProbeNow = checkNeedProbeNow(node, verifyNodes);
                if (!needProbeNow) {
                    continue;
                }
                if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTING) {
                    continue;
                }
                int status = doProbe(node);
                if (status == PROBE_STATUS_IGNORE) {
                    continue;
                }
                if (status == PROBE_STATUS_SUCCESS) {
                    node.setStatus(NodeStatusEnum.CONNECTABLE);
                    canConnectNodes.put(node.getId(), node);
                    verifyNodes.remove(node.getId());
                    LoggerUtil.logger(node.getNodeGroup().getChainId()).info("增加可用跨链,移除探测信息:{}", node.getId());
                    LoggerUtil.logger(node.getNodeGroup().getChainId()).info("跨链连接{}探测可用，进行跨链分享", node.getId());
                    doShare(node, true);
                } else {
                    node.setStatus(NodeStatusEnum.UNAVAILABLE);
                    node.setConnectStatus(NodeConnectStatusEnum.FAIL);
                    node.setFailCount(node.getFailCount() + 1);
                    //重置成功探测时间
                }
            }
        }
    }

    private void processFailNodes() {
        try {
            List<NodeGroup> list = NodeGroupManager.getInstance().getNodeGroups();
            Collections.shuffle(list);
            for (NodeGroup nodeGroup : list) {
                processFailNodes(nodeGroup.getLocalNetNodeContainer());
                processFailNodes(nodeGroup.getCrossNodeContainer());
            }

        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void processFailNodes(NodesContainer nodesContainer) {
        try {
            Map<String, Node> canConnectNodes = nodesContainer.getCanConnectNodes();
            Map<String, Node> failNodes = nodesContainer.getFailNodes();
            if (failNodes.size() > 0) {
                probeNodes(failNodes, canConnectNodes, nodesContainer);
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void probeNodes(Map<String, Node> verifyNodes, Map<String, Node> canConnectNodes, NodesContainer nodesContainer) {
        for (Map.Entry<String, Node> nodeEntry : verifyNodes.entrySet()) {
            Node node = nodeEntry.getValue();
            boolean needProbeNow = checkNeedProbeNow(node, verifyNodes);
            if (!needProbeNow) {
                continue;
            }
            if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTING) {
                LoggerUtil.COMMON_LOG.info("{} is in connecting",node.getId());
                continue;
            }
            int status = doProbe(node);

            if (status == PROBE_STATUS_IGNORE) {
                continue;
            }
            verifyNodes.remove(node.getId());
            if (status == PROBE_STATUS_SUCCESS) {
                node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
                //代表断链次数，也可能是多次连接在握手时候断开。只有真正握手成功的才能重置为0
                node.setFailCount(node.getFailCount() + 1);
                if (nodesContainer.hadInConnection(node.getIp())) {
                    node.setStatus(NodeStatusEnum.AVAILABLE);
                } else {
                    node.setStatus(NodeStatusEnum.CONNECTABLE);
                }
                canConnectNodes.put(node.getId(), node);

                if (!node.isHadShare()) {
                    // 第一次探测且成功，只有在第一次探测成功时情况，才转发节点信息
                    doShare(node, false);
                    node.setHadShare(true);
                }
            } else if (status == PROBE_STATUS_FAIL) {
                ConnectionManager.getInstance().nodeConnectFail(node);
                if (node.isCrossConnect()) {
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
        if (failCount == 0) {
            probeInterval = 0;
        } else if (failCount <= 10) {
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
    private int doProbe(Node node) {

        if (node == null) {
            return PROBE_STATUS_FAIL;
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTING);
        node.setConnectedListener(() -> {
            //探测可连接后，断开连接
            LoggerUtil.logger(node.getNodeGroup().getChainId()).debug("探测可连接:{},之后自动断开", node.getId());
            node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
            node.getChannel().close();
        });

        node.setDisconnectListener(() -> {
            LoggerUtil.logger(node.getNodeGroup().getChainId()).debug("探测进入断开:{},failCount={}", node.getId(), node.getFailCount());
            node.setChannel(null);
            int availableNodesCount = 0;
            if (node.isCrossConnect()) {
                availableNodesCount = node.getNodeGroup().getCrossNodeContainer().getConnectedNodes().size();
            } else {
                availableNodesCount = node.getNodeGroup().getLocalNetNodeContainer().getConnectedNodes().size();
            }
            if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTED) {
                //探测可连接
                node.setConnectStatus(NodeConnectStatusEnum.DISCONNECT);
                future.complete(PROBE_STATUS_SUCCESS);
            } else if (availableNodesCount == 0) {
                //可能网络不通
                node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
                future.complete(PROBE_STATUS_IGNORE);
            } else {
                node.setConnectStatus(NodeConnectStatusEnum.FAIL);
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
            LoggerUtil.logger(node.getNodeGroup().getChainId()).error(e);
            return PROBE_STATUS_IGNORE;
        }
    }

    /**
     * 探测为可用的节点后广播给其他节点（可以是跨链节点）
     *
     * @param node
     */
    private void doShare(Node node, boolean isLocalToCrossShare) {
        if (node.isCrossConnect()) {
            //网络组内跨链节点不传播, 本地网分享传播给跨链外网
            if (isLocalToCrossShare) {
                NodeGroup nodeGroup = node.getNodeGroup();
                if (nodeGroup.isMoonGroup()) {
                    //分享给所有外链连接点(卫星链)
                    List<NodeGroup> nodeGroupList1 = NodeGroupManager.getInstance().getNodeGroups();
                    for (NodeGroup nodeGroup1 : nodeGroupList1) {
                        if (nodeGroup1.getChainId() == nodeGroup.getChainId()) {
                            continue;
                        }
                        //分享给跨链节点,跨链节点的port为0
                        broadcastNewAddr(node.getIp(), 0, node.getRemoteCrossPort(), nodeGroup1.getMagicNumber(), nodeGroup1.getChainId(), true, true);
                    }
                } else {
                    //分享给跨链节点,跨链节点的port为0
                    broadcastNewAddr(node.getIp(), 0, node.getRemoteCrossPort(), node.getMagicNumber(), nodeGroup.getChainId(), true, true);
                }
            }
        } else {
            //自有网络广播
            broadcastNewAddr(node.getIp(), node.getRemotePort(), node.getRemoteCrossPort(), node.getMagicNumber(), node.getNodeGroup().getChainId(),
                    false, false);
        }
    }

    private void broadcastNewAddr(String ip, int port, int crossPort, long magicNumber, int chainId, boolean isCross, boolean isCrossAddress) {
        IpAddressShare ipAddress = new IpAddressShare(ip, port, crossPort);
        List<IpAddressShare> list = new ArrayList<>();
        list.add(ipAddress);
        AddrMessage addrMessage = MessageFactory.getInstance().buildAddrMessage(list, magicNumber, chainId, isCrossAddress ? (byte) 1 : (byte) 0);
        MessageManager.getInstance().broadcastNewAddr(addrMessage, null, isCross, true);
    }
}
