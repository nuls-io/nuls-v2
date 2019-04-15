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
import io.nuls.api.db.ChainService;
import io.nuls.api.db.mongo.MongoAccountServiceImpl;
import io.nuls.api.db.mongo.MongoBlockServiceImpl;
import io.nuls.api.db.mongo.MongoChainServiceImpl;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.rpc.*;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;

import java.util.List;

/**
 * @author Niels
 */
@Controller
public class AccountController {

    @Autowired
    private MongoAccountServiceImpl mongoAccountServiceImpl;
    @Autowired
    private MongoBlockServiceImpl blockHeaderService;
    @Autowired
    private MongoChainServiceImpl chainService;

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
        RpcResult result = new RpcResult();
        PageInfo<AccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = mongoAccountServiceImpl.pageQuery(chainId, pageIndex, pageSize);
        } else {
            pageInfo = new PageInfo<>(pageIndex, pageSize);
        }
        result.setResult(pageInfo);
        return result;
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
        RpcResult result = new RpcResult();
        PageInfo<TxRelationInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = mongoAccountServiceImpl.getAccountTxs(chainId, address, pageIndex, pageSize, type, isMark);
        } else {
            pageInfo = new PageInfo<>(pageIndex, pageSize);
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
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        RpcResult result = new RpcResult();
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
        }

        AccountInfo accountInfo = mongoAccountServiceImpl.getAccountInfo(chainId, address);
        if (accountInfo == null) {
            return result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
        }
        AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, defaultAsset.getChainId(), defaultAsset.getAssetId());
        accountInfo.setBalance(balanceInfo.getBalance());
        accountInfo.setConsensusLock(balanceInfo.getConsensusLock());
        accountInfo.setTimeLock(balanceInfo.getTimeLock());
        return result.setResult(accountInfo);
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

        PageInfo<AccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = mongoAccountServiceImpl.getCoinRanking(pageIndex, pageSize, sortType, chainId);
        } else {
            pageInfo = new PageInfo<>(pageIndex, pageSize);
        }
        return new RpcResult().setResult(pageInfo);
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
    }

    @RpcMethod("getAccountBalance")
    public RpcResult getAccountBalance(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, assetId;
        String address;
        try {
            chainId = (int) params.get(0);
            assetId = (int) params.get(1);
            address = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.dataNotFound();
        }
        if (assetId <= 0) {
            AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
            assetId = defaultAsset.getAssetId();
        }
        BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, address, chainId, assetId);
        return RpcResult.success(balanceInfo);
    }

}
