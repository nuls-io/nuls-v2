package io.nuls.crosschain.nuls.rpc.cmd;

import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.servive.BlockService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.Map;

/**
 * 提供给区块模块调用的接口
 * @author tag
 * @date 2019/4/25
 */
@Component
public class BlockCmd extends BaseCmd {
    @Autowired
    private BlockService service;
    /**
     * 区块模块高度变化通知跨链模块
     * */
    @CmdAnnotation(cmd = "newBlockHeight", version = 1.0, description = "receive new block height")
    @Parameter(parameterName = ParamConstant.CHAIN_ID, parameterType = ParamConstant.PARAM_TYPE_INT)
    public Response newBlockHeight(Map<String,Object> params){
        Result result = service.newBlockHeight(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }
}
