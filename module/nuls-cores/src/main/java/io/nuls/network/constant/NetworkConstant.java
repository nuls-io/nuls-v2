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
 * Configure Constants
 *
 * @author lan
 */
public interface NetworkConstant {


    /**
     * ========================================
     * -----------[netty configs ] -------
     * ========================================
     */

    int READ_IDEL_TIME_OUT = 0;
    int WRITE_IDEL_TIME_OUT = 0;
    /**
     * In the absence of both reading and writing,60Second timeout, in unitss
     * 60 seconds timeout, unit s when there is no reading or writing
     */
    int ALL_IDLE_TIME_OUT = 60;
    /**
     * Before formally transmitting handshake business data,Maximum allowed connections
     */
    // int MAX_ANONYMOUS_CONNECT_COUNT = 100;
    /**
     * Record the data length of this frame
     * MAX FRAME LENGTH
     */
    int MAX_FRAME_LENGTH = 10 * 1024 * 1024;
    /**
     * netty The timeout for initiating a connection,Unit seconds
     * netty connect time out,unit s
     */
    int CONNETCI_TIME_OUT = 6000;

    int HIGH_WATER_MARK = 8 * 1024 * 1024;
    int LOW_WATER_MARK = 4 * 1024 * 1024;

    int MAX_SAME_IP_PER_GROUP = 10;

    /**
     * 10There is no change in the number and height of networks within the second chain,It is considered that the network status has stabilized
     * 10 seconds The number and speed of the network in the chain are unchanged, and the network status is considered stable.
     */
    int NODEGROUP_NET_STABLE_TIME_MILLIONS = 10 * 1000;
    /**
     * Broadcasting ratio
     */
    int FULL_BROADCAST_PERCENT = 100;
    /**
     * MinimalPEERQuantity, less than or equal to this value, proportional broadcasting will be cancelled,according to100%Node broadcasting
     */
    int BROADCAST_MIN_PEER_NUMBER = 7;
    /**
     * ========================================
     * --------[RPC CMD] -------
     * ========================================
     * Internal protocol instructions
     * Internal protocol directive
     */
    String CMD_MESSAGE_VERSION = "version";
    String CMD_MESSAGE_VERACK = "verAck";
    String CMD_MESSAGE_ADDR = "addr";
    String CMD_MESSAGE_GET_ADDR = "getAddr";
    String CMD_MESSAGE_BYE = "bye";
    String CMD_MESSAGE_GET_TIME = "getTime";
    String CMD_MESSAGE_RESPONSE_TIME = "responseTime";
    String CMD_MESSAGE_SEND_LOCAL_INFOS = "peerInfos";
    String CMD_MESSAGE_PING = "ping";
    String CMD_MESSAGE_PONG = "pong";
    /**
     * ========================================
     * --------[RPC CMD] -------
     * ========================================
     * External instructions
     * External instruction
     */
    String CMD_BL_BEST_BLOCK_HEADER = "latestBlockHeader";


    /**
     * --------[DB tables] -------
     */
    String DB_NAME_NETWORK_NODEGROUPS = "nw_groups";
    //    String DB_NAME_NETWORK_NODES="nwNodes";
    String DB_NAME_NETWORK_GROUP_NODES = "nw_group_nodes";
    String DB_NAME_NETWORK_PROTOCOL_REGISTER = "nw_protocol_register";
    /**
     * --------[Special Splitter] -------
     */
    String COMMA = ",";
    String COLON = ":";
    String DOWN_LINE = "_";


    long MAX_NUMBER_4_BYTE = 4294967295L;
    int MAX_NUMBER_2_BYTE = 65535;

    int MAX_CACHE_MSG_QUEUE = 100;
    int INIT_CACHE_MSG_QUEUE_NUMBER = 110;

    int MAX_CACHE_MSG_CYCLE_MILL_TIME = 5000;
    int MAX_CACHE_MSG_TRY_TIME = 5;
}
