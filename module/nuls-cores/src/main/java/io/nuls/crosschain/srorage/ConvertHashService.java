package io.nuls.crosschain.srorage;

import io.nuls.base.data.NulsHash;

import java.util.List;

/**
 * Cross chain transaction protocolHashCorresponding table
 * Hash Correspondence Table of Cross-Chain Transaction Protocol
 *
 * @author  tag
 * 2019/6/19
 * */
public interface ConvertHashService {
    /**
     * preserve
     * @param originalHash    Cross chain transactions receivedHash
     * @param localHash       Cross chain transactions under this chain protocolHahs
     * @param chainID         chainID
     * @return                Whether the save was successful or not
     * */
    boolean save(NulsHash originalHash, NulsHash localHash, int chainID);

    /**
     * query
     * @param originalHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          HashCorresponding transactions
     * */
    NulsHash get(NulsHash originalHash, int chainID);

    /**
     * delete
     * @param originalHash   Friendly Chain Protocol Cross Chain TransactionsHash
     * @param chainID   chainID
     * @return          Whether the deletion was successful or not
     * */
    boolean delete(NulsHash originalHash,int chainID);

    /**
     * Query All
     * @param chainID   chainID
     * @return          All data in this table
     * */
    List<NulsHash> getList(int chainID);
}
