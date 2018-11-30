package io.nuls.ledger.db;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.serializers.AccountStateSerializer;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Service
public class RepositoryImpl implements Repository {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AccountStateSerializer accountStateSerializer;

    /**
     * put accountState to rocksdb
     *
     * @param key
     * @param accountState
     */
    @Override
    public void putAccountState(byte[] key, AccountState accountState) {
        try {
            RocksDBService.put(DataBaseArea.TB_LEDGER_ACCOUNT, key, accountStateSerializer.serialize(accountState));
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
        byte[] stream = RocksDBService.get(DataBaseArea.TB_LEDGER_ACCOUNT, key);
        if (stream == null) {
            return null;
        }
        AccountState state = accountStateSerializer.deserialize(stream);
        return state;
    }
}
