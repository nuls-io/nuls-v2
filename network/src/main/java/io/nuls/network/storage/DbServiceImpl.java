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
package io.nuls.network.storage;

import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.po.GroupNodeKeys;
import io.nuls.network.model.po.NodeGroupPo;
import io.nuls.network.model.po.NodePo;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * DbServiceImpl
 * @author lan
 * @date 2018/11/01
 *
 */
public class DbServiceImpl implements DbService,InitializingBean {
    public static String DEFAULT_ENCODING = "UTF-8";
    @Override
    public List<NodeGroupPo> getAllNodeGroups() throws NulsException {
        List<byte[]>  nodeGroupBytes=RocksDBService.valueList(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS);
        List<NodeGroupPo> list=new ArrayList<>();
        try {
        if(null != nodeGroupBytes && nodeGroupBytes.size()> 0){
            for( byte[] poBytes:nodeGroupBytes){
                NodeGroupPo nodeGroupPo=new NodeGroupPo();
                nodeGroupPo.parse(poBytes,0);
                list.add(nodeGroupPo);
            }
        }
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
        return list;
    }

    @Override
    public List<NodePo> getAllNodes() throws NulsException {
        List<byte[]>  nodeBytes=RocksDBService.valueList(NetworkConstant.DB_NAME_NETWORK_NODES);
        List<NodePo> list=new ArrayList<>();
        try {
            if(null != nodeBytes && nodeBytes.size()> 0){
                for( byte[] poBytes:nodeBytes){
                    NodePo nodePo=new NodePo();
                    nodePo.parse(poBytes,0);
                    list.add(nodePo);
                }
            }
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
        return list;
    }
    @Override
    public Map<String,NodePo> getAllNodesMap() throws NulsException {
        List<Entry<byte[],byte[]>> nodeBytes=RocksDBService.entryList(NetworkConstant.DB_NAME_NETWORK_NODES);
        Map<String,NodePo>  nodeMap=new ConcurrentHashMap<>();
        try {
            if(null != nodeBytes && nodeBytes.size()> 0){
                for( Entry<byte[],byte[]> poBytes:nodeBytes){
                    NodePo nodePo=new NodePo();
                    nodePo.parse(poBytes.getValue(),0);
                    nodeMap.put(nodePo.getId(),nodePo);
                }
            }
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
        return nodeMap;
    }

    @Override
    public void saveNodeGroups(List<NodeGroupPo> nodeGroups) {
        Map<byte[],byte[]> nodeGroupsMap=new HashMap<>();
        try {
        for(NodeGroupPo nodeGroupPo:nodeGroups){
            nodeGroupsMap.put(ByteUtils.intToBytes(nodeGroupPo.getChainId()),nodeGroupPo.serialize());
        }
            RocksDBService.batchPut(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS,nodeGroupsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveNodes(List<NodePo> nodePos) {
        Map<byte[],byte[]> nodeMap=new HashMap<>();
        try {
            for(NodePo nodePo:nodePos){
                nodeMap.put((nodePo.getId().getBytes(DEFAULT_ENCODING)),nodePo.serialize());
            }
            RocksDBService.batchPut(NetworkConstant.DB_NAME_NETWORK_NODES,nodeMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void batchSaveGroupNodeKeys(List<GroupNodeKeys> groupNodeKeysList) {
        Map<byte[],byte[]> groupNodeKeysMap=new HashMap<>();
        try {
            for(GroupNodeKeys groupNodeKeys:groupNodeKeysList){
                groupNodeKeysMap.put(ByteUtils.intToBytes(groupNodeKeys.getChainId()),groupNodeKeys.serialize());
            }
            RocksDBService.batchPut(NetworkConstant.DB_NAME_NETWORK_GROUP_NODESKEYS,groupNodeKeysMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveGroupNodeKeys(GroupNodeKeys groupNodeKeys) {
        try {
            RocksDBService.put(NetworkConstant.DB_NAME_NETWORK_GROUP_NODESKEYS,
                    ByteUtils.intToBytes(groupNodeKeys.getChainId()),groupNodeKeys.serialize());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteNode(String nodeId) {
        try {
            RocksDBService.delete(NetworkConstant.DB_NAME_NETWORK_NODES,
                    nodeId.getBytes(DEFAULT_ENCODING));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteGroup(int chainId) {
        try {
            RocksDBService.delete(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS,
                    String.valueOf(chainId).getBytes(DEFAULT_ENCODING));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteGroupNodeKeys(int chainId) {
        try {
            RocksDBService.delete(NetworkConstant.DB_NAME_NETWORK_GROUP_NODESKEYS,
                    String.valueOf(chainId).getBytes(DEFAULT_ENCODING));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public NodeGroupPo getNodeGroupByChainId(int chainId) throws NulsException {
        byte [] bytes=RocksDBService.get(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS,ByteUtils.intToBytes(chainId));
        NodeGroupPo nodeGroupPo=new NodeGroupPo();
        nodeGroupPo.parse(bytes,0);
        return nodeGroupPo;
    }

    @Override
    public GroupNodeKeys getGroupNodeKeysByChainId(int chainId) throws NulsException {
        byte [] bytes=RocksDBService.get(NetworkConstant.DB_NAME_NETWORK_GROUP_NODESKEYS,ByteUtils.intToBytes(chainId));
        GroupNodeKeys groupNodeKeys=new GroupNodeKeys();
        groupNodeKeys.parse(bytes,0);
        return groupNodeKeys;
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        try {
            RocksDBService.createTable(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS);
            RocksDBService.createTable(NetworkConstant.DB_NAME_NETWORK_NODES);
            RocksDBService.createTable(NetworkConstant.DB_NAME_NETWORK_GROUP_NODESKEYS);
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
    }
}
