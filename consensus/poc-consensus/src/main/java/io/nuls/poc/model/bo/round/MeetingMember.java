/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.poc.model.bo.round;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.tools.crypto.Sha256Hash;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.parse.SerializeUtils;
import java.util.ArrayList;
import java.util.List;
/**
 * 轮次成员信息类
 * Round Membership Information Class
 *
 * @author tag
 * 2018/11/12
 */
public class MeetingMember implements Comparable<MeetingMember> {
    /**
    * 轮次下标
    * Subscript in order
    * */
    private long roundIndex;
    /**
    * 轮次开始打包时间
    * Round start packing time
    * */
    private long roundStartTime;
    /**
    * 节点在轮次中的下标（第几个出块）
    * Subscription of Nodes in Rounds (Number of Blocks)
    * */
    private int packingIndexOfRound;
    /**
    * 共识节点对象
    * Consensus node object
    * */
    private Agent agent;
    /**
    * 共识节--委托信息列表
    * Consensus Festival - Delegation Information List
    * */
    private List<Deposit> depositList = new ArrayList<>();
    /**
    * 排序值
    * Ranking value
    * */
    private String sortValue;
    /**
    * 开始打包时间
    * Start packing time
    * */
    private long packStartTime;
    /**
    * 打包结束时间
    * end packing time
    * */
    private long packEndTime;

    /**
     * 计算节点打包排序值
     * Computing Packing Sort Value of Nodes
     * */
    public String getSortValue() {
        if (this.sortValue == null) {
            byte[] hash = ByteUtils.concatenate(agent.getPackingAddress(), SerializeUtils.uint64ToByteArray(roundStartTime));
            sortValue = Sha256Hash.twiceOf(hash).toString();
        }
        return sortValue;
    }

    public int getPackingIndexOfRound() {
        return packingIndexOfRound;
    }

    public void setPackingIndexOfRound(int packingIndexOfRound) {
        this.packingIndexOfRound = packingIndexOfRound;
    }

    public long getPackStartTime() {
        return packStartTime;
    }

    public void setPackStartTime(long packStartTime) {
        this.packStartTime = packStartTime;
    }

    public long getPackEndTime() {
        return packEndTime;
    }

    public void setPackEndTime(long packEndTime) {
        this.packEndTime = packEndTime;
    }

    @Override
    public int compareTo(MeetingMember o2) {
        return this.getSortValue().compareTo(o2.getSortValue());
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
    }

    public List<Deposit> getDepositList() {
        return depositList;
    }

    public void setDepositList(List<Deposit> depositList) {
        this.depositList = depositList;
    }

    public void setSortValue(String sortValue) {
        this.sortValue = sortValue;
    }

    public long getRoundStartTime() {
        return roundStartTime;
    }

    public void setRoundStartTime(long roundStartTime) {
        this.roundStartTime = roundStartTime;
    }

    public long getRoundIndex() {
        return roundIndex;
    }

    public void setRoundIndex(long roundIndex) {
        this.roundIndex = roundIndex;
    }
}
