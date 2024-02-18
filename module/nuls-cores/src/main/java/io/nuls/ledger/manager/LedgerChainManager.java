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
package io.nuls.ledger.manager;

import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.LedgerChain;
import io.nuls.ledger.service.AssetRegMngService;
import io.nuls.ledger.service.BlockDataService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.storage.impl.LgBlockSyncRepositoryImpl;
import io.nuls.ledger.storage.impl.RepositoryImpl;
import io.nuls.ledger.utils.LoggerUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chain management,Responsible for initializing each chain,working,start-up,Parameter maintenance, etc
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author lanjinsheng
 * @date 2019/02/11
 */
@Component
public class LedgerChainManager {
    @Autowired
    BlockDataService blockDataService;
    @Autowired
    AssetRegMngService assetRegMngService;
    @Autowired
    NulsCoresConfig ledgerConfig;
    private Map<Integer, LedgerChain> chainMap = new ConcurrentHashMap<>();
    Map<String, Object> localChainDefaultAsset = new HashMap<>(16);

    /**
     * Add Chain
     *
     * @param chainId
     * @throws Exception
     */
    public void addChain(int chainId) throws Exception {
        if (null != chainMap.get(chainId)) {
            return;
        }
        LedgerChain ledgerChain = new LedgerChain(chainId);
        //Establishing logs
        LoggerUtil.createLogger(chainId);
        //set up a database
        SpringLiteContext.getBean(RepositoryImpl.class).initChainDb(chainId);
        SpringLiteContext.getBean(LgBlockSyncRepositoryImpl.class).initChainDb(chainId);
        chainMap.put(chainId, ledgerChain);
    }

    /**
     * Initialize and start the chain
     * Initialize and start the chain
     */
    public void runChain(int chainId) throws Exception {

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
     * Perform data validation processing,For example, data inconsistency caused by abnormal module shutdown.
     * The confirmed height isx,Then proceed withxHighly efficient data recovery processing
     */
    private void initLedgerDatas() throws Exception {
        blockDataService.initBlockDatas();
        assetRegMngService.initDBAssetsIdMap();
    }

    /**
     * Initialize database
     */
    private void initRocksDb() {
        try {
            RocksDBService.init(ledgerConfig.getDataPath() + File.separator + ModuleE.LG.name);
            Repository initDB = SpringLiteContext.getBean(RepositoryImpl.class);
            initDB.initTableName();
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
        }
    }

    public void initChains() throws Exception {
        initRocksDb();
        initLedgerDatas();
    }

    public void syncBlockHeight() {
        try {
            blockDataService.syncBlockHeight();
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
        }
    }

    public LedgerChain getChain(int key) {
        return this.chainMap.get(key);
    }

    public Map<String, Object> getLocalChainDefaultAsset() {
        if (localChainDefaultAsset.size() > 0) {
            return localChainDefaultAsset;
        }
        localChainDefaultAsset.put("assetChainId", ledgerConfig.getChainId());
        localChainDefaultAsset.put("assetId", ledgerConfig.getAssetId());
        localChainDefaultAsset.put("initNumber", 0);
        localChainDefaultAsset.put("decimalPlace", ledgerConfig.getDecimals());
        localChainDefaultAsset.put("assetName", ledgerConfig.getSymbol());
        localChainDefaultAsset.put("assetSymbol", ledgerConfig.getSymbol());
        localChainDefaultAsset.put("assetType", LedgerConstant.COMMON_ASSET_TYPE);
        localChainDefaultAsset.put("assetAddress", "");
        return localChainDefaultAsset;
    }
}
