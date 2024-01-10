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

import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.ioc.SpringLiteContext;
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
 * （Node group）Administration
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
     * Obtain shareable node information
     *
     * @param node
     * @return
     */
    public List<IpAddressShare> getAvailableShareNodes(Node node, int getChainId, boolean isCrossAddress) {
        NulsCoresConfig networkConfig = SpringLiteContext.getBean(NulsCoresConfig.class);

        List<IpAddressShare> addressList = new ArrayList<>();
        List nodesList = new ArrayList();
        if (node.isCrossConnect()) {
            //Cross chain node requests
            //Taking local network addresses to support cross chain connections,Cross chain request addresses are all taken from the other party's local networkIP
            if (networkConfig.isMoonNode()) {
                //It is the main network node, reply
                nodesList.addAll(NodeGroupManager.getInstance().getMoonMainNet().getLocalShareToCrossCanConnectNodes().values());
            } else {
                nodesList.addAll(node.getNodeGroup().getLocalShareToCrossCanConnectNodes().values());
            }
        } else {
            //Non cross chain node requests,branch2There are two types: one is to obtain cross chain addresses from external networks, and the other is to obtain one's own network address
            NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(getChainId);
            if (isCrossAddress) {
                //Main network Cross chain network group
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
             * Exclude self connection information, such as networkingA=====B,AtowardsBRequest address,BThe given address list needs to be excludedAAddress.
             * Exclude self-connection information, such as networking A=====B,
             * A requests an address from B, and the address list given by B excludes the A address.
             */
            if (peer.getIp().equals(fromIp)) {
                continue;
            }
            /*
             * Only actively connected node addresses can be used.
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
        NulsCoresConfig networkConfig = SpringLiteContext.getBean(NulsCoresConfig.class);
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
        NulsCoresConfig networkConfig = SpringLiteContext.getBean(NulsCoresConfig.class);
        /*
         * Obtain configuration information for self owned networknodeGroupConfiguration initialization
         * Obtain the configuration information and initialize the nodeGroup configuration of the own netw
         */
        NodeGroup nodeGroup = new NodeGroup();
        nodeGroupManager.addNodeGroup(networkConfig.getChainId(), nodeGroup);

        /*
         *The cross chain part of the friend chain waits for the initialization call of the cross chain module, and the cross chain of the satellite chaingroupInitialize through database
         *Retrieve existing data from the databasenodeGroupCross chain network group information
         *  Friends chain cross-chain part waiting for the initialization call of the cross-chain module, the cross-chain group of the satellite chain is initialized through the database
         * Get the existing nodeGroup cross-chain network group information in the database
         */
        List<NodeGroup> list = storageManager.getAllNodeGroupFromDb();
        for (NodeGroup dbNodeGroup : list) {
            if (dbNodeGroup.getChainId() == nodeGroup.getChainId()) {
                //Configuredgroupfirst,Ignoring database storage
                continue;
            }
            //The default cross chain network group properties of the main networkactive
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
