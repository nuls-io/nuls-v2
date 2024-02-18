package io.nuls.chain.storage;


import io.nuls.chain.model.po.CacheDatas;

/**
 *
 * Backup and restore transaction data that is closely related to the block
 * @author lan
 * @date 2019/01/05
 */
public interface CacheDatasStorage {

    /**
     *
     * @param key        blockHeight
     * @param moduleTxDatas
     * @throws Exception Any error will throw an exception
     */
    void save(long key, CacheDatas moduleTxDatas) throws Exception;

    /**
     * @param key  blockHeight
     * @throws Exception Any error will throw an exception
     */
    void delete(long key) throws Exception;


    /**
     * @param key block height
     * @return ModuleTxDatas
     * @throws Exception Any error will throw an exception
     */
    CacheDatas load(long key) throws Exception;
}
