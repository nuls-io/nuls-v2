package io.nuls.core.rpc.modulebootstrap;

import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.JSONUtils;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-01 10:51
 * @Description: 功能描述
 */
@Component
public class ModuleStatusCmd extends BaseCmd {

    @Autowired
    RpcModule rpcModule;

    @CmdAnnotation(
            scope = Constants.PRIVATE,
            cmd = "listenerDependenciesReady",
            version = 1.0,
            minEvent = 1,
            description = "notify module is ready")
    public Response listenerDependenciesReady(Map<String, Object> map) {
        Module module = JSONUtils.map2pojo(map, Module.class);
        rpcModule.listenerDependenciesReady(module);
        return success("ModuleReadyListener success");
    }

    @CmdAnnotation(
            scope = Constants.PRIVATE,
            cmd = "registerModuleDependencies", version = 1.0, minEvent = 1,
            description = "Register module followerList")
    public Response followModule(Map<String, Object> param) {
        Module module = JSONUtils.map2pojo(param, Module.class);
        rpcModule.followModule(module);
        return success("ModuleDependenciesRegisterListener success");
    }

    @CmdAnnotation(
            scope = Constants.PRIVATE,
            cmd = "connectReady", version = 1.0, minEvent = 1,
            description = "check module rpc is ready")
    public Response connectReady(Map<String, Object> param) {
        return success(Boolean.valueOf(rpcModule.isReady()));
    }

}
