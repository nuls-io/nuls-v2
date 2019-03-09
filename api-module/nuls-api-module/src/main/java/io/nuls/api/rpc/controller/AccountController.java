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

import io.nuls.api.db.AccountService;
import io.nuls.api.db.BlockService;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.model.po.db.AccountInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.TxRelationInfo;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
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
    private AccountService accountService;
    //    @Autowired
//    private UTXOService utxoService;
    @Autowired
    private BlockService blockHeaderService;

    @RpcMethod("getAccountList")
    public RpcResult getAccountList(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int pageIndex = (int) params.get(0);
        int pageSize = (int) params.get(1);
        int chainId = (int) params.get(2);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }

        PageInfo<AccountInfo> pageInfo = accountService.pageQuery(chainId, pageIndex, pageSize);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }


    @RpcMethod("getAccountTxs")
    public RpcResult getAccountTxs(List<Object> params) {
        VerifyUtils.verifyParams(params, 6);

        int pageIndex = (int) params.get(0);
        int pageSize = (int) params.get(1);
        String address = (String) params.get(2);
        int type = (int) params.get(3);
        boolean isMark = (boolean) params.get(4);
        int chainId = (int) params.get(5);

        if (!AddressTool.validAddress(chainId, address)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] is inValid"));
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<TxRelationInfo> relationInfos = accountService.getAccountTxs(chainId, address, pageIndex, pageSize, type, isMark);
        RpcResult result = new RpcResult();
        result.setResult(relationInfos);
        return result;
    }
//
//    @RpcMethod("getAccount")
//    public RpcResult getAccount(List<Object> params) {
//        VerifyUtils.verifyParams(params, 1);
//        String address = (String) params.get(0);
//        if (!AddressTool.validAddress(address)) {
//            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] is inValid"));
//        }
//
//        AccountInfo accountInfo = accountService.getAccountInfo(address);
//        RpcResult result = new RpcResult();
//        if (accountInfo == null) {
//            return result.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
//        }
//
//        List<Output> outputs = utxoService.getAccountUtxos(address);
//        CalcUtil.calcBalance(accountInfo, outputs, blockHeaderService.getBestBlockHeight());
//
//        return result.setResult(accountInfo);
//    }
}
