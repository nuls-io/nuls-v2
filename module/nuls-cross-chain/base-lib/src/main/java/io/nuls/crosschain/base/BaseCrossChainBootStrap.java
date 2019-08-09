package io.nuls.crosschain.base;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.RpcModule;

import java.util.*;

import static io.nuls.crosschain.base.constant.CrossChainConstant.*;

/**
 * 跨链模块启动类
 * Cross Chain Module Startup and Initialization Management
 * @author tag
 * 2019/4/10
 */
public abstract class BaseCrossChainBootStrap extends RpcModule {
    private Set<String> rpcPaths = new HashSet<>(){{add(RPC_PATH);}};

    /**
     * 新增需要加入RPC的CMD所在目录
     * Add the directory where the CMD needs to be added to RPC
     * */
    protected void registerRpcPath(String rpcPath){
        rpcPaths.add(rpcPath);
    }


    @Override
    public void init() {
        super.init();
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.CC.name,ROLE);
    }

    /**
     * 指定RpcCmd的包名
     * 可以不实现此方法，若不实现将使用spring init扫描的包
     * @return
     */
    @Override
    public Set<String> getRpcCmdPackage(){
        return rpcPaths;
    }

    protected Set<String> getRpcPaths() {
        return rpcPaths;
    }

    public void setRpcPaths(Set<String> rpcPaths) {
        this.rpcPaths = rpcPaths;
    }
}
