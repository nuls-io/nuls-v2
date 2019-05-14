/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.mykernel;

import io.nuls.core.basic.ModuleConfig;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.annotation.Configuration;
import io.nuls.core.core.annotation.Value;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.log.logback.LoggerBuilder;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.config.IniEntity;
import io.nuls.core.rpc.info.NoUse;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.thread.ThreadUtils;
import lombok.Cleanup;
import lombok.Setter;
import org.ini4j.Config;
import org.ini4j.Ini;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 虚拟核心模块启动类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 上午10:20
 */
@Component
@Setter
@Configuration(domain = ModuleE.Constant.KERNEL)
public class MyKernelBootstrap implements ModuleConfig {

    @Value("logLevel")
    private String logLevel = "INFO";

    @Value("logPath")
    private String logPath;

    @Value("dataPath")
    private String dataPath;

    @Value("debug")
    private int debug;

    @Value("active.module")
    private String config;

    private static List<String> MODULE_STOP_LIST_SCRIPT = new ArrayList<>();

    static String[] args;

    static NulsLogger log = LoggerBuilder.getLogger("kernel");

    public static void main(String[] args) throws Exception {
        NulsRpcModuleBootstrap.printLogo("/logo");
        System.setProperty("io.netty.tryReflectionSetAccessible", "true");
        MyKernelBootstrap.args = args;
        SpringLiteContext.init("io.nuls.mykernel","io.nuls.core.rpc.cmd.kernel");
        MyKernelBootstrap bootstrap = SpringLiteContext.getBean(MyKernelBootstrap.class);
        bootstrap.doStart();
    }

    /**
     * 启动所有子模块
     * 遍历/Modules/Nuls目录，通过判断目录中是否存在module.ncf文件来确定当前是否为可启动模块
     * 找到模块后，调用./start.sh脚本启动子模块
     * @param args
     */
    private void startOtherModule(String[] args) {
        //启动时第一个参数值为"startModule"时启动所有子模块
        if (args.length > 0 && "startModule".equals(args[0])) {
            //增加程序结束的钩子，监听到主线程停止时，调用./stop.sh停止所有的子模块
            Runtime.getRuntime().addShutdownHook(new Thread(()->{
                log.info("jvm shutdown");
                log.info("停止子模块");
                log.info("停止脚本列表");
                MODULE_STOP_LIST_SCRIPT.stream().forEach(log::info);
                MODULE_STOP_LIST_SCRIPT.stream().forEach(stop->{
                    try {
                        printRuntimeConsole(Runtime.getRuntime().exec(stop));
                        log.info("停止子模块:{}",stop);
                    } catch (IOException e) {
                        log.error("调用脚本停止模块出现异常：{}",stop);
                    }
                });
            }));
            ThreadUtils.createAndRunThread("startModule",()->{
                try {
                    //等待mykernel启动完毕
                    while (!ConnectManager.isReady()) {
                        TimeUnit.SECONDS.sleep(5);
                    }
                    //获取Modules目录
                    File modules = new File(args[1]);
                    //遍历modules目录查找带有module.ncf文件的目录
                    try{
                        findModule(modules);
                    }catch (Exception e1){
                        log.error("启动模块发生错误:{}", modules.getName());
                    }
                }catch(Exception e){
                    log.error("启动模块发生错误");
                }
            });

        }
    }

    /**
     * 递归遍历Modules目录
     * @param modules
     * @throws Exception
     */
    private void findModule(File modules) throws Exception {
        if (modules.isFile()) {
            return;
        }
        if (Arrays.stream(modules.listFiles()).anyMatch(f -> "Module.ncf".equals(f.getName())) && Arrays.stream(modules.listFiles()).anyMatch(f -> f.getName().endsWith("jar"))) {
            startModule(modules);
            return;
        }
        Arrays.stream(modules.listFiles()).forEach(f -> {
            try {
                findModule(f);
            } catch (Exception e) {
                log.error("启动模块发生错误:{}", f.getName(), e);
            }
        });
    }

    /**
     * 启动模块
     * @param modules
     * @throws Exception
     */
    private void startModule(File modules) throws Exception {
        Config cfg = new Config();
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        ini.load(new File(modules.getAbsolutePath() + File.separator + "Module.ncf"));
        IniEntity ie = new IniEntity(ini);
        String managed = ie.getCfgValue("Core", "Managed");
        if ("1".equals(managed)) {
            ThreadUtils.createAndRunThread("module-start", () -> {
                Process process = null;
                try {
                    String cmd = modules.getAbsolutePath() + File.separator + "start.sh "
                            + " --jre " + System.getProperty("java.home")
                            + " --managerurl " + "ws://127.0.0.1:7771/ "
                            + (StringUtils.isNotBlank(logPath) ? " --logpath " + logPath: "")
                            + (StringUtils.isNotBlank(dataPath) ? " --datapath " + dataPath : "")
                            + (StringUtils.isNotBlank(logLevel) ? " --loglevel " + logLevel : "")
                            + " --debug " + debug
                            + (StringUtils.isNotBlank(config) ? " --config " + config : "")
                            + " -r ";
                    Log.info("run script:{}",cmd);
                    process = Runtime.getRuntime().exec(cmd);
                    synchronized (MODULE_STOP_LIST_SCRIPT){
                        MODULE_STOP_LIST_SCRIPT.add(modules.getAbsolutePath() + File.separator + "stop ");
                    }
                    printRuntimeConsole(process);
                } catch (IOException e) {
                    log.error("启动模块异常",e);
                }
            });
        }
    }

    private void printRuntimeConsole(Process process) throws IOException {
        @Cleanup BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            log.info(line);
        }
    }

    public boolean doStart() {
        startOtherModule(args);
        int port = 7771;
        String host = "0.0.0.0";
        String path = "/";
        try {
            NoUse.startKernel(host, port, path);
        } catch (Exception e) {
            log.error("mykernel start fail",e);
        }
        log.info("MYKERNEL STARTED. URL: ws://{}{}", host + ":" + port, path);
        return false;
    }

}
