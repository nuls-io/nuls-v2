package io.nuls.ledger.rpc.api;

import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * the api call transaction module cmd
 * Created by wangkun23 on 2018/11/28.
 */
@Service
public class TxServiceApi {

    final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * get pending state transactions
     *
     * @return
     */
    public List getPendingStateTxs() {
        Map<String, Object> params = new HashMap<>();
        params.put(Constants.VERSION_KEY_STR, "1.0");
        params.put("chainId", 8096);
        params.put("address", "123");
        try {
            Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "lg_getBalance", params);
            logger.info("response {}", response);
        } catch (Exception e) {
            logger.error("", e);
        }
        return Collections.emptyList();
    }
}
