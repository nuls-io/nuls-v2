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

import io.nuls.network.model.po.GroupNodeKeys;
import io.nuls.network.model.po.NodeGroupPo;
import io.nuls.network.model.po.NodePo;
import io.nuls.tools.exception.NulsException;

import java.util.List;
import java.util.Map;

/**
 * DbService
 * @author lan
 * @date 2018/11/01
 *
 */
public interface  DbService {
    /**
     * get nodeGroups
     * @return
     */
    List<NodeGroupPo> getAllNodeGroups() throws NulsException;

    /**
     * get nodes
     * @return
     */
    public List<NodePo> getAllNodes() throws NulsException;
    public Map<String,NodePo> getAllNodesMap() throws NulsException;
    /**
     * save node groups
     * @param nodeGroups
     */
    public void saveNodeGroups(List<NodeGroupPo> nodeGroups);

    public void saveNodes(List<NodePo> nodePos);
    public void batchSaveGroupNodeKeys(List<GroupNodeKeys> groupNodeKeysList);
    public void saveGroupNodeKeys(GroupNodeKeys groupNodeKeys);

    public void deleteNode(String nodeId);

    public void deleteGroup(int chainId);

    public void deleteGroupNodeKeys(int chainId);


    public NodeGroupPo getNodeGroupByChainId(int chainId) throws NulsException;
    public GroupNodeKeys getGroupNodeKeysByChainId(int chainId) throws NulsException;



}
