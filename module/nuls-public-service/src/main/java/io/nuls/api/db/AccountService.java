package io.nuls.api.db;

import io.nuls.api.model.po.AccountInfo;
import io.nuls.api.model.po.PageInfo;
import io.nuls.api.model.po.TxRelationInfo;
import io.nuls.api.model.po.mini.MiniAccountInfo;

import java.math.BigInteger;
import java.util.Map;

public interface AccountService {

    void initCache();

    AccountInfo getAccountInfo(int chainId, String address);

    MiniAccountInfo getMiniAccountInfo(int chainId, String address);

    void saveAccounts(int chainId, Map<String, AccountInfo> accountInfoMap);

    PageInfo<AccountInfo> pageQuery(int chainId, int pageNumber, int pageSize);

    PageInfo<TxRelationInfo> getAccountTxs(int chainId, String address, int pageIndex, int pageSize, int type, long startHeight, long endHeight, int assetChainId, int assetId);

    PageInfo<TxRelationInfo> getAcctTxs(int chainId, int assetChainId, int assetId, String address, int type, long startTime, long endTime, int pageIndex, int pageSize);

    PageInfo<MiniAccountInfo> getCoinRanking(int pageIndex, int pageSize, int chainId);

    BigInteger getAllAccountBalance(int chainId);

    BigInteger getAccountTotalBalance(int chainId, String address);

    void testBalance(int chainId);
}
