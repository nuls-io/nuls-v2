package io.nuls.chain.storage;


import io.nuls.chain.model.dto.BlockChain;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainStorage {

    /**
     * Save chain
     *
     * @param key   The key
     * @param chain Chain object that needs to be saved
     * @return true/false
     */
    boolean save(int key, BlockChain chain);

    /**
     * Update chain
     *
     * @param key
     * @param blockChain
     * @return
     */
    boolean update(int key, BlockChain blockChain);

    /**
     * delete chain
     *
     * @param key
     * @return
     */
    boolean delete(int key);


    /**
     * Find chain based on key
     *
     * @param key The key
     * @return Chain object
     */
    BlockChain load(int key);


}
