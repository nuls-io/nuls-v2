package io.nuls.api.rpc.controller;

import io.nuls.api.db.ContractService;
import io.nuls.api.db.TokenService;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ContractController {

    @Autowired
    private ContractService contractService;
    @Autowired
    private TokenService tokenService;

    @RpcMethod("getContract")
    public RpcResult getContract(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId = (int) params.get(0);
        String contractAddress = (String) params.get(1);
        if (!AddressTool.validAddress(chainId, contractAddress)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
        }
        RpcResult rpcResult = new RpcResult();
        try {
            ContractInfo contractInfo = contractService.getContractInfo(chainId, contractAddress);
            if (contractInfo == null) {
                rpcResult.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
            } else {
                rpcResult.setResult(contractInfo);
            }
        } catch (Exception e) {
            Log.error(e);
            rpcResult.setError(new RpcResultError(RpcErrorCode.SYS_UNKNOWN_EXCEPTION));
        }
        return rpcResult;
    }

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
        PageInfo<AccountTokenInfo> pageInfo = tokenService.getAccountTokens(chainId, address, pageIndex, pageSize);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("getContractTokens")
    public RpcResult getContractTokens(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId = (int) params.get(0);
        int pageIndex = (int) params.get(1);
        int pageSize = (int) params.get(2);
        String contractAddress = (String) params.get(3);
        if (!AddressTool.validAddress(chainId, contractAddress)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<AccountTokenInfo> pageInfo = tokenService.getContractTokens(chainId, contractAddress, pageIndex, pageSize);
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
        PageInfo<TokenTransfer> pageInfo = tokenService.getTokenTransfers(chainId, address, contractAddress, pageIndex, pageSize);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("getContractTxList")
    public RpcResult getContractTxList(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId = (int) params.get(0);
        int pageIndex = (int) params.get(1);
        int pageSize = (int) params.get(2);
        int type = (int) params.get(3);
        String contractAddress = (String) params.get(4);

        if (!AddressTool.validAddress(chainId, contractAddress)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[contractAddress] is inValid"));
        }
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<ContractTxInfo> pageInfo = contractService.getContractTxList(chainId, contractAddress, type, pageIndex, pageSize);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("getContractList")
    public RpcResult getContractList(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId = (int) params.get(0);
        int pageIndex = (int) params.get(1);
        int pageSize = (int) params.get(2);
        boolean onlyNrc20 = (boolean) params.get(3);
        boolean isHidden = (boolean) params.get(4);
        if (pageIndex <= 0) {
            pageIndex = 1;
        }
        if (pageSize <= 0 || pageSize > 100) {
            pageSize = 10;
        }
        PageInfo<ContractInfo> pageInfo = contractService.getContractList(chainId, pageIndex, pageSize, onlyNrc20, isHidden);
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

}
