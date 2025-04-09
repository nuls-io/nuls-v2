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
package io.nuls.transaction.manager;

import io.nuls.common.CommonContext;
import io.nuls.common.ConfigBean;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.common.NulsCoresConfig;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.utils.LoggerUtil;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * Chain management,Responsible for initializing each chain,working,start-up,Parameter maintenance, etc
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author qinyifeng
 * @date 2018/12/11
 */
@Component
public class ChainManager {

    @Autowired
    private SchedulerManager schedulerManager;

    @Autowired
    private NulsCoresConfig txConfig;

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    public ChainManager( ) {
    }

    public ChainManager(SchedulerManager schedulerManager, NulsCoresConfig txConfig) {
        this.schedulerManager = schedulerManager;
        this.txConfig = txConfig;
    }


    /**
     * Initializes the chain system by creating and configuring chain instances based on the provided configuration.
     *
     * @throws Exception if an error occurs during the initialization process
     */
    public void initChain() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            return;
        }
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            chain.setConfig(entry.getValue());
            initLogger(chain);
            initTable(chain);
            chainMap.put(chainId, chain);
            chain.getLogger().debug("Chain:{} init success..", chainId);
            //ProtocolLoader.load(chainId);
        }
    }


    /**
     * Runs the initialization and scheduling process for all chains.
     * This method iterates through the chain mapping and performs cache initialization,
     * scheduler creation, and chain state updates for each chain.
     *
     * @throws Exception If an error occurs during the initialization or scheduling process of a chain,
     *                   an exception may be thrown.
     */
    public void runChain() throws Exception {

        for (Chain chain: chainMap.values()) {
            initCache(chain);
            schedulerManager.createTransactionScheduler(chain);
            chainMap.put(chain.getChainId(), chain);
            chain.getLogger().debug("Chain:{} runChain success..", chain.getChainId());
        }
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
            Read database chain information configuration
            Read database chain information configuration
             */
            Map<Integer, ConfigBean> configMap = CommonContext.CONFIG_BEAN_MAP;

            /*
            If the system is running for the first time and there is no storage chain information in the local database, it is necessary to read the main chain configuration information from the configuration file
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if (configMap.isEmpty()) {
                ConfigBean configBean = txConfig;
                configMap.put(configBean.getChainId(), configBean);
            }
            return configMap;
        } catch (Exception e) {
            LOG.error(e);
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
        NulsLogger logger = chain.getLogger();
        int chainId = chain.getConfig().getChainId();
        try {
            //Unconfirmed table
            if(RocksDBService.existTable(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId)){
                RocksDBService.destroyTable(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId);
            }

            /*
            Create confirmed transaction table
            Create confirmed transaction table
            */
            RocksDBService.createTable(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId);


            /*
            Verified Unpackaged Transactions Unconfirmed
            Verified transaction
            */
            RocksDBService.createTable(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                logger.error(e);
            }
        }
    }

    /**
     * Initialize chain cache data
     * Initialize chain caching entity
     *
     * @param chain chain info
     */
    private void initCache(Chain chain) {
        BlockingDeque<TransactionNetPO> unverifiedQueue = new LinkedBlockingDeque<>((int)txConfig.getTxUnverifiedQueueSize());
        chain.setUnverifiedQueue(unverifiedQueue);
    }

    private void initLogger(Chain chain) {
        LoggerUtil.init(chain);
    }

    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }

    public boolean containsKey(int key) {
        return this.chainMap.containsKey(key);
    }

    public Chain getChain(int key) {
        return this.chainMap.get(key);
    }
}
