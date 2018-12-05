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


/**
 * 全网共识信息类
 * Network-wide Consensus Information Class
 *
 * @author tag
 * 2018/11/20
 * */

public class WholeNetConsensusInfoDTO {
    private int agentCount;
    private String totalDeposit;
    @JsonIgnore
    private String rewardOfDay;
    private int consensusAccountNumber;
    private int packingAgentCount;
    public int getConsensusAccountNumber() {
        return consensusAccountNumber;
    }
    public void setConsensusAccountNumber(int consensusAccountNumber) {
        this.consensusAccountNumber = consensusAccountNumber;
    }

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

    public String getRewardOfDay() {
        return rewardOfDay;
    }

    public void setRewardOfDay(String rewardOfDay) {
        this.rewardOfDay = rewardOfDay;
    }

    public void setPackingAgentCount(int packingAgentCount) {
        this.packingAgentCount = packingAgentCount;
    }

    public int getPackingAgentCount() {
        return packingAgentCount;
    }
}
