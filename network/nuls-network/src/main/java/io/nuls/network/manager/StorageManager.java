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

import io.nuls.db.service.RocksDBService;
import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.po.GroupNodeKeys;
import io.nuls.network.model.po.NodeGroupPo;
import io.nuls.network.model.po.NodePo;
import io.nuls.network.storage.DbService;
import io.nuls.network.storage.DbServiceImpl;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 存储管理
 * storage  manager
 * @author lan
 * @date 2018/11/01
 *
 */
public class StorageManager extends BaseManager{
    private static StorageManager storageManager=new StorageManager();
    private static Map<String,NodePo> cacheAllNodes=new ConcurrentHashMap<>();
    DbService dbService=null;
    NetworkParam networkParam = NetworkParam.getInstance();
    private StorageManager(){

    }
    public DbService getDbService(){
        return dbService;
    }
    public static StorageManager getInstance(){
        return storageManager;
    }

    public List<NodeGroup> getAllNodeGroupFromDb(){
        List<NodeGroup> nodeGroups=new ArrayList<>();
        try {
            List<NodeGroupPo> nodeGroupPos=dbService.getAllNodeGroups();
            for(NodeGroupPo nodeGroupPo:nodeGroupPos){
                nodeGroups.add((NodeGroup)nodeGroupPo.parseDto());
            }
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return nodeGroups;
    }
    /**
     * get Nodes
     * @param chainId
     * @return
     */
    List<Node> getNodesByChainId(int chainId){
        List<Node> nodes=new ArrayList<>();
        try {
            if(cacheAllNodes.size() == 0){
                cacheAllNodes=dbService.getAllNodesMap();
            }
            GroupNodeKeys groupNodeKeysList=dbService.getGroupNodeKeysByChainId(chainId);
            if(null != groupNodeKeysList){
                for(String nodeKey:groupNodeKeysList.getNodeKeys()){
                    NodePo nodePo=cacheAllNodes.get(nodeKey);
                    nodes.add((Node)nodePo.parseDto());
                }
            }

        } catch (NulsException e) {
            e.printStackTrace();
        }
        return nodes;
    }

    public void saveNodes(List<NodePo> list,int chainId) {
        try {
            GroupNodeKeys groupNodeKeys = dbService.getGroupNodeKeysByChainId(chainId);
            List<String> groupNodeKeyList = groupNodeKeys.getNodeKeys();
            for (NodePo nodePo : list) {
                groupNodeKeyList.add(nodePo.getId());
            }
            dbService.saveNodes(list);
            dbService.saveGroupNodeKeys(groupNodeKeys);
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }
    public void delGroupNodes(List<String> list,int chainId) {
        try {
            GroupNodeKeys groupNodeKeys = dbService.getGroupNodeKeysByChainId(chainId);
            List<String> groupNodeKeyList = groupNodeKeys.getNodeKeys();
            for (String nodeId : list) {
                groupNodeKeyList.remove(nodeId);
            }
            dbService.saveGroupNodeKeys(groupNodeKeys);
        } catch (NulsException e) {
            e.printStackTrace();
        }
    }
   public void saveNodeGroups(List<NodeGroupPo> nodeGroups){
        dbService.saveNodeGroups(nodeGroups);
   }


    @Override
    public void init() {
        try {

            dbService=SpringLiteContext.getBean(DbServiceImpl.class);
//            ((InitializingBean)dbService).afterPropertiesSet();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {

    }
}
