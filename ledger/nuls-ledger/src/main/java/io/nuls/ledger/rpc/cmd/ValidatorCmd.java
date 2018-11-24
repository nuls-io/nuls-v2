package io.nuls.ledger.rpc.cmd;

import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by wangkun23 on 2018/11/22.
 */
@Component
public class ValidatorCmd extends BaseCmd {
    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * validate coin data
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_validateCoinData", version = 1.0, preCompatible = true)
    public CmdResponse validateCoinData(List params) {
        for (Object param : params) {
            logger.info("param {}", param);
        }
        //TODO.. 验证参数个数和格式
        short chainId = (short) params.get(0);
        String address = (String) params.get(1);
        Long amount = (Long) params.get(2);
        Long nonce = (Long) params.get(3);

        //TODO.. validate

        return success("", "hash");
    }

}
