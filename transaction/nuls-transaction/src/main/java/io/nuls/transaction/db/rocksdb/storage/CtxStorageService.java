package io.nuls.transaction.db.rocksdb.storage;

import io.nuls.base.data.NulsDigestData;
import io.nuls.transaction.model.bo.CrossTx;

import java.util.List;

/**
 * 其他链发送过来的跨链交易,从开始跨链验证时就进行存储
 * Cross-chain transaction in verification
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
public interface CtxStorageService {

    /**
     * 新增或修改跨链交易数据
     *
     * @param chainId
     * @param ctx
     * @return
     */
    boolean putTx(int chainId, CrossTx ctx);

    /**
     * 批量新增跨链交易数据
     *
     * @param chainId
     * @param ctxList
     * @return
     */
    boolean putTxs(int chainId, List<CrossTx> ctxList);

    /**
     * 删除跨链交易
     *
     * @param chainId
     * @param hash
     * @return
     */
    boolean removeTx(int chainId, NulsDigestData hash);

    /**
     * 根据交易哈希查询跨链交易
     *
     * @param chainId
     * @param hash
     * @return
     */
    CrossTx getTx(int chainId, NulsDigestData hash);

    /**
     * 查询指定链下所有跨链交易
     * Query all cross-chain transactions in the specified chain
     *
     * @param chainId
     * @return
     */
    List<CrossTx> getTxList(int chainId);

}
