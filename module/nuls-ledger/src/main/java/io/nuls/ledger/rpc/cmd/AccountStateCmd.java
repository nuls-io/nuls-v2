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
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
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
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_BALANCE, version = 1.0,
            description = "获取账户资产(已入区块)")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetChainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "资产所在地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "total", valueType = BigInteger.class, description = "总金额"),
                    @Key(name = "freeze", valueType = BigInteger.class, description = "冻结金额"),
                    @Key(name = "available", valueType = String.class, description = "可用金额")
            })
    )
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
        return response;
    }

    /**
     * 获取账户锁定列表
     * get user account freeze
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_FREEZE_LIST, version = 1.0,
            description = "分页获取账户锁定资产列表")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "资产所在地址"),
            @Parameter(parameterName = "pageNumber", parameterType = "int", parameterDes = "起始页数"),
            @Parameter(parameterName = "pageSize", parameterType = "int", parameterDes = "每页显示数量")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "totalCount", valueType = Integer.class, description = "记录总数"),
                    @Key(name = "pageNumber", valueType = Integer.class, description = "起始页数"),
                    @Key(name = "pageSize", valueType = Integer.class, description = "每页显示数量"),
                    @Key(name = "list", valueType = List.class, valueElement = FreezeLockState.class, description = "锁定金额列表")
            })
    )
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
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_NONCE, version = 1.0,
            description = "获取账户资产NONCE值")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetChainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "资产所在地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "nonce", valueType = String.class, description = "账户资产nonce值"),
                    @Key(name = "nonceType", valueType = Integer.class, description = "1：已确认的nonce值,0：未确认的nonce值")

            })
    )
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
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedInfo(address, chainId, assetChainId, assetId, accountState);
        if (null == accountStateUnconfirmed) {
            rtMap.put("nonce", RPCUtil.encode(accountState.getNonce()));
            rtMap.put("nonceType", LedgerConstant.CONFIRMED_NONCE);
        } else {
            rtMap.put("nonce", RPCUtil.encode(accountStateUnconfirmed.getNonce()));
            rtMap.put("nonceType", LedgerConstant.UNCONFIRMED_NONCE);
        }
        return success(rtMap);
    }

    @CmdAnnotation(cmd = CmdConstant.CMD_GET_BALANCE_NONCE, version = 1.0,
            description = "获取账户资产余额与NONCE值")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "运行的链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetChainId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "资产链Id,取值区间[1-65535]"),
            @Parameter(parameterName = "assetId", parameterType = "int", parameterValidRange = "[1-65535]", parameterDes = "资产Id,取值区间[1-65535]"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "资产所在地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "nonce", valueType = String.class, description = "账户资产nonce值"),
                    @Key(name = "nonceType", valueType = Integer.class, description = "1：已确认的nonce值,0：未确认的nonce值"),
                    @Key(name = "available", valueType = BigInteger.class, description = "可用金额"),
                    @Key(name = "permanentLocked", valueType = BigInteger.class, description = "永久锁定金额"),
                    @Key(name = "timeHeightLocked", valueType = BigInteger.class, description = "高度或时间锁定金额")
            })
    )
    public Response getBalanceNonce(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = (Integer) params.get("assetChainId");
        String address = (String) params.get("address");
        Integer assetId = (Integer) params.get("assetId");
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        AccountState accountState = accountStateService.getAccountStateReCal(address, chainId, assetChainId, assetId);
        Map<String, Object> rtMap = new HashMap<>(6);
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedInfo(address, chainId, assetChainId, assetId, accountState);
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
