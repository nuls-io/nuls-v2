package io.nuls.chain.storage;


import io.nuls.chain.model.dto.Chain;

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
    boolean save(int key, Chain chain);

    /**
     * Update chain
     *
     * @param key
     * @param chain
     * @return
     */
    boolean update(int key, Chain chain);

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
    Chain load(int key);


}
