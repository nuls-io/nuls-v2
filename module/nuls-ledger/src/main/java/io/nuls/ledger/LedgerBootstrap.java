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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.protocol.ProtocolLoader;
import io.nuls.base.protocol.RegisterHelper;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.util.AddressPrefixDatas;
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
    @Autowired
    AddressPrefixDatas addressPrefixDatas;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    @Override
    public Module[] declareDependent() {

        return new Module[]{
                Module.build(ModuleE.TX),
                Module.build(ModuleE.NW),
                Module.build(ModuleE.BL),
                Module.build(ModuleE.AC)
        };

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
            //增加地址工具类初始化
            AddressTool.init(addressPrefixDatas);
            LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME = ledgerConfig.getUnconfirmedTxExpired();
            LedgerConstant.DEFAULT_ENCODING = ledgerConfig.getEncoding();
            LedgerConstant.blackHolePublicKey = HexUtil.decode(ledgerConfig.getBlackHolePublicKey());
            LedgerChainManager ledgerChainManager = SpringLiteContext.getBean(LedgerChainManager.class);
            ledgerChainManager.initChains();
            LoggerUtil.COMMON_LOG.info("Ledger data init complete!");
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            LoggerUtil.COMMON_LOG.error("start fail...");
            System.exit(-1);
        }

    }

    @Override
    public boolean doStart() {
        //springLite容器初始化AppInitializing
        LoggerUtil.COMMON_LOG.info("Ledger READY");
        return true;
    }

    @Override
    public void onDependenciesReady(Module module) {
        try {
            ProtocolLoader.load(ledgerConfig.getChainId());
            /*注册交易处理器*/
            if (ModuleE.TX.abbr.equals(module.getName())) {
                int chainId = ledgerConfig.getChainId();
                boolean regSuccess = RegisterHelper.registerTx(chainId, ProtocolGroupManager.getCurrentProtocol(chainId));
                if (!regSuccess) {
                    LoggerUtil.COMMON_LOG.error("RegisterHelper.registerTx fail..");
                    System.exit(-1);
                }
                LoggerUtil.COMMON_LOG.info("regTxRpc complete.....");
            }
            if (ModuleE.PU.abbr.equals(module.getName())) {
                //注册相关交易
                boolean regSuccess = RegisterHelper.registerProtocol(ledgerConfig.getChainId());
                if (!regSuccess) {
                    LoggerUtil.COMMON_LOG.error("RegisterHelper.registerProtocol fail..");
                    System.exit(-1);
                }
                LoggerUtil.COMMON_LOG.info("register protocol ...");
            }
            /*处理区块信息*/
            if (ModuleE.BL.abbr.equals(module.getName())) {
                LedgerChainManager ledgerChainManager = SpringLiteContext.getBean(LedgerChainManager.class);
                ledgerChainManager.syncBlockHeight();
            }

        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            System.exit(-1);

        }
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        LoggerUtil.COMMON_LOG.info("Ledger onDependenciesReady");
        NulsDateUtils.getInstance().start(5 * 60 * 1000);
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }
}
