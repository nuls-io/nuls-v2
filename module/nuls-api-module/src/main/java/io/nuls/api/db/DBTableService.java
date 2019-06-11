package io.nuls.api.db;

/**
 *
 */
public interface DBTableService {

    void initCache();

    void addDefaultChain();

    void addChain(int chainId, int defaultAssetId, String chainName, String symbol, int decimals);
}
