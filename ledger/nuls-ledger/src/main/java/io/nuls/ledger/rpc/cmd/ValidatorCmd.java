package io.nuls.ledger.rpc.cmd;

import io.nuls.ledger.validator.CoinDataValidator;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/22.
 */
@Component
public class ValidatorCmd extends BaseCmd {
    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    CoinDataValidator coinDataValidator;

    /**
     * validate coin data
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_validateCoinData",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0,
            description = "test getHeight 1.0")
    public Response validateCoinData(Map params) {
        //TODO.. 验证参数个数和格式
        String address = (String) params.get("address");
        Integer chainId = (Integer) params.get("chainId");
        Integer assetId = (Integer) params.get("assetId");
        BigInteger amount = (BigInteger) params.get("amount");
        Integer nonce = (Integer) params.get("nonce");
        Boolean result = coinDataValidator.validate(address, chainId, assetId, amount, nonce);
        return success(result);
    }

}
