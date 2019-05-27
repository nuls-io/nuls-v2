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

package io.nuls.block.rpc.call;

import io.nuls.base.data.NulsHash;
import io.nuls.block.manager.ContextManager;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2019/3/15
 */
public class ContractCall {

    public static List<NulsHash> contractOfflineTxHashList(int chainId, String blockHash) throws NulsException {
        Map params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("blockHash", blockHash);
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            String cmd = "sc_contract_offline_tx_hash_list";
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.SC.abbr, cmd, params);
            if (!response.isSuccess()) {
                String errorCode = response.getResponseErrorCode();
                commonLog.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, errorCode, response.getResponseComment());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            Map data = (Map) response.getResponseData();
            Map result = (Map) data.get(cmd);
            commonLog.debug("moduleCode:{}, -cmd:{}, -contractProcess -rs: {}", ModuleE.SC.abbr, "sc_contract_offline_tx_hash_list", JSONUtils.obj2json(result));
            Object obj = result.get("list");
            if (null == obj) {
                return new ArrayList<>();
            }
            List<NulsHash> hashList = new ArrayList<>();
            for (String hashStr : (List<String>) obj) {
                hashList.add(NulsHash.fromHex(hashStr));
            }
            return hashList;
        } catch (Exception e) {
            commonLog.error(e);
            throw new NulsException(e);
        }
    }
}
