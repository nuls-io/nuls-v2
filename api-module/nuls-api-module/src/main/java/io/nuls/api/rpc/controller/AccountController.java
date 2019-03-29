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
import io.nuls.api.db.AccountService;
import io.nuls.api.db.BlockService;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.AccountInfo;
import io.nuls.api.model.po.db.AssetInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.TxRelationInfo;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.provider.ServiceManager;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;
import io.nuls.tools.model.FormatValidUtils;

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

    private io.nuls.api.provider.account.AccountService cmdAccountService = ServiceManager.get(io.nuls.api.provider.account.AccountService.class);

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
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        RpcResult result = new RpcResult();
        PageInfo<AccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.pageQuery(chainId, pageIndex, pageSize);
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
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        RpcResult result = new RpcResult();
        PageInfo<TxRelationInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.getAccountTxs(chainId, address, pageIndex, pageSize, type, isMark);
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

        AccountInfo accountInfo = accountService.getAccountInfo(chainId, address);
        if (accountInfo == null) {
            return result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
        }
        AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
        AccountInfo account = WalletRpcHandler.getAccountBalance(chainId, address, defaultAsset.getChainId(), defaultAsset.getAssetId());
        accountInfo.setBalance(account.getBalance());
        accountInfo.setConsensusLock(account.getConsensusLock());
        accountInfo.setTimeLock(account.getTimeLock());
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

        PageInfo<AccountInfo> pageInfo;
        if (CacheManager.isChainExist(chainId)) {
            pageInfo = accountService.getCoinRanking(pageIndex, pageSize, sortType, chainId);
        } else {
            pageInfo = new PageInfo<>(pageIndex, pageSize);
        }
        return new RpcResult().setResult(pageInfo);
    }

    @RpcMethod("createAccount")
    public RpcResult createAccount(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, count;
        String password;
        try {
            chainId = (int) params.get(0);
            count = (int) params.get(1);
            password = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if(!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }


        return null;
    }

//    @RpcMethod("getAccountTokens")
//    public RpcResult getAccountTokens(List<Object> params) {
//        VerifyUtils.verifyParams(params, 4);
//        int chainId = (int) params.get(0);
//        int pageIndex = (int) params.get(1);
//        int pageSize = (int) params.get(2);
//        String address = (String) params.get(3);
//
//        if (!AddressTool.validAddress(chainId, address)) {
//            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] is inValid"));
//        }
//        if (pageIndex <= 0) {
//            pageIndex = 1;
//        }
//        if (pageSize <= 0 || pageSize > 100) {
//            pageSize = 10;
//        }
//        // todo
//        //PageInfo<AccountTokenInfo> pageInfo = tokenService.getAccountTokens(address, pageIndex, pageSize);
//        PageInfo<AccountTokenInfo> pageInfo = new PageInfo<>();
//        RpcResult result = new RpcResult();
//        result.setResult(pageInfo);
//        return result;
//    }
}
