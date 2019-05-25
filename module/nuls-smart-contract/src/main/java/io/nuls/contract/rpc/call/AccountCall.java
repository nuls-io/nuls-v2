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

import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.contract.rpc.CallHelper;
import io.nuls.contract.util.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2019-02-27
 */
public class AccountCall {

    public static String createContractAddress(int chainId) throws NulsException {
        try {
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            Map resultMap = (Map) CallHelper.request(ModuleE.AC.abbr, "ac_createContractAccount", params);
            return (String) resultMap.get("address");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public static Result validationPassword(int chainId, String address, String passwd) {
        try {
            if (StringUtils.isBlank(address) || StringUtils.isBlank(passwd)) {
                return Result.getFailed(NULL_PARAMETER);
            }
            Map<String, Object> params = new HashMap<>(4);
            params.put(Constants.CHAIN_ID, chainId);
            params.put("address", address);
            params.put("password", passwd);
            Map resultMap = (Map) CallHelper.request(ModuleE.AC.abbr, "ac_validationPassword", params);
            boolean validate = (boolean) resultMap.get("value");
            if (validate) {
                return getSuccess();
            }
            return Result.getFailed(PASSWORD_IS_WRONG);
        } catch (Exception e) {
            Log.error(e);
            return Result.getFailed(ACCOUNT_NOT_EXIST);
        }
    }

    public static void transactionSignature(int chainId, String address, String password, Transaction tx) throws NulsException {
        try {
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put(Constants.CHAIN_ID, chainId);
            callParams.put("address", address);
            callParams.put("password", password);
            callParams.put("data", RPCUtil.encode(tx.getHash().getBytes()));
            Response signResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_signDigest", callParams);
            if (!signResp.isSuccess()) {
                throw new NulsException(SIGNATURE_ERROR);
            }
            HashMap signResult = (HashMap) ((HashMap) signResp.getResponseData()).get("ac_signDigest");
            p2PHKSignature.parse(RPCUtil.decode((String) signResult.get("signature")), 0);
            TransactionSignature signature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            p2PHKSignatures.add(p2PHKSignature);
            signature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(signature.serialize());
        } catch (NulsException e) {
            throw e;
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

}
