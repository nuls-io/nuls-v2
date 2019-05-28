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
package io.nuls.contract.manager;

import ch.qos.logback.classic.Level;
import io.nuls.base.protocol.ProtocolLoader;
import io.nuls.contract.config.ContractConfig;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.storage.ConfigStorageService;
import io.nuls.contract.util.Log;
import io.nuls.contract.util.LogUtil;
import io.nuls.contract.util.VMContext;
import io.nuls.contract.vm.program.ProgramExecutor;
import io.nuls.contract.vm.program.impl.ProgramExecutorImpl;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.service.RocksDBService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链管理类,负责各条链的初始化,运行,启动,参数维护等
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author: PierreLuo
 * @date: 2019-02-26
 */
@Component
public class ChainManager {

    @Autowired
    private VMContext vmContext;

    @Autowired
    private ConfigStorageService configStorageService;

    @Autowired
    private ContractConfig contractConfig;

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * 初始化并启动链
     * Initialize and start the chain
     */
    public void initChain() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            return;
        }
        /*
        根据配置信息创建初始化链/Initialize chains based on configuration information
        */
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            chain.setConfig(entry.getValue());
            /*
             * 初始化链数据库表/Initialize linked database tables
             */
            initTable(chain);
            /*
             * 初始化智能合约执行器
             */
            initContractExecutor(chain);
            /*
             * 初始化智能合约nrc20-token管理器
             */
            initTokenBalanceManager(chain);
            /*
             * 初始化智能合约创建合约未确认交易管理器
             */
            initContractTxCreateUnconfirmedManager(chain);
            /*
             * 初始化链日志
             */
            initContractChainLog(chainId);
            chainMap.put(chainId, chain);
            ProtocolLoader.load(chainId);
        }
    }

    private void initTokenBalanceManager(Chain chain) {
        ContractTokenBalanceManager tokenBalanceManager = ContractTokenBalanceManager.newInstance(chain.getChainId());
        chain.setContractTokenBalanceManager(tokenBalanceManager);
    }

    private void initContractTxCreateUnconfirmedManager(Chain chain) {
        ContractTxCreateUnconfirmedManager manager = ContractTxCreateUnconfirmedManager.newInstance(chain.getChainId());
        chain.setContractTxCreateUnconfirmedManager(manager);
    }

    private void initContractExecutor(Chain chain) {
        ProgramExecutor programExecutor = new ProgramExecutorImpl(vmContext, chain);
        chain.setProgramExecutor(programExecutor);
    }

    /**
     * 初始化链日志
     */
    private void initContractChainLog(int chainId) {
        Level fileLevel = Level.toLevel(contractConfig.getLogFileLevel());
        Level consoleLevel = Level.toLevel(contractConfig.getLogConsoleLevel());
        LogUtil.configChainLog(chainId, ContractConstant.LOG_FILE_FOLDER, ContractConstant.LOG_FILE_NAME, fileLevel, consoleLevel);
    }

    /**
     * 停止一条链
     * Delete a chain
     *
     * @param chainId 链ID/chain id
     */
    public void stopChain(int chainId) {

    }


    /**
     * 读取配置文件创建并初始化链
     * Read the configuration file to create and initialize the chain
     */
    private Map<Integer, ConfigBean> configChain() {
        try {
            /*
            读取数据库链信息配置/Read database chain information configuration
             */
            Map<Integer, ConfigBean> configMap = configStorageService.getList();
            /*
            如果系统是第一次运行，则本地数据库没有存储链信息，此时需要从配置文件读取主链配置信息
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if (configMap == null || configMap.size() == 0) {
                //String configJson = IoUtils.read(ContractConstant.CONFIG_FILE_PATH);
                //List<ConfigItem> configItemList = JSONUtils.json2list(configJson, ConfigItem.class);
                //ConfigBean configBean = ConfigManager.initManager(configItemList);
                ConfigBean configBean = contractConfig.getChainConfig();
                if (configBean == null) {
                    return null;
                }
                configMap.put(configBean.getChainId(), configBean);
            }
            return configMap;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    /**
     * 初始化链相关表
     * Initialization chain correlation table
     *
     * @param chain
     */
    private void initTable(Chain chain) {
        int chainId = chain.getConfig().getChainId();
        try {
            // 合约地址表
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + "_" + chainId);
            // 合约地址与交易关联表
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_LEDGER_TX_INDEX + "_" + chainId);
            // 合约内部转账表
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_TRANSFER_TX + "_" + chainId);
            // 执行结果表
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_EXECUTE_RESULT + "_" + chainId);
            // 收藏地址表
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_COLLECTION + "_" + chainId);
            // nrc20-token地址表
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_NRC20_TOKEN_ADDRESS + "_" + chainId);
            // nrc20-token转账表
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_NRC20_TOKEN_TRANSFER + "_" + chainId);

        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
            }
        }
    }

    public static void chainHandle(int chainId) {
        // 设置日志分链打印
        Log.currentThreadChainId(chainId);
    }


    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }
}
