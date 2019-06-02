/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.rpc.call;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.Transaction;
import io.nuls.contract.rpc.CallHelper;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-02-27
 */
public class TransactionCall {

    public static boolean newTx(int chainId, String txData) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("tx", txData);
        try {
            Map<String, Boolean> registerResult = (Map<String, Boolean>) CallHelper.request(ModuleE.TX.abbr, "tx_newTx", params);
            return registerResult.get("value");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
    public static Transaction getConfirmedTx(int chainId, String txHash) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("txHash", txHash);
        try {
            Map result = (Map) CallHelper.request(ModuleE.TX.abbr, "tx_getConfirmedTx", params);
            String txData = (String) result.get("tx");
            if (StringUtils.isBlank(txData)) {
                return null;
            }
            Transaction tx = new Transaction();
            tx.parse(RPCUtil.decode(txData), 0);
            return tx;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static boolean baseValidateTx(int chainId, String txData) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("tx", txData);
        try {
            Map<String, Boolean> baseValidateResult = (Map<String, Boolean>) CallHelper.request(ModuleE.TX.abbr, "tx_baseValidateTx", params);
            return baseValidateResult.get("value");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }


}
