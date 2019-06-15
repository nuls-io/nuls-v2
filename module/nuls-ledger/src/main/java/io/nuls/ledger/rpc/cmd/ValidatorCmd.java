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

import io.nuls.base.RPCUtil;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.ledger.validator.CoinDataValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/22.
 * 校验rpc接口
 *
 * @author lanjinsheng
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
    @CmdAnnotation(cmd = CmdConstant.CMD_VERIFY_COINDATA_PACKAGED,
            version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response verifyCoinDataPackaged(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        String txStr = (String) params.get("tx");
        Transaction tx = new Transaction();
        Response response = null;
        ValidateResult validateResult = null;
        try {
//            LockerUtil.LEDGER_LOCKER.lock();
            tx.parse(RPCUtil.decode(txStr), 0);
            LoggerUtil.logger(chainId).debug("确认交易校验：chainId={},txHash={}", chainId, tx.getHash().toHex());
            validateResult = coinDataValidator.bathValidatePerTx(chainId, tx);
            Map<String, Object> rtMap = new HashMap<>(1);
            if (validateResult.isSuccess() || validateResult.isOrphan()) {
                rtMap.put("orphan", validateResult.isOrphan());
                response = success(rtMap);
            } else {
                response = failed(validateResult.toErrorCode());
            }
            if (!validateResult.isSuccess()) {
                LoggerUtil.logger(chainId).debug("validateCoinData returnCode={},returnMsg={}", validateResult.getValidateCode(), validateResult.getValidateDesc());
            }
        } catch (NulsException e) {
            response = failed(e.getErrorCode());
            LoggerUtil.logger(chainId).error("validateCoinData exception:{}", e);
        } catch (Exception e) {
            response = failed("validateCoinData exception");
            LoggerUtil.logger(chainId).error("validateCoinData exception:{}", e);
        } finally {
//            LockerUtil.LEDGER_LOCKER.unlock();
        }
        return response;
    }

    /**
     * validate coin entity
     * 进行nonce-hash校验，进行可用余额校验
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_VERIFY_COINDATA_BATCH_PACKAGED,
            version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    public Response verifyCoinDataBatchPackaged(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        try {
//            LockerUtil.LEDGER_LOCKER.lock();
            List<String> txStrList = (List) params.get("txList");
            List<Transaction> txList = new ArrayList<>();
            Response parseResponse = parseTxs(txStrList, txList, chainId);
            if (!parseResponse.isSuccess()) {
                LoggerUtil.logger(chainId).debug("verifyCoinDataBatchPackaged response={}", parseResponse);
                return parseResponse;
            }
            List<String> orphanList = new ArrayList<>();
            List<String> successList = new ArrayList<>();
            List<String> failList = new ArrayList<>();
            for (Transaction tx : txList) {
                String txHash = tx.getHash().toHex();
                ValidateResult validateResult = coinDataValidator.bathValidatePerTx(chainId, tx);
                ;
                if (validateResult.isSuccess()) {
                    //success
                    successList.add(txHash);
//                    LoggerUtil.logger(chainId).debug("verifyCoinDataBatchPackaged success txHash={}", txHash);
                } else if (validateResult.isOrphan()) {
                    orphanList.add(txHash);
                    LoggerUtil.logger(chainId).debug("verifyCoinDataBatchPackaged Orphan txHash={}", txHash);
                } else {
                    failList.add(txHash);
                    LoggerUtil.logger(chainId).debug("verifyCoinDataBatchPackaged failed txHash={}", txHash);
                }
            }

            Map<String, Object> rtMap = new HashMap<>(3);
            rtMap.put("fail", failList);
            rtMap.put("orphan", orphanList);
            rtMap.put("success", successList);
            return success(rtMap);
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error("verifyCoinDataBatchPackaged exception ={}", e);
            return failed(LedgerErrorCode.SYS_UNKOWN_EXCEPTION);
        } finally {
//            LockerUtil.LEDGER_LOCKER.unlock();
        }
    }

    /**
     * validate coin entity
     * 进行nonce-hash校验，进行单笔交易的未确认校验
     * 用于第三方打包交易校验
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_VERIFY_COINDATA,
            version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response verifyCoinData(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        String txStr = (String) params.get("tx");
        Transaction tx = new Transaction();
        Response response = null;
        ValidateResult validateResult = null;
        try {
            tx.parse(RPCUtil.decode(txStr), 0);
//            LoggerUtil.logger(chainId).debug("交易coinData校验：chainId={},txHash={}", chainId, tx.getHash().toString());
            validateResult = coinDataValidator.verifyCoinData(chainId, tx);
            Map<String, Object> rtMap = new HashMap<>(1);
            if (validateResult.isSuccess() || validateResult.isOrphan()) {
                rtMap.put("orphan", validateResult.isOrphan());
                response = success(rtMap);
            } else {
                response = failed(validateResult.toErrorCode());
            }
            if (!validateResult.isSuccess()) {
                LoggerUtil.logger(chainId).debug("validateCoinData returnCode={},returnMsg={}", validateResult.getValidateCode(), validateResult.getValidateDesc());
            }
        } catch (NulsException e) {
            response = failed(e.getErrorCode());
            LoggerUtil.logger(chainId).error("validateCoinData exception:{}", e);
        } catch (Exception e) {
            response = failed("validateCoinData exception");
            LoggerUtil.logger(chainId).error("validateCoinData exception:{}", e);
        }
        return response;
    }

    /**
     * 回滚打包确认交易状态
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_ROLLBACKTX_VALIDATE_STATUS,
            version = 1.0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "tx", parameterType = "String")
    public Response rollbackTxValidateStatus(Map params) {
        Map<String, Object> rtData = new HashMap<>(1);
        boolean value = false;
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        try {
            String txStr = params.get("tx").toString();
            LoggerUtil.logger(chainId).debug("rollbackrTxValidateStatus chainId={}", chainId);
            Transaction tx = parseTxs(txStr, chainId);
            if (null == tx) {
                LoggerUtil.logger(chainId).debug("txHex is invalid chainId={},txHex={}", chainId, txStr);
                return failed("txHex is invalid");
            }
            LoggerUtil.logger(chainId).debug("rollbackrTxValidateStatus chainId={},txHash={}", chainId, tx.getHash().toHex());
            //未确认回滚已被调用方处理过了
//            transactionService.rollBackUnconfirmTx(chainId, tx);
            if (coinDataValidator.rollbackTxValidateStatus(chainId, tx)) {
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

    /**
     * bathValidateBegin
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_BATCH_VALIDATE_BEGIN,
            version = 1.0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response batchValidateBegin(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        try {
//            LockerUtil.LEDGER_LOCKER.lock();
            LoggerUtil.logger(chainId).debug("chainId={} batchValidateBegin", chainId);
            coinDataValidator.beginBatchPerTxValidate(chainId);
        } finally {
//            LockerUtil.LEDGER_LOCKER.unlock();
        }
        Map<String, Object> rtData = new HashMap<>(1);
        rtData.put("value", true);
        return success(rtData);
    }

    /**
     * 接收到peer区块时调用验证
     *
     * @param params
     * @return
     */

    @CmdAnnotation(cmd = CmdConstant.CMD_BLOCK_VALIDATE,
            version = 1.0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txList", parameterType = "List")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response blockValidate(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        long blockHeight = Long.valueOf(params.get("blockHeight").toString());
        List<String> txStrList = (List) params.get("txList");
        LoggerUtil.logger(chainId).debug("chainId={} blockHeight={} blockValidate", chainId, blockHeight);
        if (null == txStrList || 0 == txStrList.size()) {
            LoggerUtil.logger(chainId).error("txStrList is blank");
            return failed("txStrList is blank");
        }
        LoggerUtil.logger(chainId).debug("commitBlockTxs txHexListSize={}", txStrList.size());
        List<Transaction> txList = new ArrayList<>();
        Response parseResponse = parseTxs(txStrList, txList, chainId);
        if (!parseResponse.isSuccess()) {
            LoggerUtil.logger(chainId).debug("commitBlockTxs response={}", parseResponse);
            return parseResponse;
        }
        Map<String, Object> rtData = new HashMap<>(1);
        if (coinDataValidator.blockValidate(chainId, blockHeight, txList)) {
            rtData.put("value", true);
        } else {
            rtData.put("value", false);
        }
        LoggerUtil.logger(chainId).debug("chainId={} blockHeight={},return={}", chainId, blockHeight, success(rtData));
        return success(rtData);
    }
}
