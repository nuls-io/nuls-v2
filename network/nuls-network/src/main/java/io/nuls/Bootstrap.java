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
package io.nuls;


import io.nuls.db.service.RocksDBService;
import io.nuls.network.cfg.NulsConfig;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.*;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;
import io.nuls.tools.parse.I18nUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * boot strap
 * @author lan
 * @date 2018/11/01
 *
 */
public class Bootstrap {
    private static Bootstrap bootstrap = null;

    private Bootstrap() {
    }

    public static Bootstrap getInstance() {
        if (bootstrap == null) {
            synchronized (Bootstrap.class) {
                if (bootstrap == null) {
                    bootstrap = new Bootstrap();
                }
            }
        }
        return bootstrap;
    }


    public static void main(String[] args) {

        Bootstrap.getInstance().moduleStart();

    }

    public void moduleStart(){
        try {
            System.setProperty("io.netty.tryReflectionSetAccessible", "true");
//            --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
//            --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-exports java.base/sun.nio.ch=ALL-UNNAMED
            cfgInit();
            managerInit();
            managerStart();
        } catch (Exception e) {
            Log.error("Network Bootstrap failed", e);
            System.exit(-1);
        }
    }

    /**
     * 配置信息初始化
     *Configuration information initialization
     */
    public  void cfgInit() {
        try {
            NetworkParam networkParam = NetworkParam.getInstance();
            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConfig.MODULES_CONFIG_FILE);
            // set system language
            String language = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_LANGUAGE,"en");
            networkParam.setLanguage(language);
            I18nUtils.loadLanguage("languages", language);
            I18nUtils.setLanguage(language);
            //set encode
            String encoding = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_ENCODING,"UTF-8");
            networkParam.setEncoding(encoding);
            //set db path
            String dbPath =  NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_DBPATH,"./data");
            networkParam.setDbPath(dbPath);
            //net parameters
            networkParam.setChainId(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_CHAIN_ID, 0));
            networkParam.setPacketMagic(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_MAGIC, 123456789));
            networkParam.setMaxInCount(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_NODE_MAX_IN, 10));
            networkParam.setMaxOutCount(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_NODE_MAX_OUT, 100));
            networkParam.setMaxInSameIp((int)(networkParam.getMaxInCount()/networkParam.getMaxOutCount()));
            networkParam.setPort(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_SERVER_PORT, 8003));
            String seedIp = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_SEED_IP, "192.168.1.131:8003");
            List<String> ipList = new ArrayList<>();
            for (String ip : seedIp.split(NetworkConstant.COMMA)) {
                ipList.add(ip);
            }
            networkParam.setSeedIpList(ipList);
            //moon config
            networkParam.setMoonNode(Boolean.valueOf(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_MOON_NODE, false)));
            networkParam.setCrossMaxInCount(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_CROSS_NODE_MAX_IN, 1));
            networkParam.setCrossMaxOutCount(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_CROSS_NODE_MAX_OUT, 1));
            networkParam.setCorssMaxInSameIp((int)(networkParam.getCrossMaxInCount()/networkParam.getCrossMaxOutCount()));
            networkParam.setCrossPort(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_CROSS_SERVER_PORT, 0));
            String seedMoonIp = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_MOON_SEED_IP, "0.0.0.0:8003");
            List<String> ipMoonList = new ArrayList<>();
            for (String ip : seedMoonIp.split(NetworkConstant.COMMA)) {
                ipMoonList.add(ip);
            }
            networkParam.setSeedIpList(ipList);
        } catch (IOException e) {
            Log.error("Network Bootstrap cfgInit failed", e);
            throw new RuntimeException("Network Bootstrap cfgInit failed");
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }

    /**
     * 管理器初始化
     *Manager initialization
     */
    public  void managerInit(){
        RocksDBService.init(NetworkParam.getInstance().getDbPath());
        SpringLiteContext.init("io.nuls.network", new ModularServiceMethodInterceptor());
        StorageManager.getInstance().init();
        LocalInfoManager.getInstance().init();
        NodeGroupManager.getInstance().init();
        ConnectionManager.getInstance().init();
        MessageManager.getInstance().init();
        RpcManager.getInstance().init();
        TaskManager.getInstance().init();

    }

    /**
     *
     * 启动管理模块
     * Manager start
     */
    public  void managerStart(){
        Log.debug("managerStart begin=========");
        NodeGroupManager.getInstance().start();
        ConnectionManager.getInstance().start();
        TaskManager.getInstance().start();
        RpcManager.getInstance().start();
        Log.debug("managerStart end============");
    }

}
