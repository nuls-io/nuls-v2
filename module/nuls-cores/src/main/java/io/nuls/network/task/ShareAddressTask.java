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

import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddressShare;
import io.nuls.network.utils.LoggerUtil;

import java.util.*;

/**
 * share self address task
 * Share your own external networkIPPort task
 *
 * @author lan
 * @create 2018/11/14
 */
public class ShareAddressTask implements Runnable {

    private final NulsCoresConfig networkConfig = SpringLiteContext.getBean(NulsCoresConfig.class);
    private NodeGroup nodeGroup = null;
    private boolean isCross = false;

    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    public ShareAddressTask(NodeGroup nodeGroup, boolean isCross) {
        this.nodeGroup = nodeGroup;
        this.isCross = isCross;
    }

    @Override
    public void run() {
        if (isCross) {
            doCrossNet();
        } else {
            doLocalNet();
        }

    }

    private void doLocalNet() {
        //getMoreNodes
        MessageManager.getInstance().sendGetAddressMessage(nodeGroup, false, false, true);
        //local node get Cross address by local net
        if (nodeGroup.isMoonGroup()) {
            //Get a list of cross chain friend chain connections
            List<NodeGroup> nodeGroups = NodeGroupManager.getInstance().getNodeGroups();
            for (NodeGroup crossNodeGroup : nodeGroups) {
                MessageManager.getInstance().sendGetCrossAddressMessage(nodeGroup, crossNodeGroup, false, true, true);
            }
        } else {
            MessageManager.getInstance().sendGetAddressMessage(nodeGroup, false, true, true);
        } //shareMyServer
        String externalIp = getMyExtranetIp();
        if (externalIp == null) {
            return;
        }
        networkConfig.getLocalIps().add(externalIp);
        /*Connection sharing of self owned network*/
        if (!nodeGroup.isMoonCrossGroup()) {
            LoggerUtil.logger(nodeGroup.getChainId()).info("begin share self ip  is {}:{}", externalIp, networkConfig.getPort());
            Node myNode = new Node(nodeGroup.getMagicNumber(), externalIp, networkConfig.getPort(), networkConfig.getCrossPort(), Node.OUT, false);
            myNode.setConnectedListener(() -> {
                myNode.getChannel().close();
                LoggerUtil.logger(nodeGroup.getChainId()).info("self ip verify success,doShare ：share self ip  is {}:{}", externalIp, networkConfig.getPort());
                //If it is the main network satellite chain,Self owned network discovery needs to be broadcasted to all cross chain branches,If it is a friend chain, the self owned network discovery also needs to be broadcasted to the main network
                doShare(externalIp, nodeGroup.getLocalNetNodeContainer().getAvailableNodes(),
                        networkConfig.getPort(), networkConfig.getCrossPort(), false);
            });
            myNode.setDisconnectListener(() -> myNode.setChannel(null));
            connectionManager.connection(myNode);
        }
    }

    private void doCrossNet() {
        //getMoreNodes
        MessageManager.getInstance().sendGetAddressMessage(nodeGroup, true, true, true);
        //shareMyServer
        String externalIp = getMyExtranetIp();
        if (externalIp == null) {
            return;
        }
        networkConfig.getLocalIps().add(externalIp);
        if (nodeGroup.isCrossActive()) {
            //Opened cross chain business
            LoggerUtil.logger(nodeGroup.getChainId()).info("begin cross ip share. self ip  is {}:{}", externalIp, networkConfig.getCrossPort());
            Node crossNode = new Node(nodeGroup.getMagicNumber(), externalIp, networkConfig.getCrossPort(), networkConfig.getCrossPort(), Node.OUT, true);
            crossNode.setConnectedListener(() -> {
                crossNode.getChannel().close();
                LoggerUtil.logger(nodeGroup.getChainId()).info("cross ip verify success,doShare {}:{}", externalIp, networkConfig.getCrossPort());
                doShare(externalIp, nodeGroup.getCrossNodeContainer().getAvailableNodes(), networkConfig.getCrossPort(), networkConfig.getCrossPort(), true);
            });
            connectionManager.connection(crossNode);
        }
    }

    private String getMyExtranetIp() {
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(nodeGroup.getLocalNetNodeContainer().getConnectedNodes().values());
        nodes.addAll(nodeGroup.getCrossNodeContainer().getConnectedNodes().values());
        return getMostSameIp(nodes);
    }

    private String getMostSameIp(Collection<Node> nodes) {

        Map<String, Integer> ipMaps = new HashMap<>();

        for (Node node : nodes) {
            String ip = node.getExternalIp();
            if (ip == null) {
                continue;
            }
            Integer count = ipMaps.get(ip);
            if (count == null) {
                ipMaps.put(ip, 1);
            } else {
                ipMaps.put(ip, count + 1);
            }
        }

        int maxCount = 0;
        String ip = null;
        for (Map.Entry<String, Integer> entry : ipMaps.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                ip = entry.getKey();
            }
        }

        return ip;
    }

    private void doShare(String externalIp, Collection<Node> nodes, int port, int crossPort, boolean isCrossAddress) {
        IpAddressShare ipAddressShare = new IpAddressShare(externalIp, port, crossPort);
        MessageManager.getInstance().broadcastSelfAddrToAllNode(nodes, ipAddressShare, isCrossAddress, true);
    }
}
