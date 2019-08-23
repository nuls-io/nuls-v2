package io.nuls.api.db;

import io.nuls.api.model.po.ChainConfigInfo;
import io.nuls.api.model.po.ChainInfo;

/**
 *
 */
public interface DBTableService {

    void initCache();

    void addDefaultChainCache();

    void addChainCache(ChainInfo chainInfo, ChainConfigInfo configInfo);
}
