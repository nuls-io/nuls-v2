package io.nuls.ledger.service.processor;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.constant.TransactionType;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

/**
 * 转账交易处理
 * Created by wangkun23 on 2018/11/29.
 */
@Service
public class TransferProcessor implements TxProcessor {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AccountStateService accountStateService;

    @Override
    public void process(Transaction transaction) {
        //1 判断是否是转账交易，不是直接不处理
        if (transaction.getType() != TransactionType.TX_TYPE_TRANSFER) {
            logger.error("transaction type:{} is not transfer type.", transaction.getType());
            return;
        }
        //TODO..
        //2 获取coinDaData的数据
        byte[] coinDateBytes = transaction.getCoinData();

        CoinData coinData = new CoinData();
        try {
            coinData.parse(new NulsByteBuffer(coinDateBytes));
        } catch (NulsException e) {
            logger.error("coinData parse error", e);
        }
        //3 增减用户账户的余额
        List<CoinFrom> froms = coinData.getFrom();
        for (CoinFrom from : froms) {

        }

        List<CoinTo> tos = coinData.getTo();
        for (CoinTo to : tos) {
            String address = new String(to.getAddress());
            int chainId = to.getAssetsChainId();
            int assetId = to.getAssetsId();
            BigInteger amount = to.getAmount();
            accountStateService.addBalance(address, chainId, assetId, amount);
        }
    }
}
