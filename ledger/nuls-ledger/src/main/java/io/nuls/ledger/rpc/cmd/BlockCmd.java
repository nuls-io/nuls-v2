package io.nuls.ledger.rpc.cmd;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockHeader;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by wangkun23 on 2018/12/4.
 */
@Component
public class BlockCmd extends BaseCmd {

    final Logger logger = LoggerFactory.getLogger(getClass());


    /**
     * sync block header
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "lg_block_sync",
            version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "test lg_block_sync 1.0")
    public Response blockSync(Map params) {
        String blockHeaderHex = (String) params.get("value");
        if (StringUtils.isNotBlank(blockHeaderHex)) {
            return failed("blockHeaderHex not blank");
        }
        byte[] blockHeaderStream = HexUtil.decode(blockHeaderHex);
        BlockHeader blockHeader = new BlockHeader();
        try {
            blockHeader.parse(new NulsByteBuffer(blockHeaderStream));
        } catch (NulsException e) {
            logger.error("blockHeader parse error", e);
        }

        return success();
    }
}
