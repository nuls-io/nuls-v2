package io.nuls.poc.tx.v1;

import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.service.impl.RandomSeedService;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.manager.RoundManager;

import java.util.List;
import java.util.Map;

/**
 * CoinBase交易处理器
 * @author tag
 * @date 2019/6/1
 */
@Component("CoinBaseProcessorV1")
public class CoinBaseProcessor implements TransactionProcessor {

    @Autowired
    private RoundManager roundManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private RandomSeedService randomSeedService;

    @Override
    public int getType() {
        return TxType.COIN_BASE;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        return null;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {

        if(blockHeader == null) {
            Log.warn("empty blockHeader");
            return true;
        }
        /*
         * 借用CoinBase交易commit函数保存底层随机数
         */
        try{
            Chain chain = chainManager.getChainMap().get(chainId);
            BlockHeader newestHeader = chain.getNewestHeader();
            if(newestHeader == null) {
                Log.warn("empty newestHeader");
                return true;
            }
            byte[] prePackingAddress = newestHeader.getPackingAddress(chainId);
            randomSeedService.processBlock(chainId, blockHeader, prePackingAddress);
        }catch (Exception e) {
            Log.error("save random seed error.", e);
        }
        return true;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        if(blockHeader == null) {
            return true;
        }
        /*
         * 借用CoinBase交易rollback函数回滚底层随机数
         */
        try {
            randomSeedService.rollbackBlock(chainId, blockHeader);
        } catch (Exception e) {
            Log.error("rollback random seed error.", e);
        }
        return true;
    }
}
