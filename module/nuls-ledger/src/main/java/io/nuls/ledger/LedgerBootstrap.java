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

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.config.LedgerConfig;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.utils.LoggerUtil;

/**
 * @author: Niels Wang
 * @date: 2018/10/15
 */
@Component
public class LedgerBootstrap extends RpcModule {
    @Autowired
    LedgerConfig ledgerConfig;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    @Override
    public Module[] declareDependent() {

        return new Module[]{
                new Module(ModuleE.NW.abbr, "1.0")};

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
            LoggerUtil.logLevel = ledgerConfig.getLogLevel();
            LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME = ledgerConfig.getUnconfirmedTxExpired();
            LedgerConstant.DEFAULT_ENCODING = ledgerConfig.getEncoding();
            LedgerConstant.blackHolePublicKey = ByteUtils.toBytes(ledgerConfig.getBlackHolePublicKey(), LedgerConstant.DEFAULT_ENCODING);
            LedgerChainManager ledgerChainManager = SpringLiteContext.getBean(LedgerChainManager.class);
            ledgerChainManager.initChains();
            Log.info("Ledger data init  complete!");
        } catch (Exception e) {
            Log.error(e);
            Log.error("start fail...");
            System.exit(-1);
        }

    }

    @Override
    public boolean doStart() {
        //springLite容器初始化AppInitializing
        Log.info("Ledger READY");
        return true;
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        Log.info("Ledger onDependenciesReady");
        NulsDateUtils.getInstance().start(5 * 60 * 1000);
//        TaskManager.getInstance().start();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
