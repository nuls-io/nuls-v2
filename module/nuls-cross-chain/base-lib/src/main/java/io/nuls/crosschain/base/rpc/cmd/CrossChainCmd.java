package io.nuls.crosschain.base.rpc.cmd;

import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.crosschain.base.service.CrossChainService;

import java.util.Map;

/**
 * 跨链模块服务处理接口类
 * @author tag
 * @date 2019/4/8
 */
@Component
public class CrossChainCmd  extends BaseCmd {
    @Autowired
    private CrossChainService service;

    /**
     * 创建跨链交易
     * */
    @CmdAnnotation(cmd = "createCrossTx", version = 1.0, description = "create cross transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response createCrossTx(Map<String,Object> params){
        Result result = service.createCrossTx(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

    /**
     * 单笔跨链交易验证
     * */
    @CmdAnnotation(cmd = "validCrossTx", version = 1.0, description = "valid cross transaction 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response validCrossTx(Map<String,Object> params){
        Result result = service.validCrossTx(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

//    /**
//     * 跨链模块交易提交
//     * */
//    @CmdAnnotation(cmd = BaseConstant.TX_COMMIT, version = 1.0, description = "commmit cross transaction 1.0")
//    @Parameter(parameterName = "chainId", parameterType = "int")
//    public Response commitCrossTx(Map<String,Object> params){
//        Result result = service.commitCrossTx(params);
//        if(result.isFailed()){
//            return failed(result.getErrorCode());
//        }
//        return success(result.getData());
//    }
//
//    /**
//     * 跨链模块交易回滚
//     * */
//    @CmdAnnotation(cmd = BaseConstant.TX_ROLLBACK, version = 1.0, description = "rollback cross transaction 1.0")
//    @Parameter(parameterName = "chainId", parameterType = "int")
//    public Response rollbackCrossTx(Map<String,Object> params){
//        Result result = service.rollbackCrossTx(params);
//        if(result.isFailed()){
//            return failed(result.getErrorCode());
//        }
//        return success(result.getData());
//    }
//
//    /**
//     * 批量验证
//     * */
//    @CmdAnnotation(cmd = BaseConstant.TX_VALIDATOR, version = 1.0, description = "cross transaction batch valid 1.0")
//    @Parameter(parameterName = "chainId", parameterType = "int")
//    public Response crossTxBacthValid(Map<String,Object> params){
//        Result result = service.crossTxBatchValid(params);
//        if(result.isFailed()){
//            return failed(result.getErrorCode());
//        }
//        return success(result.getData());
//    }

    /**
     * 查询跨链交易处理状态
     * */
    @CmdAnnotation(cmd = "getCrossTxState", version = 1.0, description = "get cross transaction process state 1.0")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getCrossTxState(Map<String,Object> params){
        Result result = service.getCrossTxState(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
