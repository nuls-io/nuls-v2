package io.nuls.api.provider.consensus.facade;

import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-26 15:44
 * @Description: 功能描述
 */
@Data
public class AgentInfo {
        private String agentHash;
        private String agentAddress;
        private String packingAddress;
        private String rewardAddress;
        private String deposit;
        private double commissionRate;
        private String agentName;
        private String agentId;
        private long time;
        private long blockHeight = -1L;
        private long delHeight = -1L;
        private int status;
        private double creditVal;
        private String totalDeposit;
        private String txHash;
        private int memberCount;
        private String version;
}
