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
package io.nuls.ledger.manager;

import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.ledger.config.LedgerConfig;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.LedgerChain;
import io.nuls.ledger.service.BlockDataService;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.storage.impl.RepositoryImpl;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链管理类,负责各条链的初始化,运行,启动,参数维护等
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 * @author lanjinsheng
 * @date 2019/02/11
 */
@Service
public class LedgerChainManager {
    @Autowired
    BlockDataService blockDataService;
    @Autowired
    LedgerConfig ledgerConfig;
    private Map<Integer, LedgerChain> chainMap = new ConcurrentHashMap<>();

    /**
     * 增加链
     *
     * @param chainId
     * @throws Exception
     */
    public void addChain(int chainId) throws Exception {
        if (null != chainMap.get(chainId)) {
            return;
        }
        LedgerChain ledgerChain = new LedgerChain(chainId);
        chainMap.put(chainId, ledgerChain);
    }

    /**
     * 初始化并启动链
     * Initialize and start the chain
     */
    public void runChain(int chainId) throws Exception {

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
     * 进行数据的校验处理,比如异常关闭模块造成的数据不一致。
     * 确认的高度是x,则进行x高度的数据恢复处理
     */
    private void initLedgerDatas() throws Exception {
        blockDataService.initBlockDatas();
    }

    /**
     * 初始化数据库
     */
    private void initRocksDb() {
        try {
            RocksDBService.init(ledgerConfig.getDataPath() + File.separator + ModuleE.LG.name);
            Repository initDB = SpringLiteContext.getBean(RepositoryImpl.class);
            initDB.initTableName();
        } catch (Exception e) {
            LoggerUtil.logger().error(e);
        }
    }

    public void initChains() throws Exception {
        initRocksDb();
        initLedgerDatas();
    }


    public LedgerChain getChain(int key) {
        return this.chainMap.get(key);
    }
}
