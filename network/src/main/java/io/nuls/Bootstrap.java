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


import io.nuls.network.cfg.ConfigLoader;
import io.nuls.network.cfg.NulsConfig;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.*;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.tools.log.Log;

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
    public static void main(String[] args) {
        Thread.currentThread().setName("NulsNetwork");

        try {
            System.setProperty("io.netty.tryReflectionSetAccessible", "true");
//            --add-exports java.base/jdk.internal.misc=ALL-UNNAMED
//            --add-exports java.base/jdk.internal.ref=ALL-UNNAMED --add-exports java.base/sun.nio.ch=ALL-UNNAMED
//            System.setProperty("protostuff.runtime.allow_null_array_element", "true");
            cfgInit();
            cfgInit();
            managerInit();
            managerStart();
            startNetty();
            //TODO:模块相关状态维护待写
        } catch (Exception e) {
            Log.error("Network Bootstrap failed", e);
            System.exit(-1);
        }
    }

    public static void cfgInit() {
        try {

            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConfig.MODULES_CONFIG_FILE);
            NetworkParam networkParam = NetworkParam.getInstance();
            networkParam.setChainId(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_CHAIN_ID, 0));
            networkParam.setPacketMagic(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_MAGIC, 123456789));
            networkParam.setMaxInCount(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_NODE_MAX_IN, 10));
            networkParam.setMaxOutCount(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_NODE_MAX_OUT, 100));
            networkParam.setMaxInSameIp((int)(networkParam.getMaxInCount()/networkParam.getMaxOutCount()));
            networkParam.setPort(NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_SERVER_PORT, 8003));
            String seedIp = NulsConfig.MODULES_CONFIG.getCfgValue(NetworkConstant.NETWORK_SECTION, NetworkConstant.NETWORK_SELF_SEED_IP, "192.168.1.131:8003");
            List<String> ipList = new ArrayList<>();
            for (String ip : seedIp.split(",")) {
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
            for (String ip : seedMoonIp.split(",")) {
                ipMoonList.add(ip);
            }
            networkParam.setSeedIpList(ipList);
            Log.info(String.valueOf(networkParam.getPacketMagic()));
        } catch (IOException e) {
            Log.error("Network Bootstrap cfgInit failed", e);
            throw new RuntimeException("Network Bootstrap cfgInit failed");
        }
    }
    public static void managerInit(){
        NodeGroupManager.getInstance().init();
        NodeManager.getInstance().init();
        MessageManager.getInstance().init();
        LocalInfoManager.getInstance().init();
    }

    public static void managerStart(){
        NodeGroupManager.getInstance().start();
        NodeManager.getInstance().start();
    }


    public static void startNetty(){

        ConnectionManager.getInstance().nettyBoot();
       Log.info("==========================NettyStart");
    }

}
