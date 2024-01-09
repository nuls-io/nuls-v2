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

import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.TaskManager;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.utils.LoggerUtil;

import java.util.List;

/**
 * @author lan
 * @description Group event monitor
 * 判断网络组的连接情况，是否稳定连接,将网络状态进行事件通告
 * @date 2018/11/14
 **/
public class GroupStatusMonitor implements Runnable {
    @Override
    public void run() {
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
        for (NodeGroup nodeGroup : nodeGroupList) {
            if (nodeGroup.isMoonCrossGroup()) {
                updateStatus(nodeGroup.getCrossNodeContainer(), nodeGroup,true);
            } else if (nodeGroup.isMoonGroup()) {
                updateStatus(nodeGroup.getLocalNetNodeContainer(), nodeGroup,false);
            } else {
                updateStatus(nodeGroup.getLocalNetNodeContainer(), nodeGroup,false);
                updateStatus(nodeGroup.getCrossNodeContainer(), nodeGroup,true);
            }
        }
    }

    private void updateStatus(NodesContainer nodesContainer, NodeGroup nodeGroup,boolean isCross) {
        if (NodeGroup.WAIT1 == nodesContainer.getStatus()) {
            long time = nodesContainer.getLatestHandshakeSuccTime() + NetworkConstant.NODEGROUP_NET_STABLE_TIME_MILLIONS;
            //最近10s没有新的网络连接产生
            if (time < System.currentTimeMillis() && nodesContainer.getAvailableNodes().size() > 0) {
                //通知链管理模块
                //发布网络状态事件
                nodesContainer.setStatus(NodeGroup.WAIT2);
                LoggerUtil.logger(nodeGroup.getChainId()).info("ChainId={} isCross={} NET STATUS UPDATE TO OK", nodeGroup.getChainId(),isCross);
            } else {
//                LoggerUtil.logger().info("ChainId={}  isCross={} NET IS IN INIT", nodeGroup.getChainId(),isCross);
            }
        } else if (NodeGroup.WAIT2 == nodesContainer.getStatus()) {
            if (nodeGroup.isActive(isCross)) {
                nodesContainer.setStatus(NodeGroup.OK);
                //执行分享地址线程
                if(!nodesContainer.isHadShareAddr()) {
                    TaskManager.getInstance().createShareAddressTask(nodeGroup, isCross);
                    nodesContainer.setHadShareAddr(true);
                }
            }
        } else if (NodeGroup.OK == nodesContainer.getStatus()) {
            if (!nodeGroup.isActive(isCross)) {
                //发布网络状态事件
                nodesContainer.setStatus(NodeGroup.WAIT2);
                LoggerUtil.logger(nodeGroup.getChainId()).info("ChainId={},isCross={} NET STATUS UPDATE TO WAITING", nodeGroup.getChainId(),isCross);
            }
        }
    }
}
