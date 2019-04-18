package io.nuls.api.db;

import io.nuls.api.model.po.db.CoinDataInfo;
import io.nuls.api.model.po.db.PageInfo;
import io.nuls.api.model.po.db.TransactionInfo;
import io.nuls.api.model.po.db.TxRelationInfo;

import java.util.List;
import java.util.Set;

public interface TransactionService {

    void saveTxList(int chainId, List<TransactionInfo> txList);

    void saveCoinDataList(int chainId, List<CoinDataInfo> coinDataList);

    void saveTxRelationList(int chainId, Set<TxRelationInfo> relationInfos);

    PageInfo<TransactionInfo> getTxList(int chainId, int pageIndex, int pageSize, int type, boolean isHidden);

    PageInfo<TransactionInfo> getBlockTxList(int chainId, int pageIndex, int pageSize, long blockHeight, int type);

    TransactionInfo getTx(int chainId, String txHash);

    void rollbackTxRelationList(int chainId, List<String> txHashList);

    void rollbackTx(int chainId, List<String> txHashList);

    void saveUnConfirmTx(int chainId, TransactionInfo tx);
}
