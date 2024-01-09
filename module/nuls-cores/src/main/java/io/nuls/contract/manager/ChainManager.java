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
package io.nuls.contract.manager;

import io.nuls.common.CommonContext;
import io.nuls.common.ConfigBean;
import io.nuls.common.NulsCoresConfig;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.model.bo.Chain;
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
 * Chain management,Responsible for initializing each chain,working,start-up,Parameter maintenance, etc
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
    private NulsCoresConfig contractConfig;

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * Initialize and start the chain
     * Initialize and start the chain
     */
    public void initChain() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            return;
        }
        /*
        Create an initialization chain based on configuration information/Initialize chains based on configuration information
        */
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            chain.setConfig(entry.getValue());
            /*
             * Initialize Chain Database Table/Initialize linked database tables
             */
            initTable(chain);
            /*
             * Initialize smart contract executor
             */
            initContractExecutor(chain);
            /*
             * Initialize smart contract creation contract unconfirmed transaction manager
             */
            initContractTxCreateUnconfirmedManager(chain);
            /*
             * Initialize Chain Log
             */
            initContractChainLog(chainId);
            chainMap.put(chainId, chain);
        }
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
     * Initialize Chain Log
     */
    private void initContractChainLog(int chainId) {
        LogUtil.configChainLog(chainId, ContractConstant.LOG_FILE_NAME);
    }

    /**
     * Stop a chain
     * Delete a chain
     *
     * @param chainId chainID/chain id
     */
    public void stopChain(int chainId) {

    }


    /**
     * Read configuration file to create and initialize chain
     * Read the configuration file to create and initialize the chain
     */
    private Map<Integer, ConfigBean> configChain() {
        try {
            /*
            Read database chain information configuration/Read database chain information configuration
             */
            Map<Integer, ConfigBean> configMap = CommonContext.CONFIG_BEAN_MAP;
            /*
            If the system is running for the first time and there is no storage chain information in the local database, it is necessary to read the main chain configuration information from the configuration file
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if (configMap == null || configMap.size() == 0) {
                //String configJson = IoUtils.read(ContractConstant.CONFIG_FILE_PATH);
                //List<ConfigItem> configItemList = JSONUtils.json2list(configJson, ConfigItem.class);
                //ConfigBean configBean = ConfigManager.initManager(configItemList);
                ConfigBean configBean = contractConfig;
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
     * Initialize Chain Related Tables
     * Initialization chain correlation table
     *
     * @param chain
     */
    private void initTable(Chain chain) {
        int chainId = chain.getChainId();
        try {
            // Contract Address Table
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + "_" + chainId);
            // Execution Results Table
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_EXECUTE_RESULT + "_" + chainId);
            // Contract generation transaction offline savinghashRelationship table
            RocksDBService.createTable(ContractDBConstant.DB_NAME_CONTRACT_OFFLINE_TX_HASH_LIST + "_" + chainId);

        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
            }
        }
    }

    public static void chainHandle(int chainId, int blockType) {
        // Set up log chain printing
        Log.currentThreadChainId(chainId);
        // Set transaction module request block processing mode, Packaging blocks - 0, Verify Block - 1
        Chain.putCurrentThreadBlockType(blockType);
    }

    public static void chainHandle(int chainId) {
        // Set up log chain printing
        Log.currentThreadChainId(chainId);
    }

    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }
}
