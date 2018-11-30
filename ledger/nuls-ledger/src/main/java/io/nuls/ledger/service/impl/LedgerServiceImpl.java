package io.nuls.ledger.service.impl;

import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.service.LedgerService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Service
public class LedgerServiceImpl implements LedgerService {

    @Autowired
    private Repository repository;


    /**
     * get user account balance
     *
     * @param address
     * @return
     */
    @Override
    public BigInteger getBalance(String address) {
        return BigInteger.ONE;
    }

}
