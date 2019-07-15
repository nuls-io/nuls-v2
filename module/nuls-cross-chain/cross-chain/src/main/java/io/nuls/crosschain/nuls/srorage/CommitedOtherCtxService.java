package io.nuls.crosschain.nuls.srorage;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;

import java.util.List;

/**
 * 已打包的其他链签名拜占庭通过的交易
 * Other packaged chain signatures Byzantine-approved transactions
 *
 * @author  tag
 * 2019/6/21
 * */
public interface CommitedOtherCtxService {
    /**
     * 保存
     * @param atxHash   友链协议跨链交易Hash
     * @param ctx       主网协议跨链交易
     * @param chainID   链ID
     * @return          保存成功与否
     * */
    boolean save(NulsHash atxHash, Transaction ctx, int chainID);

    /**
     * 查询
     * @param atxHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          Hash对应的交易
     * */
    Transaction get(NulsHash atxHash, int chainID);

    /**
     * 删除
     * @param atxHash   友链协议跨链交易Hash
     * @param chainID   链ID
     * @return          删除成功与否
     * */
    boolean delete(NulsHash atxHash,int chainID);

    /**
     * 查询所有
     * @param chainID   链ID
     * @return          该表所有数据
     * */
    List<Transaction> getList(int chainID);
}
