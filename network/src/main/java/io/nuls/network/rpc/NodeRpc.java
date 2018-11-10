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
package io.nuls.network.rpc;

import io.nuls.network.constant.NetworkErrorCode;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.po.NodePo;
import io.nuls.network.model.vo.NodeVo;
import io.nuls.network.storage.DbService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: nuls2.0
 * @description: node rpc
 * @author: lan
 * @create: 2018/11/09
 **/
public class NodeRpc extends BaseCmd {
    NodeGroupManager nodeGroupManager=NodeGroupManager.getInstance();
    DbService dbService=StorageManager.getInstance().getDbService();
    /**
     * nw_addNodes
     * 增加节点
     */
    @CmdAnnotation(cmd = "nw_addNodes", version = 1.0, preCompatible = true)
    public CmdResponse addNodes(List params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        int isCross = Integer.valueOf(String.valueOf(params.get(1)));
        String nodes=String.valueOf(params.get(2));
        Log.info("chainId:"+chainId+"==nodes:"+nodes);
        if( chainId < 0 || StringUtils.isBlank(nodes)){
            return failed(NetworkErrorCode.PARAMETER_ERROR, 1.0, "");
        }
        boolean blCross=false;
        if(1 == isCross){
            blCross=true;
        }
        String []peers = nodes.split(",");
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        List<NodePo> nodePos = new ArrayList<>();
        for(String peer:peers){
            String []ipPort=peer.split(":");
            Node node=new Node(ipPort[0],Integer.valueOf(ipPort[1]),Node.OUT,blCross);
            nodeGroup.addDisConnetNode(node,false);
            nodePos.add((NodePo) node.parseToPo());
        }
        StorageManager.getInstance().saveNodes(nodePos,chainId);
        return success(1.0, "success", "");
    }


    /**
     * nw_delNodes
     * 增加节点
     */
    @CmdAnnotation(cmd = "nw_delNodes", version = 1.0, preCompatible = true)
    public CmdResponse delNodes(List params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        String nodes=String.valueOf(params.get(1));
        Log.info("chainId:"+chainId+"==nodes:"+nodes);
        if( chainId < 0 || StringUtils.isBlank(nodes)){
            return failed(NetworkErrorCode.PARAMETER_ERROR, 1.0, "");
        }
        String []peers = nodes.split(",");
        NodeGroup nodeGroup = nodeGroupManager.getNodeGroupByChainId(chainId);
        List<NodePo> nodePos = new ArrayList<>();
        for(String nodeId:peers){
         //移除 peer
         // TODO:
          Node node =  nodeGroup.getConnectNodeMap().get(nodeId);

          if(null != node){

          }



        }
        return success(1.0, "success", "");
    }
    @CmdAnnotation(cmd = "nw_getNodes", version = 1.0, preCompatible = true)
    public CmdResponse nw_getNodes(List params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        int state = Integer.valueOf(String.valueOf(params.get(1)));
        int isCross = Integer.valueOf(String.valueOf(params.get(2)));
        int startPage = Integer.valueOf(String.valueOf(params.get(3)));
        int pageSize = Integer.valueOf(String.valueOf(params.get(4)));
        Log.info("chainId:"+chainId+"==state:"+state+"==isCross:"+isCross+"==startPage："+startPage+"==pageSize:"+pageSize);
        NodeGroup nodeGroup=NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        List<Node> nodes=new ArrayList<>();
        if(0 == isCross){
            if(0 == state) {
                nodes.addAll(nodeGroup.getConnectNodes());
                nodes.addAll(nodeGroup.getDisConnectNodes());
            }else if(1 == state){
                nodes.addAll(nodeGroup.getConnectNodes());
            }else if(2 == state){
                nodes.addAll(nodeGroup.getDisConnectNodes());
            }
        }else{
            if(0 == state) {
                nodes.addAll(nodeGroup.getConnectCrossNodes());
                nodes.addAll(nodeGroup.getDisConnectCrossNodes());
            }else if(1 == state){
                nodes.addAll(nodeGroup.getConnectCrossNodes());
            }else if(2 == state){
                nodes.addAll(nodeGroup.getDisConnectCrossNodes());
            }

        }
        int total=nodes.size();
        List<NodeVo> pageList=new ArrayList<>();
        int currIdx = (startPage > 1 ? (startPage -1) * pageSize : 0);
        for (int i = 0; i < pageSize && i <(total - currIdx); i++){
            Node node= nodes.get(currIdx + i);
            NodeVo  nodeVo=buildNodeVo(node,nodeGroup.getMagicNumber(),chainId);
            pageList.add(nodeVo);
        }
        return success(1.0, "success", pageList);
    }

    /**
     * nw_updateNodeInfo
     * 更新区块高度与hash
     */
    @CmdAnnotation(cmd = "nw_updateNodeInfo", version = 1.0, preCompatible = true)
    public CmdResponse updateNodeInfo(List params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        String nodeId = String.valueOf(params.get(1));
        long blockHeight = Long.valueOf(String.valueOf(params.get(2)));
        String blockHash=String.valueOf(params.get(3));
        NodeGroup nodeGroup=nodeGroupManager.getNodeGroupByChainId(chainId);
        if(null == nodeGroup){
            return failed(NetworkErrorCode.PARAMETER_ERROR, 1.0, "");
        }
        Node node = nodeGroup.getConnectNodeMap().get(nodeId);
        if(null == node){
            return failed(NetworkErrorCode.PARAMETER_ERROR, 1.0, "");
        }
        NodeGroupConnector nodeGroupConnector=node.getNodeGroupConnector(nodeGroup.getMagicNumber());
        if(null != nodeGroupConnector){
            nodeGroupConnector.setBlockHash(blockHash);
            nodeGroupConnector.setBlockHeight(blockHeight);
        }
        if(blockHeight > nodeGroup.getHightest()){
            nodeGroup.setHightest(blockHeight);
            nodeGroup.setHightestBlockNodeId(node.getId());
        }
        return success(1.0, "success", "");
    }



    private NodeVo buildNodeVo(Node node,long magicNumber,int chainId){
        NodeVo nodeVo=new NodeVo();
        NodeGroupConnector nodeGroupConnector=node.getNodeGroupConnector(magicNumber);
        if(null != nodeGroupConnector){
            nodeVo.setBlockHash(nodeGroupConnector.getBlockHash());
            nodeVo.setBlockHeight(nodeGroupConnector.getBlockHeight());
            nodeVo.setState(nodeGroupConnector.getStatus() == Node.HANDSHAKE ? 1 : 0);
            nodeVo.setTime(nodeGroupConnector.getCreateTime());
        }else{
            nodeVo.setTime(0);
        }
        nodeVo.setChainId(chainId);
        nodeVo.setIp(node.getIp());
        nodeVo.setIsOut(node.getType() == Node.OUT ? 1 : 0);
        nodeVo.setMagicNumber(magicNumber);
        nodeVo.setNodeId(node.getId());
        nodeVo.setPort(node.getRemotePort());
        return nodeVo;
    }
}
