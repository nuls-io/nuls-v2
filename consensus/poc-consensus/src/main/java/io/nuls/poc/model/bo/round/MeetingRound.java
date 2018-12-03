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
import io.nuls.poc.utils.manager.ConfigManager;
import io.nuls.tools.data.DoubleUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.thread.TimeService;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author tag
 * 2018/11/12
 */
public class MeetingRound {
    //总权重
    private double totalWeight;
    //本地打包节点在当前轮次的下标
    private long index;
    //当前轮次开始打包时间
    private long startTime;
    //当前轮次打包结束时间
    private long endTime;
    //当前轮次打包节点数量
    private int memberCount;
    //当前轮次打包成员列表
    private List<MeetingMember> memberList;
    //上一轮次信息
    private MeetingRound preRound;
    //本地打包成员
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

    public void init(List<MeetingMember> memberList,int chain_id) {
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
            member.setPackStartTime(startTime + i * ConfigManager.config_map.get(chain_id).getPacking_interval());
            member.setPackEndTime(member.getPackStartTime() + ConfigManager.config_map.get(chain_id).getPacking_interval());
            totalWeight += DoubleUtils.mul(member.getAgent().getCreditVal(),new BigDecimal(member.getAgent().getTotalDeposit().add(member.getAgent().getDeposit())).doubleValue());
        }
        endTime = startTime + memberCount * ConfigManager.config_map.get(chain_id).getPacking_interval();
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
