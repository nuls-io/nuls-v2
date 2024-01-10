package io.nuls.crosschain.srorage;

import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;

/**
 * Registered cross chain transaction database operation class
 * Registered Cross-Chain Transaction Database Operations Class
 *
 * @author  tag
 * 2019/5/30
 * */
public interface RegisteredCrossChainService {
    /**
     * preserve
     * @param   registeredChainMessage  Registered cross chain chain list
     * @return  Whether the save was successful
     * */
    boolean save(RegisteredChainMessage registeredChainMessage);

    /**
     * query
     * @return  Registered cross chain chain information
     * */
    RegisteredChainMessage get();

    /**
     * Determine whether a specified asset can be traded across chains
     * @param assetChainId
     * @param assetId
     * @return
     */
    boolean canCross(int assetChainId,int assetId);

}
