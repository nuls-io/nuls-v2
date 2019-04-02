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
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Controller;
import io.nuls.tools.core.annotation.RpcMethod;
import io.nuls.tools.log.Log;

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

    @RpcMethod("getChains")
    public RpcResult getChains(List<Object> params) {
        Map<String, Object> map = new HashMap<>();
        map.put("default", ApiContext.defaultChainId);
        map.put("list", CacheManager.getApiCaches().keySet());

        return RpcResult.success(map);
    }

    @RpcMethod("getCoinInfo")
    public RpcResult getCoinInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId = (int) params.get(0);
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        ApiCache apiCache = CacheManager.getCache(chainId);
        return RpcResult.success(apiCache.getContextInfo());
    }

    @RpcMethod("search")
    public RpcResult search(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId = (int) params.get(0);
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }

        String text = (String) params.get(1);
        text = text.trim();
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
        ContractInfo contractInfo = null;
        try {
            contractInfo = contractService.getContractInfo(chainId, text);
        } catch (Exception e) {
            Log.error(e);
            throw new JsonRpcException();
        }
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
            throw new NotFoundException();
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
