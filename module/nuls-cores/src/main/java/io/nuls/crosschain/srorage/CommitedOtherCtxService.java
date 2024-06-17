package io.nuls.crosschain.srorage;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;

import java.util.List;

/**
 * Transactions approved by Byzantium with other chain signatures already packaged
 * Other packaged chain signatures Byzantine-approved transactions
 *
 * @author  tag
 * 2019/6/21
 * */
public interface CommitedOtherCtxService {
    /**
     * preserve
     * @param atxHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param ctx       Main network protocol cross chain transactions
     * @param chainID   chainID
     * @return          Whether the save was successful or not
     * */
    boolean save(NulsHash atxHash, Transaction ctx, int chainID);

    /**
     * query
     * @param atxHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          HashCorresponding transactions
     * */
    Transaction get(NulsHash atxHash, int chainID);

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
    List<Transaction> getList(int chainID);
}
