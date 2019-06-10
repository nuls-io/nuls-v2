/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.api.rpc.controller;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.AccountLedgerService;
import io.nuls.api.db.AccountService;
import io.nuls.api.db.BlockService;
import io.nuls.api.db.ChainService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.po.db.mini.MiniAccountInfo;
import io.nuls.api.model.rpc.*;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.model.StringUtils;

import java.util.List;

/**
 * @author Niels
 */
@Controller
public class AccountController {

    @Autowired
    private AccountService accountService;
    @Autowired
    private BlockService blockHeaderService;
    @Autowired
    private ChainService chainService;
    @Autowired
    private AccountLedgerService accountLedgerService;

    @RpcMethod("getAccountList")
    public RpcResult getAccountList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, pageIndex, pageSize;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }
        try {
            RpcResult result = new RpcResult();
            PageInfo<AccountInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                pageInfo = accountService.pageQuery(chainId, pageIndex, pageSize);
            } else {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
            }
            result.setResult(pageInfo);
            return result;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAccountTxs")
    public RpcResult getAccountTxs(List<Object> params) {
        VerifyUtils.verifyParams(params, 6);
        int chainId, pageIndex, pageSize, type;
        String address;
        boolean isMark;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            address = (String) params.get(3);
            type = (int) params.get(4);
            isMark = (boolean) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            RpcResult result = new RpcResult();
            PageInfo<TxRelationInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                pageInfo = accountService.getAccountTxs(chainId, address, pageIndex, pageSize, type, isMark);
            } else {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
            }
            result.setResult(pageInfo);
            return result;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAcctTxs")
    public RpcResult getAcctTxs(List<Object> params) {
        VerifyUtils.verifyParams(params, 6);
        int chainId, pageIndex, pageSize, type;
        String address;
        boolean isMark;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            address = (String) params.get(3);
            type = (int) params.get(4);
            isMark = (boolean) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            RpcResult result = new RpcResult();
            PageInfo<TxRelationInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                pageInfo = accountService.getAcctTxs(chainId, address, pageIndex, pageSize, type, isMark);
            } else {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
            }
            result.setResult(pageInfo);
            return result;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAccount")
    public RpcResult getAccount(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        try {
            RpcResult result = new RpcResult();
            ApiCache apiCache = CacheManager.getCache(chainId);
            if (apiCache == null) {
                return result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
            }
            AccountInfo accountInfo = accountService.getAccountInfo(chainId, address);
            if (accountInfo == null) {
                accountInfo = new AccountInfo(address);
            } else {
                AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, defaultAsset.getChainId(), defaultAsset.getAssetId());
                accountInfo.setBalance(balanceInfo.getBalance());
                accountInfo.setConsensusLock(balanceInfo.getConsensusLock());
                accountInfo.setTimeLock(balanceInfo.getTimeLock());
            }
            return result.setResult(accountInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getCoinRanking")
    public RpcResult getCoinRanking(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageIndex, pageSize, sortType;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            sortType = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (sortType < 0 || sortType > 1) {
            return RpcResult.paramError("[sortType] is invalid");
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<MiniAccountInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                pageInfo = accountService.getCoinRanking(pageIndex, pageSize, sortType, chainId);
            } else {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
            }
            return new RpcResult().setResult(pageInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAccountFreezes")
    public RpcResult getAccountFreezes(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageIndex, pageSize, assetId;
        String address;
        try {
            chainId = (int) params.get(0);
            pageIndex = (int) params.get(1);
            pageSize = (int) params.get(2);
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<FreezeInfo> pageInfo;
            if (CacheManager.isChainExist(chainId)) {
                ApiCache apiCache = CacheManager.getCache(chainId);
                assetId = apiCache.getChainInfo().getDefaultAsset().getAssetId();
                Result<PageInfo<FreezeInfo>> result = WalletRpcHandler.getFreezeList(chainId, pageIndex, pageSize, address, assetId);
                if (result.isFailed()) {
                    return RpcResult.failed(result);
                }
                pageInfo = result.getData();
                return RpcResult.success(pageInfo);
            } else {
                pageInfo = new PageInfo<>(pageIndex, pageSize);
                return RpcResult.success(pageInfo);
            }
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getAccountBalance")
    public RpcResult getAccountBalance(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, assetChainId, assetId;
        String address;
        try {
            chainId = (int) params.get(0);
            assetChainId = (int) params.get(1);
            assetId = (int) params.get(2);
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
            ApiCache apiCache = CacheManager.getCache(chainId);
            if (apiCache == null) {
                return RpcResult.dataNotFound();
            }
            if (assetId <= 0) {
                AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
                assetId = defaultAsset.getAssetId();
            }
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, assetChainId, assetId);
            return RpcResult.success(balanceInfo);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("isAliasUsable")
    public RpcResult isAliasUsable(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String alias;
        try {
            chainId = (int) params.get(0);
            alias = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (StringUtils.isBlank(alias)) {
            return RpcResult.paramError("[alias] is inValid");
        }

        try {
            ApiCache apiCache = CacheManager.getCache(chainId);
            if (apiCache == null) {
                return RpcResult.dataNotFound();
            }

            Result result = WalletRpcHandler.isAliasUsable(chainId, alias);
            return RpcResult.success(result.getData());
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }


    @RpcMethod("getAccountLedgerList")
    public RpcResult getAccountLedgerList(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            ApiCache apiCache = CacheManager.getCache(chainId);
            if (apiCache == null) {
                return RpcResult.dataNotFound();
            }
            List<AccountLedgerInfo> list = accountLedgerService.getAccountLedgerInfoList(chainId, address);
            for (AccountLedgerInfo ledgerInfo : list) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, ledgerInfo.getChainId(), ledgerInfo.getAssetId());
                ledgerInfo.setBalance(balanceInfo.getBalance());
                ledgerInfo.setTimeLock(balanceInfo.getTimeLock());
                ledgerInfo.setConsensusLock(balanceInfo.getConsensusLock());
                AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(ledgerInfo.getAssetKey());
                if (assetInfo != null) {
                    ledgerInfo.setSymbol(assetInfo.getSymbol());
                }
            }
            return RpcResult.success(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }


    @RpcMethod("getAccountCrossLedgerList")
    public RpcResult getAccountCrossLedgerList(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            ApiCache apiCache = CacheManager.getCache(chainId);
            if (apiCache == null) {
                return RpcResult.dataNotFound();
            }
            List<AccountLedgerInfo> list = accountLedgerService.getAccountCrossLedgerInfoList(chainId, address);
            for (AccountLedgerInfo ledgerInfo : list) {
                BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, ledgerInfo.getChainId(), ledgerInfo.getAssetId());
                ledgerInfo.setBalance(balanceInfo.getBalance());
                ledgerInfo.setTimeLock(balanceInfo.getTimeLock());
                ledgerInfo.setConsensusLock(balanceInfo.getConsensusLock());
                AssetInfo assetInfo = CacheManager.getAssetInfoMap().get(ledgerInfo.getAssetKey());
                if (assetInfo != null) {
                    ledgerInfo.setSymbol(assetInfo.getSymbol());
                }
            }
            return RpcResult.success(list);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }
}
