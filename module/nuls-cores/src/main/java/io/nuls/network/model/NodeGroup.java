/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.constant.NodeStatusEnum;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.dto.Dto;
import io.nuls.network.model.dto.RpcCacheMessage;
import io.nuls.network.model.po.*;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.utils.LoggerUtil;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Node Group Object
 *
 * @author lan
 * @date 2018/11/01
 */
public class NodeGroup implements Dto {
    NulsCoresConfig networkConfig = SpringLiteContext.getBean(NulsCoresConfig.class);
    /**
     * Information that cannot be processed in a timely manner by caching network groups
     */
    private BlockingDeque<RpcCacheMessage> cacheMsgQueue = new LinkedBlockingDeque<>(NetworkConstant.INIT_CACHE_MSG_QUEUE_NUMBER);

    private long magicNumber;
    private int chainId;
    private int maxOut;
    private int maxIn;
    /**
     * Maximum number of cross chain connections
     */
    private int maxCrossOut = 0;
    private int maxCrossIn = 0;

    private int minAvailableCount;

    /**
     * Is the cross chain network activated,Default cross chain on satellite chaintrue,
     * Friend Chain DefaultfalseThis property is only applicable when requesting cross chain modulestrue
     */
    private boolean isCrossActive = false;


    /**
     * localNet-Nodes in their own network
     */
    private NodesContainer localNetNodeContainer = new NodesContainer();

    /**
     * Cross chain-Cross chain connected nodes
     */
    private NodesContainer crossNodeContainer = new NodesContainer();

    private Map<String, Node> localShareToCrossUncheckNodes = new ConcurrentHashMap<>();
    private Map<String, Node> localShareToCrossCanConnectNodes = new ConcurrentHashMap<>();

    private boolean hadBlockHeigh = false;
    private Lock locker = new ReentrantLock();
    /**
     * GROUP  STATUS
     * INITIALIZED Status, waiting to connect
     * The unavailable state during initial creation isWAIT1
     * reachOKThe unavailable state of post shock isWAIT2
     */
    public final static int WAIT1 = 1;
    public final static int WAIT2 = 2;
    public final static int OK = 3;
    private final static int DESTROY = -1;
    private final static int RECONNECT = -2;


    public static Map<String, String> statusMap = new HashMap<>();

    static {
        statusMap.put(String.valueOf(WAIT1), "netInit(initialization)");
        statusMap.put(String.valueOf(WAIT2), "waitConnected(To be networked)");
        statusMap.put(String.valueOf(OK), "running(Running)");
        statusMap.put(String.valueOf(DESTROY), "destroy(Cancelling)");
        statusMap.put(String.valueOf(RECONNECT), "reconnect(Reconnection)");
    }

    public String getLocalStatus() {
        return statusMap.get(String.valueOf(localNetNodeContainer.getStatus()));
    }

    public String getCrossStatus() {
        return statusMap.get(String.valueOf(crossNodeContainer.getStatus()));
    }

    public NodeGroup() {
        this.magicNumber = networkConfig.getPacketMagic();
        this.chainId = networkConfig.getChainId();
        this.maxIn = networkConfig.getMaxInCount();
        this.maxOut = networkConfig.getMaxOutCount();
        this.minAvailableCount = 0;

    }

