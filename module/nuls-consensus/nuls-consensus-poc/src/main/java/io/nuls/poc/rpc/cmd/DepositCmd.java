package io.nuls.poc.rpc.cmd;

import io.nuls.core.rpc.model.*;
import io.nuls.poc.model.dto.output.AgentDTO;
import io.nuls.poc.model.dto.output.DepositDTO;
import io.nuls.poc.service.DepositService;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.List;
import java.util.Map;

/**
 * 共识委托相关接口
 * @author tag
 * 2018/11/7
 * */
@Component
public class DepositCmd extends BaseCmd {
    @Autowired
    private DepositService service;

    /**
     * 委托共识
     * */
    @CmdAnnotation(cmd = "cs_depositToAgent", version = 1.0, description = "创建委托交易/deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "节点HASH")
    @Parameter(parameterName = "deposit", parameterType = "String", parameterDes = "委托金额")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码")
    @ResponseData(name = "返回值", description = "加入共识交易Hash", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "加入共识交易Hash")
    }))
    public Response depositToAgent(Map<String,Object> params){
        Result result = service.depositToAgent(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 退出共识
     * */
    @CmdAnnotation(cmd = "cs_withdraw", version = 1.0, description = "退出委托交易/withdraw deposit agent transaction")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址")
    @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "加入共识交易HASH")
    @Parameter(parameterName = "password", parameterType = "String", parameterDes = "账户密码")
    @ResponseData(name = "返回值", description = "退出共识交易Hash", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "txHash", description = "退出共识交易Hash")
    }))
    public Response withdraw(Map<String,Object> params){
        Result result = service.withdraw(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 查询委托信息列表
     * */
    @CmdAnnotation(cmd = "cs_getDepositList", version = 1.0, description = "查询指定账户或指定节点的委托信息/Query delegation information for a specified account or node")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id")
    @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "页码")
    @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "每页数量")
    @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址")
    @Parameter(parameterName = "agentHash", parameterType = "String", parameterDes = "节点HASH")
    @ResponseData(name = "返回值", description = "返回一个Page对象，这里只描述Page对象中的集合",
            responseType = @TypeDescriptor(value = List.class, collectionElement = DepositDTO.class)
    )
    public Response getDepositList(Map<String,Object> params){
        Result result = service.getDepositList(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
