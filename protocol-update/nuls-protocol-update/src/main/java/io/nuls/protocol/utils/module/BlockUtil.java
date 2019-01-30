/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.protocol.utils.module;

import io.nuls.base.data.BlockHeader;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.protocol.rpc.callback.BlockHeaderInvoke;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用区块管理模块接口的工具类
 * Utility class that invokes the block module interface
 *
 * @author captain
 * @version 1.0
 * @date 19-1-25 上午11:45
 */
public class BlockUtil {

    /**
     * 更新本模块的运行时状态
     *
     * @param chainId
     * @param begin
     * @param end
     * @return
     */
    public static List<ProtocolVersion> getBlockHeaders(int chainId, long begin, long end) {
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(3);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("begin", begin);
            params.put("end", end);

            Response response = CmdDispatcher.requestAndResponse(ModuleE.BL.abbr, "getBlockHeadersByHeightRange", params);
            if (response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                List<ProtocolVersion> result = (List) responseData.get("getBlockHeadersByHeightRange");
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
        return null;
    }

    public static void register(int chainId){
        NulsLogger commonLog = ContextManager.getContext(chainId).getCommonLog();
        try {
            Map<String, Object> params = new HashMap<>(2);
//            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            CmdDispatcher.requestAndInvoke(ModuleE.BL.abbr, "latestBlockHeader", params, "0", "1", new BlockHeaderInvoke(chainId));

        } catch (Exception e) {
            e.printStackTrace();
            commonLog.error(e);
        }
    }

}
