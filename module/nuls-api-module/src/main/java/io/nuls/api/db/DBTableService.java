package io.nuls.api.db;

import io.nuls.api.model.po.db.ChainConfigInfo;
import io.nuls.api.model.po.db.ChainInfo;

/**
 *
 */
public interface DBTableService {

    void initCache();

    void addDefaultChainCache();

    void addChainCache(ChainInfo chainInfo, ChainConfigInfo configInfo);
}
