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

import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.po.GroupNodesPo;
import io.nuls.network.model.po.GroupPo;
import io.nuls.network.model.po.NodePo;
import io.nuls.network.model.po.RoleProtocolPo;
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
 *
 * @author lan
 * @date 2018/11/01
 */
public class StorageManager extends BaseManager {
    private static StorageManager storageManager = new StorageManager();
    private static Map<String, NodePo> cacheAllNodes = new ConcurrentHashMap<>();
    private DbService dbService = null;

    private StorageManager() {

    }

    public DbService getDbService() {
        return dbService;
    }

    public static StorageManager getInstance() {
        return storageManager;
    }

    /**
     * getAllNodeGroupFromDb
     *
     * @return List<NodeGroup
                    */
    List<NodeGroup> getAllNodeGroupFromDb() {
        List<NodeGroup> nodeGroups = new ArrayList<>();
        try {
            List<GroupPo> groupPos = dbService.getAllNodeGroups();
            for (GroupPo groupPo : groupPos) {
                nodeGroups.add((NodeGroup) groupPo.parseDto());
            }
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return nodeGroups;
    }

    /**
     * get Nodes
     *
     * @param chainId chainId
     * @return List<Node>
     */
    GroupNodesPo getNodesByChainId(int chainId) {
        try {
            return dbService.getNodesByChainId(chainId);
        } catch (NulsException e) {
            e.printStackTrace();
        }
        return null;
    }

//    public void saveNodes(List<NodePo> list,int chainId) {
//        try {
//          //TODO:
//        } catch (NulsException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 删除 数据库 group信息
     * delGroupNodes
     * @param list list
     * @param chainId chainId
     */
//    public void delGroupNodes(List<String> list,int chainId) {
//        try {
////            GroupNodeKeys groupNodeKeys = dbService.getGroupNodeKeysByChainId(chainId);
////            List<String> groupNodeKeyList = groupNodeKeys.getNodeKeys();
////            for (String nodeId : list) {
////                groupNodeKeyList.remove(nodeId);
////            }
////            dbService.saveGroupNodeKeys(groupNodeKeys);
//        } catch (NulsException e) {
//            e.printStackTrace();
//        }
//    }

    /**
     * 加载所有模块的协议注册信息
     * load ProtocolRegisterInfos form db
     *
     * @return List<RoleProtocolPo>
     */
    public List<RoleProtocolPo> getProtocolRegisterInfos() {
        return dbService.getProtocolRegisterInfos();
    }

    @Override
    public void init() {
        try {
            dbService = SpringLiteContext.getBean(DbServiceImpl.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {

    }
}
