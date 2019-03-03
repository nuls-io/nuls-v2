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
package io.nuls.network.rpc.cmd;

import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.constant.NodeConnectStatusEnum;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.network.model.po.NodePo;
import io.nuls.network.model.vo.NodeVo;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * @author lan
 * @description Open peer connection remote call node rpc
 * 开放 peer 连接的远程调用 node rpc
 * @create 2018/11/09
 **/
@Component
public class NodeRpc extends BaseCmd {
    private NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
    private static final int STATE_ALL = 0;
    private static final int STATE_CONNECT = 1;
    private static final int STATE_DIS_CONNECT = 2;

    /**
     * nw_addNodes
     * 增加节点
     */
    @CmdAnnotation(cmd = "nw_addNodes", version = 1.0,
            description = "addNodes")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "isCross", parameterType = "int", parameterValidRange = "[0,1]")
    @Parameter(parameterName = "nodes", parameterType = "String")
    public Response addNodes(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        int isCross = Integer.valueOf(String.valueOf(params.get("isCross")));
        String nodes = String.valueOf(params.get("nodes"));
        Log.info("chainId:" + chainId + "==nodes:" + nodes);
        if (chainId < 0 || StringUtils.isBlank(nodes)) {
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        boolean blCross = false;
        if (1 == isCross) {
            blCross = true;
        }
        String[] peers = nodes.split(",");
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        List<NodePo> nodePos = new ArrayList<>();
        for (String peer : peers) {
            String[] ipPort = peer.split(":");
            IpAddress address = new IpAddress(ipPort[0], Integer.valueOf(ipPort[1]));
            nodeGroup.addNeedCheckNode(address, blCross);
        }
        return success();
    }


    /**
     * nw_delNodes
     * 删除节点
     */
    @CmdAnnotation(cmd = "nw_delNodes", version = 1.0, description = "delNodes")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "nodes", parameterType = "String")
    public Response delNodes(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        String nodes = String.valueOf(params.get("nodes"));
        Log.info("chainId:" + chainId + "==nodes:" + nodes);
        if (chainId < 0 || StringUtils.isBlank(nodes)) {
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        String[] peers = nodes.split(",");
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        for (String nodeId : peers) {
            //移除 peer
            Node node = nodeGroup.getLocalNetNodeContainer().getConnectedNodes().get(nodeId);
            if (null != node) {
                node.close();
            } else {
                nodeGroup.getLocalNetNodeContainer().getCanConnectNodes().remove(nodeId);
                nodeGroup.getLocalNetNodeContainer().getUncheckNodes().remove(nodeId);
                nodeGroup.getLocalNetNodeContainer().getDisconnectNodes().remove(nodeId);
                nodeGroup.getLocalNetNodeContainer().getFailNodes().remove(nodeId);
            }

            node = nodeGroup.getCrossNodeContainer().getConnectedNodes().get(nodeId);
            if (null != node) {
                node.close();
            } else {
                nodeGroup.getCrossNodeContainer().getCanConnectNodes().remove(nodeId);
                nodeGroup.getCrossNodeContainer().getUncheckNodes().remove(nodeId);
                nodeGroup.getCrossNodeContainer().getDisconnectNodes().remove(nodeId);
                nodeGroup.getCrossNodeContainer().getFailNodes().remove(nodeId);
            }

        }
        return success();
    }

    @CmdAnnotation(cmd = "nw_getNodes", version = 1.0, description = "getNodes")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "state", parameterType = "int", parameterValidRange = "[0,2]")
    @Parameter(parameterName = "isCross", parameterType = "int", parameterValidRange = "[0,1]")
    @Parameter(parameterName = "startPage", parameterType = "int", parameterValidRange = "[0,65535]")
    @Parameter(parameterName = "pageSize", parameterType = "int", parameterValidRange = "[0,65535]")
    public Response getNodes(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        int state = Integer.valueOf(String.valueOf(params.get("state")));
        int isCross = Integer.valueOf(String.valueOf(params.get("isCross")));
        int startPage = Integer.valueOf(String.valueOf(params.get("startPage")));
        int pageSize = Integer.valueOf(String.valueOf(params.get("pageSize")));
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        List<Node> nodes = new ArrayList<>();

        if (0 == isCross) {
            /*
             * 普通连接
             * comment connection
             */
            addNode(nodes, state, nodeGroup.getLocalNetNodeContainer());

        } else {
            /*
             * 跨链连接
             * cross connection
             */
            addNode(nodes, state, nodeGroup.getCrossNodeContainer());
        }
        int total = nodes.size();
        List<NodeVo> pageList = new ArrayList<>();
        if (0 == startPage && 0 == pageSize) {
            //get all datas
            for (Node node : nodes) {
                pageList.add(buildNodeVo(node, nodeGroup.getMagicNumber(), chainId));
            }
        } else {
            //get by page
            int currIdx = (startPage > 1 ? (startPage - 1) * pageSize : 0);
            for (int i = 0; i < pageSize && i < (total - currIdx); i++) {
                Node node = nodes.get(currIdx + i);
                NodeVo nodeVo = buildNodeVo(node, nodeGroup.getMagicNumber(), chainId);
                pageList.add(nodeVo);
            }
        }
        return success(pageList);
    }

    private void addNode(List<Node> nodes, int state, NodesContainer nodesContainer) {
        if (STATE_ALL == state) {
            /*
             * all connection
             */
            nodes.addAll(nodesContainer.getConnectedNodes().values());
            nodes.addAll(nodesContainer.getCanConnectNodes().values());
            nodes.addAll(nodesContainer.getDisconnectNodes().values());
            nodes.addAll(nodesContainer.getUncheckNodes().values());
            nodes.addAll(nodesContainer.getFailNodes().values());
        } else if (STATE_CONNECT == state) {
            /*
             * only  connection
             */
            nodes.addAll(nodesContainer.getAvailableNodes());
        } else if (STATE_DIS_CONNECT == state) {
            /*
             * only dis connection
             */
            nodes.addAll(nodesContainer.getCanConnectNodes().values());
            nodes.addAll(nodesContainer.getDisconnectNodes().values());
            nodes.addAll(nodesContainer.getUncheckNodes().values());
            nodes.addAll(nodesContainer.getFailNodes().values());
        }
    }

    /**
     * nw_updateNodeInfo
     * 更新区块高度与hash
     */
    @CmdAnnotation(cmd = "nw_updateNodeInfo", version = 1.0, description = "updateNodeInfo")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    @Parameter(parameterName = "blockHash", parameterType = "String")
    public Response updateNodeInfo(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        String nodeId = String.valueOf(params.get("nodeId"));
        long blockHeight = Long.valueOf(String.valueOf(params.get("blockHeight")));
        String blockHash = String.valueOf(params.get("blockHash"));
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        if (null == nodeGroup) {
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        Node node = nodeGroup.getConnectedNode(nodeId);
        if (null == node) {
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        Log.info("update nodeinfo:{}, chainId:{}, height:{}, hash:{}", nodeId, chainId, blockHeight, blockHash);
        node.setBlockHash(blockHash);
        node.setBlockHeight(blockHeight);
        return success();
    }


    private NodeVo buildNodeVo(Node node, long magicNumber, int chainId) {
        NodeVo nodeVo = new NodeVo();
        nodeVo.setBlockHash(node.getBlockHash());
        nodeVo.setBlockHeight(node.getBlockHeight());
        nodeVo.setState(node.getConnectStatus() == NodeConnectStatusEnum.AVAILABLE ? 1 : 0);
        nodeVo.setTime(node.getConnectTime());
        nodeVo.setChainId(chainId);
        nodeVo.setIp(node.getIp());
        nodeVo.setIsOut(node.getType() == Node.OUT ? 1 : 0);
        nodeVo.setMagicNumber(magicNumber);
        nodeVo.setNodeId(node.getId());
        nodeVo.setPort(node.getRemotePort());
        return nodeVo;
    }
}
