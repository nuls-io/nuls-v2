package io.nuls.provider.rpctools;

import io.nuls.base.api.provider.Result;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.provider.rpctools.vo.AccountBalance;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-12 17:31
 * @Description: 账本模块工具类
 */
@Component
public class LegderTools implements CallRpc {

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


}
