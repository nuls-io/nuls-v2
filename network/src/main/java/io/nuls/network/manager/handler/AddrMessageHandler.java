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

package io.nuls.network.manager.handler;

import io.nuls.network.manager.MessageFactory;
import io.nuls.network.manager.MessageManager;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.message.AddrMessage;
import io.nuls.network.model.message.base.BaseMessage;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * address message handler
 * @author lan
 * @date 2018/11/02
 *
 */
public class AddrMessageHandler extends BaseMessageHandler {

    private static AddrMessageHandler instance = new AddrMessageHandler();

    private AddrMessageHandler() {

    }

    public static AddrMessageHandler getInstance() {
        return instance;
    }

    @Override
    public NetworkEventResult recieve(BaseMessage message, String nodeKey,boolean isServer) {
        NodeGroup nodeGroup=NodeGroupManager.getInstance().getNodeGroupByMagic(message.getHeader().getMagicNumber());
        Node node =nodeGroup .getConnectNodeMap().get(nodeKey);
        Log.debug("AddrMessageHandler Recieve:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        //处理
        AddrMessage addrMessage=(AddrMessage)message;
        List<IpAddress> ipAddressList=addrMessage.getMsgBody().getIpAddressList();

        //TODO:判断地址是否本地已经拥有，如果拥有不转发，PEER是跨链网络也不转发
        List<Node> addNodes=new ArrayList<>();
        List<IpAddress> addAddressList=new ArrayList<>();
        if(node.isCrossConnect()){
            //跨链，只存储，不转发
            for(IpAddress ipAddress:ipAddressList){
                String ip=ipAddress.getIp().getHostAddress();
                int port=ipAddress.getPort();
                Log.info("======ip:"+ip+":"+ipAddress.getPort());
                String nodeId=ip+":"+port;
                if( null != nodeGroup.getConnectCrossNodeMap().get(nodeId) && null != nodeGroup.getDisConnectCrossNodeMap().get(nodeId)){
                    //增加存储节点
                    Node crossNode=new Node(ip,port,Node.OUT,true);
                    nodeGroup.addDisConnetNode(crossNode,false);
                }
            }
        }else{
            //自有网络，存储，转发
            for(IpAddress ipAddress:ipAddressList){
                String ip=ipAddress.getIp().getHostAddress();
                int port=ipAddress.getPort();
                Log.info("======ip:"+ip+":"+ipAddress.getPort());
                String nodeId=ip+":"+port;
                if( null != nodeGroup.getConnectCrossNodeMap().get(nodeId) && null != nodeGroup.getDisConnectCrossNodeMap().get(nodeId)){
                    //增加存储节点
                    Node crossNode=new Node(ip,port,Node.OUT,true);
                    nodeGroup.addDisConnetNode(crossNode,false);
                    IpAddress addIpAddress=new IpAddress(ip,port);
                    addAddressList.add(addIpAddress);
                }
            }

            if(addAddressList.size()>0){
                //向自有网络广播
                AddrMessage addrMessagebroadCast=MessageFactory.getInstance().buildAddrMessage(addAddressList,nodeGroup.getMagicNumber());
                MessageManager.getInstance().broadcastAddrToAllNode(addrMessagebroadCast,node,true);
            }
        }
        return new NetworkEventResult(true, null);
    }

    @Override
    public NetworkEventResult send(BaseMessage message, Node node, boolean isServer, boolean asyn) {
        Log.debug("AddrMessageHandler Send:"+(isServer?"Server":"Client")+":"+node.getIp()+":"+node.getRemotePort()+"==CMD=" +message.getHeader().getCommandStr());
        return super.send(message,node,isServer,asyn);
    }
}
