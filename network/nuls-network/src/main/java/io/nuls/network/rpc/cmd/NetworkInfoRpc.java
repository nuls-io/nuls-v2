package io.nuls.network.rpc.cmd;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.Parameters;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 16:04
 * @Description:
 *   网络信息查询接口
 */
@Component
public class NetworkInfoRpc extends BaseCmd {

    @CmdAnnotation(cmd="nw_info",version = 1.0,description = "get network info")
    @Parameters({
            @Parameter(parameterName = "chainId",parameterType = "short",canNull = false)
    })
    public Response getNetworkInfo(Map<String,Object>param){
        Map<String,Object> res = new HashMap<>(5);
        res.put("localBestHeight",1); //本地最新高度
        res.put("netBestHeight",1); //网络最新高度
        res.put("timeOffset",1); //网络时间偏移
        res.put("inCount",1);  //被动连接节点数量
        res.put("outCount",1); //主动连接节点数量
        return success(res);
    }

    @CmdAnnotation(cmd = "nw_nodes",version = 1.0,description = "get nodes")
    @Parameters({
            @Parameter(parameterName = "chainId",parameterType = "short",canNull = false)
    })
    public Response getNetworkNodeList(Map<String,Object> param){
        List<String> res = new ArrayList<>();
        res.add("115.212.11.111");
        return success(res);
    }

}
