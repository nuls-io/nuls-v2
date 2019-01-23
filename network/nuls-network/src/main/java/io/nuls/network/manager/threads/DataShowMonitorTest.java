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

import io.nuls.network.manager.ConnectionManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.MessageHandlerFactory;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.ProtocolRoleHandler;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * Group event monitor
 * 测试 定时打印连接信息
 * @author  lan
 * @create  2018/11/14
 */
public class DataShowMonitorTest implements Runnable  {
    @Override
    public void run() {
        //test
        printlnCachePeer();
        printlnPeer();
        printlnMem();
        printlnProtocolMap();
    }

    private void printlnProtocolMap(){
        Collection<Map<String,ProtocolRoleHandler>> values =MessageHandlerFactory.getInstance().getProtocolRoleHandlerMap().values();
        for (Map<String,ProtocolRoleHandler> map : values) {
            Collection<ProtocolRoleHandler> list = map.values();
            for (ProtocolRoleHandler protocolRoleHandler : list) {
                Log.debug("protocolRoleHandler =================={}==={}",protocolRoleHandler.getRole(),protocolRoleHandler.getHandler());
            }
        }

    }

    private void printlnCachePeer(){
        List<Node> list= ConnectionManager.getInstance().getCacheAllNodeList();
        Log.info("begin============================printlnCachePeer:"+list.size());
        for(Node node:list){
            Log.info("************cache connect:"+node.getId());
        }
        Log.info("end============================printlnCachePeer:"+list.size());
    }
    private void printlnMem(){
//       byte[] bys = new byte[1024*1024];//申请1M内存
//       Log.debug("Java进程可以向操作系统申请到的最大内存:"+(Runtime.getRuntime().maxMemory())/(1024*1024)+"M");
//       Log.debug("Java进程空闲内存:"+(Runtime.getRuntime().freeMemory())/(1024*1024)+"M");
//       Log.debug("Java进程现在从操作系统那里已经申请了内存:"+(Runtime.getRuntime().totalMemory())/(1024*1024)+"M");
    }
    private void printlnPeer(){

        NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();
        List<NodeGroup> nodeGroupList = nodeGroupManager.getNodeGroups();
        for(NodeGroup nodeGroup:nodeGroupList){
            Log.info("chainId={},magicNumber={},isSelfChain={}",nodeGroup.getChainId(),nodeGroup.getMagicNumber(),nodeGroup.isSelf());
            Collection<Node> c1=nodeGroup.getConnectNodes();
            Log.info("begin============================printlnPeer c1:SelfConnectNodes============="+c1.size());
            for(Node n:c1){
                Log.info("*************connect:{},info:{}",n.getId(),n.getNodeGroupConnectorsInfos());
            }
            Log.info("end============================printlnPeer c1:SelfConnectNodes=============");

            Collection<Node> c2=nodeGroup.getDisConnectNodes();
            Log.info("begin============================printlnPeer c2:SelfDisConnectNodes============="+c2.size());
            for(Node n:c2){
                Log.info("***********disconnect:"+n.getId());
            }
            Log.info("end============================printlnPeer c2:SelfDisConnectNodes=============");

            Collection<Node> c3=nodeGroup.getConnectCrossNodes();
            Log.info("begin============================printlnPeer c3:crossConnectNodes============="+c3.size());
            for(Node n:c3){
                Log.info("************cross connect:"+n.getId());
            }
            Log.info("end============================printlnPeer c3:crossConnectNodes=============");

            Collection<Node> c4=nodeGroup.getDisConnectCrossNodes();
            Log.info("begin============================printlnPeer c4:crossDisConnectNodes============="+c4.size());
            for(Node n:c4){
                Log.info("*************cross disconnect:"+n.getId());
            }
            Log.info("end============================printlnPeer c4:crossDisConnectNodes=============");
        }
    }
}
