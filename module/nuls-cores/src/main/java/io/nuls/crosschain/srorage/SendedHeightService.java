package io.nuls.crosschain.srorage;

import io.nuls.crosschain.model.po.SendCtxHashPO;

import java.util.Map;

/**
 * The height of blocks broadcasted to other chain nodes and the cross chain transactions broadcastedHashList database related operations
 * Block Height Broadcast to Other Chain Nodes and Related Operation of Broadcast Cross-Chain Transaction Hash List Database
 *
 * @author  tag
 * 2019/4/16
 * */
public interface SendedHeightService {
    /**
     * preserve
     * @param height            Friendly Chain Protocol Cross Chain TransactionsHash
     * @param po                po
     * @param chainID           chainID
     * @return                  Whether the save was successful or not
     * */
    boolean save(long height, SendCtxHashPO po, int chainID);

    /**
     * query
     * @param height    Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          Highly Corresponding TransactionsHashlist
     * */
    SendCtxHashPO get(long height, int chainID);

    /**
     * delete
     * @param height    Delete key
     * @param chainID   chainID
     * @return          Whether the deletion was successful or not
     * */
    boolean delete(long height,int chainID);

    /**
     * Query All
     * @param chainID   chainID
     * @return          All data in this table
     * */
    Map<Long , SendCtxHashPO> getList(int chainID);
}
