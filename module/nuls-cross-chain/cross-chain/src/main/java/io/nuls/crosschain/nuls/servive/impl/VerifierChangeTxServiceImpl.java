package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.service.VerifierChangeTxService;

import java.util.List;
import java.util.Map;

/**
 * 验证人变更交易实现类
 *
 * @author tag
 * @date 2019/6/19
 */
@Component
public class VerifierChangeTxServiceImpl implements VerifierChangeTxService {

    @Override
    public List<Transaction> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return null;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return false;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        return false;
    }
}
