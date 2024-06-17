package io.nuls.network.rpc.cmd;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.network.constant.CmdConstant;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.rpc.call.BlockRpcService;
import io.nuls.network.rpc.call.impl.BlockRpcServiceImpl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 16:04
 * @Description: Network information query interface
 */
@Component
@NulsCoresCmd(module = ModuleE.NW)
public class NetworkInfoRpc extends BaseCmd {

    @CmdAnnotation(cmd = CmdConstant.CMD_NW_INFO, version = 1.0,
            description = "Obtain basic information about node networks")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Connected ChainId,Value range[1-65535]")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "localBestHeight", valueType = Long.class, description = "Local node block height"),
            @Key(name = "netBestHeight", valueType = Long.class, description = "The highest height of network node blocks"),
            @Key(name = "timeOffset", valueType = Long.class, description = "Node and network time difference value"),
            @Key(name = "inCount", valueType = Integer.class, description = "the mostServer,peerAccess quantity"),
            @Key(name = "outCount", valueType = Integer.class, description = "As aclientConnect externalServerquantity")
    }))
    public Response getNetworkInfo(Map<String, Object> params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        Map<String, Object> res = new HashMap<>(5);
        List<Node> nodes = nodeGroup.getLocalNetNodeContainer().getAvailableNodes();
        long localBestHeight = 0;
        long netBestHeight = 0;
        int inCount = 0;
        int outCount = 0;
        BlockRpcService blockRpcService = SpringLiteContext.getBean(BlockRpcServiceImpl.class);
        for (Node node : nodes) {
            if (node.getBlockHeight() > netBestHeight) {
                netBestHeight = node.getBlockHeight();
            }
            if (node.getType() == Node.IN) {
                inCount++;
            } else {
                outCount++;
            }
        }
        if (nodeGroup.isMoonCrossGroup()) {
            localBestHeight = 0;
        } else {
            localBestHeight = blockRpcService.getBestBlockHeader(chainId).getBlockHeight();
        }
        //Latest local altitude
        res.put("localBestHeight", localBestHeight);
        //The latest height of the internet
        if (localBestHeight > netBestHeight) {
            netBestHeight = localBestHeight;
        }
        res.put("netBestHeight", netBestHeight);
        //Network time offset
        res.put("timeOffset", TimeManager.netTimeOffset);
        //Number of passive connection nodes
        res.put("inCount", inCount);
        //Number of active connection nodes
        res.put("outCount", outCount);
        return success(res);
    }

    @CmdAnnotation(cmd = CmdConstant.CMD_NW_NODES, version = 1.0,
            description = "Obtain network connection node information")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Connected ChainId,Value range[1-65535]")
    })
    @ResponseData(name = "Return value", description = "Return aListobject",
            responseType = @TypeDescriptor(value = List.class, collectionElement = Map.class, mapKeys = {
                    @Key(name = "peer", valueType = String.class, description = "peernodeID"),
                    @Key(name = "blockHeight", valueType = Long.class, description = "Node height"),
                    @Key(name = "blockHash", valueType = String.class, description = "nodeHash")
            })
    )
    public Response getNetworkNodeList(Map<String, Object> params) {
        int chainId = Integer.valueOf(String.valueOf(params.get("chainId")));
        List<Map<String, Object>> res = new ArrayList<>();
        NodeGroup nodeGroup = NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        List<Node> nodes = new ArrayList<>();
        nodes.addAll(nodeGroup.getLocalNetNodeContainer().getAvailableNodes());
        nodes.addAll(nodeGroup.getCrossNodeContainer().getAvailableNodes());
        for (Node node : nodes) {
            Map<String, Object> data = new HashMap<>();
            //ip:port
            data.put("peer", node.getId());
            data.put("blockHeight", node.getBlockHeight());
            data.put("blockHash", node.getBlockHash());
            res.add(data);
        }
        return success(res);
    }
}
