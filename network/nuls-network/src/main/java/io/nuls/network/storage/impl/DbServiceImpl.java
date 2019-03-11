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
package io.nuls.network.storage.impl;

import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.po.GroupNodesPo;
import io.nuls.network.model.po.GroupPo;
import io.nuls.network.model.po.RoleProtocolPo;
import io.nuls.network.storage.DbService;
import io.nuls.network.storage.InitDB;
import io.nuls.network.utils.LoggerUtil;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.model.ByteUtils;
import io.nuls.tools.exception.NulsException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * DbServiceImpl
 *
 * @author lan
 * @date 2018/11/01
 */
@Service
public class DbServiceImpl implements DbService,InitDB,InitializingBean {
    private static String DEFAULT_ENCODING = "UTF-8";

    @Override
    public List<GroupPo> getAllNodeGroups() throws NulsException {
        List<byte[]> nodeGroupBytes = RocksDBService.valueList(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS);
        List<GroupPo> list = new ArrayList<>();
        try {
            if (null != nodeGroupBytes && nodeGroupBytes.size() > 0) {
                for (byte[] poBytes : nodeGroupBytes) {
                    GroupPo nodeGroupPo = new GroupPo();
                    nodeGroupPo.parse(poBytes, 0);
                    list.add(nodeGroupPo);
                }
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsException(e);
        }
        return list;
    }


    @Override
    public void saveNodes(NodeGroup nodeGroup) {
        int chainId = nodeGroup.getChainId();
        GroupNodesPo groupNodesPo = new GroupNodesPo();
        try {
            groupNodesPo.setCrossNodeContainer(nodeGroup.getCrossNodeContainer().parseToNodesContainerPo());
            groupNodesPo.setSelfNodeContainer(nodeGroup.getLocalNetNodeContainer().parseToNodesContainerPo());
            RocksDBService.put(NetworkConstant.DB_NAME_NETWORK_GROUP_NODES,
                    ByteUtils.intToBytes(chainId), groupNodesPo.serialize());
            LoggerUtil.Log.info("save group={} nodes",nodeGroup.getChainId());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public GroupNodesPo getNodesByChainId(int chainId) throws NulsException {
        byte[] bytes = RocksDBService.get(NetworkConstant.DB_NAME_NETWORK_GROUP_NODES, ByteUtils.intToBytes(chainId));
        GroupNodesPo groupNodesPo = new GroupNodesPo();
        groupNodesPo.parse(bytes, 0);
        return groupNodesPo;
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
    public GroupPo getNodeGroupByChainId(int chainId) throws NulsException {
        byte[] bytes = RocksDBService.get(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS, ByteUtils.intToBytes(chainId));
        GroupPo nodeGroupPo = new GroupPo();
        nodeGroupPo.parse(bytes, 0);
        return nodeGroupPo;
    }


    @Override
    public void saveOrUpdateProtocolRegisterInfo(RoleProtocolPo roleProtocolPo) {
        try {
            RocksDBService.put(NetworkConstant.DB_NAME_NETWORK_PROTOCOL_REGISTER,
                    ByteUtils.toBytes(roleProtocolPo.getRole(), DEFAULT_ENCODING), roleProtocolPo.serialize());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<RoleProtocolPo> getProtocolRegisterInfos() {
        List<Entry<byte[], byte[]>> protocolRegisterBytes = RocksDBService.entryList(NetworkConstant.DB_NAME_NETWORK_PROTOCOL_REGISTER);
        List<RoleProtocolPo> roleProtocolPos = new ArrayList<>();
        try {
            if (null != protocolRegisterBytes && protocolRegisterBytes.size() > 0) {
                for (Entry<byte[], byte[]> poBytes : protocolRegisterBytes) {
                    RoleProtocolPo roleProtocolPo = new RoleProtocolPo();
                    roleProtocolPo.parse(poBytes.getValue(), 0);
                    roleProtocolPos.add(roleProtocolPo);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.error(e.getMessage());
        }
        return roleProtocolPos;
    }

    @Override
    public void saveNodeGroups(List<GroupPo> nodeGroups) {
        Map<byte[], byte[]> nodeGroupsMap = new HashMap<>();
        try {
            for (GroupPo nodeGroupPo : nodeGroups) {
                nodeGroupsMap.put(ByteUtils.intToBytes(nodeGroupPo.getChainId()), nodeGroupPo.serialize());
            }
            RocksDBService.batchPut(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS, nodeGroupsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void afterPropertiesSet() throws NulsException {

    }

    @Override
    public void initTableName() throws NulsException {
        try {
            if (!RocksDBService.existTable(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS)) {
                RocksDBService.createTable(NetworkConstant.DB_NAME_NETWORK_NODEGROUPS);
            }
            if (!RocksDBService.existTable(NetworkConstant.DB_NAME_NETWORK_GROUP_NODES)) {
                RocksDBService.createTable(NetworkConstant.DB_NAME_NETWORK_GROUP_NODES);
            }

            if (!RocksDBService.existTable(NetworkConstant.DB_NAME_NETWORK_PROTOCOL_REGISTER)) {
                RocksDBService.createTable(NetworkConstant.DB_NAME_NETWORK_PROTOCOL_REGISTER);
            }
        } catch (Exception e) {
            Log.error(e.getMessage());
            throw new NulsException(e);
        }
    }
}
