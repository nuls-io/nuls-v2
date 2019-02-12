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
package io.nuls.network.rpc.internal;

import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.po.GroupPo;
import io.nuls.network.model.vo.NodeGroupVo;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;

import java.util.*;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * @author lan
 * @description 远程调用接口
 * @date 2018/11/07
 **/
@Component
public class NodeGroupRpc extends BaseCmd {
    private NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();

    /**
     * nw_createNodeGroup
     * 创建跨链网络
     */
    @CmdAnnotation(cmd = "nw_createNodeGroup", version = 1.0,
            description = "createNodeGroup")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "magicNumber", parameterType = "String")
    @Parameter(parameterName = "maxOut", parameterType = "String")
    @Parameter(parameterName = "maxIn", parameterType = "String")
    @Parameter(parameterName = "minAvailableCount", parameterType = "String")
    @Parameter(parameterName = "seedIps", parameterType = "String")
    @Parameter(parameterName = "isMoonNode", parameterType = "int", parameterValidRange = "[0,1]")
    public Response createNodeGroup(Map params) {
        List<GroupPo> nodeGroupPos = new ArrayList<>();
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        long magicNumber = Long.valueOf(String.valueOf(params.get("magicNumber")));
        int maxOut;
        if (StringUtils.isNotBlank(String.valueOf(params.get("maxOut")))) {
            maxOut = Integer.valueOf(String.valueOf(params.get("maxOut")));
        } else {
            maxOut = NetworkParam.getInstance().getMaxOutCount();
        }

        int maxIn;
        if (StringUtils.isNotBlank(String.valueOf(params.get("maxIn")))) {
            maxIn = Integer.valueOf(String.valueOf(params.get("maxIn")));
        } else {
            maxIn = NetworkParam.getInstance().getMaxInCount();
        }
        int minAvailableCount = Integer.valueOf(String.valueOf(params.get("minAvailableCount")));
        int isMoonNode = Integer.valueOf(String.valueOf(params.get("isMoonNode")));
        boolean isMoonNet = (isMoonNode == 1);
        //友链创建的是链工厂，isSelf 为true
        boolean isSelf = !isMoonNet;
        if (!NetworkParam.getInstance().isMoonNode()) {
            Log.info("MoonNode is false，but param isMoonNode is 1");
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(magicNumber);
        if (null != nodeGroup) {
            Log.info("getNodeGroupByMagic: nodeGroup  exist");
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        nodeGroup = new NodeGroup(magicNumber, chainId, maxIn, maxOut, minAvailableCount);
        //存储nodegroup
        nodeGroupPos.add((GroupPo) nodeGroup.parseToPo());
        StorageManager.getInstance().getDbService().saveNodeGroups(nodeGroupPos);
        nodeGroupManager.addNodeGroup(nodeGroup.getChainId(), nodeGroup);
        // 成功
        return success();
    }

    /**
     * nw_activeCross
     * 友链激活跨链
     */
    @CmdAnnotation(cmd = "nw_activeCross", version = 1.0,
            description = "activeCross")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "maxOut", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "maxIn", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "seedIps", parameterType = "String")
    public Response activeCross(Map params) {
        List<GroupPo> nodeGroupPos = new ArrayList<>();
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        int maxOut;
        if (StringUtils.isNotBlank(String.valueOf(params.get("maxOut")))) {
            maxOut = Integer.valueOf(String.valueOf(params.get("maxOut")));
        } else {
            maxOut = NetworkParam.getInstance().getMaxOutCount();
        }

        int maxIn;
        if (StringUtils.isNotBlank(String.valueOf(params.get("maxIn")))) {
            maxIn = Integer.valueOf(String.valueOf(params.get("maxIn")));
        } else {
            maxIn = NetworkParam.getInstance().getMaxInCount();
        }
        String seedIps = String.valueOf(params.get("seedIps"));
        //友链的跨链协议调用
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        if (null == nodeGroup) {
            Log.info("getNodeGroupByMagic is null");
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        if (chainId != nodeGroup.getChainId()) {
            Log.info("chainId != nodeGroup.getChainId()");
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        nodeGroup.setMaxCrossIn(maxIn);
        nodeGroup.setMaxCrossOut(maxOut);
        List<String> ipList = new ArrayList<>();
        if (StringUtils.isNotBlank(seedIps)) {
            String[] ips = seedIps.split(NetworkConstant.COMMA);
            Collections.addAll(ipList, ips);
        }
        NetworkParam.getInstance().setMoonSeedIpList(ipList);
        nodeGroup.setCrossActive(true);
        return success();
    }

    /**
     * nw_getGroupByChainId
     * 查看指定网络组信息
     */
    @CmdAnnotation(cmd = "nw_getGroupByChainId", version = 1.0,
            description = "getGroupByChainId")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    public Response getGroupByChainId(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        NodeGroupVo nodeGroupVo = buildNodeGroupVo(nodeGroup);
        return success(nodeGroupVo);
    }

    private NodeGroupVo buildNodeGroupVo(NodeGroup nodeGroup) {
        NodeGroupVo nodeGroupVo = new NodeGroupVo();
        nodeGroupVo.setChainId(nodeGroup.getChainId());
        nodeGroupVo.setMagicNumber(nodeGroup.getMagicNumber());
//        if(null != nodeGroupConnector){
//            nodeGroupVo.setBlockHash(nodeGroupConnector.getBlockHash());
//            nodeGroupVo.setBlockHeight(nodeGroupConnector.getBlockHeight());
//        }
        nodeGroupVo.setConnectCount(nodeGroup.getLocalNetNodeContainer().getConnectedNodes().size());
        nodeGroupVo.setDisConnectCount(nodeGroup.getLocalNetNodeContainer().getCanConnectNodes().size()
                + nodeGroup.getLocalNetNodeContainer().getDisconnectNodes().size() +
                nodeGroup.getLocalNetNodeContainer().getUncheckNodes().size() +
                nodeGroup.getLocalNetNodeContainer().getFailNodes().size());
        nodeGroupVo.setConnectCrossCount(nodeGroup.getCrossNodeContainer().getConnectedNodes().size());
        nodeGroupVo.setDisConnectCrossCount(nodeGroup.getCrossNodeContainer().getCanConnectNodes().size()
                + nodeGroup.getCrossNodeContainer().getDisconnectNodes().size() +
                nodeGroup.getCrossNodeContainer().getUncheckNodes().size() +
                nodeGroup.getCrossNodeContainer().getFailNodes().size());
        nodeGroupVo.setInCount(nodeGroup.getLocalNetNodeContainer().getConnectedCount(Node.IN));
        nodeGroupVo.setOutCount(nodeGroup.getLocalNetNodeContainer().getConnectedCount(Node.OUT));
        nodeGroupVo.setInCrossCount(nodeGroup.getCrossNodeContainer().getConnectedCount(Node.IN));
        nodeGroupVo.setOutCrossCount(nodeGroup.getCrossNodeContainer().getConnectedCount(Node.OUT));
        //网络连接，并能正常使用
        if (nodeGroup.isMoonCrossGroup()) {
            nodeGroupVo.setIsActive(nodeGroup.isActive(true) ? 1 : 0);
        } else {
            nodeGroupVo.setIsActive(nodeGroup.isActive(false) ? 1 : 0);
        }
        //跨链模块是否可用
        nodeGroupVo.setIsCrossActive(nodeGroup.isCrossActive() ? 1 : 0);
        nodeGroupVo.setIsMoonNet(nodeGroup.isMoonGroup() ? 1 : 0);
        nodeGroupVo.setTotalCount(nodeGroupVo.getInCount() + nodeGroupVo.getOutCount() + nodeGroupVo.getInCrossCount() + nodeGroupVo.getOutCrossCount());
        return nodeGroupVo;
    }

    /**
     * nw_getChainConnectAmount
     * 查看指定网络组信息
     */
    @CmdAnnotation(cmd = "nw_getChainConnectAmount", version = 1.0,
            description = "nw_getChainConnectAmount")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "isCross", parameterType = "boolean")
    public Response getChainConnectAmount(Map params) {
        try {
            int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
            NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
            boolean isCross = Boolean.valueOf(String.valueOf(params.get("isCross")));
            Map<String, Object> rtMap = new HashMap<>();
            rtMap.put("connectAmount", nodeGroup.getAvailableNodes(isCross).size());
            return success(rtMap);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(e.getMessage());
        }
    }


    /**
     * nw_delNodeGroup
     * 注销指定网络组信息
     */
    @CmdAnnotation(cmd = "nw_delNodeGroup", version = 1.0,
            description = "delGroupByChainId")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    public Response delGroupByChainId(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        StorageManager.getInstance().getDbService().deleteGroup(chainId);
        //删除网络连接
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        nodeGroup.destroy();
        return success();
    }

    /**
     * nw_getSeeds
     * 查询跨链种子节点
     */
    @CmdAnnotation(cmd = "nw_getSeeds", version = 1.0,
            description = "delGroupByChainId")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    public Response getCrossSeeds(List params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        Log.info("chainId:" + chainId);
        List<String> seeds = NetworkParam.getInstance().getMoonSeedIpList();
        if (null == seeds) {
            return success();
        }
        StringBuilder seedsStr = new StringBuilder();
        for (String seed : seeds) {
            seedsStr.append(seed);
            seedsStr.append(",");
        }
        if (seedsStr.length() > 0) {
            return success(seedsStr.substring(0, seedsStr.length()));
        }
        return success();
    }


    /**
     * nw_reconnect
     * 重连网络
     */
    @CmdAnnotation(cmd = "nw_reconnect", version = 1.0,
            description = "reconnect")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    public Response reconnect(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        Log.info("chainId:" + chainId);
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        nodeGroup.reconnect();
        return success();
    }

    /**
     * nw_getGroups
     * 获取链组信息
     */
    @CmdAnnotation(cmd = "nw_getGroups", version = 1.0,
            description = "getGroups")
    @Parameter(parameterName = "startPage", parameterType = "int", parameterValidRange = "[0,65535]")
    @Parameter(parameterName = "pageSize", parameterType = "int", parameterValidRange = "[0,65535]")
    public Response getGroups(Map params) {
        int startPage = Integer.valueOf(String.valueOf(params.get("startPage")));
        int pageSize = Integer.valueOf(String.valueOf(params.get("pageSize")));
        List<NodeGroup> nodeGroups = nodeGroupManager.getNodeGroups();
        int total = nodeGroups.size();
        List<NodeGroupVo> pageList = new ArrayList<>();
        if (startPage == 0 && pageSize == 0) {
            for (NodeGroup nodeGroup : nodeGroups) {
                pageList.add(buildNodeGroupVo(nodeGroup));
            }
        } else {
            int currIdx = (startPage > 1 ? (startPage - 1) * pageSize : 0);
            for (int i = 0; i < pageSize && i < (total - currIdx); i++) {
                NodeGroup nodeGroup = nodeGroups.get(currIdx + i);
                NodeGroupVo nodeGroupVo = buildNodeGroupVo(nodeGroup);
                pageList.add(nodeGroupVo);
            }
        }
        return success(pageList);
    }

}
