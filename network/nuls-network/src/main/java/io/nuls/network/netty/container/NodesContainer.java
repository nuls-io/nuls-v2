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

package io.nuls.network.netty.container;

import io.nuls.network.constant.NodeStatusEnum;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.po.NodePo;
import io.nuls.network.model.po.NodesContainerPo;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodesContainer implements Serializable {

    private Map<String, Node> canConnectNodes = new ConcurrentHashMap<>();
    private Map<String, Node> connectedNodes = new ConcurrentHashMap<>();
    private Map<String, Node> disconnectNodes = new ConcurrentHashMap<>();
    private Map<String, Node> failNodes = new ConcurrentHashMap<>();
    private Map<String, Node> uncheckNodes = new ConcurrentHashMap<>();
    /**
     * 记录作为server的连接记录
     */
    private Map<String, Integer> inNodes = new ConcurrentHashMap<>();
    /**
     * 最近一次握手成功时间
     */
    private volatile long latestHandshakeSuccTime = 0;
    private volatile boolean hadShareAddr = false;
    private volatile int status = NodeGroup.WAIT1;


    public NodesContainer() {

    }

    public int getConnectPeerNum() {
        return connectedNodes.size();
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getLatestHandshakeSuccTime() {
        return latestHandshakeSuccTime;
    }

    public void setLatestHandshakeSuccTime(long latestHandshakeSuccTime) {
        this.latestHandshakeSuccTime = latestHandshakeSuccTime;
    }

    public boolean isHadShareAddr() {
        return hadShareAddr;
    }

    public void setHadShareAddr(boolean hadShareAddr) {
        this.hadShareAddr = hadShareAddr;
    }

    public NodesContainerPo parseToNodesContainerPo() {
        NodesContainerPo containerPo = new NodesContainerPo();
        for (Node node : disconnectNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getDisConnectNodes().add((NodePo) node.parseToPo());
            }
        }
        for (Node node : canConnectNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getCanConnectNodes().add((NodePo) node.parseToPo());
            }
        }
        for (Node node : failNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getFailNodes().add((NodePo) node.parseToPo());
            }
        }
        for (Node node : uncheckNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getUncheckNodes().add((NodePo) node.parseToPo());
            }
        }
        for (Node node : connectedNodes.values()) {
            if (node.getType() == Node.OUT) {
                containerPo.getCanConnectNodes().add((NodePo) node.parseToPo());
            }
        }
        return containerPo;
    }

    public NodesContainer(NodesContainerPo containerPo) {
        Node node = null;
        if (containerPo.getDisConnectNodes() != null) {
            for (NodePo nodePo : containerPo.getDisConnectNodes()) {
                node = (Node) nodePo.parseDto();
                disconnectNodes.put(node.getId(), node);
            }
        }
        if (containerPo.getUncheckNodes() != null) {
            for (NodePo nodePo : containerPo.getUncheckNodes()) {
                node = (Node) nodePo.parseDto();
                uncheckNodes.put(node.getId(), node);
            }
        }
        if (containerPo.getFailNodes() != null) {
            for (NodePo nodePo : containerPo.getFailNodes()) {
                node = (Node) nodePo.parseDto();
                failNodes.put(node.getId(), node);
            }
        }
        if (containerPo.getCanConnectNodes() != null) {
            for (NodePo nodePo : containerPo.getCanConnectNodes()) {
                node = (Node) nodePo.parseDto();
                canConnectNodes.put(node.getId(), node);
            }
        }
    }

    public boolean markCanuseNodeByIp(String ip, int type) {
        if (type == NodeStatusEnum.AVAILABLE) {
            inNodes.put(ip, 1);
        } else {
            inNodes.remove(ip);
        }
        return (markCanuseNodeByIp(ip, type, canConnectNodes));
    }

    public boolean markCanuseNodeByIp(String ip, int type, Map<String, Node> nodes) {
        if (ip == null) {
            return false;
        }
        Iterator<Node> it = nodes.values().iterator();
        while (it.hasNext()) {
            Node node = it.next();
            if (ip.equals(node.getIp())) {
                node.setStatus(type);
                return true;
            }
        }
        return false;
    }

    public boolean addNeedCheckNode(Node newNode) {

        String nodeId = newNode.getId();

        Node node = uncheckNodes.get(nodeId);
        if (node != null) {
            return false;
        }

        node = canConnectNodes.get(nodeId);
        if (node != null) {
            return false;
        }

        node = connectedNodes.get(nodeId);
        if (node != null) {
            return false;
        }

        node = disconnectNodes.get(nodeId);
        if (node != null) {
            return false;
        }

        node = failNodes.get(nodeId);
        if (node != null) {
            return false;
        }

        newNode.setLastProbeTime(0L);
        uncheckNodes.put(nodeId, newNode);
        newNode.setStatus(NodeStatusEnum.UNCHECK);

        return true;
    }

    public int getConnectedCount(int type) {
        int size = 0;

        for (Node node : connectedNodes.values()) {
            if (node.getType() == type) {
                size++;
            }
        }

        return size;
    }

    public void clear() {
        this.uncheckNodes.clear();
        this.canConnectNodes.clear();
        this.disconnectNodes.clear();
        this.failNodes.clear();
        this.connectedNodes.clear();
    }

    public boolean hadInConnection(String ip) {
        return (null != inNodes.get(ip));
    }

    public Node getNode(String nodeId) {
        return connectedNodes.get(nodeId);
    }

    public Map<String, Node> getCanConnectNodes() {
        return canConnectNodes;
    }

    public void setCanConnectNodes(Map<String, Node> canConnectNodes) {
        this.canConnectNodes = canConnectNodes;
    }

    public Map<String, Node> getConnectedNodes() {
        return connectedNodes;
    }

    public void setConnectedNodes(Map<String, Node> connectedNodes) {
        this.connectedNodes = connectedNodes;
    }

    public Map<String, Node> getDisconnectNodes() {
        return disconnectNodes;
    }

    public void setDisconnectNodes(Map<String, Node> disconnectNodes) {
        this.disconnectNodes = disconnectNodes;
    }

    public Map<String, Node> getFailNodes() {
        return failNodes;
    }

    public void setFailNodes(Map<String, Node> failNodes) {
        this.failNodes = failNodes;
    }

    public Map<String, Node> getUncheckNodes() {
        return uncheckNodes;
    }

    public void setUncheckNodes(Map<String, Node> uncheckNodes) {
        this.uncheckNodes = uncheckNodes;
    }


}
