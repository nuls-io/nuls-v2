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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * 未确认交易提交，提交失败直接返回错误信息
 * Created by wangkun23 on 2018/11/20.
 */
@Component
public class TransactionCmd extends BaseCmd {

    @Autowired
    private TransactionService transactionService;


    Response parseTxs(List<String> txHexList, List<Transaction> txList) {
        for (String txHex : txHexList) {
            if (StringUtils.isBlank(txHex)) {
                return failed("txHex is blank");
            }
            byte[] txStream = HexUtil.decode(txHex);
            Transaction tx = new Transaction();
            try {
                tx.parse(new NulsByteBuffer(txStream));
                txList.add(tx);
            } catch (NulsException e) {
                logger.error("transaction parse error", e);
                return failed("transaction parse error");
            }
        }
        return success();
    }

    Transaction parseTxs(String txHex) {
        if (StringUtils.isBlank(txHex)) {
            return null;
        }
        byte[] txStream = HexUtil.decode(txHex);
        Transaction tx = new Transaction();
        try {
            tx.parse(new NulsByteBuffer(txStream));
        } catch (NulsException e) {
            logger.error("transaction parse error", e);
            return null;
        }
        return tx;
    }


    /**
     * 未确认交易提交
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "commitUnconfirmedTx",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHex", parameterType = "String")
    public Response commitUnconfirmedTx(Map params) {
        Map<String, Object> rtData = new HashMap<>();
        Integer chainId = (Integer) params.get("chainId");
        String txHex = params.get("txHex").toString();
        LoggerUtil.logger.debug("commitUnconfirmedTx chainId={},txHex={}", chainId, txHex);
        Transaction tx = parseTxs(txHex);
        if (null == tx) {
            LoggerUtil.logger.error("txHex is invalid chainId={},txHex={}", chainId, txHex);
            return failed("txHex is invalid");
        }
        int value = 0;
        if (transactionService.unConfirmTxProcess(chainId, tx)) {
            value = 1;
        } else {
            value = 0;
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger.debug(" chainId={},txHex={},response={}", chainId, txHex, response);
        return response;
    }

    /**
     * 区块交易提交
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "commitBlockTxs",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHexList", parameterType = "List")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response commitBlockTxs(Map params) {
        Map<String, Object> rtData = new HashMap<>();
        Integer chainId = (Integer) params.get("chainId");
        List<String> txHexList = (List) params.get("txHexList");
        long blockHeight = Long.valueOf(params.get("blockHeight").toString());
        LoggerUtil.logger.debug("commitBlockTxs chainId={},blockHeight={}", chainId,blockHeight);
        if (null == txHexList || 0 == txHexList.size()) {
            LoggerUtil.logger.error("txHexList is blank");
            return failed("txHexList is blank");
        }
        LoggerUtil.logger.debug("commitBlockTxs txHexList={}",txHexList.size());
        int value = 0;
        List<Transaction> txList = new ArrayList<>();
        Response parseResponse = parseTxs(txHexList, txList);
        if (!parseResponse.isSuccess()) {
            LoggerUtil.logger.debug("commitBlockTxs response={}", parseResponse);
            return parseResponse;
        }

        if (transactionService.confirmBlockProcess(chainId, txList, blockHeight)) {
            value = 1;
        } else {
            value = 0;
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger.debug("response={}", response);
        return response;
    }

    /**
     * 逐笔回滚未确认交易
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "rollBackUnconfirmTx",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHex", parameterType = "String")
    public Response rollBackUnconfirmTx(Map params) {
        Map<String, Object> rtData = new HashMap<>();
        int value = 0;
        try {
            Integer chainId = (Integer) params.get("chainId");
            String txHex = params.get("txHex").toString();
            LoggerUtil.logger.debug("rollBackUnconfirmTx chainId={},txHex={}", chainId, txHex);
            Transaction tx = parseTxs(txHex);
            if (null == tx) {
                LoggerUtil.logger.debug("txHex is invalid chainId={}", chainId);
                return failed("txHex is invalid");
            }
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
        LoggerUtil.logger.debug("response={}", response);
        return response;

    }


    /**
     * 回滚区块交易
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "rollBackBlockTxs",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "blockHeight", parameterType = "long")
    public Response rollBackBlockTxs(Map params) {
        Map<String, Object> rtData = new HashMap<>();
        int value = 0;
        try {
            Integer chainId = (Integer) params.get("chainId");
            long blockHeight = Long.valueOf(params.get("blockHeight").toString());
            LoggerUtil.logger.debug("rollBackBlockTxs chainId={},blockHeight={}", chainId, blockHeight);
            if (transactionService.rollBackConfirmTxs(chainId, blockHeight)) {
                value = 1;
            } else {
                value = 0;
            }
        } catch (Exception e) {
            LoggerUtil.logger.error(e);
            e.printStackTrace();
        }
        rtData.put("value", value);
        Response response = success(rtData);
        LoggerUtil.logger.debug("rollBackBlockTxs response={}", response);
        return response;
    }
}
