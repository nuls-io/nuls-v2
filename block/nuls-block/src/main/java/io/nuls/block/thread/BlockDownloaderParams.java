/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.thread;

import io.nuls.base.data.NulsDigestData;
import io.nuls.block.model.Node;
import lombok.Data;

import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;

/**
 * 一次区块下载过程中用到的参数
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 上午10:58
 */
@Data
public class BlockDownloaderParams {

    /**
     * 网络最新区块高度
     */
    private long netLatestHeight;
    /**
     * 网络最新区块hash
     */
    private NulsDigestData netLatestHash;
    /**
     * 本地最新区块高度
     */
    private long localLatestHeight;
    /**
     * 本地最新区块hash
     */
    private NulsDigestData localLatestHash;
    /**
     * 网络上一致可用的节点(阻塞，用于正常同步)
     */
    private PriorityBlockingQueue<Node> nodes;
    /**
     * 网络上一致可用的节点(用于失败重试)
     */
    private List<Node> list;
    /**
     * 网络上可用节点数>=nodes.size()
     */
    private int availableNodesCount;

}
