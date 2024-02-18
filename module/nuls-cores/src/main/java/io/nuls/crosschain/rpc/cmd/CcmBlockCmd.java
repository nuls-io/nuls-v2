package io.nuls.crosschain.rpc.cmd;

import io.nuls.core.rpc.model.*;
import io.nuls.crosschain.servive.BlockService;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * Provide interfaces for block module calls
 * @author tag
 * @date 2019/4/25
 */
@Component
@NulsCoresCmd(module = ModuleE.CC)
public class CcmBlockCmd extends BaseCmd {
    @Autowired
    private BlockService service;
    /**
     * Block module height change notification cross chain module
     * */
    @CmdAnnotation(cmd = "newBlockHeight", version = 1.0, description = "Chain block height change/receive new block height")
    @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainID")
    @Parameter(parameterName = "height", parameterType = "long", parameterDes = "chainID")
    @Parameter(parameterName = "download", parameterType = "int", parameterDes = "download 0Blocking download in progress,1Received the latest block")
    @ResponseData(description = "No specific return value, successful without errors")
    public Response newBlockHeight(Map<String,Object> params){
        Result result = service.newBlockHeight(params);
        if(result.isFailed()){
            return failed(result.getErrorCode());
        }
        return success(result.getData());
    }

}
