/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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


import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.network.constant.ManagerStatusEnum;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.*;
import io.nuls.network.storage.InitDB;
import io.nuls.network.storage.impl.DbServiceImpl;
import io.nuls.network.utils.IpUtil;
import io.nuls.network.utils.LoggerUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * boot strap
 *
 * @author lan
 * @date 2018/11/01
 */
@Component
public class NetworkBootstrap implements INulsCoresBootstrap {
    @Autowired
    NulsCoresConfig networkConfig;
    private boolean hadRun = false;

    @Override
    public int order() {
        return 2;
    }

    @Override
    public void mainFunction(String[] args) {
        this.init();
    }

    private boolean validatCfg() {
        if (networkConfig.getPacketMagic() > NetworkConstant.MAX_NUMBER_4_BYTE) {
            Log.error("Network cfg error.packageMagic:{}>{}", networkConfig.getPacketMagic(), NetworkConstant.MAX_NUMBER_4_BYTE);
            return false;
        }
        if (networkConfig.getChainId() > NetworkConstant.MAX_NUMBER_2_BYTE) {
            Log.error("Network cfg error.chainId:{}>{}", networkConfig.getChainId(), NetworkConstant.MAX_NUMBER_2_BYTE);
            return false;
        }
        if (networkConfig.getPort() > NetworkConstant.MAX_NUMBER_2_BYTE) {
            Log.error("Network cfg error.port:{}>{}", networkConfig.getPort(), NetworkConstant.MAX_NUMBER_2_BYTE);
            return false;
        }
        if (networkConfig.getCrossPort() > NetworkConstant.MAX_NUMBER_2_BYTE) {
            Log.error("Network cfg error.crossPort:{}>{}", networkConfig.getCrossPort(), NetworkConstant.MAX_NUMBER_2_BYTE);
            return false;
        }
        return true;
    }

    /**
     * 配置信息初始化
     * Configuration information initialization
     */
    private void jsonCfgInit() throws Exception {
        try {
            String seedIps = networkConfig.getSelfSeedIps();
            List<String> ipList = new ArrayList<>();
            if (StringUtils.isNotBlank(seedIps)) {
                String[] seedIpsArray = seedIps.split(NetworkConstant.COMMA);
                for (String seedIp : seedIpsArray) {
                    seedIp = IpUtil.changeHostToIpStr(seedIp);
                    if (null != seedIp) {
                        ipList.add(seedIp);
                    }
                }
                networkConfig.setSeedIpList(ipList);
            }
            if (networkConfig.getMainChainId() == networkConfig.getChainId()) {
                networkConfig.setMoonNode(true);
            } else {
                networkConfig.setMoonNode(false);
            }

            networkConfig.setMaxInSameIp(NetworkConstant.MAX_SAME_IP_PER_GROUP);
            networkConfig.setCrossMaxInSameIp(NetworkConstant.MAX_SAME_IP_PER_GROUP);
            String seedMoonIp = networkConfig.getMoonSeedIps();
            List<String> ipMoonList = new ArrayList<>();
            Collections.addAll(ipMoonList, seedMoonIp.split(NetworkConstant.COMMA));
            networkConfig.setMoonSeedIpList(ipMoonList);
            networkConfig.getLocalIps().addAll(IpUtil.getIps());
        } catch (Exception e) {
            Log.error("Network NetworkBootstrap cfgInit failed", e);
            throw new RuntimeException("Network NetworkBootstrap cfgInit failed");
        }
    }

    private void dbInit() throws Exception {
        RocksDBService.init(networkConfig.getDataPath() + File.separator + ModuleE.NW.name);
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
        ConnectionManager.getInstance().init();
        TaskManager.getInstance().init();

    }

    public void init() {
        try {
            System.setProperty("io.netty.tryReflectionSetAccessible", "true");
            if (!validatCfg()) {
                System.exit(-1);
            }
            jsonCfgInit();
            dbInit();
            managerInit();
        } catch (Exception e) {
            Log.error(e);
            Log.error("exit,start fail...");
            System.exit(-1);
        }

    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.NW.abbr, "1.0");
    }

    public boolean doStart() {
        Log.info("doStart begin=========");
        NodeGroupManager.getInstance().start();
        LoggerUtil.COMMON_LOG.info("doStart end=========");
        return true;
    }

    /**
     * onDependenciesReady 会在依赖模块重新ready后再次执行
     *
     * @return
     */
    @Override
    public void onDependenciesReady() {
        LoggerUtil.COMMON_LOG.info("network onDependenciesReady");
        try {
            doStart();
            if (!hadRun) {
                ConnectionManager.getInstance().start();
                TaskManager.getInstance().start();
                hadRun = true;
            } else {
                //恢复连接
                ConnectionManager.getInstance().change(ManagerStatusEnum.RUNNING);
                NodeGroupManager.getInstance().change(ManagerStatusEnum.RUNNING);
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            LoggerUtil.COMMON_LOG.error("exit,start fail...");
            System.exit(-1);
        }
        LoggerUtil.COMMON_LOG.info("network RUNNING......");
    }

}
