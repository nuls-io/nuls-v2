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

import io.nuls.db.service.RocksDBService;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.po.NodeGroupPo;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DbServiceImpl
 * @author lan
 * @date 2018/11/01
 *
 */
public class DbServiceImpl implements DbService,InitializingBean {
    @Override
    public List<NodeGroup> getAllNodeGroups() {
        return new ArrayList<>();
    }

    @Override
    public List<Node> getNodesByChainId(int chainId) {
        return new ArrayList<>();
    }

    @Override
    public void saveNodeGroups(List<NodeGroup> nodeGroups) {
        Map<byte[],byte[]> nodeGroupsMap=new HashMap<>();
        try {
        for(NodeGroup nodeGroup:nodeGroups){
                nodeGroupsMap.put(ByteUtils.intToBytes(nodeGroup.getChainId()),nodeGroup.parseToPo().serialize());
        }
            RocksDBService.batchPut(NetworkConstant.DB_NAME_NETWORK_NODEGROUP,nodeGroupsMap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public NodeGroupPo getNodeGroupByChainId(int chainId) throws NulsException {
        byte [] bytes=RocksDBService.get(NetworkConstant.DB_NAME_NETWORK_NODEGROUP,ByteUtils.intToBytes(chainId));
        NodeGroupPo nodeGroupPo=new NodeGroupPo();
        nodeGroupPo.parse(bytes,0);
        return nodeGroupPo;
    }

    @Override
    public void saveNodesByChainId(List<Node> nodes, int chainId) {

    }

    @Override
    public void saveAllNodesByNodeId(List<Node> nodes, int chainId) {

    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        try {
            RocksDBService.createTable(NetworkConstant.DB_NAME_NETWORK_NODEGROUP);
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
    }
}
