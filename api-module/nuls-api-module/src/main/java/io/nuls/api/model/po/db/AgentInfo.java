package io.nuls.api.model.po.db;

import lombok.Data;

import java.math.BigInteger;

@Data
public class AgentInfo extends TxDataInfo {

    private String txHash;

    private String agentId;

    private String agentAddress;

    private String packingAddress;

    private String rewardAddress;

    private String agentAlias;

    private BigInteger deposit;

    private double commissionRate;

    private long createTime;
    // 0:待共识，1:共识中，2:退出共识
    private int status;

    private BigInteger totalDeposit;

    private int depositCount;

    private double creditValue;

    private long totalPackingCount;

    private double lostRate;

    private long lastRewardHeight;

    private String deleteHash;

    private long blockHeight;

    private long deleteHeight;

    private BigInteger totalReward;

    private BigInteger commissionReward;

    private BigInteger agentReward;

    private boolean isNew;

    private long roundPackingTime;

    private int version;

    public AgentInfo() {
        totalReward = BigInteger.ZERO;
        totalPackingCount = 0;
    }

}
