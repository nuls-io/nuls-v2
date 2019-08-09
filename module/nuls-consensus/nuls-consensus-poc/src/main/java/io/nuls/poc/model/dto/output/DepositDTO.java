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


import io.nuls.base.basic.AddressTool;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.model.BigIntegerUtils;

/**
 * 共识信息类
 * Consensus information class
 *
 * @author tag
 * 2018/11/20
 */
@ApiModel(name = "委托信息")
public class DepositDTO {
    @ApiModelProperty(description = "委托金额")
    private String deposit;
    @ApiModelProperty(description = "节点HASH")
    private String agentHash;
    @ApiModelProperty(description = "账户地址")
    private String address;
    @ApiModelProperty(description = "委托时间")
    private Long time;
    @ApiModelProperty(description = "委托交易HASH")
    private String txHash;
    @ApiModelProperty(description = "委托交易被打包高度")
    private Long blockHeight;
    @ApiModelProperty(description = "退出委托高度")
    private Long delHeight;

    /**
     * 0:待共识, 1:已共识
     */
    @ApiModelProperty(description = "节点状态 0:待共识, 1:已共识")
    private int status;
    @ApiModelProperty(description = "节点名称")
    private String agentName;
    @ApiModelProperty(description = "节点地址")
    private String agentAddress;

    public DepositDTO(Deposit deposit) {
        this.deposit = BigIntegerUtils.bigIntegerToString(deposit.getDeposit());
        this.agentHash = deposit.getAgentHash().toHex();
        this.address = AddressTool.getStringAddressByBytes(deposit.getAddress());
        this.time = deposit.getTime();
        this.txHash = deposit.getTxHash().toHex();
        this.blockHeight = deposit.getBlockHeight();
        this.delHeight = deposit.getDelHeight();
        this.status = deposit.getStatus();
    }

    public DepositDTO(Deposit deposit, Agent agent) {
        this(deposit);
        if (agent != null) {
            this.agentAddress = AddressTool.getStringAddressByBytes(agent.getAgentAddress());
            this.agentName = SpringLiteContext.getBean(AgentManager.class).getAgentId(agent.getTxHash());
        }
    }

    public String getDeposit() {
        return deposit;
    }

    public void setDeposit(String deposit) {
        this.deposit = deposit;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public Long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public Long getDelHeight() {
        return delHeight;
    }

    public void setDelHeight(Long delHeight) {
        this.delHeight = delHeight;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentAddress() {
        return agentAddress;
    }

    public void setAgentAddress(String agentAddress) {
        this.agentAddress = agentAddress;
    }
}
