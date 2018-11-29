package io.nuls.ledger.db;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.model.AccountState;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Service
public class RepositoryImpl implements Repository {
    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * put accountState to rocksdb
     *
     * @param key
     * @param accountState
     */
    @Override
    public void putAccountState(byte[] key, AccountState accountState) {
        try {
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, key, accountState.serialize());
        } catch (Exception e) {
            logger.error("putAccountState serialize error.", e);
        }
    }

    /**
     * get accountState from rocksdb
     *
     * @param key
     * @return
     */
    @Override
    public AccountState getAccountState(byte[] key) {
        byte[] value = RocksDBService.get(DataBaseArea.TB_LEDGER_ACCOUNT, key);
        if (value == null) {
            return null;
        }
        AccountState state = new AccountState();
        try {
            state.parse(value, 0);
            return state;
        } catch (NulsException e) {
            logger.error("getAccountState serialize error.", e);
        }
        return null;
    }
}
