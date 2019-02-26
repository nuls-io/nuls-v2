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
package io.nuls.network.manager.threads;

import io.nuls.network.constant.NetworkParam;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * 节点维护任务
 *
 * @author: ln
 * @date: 2018/12/8
 */
public class NodeMaintenanceTask implements Runnable {

    private final NetworkParam networkParam = NetworkParam.getInstance();
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
            Log.error(e);
        }
    }

    private void process(NodeGroup nodeGroup, boolean isCross) {
        List<Node> needConnectNodes = getNeedConnectNodes(nodeGroup, isCross);
        if (needConnectNodes == null || needConnectNodes.size() == 0) {
            return;
        }

        for (Node node : needConnectNodes) {
            node.setType(Node.OUT);
            connectionNode(node);
        }
    }

    private boolean connectionNode(Node node) {
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTING);

        node.setRegisterListener(() -> Log.debug("new node {} try connecting!", node.getId()));

        node.setConnectedListener(() -> connectionManager.nodeClientConnectSuccess(node));

        node.setDisconnectListener(() -> {
            Log.info("-----------out node disconnect:" + node.getId());
            connectionManager.nodeConnectDisconnect(node);
        });
        return connectionManager.connection(node);
    }

    private List<Node> getNeedConnectNodes(NodeGroup nodeGroup, boolean isCross) {
        Collection<Node> connectedNodes = nodeGroup.getConnectedNodes(isCross);
        if (connectedNodes.size() >= networkParam.getMaxOutCount()) {
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

        int maxCount = networkParam.getMaxOutCount() - connectedNodes.size();
        if (nodeList.size() < maxCount) {
            return nodeList;
        }

        Collections.shuffle(nodeList);

        return nodeList.subList(0, maxCount);
    }
}
