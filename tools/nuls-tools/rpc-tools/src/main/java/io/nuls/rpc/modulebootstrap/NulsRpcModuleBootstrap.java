package io.nuls.rpc.modulebootstrap;

import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

/**
 * @Author: zhoulijun
 * @Time: 2019-02-28 14:27
 * @Description: 功能描述
 */
public class NulsRpcModuleBootstrap {

    private static final String DEFAULT_SCAN_PACKAGE = "io.nuls" ;

    public static void main(String[] args) {
        NulsRpcModuleBootstrap.run(args);
    }

    public static void run(String[] args){
        run(DEFAULT_SCAN_PACKAGE,args);
    }

    public static void run(String scanPackage,String[] args){
        SpringLiteContext.init(scanPackage,"io.nuls.rpc.modulebootstrap");
        RpcModule module;
        try {
            module = SpringLiteContext.getBean(RpcModule.class);
        }catch(NulsRuntimeException e){
            Log.error("未找到到RpcModule的实现类");
            return ;
        }
        module.run(scanPackage,"ws://" + args[0]);
    }

}
