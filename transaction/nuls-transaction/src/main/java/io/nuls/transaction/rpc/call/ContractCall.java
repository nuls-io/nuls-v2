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

package io.nuls.transaction.rpc.call;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.rpc.model.ModuleE;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2019/3/15
 */
public class ContractCall {


    /**
     * 打包智能合约通知
     * @param chain
     * @param blockHeight
     * @param blockTime
     * @param packingAddress
     * @param preStateRoot
     * @return
     * @throws NulsException
     */
    public static boolean contractBatchBegin(Chain chain, long blockHeight, long blockTime, String packingAddress, String preStateRoot) {
        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put("chainId", chain.getChainId());
        params.put("blockHeight", blockHeight);
        params.put("blockTime", blockTime);
        params.put("packingAddress", packingAddress);
        params.put("preStateRoot", preStateRoot);
        try {
            TransactionCall.request(ModuleE.SC.abbr, "sc_batch_begin", params);
            return true;
        } catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return false;
        }

    }
    /**
     * 调用智能合约, 合约执行成功与否,不影响交易的打包
     * @param chain
     * @param txHex
     * @return
     * @throws NulsException
     */
    public static boolean invokeContract(Chain chain, String txHex) {

        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put("chainId", chain.getChainId());
        params.put("txHex", txHex);
        try {
            TransactionCall.request(ModuleE.SC.abbr, "sc_invoke_contract", params);
            return true;
        } catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
            return false;
        }
    }

    /**
     * 调用智能合约
     * @param chain
     * @param blockHeight
     * @return
     * @throws NulsException
     */
    public static Map<String, Object> contractBatchEnd(Chain chain, long blockHeight) throws NulsException {

        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put("chainId", chain.getChainId());
        params.put("blockHeight", blockHeight);
        Map result = null;
        try {
           result = (Map) TransactionCall.request(ModuleE.SC.abbr, "sc_batch_end", params);
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("moduleCode:{}, -cmd:{}, -contractProcess -rs: {}",
                    ModuleE.SC.abbr, "sc_batch_end", JSONUtils.obj2json(result));
        }catch (JsonProcessingException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
        }catch (NulsException e) {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
        }
        return result;
    }
}
