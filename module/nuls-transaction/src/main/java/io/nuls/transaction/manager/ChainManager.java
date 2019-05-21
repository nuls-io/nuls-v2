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
package io.nuls.transaction.manager;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.protocol.ProtocolLoader;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxDBConstant;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.storage.ConfigStorageService;
import io.nuls.transaction.threadpool.NetTxThreadPoolExecutor;
import io.nuls.transaction.utils.LoggerUtil;

import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 链管理类,负责各条链的初始化,运行,启动,参数维护等
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author qinyifeng
 * @date 2018/12/11
 */
@Component
public class ChainManager {

    @Autowired
    private ConfigStorageService configService;

    @Autowired
    private SchedulerManager schedulerManager;

    @Autowired
    private TxConfig txConfig;

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
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            chain.setConfig(entry.getValue());
            initLogger(chain);
            initTable(chain);
            chainMap.put(chainId, chain);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("Chain:{} init success..", chainId);
            ProtocolLoader.load(chainId);
        }
    }

    /**
     * 初始化并启动链
     * Initialize and start the chain
     */
    public void runChain() throws Exception {

        for (Chain chain: chainMap.values()) {
            initCache(chain);
            schedulerManager.createTransactionScheduler(chain);
            chainMap.put(chain.getChainId(), chain);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("Chain:{} runChain success..", chain.getChainId());
        }
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
            读取数据库链信息配置
            Read database chain information configuration
             */
            Map<Integer, ConfigBean> configMap = configService.getList();

            /*
            如果系统是第一次运行，则本地数据库没有存储链信息，此时需要从配置文件读取主链配置信息
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if (configMap.isEmpty()) {
                ConfigBean configBean = txConfig;

                boolean saveSuccess = configService.save(configBean, configBean.getChainId());
                if(saveSuccess){
                    configMap.put(configBean.getChainId(), configBean);
                }
            }
            return configMap;
        } catch (Exception e) {
            LOG.error(e);
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
        NulsLogger logger = chain.getLoggerMap().get(TxConstant.LOG_TX);
        int chainId = chain.getConfig().getChainId();
        try {
            /*
            创建已确认交易表
            Create confirmed transaction table
            */
            RocksDBService.createTable(TxDBConstant.DB_TRANSACTION_CONFIRMED_PREFIX + chainId);

            /*
            已验证未打包交易
            Verified transaction
            */
            RocksDBService.createTable(TxDBConstant.DB_TRANSACTION_UNCONFIRMED_PREFIX + chainId);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                logger.error(e.getMessage());
            }
        }
    }

    /**
     * 初始化链缓存数据
     * Initialize chain caching entity
     *
     * @param chain chain info
     */
    private void initCache(Chain chain) {
        BlockingDeque<TransactionNetPO> unverifiedQueue = new LinkedBlockingDeque<>((int)txConfig.getTxUnverifiedQueueSize());
        chain.setUnverifiedQueue(unverifiedQueue);

        NetTxThreadPoolExecutor netTxThreadPoolExecutor = new NetTxThreadPoolExecutor(chain);
        chain.setNetTxThreadPoolExecutor(netTxThreadPoolExecutor);
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
