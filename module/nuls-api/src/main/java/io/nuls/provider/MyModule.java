package io.nuls.provider;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.provider.api.config.Config;
import io.nuls.provider.rpctools.TransactionTools;
import io.nuls.v2.SDKContext;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-10 20:54
 * @Description: 模块业务实现类
 */
@Component
public class MyModule {

    @Autowired
    TransactionTools transactionTools;
    @Autowired
    private Config config;

    /**
     * 启动模块
     * 模块启动后，当申明的依赖模块都已经准备就绪将调用此函数
     * @param moduleName
     * @return
     */
    public RpcModuleState startModule(String moduleName){
        //注册交易
        //transactionTools.registerTx(moduleName,200);
        SDKContext.addressPrefix = config.getAddressPrefix();
        return RpcModuleState.Running;
    }

}
