package io.nuls.api.rpc.controller;

import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.db.ContractService;
import io.nuls.api.db.TokenService;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.po.db.mini.MiniContractInfo;
import io.nuls.api.model.rpc.BalanceInfo;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class ContractController {

    @Autowired
    private ContractService contractService;
    @Autowired
    private TokenService tokenService;

    @RpcMethod("getContract")
    public RpcResult getContract(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String contractAddress;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            contractAddress = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }


        if (!AddressTool.validAddress(chainId, contractAddress)) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }

        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        ContractInfo contractInfo = contractService.getContractInfo(chainId, contractAddress);
        if (contractInfo == null) {
            rpcResult.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
        } else {
            ApiCache apiCache = CacheManager.getCache(chainId);
            AssetInfo defaultAsset = apiCache.getChainInfo().getDefaultAsset();
            BalanceInfo balanceInfo = WalletRpcHandler.getAccountBalance(chainId, contractAddress, defaultAsset.getChainId(), defaultAsset.getAssetId());
            contractInfo.setBalance(balanceInfo.getTotalBalance());
            rpcResult.setResult(contractInfo);
        }
        return rpcResult;
    }

    @RpcMethod("getAccountTokens")
    public RpcResult getAccountTokens(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageNumber, pageSize;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageIndex] is invalid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is invalid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is invalid");
        }

        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is invalid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        PageInfo<AccountTokenInfo> pageInfo;
        if (!CacheManager.isChainExist(chainId)) {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        } else {
            pageInfo = tokenService.getAccountTokens(chainId, address, pageNumber, pageSize);
        }

        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("getContractTokens")
    public RpcResult getContractTokens(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, pageNumber, pageSize;
        String contractAddress;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is invalid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is invalid");
        }
        try {
            contractAddress = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }
        if (!AddressTool.validAddress(chainId, contractAddress)) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<AccountTokenInfo> pageInfo;
            if (!CacheManager.isChainExist(chainId)) {
                pageInfo = new PageInfo<>(pageNumber, pageSize);
            } else {
                pageInfo = tokenService.getContractTokens(chainId, contractAddress, pageNumber, pageSize);
            }
            RpcResult result = new RpcResult();
            result.setResult(pageInfo);
            return result;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getTokenTransfers")
    public RpcResult getTokenTransfers(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, pageNumber, pageSize;
        String address, contractAddress;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is invalid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is invalid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is invalid");
        }
        try {
            contractAddress = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }

        if (!StringUtils.isBlank(address) && !AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is invalid");
        }
        if (!StringUtils.isBlank(contractAddress) && !AddressTool.validAddress(chainId, contractAddress)) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<TokenTransfer> pageInfo;
            if (!CacheManager.isChainExist(chainId)) {
                pageInfo = new PageInfo<>(pageNumber, pageSize);
            } else {
                pageInfo = tokenService.getTokenTransfers(chainId, address, contractAddress, pageNumber, pageSize);
            }
            RpcResult result = new RpcResult();
            result.setResult(pageInfo);
            return result;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getContractTxList")
    public RpcResult getContractTxList(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, pageNumber, pageSize, type;
        String contractAddress;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is invalid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is invalid");
        }
        try {
            type = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[type] is invalid");
        }
        try {
            contractAddress = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }

        if (!AddressTool.validAddress(chainId, contractAddress)) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }
        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        try {
            PageInfo<ContractTxInfo> pageInfo;
            if (!CacheManager.isChainExist(chainId)) {
                pageInfo = new PageInfo<>(pageNumber, pageSize);
            } else {
                pageInfo = contractService.getContractTxList(chainId, contractAddress, type, pageNumber, pageSize);
            }
            RpcResult result = new RpcResult();
            result.setResult(pageInfo);
            return result;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("getContractList")
    public RpcResult getContractList(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, pageNumber, pageSize;
        boolean onlyNrc20, isHidden;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is invalid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is invalid");
        }
        try {
            onlyNrc20 = (boolean) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[onlyNrc20] is invalid");
        }
        try {
            isHidden = (boolean) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[isHidden] is invalid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }

        PageInfo<MiniContractInfo> pageInfo;
        if (!CacheManager.isChainExist(chainId)) {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        } else {
            pageInfo = contractService.getContractList(chainId, pageNumber, pageSize, onlyNrc20, isHidden);
        }
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    @RpcMethod("getAccountContractList")
    public RpcResult getAccountContractList(List<Object> params) {
        VerifyUtils.verifyParams(params, 6);
        int chainId, pageNumber, pageSize;
        boolean onlyNrc20, isHidden;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is invalid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is invalid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is invalid");
        }
        try {
            onlyNrc20 = (boolean) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[onlyNrc20] is invalid");
        }
        try {
            isHidden = (boolean) params.get(5);
        } catch (Exception e) {
            return RpcResult.paramError("[isHidden] is invalid");
        }

        if (pageNumber <= 0) {
            pageNumber = 1;
        }
        if (pageSize <= 0 || pageSize > 1000) {
            pageSize = 10;
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is invalid");
        }

        PageInfo<MiniContractInfo> pageInfo;
        if (!CacheManager.isChainExist(chainId)) {
            pageInfo = new PageInfo<>(pageNumber, pageSize);
        } else {
            pageInfo = contractService.getContractList(chainId, pageNumber, pageSize, address, onlyNrc20, isHidden);
        }
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }


    @RpcMethod("getContractListById")
    public RpcResult getContractListById(List<Object> params) {
        VerifyUtils.verifyParams(params, 5);
        int chainId, pageNumber, pageSize, totalCount;
        List<String> contractAddressList;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            pageNumber = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[pageNumber] is invalid");
        }
        try {
            pageSize = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[pageSize] is invalid");
        }
        try {
            totalCount = (int) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[totalCount] is invalid");
        }
        try {
            contractAddressList = (List<String>) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[contractAddressArrays] is invalid");
        }

        PageInfo<MiniContractInfo> pageInfo = new PageInfo<>(pageNumber, pageSize);
        if (CacheManager.isChainExist(chainId)) {
            pageInfo.setTotalCount(totalCount);
            pageInfo.setList(contractService.getContractList(chainId, contractAddressList));
        }
        RpcResult result = new RpcResult();
        result.setResult(pageInfo);
        return result;
    }

    /**
     * 上传合约代码jar包
     */
    @RpcMethod("uploadContractJar")
    public RpcResult upload(List<Object> params) throws NulsException {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String jarFileData;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            jarFileData = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[jarFileData] is invalid");
        }

        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<Map> mapResult = WalletRpcHandler.uploadContractJar(chainId, jarFileData);
        rpcResult.setResult(mapResult.getData());
        return rpcResult;
    }

    /**
     * 获取合约代码构造函数
     */
    @RpcMethod("getContractConstructor")
    public RpcResult getContractConstructor(List<Object> params) throws NulsException {
        VerifyUtils.verifyParams(params, 2);
        int chainId;
        String contractCode;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            contractCode = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[contractCode] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<Map> mapResult = WalletRpcHandler.getContractConstructor(chainId, contractCode);
        Map resultData = mapResult.getData();
        if (resultData == null) {
            rpcResult.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
        } else {
            rpcResult.setResult(resultData);
        }
        return rpcResult;
    }

    /**
     * 获取合约方法参数类型
     */
    @RpcMethod("getContractMethodArgsTypes")
    public RpcResult getContractMethodArgsTypes(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId;
        String contractAddress;
        String methodName;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            contractAddress = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }
        try {
            methodName = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[methodName] is invalid");
        }
        if (!AddressTool.validAddress(chainId, contractAddress)) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }

        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        ContractInfo contractInfo = contractService.getContractInfo(chainId, contractAddress);
        if (contractInfo == null) {
            rpcResult.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
        } else {
            List<ContractMethod> methods = contractInfo.getMethods();
            List<String> argsTypes = null;
            for (ContractMethod method : methods) {
                if (method.getName().equals(methodName)) {
                    List<ContractMethodArg> args = method.getParams();
                    argsTypes = new ArrayList<>();
                    for (ContractMethodArg arg : args) {
                        argsTypes.add(arg.getType());
                    }
                    break;
                }
            }
            if (argsTypes == null) {
                return RpcResult.dataNotFound();
            }
            rpcResult.setResult(argsTypes);
        }
        return rpcResult;
    }

    /**
     * 验证创建合约
     *
     * @param params
     * @return
     */
    @RpcMethod("validateContractCreate")
    public RpcResult validateContractCreate(List<Object> params) throws NulsException {
        VerifyUtils.verifyParams(params, 6);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<Map> mapResult = WalletRpcHandler.validateContractCreate(chainId,
                params.get(1),
                params.get(2),
                params.get(3),
                params.get(4),
                params.get(5)
        );
        rpcResult.setResult(mapResult.getData());
        return rpcResult;
    }

    /**
     * 验证调用合约
     *
     * @param params
     * @return
     */
    @RpcMethod("validateContractCall")
    public RpcResult validateContractCall(List<Object> params) throws NulsException {
        VerifyUtils.verifyParams(params, 9);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<Map> mapResult = WalletRpcHandler.validateContractCall(chainId,
                params.get(1),
                params.get(2),
                params.get(3),
                params.get(4),
                params.get(5),
                params.get(6),
                params.get(7),
                params.get(8)
        );
        rpcResult.setResult(mapResult.getData());
        return rpcResult;
    }

    /**
     * 验证删除合约
     *
     * @param params
     * @return
     */
    @RpcMethod("validateContractDelete")
    public RpcResult validateContractDelete(List<Object> params) throws NulsException {
        VerifyUtils.verifyParams(params, 3);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<Map> mapResult = WalletRpcHandler.validateContractDelete(chainId,
                params.get(1),
                params.get(2)
        );
        rpcResult.setResult(mapResult.getData());
        return rpcResult;
    }

    /**
     * 预估创建合约交易的gas
     *
     * @param params
     * @return
     */
    @RpcMethod("imputedContractCreateGas")
    public RpcResult imputedContractCreateGas(List<Object> params) throws NulsException {
        VerifyUtils.verifyParams(params, 4);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<Map> mapResult = WalletRpcHandler.imputedContractCreateGas(chainId,
                params.get(1),
                params.get(2),
                params.get(3)
        );
        rpcResult.setResult(mapResult.getData());
        return rpcResult;
    }

    /**
     * 预估调用合约交易的gas
     *
     * @param params
     * @return
     */
    @RpcMethod("imputedContractCallGas")
    public RpcResult imputedContractCallGas(List<Object> params) throws NulsException {
        VerifyUtils.verifyParams(params, 7);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<Map> mapResult = WalletRpcHandler.imputedContractCallGas(chainId,
                params.get(1),
                params.get(2),
                params.get(3),
                params.get(4),
                params.get(5),
                params.get(6)
        );
        rpcResult.setResult(mapResult.getData());
        return rpcResult;
    }

    /**
     * 调用合约不上链方法
     *
     * @param params
     * @return
     */
    @RpcMethod("invokeView")
    public RpcResult invokeView(List<Object> params) throws NulsException {
        VerifyUtils.verifyParams(params, 5);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<Map> mapResult = WalletRpcHandler.invokeView(chainId,
                params.get(1),
                params.get(2),
                params.get(3),
                params.get(4)
        );
        rpcResult.setResult(mapResult.getData());
        return rpcResult;
    }

    /**
     * 获取合约方法信息
     *
     * @param params
     * @return
     */
    @RpcMethod("getContractMethod")
    public RpcResult getContractMethod(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId = (int) params.get(0);
        String contractAddress = (String) params.get(1);
        String methodName = (String) params.get(2);
        String methodDesc = null;
        if (params.size() > 3) {
            methodDesc = (String) params.get(3);
        }

        if (!AddressTool.validAddress(chainId, contractAddress)) {
            return RpcResult.paramError("[contractAddress] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        if (StringUtils.isBlank(methodName)) {
            return RpcResult.paramError("[methodName] is invalid");
        }
        RpcResult rpcResult = new RpcResult();
        ContractInfo contractInfo = contractService.getContractInfo(chainId, contractAddress);
        if (contractInfo == null) {
            rpcResult.setError(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
        }
        List<ContractMethod> methods = contractInfo.getMethods();
        ContractMethod resultMethod = null;
        boolean isEmptyMethodDesc = StringUtils.isBlank(methodDesc);
        for (ContractMethod method : methods) {
            if (method.getName().equals(methodName)) {
                if (isEmptyMethodDesc) {
                    resultMethod = method;
                    break;
                } else if (methodDesc.equals(method.getDesc())) {
                    resultMethod = method;
                    break;
                }
            }
        }
        if (resultMethod == null) {
            return RpcResult.dataNotFound();
        }
        rpcResult.setResult(resultMethod);
        return rpcResult;
    }

}
