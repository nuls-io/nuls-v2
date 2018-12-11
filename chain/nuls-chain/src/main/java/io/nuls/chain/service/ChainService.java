package io.nuls.chain.service;


import io.nuls.chain.model.dto.BlockChain;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public interface ChainService {


    boolean initChain();


    boolean saveChain(BlockChain blockChain);


    boolean updateChain(BlockChain blockChain);


    boolean delChain(BlockChain blockChain);


    BlockChain getChain(int chainId);


}
