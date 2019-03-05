package io.nuls.rpc.modulebootstrap;

import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.ThreadUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

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

    public static void run(String[] args) {
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
        ThreadUtils.createAndRunThread(module.moduleInfo().getName()+"-thread",()->{
            module.run(scanPackage,"ws://" + args[0]);
        });
        if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
            System.setProperty("jline.WindowsTerminal.directConsole", "false");
        }
        BufferedReader is_reader = new BufferedReader(new InputStreamReader(System.in));
        while(true){
            try {
                String cmd = is_reader.readLine();
                System.out.println(cmd);
                switch (cmd){
                    case "f":
                        System.out.println("模块的追随者：");
                        module.getFollowerList().forEach(System.out::println);
                        break;
                    case "d":
                        System.out.println("依赖的模块列表");
                        Arrays.stream(module.getDependencies()).forEach(d->{
                            System.out.println(d.name + " is ready : " + module.isDependencieReady(d));
                        });
                        break;
                    case "s":
                        System.out.println("当前状态："+module.getState());
                        break;
                        default:
                            System.out.println("错误的输入,请输入f,d,s");
                            break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
