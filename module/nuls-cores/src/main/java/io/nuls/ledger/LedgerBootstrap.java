/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger;

import io.nuls.common.INulsCoresBootstrap;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.util.AddressPrefixDatas;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.utils.LoggerUtil;

/**
 * @author: Niels Wang
 * @date: 2018/10/15
 */
@Component
public class LedgerBootstrap implements INulsCoresBootstrap {
    @Autowired
    NulsCoresConfig ledgerConfig;
    @Autowired
    AddressPrefixDatas addressPrefixDatas;

    @Override
    public int order() {
        return 5;
    }

    @Override
    public void mainFunction(String[] args) {
        this.init();
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.LG.abbr, "1.0");
    }

    public void init() {
        try {
            LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME = ledgerConfig.getUnconfirmedTxExpired();
            LedgerConstant.DEFAULT_ENCODING = ledgerConfig.getEncoding();
            LedgerConstant.blackHolePublicKey = HexUtil.decode(ledgerConfig.getBlackHolePublicKey());
            LedgerChainManager ledgerChainManager = SpringLiteContext.getBean(LedgerChainManager.class);
            ledgerChainManager.initChains();
            LoggerUtil.COMMON_LOG.info("Ledger data init complete!");
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            LoggerUtil.COMMON_LOG.error("start fail...");
            System.exit(-1);
        }

    }

    @Override
    public void onDependenciesReady() {
        try {
            LedgerChainManager ledgerChainManager = SpringLiteContext.getBean(LedgerChainManager.class);
            ledgerChainManager.syncBlockHeight();
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            System.exit(-1);
        }
    }

}
