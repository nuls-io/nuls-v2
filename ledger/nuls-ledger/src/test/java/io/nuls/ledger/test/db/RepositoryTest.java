package io.nuls.ledger.test.db;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.config.AppConfig;
import io.nuls.ledger.db.DataBaseArea;
import io.nuls.ledger.db.Repository;
import io.nuls.ledger.db.RepositoryImpl;
import io.nuls.ledger.model.AccountState;
import io.nuls.tools.log.Log;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by wangkun23 on 2018/11/21.
 */
public class RepositoryTest {

    final Logger logger = LoggerFactory.getLogger(getClass());

    private Repository repository;

    @Before
    public void before() {
        try {
            AppConfig.loadModuleConfig();
            RocksDBService.init(AppConfig.moduleConfig.getDatabaseDir());
            if (!RocksDBService.existTable(DataBaseArea.TB_LEDGER_ACCOUNT)) {
                RocksDBService.createTable(DataBaseArea.TB_LEDGER_ACCOUNT);
            }
        } catch (Exception e) {
            Log.error(e);
        }
        repository = new RepositoryImpl();
    }

    @Test
    public void createAccount() {
        short chainId = 1;
        String address = "Nse1EUja45SVamtwwmx9cusFhmyyYmtG";
        AccountState state = repository.createAccount(chainId, address.getBytes());
        logger.info("state {}", state);
    }

    @Test
    public void increaseNonce() {
        String address = "Nse1EUja45SVamtwwmx9cusFhmyyYmtG";
        Long nonce = repository.increaseNonce(address.getBytes());
        logger.info("nonce {}", nonce);
    }

    @Test
    public void getNonce() {
        String address = "Nse1EUja45SVamtwwmx9cusFhmyyYmtG";
        Long nonce = repository.getNonce(address.getBytes());
        logger.info("nonce {}", nonce);
    }


    @Test
    public void addBalance() {
        String address = "Nse1EUja45SVamtwwmx9cusFhmyyYmtG";
        for (int i = 0; i < 100; i++) {
            long balance = 100L;
            balance = repository.addBalance(address.getBytes(), balance);
            logger.info("balance {}", balance);
        }
    }

    @Test
    public void getBalance() {
        String address = "Nse1EUja45SVamtwwmx9cusFhmyyYmtG";
        long balance = repository.getBalance(address.getBytes());
        logger.info("balance {}", balance);
    }
}
