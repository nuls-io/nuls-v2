package io.nuls.api.rpc.controller;

import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.model.po.db.AccountTokenInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.TokenTransfer;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;
import io.nuls.tools.model.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ContractController {

    @RpcMethod("getAccountTokens")
    public RpcResult getAccountTokens(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId = (int) params.get(0);
        int pageIndex = (int) params.get(1);
        int pageSize = (int) params.get(2);
        String address = (String) params.get(3);
        if (!AddressTool.validAddress(chainId, address)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] is inValid"));
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
//        PageInfo<AccountTokenInfo> pageInfo = tokenService.getAccountTokens(address, pageIndex, pageSize);
        PageInfo<AccountTokenInfo> pageInfo = new PageInfo<>(pageIndex, pageSize, 1, new ArrayList<>());
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("getTokenTransfers")
    public RpcResult getTokenTransfers(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId = (int) params.get(0);
        int pageIndex = (int) params.get(1);
        int pageSize = (int) params.get(2);
        String address = (String) params.get(3);
        String contractAddress = (String) params.get(4);

        if (StringUtils.isBlank(address) && StringUtils.isBlank(contractAddress)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] or [contractAddress] is inValid"));
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        //PageInfo<TokenTransfer> pageInfo = tokenService.getTokenTransfers(address, contractAddress, pageIndex, pageSize);
        PageInfo<TokenTransfer> pageInfo = new PageInfo<>(1, 10, 1, new ArrayList<>());
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }
}
