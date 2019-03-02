/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.network.model;

import io.nuls.network.constant.NetworkParam;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.constant.NodeStatusEnum;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.dto.Dto;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.po.*;
import io.nuls.network.netty.container.NodesContainer;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 节点组对象
 *
 * @author lan
 * @date 2018/11/01
 */
public class NodeGroup implements Dto {
    private long magicNumber;
    private int chainId;
    private int maxOut;
    private int maxIn;
    /**
     * 跨链最大连接数
     */
    private int maxCrossOut = 0;
    private int maxCrossIn = 0;

    private int minAvailableCount;

    /**
     * 跨链网络是否激活,卫星链上的默认跨链true,
     * 友链默认false，在跨链模块请求时候这个属性才为true
     */
    private boolean isCrossActive = false;


    /**
     * localNet-自有网络的节点
     */
    private NodesContainer localNetNodeContainer = new NodesContainer();

    /**
     * 跨链-跨链连接的节点
     */
    private NodesContainer crossNodeContainer = new NodesContainer();


    private Lock locker = new ReentrantLock();
    /**
     * GROUP  STATUS
     * INITIALIZED 状态，等待连接中
     * 初始创建时候不可用状态是WAIT1
     * 到达OK后震荡不可用状态是WAIT2
     */
    public final static int WAIT1 = 1;
    public final static int WAIT2 = 2;
    public final static int OK = 3;
    private final static int DESTROY = -1;
    private final static int RECONNECT = -2;

    public NodeGroup(long magicNumber, int chainId, int maxIn, int maxOut, int minAvailableCount) {
        this.magicNumber = magicNumber;
        this.chainId = chainId;
        this.maxIn = maxIn;
        this.maxOut = maxOut;
        this.minAvailableCount = minAvailableCount;
        if (NetworkParam.getInstance().isMoonNode()) {
            isCrossActive = true;
        }
    }

    public NodesContainer getLocalNetNodeContainer() {
        return localNetNodeContainer;
    }

    public void setLocalNetNodeContainer(NodesContainer localNetNodeContainer) {
        this.localNetNodeContainer = localNetNodeContainer;
    }

    public NodesContainer getCrossNodeContainer() {
        return crossNodeContainer;
    }

    public void setCrossNodeContainer(NodesContainer crossNodeContainer) {
        this.crossNodeContainer = crossNodeContainer;
    }

    public boolean isCrossActive() {
        return isCrossActive;
    }

    public void setCrossActive(boolean crossActive) {
        isCrossActive = crossActive;
    }


    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getMaxOut() {
        return maxOut;
    }

    public void setMaxOut(int maxOut) {
        this.maxOut = maxOut;
    }

    public int getMaxIn() {
        return maxIn;
    }

    public void setMaxIn(int maxIn) {
        this.maxIn = maxIn;
    }

    public int getMinAvailableCount() {
        return minAvailableCount;
    }

    public void setMinAvailableCount(int minAvailableCount) {
        this.minAvailableCount = minAvailableCount;
    }

    public int getMaxCrossOut() {
        return maxCrossOut;
    }

    public void setMaxCrossOut(int maxCrossOut) {
        this.maxCrossOut = maxCrossOut;
    }

    public int getMaxCrossIn() {
        return maxCrossIn;
    }

    public void setMaxCrossIn(int maxCrossIn) {
        this.maxCrossIn = maxCrossIn;
    }

    /**
     * 是否为友链注册的跨链Group
     *
     * @return
     */
    public boolean isMoonCrossGroup() {
        if (NetworkParam.getInstance().isMoonNode() && NetworkParam.getInstance().getChainId() != chainId) {
            return true;
        }
        return false;
    }

