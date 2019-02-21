/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.rpc;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.block.constant.BlockErrorCode;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.message.HashListMessage;
import io.nuls.block.message.TxGroupMessage;
import io.nuls.block.utils.module.NetworkUtil;
import io.nuls.block.utils.module.TransactionUtil;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.List;
import java.util.Map;

import static io.nuls.block.constant.CommandConstant.GET_TXGROUP_MESSAGE;
import static io.nuls.block.constant.CommandConstant.TXGROUP_MESSAGE;


/**
 * 处理收到的{@link HashListMessage},用于区块的广播与转发
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午4:23
 */
@Component
public class GetTxGroupHandler extends BaseCmd {

    @CmdAnnotation(cmd = GET_TXGROUP_MESSAGE, version = 1.0, scope = Constants.PUBLIC, description = "")
    public Response process(Map map) {
        int chainId = Integer.parseInt(map.get("chainId").toString());
        String nodeId = map.get("nodeId").toString();
        HashListMessage message = new HashListMessage();
        NulsLogger messageLog = ContextManager.getContext(chainId).getMessageLog();
        byte[] decode = HexUtil.decode(map.get("messageBody").toString());
        try {
            message.parse(new NulsByteBuffer(decode));
        } catch (NulsException e) {
            e.printStackTrace();
            messageLog.error(e);
            return failed(BlockErrorCode.PARAMETER_ERROR);
        }

        List<NulsDigestData> hashList = message.getTxHashList();
        messageLog.info("recieve HashListMessage from node-" + nodeId + ", chainId:" + chainId + ", txcount:" + hashList.size() + ", hashList:" + hashList);
        TxGroupMessage request = new TxGroupMessage();
        List<Transaction> transactions = TransactionUtil.getTransactions(chainId, hashList);
        messageLog.info("transactions size:" + transactions.size());
        transactions.forEach(e -> messageLog.info("transaction hash:" + e.getHash()));
        request.setBlockHash(message.getBlockHash());
        request.setTransactions(transactions);
        NetworkUtil.sendToNode(chainId, request, nodeId, TXGROUP_MESSAGE);
        return success();
    }

}
