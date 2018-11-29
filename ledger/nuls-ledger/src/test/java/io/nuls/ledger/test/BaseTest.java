package io.nuls.ledger.test;

import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.config.AppConfig;
import io.nuls.ledger.db.DataBaseArea;
import io.nuls.tools.core.inteceptor.ModularServiceMethodInterceptor;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;
import org.junit.Before;

/**
 * Created by wangkun23 on 2018/11/29.
 */
public class BaseTest {

    @Before
    public void before() {
        try {
            AppConfig.loadModuleConfig();
            RocksDBService.init(AppConfig.moduleConfig.getDatabaseDir());
            if (!RocksDBService.existTable(DataBaseArea.TB_LEDGER_ACCOUNT)) {
                RocksDBService.createTable(DataBaseArea.TB_LEDGER_ACCOUNT);
            }
            SpringLiteContext.init("io.nuls.ledger", new ModularServiceMethodInterceptor());
            TimeService.getInstance().start();
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
