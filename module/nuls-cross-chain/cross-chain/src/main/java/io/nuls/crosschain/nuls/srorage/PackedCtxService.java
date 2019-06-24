package io.nuls.crosschain.nuls.srorage;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;

import java.util.List;

/**
 * 已打包跨链交易接口类
 * Packaged Cross-Chain Transaction Interface Class
 *
 * @author  tag
 * 2019/6/19
 * */
public interface PackedCtxService {
    /**
     * 保存
     * @param mtxHash   友链协议跨链交易Hash
     * @param ctx       主网协议跨链交易
     * @param chainID   链ID
     * @return          保存成功与否
     * */
    boolean save(NulsHash mtxHash, Transaction ctx, int chainID);

    /**
     * 查询
     * @param mtxHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          Hash对应的交易
     * */
    Transaction get(NulsHash mtxHash, int chainID);

    /**
     * 删除
     * @param mtxHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          删除成功与否
     * */
    boolean delete(NulsHash mtxHash,int chainID);

    /**
     * 查询所有
     * @param chainID   链ID
     * @return          该表所有数据
     * */
    List<Transaction> getList(int chainID);
}
