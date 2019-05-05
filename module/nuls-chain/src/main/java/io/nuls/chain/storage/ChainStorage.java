package io.nuls.chain.storage;


import io.nuls.chain.model.po.BlockChain;

/**
 * 关于链的所有操作：增删改查
 * All operations on the chain: Save, delete, update, query
 *
 * @author tangyi
 * @date 2018/11/8
 */
public interface ChainStorage {

    /**
     * 保存链到数据库
     * Save BlockChain to database
     *
     * @param key        Chain ID
     * @param blockChain The BlockChain saved
     * @throws Exception Any error will throw an exception
     */
    void save(int key, BlockChain blockChain) throws Exception;

    /**
     * 更新链信息
     * Update BlockChain
     *
     * @param key        Chain ID
     * @param blockChain The BlockChain updated
     * @throws Exception Any error will throw an exception
     */
    void update(int key, BlockChain blockChain) throws Exception;

    /**
     * 从数据库彻底删除链
     * Delete BlockChain in database
     *
     * @param key Chain ID
     * @throws Exception Any error will throw an exception
     */
    void delete(int key) throws Exception;


    /**
     * 根据序号获取链
     * Get the chain according to the ID
     *
     * @param key Chain ID
     * @return BlockChain
     * @throws Exception Any error will throw an exception
     */
    BlockChain load(int key) throws Exception;

}
