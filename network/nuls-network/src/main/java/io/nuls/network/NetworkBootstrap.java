/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.network;


import io.nuls.db.service.RocksDBService;
import io.nuls.network.cfg.NetworkConfig;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.*;
import io.nuls.network.storage.InitDB;
import io.nuls.network.storage.impl.DbServiceImpl;
import io.nuls.network.utils.IpUtil;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Configuration;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.parse.I18nUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * boot strap
 *
 * @author lan
 * @date 2018/11/01
 */
@Configuration
public class NetworkBootstrap extends RpcModule {
    @Autowired
    NetworkConfig networkConfig;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":8887/ws"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    /**
     * 配置信息初始化
     * Configuration information initialization
     */
    private void jsonCfgInit() throws Exception {
        try {
            I18nUtils.loadLanguage(NetworkBootstrap.class, "languages", networkConfig.getLanguage());
            String seedIp = networkConfig.getSelfSeedIps();
            List<String> ipList = new ArrayList<>();
            Collections.addAll(ipList, seedIp.split(NetworkConstant.COMMA));
            networkConfig.setSeedIpList(ipList);
            networkConfig.setMaxInSameIp((networkConfig.getMaxInCount() / networkConfig.getMaxOutCount()));
            networkConfig.setCorssMaxInSameIp((networkConfig.getCrossMaxInCount() / networkConfig.getCrossMaxOutCount()));
            String seedMoonIp = networkConfig.getMoonSeedIps();
            List<String> ipMoonList = new ArrayList<>();
            Collections.addAll(ipMoonList, seedMoonIp.split(NetworkConstant.COMMA));
            networkConfig.setMoonSeedIpList(ipMoonList);
            networkConfig.getLocalIps().addAll(IpUtil.getIps());
        } catch (Exception e) {
            e.printStackTrace();
            Log.error("Network NetworkBootstrap cfgInit failed", e);
            throw new RuntimeException("Network NetworkBootstrap cfgInit failed");
        }
    }

    private void dbInit() throws Exception {
        RocksDBService.init(networkConfig.getDataPath()+NetworkConstant.MODULE_DB_PATH);
        InitDB dbService = SpringLiteContext.getBean(DbServiceImpl.class);
        dbService.initTableName();
    }

    /**
     * 管理器初始化
     * Manager initialization
     */
    private void managerInit() throws Exception {
        StorageManager.getInstance().init();
        NodeGroupManager.getInstance().init();
        MessageManager.getInstance().init();
        RpcManager.getInstance().init();
        ConnectionManager.getInstance().init();
        TaskManager.getInstance().init();

    }

    /**
     * 初始化模块信息，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        try {
            super.init();
            System.setProperty("io.netty.tryReflectionSetAccessible", "true");
//            --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
//            --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-exports java.base/sun.nio.ch=ALL-UNNAMED
            jsonCfgInit();
            dbInit();
            managerInit();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }

    @Override
    public Module[] getDependencies() {
        return new Module[]{new Module(ModuleE.BL.abbr, "1.0")};
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.NW.abbr, "1.0");
    }

    @Override
    public boolean doStart() {
        Log.debug("doStart begin=========");
        LoggerUtil.Log.info("NW doStart 1");
        try {
            NodeGroupManager.getInstance().start();
//            RpcManager.getInstance().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Log.debug("doStart end=========");
        LoggerUtil.Log.info("NW doStart 2");
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        LoggerUtil.Log.info("network onDependenciesReady");
        try {
            ConnectionManager.getInstance().start();
            TaskManager.getInstance().start();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
        LoggerUtil.Log.info("NW RUNNING");
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
