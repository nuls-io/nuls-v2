package io.nuls.crosschain.nuls.srorage;

import io.nuls.base.data.NulsHash;

import java.util.List;

/**
 * 接收到新的跨链交易数据库相关操作
 * New Cross-Chain Transaction Database Related Operations
 *
 * @author  tag
 * 2019/4/16
 * */
public interface ConvertToCtxService {
    /**
     * 保存
     * @param originalHash    接收到的跨链交易Hash
     * @param localHash       本链协议跨链交易Hahs
     * @param chainID         链ID
     * @return                保存成功与否
     * */
    boolean save(NulsHash originalHash, NulsHash localHash, int chainID);

    /**
     * 查询
     * @param originalHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          Hash对应的交易
     * */
    NulsHash get(NulsHash originalHash, int chainID);

    /**
     * 删除
     * @param originalHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          删除成功与否
     * */
    boolean delete(NulsHash originalHash,int chainID);

    /**
     * 查询所有
     * @param chainID   链ID
     * @return          该表所有数据
     * */
    List<NulsHash> getList(int chainID);
}
