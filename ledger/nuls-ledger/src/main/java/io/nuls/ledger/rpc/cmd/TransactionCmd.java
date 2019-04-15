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
import io.nuls.ledger.manager.LedgerChainManager;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 未确认交易提交，提交失败直接返回错误信息
 *
 * @author lanjinsheng
 * @date 2018/11/20
 */
@Component
public class TransactionCmd extends BaseLedgerCmd {

    @Autowired
    private TransactionService transactionService;

    /**
     * 未确认交易提交
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "commitUnconfirmedTx",
            version = 1.0,  minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response commitUnconfirmedTx(Map params) {
        long time1 = System.currentTimeMillis();
        Integer chainId = (Integer) params.get("chainId");
        Response response = null;
        try {
            String txStr = params.get("tx").toString();
            Transaction tx = parseTxs(txStr, chainId);
            if (null == tx) {
                LoggerUtil.logger(chainId).error("txStr is invalid chainId={},txHex={}", chainId, txStr);
                return failed("txStr is invalid");
            }
            ValidateResult validateResult = transactionService.unConfirmTxProcess(chainId, tx);
            response = success(validateResult);
            LoggerUtil.logger(chainId).debug("####commitUnconfirmedTx chainId={},txHash={},value={}=={}", chainId, tx.getHash().toString(), validateResult.getValidateCode(),validateResult.getValidateDesc());
        } catch (Exception e) {
            e.printStackTrace();
            LoggerUtil.logger(chainId).error("commitUnconfirmedTx exception ={}", e.getMessage());
            return failed(e.getMessage());
        }
        return response;
    }

    /**
     * 区块交易提交
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "commitBlockTxs",
            version = 1.0,  minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response commitBlockTxs(Map params) {
        Map<String, Object> rtData = new HashMap<>(1);
        Integer chainId = (Integer) params.get("chainId");
        long blockHeight = Long.valueOf(params.get("blockHeight").toString());
        List<String> txStrList = (List) params.get("txList");
        if (blockHeight == 0) {
            //进行创世初始化
            try {
                SpringLiteContext.getBean(LedgerChainManager.class).addChain(chainId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        LoggerUtil.logger(chainId).debug("commitBlockTxs chainId={},blockHeight={}", chainId, blockHeight);
        if (null == txStrList || 0 == txStrList.size()) {
            LoggerUtil.logger(chainId).error("txList is blank");
            return failed("txList is blank");
        }
        LoggerUtil.logger(chainId).debug("commitBlockTxs txHexList={}", txStrList.size());
        int value = 0;
        List<Transaction> txList = new ArrayList<>();
        Response parseResponse = parseTxs(txStrList, txList, chainId);
        if (!parseResponse.isSuccess()) {
            LoggerUtil.logger(chainId).debug("commitBlockTxs response={}", parseResponse);
            return parseResponse;
        }

        if (transactionService.confirmBlockProcess(chainId, txList, blockHeight)) {
            value = 1;
        } else {
            value = 0;
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger(chainId).debug("response={}", response);
        return response;
    }

    /**
     * 逐笔回滚未确认交易
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "rollBackUnconfirmTx",
            version = 1.0, minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response rollBackUnconfirmTx(Map params) {
        Map<String, Object> rtData = new HashMap<>(1);
        int value = 0;
        Integer chainId = (Integer) params.get("chainId");
        try {
            String txStr = params.get("tx").toString();
            LoggerUtil.logger(chainId).debug("rollBackUnconfirmTx chainId={}", chainId);
            Transaction tx = parseTxs(txStr, chainId);
            if (null == tx) {
                LoggerUtil.logger(chainId).debug("tx is invalid chainId={},txHex={}", chainId, txStr);
                return failed("tx is invalid");
            }
            LoggerUtil.txUnconfirmedRollBackLog(chainId).debug("rollBackUnconfirmTx chainId={},txHash={}", chainId, tx.getHash().toString());
            if (transactionService.rollBackUnconfirmTx(chainId, tx)) {
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
     * 回滚区块交易
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "rollBackBlockTxs",
            version = 1.0,  minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    @Parameter(parameterName = "txList", parameterType = "List")
    public Response rollBackBlockTxs(Map params) {
        Map<String, Object> rtData = new HashMap<>(1);
        int value = 0;
        Integer chainId = (Integer) params.get("chainId");
        try {
            long blockHeight = Long.valueOf(params.get("blockHeight").toString());
            List<String> txStrList = (List) params.get("txList");
            if (null == txStrList || 0 == txStrList.size()) {
                LoggerUtil.logger(chainId).error("txList is blank");
                return failed("txList is blank");
            }
            LoggerUtil.logger(chainId).debug("rollBackBlockTxs txStrList={}", txStrList.size());
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txStrList, txList, chainId);
            if (!parseResponse.isSuccess()) {
                LoggerUtil.logger(chainId).debug("commitBlockTxs response={}", parseResponse);
                return parseResponse;
            }

            LoggerUtil.txRollBackLog(chainId).debug("rollBackBlockTxs chainId={},blockHeight={}", chainId, blockHeight);
            if (transactionService.rollBackConfirmTxs(chainId, blockHeight, txList)) {
                value = 1;
            } else {
                value = 0;
            }
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
            e.printStackTrace();
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger(chainId).debug("rollBackBlockTxs response={}", response);
        return response;
    }
}
