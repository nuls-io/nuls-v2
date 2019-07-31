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

import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.network.cfg.NetworkConfig;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.constant.NodeStatusEnum;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.dto.Dto;
import io.nuls.network.model.dto.RpcCacheMessage;
import io.nuls.network.model.po.*;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.utils.LoggerUtil;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 节点组对象
 *
 * @author lan
 * @date 2018/11/01
 */
public class NodeGroup implements Dto {
    NetworkConfig networkConfig = SpringLiteContext.getBean(NetworkConfig.class);
    /**
     * 缓存网络组种无法及时处理的信息
     */
    private BlockingDeque<RpcCacheMessage> cacheMsgQueue = new LinkedBlockingDeque<>(NetworkConstant.INIT_CACHE_MSG_QUEUE_NUMBER);

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

    private Map<String, Node> localShareToCrossUncheckNodes = new ConcurrentHashMap<>();
    private Map<String, Node> localShareToCrossCanConnectNodes = new ConcurrentHashMap<>();

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
    public static Map<String, String> statusMap = new HashMap<>();

    static {
        statusMap.put(String.valueOf(WAIT1), "netInit(初始化)");
        statusMap.put(String.valueOf(WAIT2), "waitConnected(待组网)");
        statusMap.put(String.valueOf(OK), "running(运行中)");
        statusMap.put(String.valueOf(DESTROY), "destroy(注销中)");
        statusMap.put(String.valueOf(RECONNECT), "reconnect(重连中)");
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
        //在卫星链上，链id不等于默认id，则是用户注册的跨链
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
     * 1.在可用连接充足情况下，保留一个种子连接，其他的种子连接需要断开
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
            //连接的种子数量大于1，并且可用连接数量大于0
            if (nodes.size() > 1 && canConnectNodesNum > 0) {
                Collections.shuffle(nodes);
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
            Log.error(e);
        }
    }

    /**
     * 是否为主网链
     *
     * @return
     */
    public boolean isMoonGroup() {
        if (networkConfig.isMoonNode() && networkConfig.getChainId() == chainId) {
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
                    /*是本地新增节点 并且 跨链端口存在，则放入跨链待检测队列中*/
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
            if (node.getStatus() == NodeStatusEnum.CONNECTABLE) {
                //排除已经连接的信息
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
