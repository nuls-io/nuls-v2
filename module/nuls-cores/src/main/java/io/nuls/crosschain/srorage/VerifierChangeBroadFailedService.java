package io.nuls.crosschain.srorage;
import io.nuls.crosschain.model.po.VerifierChangeSendFailPO;

import java.util.Map;

/**
 * Main network verifier change message broadcast failure message database related interface
 * Main Network Verifier Change Message Broadcasting Failure Message Database Related Interface
 *
 * @author  tag
 * 2019/6/28
 * */
public interface VerifierChangeBroadFailedService {
    /**
     * preserve
     * @param height            Friendly Chain Protocol Cross Chain TransactionsHash
     * @param po                po
     * @param chainID           chainID
     * @return                  Whether the save was successful or not
     * */
    boolean save(long height, VerifierChangeSendFailPO po, int chainID);

    /**
     * query
     * @param height    Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          Highly Corresponding TransactionsHashlist
     * */
    VerifierChangeSendFailPO get(long height, int chainID);

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
    Map<Long , VerifierChangeSendFailPO> getList(int chainID);
}
