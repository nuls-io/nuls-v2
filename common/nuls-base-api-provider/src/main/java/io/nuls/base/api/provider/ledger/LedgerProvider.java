package io.nuls.base.api.provider.ledger;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ledger.facade.AccountBalanceInfo;
import io.nuls.base.api.provider.ledger.facade.GetBalanceReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 13:42
 * @Description: 功能描述
 */
public interface LedgerProvider {

    /**
     * 获取账户余额
     * @param req
     * @return
     */
    Result<AccountBalanceInfo> getBalance(GetBalanceReq req);

}
