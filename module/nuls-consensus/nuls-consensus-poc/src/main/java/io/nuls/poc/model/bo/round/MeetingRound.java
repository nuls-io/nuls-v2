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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.DoubleUtils;
import io.nuls.core.model.StringUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * 轮次信息类
 * Information about rotation
 *
 * @author tag
 * 2018/11/12
 */
@ApiModel(name = "轮次信息")
public class MeetingRound {
    /**
     * 总权重
     * Total weight
     * */
    @ApiModelProperty(description = "当前轮次总权重")
    private double totalWeight;
    /**
     * 本地打包节点在当前轮次的下标
     * Subscription of Local Packing Node in Current Round
     * */
    @ApiModelProperty(description = "轮次下标")
    private long index;
    /**
     * 当前轮次开始打包时间
     * Current Round Start Packing Time
     * */
    @ApiModelProperty(description = "轮次开始时间")
    private long startTime;
    /**
     * 当前轮次打包结束时间
     * End time of front packing
     * */
    @ApiModelProperty(description = "轮次结束时间")
    private long endTime;
    /**
     * 当前轮次打包节点数量
     * Number of Packing Nodes in Current Round
     * */
    @ApiModelProperty(description = "本轮次出块节点数")
    private int memberCount;
    /**
     * 当前轮次打包成员列表
     * Current rounds packaged membership list
     * */
    @ApiModelProperty(description = "本轮次出块成员信息", type = @TypeDescriptor(value = List.class, collectionElement = MeetingMember.class))
    private List<MeetingMember> memberList;
    /**
     * 上一轮轮次信息
     * Last round of information
     * */
    @ApiModelProperty(description = "上一轮信息")
    private MeetingRound preRound;
    /**
     * 本地打包成员信息
     * Locally packaged member information
     * */
    @ApiModelProperty(description = "当前节点出块信息")
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
        MeetingMember member;
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

    public MeetingMember getOnlyMember(byte[] address,Chain chain){
        for (MeetingMember member : memberList) {
            if (Arrays.equals(address, member.getAgent().getPackingAddress())) {
                return member;
            }
        }
        return null;
    }

    public MeetingMember getMember(byte[] address,Chain chain) {
        for (MeetingMember member : memberList) {
            if (Arrays.equals(address, member.getAgent().getPackingAddress()) && validAccount(chain, AddressTool.getStringAddressByBytes(member.getAgent().getPackingAddress()))) {
                return member;
            }
        }
        return null;
    }

    private boolean validAccount(Chain chain,String address) {
        try {
            HashMap callResult = CallMethodUtils.accountValid(chain.getConfig().getChainId(), address, chain.getConfig().getPassword());
            String priKey = (String) callResult.get("priKey");
            if (StringUtils.isNotBlank(priKey)){
                return true;
            }
        }catch (Exception e){
            Log.error(e);
        }
        return false;
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

    public void calcLocalPacker(List<byte[]> localAddressList,Chain chain) {
        for (byte[] address:localAddressList) {
            MeetingMember member = getMember(address,chain);
            if (null != member) {
                myMember = member;
                break;
            }
        }
        if(myMember != null && !chain.isPacker()){
            CallMethodUtils.sendState(chain,true);
            chain.setPacker(true);
        }
        if(myMember == null && chain.isPacker()){
            CallMethodUtils.sendState(chain,false);
            chain.setPacker(false);
        }
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (MeetingMember member : this.getMemberList()) {
            str.append(Address.fromHashs(member.getAgent().getPackingAddress()).getBase58());
            str.append(" ,order:" + member.getPackingIndexOfRound());
            str.append(",packTime:" + new Date(member.getPackEndTime() * 1000));
            str.append(",creditVal:" + member.getAgent().getRealCreditVal());
            str.append("\n");
        }
        if (null == this.getPreRound()) {
            return ("round:index:" + this.getIndex() + " , start:" + new Date(this.getStartTime() * 1000)
                    + ", netTime:(" + new Date(NulsDateUtils.getCurrentTimeMillis()).toString() + ") , totalWeight : " + totalWeight + " ,members:\n" + str);
        } else {
            return ("round:index:" + this.getIndex() + " ,preIndex:" + this.getPreRound().getIndex() + " , start:" + new Date(this.getStartTime())
                    + ", netTime:(" + new Date(NulsDateUtils.getCurrentTimeMillis()).toString() + ") , totalWeight : " + totalWeight + "  , members:\n" + str);
        }
    }
}
