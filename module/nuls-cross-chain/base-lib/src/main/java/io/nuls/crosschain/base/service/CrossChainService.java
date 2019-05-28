package io.nuls.crosschain.base.service;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;

import java.util.List;
import java.util.Map;

/**
 * 跨链模块服务接口类
 * @author tag
 * @date 2019/4/8
 */
public interface CrossChainService {

    /**
     * 创建跨链交易
     * @param params 创建跨链交易所需参数
     *
     * @return processor result
     * */
    Result createCrossTx(Map<String,Object> params);

    /**
     * 跨链交易验证
     * @param params 跨链交易验证所需参数
     *
     * @return processor result
     * */
    Result validCrossTx(Map<String,Object> params);

    /**
     * 跨链交易提交
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean commitCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 跨链交易回滚
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    boolean rollbackCrossTx(int chainId, List<Transaction> txs, BlockHeader blockHeader);

    /**
     * 跨链交易批量验证
     * @param chainId       chain ID
     * @param txs           cross chain transaction list
     * @param txMap         Consensus Module All Transaction Classification
     * @param blockHeader   block header
     *
     * @return processor result
     * */
    List<Transaction> crossTxBatchValid(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader);

    /**
     * 查询跨链交易在主网的处理结果
     * @param params 跨链交易验证所需参数
     *
     * @return processor result
     * */
    Result getCrossTxState(Map<String,Object> params);

    /**
     * 设置跨链交易类型
     * Setting up cross-chain transaction types
     *
     * @return
     */
    default int getCrossChainTxType() {
        return TxType.CROSS_CHAIN;
    }
}
