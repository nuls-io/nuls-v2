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
 * @author lan
 *
 */
public interface NetworkConstant {

    /**
     * -----------[netty configs ]------------
     */
    int READ_IDEL_TIME_OUT = 0;
    int WRITE_IDEL_TIME_OUT = 0;
    int ALL_IDEL_TIME_OUT = 100;
    int MAX_FRAME_LENGTH = 10 * 1024 * 1024;
    int CONNETCI_TIME_OUT = 6000;

    /**
     * --------[network configs] -------
     */
    String NETWORK_SECTION = "network";
    String NETWORK_SELF_SERVER_PORT = "network.self.server.port";
    String NETWORK_SELF_MAGIC = "network.self.magic";
    String NETWORK_SELF_NODE_MAX_IN = "network.self.max.in";
    String NETWORK_SELF_NODE_MAX_OUT = "network.self.max.out";
    String NETWORK_SELF_SEED_IP = "network.self.seed.ip";
    String NETWORK_SELF_CHAIN_ID = "network.self.chainId";

    String NETWORK_MOON_NODE = "network.moon.node";
    String NETWORK_CROSS_SERVER_PORT = "network.cross.server.port";
    String NETWORK_CROSS_NODE_MAX_IN = "network.cross.max.in";
    String NETWORK_CROSS_NODE_MAX_OUT = "network.cross.max.out";
    String NETWORK_MOON_SEED_IP = "network.moon.seed.ip";

    String CMD_MESSAGE_VERSION = "version";
    String CMD_MESSAGE_VERACK = "verack";
    String CMD_MESSAGE_ADDR = "addr";
    String CMD_MESSAGE_GET_ADDR = "getaddr";

    String DB_NAME_NETWORK_NODEGROUPS="nwNodeGroups";
    String DB_NAME_NETWORK_NODES="nwNodes";
    String DB_NAME_NETWORK_GROUP_NODESKEYS="nwGroupNodesKeys";

    //特殊字符
    String COMMA=",";
    String COLON=":";
}
