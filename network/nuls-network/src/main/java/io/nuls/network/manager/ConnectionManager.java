/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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


import io.netty.channel.socket.SocketChannel;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.nuls.network.constant.*;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.manager.handler.base.BaseMeesageHandlerInf;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.message.VersionMessage;
import io.nuls.network.model.po.GroupNodesPo;
import io.nuls.network.netty.NettyClient;
import io.nuls.network.netty.NettyServer;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.utils.IpUtil;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.TimeService;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * 连接管理器,连接的启动，停止，连接引用缓存管理
 * Connection manager, connection start, stop, connection reference cache management
 *
 * @author lan
 * @date 2018/11/01
 */
public class ConnectionManager extends BaseManager {
    private TaskManager taskManager = TaskManager.getInstance();
    private static ConnectionManager instance = new ConnectionManager();
    /**
     * 作为Server 被动连接的peer
     * Passer as a server passive connection
     */
    private Map<String, Node> cacheConnectNodeInMap = new ConcurrentHashMap<>();
    /**
     * 作为client 主动连接的peer
     * As the client actively connected peer
     */
    private Map<String, Node> cacheConnectNodeOutMap = new ConcurrentHashMap<>();


    public static ConnectionManager getInstance() {
        return instance;
    }

    private ConnectionManager() {

    }

    /**
     * 节点连接失败
     *
     * @param node
     */
    public void nodeConnectFail(Node node) {

        node.setStatus(NodeStatusEnum.UNAVAILABLE);
        node.setConnectStatus(NodeConnectStatusEnum.FAIL);

        node.setFailCount(node.getFailCount() + 1);
        node.setLastProbeTime(TimeService.currentTimeMillis());
    }

    private StorageManager storageManager = StorageManager.getInstance();
    private ManagerStatusEnum status = ManagerStatusEnum.UNINITIALIZED;

    public boolean isRunning() {
        return instance.status == ManagerStatusEnum.RUNNING;
    }

    /**
     * 加载种子节点
     * loadSeedsNode
     */
    private void loadSeedsNode() {
        NetworkParam networkParam = NetworkParam.getInstance();
        List<String> list = networkParam.getSeedIpList();
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByMagic(networkParam.getPacketMagic());
        for (String seed : list) {
            String[] peer = seed.split(NetworkConstant.COLON);
            if (IpUtil.getIps().contains(peer[0])) {
                continue;
            }
            Node node = new Node(nodeGroup.getMagicNumber(), peer[0], Integer.valueOf(peer[1]), Node.OUT, false);
            node.setConnectStatus(NodeConnectStatusEnum.UNCONNECT);
            node.setSeedNode(true);
            node.setStatus(NodeStatusEnum.CONNECTABLE);
            nodeGroup.getLocalNetNodeContainer().getCanConnectNodes().put(node.getId(), node);
            nodeGroup.getLocalNetNodeContainer().getUncheckNodes().remove(node.getId());
            nodeGroup.getLocalNetNodeContainer().getDisconnectNodes().remove(node.getId());
            nodeGroup.getLocalNetNodeContainer().getFailNodes().remove(node.getId());
        }
    }

    public void nodeClientConnectSuccess(Node node) {
        NodeGroup nodeGroup = node.getNodeGroup();
        NodesContainer nodesContainer = null;
        if (node.isCrossConnect()) {
            nodesContainer = nodeGroup.getCrossNodeContainer();
        } else {
            nodesContainer = nodeGroup.getLocalNetNodeContainer();
        }
        nodesContainer.getConnectedNodes().put(node.getId(), node);
        nodesContainer.getCanConnectNodes().remove(node.getId());
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
        Log.debug("client node {} connect success !", node.getId());
        //发送握手
        VersionMessage versionMessage = MessageFactory.getInstance().buildVersionMessage(node, nodeGroup.getMagicNumber());
        BaseMeesageHandlerInf handler = MessageHandlerFactory.getInstance().getHandler(versionMessage.getHeader().getCommandStr());
        handler.send(versionMessage, node, true);
    }

