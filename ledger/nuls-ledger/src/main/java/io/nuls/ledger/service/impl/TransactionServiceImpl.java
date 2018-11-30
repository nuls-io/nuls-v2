package io.nuls.ledger.service.impl;

import io.nuls.ledger.model.Transaction;
import io.nuls.ledger.service.TransactionService;
import io.nuls.tools.core.annotation.Service;

/**
 * Created by wangkun23 on 2018/11/28.
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    /**
     * 已确认交易数据处理
     *
     * @param transaction
     */
    @Override
    public void txProcess(Transaction transaction) {
        //先判断对应的交易类型
        //TX_TYPE_COINBASE  直接累加账户余额
        //TX_TYPE_TRANSFER  减去发送者账户余额，增加to方的余额
        //TX_TYPE_ACCOUNT_ALIAS  //减去发送者账户余额 Burned
        //TX_TYPE_REGISTER_AGENT
        //TX_TYPE_STOP_AGENT
        //TX_TYPE_JOIN_CONSENSUS
        //TX_TYPE_CANCEL_DEPOSIT

        //TX_TYPE_YELLOW_PUNISH
        //TX_TYPE_RED_PUNISH
    }
}
