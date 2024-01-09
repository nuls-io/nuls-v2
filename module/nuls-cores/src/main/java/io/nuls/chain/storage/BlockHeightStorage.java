package io.nuls.chain.storage;


import io.nuls.chain.model.po.BlockHeight;

/**
 *
 * Block height storage
 * @author lan
 * @date 2019/01/05
 */
public interface BlockHeightStorage {

    /**
     *
     * @param chainId
     * @return
     */
    BlockHeight getBlockHeight(int chainId);


    /**
     *
     * @param chainId
     * @param blockHeight
     */
    void saveOrUpdateBlockHeight(int chainId,  BlockHeight blockHeight) throws Exception;
}
