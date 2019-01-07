/**
 * MIT License
 * <p>
 * Copyright (c) 2018-2019 nuls.io
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

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.model.bo.Chain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: qinyifeng
 * @date: 2019/01/03
 */
public class ConsensusCall {

    /**
     * 验证是否是共识节点
     * @param agentAddress 节点地址
     * @return
     */
    public static boolean isConsensusNode(Chain chain, String agentAddress) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            //TODO
            params.put("chainId", chain.getChainId());
            params.put("agentAddress", agentAddress);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "isConsensusNode", params);
            if (cmdResp.isSuccess()) {
                HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("cs_isConsensusNode"));
                return (Boolean) result.get("value");
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return true;
    }
}
