package io.nuls.api.rpc.controller;

import io.nuls.api.ApiContext;
import io.nuls.api.analysis.WalletRpcHandler;
import io.nuls.api.cache.ApiCache;
import io.nuls.api.constant.AddressType;
import io.nuls.api.db.*;
import io.nuls.api.exception.JsonRpcException;
import io.nuls.api.manager.CacheManager;
import io.nuls.api.model.po.*;
import io.nuls.api.model.rpc.RpcErrorCode;
import io.nuls.api.model.rpc.RpcResult;
import io.nuls.api.model.rpc.RpcResultError;
import io.nuls.api.model.rpc.SearchResultDTO;
import io.nuls.api.utils.AssetTool;
import io.nuls.api.utils.DBUtil;
import io.nuls.api.utils.VerifyUtils;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ChainController {

    @Autowired
    private BlockService blockService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private AccountLedgerService ledgerService;
    @Autowired
    private ContractService contractService;
    @Autowired
    private StatisticalService statisticalService;
    @Autowired
    private AgentService agentService;
    @Autowired
    private ChainService chainService;

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

        List<ChainInfo> chainInfoList = chainService.getOtherChainInfoList(chainId);
//
//
//        List<Map<String, Object>> chainInfoList = new ArrayList<>();
//        for (ChainInfo chainInfo : CacheManager.getChainInfoMap().values()) {
//            if (chainInfo.getChainId() != chainId) {
//                Map<String, Object> map = new HashMap<>();
//                map.put("chainId", chainInfo.getChainId());
//                map.put("chainName", chainInfo.getChainName());
//                chainInfoList.add(map);
//            }
//        }
        ;
        return RpcResult.success(chainInfoList.stream().map(d->{
            if(d.getChainId() == 9){
                d.setChainName("NerveNetwork");
            }
            return d;
        }).collect(Collectors.toList()));
    }

    @RpcMethod("getOtherChainInfo")
    public RpcResult getOtherChainInfo(List<Object> params) {
        VerifyUtils.verifyParams(params, 1);
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is invalid");
        }

        ChainInfo chainInfo = chainService.getChainInfo(chainId);
        if (chainInfo != null) {
            if (chainInfo.getChainId() == 9) {
                chainInfo.setChainName("NerveNetwork");
            }
        }

        return RpcResult.success(chainInfo);
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

        Map<String, Object> map = new HashMap<>();
        map.put("chainId", chainId);
        map.put("networkHeight", ApiContext.networkHeight);
        map.put("localHeight", ApiContext.localHeight);

        ApiCache apiCache = CacheManager.getCache(chainId);
        AssetInfo assetInfo = apiCache.getChainInfo().getDefaultAsset();
        Map<String, Object> assetMap = new HashMap<>();
        assetMap.put("chainId", assetInfo.getChainId());
        assetMap.put("assetId", assetInfo.getAssetId());
        assetMap.put("symbol", assetInfo.getSymbol());
        assetMap.put("decimals", assetInfo.getDecimals());
        map.put("defaultAsset", assetMap);
        //agentAsset
        assetInfo = CacheManager.getRegisteredAsset(DBUtil.getAssetKey(apiCache.getConfigInfo().getAgentChainId(), apiCache.getConfigInfo().getAgentAssetId()));
        if (assetInfo != null) {
            assetMap = new HashMap<>();
            assetMap.put("chainId", assetInfo.getChainId());
            assetMap.put("assetId", assetInfo.getAssetId());
            assetMap.put("symbol", assetInfo.getSymbol());
            assetMap.put("decimals", assetInfo.getDecimals());
            map.put("agentAsset", assetMap);
        } else {
            map.put("agentAsset", null);
        }
        map.put("magicNumber", ApiContext.magicNumber);
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

    @RpcMethod("getByzantineCount")
    public RpcResult getByzantineCount(List<Object> params) {
        int chainId;
        String txHash;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHash = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHash] is inValid");
        }

        Result result = WalletRpcHandler.getByzantineCount(chainId, txHash);
        if (result.isFailed()) {
            throw new JsonRpcException(result.getErrorCode());
        }
        Map<String, Object> map = (Map<String, Object>) result.getData();
        return RpcResult.success(map);
    }

    @RpcMethod("assetGet")
    public RpcResult assetGet(List<Object> params) {
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("trades", coinContextInfo.getTxCount());
        map.put("totalAssets", AssetTool.toCoinString(coinContextInfo.getTotal()));
        map.put("circulation", AssetTool.toCoinString(coinContextInfo.getCirculation()));
        map.put("deposit", AssetTool.toCoinString(coinContextInfo.getConsensusTotal()));
        map.put("business", AssetTool.toCoinString(coinContextInfo.getBusiness()));
        map.put("team", AssetTool.toCoinString(coinContextInfo.getTeam()));
        map.put("community", AssetTool.toCoinString(coinContextInfo.getCommunity()));
        map.put("unmapped", AssetTool.toCoinString(coinContextInfo.getUnmapped()));
        map.put("destroy", AssetTool.toCoinString(coinContextInfo.getDestroy()));
        int consensusCount = apiCache.getCurrentRound().getMemberCount() - apiCache.getChainInfo().getSeeds().size();
        if (consensusCount < 0) {
            consensusCount = 0;
        }
        map.put("consensusNodes", consensusCount);
        long count = 0;
        if (apiCache.getBestHeader() != null) {
            count = agentService.agentsCount(chainId, apiCache.getBestHeader().getHeight());
        }
        map.put("totalNodes", count);

        return RpcResult.success(map);
    }

    @RpcMethod("getTotalSupply")
    public RpcResult getTotalSupply(List<Object> params) {
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        Map<String, Object> map = new HashMap<>();
        BigInteger supply = coinContextInfo.getTotal().subtract(coinContextInfo.getDestroy());
        map.put("supplyCoin", AssetTool.toCoinString(supply) + "");
        return RpcResult.success(map);
    }


    @RpcMethod("getCirculation")
    public RpcResult getCirculation(List<Object> params) {
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("circulation", AssetTool.toCoinString(coinContextInfo.getCirculation()) + "");
        return RpcResult.success(map);
    }

    @RpcMethod("getDestroy")
    public RpcResult getDestroy(List<Object> params) {
        int chainId;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }

        ApiCache apiCache = CacheManager.getCache(chainId);
        if (apiCache == null) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        CoinContextInfo coinContextInfo = apiCache.getCoinContextInfo();
        Map<String, Object> map = new HashMap<>();
        map.put("destroy", AssetTool.toCoinString(coinContextInfo.getDestroy()) + "");
        map.put("list", coinContextInfo.getDestroyInfoList());
        return RpcResult.success(map);
    }
}
