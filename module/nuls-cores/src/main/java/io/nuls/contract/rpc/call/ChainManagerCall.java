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

import io.nuls.contract.rpc.CallHelper;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-10-21
 */
public class ChainManagerCall {

    /**
     * 查询是否为跨链资产
     */
    public static boolean isCrossAssets(int chainId, int assetId) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("assetId", assetId);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_asset", params);
            return callResp.isSuccess();
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static int getCrossAssetsDecimals(int chainId, int assetId) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("assetId", assetId);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CM.abbr, "cm_asset", params);
            if (!callResp.isSuccess()) {
                return 0;
            }
            Map resData = (Map) callResp.getResponseData();
            Map resultMap = (Map) resData.get("cm_asset");
            Object decimalPlaces = resultMap.get("decimalPlaces");
            if (decimalPlaces == null) {
                return 0;
            }
            return Integer.parseInt(decimalPlaces.toString());
        } catch (Exception e) {
            return 0;
        }
    }
}
