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

package io.nuls.poc.model.dto.output;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.BigIntegerUtils;


/**
 * 节点信息类
 * Node information class
 *
 * @author tag
 * 2018/11/20
 */
@ApiModel(name = "节点信息详情")
public class AgentDTO {
    @ApiModelProperty(description = "节点HASH")
    private String agentHash;
    @ApiModelProperty(description = "节点地址")
    private String agentAddress;
    @ApiModelProperty(description = "节点出块地址")
    private String packingAddress;
    @ApiModelProperty(description = "节点奖励地址")
    private String rewardAddress;
    @ApiModelProperty(description = "抵押金额")
    private String deposit;
    @ApiModelProperty(description = "佣金比例")
    private byte commissionRate;
    @ApiModelProperty(description = "节点名称")
    private String agentName;
    @ApiModelProperty(description = "节点ID")
    private String agentId;
    @JsonIgnore
    @ApiModelProperty(description = "节点简介")
    private String introduction;
    @ApiModelProperty(description = "节点创建时间")
    private long time;
    @ApiModelProperty(description = "节点打包高度")
    private long blockHeight;
    @ApiModelProperty(description = "节点失效高度")
    private long delHeight;
    @ApiModelProperty(description = "状态")
    private int status;
    @ApiModelProperty(description = "信誉值")
    private double creditVal;
    @ApiModelProperty(description = "总委托金额")
    private String totalDeposit;
    @ApiModelProperty(description = "创建节点交易HASH")
    private String txHash;
    @ApiModelProperty(description = "委托人数")
    private final int memberCount;
    @ApiModelProperty(description = "版本")
    private String version;
    public AgentDTO(Agent agent) {
        this.agentHash = agent.getTxHash().toHex();
        this.agentAddress = AddressTool.getStringAddressByBytes(agent.getAgentAddress());
        this.packingAddress = AddressTool.getStringAddressByBytes(agent.getPackingAddress());
        this.rewardAddress = AddressTool.getStringAddressByBytes(agent.getRewardAddress());
        this.deposit = BigIntegerUtils.bigIntegerToString(agent.getDeposit());
        this.commissionRate = agent.getCommissionRate();
        this.agentName = agent.getAlais();
        this.agentId = SpringLiteContext.getBean(AgentManager.class).getAgentId(agent.getTxHash());
        this.time = agent.getTime();
        this.blockHeight = agent.getBlockHeight();
        this.delHeight = agent.getDelHeight();
        this.status = agent.getStatus();
        this.creditVal = agent.getRealCreditVal();
        this.totalDeposit = String.valueOf(agent.getTotalDeposit());
        this.txHash = agent.getTxHash().toHex();
        this.memberCount = agent.getMemberCount();
    }
    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }

    public String getPackingAddress() {
        return packingAddress;
    }

    public void setPackingAddress(String packingAddress) {
        this.packingAddress = packingAddress;
    }

    public String getRewardAddress() {
        return rewardAddress;
    }

    public void setRewardAddress(String rewardAddress) {
        this.rewardAddress = rewardAddress;
    }

    public byte getCommissionRate() {
        return commissionRate;
    }

    public void setCommissionRate(byte commissionRate) {
        this.commissionRate = commissionRate;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(long delHeight) {
        this.delHeight = delHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public double getCreditVal() {
        return creditVal;
    }

    public void setCreditVal(double creditVal) {
        this.creditVal = creditVal;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(String totalDeposit) {
        this.totalDeposit = totalDeposit;
    }
}
