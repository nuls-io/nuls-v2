package io.nuls.network.rpc.cmd;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.Parameters;
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
 * @Description: 网络信息查询接口
 */
@Component
public class NetworkInfoRpc extends BaseCmd {

    @CmdAnnotation(cmd = CmdConstant.CMD_NW_INFO, version = 1.0, description = "get network info")
    @Parameters({
            @Parameter(parameterName = "chainId", parameterType = "short", canNull = false)
    })
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
        //本地最新高度
        res.put("localBestHeight", localBestHeight);
        //网络最新高度
        if (localBestHeight > netBestHeight) {
            netBestHeight = localBestHeight;
        }
        res.put("netBestHeight", netBestHeight);
        //网络时间偏移
        res.put("timeOffset", TimeManager.netTimeOffset);
        //被动连接节点数量
        res.put("inCount", inCount);
        //主动连接节点数量
        res.put("outCount", outCount);
        return success(res);
    }

    @CmdAnnotation(cmd = CmdConstant.CMD_NW_NODES, version = 1.0, description = "get nodes")
    @Parameters({
            @Parameter(parameterName = "chainId", parameterType = "short", canNull = false)
    })
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
