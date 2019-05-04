/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.network.task;

import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.BestBlockInfo;
import io.nuls.network.model.message.PeerInfoMessage;
import io.nuls.network.rpc.call.impl.BlockRpcServiceImpl;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.util.List;

/**
 * Group event monitor
 * 测试 定时打印连接信息
 *
 * @author lan
 * @create 2018/11/14
 */
public class LocalInfosSendTask implements Runnable {
    @Override
    public void run() {
        broadcastLocalInfos();
    }

    private void broadcastLocalInfos() {
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
        for (NodeGroup nodeGroup : nodeGroupList) {
                //获取本地高度与hash
                BlockRpcServiceImpl blockRpcServiceImpl = SpringLiteContext.getBean(BlockRpcServiceImpl.class);
                BestBlockInfo bestBlockInfo = blockRpcServiceImpl.getBestBlockHeader(nodeGroup.getChainId());
                if (bestBlockInfo.getBlockHeight() == 0) {
                    continue;
                }
                PeerInfoMessage peerInfoMessage = MessageFactory.getInstance().buildPeerInfoMessage(nodeGroup.getMagicNumber(), bestBlockInfo);
                NetworkEventResult result = MessageManager.getInstance().broadcastToAllNode(peerInfoMessage, null, false, true);
        }
    }
}
