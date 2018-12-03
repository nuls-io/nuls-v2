package io.nuls.ledger.service.impl;

import io.nuls.base.data.Transaction;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.service.processor.CoinBaseProcessor;
import io.nuls.ledger.service.processor.TransferProcessor;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/28.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    CoinBaseProcessor coinBaseProcessor;


    @Autowired
    TransferProcessor transferProcessor;

    /**
     * 已确认交易数据处理
     *
     * @param transaction
     */
    @Override
    public void txProcess(Transaction transaction) {
        if (transaction == null) {
            return;
        }
        //先判断对应的交易类型
        switch (transaction.getType()) {
            case 1:
                //TX_TYPE_COIN_BASE 直接累加账户余额
                coinBaseProcessor.process(transaction);
                break;
            case 2:
                //TX_TYPE_TRANSFER 减去发送者账户余额，增加to方的余额
                transferProcessor.process(transaction);
                break;
            default:
                logger.info("tx type incorrect: {}", transaction.getType());
        }

        //TX_TYPE_ACCOUNT_ALIAS  //减去发送者账户余额 Burned
        //TX_TYPE_REGISTER_AGENT
        //TX_TYPE_STOP_AGENT
        //TX_TYPE_JOIN_CONSENSUS
        //TX_TYPE_CANCEL_DEPOSIT

        //TX_TYPE_YELLOW_PUNISH
        //TX_TYPE_RED_PUNISH
    }
}
