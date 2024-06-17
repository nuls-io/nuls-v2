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

package io.nuls.block.thread.monitor;

import io.nuls.block.model.ChainContext;
import io.nuls.common.ConfigBean;
import io.nuls.block.rpc.call.ConsensusCall;
import io.nuls.block.rpc.call.NetworkCall;
import io.nuls.block.rpc.call.TransactionCall;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.rpc.util.NulsDateUtils;

import static io.nuls.block.constant.Constant.MODULE_WAITING;

/**
 * Block height monitor
 * Start every fixed time interval
 * If it is found that the block height has not been updated,Notify the network module to reset available nodes,And restart the block synchronization thread
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 afternoon3:53
 */
public class NetworkResetMonitor extends BaseMonitor {

    private static final NetworkResetMonitor INSTANCE = new NetworkResetMonitor();

    public static NetworkResetMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger commonLog) {
        ConfigBean parameters = context.getParameters();
        long reset = parameters.getResetTime();
        long time = context.getLatestBlock().getHeader().getTime() * 1000;
        //If(Current timestamp-Latest block timestamp)>Reset network threshold,Notify the network module to reset available nodes
        long currentTime = NulsDateUtils.getCurrentTimeMillis();
        commonLog.debug("chainId-" + chainId + ",currentTime-" + currentTime + ",blockTime-" + time + ",diffrence-" + (currentTime - time));
        if (currentTime - time > reset) {
            commonLog.info("chainId-" + chainId + ",NetworkReset!");
            NetworkCall.resetNetwork(chainId);
            //Restart the block synchronization thread
            ConsensusCall.notice(chainId, MODULE_WAITING);
            TransactionCall.notice(chainId, MODULE_WAITING);
            BlockSynchronizer.syn(chainId);
        }
    }

}
