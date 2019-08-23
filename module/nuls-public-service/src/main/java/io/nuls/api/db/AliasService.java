package io.nuls.api.db;

import io.nuls.api.model.po.AliasInfo;

import java.util.List;

public interface AliasService {

    void initCache();

    AliasInfo getAliasByAddress(int chainId, String address);

    AliasInfo getByAlias(int chainId, String alias);

    void saveAliasList(int chainId, List<AliasInfo> aliasInfoList) ;

    void rollbackAliasList(int chainId, List<AliasInfo> aliasInfoList);
}
