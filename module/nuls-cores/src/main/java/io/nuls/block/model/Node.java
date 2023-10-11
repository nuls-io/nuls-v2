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

package io.nuls.block.model;

import io.nuls.base.data.NulsHash;
import io.nuls.block.constant.NodeEnum;
import io.nuls.block.utils.LoggerUtil;

import java.util.StringJoiner;

/**
 * 节点
 *
 * @author captain
 * @version 1.0
 * @date 18-11-30 下午2:48
 */
public class Node {

    /**
     * ip+port
     */
    private String id;
    /**
     * 最新区块高度
     */
    private long height;
    /**
     * 该节点关联的下载任务区间的起始高度
     */
    private long startHeight;
    /**
     * 该节点关联的下载任务区间的结束高度
     */
    private long endHeight;
    /**
     * 最新区块hash
     */
    private NulsHash hash;
    /**
     * 下载信用值,初始值50
     */
    private int credit = 50;
    /**
     * 累计失败次数
     */
    private int failedCount = 0;
    /**
     * 批量下载任务开始时间
     */
    private long startTime = 0;
    /**
     * 节点状态
     */
    private NodeEnum nodeEnum;

    public long getStartHeight() {
        return startHeight;
    }

    public void setStartHeight(long startHeight) {
        this.startHeight = startHeight;
    }

    public long getEndHeight() {
        return endHeight;
    }

    public void setEndHeight(long endHeight) {
        this.endHeight = endHeight;
    }

    public int getFailedCount() {
        return failedCount;
    }

    public void setFailedCount(int failedCount) {
        this.failedCount = failedCount;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public NodeEnum getNodeEnum() {
        return nodeEnum;
    }

    public void setNodeEnum(NodeEnum nodeEnum) {
        this.nodeEnum = nodeEnum;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public NulsHash getHash() {
        return hash;
    }

    public void setHash(NulsHash hash) {
        this.hash = hash;
    }

    public int getCredit() {
        return credit;
    }

    public void setCredit(int credit) {
        //主动设置的下载信用值不低于10
        this.credit = Math.max(credit, 10);
    }

    /**
     * 根据下载是否成功、下载耗费时间调整信用值
     */
    public synchronized void adjustCredit(boolean success) {
        if (nodeEnum.equals(NodeEnum.TIMEOUT)) {
            return;
        }
        int oldCredit = credit;
        if (success) {
            //下载成功,信用值加10,初始值50,上限为100
            credit = Math.min(100, credit + 10);
        } else {
            //下载失败,信用值降为原值的八分之一,下限为0,从最大信用值100下降到0，需要连续三次
            credit >>= 3;
            failedCount++;
            if (credit == 0 || failedCount > 10) {
                setNodeEnum(NodeEnum.TIMEOUT);
                LoggerUtil.COMMON_LOG.warn("node-{}, credit-{}, failedCount-{}, this node was marked unavailable", id, credit, failedCount);
            }
        }
        if (!success) {
            LoggerUtil.COMMON_LOG.warn("download fail! node-" + id + ",oldCredit-" + oldCredit + ",newCredit-" + credit);
        }
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Node.class.getSimpleName() + "[", "]")
                .add("id='" + id + "'")
                .add("height=" + height)
                .add("hash=" + hash)
                .add("credit=" + credit)
                .add("nodeEnum=" + nodeEnum)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Node node = (Node) o;

        return id != null ? id.equals(node.id) : node.id == null;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
