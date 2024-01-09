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
import io.nuls.ledger.model.po.sub.FreezeHeightState;
import io.nuls.ledger.model.po.sub.FreezeLockTimeState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Used to obtain account balance and account informationnoncevalue
 * Created by wangkun23 on 2018/11/19
 *
 * @author lanjinsheng .
 */
@Component
@NulsCoresCmd(module = ModuleE.LG)
public class AccountStateCmd extends BaseLedgerCmd {


    @Autowired
    private AccountStateService accountStateService;
    @Autowired
    private UnconfirmedStateService unconfirmedStateService;

    /**
     * Obtain account asset balance
     * get user account balance
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_BALANCE, version = 1.0,
            description = "Obtain account assets(Blocked)")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Run ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Asset ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "assetId,Value range[1-65535]"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset location address")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "total", valueType = BigInteger.class, description = "Total amount"),
                    @Key(name = "freeze", valueType = BigInteger.class, description = "Freeze amount"),
                    @Key(name = "available", valueType = String.class, description = "Available amount")
            })
    )
    public Response getBalance(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = (Integer) params.get("assetChainId");
        String address = LedgerUtil.getRealAddressStr((String) params.get("address"));
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
            if (LedgerUtil.isPermanentLock(freezeLockTimeState.getLockTime())) {
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


    @CmdAnnotation(cmd = "getBalanceList", version = 1.0,
            description = "Obtain a collection of account assets")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetKeyList", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "assetkeyaggregate, [assetChainId-assetId]"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset location address"),
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "list", valueType = Map.class, description = "Account asset collection")
            })
    )
    public Response getBalanceList(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        List<String> assetKeyList = (List<String>) params.get("assetKeyList");
        if (assetKeyList == null) {
            return failed(LedgerErrorCode.PARAMETER_ERROR, "invalid `assetKeyList`");
        }
        String address = LedgerUtil.getRealAddressStr((String) params.get("address"));
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        Map<String, Map> resultList = new HashMap<>();
        Map<String, Object> resultMap = new HashMap<>();
        for (String assetKey : assetKeyList) {
            assetKey = assetKey.trim();
            String[] assetInfo = assetKey.split("-");
            int assetChainId = Integer.parseInt(assetInfo[0].trim());
            int assetId = Integer.parseInt(assetInfo[1].trim());

            AccountState accountState = accountStateService.getAccountStateReCal(address, chainId, assetChainId, assetId);
            Map<String, Object> rtMap = new HashMap<>(5);
            rtMap.put("freeze", accountState.getFreezeTotal());
            rtMap.put("total", accountState.getTotalAmount());
            rtMap.put("available", accountState.getAvailableAmount());
            BigInteger permanentLocked = BigInteger.ZERO;
            BigInteger timeHeightLocked = BigInteger.ZERO;
            for (FreezeLockTimeState freezeLockTimeState : accountState.getFreezeLockTimeStates()) {
                if (LedgerUtil.isPermanentLock(freezeLockTimeState.getLockTime())) {
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

            resultList.put(assetKey, rtMap);
        }
        resultMap.put("list", resultList);
        Response response = success(resultMap);
        return response;
    }


    /**
     * Obtain account lock list
     * get user account freeze
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_FREEZE_LIST, version = 1.0,
            description = "Paging to obtain account locked asset list")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Run ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Asset ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "assetId,Value range[1-65535]"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset location address"),
            @Parameter(parameterName = "pageNumber", requestType = @TypeDescriptor(value = int.class), parameterDes = "Starting page count"),
            @Parameter(parameterName = "pageSize", requestType = @TypeDescriptor(value = int.class), parameterDes = "Display quantity per page")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "totalCount", valueType = Integer.class, description = "Total number of records"),
                    @Key(name = "pageNumber", valueType = Integer.class, description = "Starting page count"),
                    @Key(name = "pageSize", valueType = Integer.class, description = "Display quantity per page"),
                    @Key(name = "list", valueType = List.class, valueElement = FreezeLockState.class, description = "Lock Amount List")
            })
    )
    public Response getFreezeList(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = (Integer) params.get("assetChainId");
        String address = LedgerUtil.getRealAddressStr((String) params.get("address"));
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
     * Obtain accountnoncevalue
     * get user account nonce
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = CmdConstant.CMD_GET_NONCE, version = 1.0,
            description = "Obtain account assetsNONCEvalue")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Asset ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "assetId,Value range[1-65535]"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset location address"),
            @Parameter(parameterName = "isConfirmed", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "Optional items,defaultfalse. filltrue,Then it must be obtained from the confirmed transaction")

    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "nonce", valueType = String.class, description = "Account assetsnoncevalue"),
                    @Key(name = "nonceType", valueType = Integer.class, description = "1：Confirmednoncevalue,0：unacknowledgednoncevalue")

            })
    )
    public Response getNonce(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = (Integer) params.get("assetChainId");
        String address = LedgerUtil.getRealAddressStr((String) params.get("address"));
        Integer assetId = (Integer) params.get("assetId");
        boolean isConfirmed = false;
        if (null != params.get("isConfirmed")) {
            isConfirmed = Boolean.valueOf(params.get("isConfirmed").toString());
        }
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        Map<String, Object> rtMap = new HashMap<>(2);
        AccountState accountState = accountStateService.getAccountState(address, chainId, assetChainId, assetId);
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedInfo(address, chainId, assetChainId, assetId, accountState);
        if (isConfirmed || null == accountStateUnconfirmed) {
            rtMap.put("nonce", RPCUtil.encode(accountState.getNonce()));
            rtMap.put("nonceType", LedgerConstant.CONFIRMED_NONCE);
        } else {
            rtMap.put("nonce", RPCUtil.encode(accountStateUnconfirmed.getNonce()));
            rtMap.put("nonceType", LedgerConstant.UNCONFIRMED_NONCE);
        }
        return success(rtMap);
    }

    @CmdAnnotation(cmd = CmdConstant.CMD_GET_BALANCE_NONCE, version = 1.0,
            description = "Obtain account asset balance andNONCEvalue")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Running ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "Asset ChainId,Value range[1-65535]"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterValidRange = "[1-65535]", parameterDes = "assetId,Value range[1-65535]"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "Asset location address"),
            @Parameter(parameterName = "isConfirmed", requestType = @TypeDescriptor(value = boolean.class), parameterDes = "Optional items,defaultfalse. filltrue,Then it must be obtained from the confirmed transaction")
    })
    @ResponseData(name = "Return value", description = "Return aMapobject",
            responseType = @TypeDescriptor(value = Map.class, mapKeys = {
                    @Key(name = "nonce", valueType = String.class, description = "Account assetsnoncevalue"),
                    @Key(name = "nonceType", valueType = Integer.class, description = "1：Confirmednoncevalue,0：unacknowledgednoncevalue"),
                    @Key(name = "available", valueType = BigInteger.class, description = "Available amount"),
                    @Key(name = "freeze", valueType = BigInteger.class, description = "Total locked amount"),
                    @Key(name = "permanentLocked", valueType = BigInteger.class, description = "Permanently locked amount"),
                    @Key(name = "timeHeightLocked", valueType = BigInteger.class, description = "Height or Time Locked Amount")
            })
    )
    public Response getBalanceNonce(Map params) {
        Integer chainId = (Integer) params.get("chainId");
        Integer assetChainId = (Integer) params.get("assetChainId");
        String address = LedgerUtil.getRealAddressStr((String) params.get("address"));
        Integer assetId = (Integer) params.get("assetId");
        boolean isConfirmed = false;
        if (null != params.get("isConfirmed")) {
            isConfirmed = Boolean.valueOf(params.get("isConfirmed").toString());
        }
        if (!chainHanlder(chainId)) {
            return failed(LedgerErrorCode.CHAIN_INIT_FAIL);
        }
        AccountState accountState = accountStateService.getAccountStateReCal(address, chainId, assetChainId, assetId);
        Map<String, Object> rtMap = new HashMap<>(6);
        AccountStateUnconfirmed accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedInfo(address, chainId, assetChainId, assetId, accountState);
        if (isConfirmed || null == accountStateUnconfirmed) {
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
            if (LedgerUtil.isPermanentLock(freezeLockTimeState.getLockTime())) {
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
