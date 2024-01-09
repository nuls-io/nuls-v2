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
 * Unconfirmed transaction submission, submission failure returns error message directly
 *
 * @author lanjinsheng
 * @date 2018/11/20
 */
@Component
@NulsCoresCmd(module = ModuleE.LG)
public class LgmTransactionCmd extends BaseLedgerCmd {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private UnconfirmedStateService unconfirmedStateService;

    /**
     * Unconfirmed transaction submission
     *
     * @param params
     * @return
     */

    @CmdAnnotation(cmd = CmdConstant.CMD_COMMIT_UNCONFIRMED_TX, version = 1.0,
            description = "Unconfirmed transaction submission ledger(Verify and updatenoncevalue)")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "transactionHexvalue")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "orphan", valueType = Boolean.class, description = "true Orphan trading,false Non orphan transactions")
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
     * Unconfirmed transaction submission
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_COMMIT_UNCONFIRMED_TXS, version = 1.0,
            description = "Unconfirmed transaction batch submission ledger(Verify and updatenoncevalue)")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "txList", parameterType = "List", parameterDes = "[]transactionHexValue List")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "orphan", valueType = List.class, valueElement = String.class, description = "Orphan TradingHashlist"),
                    @Key(name = "fail", valueType = List.class, valueElement = String.class, description = "Verification failed transactionHashlist")
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
     * Block transaction submission
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_COMMIT_BLOCK_TXS, priority = CmdPriority.HIGH, version = 1.0,
            description = "Submit block")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "txList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "transactionHexValue List"),
            @Parameter(parameterName = "blockHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = Boolean.class, description = "true Success,false fail")
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
        LoggerUtil.logger(chainId).info("commitBlockTxs chainId={},blockHeight={},txs={}", chainId, blockHeight,txStrList.size());
        if (null == txStrList || 0 == txStrList.size()) {
            LoggerUtil.logger(chainId).error("txList is blank");
            return failed("txList is blank");
        }
        boolean value = false;
        List<Transaction> txList = new ArrayList<>();
        Response parseResponse = parseTxs(txStrList, txList, chainId);
        if (!parseResponse.isSuccess()) {
            LoggerUtil.logger(chainId).debug("commitBlockTxs response={}", parseResponse);
            return parseResponse;
        }
        if (transactionService.confirmBlockProcess(chainId, txList, blockHeight)) {
            value = true;
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger(chainId).info("response={}", value);
        return response;
    }

    /**
     * Rolling back unconfirmed transactions one by one
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_ROLLBACK_UNCONFIRMED_TX, version = 1.0,
            description = "Rollback submitted unconfirmed transactions")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "tx", requestType = @TypeDescriptor(value = String.class), parameterDes = "transactionHexvalue")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = Boolean.class, description = "true Success,false fail")
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
        LoggerUtil.logger(chainId).debug("response={}", rtData.get("value"));
        return response;

    }

    @CmdAnnotation(cmd = CmdConstant.CMD_CLEAR_UNCONFIRMED_TXS, version = 1.0,
            description = "Clear all unconfirmed transactions from accounts")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = Boolean.class, description = "true Success,false fail")
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
     * Rolling back block transactions
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_ROLLBACK_BLOCK_TXS, priority = CmdPriority.HIGH, version = 1.0,
            description = "Block rollback")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "txList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "[]transactionHexValue List"),
            @Parameter(parameterName = "blockHeight", parameterType = "long", parameterDes = "block height")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "value", valueType = Boolean.class, description = "true Success,false fail")
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
