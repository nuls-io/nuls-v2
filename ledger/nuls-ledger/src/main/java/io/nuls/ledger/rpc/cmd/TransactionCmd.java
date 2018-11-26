package io.nuls.ledger.rpc.cmd;

import io.nuls.ledger.service.LedgerService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by wangkun23 on 2018/11/20.
 */
@Component
public class TransactionCmd extends BaseCmd {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private LedgerService ledgerService;

    /**
     * save pendingState transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_saveUnConfirmTx",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "test getHeight 1.0")
    public Object saveUnConfirmTx(List params) {
        for (Object param : params) {
            logger.info("param {}", param);
        }
        //TODO.. 验证参数个数和格式
        return success("hash");
    }

    /**
     * delete pendingState transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_deleteUnConfirmTx",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "test getHeight 1.0")
    public Response deleteTransaction(List params) {
        return success("hash");
    }
}
