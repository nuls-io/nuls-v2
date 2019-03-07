/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.config.AppConfig;
import io.nuls.ledger.model.ModuleConfig;
import io.nuls.ledger.service.BlockDataService;
import io.nuls.ledger.service.impl.BlockDataServiceImpl;
import io.nuls.ledger.storage.InitDB;
import io.nuls.ledger.storage.impl.RepositoryImpl;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * @author: Niels Wang
 * @date: 2018/10/15
 */
@Component
public class LedgerBootstrap extends RpcModule {

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{HostInfo.getLocalIP() + ":8887/ws"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }
    /**
     * 进行数据的校验处理,比如异常关闭模块造成的数据不一致。
     * 确认的高度是x,则进行x高度的数据恢复处理
     */
    public static void initLedgerDatas() throws Exception {
        BlockDataService blockDataService = SpringLiteContext.getBean(BlockDataServiceImpl.class);
        blockDataService.initBlockDatas();
    }

    /**
     * 初始化数据库
     */
    public static void initRocksDb() {
        try {
            RocksDBService.init(ModuleConfig.getInstance().getDatabaseDir());
            InitDB initDB = SpringLiteContext.getBean(RepositoryImpl.class);
            initDB.initTableName();
        } catch (Exception e) {
            logger.error(e);
        }
    }

    @Override
    public Module[] getDependencies() {
        return new Module[]{new Module(ModuleE.NW.abbr, "1.0")};
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.LG.abbr, "1.0");
    }
    /**
     * 初始化模块信息，比如初始化RockDB等，在此处初始化后，可在其他bean的afterPropertiesSet中使用
     */
    @Override
    public void init() {
        try {
            super.init();
            AppConfig.loadModuleConfig();
            initRocksDb();
            initLedgerDatas();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

    }
    @Override
    public boolean doStart() {
        //springLite容器初始化AppInitializing
        LoggerUtil.logger.info("Ledger READY");
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        LoggerUtil.logger.info("Ledger onDependenciesReady");
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
