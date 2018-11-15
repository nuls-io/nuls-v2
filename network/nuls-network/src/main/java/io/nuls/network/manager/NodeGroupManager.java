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
package io.nuls.network.manager;

import io.nuls.network.constant.ManagerStatusEnum;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.NodeGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
/**
 * （节点组）管理
 * node group  manager
 * @author lan
 * @date 2018/11/01
 *
 */
public class NodeGroupManager extends BaseManager{
    public static NodeGroupManager getInstance(){
        return nodeGroupManager;
    }


    private static NodeGroupManager nodeGroupManager=new NodeGroupManager();
    StorageManager storageManager=StorageManager.getInstance();
    /**
     * key:chainId
     */
    private static Map<String,NodeGroup> nodeGroupMap=new ConcurrentHashMap<String,NodeGroup>();
    private ManagerStatusEnum status=ManagerStatusEnum.UNINITIALIZED;

    /**
     * key:magicNumber value:chainId
     */
    private static Map<String,String> mgicNumChainIdMap=new ConcurrentHashMap<String,String>();
    private NodeGroupManager(){

    }

    public static Map<String,NodeGroup> getNodeGroupMap(){
        return nodeGroupMap;
    }

    public static Collection<NodeGroup> getNodeGroupCollection(){
        return nodeGroupMap.values();
    }

    public  NodeGroup  getNodeGroupByMagic(long magicNumber){
        String chainId=mgicNumChainIdMap.get(String.valueOf(magicNumber));
        if(null == chainId){
            return null;
        }
        return nodeGroupMap.get(chainId);
    }
    public  NodeGroup  getNodeGroupByChainId(int chainId){
        return nodeGroupMap.get(chainId);
    }

    public List<NodeGroup> getNodeGroups(){
        List<NodeGroup> nodeGroups=new ArrayList<>();
        nodeGroups.addAll(nodeGroupMap.values());
        return nodeGroups;

    }

    public int getChainIdByMagicNum(long magicNum){
        if(null != mgicNumChainIdMap.get(String.valueOf(magicNum))){
            return Integer.valueOf(mgicNumChainIdMap.get(String.valueOf(magicNum)));
        }
        return 0;
    }

    public boolean addNodeGroup(int chainId,NodeGroup nodeGroup){
        nodeGroupMap.put(String.valueOf(chainId),nodeGroup);
        mgicNumChainIdMap.put(String.valueOf(nodeGroup.getMagicNumber()),String.valueOf(chainId));
        return true;
    }
    public boolean removeNodeGroup(int chainId){
        nodeGroupMap.remove(String.valueOf(chainId));
        if(null != mgicNumChainIdMap.get(chainId)){
            mgicNumChainIdMap.remove(mgicNumChainIdMap.get(chainId));
        }
        return true;
    }

    public boolean validMagicNumber(long magicNumber){
        if(null != mgicNumChainIdMap.get(String.valueOf(magicNumber))) {
            return true;
        }
        return false;
    }


    @Override
    public void init() {
        NodeGroupManager nodeGroupManager=NodeGroupManager.getInstance();
        NetworkParam networkParam=NetworkParam.getInstance();
        //获取配置的信息，进行自有网络的nodeGroup配置初始化
        NodeGroup nodeGroup=new NodeGroup(networkParam.getPacketMagic(),networkParam.getChainId(), networkParam.getMaxInCount(),networkParam.getMaxOutCount(),
                0,false);
        if(networkParam.isMoonNode()){
            nodeGroup.setMoonNet(true);
            nodeGroup.setCrossActive(true);
        }
        nodeGroupManager.addNodeGroup(networkParam.getChainId(),nodeGroup);
        //友链跨链部分等待跨链模块的初始化调用，卫星链的跨链group通过数据库进行初始化
        //获取数据库中已有的nodeGroup跨链网络组信息
        List<NodeGroup> list=storageManager.getAllNodeGroupFromDb();
        for(NodeGroup dbNodeGroup:list){
            dbNodeGroup.setMoonNet(true);
            dbNodeGroup.setSelf(false);
            dbNodeGroup.setCrossActive(true);
            nodeGroupManager.addNodeGroup(dbNodeGroup.getChainId(),dbNodeGroup);
        }
    }
    @Override
    public void start() {
        status=ManagerStatusEnum.RUNNING;
    }
}
