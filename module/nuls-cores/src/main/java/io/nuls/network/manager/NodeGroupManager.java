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
package io.nuls.network.manager;

import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.network.cfg.NetworkConfig;
import io.nuls.network.constant.ManagerStatusEnum;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddressShare;
import io.nuls.network.utils.LoggerUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * （节点组）管理
 * node group  manager
 *
 * @author lan
 * @date 2018/11/01
 **/
public class NodeGroupManager extends BaseManager {

    public static NodeGroupManager getInstance() {
        return nodeGroupManager;
    }


    private static NodeGroupManager nodeGroupManager = new NodeGroupManager();
    private StorageManager storageManager = StorageManager.getInstance();

    /**
     * key:chainId
     */
    private Map<String, NodeGroup> nodeGroupMap = new ConcurrentHashMap<>();
    private ManagerStatusEnum status = ManagerStatusEnum.UNINITIALIZED;

    /**
     * key:magicNumber value:chainId
     */
    private static Map<String, String> magicNumChainIdMap = new ConcurrentHashMap<>();

    private NodeGroupManager() {

    }

    /**
     * 获取可分享的节点信息
     *
     * @param node
     * @return
     */
    public List<IpAddressShare> getAvailableShareNodes(Node node, int getChainId, boolean isCrossAddress) {
        NetworkConfig networkConfig = SpringLiteContext.getBean(NetworkConfig.class);

        List<IpAddressShare> addressList = new ArrayList<>();
        List nodesList = new ArrayList();
        if (node.isCrossConnect()) {
            //跨链节点的请求
            //取本地网络地址去支持跨链连接,跨链的请求地址取的都是对方的本地网络IP
            if (networkConfig.isMoonNode()) {
                //是主网节点，回复
                nodesList.addAll(NodeGroupManager.getInstance().getMoonMainNet().getLocalShareToCrossCanConnectNodes().values());
            } else {
                nodesList.addAll(node.getNodeGroup().getLocalShareToCrossCanConnectNodes().values());
            }
        } else {
            //非跨链节点的请求,分2种，一种是获取外界网络的跨链地址，一种是自身网络地址
            NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(getChainId);
            if (isCrossAddress) {
                //主网 跨链网络组
                nodesList.addAll(nodeGroup.getCrossNodeContainer().getAllCanShareNodes().values());
            } else {
                nodesList.addAll(nodeGroup.getLocalNetNodeContainer().getAllCanShareNodes().values());
            }
        }

        addAddress(nodesList, addressList, node.getIp(), node.isCrossConnect());
        return addressList;

    }

    private void addAddress(Collection<Node> nodes, List<IpAddressShare> list, String fromIp, boolean isCross) {
        for (Node peer : nodes) {
            /*
             * 排除自身连接信息，比如组网A=====B，A向B请求地址，B给的地址列表需排除A地址。
             * Exclude self-connection information, such as networking A=====B,
             * A requests an address from B, and the address list given by B excludes the A address.
             */
            if (peer.getIp().equals(fromIp)) {
                continue;
            }
            /*
             * 只有主动连接的节点地址才可使用。
             * Only active node addresses are available for use.
             */
            if (Node.OUT == peer.getType()) {
                try {
                    int port = peer.getRemotePort();
                    int crossPort = peer.getRemoteCrossPort();
                    if (isCross) {
                        if (0 == crossPort) {
                            continue;
                        }
                    }
                    IpAddressShare ipAddress = new IpAddressShare(peer.getIp(), port, crossPort);
                    list.add(ipAddress);
                } catch (Exception e) {
                    LoggerUtil.COMMON_LOG.error(e);
                }
            }
        }
    }


    public Collection<NodeGroup> getNodeGroupCollection() {
        return nodeGroupMap.values();
    }

    public NodeGroup getNodeGroupByMagic(long magicNumber) {
        String chainId = magicNumChainIdMap.get(String.valueOf(magicNumber));
        if (null == chainId) {
            return null;
        }
        return nodeGroupMap.get(chainId);
    }

    public NodeGroup getNodeGroupByChainId(int chainId) {
        return nodeGroupMap.get(String.valueOf(chainId));
    }

    public NodeGroup getMoonMainNet() {
        NetworkConfig networkConfig = SpringLiteContext.getBean(NetworkConfig.class);
        if (networkConfig.isMoonNode()) {
            return getNodeGroupByChainId(networkConfig.getChainId());
        }
        return null;
    }

    /**
     * @return List<NodeGroup>
     */
    public List<NodeGroup> getNodeGroups() {
        return new ArrayList<>(nodeGroupMap.values());

    }

    public int getChainIdByMagicNum(long magicNum) {
        if (null != magicNumChainIdMap.get(String.valueOf(magicNum))) {
            return Integer.valueOf(magicNumChainIdMap.get(String.valueOf(magicNum)));
        }
        return 0;
    }

    /**
     * @param chainId   chain Id
     * @param nodeGroup nodeGroup
     */
    public void addNodeGroup(int chainId, NodeGroup nodeGroup) {
        nodeGroupMap.put(String.valueOf(chainId), nodeGroup);
        magicNumChainIdMap.put(String.valueOf(nodeGroup.getMagicNumber()), String.valueOf(chainId));
        // String logLevel = SpringLiteContext.getBean(NetworkConfig.class).getLogLevel();
        LoggerUtil.createLogs(chainId);
    }

    public void removeNodeGroup(int chainId) {
        NodeGroup nodeGroup = nodeGroupMap.remove(String.valueOf(chainId));
        if (null != nodeGroup) {
            magicNumChainIdMap.remove(String.valueOf(nodeGroup.getMagicNumber()));
        }
    }

    public boolean validMagicNumber(long magicNumber) {
        return null != magicNumChainIdMap.get(String.valueOf(magicNumber));
    }


    @Override
    public void init() {
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        NetworkConfig networkConfig = SpringLiteContext.getBean(NetworkConfig.class);
        /*
         * 获取配置的信息，进行自有网络的nodeGroup配置初始化
         * Obtain the configuration information and initialize the nodeGroup configuration of the own netw
         */
        NodeGroup nodeGroup = new NodeGroup();
        nodeGroupManager.addNodeGroup(networkConfig.getChainId(), nodeGroup);

        /*
         *友链跨链部分等待跨链模块的初始化调用，卫星链的跨链group通过数据库进行初始化
         *获取数据库中已有的nodeGroup跨链网络组信息
         *  Friends chain cross-chain part waiting for the initialization call of the cross-chain module, the cross-chain group of the satellite chain is initialized through the database
         * Get the existing nodeGroup cross-chain network group information in the database
         */
        List<NodeGroup> list = storageManager.getAllNodeGroupFromDb();
        for (NodeGroup dbNodeGroup : list) {
            if (dbNodeGroup.getChainId() == nodeGroup.getChainId()) {
                //配置的group优先,数据库存储的忽略
                continue;
            }
            //主网的默认跨链网络组属性active
            dbNodeGroup.setCrossActive(true);
            nodeGroupManager.addNodeGroup(dbNodeGroup.getChainId(), dbNodeGroup);
        }
    }

    @Override
    public void start() {
        status = ManagerStatusEnum.RUNNING;
    }

    @Override
    public void change(ManagerStatusEnum toStatus) {
        if (toStatus == ManagerStatusEnum.STOPED) {
            nodeGroupMap.forEach((key, value) -> {
                value.reconnect(false);
                value.reconnect(true);
            });
        }
        status = toStatus;
    }
}
