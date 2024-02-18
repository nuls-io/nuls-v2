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
import java.util.concurrent.*;

/**
 * Node Discovery Task
 *
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeDiscoverTask implements Runnable {

    /**
     * Node detection results -- Successful, able to connect
     */
    private final static int PROBE_STATUS_SUCCESS = 1;
    /**
     * Node detection results -- Failed, unable to connect, node unavailable
     */
    private final static int PROBE_STATUS_FAIL = 2;
    /**
     * Node detection results -- Neglecting, when the network is disconnected, that is, when none of the local nodes are connected, it is uncertain whether the other party cannot connect or the local network is unavailable. In this case, ignoring
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
                 * Local network and cross chain networking detection
                 */
                processNodes(nodeGroup.getLocalNetNodeContainer());
                processNodes(nodeGroup.getCrossNodeContainer());
                /**
                 * Local detection of available cross chain connections, successful sharing with cross chain groups
                 */
                processLocalCrossNodes(nodeGroup.getLocalShareToCrossUncheckNodes(), nodeGroup.getLocalShareToCrossCanConnectNodes());
                /**
                 *Failed node detection
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
                    LoggerUtil.logger(node.getNodeGroup().getChainId()).info("add cross node,remove from verifyNodes:{}", node.getId());
                    LoggerUtil.logger(node.getNodeGroup().getChainId()).info("share cross node={}", node.getId());
                    doShare(node, true);
                } else {
                    node.setStatus(NodeStatusEnum.UNAVAILABLE);
                    node.setConnectStatus(NodeConnectStatusEnum.FAIL);
                    node.setFailCount(node.getFailCount() + 1);
                    //Reset successful detection time
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
        int maxNodes = 20;
        int count = 0;
        List<Future<Node>> discoverList = new ArrayList<>();
        for (Map.Entry<String, Node> nodeEntry : verifyNodes.entrySet()) {
            Node node = nodeEntry.getValue();
            boolean needProbeNow = checkNeedProbeNow(node, verifyNodes);
            if (!needProbeNow) {
                continue;
            }
            if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTING) {
                LoggerUtil.COMMON_LOG.info("{} is in connecting", node.getId());
                continue;
            }
            count++;
            if (count < maxNodes) {
                Future<Node> res = ConnectionManager.getInstance().discover.submit(new Callable<Node>() {
                    @Override
                    public Node call() {
                        try {
                            int status = doProbe(node);
                            if (status == PROBE_STATUS_IGNORE) {
                                return node;
                            }
                            verifyNodes.remove(node.getId());
                            if (status == PROBE_STATUS_SUCCESS) {
                                node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
                                //Represents the number of times a connection has been broken, or it may be multiple times a connection has been disconnected during a handshake. Only those who truly shake hands can reset to0
                                node.setFailCount(node.getFailCount() + 1);
                                if (nodesContainer.hadInConnection(node.getIp())) {
                                    node.setStatus(NodeStatusEnum.AVAILABLE);
                                } else {
                                    node.setStatus(NodeStatusEnum.CONNECTABLE);
                                }
                                canConnectNodes.put(node.getId(), node);

                                if (!node.isHadShare()) {
                                    // The first detection is successful, and node information is only forwarded when the first detection is successful
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
                        } catch (Exception e) {
                            return node;
                        }
                        return node;
                    }
                });
                discoverList.add(res);
            } else {
                break;
            }
        }
        discoverList.forEach(n -> {
            try {
                //TODO pierre test
                LoggerUtil.logger(n.get().getNodeGroup().getChainId()).debug("discover node={},status={}", n.get().getId(), n.get().getStatus());
            } catch (InterruptedException e) {
                LoggerUtil.COMMON_LOG.error(e);
            } catch (ExecutionException e) {
                LoggerUtil.COMMON_LOG.error(e);
            }
        });

    }

    private boolean checkNeedProbeNow(Node node, Map<String, Node> verifyNodes) {
        // The detection interval time is determined based on the number of failures, and the number of failed detections isfailCount, detection interval isprobeIntervalThe definitions are as follows：
        // failCount : 0-10 ,probeInterval = 60s
        // failCount : 11-20 ,probeInterval = 300s
        // failCount : 21-30 ,probeInterval = 600s
        // When a node fails more than once30When, it will be removed from the node list and will be permanently discarded unless it receives another share from that node

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
        } else {
            verifyNodes.remove(node.getId());
            return false;
        }
        return (TimeManager.currentTimeMillis() - node.getLastProbeTime()) > probeInterval;
    }

    /*
     * Perform detection
     * @param int Detection results ： PROBE_STATUS_SUCCESS,success  PROBE_STATUS_FAIL,fail  PROBE_STATUS_IGNORE,skip（When the network is disconnected, that is, when none of the local nodes are connected, it is uncertain whether the other party cannot connect or the local network is unavailable. In this case, ignore）
     */
    private int doProbe(Node node) {

        if (node == null) {
            return PROBE_STATUS_FAIL;
        }

        CompletableFuture<Integer> future = new CompletableFuture<>();
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTING);
        node.setConnectedListener(() -> {
            //After detecting connectivity, disconnect
            LoggerUtil.logger(node.getNodeGroup().getChainId()).debug("verify node:{},connect success", node.getId());
            node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
            node.getChannel().close();
        });

        node.setDisconnectListener(() -> {
            LoggerUtil.logger(node.getNodeGroup().getChainId()).debug("verify node:{},disconnect,failCount={}", node.getId(), node.getFailCount());
            node.setChannel(null);
            int availableNodesCount = 0;
            if (node.isCrossConnect()) {
                availableNodesCount = node.getNodeGroup().getCrossNodeContainer().getConnectedNodes().size();
            } else {
                availableNodesCount = node.getNodeGroup().getLocalNetNodeContainer().getConnectedNodes().size();
            }
            if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTED) {
                //Detect connectable
                node.setConnectStatus(NodeConnectStatusEnum.DISCONNECT);
                future.complete(PROBE_STATUS_SUCCESS);
            } else if (availableNodesCount == 0) {
                //Possible network connectivity
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
     * Broadcast to other nodes after detecting as available nodes（It can be a cross chain node）
     *
     * @param node
     */
    private void doShare(Node node, boolean isLocalToCrossShare) {
        LoggerUtil.COMMON_LOG.info("doShare node={},isLocalToCrossShare={}", node.getId(), isLocalToCrossShare);
        if (node.isCrossConnect()) {
            //Cross chain nodes within the network group do not propagate, Local network sharing and dissemination to cross chain external networks
            if (isLocalToCrossShare) {
                NodeGroup nodeGroup = node.getNodeGroup();
                if (nodeGroup.isMoonGroup()) {
                    //Share with all external link connection points(Satellite chain)
                    List<NodeGroup> nodeGroupList1 = NodeGroupManager.getInstance().getNodeGroups();
                    for (NodeGroup nodeGroup1 : nodeGroupList1) {
                        if (nodeGroup1.getChainId() == nodeGroup.getChainId()) {
                            continue;
                        }
                        //Share with cross chain nodes,Cross chain nodeportby0
                        broadcastNewAddr(node.getIp(), 0, node.getRemoteCrossPort(), nodeGroup1.getMagicNumber(), nodeGroup1.getChainId(), true, true);
                    }
                } else {
                    //Share with cross chain nodes,Cross chain nodeportby0
                    broadcastNewAddr(node.getIp(), 0, node.getRemoteCrossPort(), node.getMagicNumber(), nodeGroup.getChainId(), true, true);
                }
            }
        } else {
            //Own network broadcasting
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
