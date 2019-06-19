package io.nuls.api.rpc.controller;

import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.AddressType;
import io.nuls.api.db.AccountService;
import io.nuls.api.db.BlockService;
import io.nuls.api.db.ContractService;
import io.nuls.api.db.TransactionService;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.exception.NotFoundException;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.db.*;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.model.rpc.SearchResultDTO;
import io.nuls.api.utils.DBUtil;
import io.nuls.api.utils.LoggerUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ChainController {

    @Autowired
    private BlockService blockService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private ContractService contractService;

    @RpcMethod("getChainInfo")
    public RpcResult getChainInfo(List<Object> params) {
        return RpcResult.success(CacheManager.getCache(ApiContext.defaultChainId).getChainInfo());
    }

    @RpcMethod("getOtherChainList")
    public RpcResult getOtherChainList(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }

        List<Map<String, Object>> chainInfoList = new ArrayList<>();
        for (ChainInfo chainInfo : CacheManager.getChainInfoMap().values()) {
            if (chainInfo.getChainId() != chainId) {
                Map<String, Object> map = new HashMap<>();
                map.put("chainId", chainInfo.getChainId());
                map.put("chainName", chainInfo.getChainName());
                chainInfoList.add(map);
            }
        }
        return RpcResult.success(chainInfoList);

    }

    @RpcMethod("getInfo")
    public RpcResult getInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        Result<Map<String, Object>> result = WalletRpcHandler.getBlockGlobalInfo(chainId);
        if (result.isFailed()) {
            return RpcResult.failed(result);
        }
        Map<String, Object> map = result.getData();
        map.put("chainId", chainId);


        Map<String,Object> assetMap = new HashMap<>();
        assetMap.put("chainId", ApiContext.defaultChainId);
        assetMap.put("assetId", ApiContext.defaultChainId);
        assetMap.put("symbol", ApiContext.defaultSymbol);
        map.put("defaultAsset", assetMap);

//        AssetInfo assetInfo = CacheManager.getRegisteredAsset(DBUtil.getAssetKey(ApiContext.agentChainId, ApiContext.agentAssetId));
//        if(assetInfo != null) {
//            asssetMap.put("symbol", assetInfo.getSymbol());
//        }


        map.put("isRunCrossChain", ApiContext.isRunCrossChain);
        map.put("isRunSmartContract", ApiContext.isRunSmartContract);
        return RpcResult.success(map);
    }

    @RpcMethod("getCoinInfo")
    public RpcResult getCoinInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        ApiCache apiCache = CacheManager.getCache(chainId);
        return RpcResult.success(apiCache.getCoinContextInfo());
    }

    @RpcMethod("search")
    public RpcResult search(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);

        int chainId;
        String text;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }
        try {
            text = params.get(1).toString().trim();
        } catch (Exception e) {
            return RpcResult.paramError("[text] is invalid");
        }


        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }

        int length = text.length();
        SearchResultDTO result = null;
        if (length < 20) {
            result = getBlockByHeight(chainId, text);
        } else if (length < 40) {
            boolean isAddress = AddressTool.validAddress(chainId, text);
            if (isAddress) {
                byte[] address = AddressTool.getAddress(text);
                if (address[2] == AddressType.CONTRACT_ADDRESS_TYPE) {
                    result = getContractByAddress(chainId, text);
                } else {
                    result = getAccountByAddress(chainId, text);
                }
            }
        } else {
            result = getResultByHash(chainId, text);
        }
        if (null == result) {
            return RpcResult.dataNotFound();
        }
        return new RpcResult().setResult(result);

    }

    private SearchResultDTO getContractByAddress(int chainId, String text) {
        ContractInfo contractInfo;
        contractInfo = contractService.getContractInfo(chainId, text);
        SearchResultDTO dto = new SearchResultDTO();
        dto.setData(contractInfo);
        dto.setType("contract");
        return dto;
    }

    private SearchResultDTO getResultByHash(int chainId, String hash) {

        BlockHeaderInfo blockHeaderInfo = blockService.getBlockHeaderByHash(chainId, hash);
        if (blockHeaderInfo != null) {
            return getBlockInfo(chainId, blockHeaderInfo);
        }

        Result<TransactionInfo> result = WalletRpcHandler.getTx(chainId, hash);
        if (result == null) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.DATA_NOT_EXISTS));
        }
        if (result.isFailed()) {
            throw new JsonRpcException(result.getErrorCode());
        }
        TransactionInfo tx = result.getData();
        SearchResultDTO dto = new SearchResultDTO();
        dto.setData(tx);
        dto.setType("tx");
        return dto;
    }

    private SearchResultDTO getAccountByAddress(int chainId, String address) {
        if (!AddressTool.validAddress(chainId, address)) {
            throw new JsonRpcException(new RpcResultError(RpcErrorCode.PARAMS_ERROR, "[address] is inValid"));
        }

        AccountInfo accountInfo = accountService.getAccountInfo(chainId, address);
        if (accountInfo == null) {
            accountInfo = new AccountInfo(address);

        }
        SearchResultDTO dto = new SearchResultDTO();
        dto.setData(accountInfo);
        dto.setType("account");
        return dto;
    }

    private SearchResultDTO getBlockByHeight(int chainId, String text) {
        Long height;
        try {
            height = Long.parseLong(text);
        } catch (Exception e) {
            return null;
        }
        BlockHeaderInfo blockHeaderInfo = blockService.getBlockHeader(chainId, height);
        if (blockHeaderInfo == null) {
            return null;
        }
        return getBlockInfo(chainId, blockHeaderInfo);
    }

    private SearchResultDTO getBlockInfo(int chainId, BlockHeaderInfo blockHeaderInfo) {
        Result<BlockInfo> result = WalletRpcHandler.getBlockInfo(chainId, blockHeaderInfo.getHash());
        if (result.isFailed()) {
            throw new JsonRpcException(result.getErrorCode());
        }
        BlockInfo block = result.getData();
        if (null == block) {
            return null;
        } else {
            SearchResultDTO dto = new SearchResultDTO();
            dto.setData(block);
            dto.setType("block");
            return dto;
        }
    }
}
