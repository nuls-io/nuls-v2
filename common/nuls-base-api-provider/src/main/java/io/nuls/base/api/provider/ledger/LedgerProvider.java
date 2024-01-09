package io.nuls.base.api.provider.ledger;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ledger.facade.*;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 13:42
 * @Description: Function Description
 */
public interface LedgerProvider {

    /**
     * Obtain account balance
     *
     * @param req
     * @return
     */
    Result<AccountBalanceInfo> getBalance(GetBalanceReq req);


    Result<Map> getLocalAsset(GetAssetReq req);

    Result<Map> getContractAsset(ContractAsset req);

    /**
     * Local asset registration
     *
     * @param req
     * @return
     */
    Result<Map> regLocalAsset(RegLocalAssetReq req);

}
