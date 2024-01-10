/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
     * cmd Get network time
     */
    String CMD_NW_CURRENT_TIME = "nw_currentTimeMillis";
    /**
     * Register Network Instruction Protocol
     */
    String CMD_NW_PROTOCOL_REGISTER = "nw_protocolRegister";
    String CMD_NW_PROTOCOL_PRIORITY_REGISTER = "protocolRegisterWithPriority";
    /**
     * Broadcast messages
     */
    String CMD_NW_BROADCAST = "nw_broadcast";

    String CMD_NW_CROSS_RANDOM_BROADCAST = "nw_crossRandomBroadcast";
    /**
     * send message
     */
    String CMD_NW_SEND_PEERS_MSG = "nw_sendPeersMsg";

    /**
     * Obtain network information
     */
    String CMD_NW_INFO = "nw_info";

    /**
     * View network nodes
     */
    String CMD_NW_NODES = "nw_nodes";
    /**
     * Create a network group
     */
    String CMD_NW_CREATE_NODEGROUP = "nw_createNodeGroup";
    /**
     * Activate cross chain network
     */
    String CMD_NW_ACTIVE_CROSS = "nw_activeCross";
    /**
     * Get network group information
     */
    String CMD_NW_GET_GROUP_BY_CHAINID = "nw_getGroupByChainId";
    /**
     * Get connection information
     */
    String CMD_NW_GET_CHAIN_CONNECT_AMOUNT = "nw_getChainConnectAmount";
    /**
     * Delete node group
     */
    String CMD_NW_GET_DELETE_NODEGROUP = "nw_delNodeGroup";
    /**
     * Get Seed Node
     */
    String CMD_NW_GET_SEEDS = "nw_getSeeds";

    String CMD_NW_GET_MAIN_NET_MAGIC_NUMBER = "nw_getMainMagicNumber";
    /**
     * Reconnect
     */
    String CMD_NW_RECONNECT = "nw_reconnect";
    /**
     * Get network group information
     */
    String CMD_NW_GET_GROUPS = "nw_getGroups";
    /**
     * Add connectable node information
     */
    String CMD_NW_ADD_NODES = "nw_addNodes";
    /**
     * Delete node information
     */
    String CMD_NW_DEL_NODES = "nw_delNodes";
    /**
     * Obtain node information
     */
    String CMD_NW_GET_NODES = "nw_getNodes";
    /**
     * Update node information
     */
    String CMD_NW_UPDATE_NODE_INFO = "nw_updateNodeInfo";


    /**
     * Module Unified Message ProcessorRPCinterface
     */
     String CMD_MSG_PROCESS = "msgProcess";
}
