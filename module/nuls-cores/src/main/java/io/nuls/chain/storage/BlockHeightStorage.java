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
