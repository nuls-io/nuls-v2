package io.nuls.chain.storage;


import io.nuls.chain.model.po.BlockHeight;

/**
 *
 * 区块高度存储
 * @author lan
 * @date 2019/01/05
 */
public interface BlockHeightStorage {

    /**
     *
     * @param chainId
     * @param isCirculateTx
     * @return
     */
    BlockHeight getBlockHeight(int chainId,boolean isCirculateTx);


    /**
     *
     * @param chainId
     * @param blockHeight
     * @param isCirculateTx
     */
    void saveOrUpdateBlockHeight(int chainId,  BlockHeight blockHeight,boolean isCirculateTx) throws Exception;
}
