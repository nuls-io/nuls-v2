package io.nuls.ledger.rpc.cmd;

import io.nuls.ledger.service.LedgerService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Component
public class LedgerCmd extends BaseCmd {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LedgerService ledgerService;

    @CmdAnnotation(cmd = "lg_test", version = 1.0, preCompatible = true)
    public CmdResponse testCmd(List params) {
        for (Object param : params) {
            logger.info("param {}", param);
        }

        Map<String, String> data = new HashMap<>();
        data.put("foo", "foo");
        data.put("bar", "bar");
        return success(0, "", data);
    }
}
