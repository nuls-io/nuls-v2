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

import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.*;
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
import io.nuls.core.exception.NulsException;
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
    @Autowired
    private AliasService aliasService;

    @RpcMethod("getAccountList")
    public RpcResult getAccountList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, pageNumber, pageSize;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }
        RpcResult result = new RpcResult();
        PageInfo<AccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.pageQuery(chainId, pageNumber, pageSize);
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        }
        result.setResult(pageInfo);
        return result;

    }

    @RpcMethod("getAccountTxs")
    public RpcResult getAccountTxs(List<Object> params) {
        VerifyUtils.verifyParams(params, 6);
        int chainId, pageNumber, pageSize, type;
        String address;
        boolean isMark;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            type = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }
        try {
            isMark = (boolean) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError("[isMark] is inValid");
        }

        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        RpcResult result = new RpcResult();
        PageInfo<TxRelationInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.getAccountTxs(chainId, address, pageNumber, pageSize, type, isMark);
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        }
        result.setResult(pageInfo);
        return result;

    }

    @RpcMethod("getAcctTxs")
    public RpcResult getAcctTxs(List<Object> params) {
        VerifyUtils.verifyParams(params, 6);
        int chainId, pageNumber, pageSize, type;
        String address;
        boolean isMark;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            type = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is inValid");
        }
        try {
            isMark = (boolean) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError("[isMark] is inValid");
        }

        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        RpcResult result = new RpcResult();
        PageInfo<TxRelationInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.getAcctTxs(chainId, address, pageNumber, pageSize, type, isMark);
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        }
        result.setResult(pageInfo);
        return result;

    }

    @RpcMethod("getAccount")
    public RpcResult getAccount(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        RpcResult result = new RpcResult();
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
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
        accountInfo.setSymbol(ApiContext.defaultSymbol);
        return result.setResult(accountInfo);
    }

    @RpcMethod("getAccountByAlias")
    public RpcResult getAccountByAlias(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String alias;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            alias = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[alias] is inValid");
        }
        RpcResult result = new RpcResult();
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }
        AliasInfo aliasInfo = aliasService.getByAlias(chainId, alias);
        if (aliasInfo == null) {
            return RpcResult.dataNotFound();
        }
        AccountInfo accountInfo = accountService.getAccountInfo(chainId, aliasInfo.getAddress());
        if (accountInfo == null) {
            return RpcResult.dataNotFound();
        } else {
            AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, aliasInfo.getAddress(), defaultAsset.getChainId(), defaultAsset.getAssetId());
            accountInfo.setBalance(balanceInfo.getBalance());
            accountInfo.setConsensusLock(balanceInfo.getConsensusLock());
            accountInfo.setTimeLock(balanceInfo.getTimeLock());
        }
        accountInfo.setSymbol(ApiContext.defaultSymbol);
        return result.setResult(accountInfo);

    }

    @RpcMethod("getCoinRanking")
    public RpcResult getCoinRanking(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageNumber, pageSize, sortType;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is inValid");
        }
        try {
            sortType = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[sortType] is inValid");
        }
        if (sortType < 0 || sortType > 1) {
            return RpcResult.paramError("[sortType] is invalid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        PageInfo<MiniAccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.getCoinRanking(pageNumber, pageSize, sortType, chainId);
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        }
        return new RpcResult().setResult(pageInfo);
    }

    @RpcMethod("getAccountFreezes")
    public RpcResult getAccountFreezes(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageNumber, pageSize, assetId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is inValid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[sortType] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        PageInfo<FreezeInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            ApiCache apiCache = CacheManager.getCache(chainId);
            assetId = apiCache.getChainInfo().getDefaultAsset().getAssetId();
            Result<PageInfo<FreezeInfo>> result = WalletRpcHandler.getFreezeList(chainId, pageNumber, pageSize, address, assetId);
            if (result.isFailed()) {
                return RpcResult.failed(result);
            }
            pageInfo = result.getData();
            return RpcResult.success(pageInfo);
        } else {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
            return RpcResult.success(pageInfo);
        }
    }

    @RpcMethod("getAccountBalance")
    public RpcResult getAccountBalance(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, assetChainId, assetId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetChainId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

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

    }

    @RpcMethod("isAliasUsable")
    public RpcResult isAliasUsable(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String alias;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            alias = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[alias] is inValid");
        }
        if (StringUtils.isBlank(alias)) {
            return RpcResult.paramError("[alias] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }

        Result result = WalletRpcHandler.isAliasUsable(chainId, alias);
        return RpcResult.success(result.getData());
    }

    @RpcMethod("getAccountLedgerList")
    public RpcResult getAccountLedgerList(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

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
    }


    @RpcMethod("getAccountCrossLedgerList")
    public RpcResult getAccountCrossLedgerList(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

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

    }
}
