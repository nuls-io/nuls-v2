/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.base.basic.AddressTool;
import io.nuls.common.CommonVersionChangeInvoker;
import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.util.AddressPrefixDatas;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxContext;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.rpc.upgrade.TxVersionChangeInvoker;
import io.nuls.transaction.utils.TxUtil;

import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2019/3/4
 */
@Component
public class TransactionBootstrap implements INulsCoresBootstrap {

    @Autowired
    private NulsCoresConfig txConfig;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;
    @Autowired
    private ChainManager chainManager;

    @Override
    public int order() {
        return 3;
    }

    @Override
    public void mainFunction(String[] args) {
        this.init();
    }

    public void init() {
        try {
            //初始化数据库配置文件
            initDB();
            initTransactionContext();
            chainManager.initChain();
            TxUtil.blackHolePublicKey = HexUtil.decode(txConfig.getBlackHolePublicKey());
        } catch (Exception e) {
            LOG.error("Transaction init error!");
            LOG.error(e);
        }
    }

    private boolean doStart() {
        try {
            chainManager.runChain();
            LOG.info("Transaction Ready...");
            return true;
        } catch (Exception e) {
            LOG.error("Transaction init error!");
            LOG.error(e);
            return false;
        }
    }


    @Override
    public void onDependenciesReady() {
        doStart();
        // add by pierre at 2019-12-04 增加与智能合约模块的连接标志
        txConfig.setCollectedSmartContractModule(true);
        // end code by pierre
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.TX.abbr, TxConstant.RPC_VERSION);
    }

    public void initDB() {
        try {
            //数据文件存储地址
            RocksDBService.init(txConfig.getDataPath() + File.separator + ModuleE.TX.name);
        } catch (Exception e) {
            LOG.error(e);
        }
    }

    private void initTransactionContext(){
        String accountBlockManagerPublicKeys = txConfig.getAccountBlockManagerPublicKeys();
        if (StringUtils.isNotBlank(accountBlockManagerPublicKeys)) {
            String[] split = accountBlockManagerPublicKeys.split(",");
            for (String pubkey : split) {
                TxContext.ACCOUNT_BLOCK_MANAGER_ADDRESS_SET.add(AddressTool.getAddressString(HexUtil.decode(pubkey.trim()), txConfig.getChainId()));
            }
            int size = TxContext.ACCOUNT_BLOCK_MANAGER_ADDRESS_SET.size();
            TxContext.ACCOUNT_BLOCK_MIN_SIGN_COUNT = BigDecimal.valueOf(size).multiply(BigDecimal.valueOf(6)).divide(BigDecimal.TEN, 0, RoundingMode.UP).intValue();
        }
	}
}
