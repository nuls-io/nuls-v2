/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.ledger.rpc.call.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.ledger.config.LedgerConfig;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.rpc.call.CallRpcService;
import io.nuls.ledger.utils.LoggerUtil;

import java.util.*;

/**
 * @author lanjinsheng
 * @description
 * @date 2019/06/05
 */
@Component
public class CallRpcServiceImpl implements CallRpcService {
    @Autowired
    LedgerConfig ledgerConfig;

    @Override
    public long getBlockLatestHeight(int chainId) {
        Map<String, Object> map = new HashMap<>();
        map.put("chainId", chainId);
        try {
            Response response = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, CmdConstant.CMD_LATEST_HEIGHT, map);
            if (null != response && response.isSuccess()) {
                Map responseData = (Map) response.getResponseData();
                if (null != responseData) {
                    Map datas = (Map) responseData.get(CmdConstant.CMD_LATEST_HEIGHT);
                    if (null != datas) {
                        return Long.parseLong(datas.get("value").toString());
                    }
                }
            } else {
                LoggerUtil.logger(chainId).error("getBlockLatestHeight fail.response={}", JSONUtils.obj2json(response));
            }
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
        }
        return 0;
    }

    @Override
    public ErrorCode newTx(Transaction tx) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("chainId", ledgerConfig.getChainId());
            params.put("tx", RPCUtil.encode(tx.serialize()));
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, CmdConstant.CMD_TX_NEW, params);
            if (!cmdResp.isSuccess()) {
                return ErrorCode.init(cmdResp.getResponseErrorCode());
            }
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return LedgerErrorCode.ERROR_TX_REG_RPC;
        }
        return null;
    }

    @Override
    public ErrorCode transactionSignature(int chainId, String address, String password, Transaction tx) throws NulsException {
        try {
            if (Arrays.equals(AddressTool.getAddressByPubKeyStr(ledgerConfig.getBlackHolePublicKey(), chainId), AddressTool.getAddress(address))) {
                return LedgerErrorCode.ERROR_ADDRESS_ERROR;
            }
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            Map<String, Object> callParams = new HashMap<>(4);
            callParams.put(Constants.CHAIN_ID, chainId);
            callParams.put("address", address);
            callParams.put("password", password);
            callParams.put("data", RPCUtil.encode(tx.getHash().getBytes()));
            Response signResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, CmdConstant.CMD_AC_SIGN_DIGEST, callParams);
            if (!signResp.isSuccess()) {
                LoggerUtil.COMMON_LOG.error("ac_signDigest rpc error....{}=={}", signResp.getResponseErrorCode(), signResp.getResponseComment());
                return ErrorCode.init(signResp.getResponseErrorCode());
            }
            HashMap signResult = (HashMap) ((HashMap) signResp.getResponseData()).get("ac_signDigest");
            p2PHKSignature.parse(RPCUtil.decode((String) signResult.get("signature")), 0);
            TransactionSignature signature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            p2PHKSignatures.add(p2PHKSignature);
            signature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(signature.serialize());
        } catch (NulsException e) {
            LoggerUtil.COMMON_LOG.error(e);
            return LedgerErrorCode.ERROR_SIGNDIGEST;
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.error(e);
            return LedgerErrorCode.ERROR_SIGNDIGEST;
        }
        return null;
    }
}
