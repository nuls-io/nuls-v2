/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

import io.netty.buffer.Unpooled;
import io.nuls.core.log.Log;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.PeerCacheMessage;
import io.nuls.network.utils.LoggerUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 处理转发,未能及时处理的peer消息
 *
 * @author lanjinsheng
 * @date 2019-07-16
 */
public class PeerCacheMsgSendTask implements Runnable {
    @Override
    public void run() {
        while (true) {
            NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
            List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
            int count = 0;
            for (NodeGroup nodeGroup : nodeGroupList) {
                int chainId = nodeGroup.getChainId();
                List<Node> nodeList = nodeGroup.getAvailableNodes(false);
                for (Node node : nodeList) {
                    List<PeerCacheMessage> backList = new ArrayList<>();
                    try {
                        int dealCount = 0;
                        while (node.getCacheSendMsgQueue().size() > 0) {
                            PeerCacheMessage peerCacheMessage = node.getCacheSendMsgQueue().takeFirst();
                            if ((TimeManager.currentTimeMillis() - peerCacheMessage.getCreateTime()) > NetworkConstant.MAX_CACHE_MSG_CYCLE_MILL_TIME) {
                                LoggerUtil.logger(chainId).error("nodeId={},createTime={},peer cache send fail,drop from cache", node.getId(), peerCacheMessage.getCreateTime());
                                continue;
                            }
                            dealCount++;
                            if (node.getChannel().isWritable()) {
                                node.getChannel().writeAndFlush(Unpooled.wrappedBuffer(peerCacheMessage.getMessage()));
                            } else {
                                backList.add(peerCacheMessage);
                                count++;
                            }
                            /**
                             * 如果超过10个，让时间给其他节点处理
                             */
                            if (dealCount > 10) {
                                break;
                            }
                        }
                        node.getCacheSendMsgQueue().addAll(backList);
                    } catch (Exception e) {
                        LoggerUtil.logger(chainId).error(e);
                    }
                }
            }
            try {
                if (count == 0) {
                    Thread.sleep(200L);
                } else {
                    LoggerUtil.COMMON_LOG.debug("cache count={}", count);
                    Thread.sleep(20L);
                }

            } catch (InterruptedException e) {
                Log.error(e);
                Log.error("currentThread interrupt!!");
                Thread.currentThread().interrupt();
            }
        }
    }
}
