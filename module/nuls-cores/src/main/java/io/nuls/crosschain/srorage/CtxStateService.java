package io.nuls.crosschain.srorage;

import java.util.List;

/**
 * Cross chain transaction processing results database related operations
 * Cross-Chain Transaction Processing Result Database Related Operating Classes
 *
 * @author  tag
 * 2019/4/16
 * */
public interface CtxStateService {
    /**
     * preserve
     * @param atxHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          Whether the save was successful or not
     * */
    boolean save(byte[] atxHash,int chainID);

    /**
     * query
     * @param atxHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          HashCorresponding transactions
     * */
    boolean get(byte[] atxHash, int chainID);

    /**
     * delete
     * @param atxHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          Whether the deletion was successful or not
     * */
    boolean delete(byte[] atxHash,int chainID);

    /**
     * Query All
     * @param chainID   chainID
     * @return          All data in this table
     * */
    List<byte[]> getList(int chainID);
}
