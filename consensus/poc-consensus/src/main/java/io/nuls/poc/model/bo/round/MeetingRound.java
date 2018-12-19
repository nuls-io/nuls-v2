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

import io.nuls.base.data.Address;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.tools.data.DoubleUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.thread.TimeService;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * 轮次信息类
 * Information about rotation
 *
 * @author tag
 * 2018/11/12
 */
public class MeetingRound {
    /**
     * 总权重
     * Total weight
     * */
    private double totalWeight;
    /**
     * 本地打包节点在当前轮次的下标
     * Subscription of Local Packing Node in Current Round
     * */
    private long index;
    /**
     * 当前轮次开始打包时间
     * Current Round Start Packing Time
     * */
    private long startTime;
    /**
     * 当前轮次打包结束时间
     * End time of front packing
     * */
    private long endTime;
    /**
     * 当前轮次打包节点数量
     * Number of Packing Nodes in Current Round
     * */
    private int memberCount;
    /**
     * 当前轮次打包成员列表
     * Current rounds packaged membership list
     * */
    private List<MeetingMember> memberList;
    /**
     * 上一轮轮次信息
     * Last round of information
     * */
    private MeetingRound preRound;
    /**
     * 本地打包成员信息
     * Locally packaged member information
     * */
    private MeetingMember myMember;

    public MeetingRound getPreRound() {
        return preRound;
    }

    public void setPreRound(MeetingRound preRound) {
        this.preRound = preRound;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public int getMemberCount() {
        return memberCount;
    }

    /**
     * 初始化轮次信息
     * Initialization Round Information
     *
     * @param memberList 打包成员信息列表/Packaged Member Information List
     * @param chain      chain info
     * */
    public void init(List<MeetingMember> memberList, Chain chain) {
        assert (startTime > 0L);
        this.memberList = memberList;
        if (null == memberList || memberList.isEmpty()) {
            throw new NulsRuntimeException(ConsensusErrorCode.DATA_ERROR);
        }
        Collections.sort(memberList);
        this.memberCount = memberList.size();
        totalWeight = 0d;
        MeetingMember member = null;
        for (int i = 0; i < memberCount; i++) {
            member = memberList.get(i);
            member.setRoundStartTime(this.getStartTime());
            member.setPackingIndexOfRound(i + 1);
            member.setPackStartTime(startTime + i * chain.getConfig().getPackingInterval());
            member.setPackEndTime(member.getPackStartTime() + chain.getConfig().getPackingInterval());
            /*
            轮次总权重等于所有节点权重之和，节点权重=(保证金+总委托金额)*节点信用值
            Round total weight equals the sum of all node weights, node weight = (margin + total Commission amount)* node credit value
            */
            BigInteger ownTotalWeight = BigInteger.ZERO;
            if(!member.getAgent().getTotalDeposit().equals(BigInteger.ZERO)){
                ownTotalWeight = member.getAgent().getTotalDeposit().add(member.getAgent().getDeposit());
            }
            totalWeight += DoubleUtils.mul(member.getAgent().getCreditVal(),new BigDecimal(ownTotalWeight));
        }
        endTime = startTime + memberCount * chain.getConfig().getPackingInterval();
    }

    public MeetingMember getMember(int order) {
        if (order == 0) {
            throw new NulsRuntimeException(ConsensusErrorCode.DATA_ERROR);
        }
        if (null == memberList || memberList.isEmpty()) {
            throw new NulsRuntimeException(ConsensusErrorCode.DATA_ERROR);
        }
        return this.memberList.get(order - 1);
    }

    public MeetingMember getMember(byte[] address) {
        for (MeetingMember member : memberList) {
            if (Arrays.equals(address, member.getAgent().getPackingAddress())) {
                return member;
            }
        }
        return null;
    }

    /**
    * 根据节点地址获取节点对应的打包信息
    * Get the packing information corresponding to the node according to the address of the node
    */
    public MeetingMember getMemberByAgentAddress(byte[] address) {
        for (MeetingMember member : memberList) {
            if (Arrays.equals(address, member.getAgent().getAgentAddress())) {
                return member;
            }
        }
        return null;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }


    public double getTotalWeight() {
        return totalWeight;
    }

    public List<MeetingMember> getMemberList() {
        return memberList;
    }

    public MeetingMember getMyMember() {
        return myMember;
    }

    public void calcLocalPacker(List<byte[]> localAddressList) {
        for (byte[] address:localAddressList) {
            MeetingMember member = getMember(address);
            if (null != member) {
                myMember = member;
                return;
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (MeetingMember member : this.getMemberList()) {
            str.append(Address.fromHashs(member.getAgent().getPackingAddress()).getBase58());
            str.append(" ,order:" + member.getPackingIndexOfRound());
            str.append(",packTime:" + new Date(member.getPackEndTime()));
            str.append(",creditVal:" + member.getAgent().getCreditVal());
            str.append("\n");
        }
        if (null == this.getPreRound()) {
            return ("round:index:" + this.getIndex() + " , start:" + new Date(this.getStartTime())
                    + ", netTime:(" + new Date(TimeService.currentTimeMillis()).toString() + ") , totalWeight : " + totalWeight + " ,members:\n :" + str);
        } else {
            return ("round:index:" + this.getIndex() + " ,preIndex:" + this.getPreRound().getIndex() + " , start:" + new Date(this.getStartTime())
                    + ", netTime:(" + new Date(TimeService.currentTimeMillis()).toString() + ") , totalWeight : " + totalWeight + "  , members:\n :" + str);
        }
    }
}