    public NodeGroup(long magicNumber, int chainId, int maxIn, int maxOut, int minAvailableCount) {
        this.magicNumber = magicNumber;
        this.chainId = chainId;
        this.maxIn = maxIn;
        this.maxOut = maxOut;
        this.minAvailableCount = minAvailableCount;
        if (networkConfig.isMoonNode()) {
            isCrossActive = true;
            this.maxCrossIn = maxIn;
            this.maxCrossOut = maxOut;
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

    public Map<String, Node> getLocalShareToCrossUncheckNodes() {
        return localShareToCrossUncheckNodes;
    }

    public void setLocalShareToCrossUncheckNodes(Map<String, Node> localShareToCrossUncheckNodes) {
        this.localShareToCrossUncheckNodes = localShareToCrossUncheckNodes;
    }

    public Map<String, Node> getLocalShareToCrossCanConnectNodes() {
        return localShareToCrossCanConnectNodes;
    }

    public void setLocalShareToCrossCanConnectNodes(Map<String, Node> localShareToCrossCanConnectNodes) {
        this.localShareToCrossCanConnectNodes = localShareToCrossCanConnectNodes;
    }

    public boolean isCrossActive() {
        return isCrossActive;
    }

    public void setCrossActive(boolean crossActive) {
        isCrossActive = crossActive;
    }

    public boolean isHadBlockHeigh() {
        return hadBlockHeigh;
    }

    public void setHadBlockHeigh(boolean hadBlockHeigh) {
        this.hadBlockHeigh = hadBlockHeigh;
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
     * Is it a cross chain registration for a friend chainGroup
     *
     * @return
     */
    public boolean isMoonCrossGroup() {
        //On the satellite chain, the chainidNot equal to defaultid, it is the cross chain registration of the user
        if (networkConfig.isMoonNode() && networkConfig.getChainId() != chainId) {
            return true;
        }
        return false;
    }

    public BlockingDeque<RpcCacheMessage> getCacheMsgQueue() {
        return cacheMsgQueue;
    }

    public void setCacheMsgQueue(BlockingDeque<RpcCacheMessage> cacheMsgQueue) {
        this.cacheMsgQueue = cacheMsgQueue;
    }

    /**
     * 1.When there are sufficient available connections, keep one seed connection and disconnect the other seed connections
     * 2.When there are not enough available connections to replace the seed, disconnect the seed connection based on the number of available connections
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
            //The number of connected seeds is greater than1And the number of available connections is greater than0
            if (nodes.size() > 1 && canConnectNodesNum > 0) {
                Collections.shuffle(nodes);
                while (canConnectNodesNum < nodes.size()) {
                    nodes.remove(0);
                }
            } else {
                return;
            }
            //Disconnect
            for (Node node : nodes) {
                node.close();
            }
        } catch (Exception e) {
            Log.error(e);
        }
    }

    /**
     * Is it the main network link
     *
     * @return
     */
    public boolean isMoonGroup() {
        if (networkConfig.isMoonNode() && networkConfig.getChainId() == chainId) {
            return true;
        }
        return false;
    }

    public boolean isMoonNode() {
        return networkConfig.isMoonNode();
    }

    public int getSameIpMaxCount(boolean isCross) {
        int sameIpMaxCount = 0;
        if (isCross) {
            sameIpMaxCount = networkConfig.getCrossMaxInSameIp();
        } else {
            sameIpMaxCount = networkConfig.getMaxInSameIp();
        }
        return sameIpMaxCount;
    }

    private void loadNodes(Map<String, Node> nodeMap, List<NodePo> nodePoList) {
        if (nodePoList != null) {
            for (NodePo nodePo : nodePoList) {
                Node node = (Node) nodePo.parseDto();
                node.setMagicNumber(magicNumber);
                if (null == nodeMap.get(node.getId())) {
                    nodeMap.put(node.getId(), node);
                }
            }
        }
    }

    public void addCrossCheckNodes(String ip, int port, int crossPort) {
        Node shareToCrossCheckNode = new Node(magicNumber, ip, crossPort, crossPort, Node.OUT, true);
        if (null == localShareToCrossUncheckNodes.get(shareToCrossCheckNode.getId()) && null == localShareToCrossCanConnectNodes.get(shareToCrossCheckNode.getId())) {
            localShareToCrossUncheckNodes.put(shareToCrossCheckNode.getId(), shareToCrossCheckNode);
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

    public boolean addNeedCheckNode(String ip, int port, int crossPort, boolean isCross) {
        locker.lock();
        try {
            if (isCross) {
                Node newNode = new Node(magicNumber, ip, crossPort, crossPort, Node.OUT, isCross);
                return crossNodeContainer.addNeedCheckNode(newNode);
            } else {
                Node newNode = new Node(magicNumber, ip, port, crossPort, Node.OUT, isCross);
                boolean localAdd = localNetNodeContainer.addNeedCheckNode(newNode);
                if (crossPort > 0 && localAdd) {
                    /*It is a locally added node also If a cross chain port exists, it will be placed in the cross chain pending detection queue*/
                    addCrossCheckNodes(ip, crossPort, crossPort);
                }
                return localAdd;
            }
        } finally {
            locker.unlock();
        }
    }

    public Collection<Node> getCanConnectNodes(boolean isCross) {
        List<Node> nodeList = new ArrayList<>();
        Collection<Node> allNodes = null;
        Map<String, Node> connectedNodes = null;
        if (isCross) {
            allNodes = crossNodeContainer.getCanConnectNodes().values();
            connectedNodes = crossNodeContainer.getConnectedNodes();
        } else {
            allNodes = localNetNodeContainer.getCanConnectNodes().values();
            connectedNodes = localNetNodeContainer.getConnectedNodes();
        }
        for (Node node : allNodes) {
            //Exclude connected information,As aserverexistenceinConnected
            if (node.getStatus() == NodeStatusEnum.CONNECTABLE) {
                if (null == connectedNodes.get(node.getId())) {
                    nodeList.add(node);
                }
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

    public void reconnect(boolean isCross) {
        if (isCross) {
            this.crossNodeContainer.setStatus(RECONNECT);
            Collection<Node> crossNodes = this.crossNodeContainer.getConnectedNodes().values();
            for (Node node : crossNodes) {
                LoggerUtil.logger(chainId).info("cross chainId={} node={} reconnect", chainId, node.getId());
                node.close();
            }
            this.crossNodeContainer.setStatus(WAIT2);
        } else {
            this.localNetNodeContainer.setStatus(RECONNECT);
            Collection<Node> nodes = this.localNetNodeContainer.getConnectedNodes().values();
            for (Node node : nodes) {
                LoggerUtil.logger(chainId).info("local chainId={} node={} reconnect", chainId, node.getId());
                node.close();
            }
            this.localNetNodeContainer.setStatus(WAIT2);
        }

    }

    /**
     * Is the network usable Non owned networks meet the minimum number of cross chain connections
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
            if (networkConfig.isMoonNode()) {
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
