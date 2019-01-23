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
package io.nuls.network.manager.threads;

import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.NodeGroup;

import java.util.List;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * @description  Group event monitor
 * 判断网络组的连接情况，是否稳定连接
 * @author lan
 * @date 2018/11/14
 **/
public class GroupStatusMonitor  implements Runnable  {
    @Override
    public void run() {
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
        for(NodeGroup nodeGroup:nodeGroupList){
            if(NodeGroup.WAIT1 == nodeGroup.getStatus() || NodeGroup.WAIT2 == nodeGroup.getStatus()){
                if(nodeGroup.isActive()){
                    long time = nodeGroup.getLatestHandshakeSuccTime()+ NetworkConstant.NODEGROUP_NET_STABLE_TIME_MILLIONS;
                    if(time < System.currentTimeMillis()){
                        if(NodeGroup.WAIT1 == nodeGroup.getStatus()){
                            Log.info("ChainId={} NET IS IN INIT",nodeGroup.getChainId());
                            //通知链管理模块
                            //TODO:
                        }
                        //发布网络状态事件
                        nodeGroup.setStatus(NodeGroup.OK);
                        Log.info("ChainId={} NET STATUS UPDATE TO OK",nodeGroup.getChainId());
                    }

                }
            }else if(NodeGroup.OK == nodeGroup.getStatus()){
                if(!nodeGroup.isActive()){
                    //发布网络状态事件
                    nodeGroup.setStatus(NodeGroup.WAIT2);
                    Log.info("ChainId={} NET STATUS UPDATE TO WAITING",nodeGroup.getChainId());
                }
            }
        }
    }

}
