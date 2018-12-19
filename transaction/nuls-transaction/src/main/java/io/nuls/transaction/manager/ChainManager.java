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

import io.nuls.base.data.NulsDigestData;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.log.logback.LoggerBuilder;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.rocksdb.storage.ConfigStorageService;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.CrossChainTx;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.bo.config.ConfigItem;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    private CrossTxVerifyingManager crossTxVerifyingManager;

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * 初始化并启动链
     * Initialize and start the chain
     */
    public void runChain() throws Exception{
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            return;
        }
        /*
        根据配置信息创建初始化链
        Initialize chains based on configuration information
        */
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            chain.setConfig(entry.getValue());
            /*
            初始化链数据库表
            Initialize linked database tables
            */
            //initTable(chain);
            initCache(chain);
            initLogger(chain);
            schedulerManager.createTransactionScheduler(chain);
            chainMap.put(chainId, chain);
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
            if (configMap == null || configMap.size() == 0) {
                String configJson = IoUtils.read(TxConstant.CONFIG_FILE_PATH);
                List<ConfigItem> configItemList = JSONUtils.json2list(configJson, ConfigItem.class);
                ConfigBean configBean = ConfigManager.initManager(configItemList);
                if (configBean == null) {
                    return null;
                }
                configMap.put(configBean.getChainId(), configBean);
                //设置当前链ID
//                TxConfig.CURRENT_CHAINID = configBean.getChainId();
                //设置当前链主资产ID
//                TxConfig.CURRENT_CHAIN_ASSETID = configBean.getAssetsId();
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
        try {
            //todo
            //DBUtil.createTable(MODULE_CONGIF);
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.info(e.getMessage());
            } else {
                Log.error(e);
            }
        }
    }

    /**
     * 初始化链缓存数据
     * Initialize chain caching data
     *
     * @param chain  chain info
     * */
    private void initCache(Chain chain){
        /**
         * 管理接收的其他链创建的跨链交易(如果有), 暂存验证中的跨链交易.
         *  TODO 初始化时需查数据库
         */
        crossTxVerifyingManager.initCrossTxVerifyingMap(chain);
    }

    private void initLogger(Chain chain){
        /*
         * 共识模块日志文件对象创建,如果一条链有多类日志文件，可在此添加
         * Creation of Log File Object in Consensus Module，If there are multiple log files in a chain, you can add them here
         * */
        NulsLogger txLogger = LoggerBuilder.getLogger(String.valueOf(chain.getConfig().getChainId()), TxConstant.MODULE_CODE);
        chain.setLogger(txLogger);
    }

    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }

    public boolean containsKey(int key){
        return this.chainMap.containsKey(key);
    }

    public Chain getChain(int key){
        return this.chainMap.get(key);
    }
}
