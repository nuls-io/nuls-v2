package io.nuls.chain.service;


import io.nuls.base.data.Transaction;
import io.nuls.chain.model.po.Asset;
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
public interface ChainService {
    /**
     * @param blockChain
     */
    void addChainMapInfo(BlockChain blockChain);

    /**
     * @param magicNumber
     * @return
     */
    boolean hadExistMagicNumber(long magicNumber);

    /**
     * @param chainName
     * @return
     */
    boolean hadExistChainName(String chainName);


    void initRegChainDatas(long mainNetMagicNumber) throws Exception;

    /**
     * holdNuls2.0The main network is registered by default toNuls2.0upper（Nuls2.0The main network can be considered asNuls2.0The first friendly chain of ecology）
     * Register the Nuls2.0 main network to Nuls2.0 by default (Nuls2.0 main network can be considered as the first friend chain of Nurs2.0 ecosystem)
     *
     * @throws Exception Any error will throw an exception
     */
    void initMainChain() throws Exception;

    /**
     * Save Chain Information
     * Save chain
     *
     * @param blockChain The BlockChain saved
     * @throws Exception Any error will throw an exception
     */
    void saveChain(BlockChain blockChain) throws Exception;

    /**
     * Update chain information
     * Update chain
     *
     * @param blockChain The BlockChain updated
     * @throws Exception Any error will throw an exception
     */
    void updateChain(BlockChain blockChain) throws Exception;

    /**
     * @param assetMap
     * @throws Exception
     */
    void batchUpdateChain(Map<String, BlockChain> assetMap) throws Exception;

    /**
     * Delete Chain Information
     * Delete chain
     *
     * @param blockChain The BlockChain deleted
     * @throws Exception Any error will throw an exception
     */
    void delChain(BlockChain blockChain) throws Exception;

    /**
     * Retrieve chain based on serial number
     * Get the chain according to the ID
     *
     * @param chainId Chain ID
     * @return BlockChain
     * @throws Exception Any error will throw an exception
     */
    BlockChain getChain(int chainId) throws Exception;

    boolean chainExist(int chainId) throws Exception;

    /**
     * Registration Chain
     * Register a new chain
     *
     * @param blockChain The BlockChain saved
     * @param asset      The Asset saved
     * @throws Exception Any error will throw an exception
     */
    void registerBlockChain(BlockChain blockChain, Asset asset) throws Exception;

    /**
     * Rollback remote call notification
     *
     * @param txs
     * @throws Exception
     */
    void rpcBlockChainRollback(List<Transaction> txs, long time) throws Exception;
    void rpcBlockChainRollbackV4(List<Transaction> txs, long time) throws Exception;
    /**
     * Destruction chain
     * Destroy a exist BlockChain
     *
     * @param blockChain The BlockChain destroyed
     * @return The BlockChain after destroyed
     * @throws Exception Any error will throw an exception
     */
    BlockChain destroyBlockChain(BlockChain blockChain) throws Exception;

    List<BlockChain> getBlockList() throws Exception;

    /**
     *
     * @param blockChain
     * @return
     */
    Map<String,Object> getBlockAssetsInfo(BlockChain blockChain) throws Exception;
    /**
     *
     * @param blockChain
     * @return
     */
    Map<String,Object> getChainAssetsSimpleInfo(BlockChain blockChain) throws Exception;

}
