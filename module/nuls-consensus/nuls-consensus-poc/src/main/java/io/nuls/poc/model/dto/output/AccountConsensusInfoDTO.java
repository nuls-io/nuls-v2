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

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * 账户共识信息类
 * Account Consensus Information Class
 *
 * @author tag
 * 2018/11/20
 */
@ApiModel(name = "账户共识信息")
public class AccountConsensusInfoDTO {
    @ApiModelProperty(description = "节点数量")
    private int agentCount;
    @ApiModelProperty(description = "参与共识的总金额")
    private String totalDeposit;
    @ApiModelProperty(description = "参与共识节点的数量")
    private int joinAgentCount;
    @ApiModelProperty(description = "可用余额")
    private String usableBalance;
    @ApiModelProperty(description = "获得的共识奖励")
    private String reward;
    @ApiModelProperty(description = "当天获得的共识奖励")
    private String rewardOfDay;
    @ApiModelProperty(description = "创建的节点HASH")
    private String agentHash;

    public int getAgentCount() {
        return agentCount;
    }

    public void setAgentCount(int agentCount) {
        this.agentCount = agentCount;
    }

    public String getTotalDeposit() {
        return totalDeposit;
    }

    public void setTotalDeposit(String totalDeposit) {
        this.totalDeposit = totalDeposit;
    }

    public int getJoinAgentCount() {
        return joinAgentCount;
    }

    public void setJoinAgentCount(int joinAgentCount) {
        this.joinAgentCount = joinAgentCount;
    }

    public String getUsableBalance() {
        return usableBalance;
    }

    public void setUsableBalance(String usableBalance) {
        this.usableBalance = usableBalance;
    }

    public String getReward() {
        return reward;
    }

    public void setReward(String reward) {
        this.reward = reward;
    }

    public String getRewardOfDay() {
        return rewardOfDay;
    }

    public void setRewardOfDay(String rewardOfDay) {
        this.rewardOfDay = rewardOfDay;
    }

    public String getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(String agentHash) {
        this.agentHash = agentHash;
    }
}
