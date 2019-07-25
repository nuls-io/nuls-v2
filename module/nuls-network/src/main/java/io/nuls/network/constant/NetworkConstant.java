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
 * 配置常量
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
     * 读写都不存在情况下，100秒超时，单位s
     * 60 seconds timeout, unit s when there is no reading or writing
     */
    int ALL_IDLE_TIME_OUT = 60;
    /**
     * 在未正式传递握手业务数据前,允许的最大连接数
     */
    // int MAX_ANONYMOUS_CONNECT_COUNT = 100;
    /**
     * 记录该帧数据长度
     * MAX FRAME LENGTH
     */
    int MAX_FRAME_LENGTH = 10 * 1024 * 1024;
    /**
     * netty 发起连接的超时时间,单位秒
     * netty connect time out,unit s
     */
    int CONNETCI_TIME_OUT = 6000;

    int HIGH_WATER_MARK = 8 * 1024 * 1024;
    int LOW_WATER_MARK = 4 * 1024 * 1024;
    /**
     * 10秒链内网络数量与高度无变更,则认为网络状态已稳定
     * 10 seconds The number and speed of the network in the chain are unchanged, and the network status is considered stable.
     */
    int NODEGROUP_NET_STABLE_TIME_MILLIONS = 10 * 1000;
    /**
     * 广播比例
     */
    int FULL_BROADCAST_PERCENT = 100;
    /**
     * 最少的PEER数量，小于等于这个值，将取消比例广播
     */
    int MIN_PEER_NUMBER = 7;
    /**
     * ========================================
     * --------[RPC CMD] -------
     * ========================================
     * 内部协议指令
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
     * 外部指令
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
