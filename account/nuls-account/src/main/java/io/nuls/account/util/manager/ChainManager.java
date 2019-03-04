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
package io.nuls.account.util.manager;

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.AccountStorageConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.config.ConfigBean;
import io.nuls.account.model.bo.config.ConfigItem;
import io.nuls.account.rpc.call.TransactionCmdCall;
import io.nuls.account.storage.ConfigService;
import io.nuls.db.constant.DBErrorCode;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

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
    private ConfigService configService;

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    /**
     * 初始化并启动链
     * Initialize and start the chain
     */
    public void runChain() {
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
            initTable(chainId);
            //注册账户相关交易
            try {
                while (true) {
                    if (TransactionCmdCall.registerTx(chainId)) {
                        break;
                    }
                    Thread.sleep(3000L);
                }
            } catch (Exception e) {
                Log.error("Transaction registerTx error!");
            }
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
                String configJson = IoUtils.read(NulsConfig.CONFIG_FILE_PATH);
                List<ConfigItem> configItemList = JSONUtils.json2list(configJson, ConfigItem.class);
                ConfigBean configBean = ConfigManager.initManager(configItemList);
                if (configBean == null) {
                    return null;
                }
                configMap.put(configBean.getChainId(), configBean);
                //设置当前链ID
                NulsConfig.CURRENT_CHAIN_ID = configBean.getChainId();
                //设置当前链主资产ID
                NulsConfig.CURRENT_MAIN_ASSETS_ID = configBean.getAssetsId();
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
     * @param chainId chain id
     */
    private void initTable(int chainId) {
        try {
            /*
            创建账户别名表
            Create account alias tables
            */
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ALIAS + chainId)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ALIAS + chainId);
            }
            if (!RocksDBService.existTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ADRESS + chainId)) {
                RocksDBService.createTable(AccountStorageConstant.DB_NAME_ACCOUNT_ALIAS_KEY_ADRESS + chainId);
            }
        } catch (Exception e) {
            if (!DBErrorCode.DB_TABLE_EXIST.equals(e.getMessage())) {
                Log.error(e.getMessage());
                throw new NulsRuntimeException(AccountErrorCode.DB_TABLE_CREATE_ERROR);
            } else {
                Log.info(e.getMessage());
            }
        }
    }

    public Map<Integer, Chain> getChainMap() {
        return chainMap;
    }

    public void setChainMap(Map<Integer, Chain> chainMap) {
        this.chainMap = chainMap;
    }
}
