package io.nuls.api.db;

import io.nuls.api.model.po.AccountTokenInfo;
import io.nuls.api.model.po.PageInfo;
import io.nuls.api.model.po.TokenTransfer;

import java.util.List;
import java.util.Map;

public interface TokenService {

    AccountTokenInfo getAccountTokenInfo(int chainId, String key);

    void saveAccountTokens(int chainId, Map<String, AccountTokenInfo> accountTokenInfos);

    PageInfo<AccountTokenInfo> getAccountTokens(int chainId, String address, int pageNumber, int pageSize);

    PageInfo<AccountTokenInfo> getContractTokens(int chainId, String contractAddress, int pageNumber, int pageSize);

    void saveTokenTransfers(int chainId, List<TokenTransfer> tokenTransfers);

    void rollbackTokenTransfers(int chainId, List<String> tokenTxHashs, long height);

    PageInfo<TokenTransfer> getTokenTransfers(int chainId, String address, String contractAddress, int pageIndex, int pageSize);

}