    /**
     * 1.在可用连接充足情况下，保留一个种子连接，其他的连接需要断开
     * 2.在可用连接不够取代种子情况下，按可用连接数来断开种子连接
     *
     * @param isCross
     * @return
     */
    public void stopConnectedSeeds(boolean isCross) {
        try {
            List<Node> nodes = null;
            int canConnectNodesNum = 0;
            if (isCross) {
                nodes = crossNodeContainer.getConnectedSeedNodes();
                canConnectNodesNum = crossNodeContainer.getCanConnectNodes().size();
            } else {
                nodes = localNetNodeContainer.getConnectedSeedNodes();
                canConnectNodesNum = localNetNodeContainer.getCanConnectNodes().size();
            }
            if (nodes.size() > 1 && canConnectNodesNum > 0) {
                Collections.shuffle(nodes);
                nodes.remove(0);
                while (canConnectNodesNum < nodes.size()) {
                    nodes.remove(0);
                }
            } else {
                return;
            }
            //断开连接
            for (Node node : nodes) {
                node.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 是否为主网链
     *
     * @return
     */
    public boolean isMoonGroup() {
        if (NetworkParam.getInstance().isMoonNode() && NetworkParam.getInstance().getChainId() == chainId) {
            return true;
        }
        return false;
    }

    public int getSameIpMaxCount(boolean isCross) {
        int sameIpMaxCount = 0;
        if (isCross) {
            sameIpMaxCount = (BigDecimal.valueOf(maxCrossIn).divide(BigDecimal.valueOf(maxCrossOut), 0, RoundingMode.HALF_DOWN)).intValue();
        } else {
            sameIpMaxCount = (BigDecimal.valueOf(maxIn).divide(BigDecimal.valueOf(maxOut), 0, RoundingMode.HALF_DOWN)).intValue();
        }
        return sameIpMaxCount;
    }

    private void loadNodes(Map<String, Node> nodeMap, List<NodePo> nodePoList) {
        if (nodePoList != null) {
            for (NodePo nodePo : nodePoList) {
                Node node = (Node) nodePo.parseDto();
                if(null == nodeMap.get(node.getId())) {
                    nodeMap.put(node.getId(), node);
                }
            }
        }
    }


    private void loadNodes(NodesContainer nodesContainer, NodesContainerPo nodesContainerPo) {
        loadNodes(nodesContainer.getDisconnectNodes(), nodesContainerPo.getDisConnectNodes());
        loadNodes(nodesContainer.getUncheckNodes(), nodesContainerPo.getUncheckNodes());
        loadNodes(nodesContainer.getFailNodes(), nodesContainerPo.getFailNodes());
        loadNodes(nodesContainer.getCanConnectNodes(), nodesContainerPo.getCanConnectNodes());
    }

    public void loadNodes(GroupNodesPo groupNodesPo) {
        loadNodes(localNetNodeContainer, groupNodesPo.getSelfNodeContainer());
        loadNodes(crossNodeContainer, groupNodesPo.getCrossNodeContainer());
    }

    public boolean addNeedCheckNode(IpAddress ipAddress, boolean isCross) {
        locker.lock();
        try {
            Node newNode = new Node(magicNumber, ipAddress.getIp().getHostAddress(), ipAddress.getPort(), Node.OUT, isCross);
            if (isCross) {
                return crossNodeContainer.addNeedCheckNode(newNode);
            } else {
                return localNetNodeContainer.addNeedCheckNode(newNode);
            }
        } finally {
            locker.unlock();
        }
    }

    public Collection<Node> getCanConnectNodes(boolean isCross) {
        List<Node> nodeList = new ArrayList<>();
        Collection<Node> allNodes = null;
        if (isCross) {
            allNodes = crossNodeContainer.getCanConnectNodes().values();
        } else {
            allNodes = localNetNodeContainer.getCanConnectNodes().values();
        }
        for (Node node : allNodes) {
            if (node.getStatus() == NodeStatusEnum.CONNECTABLE) {
                nodeList.add(node);
            }
        }
        return nodeList;
    }

    public Collection<Node> getConnectedNodes(boolean isCross) {
        if (isCross) {
            return crossNodeContainer.getConnectedNodes().values();
        } else {
            return localNetNodeContainer.getConnectedNodes().values();
        }
    }

    public List<Node> getAvailableNodes(boolean isCross) {
        if (isCross) {
            return crossNodeContainer.getAvailableNodes();
        } else {
            return localNetNodeContainer.getAvailableNodes();
        }
    }

    public Node getAvailableNode(String nodeId) {
        Node node = localNetNodeContainer.getConnectedNodes().get(nodeId);
        if (null == node) {
            node = crossNodeContainer.getConnectedNodes().get(nodeId);
        }
        if (null != node && NodeConnectStatusEnum.AVAILABLE == node.getConnectStatus()) {
            return node;
        }
        return null;
    }

    public Node getConnectedNode(String nodeId) {
        Node node = localNetNodeContainer.getConnectedNodes().get(nodeId);
        if (null == node) {
            node = crossNodeContainer.getConnectedNodes().get(nodeId);
        }
        return node;
    }

    public void destroy() {
        this.localNetNodeContainer.setStatus(DESTROY);
        this.crossNodeContainer.setStatus(DESTROY);
        NodeGroupManager.getInstance().removeNodeGroup(chainId);
        Collection<Node> nodes = this.localNetNodeContainer.getConnectedNodes().values();
        for (Node node : nodes) {
            node.close();
        }
        Collection<Node> crossNodes = this.crossNodeContainer.getConnectedNodes().values();
        for (Node node : crossNodes) {
            node.close();
        }
        this.crossNodeContainer.clear();
        this.localNetNodeContainer.clear();
    }

    public void reconnect() {
        this.localNetNodeContainer.setStatus(RECONNECT);
        this.crossNodeContainer.setStatus(RECONNECT);
        Collection<Node> nodes = this.localNetNodeContainer.getConnectedNodes().values();
        for (Node node : nodes) {
            node.close();
        }
        Collection<Node> crossNodes = this.crossNodeContainer.getConnectedNodes().values();
        for (Node node : crossNodes) {
            node.close();
        }
        this.localNetNodeContainer.setStatus(WAIT2);
        this.crossNodeContainer.setStatus(WAIT2);
    }

    /**
     * 网络是否可使用 非自有网络满足跨链连接最小数
     *
     * @return boolean
     */
    public boolean isActive(boolean isCross) {
        int activeConnectNum = 0;
        if (isCross) {
            if (DESTROY == crossNodeContainer.getStatus() || RECONNECT == crossNodeContainer.getStatus()) {
                return false;
            }
            activeConnectNum = crossNodeContainer.getConnectedNodes().size();
            if (NetworkParam.getInstance().isMoonNode()) {
                if (activeConnectNum < minAvailableCount) {
                    return false;
                }
            }
        } else {
            if (DESTROY == localNetNodeContainer.getStatus() || RECONNECT == localNetNodeContainer.getStatus()) {
                return false;
            }
            activeConnectNum = localNetNodeContainer.getConnectedNodes().size();
        }
        return activeConnectNum > 0;
    }

    public void addUnCheckNode(Node node) {
        if (node.isCrossConnect()) {
            crossNodeContainer.getUncheckNodes().put(node.getId(), node);
        } else {
            localNetNodeContainer.getUncheckNodes().put(node.getId(), node);
        }
    }

    @Override
    public BasePo parseToPo() {
        GroupPo po = new GroupPo();
        po.setChainId(chainId);
        po.setCrossActive(isCrossActive);
        po.setMagicNumber(magicNumber);
        po.setMaxCrossIn(maxCrossIn);
        po.setMaxCrossOut(maxCrossOut);
        po.setMaxIn(maxIn);
        po.setMaxOut(maxOut);
        po.setMinAvailableCount(minAvailableCount);
        return po;
    }

}
