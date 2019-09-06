package io.nuls.api.db;

import io.nuls.api.model.po.ChainStatisticalInfo;
import io.nuls.api.model.po.StatisticalInfo;

import java.util.List;

public interface StatisticalService {

    long getBestId(int chainId);

    void saveBestId(int chainId, long id);

    void updateBestId(int chainId, long id);

    void insert(int chainId, StatisticalInfo info);

    long calcTxCount(int chainId, long start, long end);

    List getStatisticalList(int chainId, int type, String field);

    ChainStatisticalInfo getChainStatisticalInfo(int chainId);

    void saveChainStatisticalInfo(ChainStatisticalInfo statisticalInfo);

}
