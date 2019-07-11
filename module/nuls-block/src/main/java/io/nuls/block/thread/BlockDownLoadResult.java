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

import io.nuls.base.data.Block;
import io.nuls.base.data.NulsHash;
import io.nuls.block.model.Node;

import java.util.List;

/**
 * 一个区块下载线程的下载结果
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 下午4:17
 */
public class BlockDownLoadResult {
    /**
     * 下载区块起始高度
     */
    private long startHeight;
    /**
     * 预计下载区块个数
     */
    private int size;
    /**
     * 区块来源节点
     */
    private Node node;
    /**
     * 标志从node节点批量下载区块是否成功,要全部下载完成才算成功
     */
    private boolean success;
    /**
     * 下载耗时(不精确)
     */
    private long duration;
    /**
     * 对应的请求hash
     */
    private NulsHash messageHash;
    /**
     * 下载到的区块
     */
    private List<Block> blockList;
    /**
     * 批量下载失败时,缺失的区块高度
     */
    private List<Long> missingHeightList;

    BlockDownLoadResult(NulsHash messageHash, long startHeight, int size, Node node, boolean b, long duration, List<Block> blockList, List<Long> missingHeightList) {
        this.messageHash = messageHash;
        this.startHeight = startHeight;
        this.size = size;
        this.node = node;
        this.success = b;
        this.duration = duration;
        this.blockList = blockList;
        this.missingHeightList = missingHeightList;
    }

    public List<Long> getMissingHeightList() {
        return missingHeightList;
    }

    public void setMissingHeightList(List<Long> missingHeightList) {
        this.missingHeightList = missingHeightList;
    }

    public List<Block> getBlockList() {
        return blockList;
    }

    public long getStartHeight() {
        return startHeight;
    }

    public void setStartHeight(long startHeight) {
        this.startHeight = startHeight;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public NulsHash getMessageHash() {
        return messageHash;
    }

    public void setMessageHash(NulsHash messageHash) {
        this.messageHash = messageHash;
    }

    public void setBlockList(List<Block> blockList) {
        this.blockList = blockList;
    }

}
