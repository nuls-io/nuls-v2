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
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.utils.LoggerUtil;

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
    @Autowired
    private UnconfirmedStateService unconfirmedStateService;

    /**
     * 未确认交易提交
     *
     * @param params
     * @return
     */

    @CmdAnnotation(cmd = CmdConstant.CMD_COMMIT_UNCONFIRMED_TX, version = 1.0,
            description = "未确认交易提交账本(校验并更新nonce值)")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "交易Hex值")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "orphan", valueType = Boolean.class, description = "true 孤儿交易，false 非孤儿交易")
            })
    )
    public Response commitUnconfirmedTx(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        Response response = null;
        try {
            String txStr = params.get("tx").toString();
            Transaction tx = parseTxs(txStr, chainId);
            if (null == tx) {
                LoggerUtil.logger(chainId).error("txStr is invalid chainId={},txHex={}", chainId, txStr);
                return failed(LedgerErrorCode.TX_IS_WRONG);
            }
            ValidateResult validateResult = transactionService.unConfirmTxProcess(chainId, tx);
            Map<String, Object> rtMap = new HashMap<>(1);
            if (validateResult.isSuccess() || validateResult.isOrphan()) {
                rtMap.put("orphan", validateResult.isOrphan());
                response = success(rtMap);
            } else {
                response = failed(validateResult.toErrorCode());
                LoggerUtil.logger(chainId).error("####commitUnconfirmedTx chainId={},txHash={},value={}=={}", chainId, tx.getHash().toHex(), validateResult.getValidateCode(), validateResult.getValidateDesc());
            }
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error("commitUnconfirmedTx exception ={}", e);
            return failed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        return response;
    }

    /**
     * 未确认交易提交
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_COMMIT_UNCONFIRMED_TXS, version = 1.0,
            description = "未确认交易批量提交账本(校验并更新nonce值)")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "txList", parameterType = "List", parameterDes = "[]交易Hex值列表")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "orphan", valueType = List.class, valueElement = String.class, description = "孤儿交易Hash列表"),
                    @Key(name = "fail", valueType = List.class, valueElement = String.class, description = "校验失败交易Hash列表")
            })
    )
    public Response commitBatchUnconfirmedTxs(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        try {
            List<String> txStrList = (List) params.get("txList");
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txStrList, txList, chainId);
            if (!parseResponse.isSuccess()) {
                LoggerUtil.logger(chainId).debug("commitBatchUnconfirmedTxs response={}", parseResponse);
                return parseResponse;
            }
            List<String> orphanList = new ArrayList<>();
            List<String> failList = new ArrayList<>();
            for (Transaction tx : txList) {
                String txHash = tx.getHash().toHex();
                ValidateResult validateResult = transactionService.unConfirmTxProcess(chainId, tx);
                if (validateResult.isSuccess()) {
                    //success
                } else if (validateResult.isOrphan()) {
                    orphanList.add(txHash);
                } else {
                    failList.add(txHash);
                }
            }

            Map<String, Object> rtMap = new HashMap<>(2);
            rtMap.put("fail", failList);
            rtMap.put("orphan", orphanList);
            return success(rtMap);
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error("commitBatchUnconfirmedTxs exception ={}", e);
            return failed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    /**
     * 区块交易提交
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_COMMIT_BLOCK_TXS, version = 1.0,
            description = "提交区块")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "txList", parameterType = "List", parameterDes = "交易Hex值列表"),
            @Parameter(parameterName = "blockHeight", parameterType = "long", parameterDes = "区块高度")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = Boolean.class, description = "true 成功，false 失败")
            })
    )
    public Response commitBlockTxs(Map params) {
        Map<String, Object> rtData = new HashMap<>(1);
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        long blockHeight = Long.valueOf(params.get("blockHeight").toString());
        List<String> txStrList = (List) params.get("txList");
        LoggerUtil.logger(chainId).debug("commitBlockTxs chainId={},blockHeight={}", chainId, blockHeight);
        if (null == txStrList || 0 == txStrList.size()) {
            LoggerUtil.logger(chainId).error("txList is blank");
            return failed("txList is blank");
        }
        boolean value = false;
        long time1 = System.currentTimeMillis();
        List<Transaction> txList = new ArrayList<>();
        Response parseResponse = parseTxs(txStrList, txList, chainId);
        long time2 = System.currentTimeMillis();
        LoggerUtil.logger(chainId).debug("commitBlockTxs txHexList={} time={}", txStrList.size(), time2 - time1);
        if (!parseResponse.isSuccess()) {
            LoggerUtil.logger(chainId).debug("commitBlockTxs response={}", parseResponse);
            return parseResponse;
        }

        if (transactionService.confirmBlockProcess(chainId, txList, blockHeight)) {
            value = true;
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
    @CmdAnnotation(cmd = CmdConstant.CMD_ROLLBACK_UNCONFIRMED_TX, version = 1.0,
            description = "回滚提交的未确认交易")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "交易Hex值")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = Boolean.class, description = "true 成功，false 失败")
            })
    )
    public Response rollBackUnconfirmTx(Map params) {
        Map<String, Object> rtData = new HashMap<>(1);
        boolean value = false;
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        try {
            String txStr = params.get("tx").toString();
            Transaction tx = parseTxs(txStr, chainId);
            if (null == tx) {
                LoggerUtil.logger(chainId).debug("tx is invalid chainId={},txHex={}", chainId, txStr);
                return failed("tx is invalid");
            }
            LoggerUtil.logger(chainId).debug("rollBackUnconfirmTx chainId={},txHash={}", chainId, tx.getHash().toHex());
            if (transactionService.rollBackUnconfirmTx(chainId, tx)) {
                value = true;
            }
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger(chainId).debug("response={}", response);
        return response;

    }

    @CmdAnnotation(cmd = CmdConstant.CMD_CLEAR_UNCONFIRMED_TXS, version = 1.0,
            description = "清除所有账户未确认交易")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = Boolean.class, description = "true 成功，false 失败")
            })
    )
    public Response clearUnconfirmTxs(Map params) {
        Map<String, Object> rtData = new HashMap<>(1);
        boolean value = false;
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        try {
            LoggerUtil.logger(chainId).debug("clearUnconfirmTxs chainId={}", chainId);
            unconfirmedStateService.clearAllAccountUnconfirmed(chainId);
            value = true;
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
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
    @CmdAnnotation(cmd = CmdConstant.CMD_ROLLBACK_BLOCK_TXS, version = 1.0,
            description = "区块回滚")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "txList", parameterType = "List", parameterDes = "[]交易Hex值列表"),
            @Parameter(parameterName = "blockHeight", parameterType = "long", parameterDes = "区块高度")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = Boolean.class, description = "true 成功，false 失败")
            })
    )
    public Response rollBackBlockTxs(Map params) {
        Map<String, Object> rtData = new HashMap<>(1);
        boolean value = false;
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        try {
            long blockHeight = Long.valueOf(params.get("blockHeight").toString());
            List<String> txStrList = (List) params.get("txList");
            if (null == txStrList || 0 == txStrList.size()) {
                LoggerUtil.logger(chainId).error("txList is blank");
                return failed("txList is blank");
            }
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txStrList, txList, chainId);
            if (!parseResponse.isSuccess()) {
                LoggerUtil.logger(chainId).debug("commitBlockTxs response={}", parseResponse);
                return parseResponse;
            }
            LoggerUtil.logger(chainId).debug("rollBackBlockTxs chainId={},blockHeight={},txStrList={}", chainId, blockHeight, txStrList.size());
            value = transactionService.rollBackConfirmTxs(chainId, blockHeight, txList);
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger(chainId).debug("rollBackBlockTxs response={}", response);
        return response;
    }
}
