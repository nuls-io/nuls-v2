package io.nuls.chain.storage;


import io.nuls.chain.model.po.BlockChain;

import java.util.List;
import java.util.Map;

/**
 * All operations on the chain：Add, delete, modify, and check
 * All operations on the chain: Save, delete, update, query
 *
 * @author tangyi
 * @date 2018/11/8
 */
public interface ChainStorage {

    /**
     * Save Chain to Database
     * Save BlockChain to database
     *
     * @param key        Chain ID
     * @param blockChain The BlockChain saved
     * @throws Exception Any error will throw an exception
     */
    void save(int key, BlockChain blockChain) throws Exception;

    /**
     * Update chain information
     * Update BlockChain
     *
     * @param key        Chain ID
     * @param blockChain The BlockChain updated
     * @throws Exception Any error will throw an exception
     */
    void update(int key, BlockChain blockChain) throws Exception;

    /**
     *
     * @param kvs
     * @throws Exception
     */
    void batchUpdate(Map<byte[], byte[]> kvs) throws Exception;

    /**
     * Completely remove the chain from the database
     * Delete BlockChain in database
     *
     * @param key Chain ID
     * @throws Exception Any error will throw an exception
     */
    void delete(int key) throws Exception;


    /**
     * Retrieve chain based on serial number
     * Get the chain according to the ID
     *
     * @param key Chain ID
     * @return BlockChain
     * @throws Exception Any error will throw an exception
     */
    BlockChain load(int key) throws Exception;

    List<BlockChain> loadAllRegChains() throws Exception;
}
