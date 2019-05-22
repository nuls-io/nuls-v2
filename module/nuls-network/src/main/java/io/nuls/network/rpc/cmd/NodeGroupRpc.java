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

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.network.cfg.NetworkConfig;
import io.nuls.network.constant.CmdConstant;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.po.GroupPo;
import io.nuls.network.model.vo.NodeGroupVo;
import io.nuls.network.utils.LoggerUtil;

import java.util.*;

/**
 * @author lan
 * @description 远程调用接口
 * @date 2018/11/07
 **/
@Component
public class NodeGroupRpc extends BaseCmd {
    @Autowired
    NetworkConfig networkConfig;

    /**
     * nw_createNodeGroup
     * 主网创建跨链网络或者链工厂创建链
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_CREATE_NODEGROUP, version = 1.0,
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
        int maxOut = 0;
        if (StringUtils.isNotBlank(String.valueOf(params.get("maxOut")))) {
            maxOut = Integer.valueOf(String.valueOf(params.get("maxOut")));
        }
        if (maxOut == 0) {
            if (networkConfig.isMoonNode()) {
                maxOut = networkConfig.getCrossMaxOutCount();
            } else {
                maxOut = networkConfig.getMaxOutCount();
            }
        }
        int maxIn = 0;
        if (StringUtils.isNotBlank(String.valueOf(params.get("maxIn")))) {
            maxIn = Integer.valueOf(String.valueOf(params.get("maxIn")));
            ;
        }
        if (maxIn == 0) {
            if (networkConfig.isMoonNode()) {
                maxIn = networkConfig.getCrossMaxInCount();
            } else {
                maxIn = networkConfig.getMaxInCount();
            }
        }
        int minAvailableCount = Integer.valueOf(String.valueOf(params.get("minAvailableCount")));
        int isMoonNode = Integer.valueOf(String.valueOf(params.get("isMoonNode")));
        boolean isMoonNet = (isMoonNode == 1);
        if (!networkConfig.isMoonNode() && isMoonNet) {
            LoggerUtil.logger(chainId).error("Local is not Moon net，but param isMoonNode is 1");
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByMagic(magicNumber);
        if (null != nodeGroup) {
            LoggerUtil.logger(chainId).error("getNodeGroupByMagic: nodeGroup  exist");
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
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_ACTIVE_CROSS, version = 1.0,
            description = "activeCross")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "maxOut", parameterType = "int")
    @Parameter(parameterName = "maxIn", parameterType = "int")
    @Parameter(parameterName = "seedIps", parameterType = "String")
    public Response activeCross(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        LoggerUtil.logger(chainId).info("params:chainId={},maxOut={},maxIn={},seedIps={}", params.get("chainId"),
                params.get("maxOut"), params.get("maxIn"), params.get("seedIps"));
        List<GroupPo> nodeGroupPos = new ArrayList<>();
        int maxOut;
        if (null == params.get("maxOut") || 0 == Integer.valueOf(params.get("maxOut").toString())) {
            maxOut = networkConfig.getMaxOutCount();
        } else {
            maxOut = Integer.valueOf(String.valueOf(params.get("maxOut")));
        }
        int maxIn;
        if (null == params.get("maxIn") || 0 == Integer.valueOf(params.get("maxIn").toString())) {
            maxIn = networkConfig.getMaxInCount();
        } else {
            maxIn = Integer.valueOf(String.valueOf(params.get("maxIn")));
        }
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        String seedIps = String.valueOf(params.get("seedIps"));
        //友链的跨链协议调用
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        if (null == nodeGroup) {
            LoggerUtil.logger(chainId).error("getNodeGroupByMagic is null");
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        if (chainId != nodeGroup.getChainId()) {
            LoggerUtil.logger(chainId).error("chainId != nodeGroup.getChainId()");
            return failed(NetworkErrorCode.PARAMETER_ERROR);
        }
        nodeGroup.setMaxCrossIn(maxIn);
        nodeGroup.setMaxCrossOut(maxOut);
        List<String> ipList = new ArrayList<>();
        if (StringUtils.isNotBlank(seedIps)) {
            String[] ips = seedIps.split(NetworkConstant.COMMA);
            Collections.addAll(ipList, ips);
        }
        for (String croosSeed : ipList) {
            String[] crossAddr = croosSeed.split(NetworkConstant.COLON);
            nodeGroup.addNeedCheckNode(crossAddr[0], Integer.valueOf(crossAddr[1]), Integer.valueOf(crossAddr[1]), true);
        }
        networkConfig.setMoonSeedIpList(ipList);
        nodeGroup.setCrossActive(true);
        return success();
    }

    /**
     * nw_getGroupByChainId
     * 查看指定网络组信息
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_GET_GROUP_BY_CHAINID, version = 1.0,
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
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_GET_CHAIN_CONNECT_AMOUNT, version = 1.0,
            description = "nw_getChainConnectAmount")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    @Parameter(parameterName = "isCross", parameterType = "boolean")
    public Response getChainConnectAmount(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        try {
            NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
            boolean isCross = Boolean.valueOf(String.valueOf(params.get("isCross")));
            Map<String, Object> rtMap = new HashMap<>();
            rtMap.put("connectAmount", nodeGroup.getAvailableNodes(isCross).size());
            return success(rtMap);
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
            return failed(e.getMessage());
        }
    }


    /**
     * nw_delNodeGroup
     * 注销指定网络组信息
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_GET_DELETE_NODEGROUP, version = 1.0,
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
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_GET_SEEDS, version = 1.0,
            description = "nw_getSeeds")
    public Response getCrossSeeds(Map params) {
        List<String> seeds = networkConfig.getMoonSeedIpList();
        if (null == seeds) {
            return success();
        }
        StringBuilder seedsStr = new StringBuilder();
        for (String seed : seeds) {
            seedsStr.append(seed);
            seedsStr.append(",");
        }
        Map<String, String> rtMap = new HashMap<>(1);
        if (seedsStr.length() > 0) {
            rtMap.put("seedsIps", seedsStr.substring(0, seedsStr.length() - 1));
        } else {
            rtMap.put("seedsIps", "");
        }
        return success(rtMap);
    }

    /**
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_GET_MAIN_NET_MAGIC_NUMBER, version = 1.0,
            description = "nw_getMainMagicNumber")
    public Response getMainMagicNumber(Map params) {
        try {
            Map<String, Object> rtMap = new HashMap<>();
            rtMap.put("value", networkConfig.getPacketMagic());
            return success(rtMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(NetworkErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    /**
     * nw_reconnect
     * 重连网络
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_RECONNECT, version = 1.0,
            description = "reconnect")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]")
    public Response reconnect(Map params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        //默认只对自有网络进行重连接
        nodeGroup.reconnect(false);
        return success();
    }

    /**
     * nw_getGroups
     * 获取链组信息
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_NW_GET_GROUPS, version = 1.0,
            description = "getGroups")
    @Parameter(parameterName = "startPage", parameterType = "int", parameterValidRange = "[0,65535]")
    @Parameter(parameterName = "pageSize", parameterType = "int", parameterValidRange = "[0,65535]")
    public Response getGroups(Map params) {
        int startPage = Integer.valueOf(String.valueOf(params.get("startPage")));
        int pageSize = Integer.valueOf(String.valueOf(params.get("pageSize")));
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
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
