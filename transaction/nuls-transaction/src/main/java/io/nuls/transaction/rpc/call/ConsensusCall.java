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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: qinyifeng
 * @date: 2019/01/03
 */
public class ConsensusCall {

    /**
     * 验证是否是共识节点
     *
     * @param agentAddress 节点地址
     * @return
     */
    @Deprecated
    public static boolean isConsensusNode(Chain chain, String agentAddress) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            //TODO cmd名称 返回值key
            params.put("chainId", chain.getChainId());
            params.put("agentAddress", agentAddress);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_isConsensusNode", params);
            if (cmdResp.isSuccess()) {
                HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("cs_isConsensusNode"));
                return (Boolean) result.get("value");
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return true;
    }

    /**
     * 获取节点打包地址
     *
     * @param chain
     * @return
     */
    public static String getNodePackagerAddress(Chain chain) {
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            //TODO cmd名称 返回值key
            params.put("chainId", chain.getChainId());
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "cs_getNodePackagerAddress", params);
            if (cmdResp.isSuccess()) {
                HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("cs_getNodePackagerAddress"));
                return (String) result.get("value");
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return null;
    }

    /**
     * 获取当前所有节点的出块地址, 主要用于主网
     *
     * @param chain
     * @return
     */
    public static List<String> getAgentAddressList(Chain chain) {
        return getRecentPackagerAddress(chain, 0L);
    }

    /**
     * 获取最近height个出块者的出块地址
     *
     * @param chain
     * @param height
     * @return
     */
    public static List<String> getRecentPackagerAddress(Chain chain, long height) {
        List<String> address = new ArrayList<>();
        try {
            Map<String, Object> params = new HashMap<>(TxConstant.INIT_CAPACITY);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            //TODO cmd名称 返回值key
            params.put("chainId", chain.getChainId());
            params.put("height", height);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.CS.abbr, "getAgentAddressList", params);
            if (cmdResp.isSuccess()) {
                HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("cs_getAgentAddressList"));
                if (null != result.get("address")) {
                    address = (List<String>) result.get("address");
                }
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return address;
    }
}
