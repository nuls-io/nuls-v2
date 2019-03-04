package io.nuls.api.model.po.db;

import lombok.Data;

import java.math.BigInteger;

@Data
public class DepositInfo extends TxDataInfo {

    private String key;

    private String txHash;

    private BigInteger amount;

    private String agentHash;

    private String address;

    private Long createTime;

    private String deleteKey;

    private long blockHeight;

    private long deleteHeight;

    private BigInteger fee;

    private boolean isNew;
    // 0 加入共识，1 退出共识
    private int type;
}
