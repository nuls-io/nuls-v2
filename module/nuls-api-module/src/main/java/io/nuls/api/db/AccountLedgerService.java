package io.nuls.api.db;

import io.nuls.api.model.po.db.AccountLedgerInfo;

import java.util.List;
import java.util.Map;

public interface AccountLedgerService {

    void initCache();

    AccountLedgerInfo getAccountLedgerInfo(int chainId, String key);

    void saveLedgerList(int chainId, Map<String, AccountLedgerInfo> accountLedgerInfoMap);

    List<AccountLedgerInfo> getAccountCrossLedgerInfoList(int chainId, String address);

}
