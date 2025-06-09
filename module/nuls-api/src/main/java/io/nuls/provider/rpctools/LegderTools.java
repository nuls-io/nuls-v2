package io.nuls.provider.rpctools;

import io.nuls.base.api.provider.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.api.cache.LedgerAssetCache;
import io.nuls.provider.api.model.AssetInfo;
import io.nuls.provider.model.dto.ContractTokenInfoDto;
import io.nuls.provider.rpctools.vo.AccountBalance;
import io.nuls.provider.rpctools.vo.AccountBalanceWithDecimals;

import java.math.BigInteger;
import java.util.*;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:31
 * @Description: Ledger module tool class
 */
@Component
public class LegderTools implements CallRpc {

    @Autowired
    private ContractTools contractTools;
    @Autowired
    private LedgerAssetCache ledgerAssetCache;

    /**
     * Obtain available balance andnonce
     * Get the available balance and nonce
     */
    public Result<AccountBalance> getBalanceAndNonce(int chainId, int assetChainId, int assetId, String address) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("assetChainId", assetChainId);
        params.put("address", address);
        params.put("assetId", assetId);
        try {
            return callRpc(ModuleE.LG.abbr, "getBalanceNonce", params, (Function<Map<String, Object>, Result<AccountBalance>>) map -> {
                if (map == null) {
                    return null;
                }
                AccountBalance balanceInfo = new AccountBalance();
                balanceInfo.setBalance(map.get("available").toString());
                balanceInfo.setTimeLock(map.get("timeHeightLocked").toString());
                balanceInfo.setConsensusLock(map.get("permanentLocked").toString());
                balanceInfo.setFreeze(map.get("freeze").toString());
                balanceInfo.setNonce((String) map.get("nonce"));
                balanceInfo.setTotalBalance(new BigInteger(balanceInfo.getBalance())
                        .add(new BigInteger(balanceInfo.getConsensusLock()))
                        .add(new BigInteger(balanceInfo.getTimeLock())).toString());
                balanceInfo.setNonceType((Integer) map.get("nonceType"));
                return new Result<>(balanceInfo);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<Map<String, AccountBalance>> getBalanceAndNonceMap(int chainId, List<String> assetKeyList, String address, List<Boolean> isConfirmedList) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("assetKeyList", assetKeyList);
        params.put("address", address);
        params.put("isConfirmedList", isConfirmedList);
        try {
            return callRpc(ModuleE.LG.abbr, "getBalanceNonceList", params, (Function<Map<String, Object>, Result<Map<String, AccountBalance>>>) _map -> {
                if (_map == null) {
                    return null;
                }
                Map<String, AccountBalance> resultMap = new LinkedHashMap<>();
                Map<String, Map> dataMap = (Map<String, Map>) _map.get("list");
                for (int i = 0; i < assetKeyList.size(); i++) {
                    String assetKey = assetKeyList.get(i);
                    Map map = dataMap.get(assetKey);
                    if (map == null) {
                        continue;
                    }

                    AccountBalance balanceInfo = new AccountBalance();
                    balanceInfo.setBalance(map.get("available").toString());
                    balanceInfo.setTimeLock(map.get("timeHeightLocked").toString());
                    balanceInfo.setConsensusLock(map.get("permanentLocked").toString());
                    balanceInfo.setFreeze(map.get("freeze").toString());
                    balanceInfo.setNonce((String) map.get("nonce"));
                    balanceInfo.setTotalBalance(new BigInteger(balanceInfo.getBalance())
                            .add(new BigInteger(balanceInfo.getConsensusLock()))
                            .add(new BigInteger(balanceInfo.getTimeLock())).toString());
                    balanceInfo.setNonceType((Integer) map.get("nonceType"));
                    resultMap.put(assetKey, balanceInfo);
                }

                return new Result<Map<String, AccountBalance>>(resultMap);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<AccountBalanceWithDecimals> getBalanceAndNonceWithDecimals(int chainId, int assetChainId, int assetId, String address) {
        Map<String, Object> params = new HashMap(4);
        params.put(Constants.CHAIN_ID, chainId);
        params.put("assetChainId", assetChainId);
        params.put("address", address);
        params.put("assetId", assetId);
        try {
            return callRpc(ModuleE.LG.abbr, "getBalanceNonce", params, (Function<Map<String, Object>, Result<AccountBalanceWithDecimals>>) map -> {
                if (map == null) {
                    return null;
                }
                AccountBalanceWithDecimals balanceInfo = new AccountBalanceWithDecimals();
                balanceInfo.setBalance(map.get("available").toString());
                balanceInfo.setTimeLock(map.get("timeHeightLocked").toString());
                balanceInfo.setConsensusLock(map.get("permanentLocked").toString());
                balanceInfo.setFreeze(map.get("freeze").toString());
                balanceInfo.setNonce((String) map.get("nonce"));
                balanceInfo.setTotalBalance(new BigInteger(balanceInfo.getBalance())
                        .add(new BigInteger(balanceInfo.getConsensusLock()))
                        .add(new BigInteger(balanceInfo.getTimeLock())).toString());
                balanceInfo.setNonceType((Integer) map.get("nonceType"));
                AssetInfo assetInfo = ledgerAssetCache.getAssetInfo(assetChainId, assetId);
                if (assetInfo != null) {
                    balanceInfo.setDecimals(assetInfo.getDecimals());
                }
                return new Result<>(balanceInfo);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }


    public Result<Map> getAllAsset(int chainId) {
        Map<String, Object> params = new HashMap(2);
        params.put(Constants.CHAIN_ID, chainId);
        try {
            return callRpc(ModuleE.LG.abbr, "lg_get_all_asset", params, (Function<Map<String, Object>, Result<Map>>) map -> {
                if (map == null) {
                    return null;
                }
                List assets = (List) map.get("assets");
                return new Result<>(assets);
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<AssetInfo> getAsset(int assetChainId, int assetId) {
        Map<String, Object> params = new HashMap(2);
        params.put(Constants.CHAIN_ID, assetChainId);
        params.put("assetId", assetId);
        try {
            return callRpc(ModuleE.LG.abbr, "getAssetRegInfoByAssetId", params, (Function<Map<String, Object>, Result<AssetInfo>>) map -> {
                if (map == null || map.get("assetSymbol") == null) {
                    return Result.fail(CommonCodeConstanst.DATA_NOT_FOUND.getCode(), null);
                }
                int decimalPlace = Integer.parseInt(map.get("decimalPlace").toString());
                String symbol = map.get("assetSymbol").toString();
                return new Result<>(new AssetInfo(assetChainId, assetId, symbol, decimalPlace));
            });
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }
    }

    public Result<List<AccountBalanceWithDecimals>> getBalanceWithDecimalsList(int chainId, List<Map> coinDtoList, String address) {
        try {
            List<AccountBalanceWithDecimals> accountBalanceList = new ArrayList<>();
            List<String> assetKeyList = new ArrayList<>();
            List<Boolean> isConfirmedList = new ArrayList<>();
            List<String> contractList = new ArrayList<>();
            List methods = new ArrayList();
            List pars = new ArrayList();
            for (int i = 0; i < coinDtoList.size(); i++) {
                Map map = coinDtoList.get(i);
                int assetChainId = (int) map.get("chainId");
                int assetId = (int) map.get("assetId");
                String contractAddress = (String) map.get("contractAddress");
                if (StringUtils.isBlank(contractAddress)) {
                    contractList.add(contractAddress);
                    contractList.add(contractAddress);
                    contractList.add(contractAddress);
                    methods.add("balanceOf");
                    methods.add("lockedBalanceOf");
                    methods.add("decimals");
                    pars.add(address);
                    pars.add(address);
                    pars.add("");
                } else {
                    assetKeyList.add(assetChainId + "-" + assetId);
                    isConfirmedList.add(false);
                }
            }
            Map<String, AccountBalanceWithDecimals> resultMap1 = new LinkedHashMap<>();
            if (!assetKeyList.isEmpty()) {
                Map<String, AccountBalance> _resultMap1 = getBalanceAndNonceMap(chainId, assetKeyList, address, isConfirmedList).getData();
                if (_resultMap1 != null && !_resultMap1.isEmpty()) {
                    _resultMap1.forEach((k, v) -> {
                        AssetInfo assetInfo = ledgerAssetCache.getAssetInfo(v.getAssetChainId(), v.getAssetId());
                        int decimals = 0;
                        if (assetInfo != null) {
                            decimals = assetInfo.getDecimals();
                        }
                        resultMap1.put(k, v.toAccountBalanceWithDecimals(decimals));
                    });
                }
            }
            Map<String, AccountBalanceWithDecimals> resultMap2 = new LinkedHashMap<>();
            if (!contractList.isEmpty()) {
                Result<List<String>> multicall = contractTools.multicall(chainId, 0, contractList, methods, pars);
                List<String> dataList = multicall.getData();
                for (int i = 0; i < contractList.size(); i += 3) {
                    String contractAddress = contractList.get(i);
                    String availableStr = dataList.get(i);
                    if (StringUtils.isBlank(availableStr)) {
                        availableStr = "0";
                    }
                    String lockedStr = dataList.get(i + 1);
                    if (StringUtils.isBlank(lockedStr)) {
                        lockedStr = "0";
                    }
                    String decimalsStr = dataList.get(i + 2);
                    if (StringUtils.isBlank(decimalsStr)) {
                        decimalsStr = "0";
                    }
                    AccountBalanceWithDecimals accountBalance = new AccountBalanceWithDecimals();
                    accountBalance.setAssetChainId(0);
                    accountBalance.setAssetId(0);
                    accountBalance.setContractAddress(contractAddress);
                    accountBalance.setBalance(availableStr);
                    accountBalance.setTotalBalance(new BigInteger(availableStr).add(new BigInteger(lockedStr)).toString());
                    accountBalance.setConsensusLock(lockedStr);
                    accountBalance.setTimeLock("0");
                    accountBalance.setFreeze("0");
                    accountBalance.setDecimals(Integer.parseInt(decimalsStr));
                    resultMap2.put(contractAddress, accountBalance);
                }
            }

            Map<String, AccountBalanceWithDecimals> allDataMap = new LinkedHashMap<>();
            allDataMap.putAll(resultMap1);
            allDataMap.putAll(resultMap2);

            for (int i = 0; i < coinDtoList.size(); i++) {
                Map map = coinDtoList.get(i);
                int assetChainId = (int) map.get("chainId");
                int assetId = (int) map.get("assetId");
                String contractAddress = (String) map.get("contractAddress");
                if (StringUtils.isBlank(contractAddress)) {
                    accountBalanceList.add(allDataMap.get(contractAddress));
                } else {
                    accountBalanceList.add(allDataMap.get(assetChainId + "-" + assetId));
                }
            }
            return new Result<List<AccountBalanceWithDecimals>>(accountBalanceList);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }

    }

    public Result<List<AccountBalance>> getBalanceList(int chainId, List<Map> coinDtoList, String address) {
        try {
            List<AccountBalance> accountBalanceList = new ArrayList<>();
            List<String> assetKeyList = new ArrayList<>();
            List<Boolean> isConfirmedList = new ArrayList<>();
            List<String> contractList = new ArrayList<>();
            List methods = new ArrayList();
            List pars = new ArrayList();
            for (int i = 0; i < coinDtoList.size(); i++) {
                Map map = coinDtoList.get(i);
                int assetChainId = (int) map.get("chainId");
                int assetId = (int) map.get("assetId");
                String contractAddress = (String) map.get("contractAddress");
                if (StringUtils.isBlank(contractAddress)) {
                    contractList.add(contractAddress);
                    contractList.add(contractAddress);
                    methods.add("balanceOf");
                    methods.add("lockedBalanceOf");
                    pars.add(address);
                    pars.add(address);
                } else {
                    assetKeyList.add(assetChainId + "-" + assetId);
                    isConfirmedList.add(false);
                }
            }
            Map<String, AccountBalance> resultMap1 = new LinkedHashMap<>();
            if (!assetKeyList.isEmpty()) {
                resultMap1 = getBalanceAndNonceMap(chainId, assetKeyList, address, isConfirmedList).getData();
                if (resultMap1 == null) {
                    resultMap1 = new LinkedHashMap<>();
                }
            }
            Map<String, AccountBalance> resultMap2 = new LinkedHashMap<>();
            if (!contractList.isEmpty()) {
                Result<List<String>> multicall = contractTools.multicall(chainId, 0, contractList, methods, pars);
                List<String> dataList = multicall.getData();
                for (int i = 0; i < contractList.size(); i += 2) {
                    String contractAddress = contractList.get(i);
                    String availableStr = dataList.get(i);
                    if (StringUtils.isBlank(availableStr)) {
                        availableStr = "0";
                    }
                    String lockedStr = dataList.get(i + 1);
                    if (StringUtils.isBlank(lockedStr)) {
                        lockedStr = "0";
                    }
                    AccountBalance accountBalance = new AccountBalance();
                    accountBalance.setAssetChainId(0);
                    accountBalance.setAssetId(0);
                    accountBalance.setContractAddress(contractAddress);
                    accountBalance.setBalance(availableStr);
                    accountBalance.setTotalBalance(new BigInteger(availableStr).add(new BigInteger(lockedStr)).toString());
                    accountBalance.setConsensusLock(lockedStr);
                    accountBalance.setTimeLock("0");
                    accountBalance.setFreeze("0");
                    resultMap2.put(contractAddress, accountBalance);
                }
            }

            Map<String, AccountBalance> allDataMap = new LinkedHashMap<>();
            allDataMap.putAll(resultMap1);
            allDataMap.putAll(resultMap2);

            for (int i = 0; i < coinDtoList.size(); i++) {
                Map map = coinDtoList.get(i);
                int assetChainId = (int) map.get("chainId");
                int assetId = (int) map.get("assetId");
                String contractAddress = (String) map.get("contractAddress");
                if (StringUtils.isBlank(contractAddress)) {
                    accountBalanceList.add(allDataMap.get(contractAddress));
                } else {
                    accountBalanceList.add(allDataMap.get(assetChainId + "-" + assetId));
                }
            }
            return new Result<List<AccountBalance>>(accountBalanceList);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }

    }
}
