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

import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.tools.parse.config.IniEntity;
import io.nuls.tools.thread.ThreadUtils;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
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
 * 区块管理模块启动类
 * Block module startup class
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 上午10:20
 */
@Slf4j
public class MyKernelBootstrap {

    private static List<String> MODULE_STOP_LIST_SCRIPT = new ArrayList<>();

    public static void main(String[] args) throws Exception {
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
        startOtherModule(args);
        NoUse.mockKernel();
    }

    /**
     * 启动所有子模块
     * 遍历/Modules/Nuls目录，通过判断目录中是否存在module.ncf文件来确定当前是否为可启动模块
     * 找到模块后，调用./start.sh脚本启动子模块
     * @param args
     */
    private static void startOtherModule(String[] args) {
        //启动时第一个参数值为"startModule"时启动所有子模块
        if (args.length > 0 && "startModule".equals(args[0])) {
            ThreadUtils.createAndRunThread("startModule",()->{
                try {
                    //等待mykernel启动完毕
                    while (!ConnectManager.isReady()) {
                        TimeUnit.SECONDS.sleep(1);
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
    private static void findModule(File modules) throws Exception {
        if (modules.isFile()) {
            return;
        }
        if (Arrays.stream(modules.listFiles()).anyMatch(f -> "module.ncf".equals(f.getName()))) {
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
    private static void startModule(File modules) throws Exception {
        Config cfg = new Config();
        cfg.setMultiSection(true);
        Ini ini = new Ini();
        ini.setConfig(cfg);
        ini.load(new File(modules.getAbsolutePath() + File.separator + "module.ncf"));
        IniEntity ie = new IniEntity(ini);
        String managed = ie.getCfgValue("Core", "Managed");
        if ("1".equals(managed)) {
            ThreadUtils.createAndRunThread("module-start", () -> {
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec(
                            modules.getAbsolutePath() + File.separator + "start.sh "
                                    + " --jre " + System.getProperty("java.home")
                                    + " --managerurl " + "127.0.0.1:8887"
                    );
                    synchronized (MODULE_STOP_LIST_SCRIPT){
                        MODULE_STOP_LIST_SCRIPT.add(modules.getAbsolutePath() + File.separator + "stop.sh ");
                    }
                    printRuntimeConsole(process);
                } catch (IOException e) {
                    log.error("启动模块异常",e);
                }
            });
        }
    }

    private static void printRuntimeConsole(Process process) throws IOException {
        @Cleanup BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;
        while ((line = input.readLine()) != null) {
            log.info(line);
        }
    }

}
