/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.rpc.cmd;

import io.nuls.base.data.Transaction;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.ledger.validator.CoinDataValidator;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/22.
 */
@Component
public class ValidatorCmd extends BaseLedgerCmd {
    @Autowired
    CoinDataValidator coinDataValidator;
    @Autowired
    TransactionService transactionService;

    /**
     * validate coin entity
     * 进行nonce-hash校验，进行可用余额校验
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "validateCoinData",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "isBatchValidate", parameterType = "boolean")
    public Response validateCoinData(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        String txHex = (String) params.get("txHex");
        boolean isBatchValidate = Boolean.valueOf(params.get("isBatchValidate").toString());
        Transaction tx = new Transaction();
        Response response = null;
        ValidateResult validateResult = null;
        try {
            tx.parse(HexUtil.decode(txHex), 0);
            if (isBatchValidate) {
                LoggerUtil.logger(chainId).debug("确认交易校验：chainId={},txHash={},isBatchValidate={}", chainId, tx.getHash().toString(), isBatchValidate);
                validateResult = coinDataValidator.bathValidatePerTx(chainId, tx);
//                if(CoinDataValidator.VALIDATE_SUCCESS_CODE != validateResult.getValidateCode()){
//                      transactionService.rollBackUnconfirmTx(chainId,tx);
//                }
            } else {
                LoggerUtil.logger(chainId).debug("未确认交易校验：chainId={},txHash={},isBatchValidate={}", chainId, tx.getHash().toString(), isBatchValidate);
                validateResult = coinDataValidator.validateCoinData(chainId, tx);
            }
            response = success(validateResult);
            LoggerUtil.logger(chainId).debug("validateCoinData returnCode={},returnMsg={}", validateResult.getValidateCode(), validateResult.getValidateDesc());
        } catch (NulsException e) {
            e.printStackTrace();
            response = failed(e.getErrorCode());
            LoggerUtil.logger(chainId).error("validateCoinData exception:{}", e.getMessage());
        }
        return response;
    }
    /**
     * 回滚打包确认交易状态
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "rollbackTxValidateStatus",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHex", parameterType = "String")
    public Response rollbackTxValidateStatus(Map params) {
        Map<String, Object> rtData = new HashMap<>();
        int value = 0;
        Integer chainId = (Integer) params.get("chainId");
        try {
            String txHex = params.get("txHex").toString();
            LoggerUtil.logger(chainId).debug("rollbackrTxValidateStatus chainId={}", chainId);
            Transaction tx = parseTxs(txHex,chainId);
            if (null == tx) {
                LoggerUtil.logger(chainId).debug("txHex is invalid chainId={},txHex={}", chainId,txHex);
                return failed("txHex is invalid");
            }
            LoggerUtil.txUnconfirmedRollBackLog(chainId).debug("rollbackrTxValidateStatus chainId={},txHash={}", chainId, tx.getHash().toString());
            //清理未确认回滚
            transactionService.rollBackUnconfirmTx(chainId,tx);
            if (coinDataValidator.rollbackTxValidateStatus(chainId, tx)) {
                value = 1;
            } else {
                value = 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger(chainId).debug("response={}", response);
        return response;

    }

    /**
     * bathValidateBegin
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "bathValidateBegin",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response bathValidateBegin(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        LoggerUtil.logger(chainId).debug("chainId={} bathValidateBegin", chainId);
        coinDataValidator.beginBatchPerTxValidate(chainId);
        Map<String, Object> rtData = new HashMap<>();
        rtData.put("value", 1);
        LoggerUtil.logger(chainId).debug("return={}", success(rtData));
        return success(rtData);
    }

    /**
     * 接收到peer区块时调用验证
     *
     * @param params
     * @return
     */

    @CmdAnnotation(cmd = "blockValidate",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHexList", parameterType = "List")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response blockValidate(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        long blockHeight = Long.valueOf(params.get("blockHeight").toString());
        List<String> txHexList = (List) params.get("txHexList");
        LoggerUtil.logger(chainId).debug("chainId={} blockHeight={} blockValidate", chainId, blockHeight);
        if (null == txHexList || 0 == txHexList.size()) {
            LoggerUtil.logger(chainId).error("txHexList is blank");
            return failed("txHexList is blank");
        }
        LoggerUtil.logger(chainId).debug("commitBlockTxs txHexListSize={}", txHexList.size());
        List<Transaction> txList = new ArrayList<>();
        Response parseResponse = parseTxs(txHexList, txList,chainId);
        if (!parseResponse.isSuccess()) {
            LoggerUtil.logger(chainId).debug("commitBlockTxs response={}", parseResponse);
            return parseResponse;
        }
        Map<String, Object> rtData = new HashMap<>();
        if (coinDataValidator.blockValidate(chainId, blockHeight, txList)) {
            rtData.put("value", 1);
        } else {
            rtData.put("value", 0);
        }
        LoggerUtil.logger(chainId).debug("chainId={} blockHeight={},return={}", chainId, blockHeight, success(rtData));
        return success(rtData);
    }
}
