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
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.NodeGroupConnector;
import io.nuls.network.model.po.NodeGroupPo;
import io.nuls.network.model.vo.NodeGroupVo;
import io.nuls.network.storage.DbService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * @program: nuls2.0
 * @description: 远程调用接口
 * @author: lan
 * @create: 2018/11/07
 **/
public class NodeGroupRpc extends BaseCmd {
NodeGroupManager nodeGroupManager=NodeGroupManager.getInstance();
DbService dbService=StorageManager.getInstance().getDbService();
    /**
     * nw_createNodeGroup
     * 创建跨链网络
     */
    @CmdAnnotation(cmd = "nw_createNodeGroup", version = 1.0, preCompatible = true)
    public CmdResponse createNodeGroup(List  params) {
        List<NodeGroupPo> nodeGroupPos=new ArrayList<>();
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        long magicNumber = Long.valueOf(String.valueOf(params.get(1)));
        int maxOut = Integer.valueOf(String.valueOf(params.get(2)));
        int maxIn = Integer.valueOf(String.valueOf(params.get(3)));
        int minAvailableCount = Integer.valueOf(String.valueOf(params.get(4)));
        String seedIps=String.valueOf(params.get(5));
        int isMoonNode=Integer.valueOf(String.valueOf(params.get(6)));
        if(0 == isMoonNode){
          //友链的跨链协议调用
            NodeGroup nodeGroup= nodeGroupManager.getNodeGroupByMagic(magicNumber);
            if(null == nodeGroup){
                Log.info("getNodeGroupByMagic is null");
                return failed(NetworkErrorCode.PARAMETER_ERROR, 1.0, "");
            }
            if(chainId != nodeGroup.getChainId()){
                Log.info("chainId != nodeGroup.getChainId()");
                return failed(NetworkErrorCode.PARAMETER_ERROR, 1.0, "");
            }
            nodeGroup.setMaxCrossIn(maxIn);
            nodeGroup.setMaxCrossOut(maxOut);
            nodeGroup.setMinAvailableCount(minAvailableCount);
            List<String> ipList = new ArrayList<>();
            for (String ip : seedIps.split(",")) {
                ipList.add(ip);
            }
            NetworkParam.getInstance().setMoonSeedIpList(ipList);
            nodeGroup.setCrossActive(true);
            NodeGroupPo po=(NodeGroupPo)nodeGroup.parseToPo();
            nodeGroupPos.add(po);
            //更新存储
        }else{
          //卫星链的跨链协议调用
            if(!NetworkParam.getInstance().isMoonNode()){
                Log.info("MoonNode is false，but param isMoonNode is 1");
                return failed(NetworkErrorCode.PARAMETER_ERROR, 1.0, "");
            }
            NodeGroup nodeGroup= nodeGroupManager.getNodeGroupByMagic(magicNumber);
            if(null != nodeGroup){
                Log.info("getNodeGroupByMagic: nodeGroup  exist");
                return failed(NetworkErrorCode.PARAMETER_ERROR, 1.0, "");
            }

            nodeGroup = new NodeGroup(magicNumber,chainId,maxIn,maxOut,minAvailableCount,true);
            nodeGroup.setSelf(false);
            //存储nodegroup
            nodeGroupPos.add((NodeGroupPo)nodeGroup.parseToPo());
            dbService.saveNodeGroups(nodeGroupPos);
            nodeGroupManager.addNodeGroup(nodeGroup.getChainId(),nodeGroup);
        }
        // 成功
        return success(1.0, "success", "");
    }
    /**
     * nw_getGroupByChainId
     * 查看指定网络组信息
     */
    @CmdAnnotation(cmd = "nw_getGroupByChainId", version = 1.0, preCompatible = true)
    public CmdResponse getGroupByChainId(List  params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        NodeGroup nodeGroup=NodeGroupManager.getInstance().getNodeGroupByChainId(chainId);
        NodeGroupVo  nodeGroupVo=buildNodeGroupVo(nodeGroup);
        return success(1.0, "success",nodeGroupVo );


    }

