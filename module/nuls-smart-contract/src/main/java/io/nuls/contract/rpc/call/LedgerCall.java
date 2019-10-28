/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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

import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.util.Log;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-02-26
 */
public class LedgerCall {

    public static Map<String, Object> getBalanceAndNonce(Chain chain, String address) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("assetChainId", chain.getConfig().getChainId());
        params.put("address", address);
        params.put("assetId", chain.getConfig().getAssetId());
        params.put("isConfirmed", false);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
            if (!callResp.isSuccess()) {
                return null;
            }
            return (HashMap) ((HashMap) callResp.getResponseData()).get("getBalanceNonce");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static Map<String, Object> getConfirmedBalanceAndNonce(Chain chain, String address) throws NulsException {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chain.getConfig().getChainId());
        params.put("assetChainId", chain.getConfig().getChainId());
        params.put("address", address);
        params.put("assetId", chain.getConfig().getAssetId());
        params.put("isConfirmed", true);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, "getBalanceNonce", params);
            if (!callResp.isSuccess()) {
                return null;
            }
            return (HashMap) ((HashMap) callResp.getResponseData()).get("getBalanceNonce");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 向账本注册NRC20资产
     */
    public static Map commitNRC20Assets(int chainId, String name, String symbol, short decimal, BigInteger totalSupply, String nrc20ContractAddress) throws NulsException {
        String cmd = "chainAssetContractReg";
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("assetName", name);
            params.put("initNumber", totalSupply.toString());
            params.put("decimalPlace", decimal);
            params.put("assetSymbol", symbol);
            params.put("contractAddress", nrc20ContractAddress);
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, cmd, params);
            if (!callResp.isSuccess()) {
                Log.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, callResp.getResponseErrorCode(), callResp.getResponseComment());
                return null;
            }
            HashMap result = (HashMap) ((HashMap) callResp.getResponseData()).get(cmd);
            return result;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 调用账本回滚已注册的NRC20资产
     */
    public static boolean rollBackNRC20Assets(int chainId, String nrc20ContractAddress) throws NulsException {
        String cmd = "chainAssetContractRollBack";
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("contractAddress", nrc20ContractAddress);
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, cmd, params);
            if (!callResp.isSuccess()) {
                Log.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, callResp.getResponseErrorCode(), callResp.getResponseComment());
                return false;
            }
            HashMap result = (HashMap) ((HashMap) callResp.getResponseData()).get(cmd);
            return Boolean.parseBoolean(result.get("value").toString());
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 获取已注册的token资产列表
     */
    public static List<Map> getRegTokenList(int chainId) throws NulsException {
        String cmd = "getAssetRegInfo";
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("assetType", ContractConstant.TOKEN_ASSET_TYPE);
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.LG.abbr, cmd, params);
            if (!callResp.isSuccess()) {
                Log.error("Call interface [{}] error, ErrorCode is {}, ResponseComment:{}", cmd, callResp.getResponseErrorCode(), callResp.getResponseComment());
                return null;
            }
            Map resultMap = (Map) ((HashMap) callResp.getResponseData()).get(cmd);
            List<Map> resultList = (List<Map>) resultMap.get("assets");
            return resultList;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
