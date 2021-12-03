package io.nuls.provider.rpctools;

import io.nuls.base.api.provider.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.model.dto.ContractTokenInfoDto;
import io.nuls.provider.rpctools.vo.AccountBalance;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:31
 * @Description: 账本模块工具类
 */
@Component
public class LegderTools implements CallRpc {

    @Autowired
    private ContractTools contractTools;

    /**
     * 获取可用余额和nonce
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


    public Result<List> getAllAsset(int chainId) {
        Map<String, Object> params = new HashMap(2);
        params.put(Constants.CHAIN_ID, chainId);
        try {
            return callRpc(ModuleE.LG.abbr, "lg_get_all_asset", params, (Function<Map<String, Object>, Result<List>>) map -> {
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

    public Result<List<AccountBalance>> getBalanceList(int chainId, List<Map> coinDtoList, String address) {
        try {
            List<AccountBalance> accountBalanceList = new ArrayList<>();
            for (int i = 0; i < coinDtoList.size(); i++) {
                Map map = coinDtoList.get(i);
                int assetChainId = (int) map.get("chainId");
                int assetId = (int) map.get("assetId");
                String contractAddress = (String) map.get("contractAddress");
                if (assetId > 0) {
                    AccountBalance accountBalance = getBalanceAndNonce(chainId, assetChainId, assetId, address).getData();
                    accountBalance.setAssetChainId(assetChainId);
                    accountBalance.setAssetId(assetId);
                    accountBalance.setContractAddress(contractAddress);
                    accountBalanceList.add(accountBalance);
                } else {
                    ContractTokenInfoDto dto = contractTools.getTokenBalance(chainId, contractAddress, address).getData();
                    AccountBalance accountBalance = new AccountBalance();
                    accountBalance.setAssetChainId(assetChainId);
                    accountBalance.setAssetId(assetId);
                    accountBalance.setContractAddress(contractAddress);
                    if (dto == null) {
                        accountBalance.setBalance("0");
                        accountBalance.setTotalBalance("0");
                    } else {
                        accountBalance.setBalance(dto.getAmount());
                        accountBalance.setTotalBalance(dto.getAmount());
                    }

                    accountBalance.setTimeLock("0");
                    accountBalance.setConsensusLock("0");
                    accountBalance.setFreeze("0");
                    accountBalanceList.add(accountBalance);
                }
            }
            return new Result<List<AccountBalance>>(accountBalanceList);
        } catch (NulsRuntimeException e) {
            return Result.fail(e.getCode(), e.getMessage());
        }

    }
}
