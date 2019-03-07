package io.nuls.cmd.client;

import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 17:07
 * @Description: 功能描述
 */
public class CmdClientBootstrap {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{HostInfo.getLocalIP() + ":8887/ws","0"};
        }else{
            args = new String[]{args[0] + ":8887/ws","0"};
        }
        NulsRpcModuleBootstrap.run("io.nuls.cmd.client",args);
    }

}
