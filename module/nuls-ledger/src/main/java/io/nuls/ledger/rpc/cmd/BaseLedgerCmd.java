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
package io.nuls.ledger.rpc.cmd;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.utils.LoggerUtil;

import java.util.List;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * @author lan
 * @description
 * @date 2019/03/13
 **/
public class BaseLedgerCmd extends BaseCmd {
    boolean chainHanlder(int chainId) {
        //链判断？判断是否是有效的.
        //进行初始化
        try {
            SpringLiteContext.getBean(LedgerChainManager.class).addChain(chainId);
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
            return false;
        }
        return true;
    }

    Response parseTxs(List<String> txStrList, List<Transaction> txList, int chainId) {
        for (String txStr : txStrList) {
            if (StringUtils.isBlank(txStr)) {
                return failed("tx is blank");
            }
            byte[] txStream = RPCUtil.decode(txStr);
            Transaction tx = new Transaction();
            try {
                tx.parse(new NulsByteBuffer(txStream));
                txList.add(tx);
            } catch (NulsException e) {
                logger(chainId).error("transaction parse error", e);
                return failed(LedgerErrorCode.TX_IS_WRONG);
            }
        }
        return success();
    }

    Transaction parseTxs(String txStr, int chainId) {
        if (StringUtils.isBlank(txStr)) {
            return null;
        }
        byte[] txStream = RPCUtil.decode(txStr);
        Transaction tx = new Transaction();
        try {
            tx.parse(new NulsByteBuffer(txStream));
        } catch (NulsException e) {
            logger(chainId).error("transaction parse error", e);
            return null;
        }
        return tx;
    }

}
