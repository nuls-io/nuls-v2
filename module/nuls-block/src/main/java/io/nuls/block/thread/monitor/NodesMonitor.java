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

import io.nuls.block.constant.StatusEnum;
import io.nuls.block.model.ChainContext;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.rpc.call.ConsensusUtil;
import io.nuls.block.rpc.call.NetworkUtil;
import io.nuls.block.rpc.call.TransactionUtil;
import io.nuls.block.thread.BlockSynchronizer;
import io.nuls.core.log.logback.NulsLogger;

import java.util.List;

import static io.nuls.block.constant.Constant.MODULE_WAITING;
import static io.nuls.block.constant.StatusEnum.WAITING;

/**
 * 网络节点数量监控线程
 *
 * @author captain
 * @version 1.0
 * @date 18-11-14 下午3:53
 */
public class NodesMonitor extends BaseMonitor {

    private static final NodesMonitor INSTANCE = new NodesMonitor();

    public static NodesMonitor getInstance() {
        return INSTANCE;
    }

    private NodesMonitor() {
        super(List.of(WAITING));
    }

    @Override
    protected void process(int chainId, ChainContext context, NulsLogger commonLog) {
        ChainParameters parameters = context.getParameters();
        int minNodeAmount = parameters.getMinNodeAmount();
        int size = NetworkUtil.getAvailableNodes(chainId).size();
        if (size < minNodeAmount && StatusEnum.RUNNING.equals(context.getStatus())) {
            commonLog.info("chainId-" + chainId + ", AvailableNodes not enough!");
            ConsensusUtil.notice(chainId, MODULE_WAITING);
            TransactionUtil.notice(chainId, MODULE_WAITING);
            context.setStatus(WAITING);
        }
        if (size >= minNodeAmount && WAITING.equals(context.getStatus())) {
            commonLog.info("chainId-" + chainId + ", AvailableNodes enough!");
            //重新开启区块同步线程
            BlockSynchronizer.syn(chainId);
        }
    }

}