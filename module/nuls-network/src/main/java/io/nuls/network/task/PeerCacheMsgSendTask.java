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

import io.nuls.core.constant.BaseConstant;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.message.MessageUtil;
import io.nuls.core.rpc.model.message.Request;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.TimeManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.PeerMessage;
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
            for (NodeGroup nodeGroup : nodeGroupList) {
                int chainId = nodeGroup.getChainId();
                List<PeerMessage> backToQueue = new ArrayList<>();
                while (nodeGroup.getCacheMsgQueue().size() > 0) {
                    PeerMessage peerMessage = nodeGroup.getCacheMsgQueue().getFirst();
                    if (peerMessage.getTryTimes() > NetworkConstant.MAX_CACHE_MSG_TRY_TIME || (TimeManager.currentTimeMillis() - peerMessage.getCreateTime()) > NetworkConstant.MAX_CACHE_MSG_CYCLE_MILL_TIME) {
                        LoggerUtil.logger(chainId).error("chainId = {},cmd={},tryTimes={},createTime={},RPC fail,drop from cache", chainId, peerMessage.getCmd(), peerMessage.getTryTimes(), peerMessage.getCreateTime());
                        continue;
                    }
                    //发送消息
                    List<String> protocolRoles = new ArrayList<>(MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap(peerMessage.getCmd()));
                    for (String role : protocolRoles) {
                        try {
                            Request request = MessageUtil.newRequest(BaseConstant.MSG_PROCESS, peerMessage.toMap(chainId), Constants.BOOLEAN_FALSE, Constants.ZERO, Constants.ZERO);
                            if (ResponseMessageProcessor.requestOnly(role, request).equals("0")) {
                                backToQueue.add(peerMessage);
                            }
                        } catch (Exception e) {
                            LoggerUtil.logger(chainId).error("{}", e);
                        }
                    }
                }
                //轮次后，将未处理的数据返回
                backToQueue.forEach(backMsg -> {
                    backMsg.setTryTimes(backMsg.getTryTimes() + 1);
                    if (backMsg.getTryTimes() > NetworkConstant.MAX_CACHE_MSG_TRY_TIME) {
                        LoggerUtil.logger(chainId).error("chainId = {},cmd={},tryTimes={},tryTimes max,drop from cache", chainId, backMsg.getCmd(), backMsg.getTryTimes());
                    } else if (nodeGroup.getCacheMsgQueue().size() > NetworkConstant.MAX_CACHE_MSG_QUEUE) {
                        LoggerUtil.logger(chainId).error("chainId = {},cmd={},tryTimes={},CacheMsgQueue max,drop from cache", chainId, backMsg.getCmd(), backMsg.getTryTimes());
                    } else {
                        nodeGroup.getCacheMsgQueue().addLast(backMsg);
                    }
                });
            }
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                Log.error(e);
                Log.error("currentThread interrupt!!");
                Thread.currentThread().interrupt();
            }
        }
    }

}
