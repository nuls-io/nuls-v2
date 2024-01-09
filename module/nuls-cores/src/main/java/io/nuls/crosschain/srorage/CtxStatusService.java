package io.nuls.crosschain.srorage;

import io.nuls.base.data.NulsHash;
import io.nuls.crosschain.model.po.CtxStatusPO;

import java.util.List;

/**
 * Cross chain transaction database related operations
 * Cross-Chain Transaction Database Related Operations
 *
 * @author  tag
 * 2019/6/24
 * */
public interface CtxStatusService {
    /**
     * preserve
     * @param atxHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param ctx       Main network protocol cross chain transactions
     * @param chainID   chainID
     * @return          Whether the save was successful or not
     * */
    boolean save(NulsHash atxHash, CtxStatusPO ctx, int chainID);

    /**
     * query
     * @param atxHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          HashCorresponding transactions
     * */
    CtxStatusPO get(NulsHash atxHash, int chainID);

    /**
     * delete
     * @param atxHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          Whether the deletion was successful or not
     * */
    boolean delete(NulsHash atxHash,int chainID);

    /**
     * Query All
     * @param chainID   chainID
     * @return          All data in this table
     * */
    List<CtxStatusPO> getList(int chainID);
}
