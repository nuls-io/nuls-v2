package io.nuls.chain.service;


import io.nuls.base.data.Transaction;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;

import java.util.List;
import java.util.Map;

/**
 * 关于链的所有操作：增删改查
 * All operations on the chain: Save, delete, update, query
 *
 * @author tangyi
 * @date 2018/11/8
 */
public interface ChainService {


    /**
     * 把Nuls2.0主网默认注册到Nuls2.0上（Nuls2.0主网可以认为是Nuls2.0生态的第一条友链）
     * Register the Nuls2.0 main network to Nuls2.0 by default (Nuls2.0 main network can be considered as the first friend chain of Nurs2.0 ecosystem)
     *
     * @throws Exception Any error will throw an exception
     */
    void initMainChain() throws Exception;

    /**
     * 保存链信息
     * Save chain
     *
     * @param blockChain The BlockChain saved
     * @throws Exception Any error will throw an exception
     */
    void saveChain(BlockChain blockChain) throws Exception;

    /**
     * 更新链信息
     * Update chain
     *
     * @param blockChain The BlockChain updated
     * @throws Exception Any error will throw an exception
     */
    void updateChain(BlockChain blockChain) throws Exception;

    /**
     * 删除链信息
     * Delete chain
     *
     * @param blockChain The BlockChain deleted
     * @throws Exception Any error will throw an exception
     */
    void delChain(BlockChain blockChain) throws Exception;

    /**
     * 根据序号获取链
     * Get the chain according to the ID
     *
     * @param chainId Chain ID
     * @return BlockChain
     * @throws Exception Any error will throw an exception
     */
    BlockChain getChain(int chainId) throws Exception;

    boolean chainExist(int chainId) throws Exception;
    boolean chainExist(int chainId, Map<String,Integer> map) throws Exception;
    /**
     * 注册链
     * Register a new chain
     * @param blockChain The BlockChain saved
     * @param asset The Asset saved
     * @throws Exception Any error will throw an exception
     */
    void registerBlockChain(BlockChain blockChain, Asset asset) throws Exception;

    /**
     * 回滚远程调用通知
     * @param txs
     * @throws Exception
     */
    void rpcBlockChainRollback(List<Transaction> txs) throws Exception;

    /**
     * 销毁链
     * Destroy a exist BlockChain
     *
     * @param blockChain The BlockChain destroyed
     * @return The BlockChain after destroyed
     * @throws Exception Any error will throw an exception
     */
    BlockChain destroyBlockChain(BlockChain blockChain) throws Exception;

}
