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

package io.nuls.provider.api.jsonrpc.controller;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.block.BlockService;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.base.api.provider.crosschain.CrossChainProvider;
import io.nuls.base.api.provider.crosschain.facade.RehandleCtxReq;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.Parameters;
import io.nuls.core.rpc.model.ResponseData;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.provider.model.jsonrpc.RpcErrorCode;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.utils.VerifyUtils;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;

import java.util.List;

/**
 * @author Niels
 */
@Controller
@Api(type = ApiType.JSONRPC)
public class CrossChainController {

    private BlockService blockService = ServiceManager.get(BlockService.class);

    private CrossChainProvider crossChainProvider = ServiceManager.get(CrossChainProvider.class);
    public CrossChainController(){
        System.out.println();
    }

    @RpcMethod("rehandlectx")
    @ApiOperation(description = "Resend transactions that have already been held by the cardholder", order = 901, detailDesc = "Resending only represents the attempt of this node and cannot guarantee the final result")
    @Parameters(value = {
            @Parameter(parameterName = "hash", requestType = @TypeDescriptor(value = String.class), parameterDes = "Cross chain transactionshash")
    })
    @ResponseData(name = "Return value", description = "Whether successful", responseType = @TypeDescriptor(value = Boolean.class))
    public RpcResult createAccount(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        String hash;
        try {
            hash = (String) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[hash] is inValid");
        }

        long blockHeight = blockService.getBlockHeaderByLastHeight(new GetBlockHeaderByLastHeightReq()).getData().getHeight();
        Result<String> result = crossChainProvider.rehandleCtx(new RehandleCtxReq(hash, blockHeight));
        if (result.isFailed()) {
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
        return RpcResult.success(true);
    }


}
