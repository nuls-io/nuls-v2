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

import io.nuls.network.cfg.NetworkConfig;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.utils.IpUtil;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 节点维护任务
 *
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeMaintenanceTask implements Runnable {

    private final NetworkConfig networkConfig = SpringLiteContext.getBean(NetworkConfig.class);
    private final ConnectionManager connectionManager = ConnectionManager.getInstance();

    @Override
    public void run() {
        try {
            List<NodeGroup> list = NodeGroupManager.getInstance().getNodeGroups();
            Collections.shuffle(list);
            for (NodeGroup nodeGroup : list) {
                process(nodeGroup, false);
                process(nodeGroup, true);
            }

        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
    }

    private void process(NodeGroup nodeGroup, boolean isCross) {
        List<Node> needConnectNodes = getNeedConnectNodes(nodeGroup, isCross);
        if (needConnectNodes == null || needConnectNodes.size() == 0) {
            return;
        }
        needConnectNodes.forEach(n -> LoggerUtil.logger(nodeGroup.getChainId()).debug("尝试连接:chainId={},isCross={},node={}", nodeGroup.getChainId(), isCross, n.getId()));
        for (Node node : needConnectNodes) {
            node.setType(Node.OUT);
            connectionNode(node);
        }
    }

    private boolean connectionNode(Node node) {
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTING);

        node.setRegisterListener(() -> LoggerUtil.logger().debug("new node {} Register!", node.getId()));

        node.setConnectedListener(() -> {
            LoggerUtil.logger().debug("主动连接成功:{}", node.getId());
            connectionManager.nodeClientConnectSuccess(node);
        });

        node.setDisconnectListener(() -> {
            LoggerUtil.logger().debug("主动连接断开:{}", node.getId());
            connectionManager.nodeConnectDisconnect(node);
        });
        return connectionManager.connection(node);
    }

    private List<Node> getNeedConnectNodes(NodeGroup nodeGroup, boolean isCross) {
        Collection<Node> connectedNodes = nodeGroup.getConnectedNodes(isCross);
        int maxOutCount = isCross ? networkConfig.getCrossMaxOutCount() : networkConfig.getMaxOutCount();
        if (connectedNodes.size() >= maxOutCount) {
            //进行种子节点的断链
            nodeGroup.stopConnectedSeeds(isCross);
            return null;
        }
        Collection<Node> canConnectNodes = nodeGroup.getCanConnectNodes(isCross);
        if (canConnectNodes.size() == 0) {
            return null;
        }
        List<Node> nodeList = new ArrayList<>(canConnectNodes);
        nodeList.removeAll(connectedNodes);
        for (Node node : nodeList) {
            if (IpUtil.isSelf(node.getIp())) {
                nodeList.remove(node);
                LoggerUtil.logger(nodeGroup.getChainId()).info("move self Address={}",node.getId());
                if (isCross) {
                    nodeGroup.getCrossNodeContainer().getCanConnectNodes().remove(node.getId());
                    break;
                } else {
                    nodeGroup.getLocalNetNodeContainer().getCanConnectNodes().remove(node.getId());
                    break;
                }
            }
        }
        //最大需要连接的数量 大于 可用连接数的时候，直接返回可用连接数，否则进行选择性返回
        int maxCount = maxOutCount - connectedNodes.size();
        if (nodeList.size() < maxCount) {
            return nodeList;
        }
        Collections.shuffle(nodeList);
        return nodeList.subList(0, maxCount);
    }

}
