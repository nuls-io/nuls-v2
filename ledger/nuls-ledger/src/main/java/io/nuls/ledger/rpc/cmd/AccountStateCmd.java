package io.nuls.ledger.rpc.cmd;

import io.nuls.ledger.db.Repository;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/19.
 */
@Component
public class AccountStateCmd extends BaseCmd {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private AccountStateService accountStateService;

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
        Integer assetId = (Integer) params.get("assetId");
        logger.info("chainId {}", chainId);
        logger.info("address {}", address);

        BigInteger balance = accountStateService.getBalance(address, chainId, assetId);
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
    public Response getNonce(Map params) {
        //TODO.. 验证参数个数和格式
        Integer chainId = (Integer) params.get("chainId");
        String address = (String) params.get("address");
        Integer assetId = (Integer) params.get("assetId");
        long nonce = accountStateService.getNonce(address, chainId, assetId);
        return success(nonce);
    }
}
