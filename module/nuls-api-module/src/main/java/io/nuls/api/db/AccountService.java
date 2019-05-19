package io.nuls.api.db;

import io.nuls.api.model.po.db.AccountInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.TxRelationInfo;
import io.nuls.api.model.po.db.mini.MiniAccountInfo;

import java.math.BigInteger;
import java.util.Map;

public interface AccountService {

    void initCache();

    AccountInfo getAccountInfo(int chainId, String address);

    void saveAccounts(int chainId, Map<String, AccountInfo> accountInfoMap);

    PageInfo<AccountInfo> pageQuery(int chainId, int pageNumber, int pageSize);

    PageInfo<TxRelationInfo> getAccountTxs(int chainId, String address, int pageIndex, int pageSize, int type, boolean isMark);

    PageInfo<MiniAccountInfo> getCoinRanking(int pageIndex, int pageSize, int sortType, int chainId);

    BigInteger getAllAccountBalance(int chainId);

    BigInteger getAccountTotalBalance(int chainId, String address);
}
