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
package io.nuls.account.util.manager;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.service.AccountService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.common.CommonContext;
import io.nuls.common.ConfigBean;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rockdb.constant.DBErrorCode;
import io.nuls.core.rockdb.service.RocksDBService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
    NulsCoresConfig accountConfig;

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();


    /**
     * Initialize Chain
     * Initialize and start the chain
     */
    public void initChain() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            return;
        }
       /* Create an initialization chain based on configuration information
        Initialize chains based on configuration information
        */
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            Chain chain = new Chain();
            int chainId = entry.getKey();
            chain.setConfig(entry.getValue());
            initLogger(chain);
            /*
            Initialize Chain Database Table
            Initialize linked database tables
            */
            initTable(chainId);
            chainMap.put(chainId, chain);
        }

        AccountService accountService = SpringLiteContext.getBean(AccountService.class);
        accountService.getAccountList();
    }


    /**
     * Load chain data
     * Initialize and start the chain
     */
    public void runChain() {

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
            Map<Integer, ConfigBean> configMap = CommonContext.CONFIG_BEAN_MAP;
            ConfigBean configBean = accountConfig;
            configMap.put(configBean.getChainId(), configBean);
            return configMap;
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
            return null;
        }
    }

    /**
     * Initialize Chain Related Tables
     * Initialization chain correlation table
     *
     * @param chainId chain id
     */
    private void initTable(int chainId) {
        try {
            /*
            Create an account alias table
            Create account alias tables
            */
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ALIAS + chainId)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ALIAS + chainId);
            }
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ADDRESS + chainId)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ADDRESS + chainId);
            }
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                LoggerUtil.LOG.error(e.getMessage());
                throw new NulsRuntimeException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
            } else {
                LoggerUtil.LOG.info(e.getMessage());
            }
        }
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

    public Chain getChain(int key) {
        return this.chainMap.get(key);
    }
}
