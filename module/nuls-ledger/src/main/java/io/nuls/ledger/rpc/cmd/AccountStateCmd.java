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

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.ledger.constant.CmdConstant;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.FreezeLockState;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.FreezeHeightState;
import io.nuls.ledger.model.po.FreezeLockTimeState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用于获取账户余额及账户nonce值
 * Created by wangkun23 on 2018/11/19
 *
 * @author lanjinsheng .
 */
@Component
public class AccountStateCmd extends BaseLedgerCmd {


    @Autowired
    private AccountStateService accountStateService;
    @Autowired
    private UnconfirmedStateService unconfirmedStateService;

    /**
     * 获取账户资产余额
     * get user account balance
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_BALANCE,
            version = 1.0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "assetChainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "assetId", parameterType = "int")
    public Response getBalance(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = (Integer) params.get("assetChainId");
        String address = (String) params.get("address");
        Integer assetId = (Integer) params.get("assetId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        LoggerUtil.logger(chainId).debug("chainId={},assetChainId={},address={},assetId={}", chainId, assetChainId, address, assetId);
        AccountState accountState = accountStateService.getAccountStateReCal(address, chainId, assetChainId, assetId);
        Map<String, Object> rtMap = new HashMap<>(5);
        rtMap.put("freeze", accountState.getFreezeTotal());
        rtMap.put("total", accountState.getTotalAmount());
        rtMap.put("available", accountState.getAvailableAmount());
        BigInteger permanentLocked = BigInteger.ZERO;
        BigInteger timeHeightLocked = BigInteger.ZERO;
        for (FreezeLockTimeState freezeLockTimeState : accountState.getFreezeLockTimeStates()) {
            if (LedgerConstant.PERMANENT_LOCK == freezeLockTimeState.getLockTime()) {
                permanentLocked = permanentLocked.add(freezeLockTimeState.getAmount());
            } else {
                timeHeightLocked = timeHeightLocked.add(freezeLockTimeState.getAmount());
            }
        }
        for (FreezeHeightState freezeHeightState : accountState.getFreezeHeightStates()) {
            timeHeightLocked = timeHeightLocked.add(freezeHeightState.getAmount());
        }
        rtMap.put("permanentLocked", permanentLocked);
        rtMap.put("timeHeightLocked", timeHeightLocked);
        Response response = success(rtMap);
        LoggerUtil.logger(chainId).debug("response={}", response);
        return response;
    }

    /**
     * 获取账户锁定列表
     * get user account freeze
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_FREEZE_LIST,
            version = 1.0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "assetId", parameterType = "int")
    @Parameter(parameterName = "pageNumber", parameterType = "int")
    @Parameter(parameterName = "pageSize", parameterType = "int")
    public Response getFreezeList(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = chainId;
        String address = (String) params.get("address");
        Integer assetId = (Integer) params.get("assetId");
        Integer pageNumber = (Integer) params.get("pageNumber");
        Integer pageSize = (Integer) params.get("pageSize");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        AccountState accountState = accountStateService.getAccountStateReCal(address, chainId, assetChainId, assetId);
        List<FreezeLockState> freezeLockStates = new ArrayList<>();

        for (FreezeLockTimeState freezeLockTimeState : accountState.getFreezeLockTimeStates()) {
            FreezeLockState freezeLockState = new FreezeLockState();
            freezeLockState.setAmount(freezeLockTimeState.getAmount());
            freezeLockState.setLockedValue(freezeLockTimeState.getLockTime());
            freezeLockState.setTime(freezeLockTimeState.getCreateTime());
            freezeLockState.setTxHash(freezeLockTimeState.getTxHash());
            freezeLockStates.add(freezeLockState);
        }
        for (FreezeHeightState freezeHeightState : accountState.getFreezeHeightStates()) {
            FreezeLockState freezeLockState = new FreezeLockState();
            freezeLockState.setAmount(freezeHeightState.getAmount());
            freezeLockState.setLockedValue(freezeHeightState.getHeight());
            freezeLockState.setTime(freezeHeightState.getCreateTime());
            freezeLockState.setTxHash(freezeHeightState.getTxHash());
            freezeLockStates.add(freezeLockState);
        }
        freezeLockStates.sort((x, y) -> Long.compare(y.getTime(), x.getTime()));
        //get by page
        int currIdx = (pageNumber > 1 ? (pageNumber - 1) * pageSize : 0);
        List<FreezeLockState> resultList = new ArrayList<>();
        if ((currIdx + pageSize) > freezeLockStates.size()) {
            resultList = freezeLockStates.subList(currIdx, freezeLockStates.size());
        } else {
            resultList = freezeLockStates.subList(currIdx, currIdx + pageSize);
        }
        Map<String, Object> rtMap = new HashMap<>(4);
        rtMap.put("totalCount", freezeLockStates.size());
        rtMap.put("pageNumber", pageNumber);
        rtMap.put("pageSize", pageSize);
        rtMap.put("list", resultList);
        return success(rtMap);
    }

    /**
     * 获取账户nonce值
     * get user account nonce
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_NONCE,
            version = 1.0,
            description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "assetChainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "assetId", parameterType = "int")
    public Response getNonce(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = (Integer) params.get("assetChainId");
        String address = (String) params.get("address");
        Integer assetId = (Integer) params.get("assetId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        Map<String, Object> rtMap = new HashMap<>(2);
        AccountState accountState = accountStateService.getAccountState(address, chainId, assetChainId, assetId);
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedInfo(accountState);
        if (null == accountStateUnconfirmed) {
            rtMap.put("nonce", RPCUtil.encode(accountState.getNonce()));
            rtMap.put("nonceType", LedgerConstant.CONFIRMED_NONCE);
        } else {
            rtMap.put("nonce", RPCUtil.encode(accountStateUnconfirmed.getNonce()));
            rtMap.put("nonceType", LedgerConstant.UNCONFIRMED_NONCE);
        }
        LoggerUtil.logger(chainId).debug("####address={}.getNonce={}", address, rtMap.get("nonce").toString());
        return success(rtMap);
    }

    @CmdAnnotation(cmd = CmdConstant.CMD_GET_BALANCE_NONCE,
            version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "assetChainId", parameterType = "int")
    @Parameter(parameterName = "address", parameterType = "String")
    @Parameter(parameterName = "assetId", parameterType = "int")
    public Response getBalanceNonce(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = (Integer) params.get("assetChainId");
        String address = (String) params.get("address");
        Integer assetId = (Integer) params.get("assetId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        LoggerUtil.logger(chainId).debug("chainId={},assetChainId={},address={},assetId={}", chainId, assetChainId, address, assetId);
        AccountState accountState = accountStateService.getAccountStateReCal(address, chainId, assetChainId, assetId);
        Map<String, Object> rtMap = new HashMap<>(6);
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedInfo(accountState);
        if (null == accountStateUnconfirmed) {
            rtMap.put("nonce", RPCUtil.encode(accountState.getNonce()));
            rtMap.put("nonceType", LedgerConstant.CONFIRMED_NONCE);
            rtMap.put("available", accountState.getAvailableAmount());
        } else {
            rtMap.put("available", accountState.getAvailableAmount().subtract(accountStateUnconfirmed.getAmount()));
            rtMap.put("nonce", RPCUtil.encode(accountStateUnconfirmed.getNonce()));
            rtMap.put("nonceType", LedgerConstant.UNCONFIRMED_NONCE);
        }
        rtMap.put("freeze", accountState.getFreezeTotal());
        BigInteger permanentLocked = BigInteger.ZERO;
        BigInteger timeHeightLocked = BigInteger.ZERO;
        for (FreezeLockTimeState freezeLockTimeState : accountState.getFreezeLockTimeStates()) {
            if (LedgerConstant.PERMANENT_LOCK == freezeLockTimeState.getLockTime()) {
                permanentLocked = permanentLocked.add(freezeLockTimeState.getAmount());
            } else {
                timeHeightLocked = timeHeightLocked.add(freezeLockTimeState.getAmount());
            }
        }
        for (FreezeHeightState freezeHeightState : accountState.getFreezeHeightStates()) {
            timeHeightLocked = timeHeightLocked.add(freezeHeightState.getAmount());
        }
        rtMap.put("permanentLocked", permanentLocked);
        rtMap.put("timeHeightLocked", timeHeightLocked);
        Response response = success(rtMap);
        return response;
    }

}
