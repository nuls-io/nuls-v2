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
package io.nuls.network.constant;

/**
 * @author lan
 * @description
 * @date 2019/03/10
 **/
public interface CmdConstant {
    /**
     * cmd 获取网络时间
     */
    String CMD_NW_CURRENT_TIME = "nw_currentTimeMillis";
    /**
     * 注册网络指令协议
     */
    String CMD_NW_PROTOCOL_REGISTER = "nw_protocolRegister";
    /**
     * 广播消息
     */
    String CMD_NW_BROADCAST = "nw_broadcast";
    /**
     * 发送消息
     */
    String CMD_NW_SEND_PEERS_MSG = "nw_sendPeersMsg";

    /**
     * 获取网络信息
     */
    String CMD_NW_INFO = "nw_info";

    /**
     * 查看网络节点
     */
    String CMD_NW_NODES = "nw_nodes";
    /**
     * 创建网络组
     */
    String CMD_NW_CREATE_NODEGROUP = "nw_createNodeGroup";
    /**
     * 激活跨链网络
     */
    String CMD_NW_ACTIVE_CROSS = "nw_activeCross";
    /**
     * 获取网络组信息
     */
    String CMD_NW_GET_GROUP_BY_CHAINID = "nw_getGroupByChainId";
    /**
     * 获取连接信息
     */
    String CMD_NW_GET_CHAIN_CONNECT_AMOUNT = "nw_getChainConnectAmount";
    /**
     * 删除节点组
     */
    String CMD_NW_GET_DELETE_NODEGROUP = "nw_delNodeGroup";
    /**
     * 获取种子节点
     */
    String CMD_NW_GET_SEEDS = "nw_getSeeds";

    String CMD_NW_GET_MAIN_NET_MAGIC_NUMBER = "nw_getMainMagicNumber";
    /**
     * 重新连接
     */
    String CMD_NW_RECONNECT = "nw_reconnect";
    /**
     * 获取网络组信息
     */
    String CMD_NW_GET_GROUPS = "nw_getGroups";
    /**
     * 添加可连接节点信息
     */
    String CMD_NW_ADD_NODES = "nw_addNodes";
    /**
     * 删除节点信息
     */
    String CMD_NW_DEL_NODES = "nw_delNodes";
    /**
     * 获取节点信息
     */
    String CMD_NW_GET_NODES = "nw_getNodes";
    /**
     * 更新节点信息
     */
    String CMD_NW_UPDATE_NODE_INFO = "nw_updateNodeInfo";

}
