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
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2019/3/15
 */
public class ContractCall {

    /**
     * 调用智能合约
     * @param chain
     * @param txHexList
     * @return
     * @throws NulsException
     */
    public static Map<String, Object> invokeContract(Chain chain, List<String> txHexList, long blockHeight, long blockTime, String packingAddress, String preStateRoot) throws NulsException {

        Map<String, Object> params = new HashMap(TxConstant.INIT_CAPACITY_8);
        params.put("chainId", chain.getChainId());
        params.put("blockHeight", blockHeight);
        params.put("blockTime", blockTime);
        params.put("packingAddress", packingAddress);
        params.put("preStateRoot", preStateRoot);
        params.put("txHexList", txHexList);
        Map result = (Map) TransactionCall.request(ModuleE.SC.abbr, "sc_invoke_contract", params);
        try {
            chain.getLoggerMap().get(TxConstant.LOG_TX).debug("moduleCode:{}, -cmd:{}, -contractProcess -rs: {}",
                    ModuleE.SC.abbr, "sc_invoke_contract", JSONUtils.obj2json(result));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return result;
    }
}
