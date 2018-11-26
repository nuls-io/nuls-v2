package io.nuls.ledger.rpc.cmd;

import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.AccountState;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Component
public class AccountStateCmd extends BaseCmd {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Repository repository;

    /**
     * when account module create new account,then create accountState
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_createAccount",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "test getHeight 1.0")
    public Response createAccount(List params) {
        for (Object param : params) {
            logger.info("param {}", param);
        }
        //TODO.. 验证参数个数和格式
        Integer chainId = (Integer) params.get(0);
        String address = (String) params.get(1);
        AccountState state = repository.createAccount(chainId.shortValue(), address.getBytes());
        return success(state);
    }

    /**
     * get user account balance
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_getBalance",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "test getHeight 1.0")
    public Response getBalance(Map params) {
        //TODO.. 验证参数个数和格式
        Integer chainId = (Integer) params.get("chainId");
        String address = (String) params.get("address");

        logger.info("chainId {}", chainId);
        logger.info("address {}", address);

        long balance = repository.getBalance(address.getBytes());
        return success(balance);
    }

    /**
     * get user account nonce
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_getNonce",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "test getHeight 1.0")
    public Response getNonce(List params) {
        for (Object param : params) {
            logger.info("param {}", param);
        }
        //TODO.. 验证参数个数和格式
        String address = (String) params.get(1);
        long nonce = repository.getNonce(address.getBytes());
        return success(nonce);
    }
}
