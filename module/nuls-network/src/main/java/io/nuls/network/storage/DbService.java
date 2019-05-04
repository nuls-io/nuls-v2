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

import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.po.GroupNodesPo;
import io.nuls.network.model.po.GroupPo;
import io.nuls.network.model.po.NodePo;
import io.nuls.network.model.po.RoleProtocolPo;
import io.nuls.tools.exception.NulsException;

import java.util.List;
import java.util.Map;

/**
 * DbService
 *
 * @author lan
 * @date 2018/11/01
 */
public interface DbService {
    /**
     * get nodeGroups
     *
     * @return List<NodeGroupPo>
     */
    List<GroupPo> getAllNodeGroups() throws NulsException;

    void saveNodes(NodeGroup nodeGroup);

    GroupNodesPo getNodesByChainId(int chainId) throws NulsException;


    void deleteGroup(int chainId);

    /**
     * @param nodeGroups nodeGroups
     * @description save node groups
     */
    void saveNodeGroups(List<GroupPo> nodeGroups);

    GroupPo getNodeGroupByChainId(int chainId) throws NulsException;


    /**
     * save protocol register info
     */

    void saveOrUpdateProtocolRegisterInfo(RoleProtocolPo roleProtocolPo);

    /**
     * init protocol register info
     */
    List<RoleProtocolPo> getProtocolRegisterInfos();

}
