package io.nuls.ledger.validator;

import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * validate Coin Data
 * Created by wangkun23 on 2018/11/22.
 */
@Component
public class CoinDataValidator {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountStateService accountStateService;

    /**
     * 验证coin data
     *
     * @param address
     * @param amount
     * @param nonce
     * @return
     */
    public boolean validate(String address, int chainId, int assetId, BigInteger amount, long nonce) {
        AccountState accountState = accountStateService.getAccountState(address, chainId, assetId);
        if (accountState == null) {
            return false;
        }
        if (accountState.getBalance().compareTo(amount) == -1) {
            logger.info("account {} balance lacked {}", address, amount);
            return false;
        }
        //TODO nonce String hash
        long targetNonce = accountState.getNonce() + 1;
        if (nonce != targetNonce) {
            logger.info("account {} nonce {} incorrect", address, nonce);
            return false;
        }
        return true;
    }
}