    private void cacheNode(Node node, SocketChannel channel) {

        String name = "node-" + node.getId();
        boolean exists = AttributeKey.exists(name);
        AttributeKey attributeKey;
        if (exists) {
            attributeKey = AttributeKey.valueOf(name);
        } else {
            attributeKey = AttributeKey.newInstance(name);
        }
        Attribute<Node> attribute = channel.attr(attributeKey);
        attribute.set(node);
    }

    public boolean nodeConnectIn(String ip, int port, SocketChannel channel) {
        boolean isCross = false;
        //client 连接 server的端口是跨链端口?
        if (channel.localAddress().getPort() == NetworkParam.getInstance().getCrossPort()) {
            isCross = true;
        }
        Log.debug("peer = {}:{} connectIn",ip,port);
        //此时无法判定业务所属的网络id，所以无法归属哪个group,只有在version消息处理时才能知道
        Node node = new Node(0L, ip, port, Node.IN, isCross);
        node.setConnectStatus(NodeConnectStatusEnum.CONNECTED);
        node.setChannel(channel);
        cacheNode(node, channel);
        return true;
    }

    public void nodeConnectDisconnect(Node node) {
        if (node.getChannel() != null) {
            node.setChannel(null);
        }
        NodeGroup nodeGroup = node.getNodeGroup();
        NodesContainer nodesContainer = null;
        if (node.isCrossConnect()) {
            nodesContainer = nodeGroup.getCrossNodeContainer();
        } else {
            nodesContainer = nodeGroup.getLocalNetNodeContainer();
        }
        //连接断开后,判断是否是为连接成功，还是连接成功后断开
        if (node.getConnectStatus() == NodeConnectStatusEnum.CONNECTED ||
                node.getConnectStatus() == NodeConnectStatusEnum.AVAILABLE) {
            node.setFailCount(0);
            node.setConnectStatus(NodeConnectStatusEnum.DISCONNECT);

            nodesContainer.getDisconnectNodes().put(node.getId(), node);
            nodesContainer.getConnectedNodes().remove(node.getId());

//            Log.info("node {} disconnect !", node.getId());
        } else {
            // 如果是未连接成功，标记为连接失败，失败次数+1，记录当前失败时间，供下次尝试连接使用
            nodeConnectFail(node);
            nodesContainer.getCanConnectNodes().remove(node.getId());
            nodesContainer.getFailNodes().put(node.getId(), node);
        }
    }


    /**
     * netty boot
     */
    private void nettyBoot() {
        serverStart();
        clientStart();
        Log.info("==========================NettyBoot");
    }

    /**
     * server start
     */
    private void serverStart() {
        NettyServer server = new NettyServer(NetworkParam.getInstance().getPort());
        NettyServer serverCross = new NettyServer(NetworkParam.getInstance().getCrossPort());
        server.init();
        serverCross.init();
        ThreadUtils.createAndRunThread("node server start", () -> {
            try {
                server.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.error(e.getMessage());
            }
        }, false);
        ThreadUtils.createAndRunThread("node crossServer start", () -> {
            try {
                serverCross.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.error(e.getMessage());
            }
        }, false);

    }

    private void clientStart() {
        taskManager.clientConnectThreadStart();
    }


    public boolean connection(Node node) {
        try {
            NettyClient client = new NettyClient(node);
            return client.start();
        } catch (Exception e) {
            Log.error("connect to node {} error : {}", node.getId(), e.getMessage());
            return false;
        }
    }

    @Override
    public void init() throws Exception {
        status = ManagerStatusEnum.INITIALIZED;
        Collection<NodeGroup> nodeGroups = NodeGroupManager.getInstance().getNodeGroupCollection();
        for (NodeGroup nodeGroup : nodeGroups) {
            if (!nodeGroup.isMoonCrossGroup()) {
                //自有网络组，增加种子节点的加载，主网的跨链网络组，则无此步骤
                loadSeedsNode();
            }
            //数据库获取node
            GroupNodesPo groupNodesPo = storageManager.getNodesByChainId(nodeGroup.getChainId());
            nodeGroup.loadNodes(groupNodesPo);
        }
    }

    @Override
    public void start() throws Exception {
        while(!ConnectManager.isReady()){
            Log.debug("wait depend modules ready");
            Thread.sleep(2000L);
        }
        nettyBoot();
        status = ManagerStatusEnum.RUNNING;
    }
}
