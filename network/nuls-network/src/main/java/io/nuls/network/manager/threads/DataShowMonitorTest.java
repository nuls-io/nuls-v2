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

import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.tools.log.Log;

import java.util.Collection;
import java.util.List;

/**
 * @program: nuls2.0
 * @description: Group event monitor
 * 测试
 * @author: lan
 * @create: 2018/11/14
 **/
public class DataShowMonitorTest implements Runnable  {
    @Override
    public void run() {
        //test
        printlnPeer();
    }

    void printlnPeer(){
        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
        for(NodeGroup nodeGroup:nodeGroupList){
            Collection<Node> c1=nodeGroup.getConnectNodes();
            for(Node n:c1){
                Log.info("*************connect:"+n.getId());
            }
            Collection<Node> c2=nodeGroup.getDisConnectNodes();
            for(Node n:c2){
                Log.info("***********disconnect:"+n.getId());
            }
            Collection<Node> c3=nodeGroup.getConnectCrossNodes();
            for(Node n:c3){
                Log.info("************cross connect:"+n.getId());
            }
            Collection<Node> c4=nodeGroup.getDisConnectCrossNodes();
            for(Node n:c4){
                Log.info("*************cross disconnect:"+n.getId());
            }
        }
    }
}
