package io.nuls.ledger.rpc.cmd;

import io.nuls.ledger.service.LedgerService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
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

    /**
     * get user account balance
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_getBalance", version = 1.0, preCompatible = true)
    public CmdResponse getBalance(List params) {
        for (Object param : params) {
            logger.info("param {}", param);
        }
        //TODO.. 验证参数个数个格式
        String address = (String) params.get(0);
        BigInteger balance = ledgerService.getBalance(address);
        Map<String, String> data = new HashMap<>();
        data.put("result", balance.toString());
        return success(0, "", data);
    }

    /**
     * get user account nonce
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_getNonce", version = 1.0, preCompatible = true)
    public CmdResponse getNonce(List params) {
        for (Object param : params) {
            logger.info("param {}", param);
        }
        Map<String, String> data = new HashMap<>();
        return success(0, "", data);
    }
}
