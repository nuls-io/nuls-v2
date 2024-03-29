package io.nuls.core.rpc.modulebootstrap;

import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.thread.ThreadUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * @Author: zhoulijun
 * @Time: 2019-02-28 14:27
 * @Description: Function Description
 */
public class NulsRpcModuleBootstrap {

    private static final String DEFAULT_SCAN_PACKAGE = "io.nuls";

    private static boolean printLogoed = false;

    public static void main(String[] args) {
        NulsRpcModuleBootstrap.run(args);
    }

    public static void run(String[] args) {
        run(DEFAULT_SCAN_PACKAGE, args);
    }

    public static void run(String scanPackage, String[] args) {
        printLogo("/logo");
        Log.info("RUN MODULE:{}",System.getProperty("app.name"));
        SpringLiteContext.init(scanPackage, "io.nuls.core.rpc.modulebootstrap", "io.nuls.core.rpc.cmd", "io.nuls.base.protocol");
        RpcModule module;
        try {
            module = SpringLiteContext.getBean(RpcModule.class);
        } catch (NulsRuntimeException e) {
            Log.error("loadingRpcModuleImplementation class failure for");
            return;
        }

        String debug = "0";
        if (args.length > 1) {
            debug = args[1];
        }
        if ("1".equals(debug)) {
            ThreadUtils.createAndRunThread(module.moduleInfo().getName() + "-thread", () -> {
                BufferedReader is_reader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    try {
                        String cmd = is_reader.readLine();
                        if (cmd == null) {
                            break;
                        }
                        switch (cmd) {
                            case "f":
                                System.out.println("Followers of modules：");
                                module.getFollowerList().entrySet().forEach(System.out::println);
                                break;
                            case "d":
                                System.out.println("List of dependent modules");
                                module.getDependencies().forEach(d -> {
                                    System.out.println(d.name + " is ready : " + module.isDependencieReady(d));
                                });
                                break;
                            case "s":
                                System.out.println("current state：" + module.getState());
                                break;
                            default:
                                System.out.println("Wrong input,Please enterf,d,s");
                                break;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        module.run(scanPackage, args[0],args);
    }

    public static void printLogo(String logoFile) {
        if(printLogoed) {
            return ;
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(Class.forName(NulsRpcModuleBootstrap.class.getName()).getResourceAsStream(logoFile)))){
            String line = reader.readLine();
            while(line != null){
                System.out.println(line);
                line = reader.readLine();
            }
            System.out.println("Module:" + System.getProperty("app.name"));
            System.out.println();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(0);
        }
        printLogoed = true;
    }

}