    private NodeGroupVo buildNodeGroupVo(NodeGroup nodeGroup){
        NodeGroupVo nodeGroupVo=new NodeGroupVo();
        nodeGroupVo.setChainId(nodeGroup.getChainId());
        nodeGroupVo.setMagicNumber(nodeGroup.getMagicNumber());
        NodeGroupConnector nodeGroupConnector=nodeGroup.getHightestNodeGroupInfo();
        if(null != nodeGroupConnector){
            nodeGroupVo.setBlockHash(nodeGroupConnector.getBlockHash());
            nodeGroupVo.setBlockHeight(nodeGroupConnector.getBlockHeight());
        }
        nodeGroupVo.setInCount(nodeGroup.getHadConnectIn());
        nodeGroupVo.setOutCount(nodeGroup.getHadConnectOut());
        nodeGroupVo.setInCrossCount(nodeGroup.getHadCrossConnectIn());
        nodeGroupVo.setOutCrossCount(nodeGroup.getHadCrossConnectOut());
        //网络连接，并能正常使用
        nodeGroupVo.setIsActive(nodeGroup.isActive()? 1 : 0);
        //跨链模块是否可用
        nodeGroupVo.setIsCrossActive(nodeGroup.isCrossActive()? 1 : 0);
        nodeGroupVo.setIsMoonNet(nodeGroup.isMoonNet()? 1 : 0);
        nodeGroupVo.setTotalCount(nodeGroupVo.getInCount()+nodeGroupVo.getOutCount()+nodeGroupVo.getInCrossCount()+nodeGroupVo.getOutCrossCount());
        return nodeGroupVo;
    }
    /**
     * nw_delNodeGroup
     * 注销指定网络组信息
     */
    @CmdAnnotation(cmd = "nw_delNodeGroup", version = 1.0, preCompatible = true)
    public CmdResponse delGroupByChainId(List  params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        dbService.deleteGroup(chainId);
        dbService.deleteGroupNodeKeys(chainId);
        //TODO:删除网络连接
        return success(1.0, "success",null );
    }

    /**
     * nw_getSeeds
     * 查询跨链种子节点
     */
    @CmdAnnotation(cmd = "nw_getSeeds", version = 1.0, preCompatible = true)
    public CmdResponse getCrossSeeds(List  params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        Log.info("chainId:"+chainId);
        List<String> seeds=NetworkParam.getInstance().getMoonSeedIpList();
        if(null == seeds){
            return success(1.0, "success", "");
        }
        StringBuffer seedsStr=new StringBuffer();
        for(String seed:seeds){
            seedsStr.append(seed);
            seedsStr.append(",");
        }
        if(seedsStr.length()> 0) {
            return success(1.0, "success", seedsStr.substring(0, seedsStr.length()));
        }
        return success(1.0, "success", "");
    }




    /**
     * nw_reconnect
     * 重连网络
     */
    @CmdAnnotation(cmd = "nw_reconnect", version = 1.0, preCompatible = true)
    public CmdResponse reconnect(List  params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        Log.info("chainId:"+chainId);
        //TODO：

        return success(1.0, "success", "");
    }

    /**
     * nw_getGroups
     * 重连网络
     */
    @CmdAnnotation(cmd = "nw_getGroups", version = 1.0, preCompatible = true)
    public CmdResponse getGroups(List  params) {
        int chainId = Integer.valueOf(String.valueOf(params.get(0)));
        Log.info("chainId:"+chainId);
        int startPage=Integer.valueOf(String.valueOf(params.get(0)));
        int pageSize=Integer.valueOf(String.valueOf(params.get(1)));
        List<NodeGroup> nodeGroups=nodeGroupManager.getNodeGroups();
        int total=nodeGroups.size();
        List<NodeGroupVo> pageList=new ArrayList<>();
        int currIdx = (startPage > 1 ? (startPage -1) * pageSize : 0);
        for (int i = 0; i < pageSize && i <(total - currIdx); i++){
            NodeGroup nodeGroup= nodeGroups.get(currIdx + i);
            NodeGroupVo  nodeGroupVo=buildNodeGroupVo(nodeGroup);
            pageList.add(nodeGroupVo);
        }
        return success(1.0, "success", pageList);
    }

}
