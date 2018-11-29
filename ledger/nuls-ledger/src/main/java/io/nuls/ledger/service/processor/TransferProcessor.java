package io.nuls.ledger.service.processor;

import io.nuls.base.data.Transaction;
import io.nuls.tools.core.annotation.Component;

/**
 * 转账交易处理
 * Created by wangkun23 on 2018/11/29.
 */
@Component
public class TransferProcessor implements TxProcessor {

    @Override
    public void process(Transaction transaction) {
        //TODO..
        //1 判断是否是 转账交易，不是直接不处理

        //2 获取coinDaData的数据

        //3 增减用户账户的余额

    }
}
