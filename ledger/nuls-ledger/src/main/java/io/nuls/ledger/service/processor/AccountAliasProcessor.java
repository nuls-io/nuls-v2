package io.nuls.ledger.service.processor;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
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
 * account alias tx
 * <p>
 * no destination address,because money burned
 * <p>
 * Created by wangkun23 on 2018/11/29.
 */
@Service
public class AccountAliasProcessor implements TxProcessor {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AccountStateService accountStateService;

    @Override
    public void process(Transaction transaction) {
        if (transaction.getType() != TransactionType.TX_TYPE_ACCOUNT_ALIAS) {
            logger.error("transaction type:{} is not account alias type.", transaction.getType());
            return;
        }
        byte[] coinDateBytes = transaction.getCoinData();

        CoinData coinData = new CoinData();
        try {
            coinData.parse(new NulsByteBuffer(coinDateBytes));
        } catch (NulsException e) {
            logger.error("coinData parse error", e);
        }
        List<CoinFrom> froms = coinData.getFrom();
        for (CoinFrom from : froms) {
            String address = new String(from.getAddress());
            int chainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            BigInteger amount = from.getAmount();
            accountStateService.increaseNonce(address, chainId, assetId);
            accountStateService.addBalance(address, chainId, assetId, amount.negate());
        }
    }
}
