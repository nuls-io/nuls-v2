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
import io.nuls.network.cfg.NulsConfig;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.*;
import io.nuls.network.storage.InitDB;
import io.nuls.network.storage.impl.DbServiceImpl;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;
import io.nuls.tools.parse.config.ConfigManager;

import java.io.IOException;
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
public class NetworkBootstrap {
    private static NetworkBootstrap bootstrap = null;

    private NetworkBootstrap() {
    }

    public static NetworkBootstrap getInstance() {
        if (bootstrap == null) {
            synchronized (NetworkBootstrap.class) {
                if (bootstrap == null) {
                    bootstrap = new NetworkBootstrap();
                }
            }
        }
        return bootstrap;
    }


    public static void main(String[] args) {

        NetworkBootstrap.getInstance().moduleStart();

    }

    public void moduleStart() {
        try {
            System.setProperty("io.netty.tryReflectionSetAccessible", "true");
//            --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
//            --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-exports java.base/sun.nio.ch=ALL-UNNAMED
            SpringLiteContext.init("io.nuls.network", new ModularServiceMethodInterceptor());
            jsonCfgInit();
            dbInit();
            managerInit();
            managerStart();
        } catch (Exception e) {
            Log.error("Network NetworkBootstrap failed", e);
            System.exit(-1);
        }
    }

    /**
     * 配置信息初始化
     * Configuration information initialization
     */
    private void jsonCfgInit() throws Exception {
        try {
            NetworkParam networkParam = NetworkParam.getInstance();
            ConfigLoader.loadJsonCfg(NulsConfig.MODULES_CONFIG_FILE);
            // set system language
            String language = ConfigManager.getValue(NetworkConstant.NETWORK_LANGUAGE);
            networkParam.setLanguage(language);
            I18nUtils.loadLanguage(NetworkBootstrap.class,"languages", language);
            I18nUtils.setLanguage(language);
            //set encode
            String encoding = ConfigManager.getValue(NetworkConstant.NETWORK_ENCODING);
            networkParam.setEncoding(encoding);
            //set storage path
            String dbPath = ConfigManager.getValue(NetworkConstant.NETWORK_DBPATH);
            networkParam.setDbPath(dbPath);
            //net parameters
            networkParam.setChainId(Integer.valueOf(ConfigManager.getValue(NetworkConstant.NETWORK_SELF_CHAIN_ID)));
            networkParam.setPacketMagic(Long.valueOf(ConfigManager.getValue(NetworkConstant.NETWORK_SELF_MAGIC)));
            networkParam.setMaxInCount(Integer.valueOf(ConfigManager.getValue(NetworkConstant.NETWORK_SELF_NODE_MAX_IN)));
            networkParam.setMaxOutCount(Integer.valueOf(ConfigManager.getValue(NetworkConstant.NETWORK_SELF_NODE_MAX_OUT)));
            networkParam.setMaxInSameIp(networkParam.getMaxInCount() / networkParam.getMaxOutCount());
            networkParam.setPort(Integer.valueOf(ConfigManager.getValue(NetworkConstant.NETWORK_SELF_SERVER_PORT)));
            String seedIp = ConfigManager.getValue(NetworkConstant.NETWORK_SELF_SEED_IP);
            List<String> ipList = new ArrayList<>();
            Collections.addAll(ipList, seedIp.split(NetworkConstant.COMMA));
            networkParam.setSeedIpList(ipList);
            //moon config
            networkParam.setMoonNode(Boolean.valueOf(ConfigManager.getValue((NetworkConstant.NETWORK_MOON_NODE))));
            networkParam.setCrossMaxInCount(Integer.valueOf(ConfigManager.getValue(NetworkConstant.NETWORK_CROSS_NODE_MAX_IN)));
            networkParam.setCrossMaxOutCount(Integer.valueOf(ConfigManager.getValue(NetworkConstant.NETWORK_CROSS_NODE_MAX_OUT)));
            networkParam.setCorssMaxInSameIp((networkParam.getCrossMaxInCount() / networkParam.getCrossMaxOutCount()));
            networkParam.setCrossPort(Integer.valueOf(ConfigManager.getValue(NetworkConstant.NETWORK_CROSS_SERVER_PORT)));
            String seedMoonIp = ConfigManager.getValue(NetworkConstant.NETWORK_MOON_SEED_IP);
            List<String> ipMoonList = new ArrayList<>();
            Collections.addAll(ipMoonList, seedMoonIp.split(NetworkConstant.COMMA));
            networkParam.setMoonSeedIpList(ipMoonList);
        } catch (IOException e) {
            Log.error("Network NetworkBootstrap cfgInit failed", e);
            throw new RuntimeException("Network NetworkBootstrap cfgInit failed");
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }
     private void dbInit()throws Exception{
         RocksDBService.init(NetworkParam.getInstance().getDbPath());
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
     * 启动管理模块
     * Manager start
     */
    private void managerStart() throws Exception {
        Log.debug("managerStart begin=========");
        NodeGroupManager.getInstance().start();
        RpcManager.getInstance().start();
        ConnectionManager.getInstance().start();
        TaskManager.getInstance().start();
        Log.debug("managerStart end============");
    }

}
