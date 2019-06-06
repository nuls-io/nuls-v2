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
import io.nuls.api.utils.LoggerUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import org.checkerframework.checker.units.qual.A;

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
        try {
            return RpcResult.success(CacheManager.getCache(ApiContext.defaultChainId).getChainInfo());
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }


    @RpcMethod("getInfo")
    public RpcResult getInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError();
        }
        if (!CacheManager.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        Result<Map<String, Object>> result = WalletRpcHandler.getBlockGlobalInfo(chainId);
        if (result.isFailed()) {
            return RpcResult.failed(result);
        }
        Map<String, Object> map = result.getData();
        map.put("isRunCrossChain", ApiContext.isRunCrossChain);
        map.put("isRunSmartContract", ApiContext.isRunSmartContract);
        return RpcResult.success(map);
    }

    @RpcMethod("getCoinInfo")
    public RpcResult getCoinInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        try {
            int chainId = (int) params.get(0);
            if (!CacheManager.isChainExist(chainId)) {
                return RpcResult.dataNotFound();
            }
            ApiCache apiCache = CacheManager.getCache(chainId);
            return RpcResult.success(apiCache.getContextInfo());
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    @RpcMethod("search")
    public RpcResult search(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);

        int chainId;
        String text;
        try {
            chainId = (int) params.get(0);
            text = (String) params.get(1);
            text = text.trim();
        } catch (Exception e) {
            return RpcResult.paramError();
        }

        try {
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
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return RpcResult.failed(RpcErrorCode.SYS_UNKNOWN_EXCEPTION);
        }
    }

    private SearchResultDTO getContractByAddress(int chainId, String text) {
        ContractInfo contractInfo = null;
        try {
            contractInfo = contractService.getContractInfo(chainId, text);
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
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
