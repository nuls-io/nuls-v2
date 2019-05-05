package io.nuls.chain.storage;


import io.nuls.chain.model.po.CacheDatas;

/**
 *
 * 区块里关注的交易数据进行备份与还原
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
