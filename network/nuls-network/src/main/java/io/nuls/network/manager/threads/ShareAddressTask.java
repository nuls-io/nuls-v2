package io.nuls.network.manager.threads;

import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;


import java.util.*;

import static io.nuls.network.utils.LoggerUtil.Log;

public class ShareAddressTask implements Runnable {

    private final NetworkParam networkParam = NetworkParam.getInstance();
    private NodeGroup nodeGroup = null;
    private boolean isCross = false;

    private ConnectionManager connectionManager = ConnectionManager.getInstance();

    public ShareAddressTask(NodeGroup nodeGroup) {
        this.nodeGroup = nodeGroup;
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
        Log.info("doLocalNet {}", nodeGroup.getChainId());
        //getMoreNodes
        MessageManager.getInstance().sendGetAddrMessage(nodeGroup, false, true);
        //shareMyServer
        String externalIp = getMyExtranetIp();
        if (externalIp == null) {
            return;
        }
        Log.info("my external ip  is {}", externalIp);
        networkParam.getLocalIps().add(externalIp);
        /*自有网络的连接分享*/
        if (!nodeGroup.isMoonCrossGroup()) {
            Log.info("share self ip  is {}", externalIp);
            Node myNode = new Node(nodeGroup.getMagicNumber(), externalIp, networkParam.getPort(), Node.OUT, false);
            myNode.setConnectedListener(() -> {
                myNode.getChannel().close();
                doShare(externalIp, nodeGroup.getLocalNetNodeContainer().getConnectedNodes().values(), networkParam.getPort());
            });
            myNode.setDisconnectListener(() -> myNode.setChannel(null));
            connectionManager.connection(myNode);
        }
    }

    private void doCrossNet() {
        Log.info("doCrossNet {}", nodeGroup.getChainId());
        //getMoreNodes
        MessageManager.getInstance().sendGetAddrMessage(nodeGroup, true, true);
        //shareMyServer
        String externalIp = getMyExtranetIp();
        if (externalIp == null) {
            return;
        }
        Log.info("my external ip  is {}", externalIp);
        networkParam.getLocalIps().add(externalIp);
        if (nodeGroup.isCrossActive()) {
            //开启了跨链业务
            Node crossNode = new Node(nodeGroup.getMagicNumber(), externalIp, networkParam.getCrossPort(), Node.OUT, true);
            crossNode.setConnectedListener(() -> {
                crossNode.getChannel().close();
                doShare(externalIp, nodeGroup.getCrossNodeContainer().getConnectedNodes().values(), networkParam.getCrossPort());
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

    private void doShare(String externalIp, Collection<Node> nodes, int port) {
        Log.info("doShare ip ={}:{}", externalIp,port);
        IpAddress ipAddress = new IpAddress(externalIp, port);
        MessageManager.getInstance().broadcastSelfAddrToAllNode(nodes, ipAddress, true);
    }


}
