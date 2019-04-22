/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction;

import io.nuls.db.service.RocksDBService;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.modulebootstrap.Module;
import io.nuls.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.rpc.modulebootstrap.RpcModule;
import io.nuls.rpc.modulebootstrap.RpcModuleState;
import io.nuls.rpc.util.TimeUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.rpc.call.NetworkCall;
import io.nuls.transaction.utils.DBUtil;
import io.nuls.transaction.utils.LoggerUtil;

import java.util.Set;

import static io.nuls.transaction.utils.LoggerUtil.LOG;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author: Charlie
 * @date: 2019/3/4
 */
@Component
public class TransactionBootstrap extends RpcModule {

    @Autowired
    private TxConfig txConfig;

    @Autowired
    private ChainManager chainManager;

    public static void main(String[] args) {
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":8887/ws"};
        }
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    @Override
    public void init() {
        try {
            //初始化系统参数
            initSys();
            //初始化数据库配置文件
            initDB();
            chainManager.initChain();
        } catch (Exception e) {
            LOG.error("Transaction init error!");
            LOG.error(e);
        }
    }

    @Override
    public boolean doStart() {
        try {
            chainManager.runChain();
            while (!isDependencieReady(ModuleE.NW.abbr)){
                LOG.debug("wait depend modules ready");
                Thread.sleep(2000L);
            }
            LOG.info("Transaction Ready...");
            return true;
        } catch (Exception e) {
            LOG.error("Transaction init error!");
            LOG.error(e);
            return false;
        }
    }


    @Override
    public void onDependenciesReady(Module module) {
        try {
            if (ModuleE.NW.abbr.equals(module.getName())) {
                NetworkCall.registerProtocol();
            }
        } catch (NulsException e) {
            LoggerUtil.LOG.error(e);
        }
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        LOG.info("Transaction onDependenciesReady");
        TimeUtils.getInstance().start();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module dependenciesModule) {
        return RpcModuleState.Ready;
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                new Module(ModuleE.NW.abbr, TxConstant.RPC_VERSION),
                new Module(ModuleE.LG.abbr, TxConstant.RPC_VERSION),
                new Module(ModuleE.BL.abbr, TxConstant.RPC_VERSION),
                new Module(ModuleE.AC.abbr, TxConstant.RPC_VERSION)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.TX.abbr, TxConstant.RPC_VERSION);
    }

    @Override
    public Set<String> getRpcCmdPackage() {
        return Set.of(TxConstant.TX_CMD_PATH);
    }

    /**
     * 初始化系统编码
     */
    private void initSys() {
        try {
            System.setProperty(TxConstant.SYS_ALLOW_NULL_ARRAY_ELEMENT, "true");
            System.setProperty(TxConstant.SYS_FILE_ENCODING, UTF_8.name());
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    public void initDB() {
        try {
            //数据文件存储地址
            RocksDBService.init(txConfig.getTxDataRoot());
            //模块配置表
            DBUtil.createTable(TxDBConstant.DB_MODULE_CONGIF);
        } catch (Exception e) {
            LOG.error(e);
        }
    }


}
