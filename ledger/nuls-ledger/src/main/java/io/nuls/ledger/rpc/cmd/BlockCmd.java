/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
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
