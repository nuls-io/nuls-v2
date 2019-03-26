package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class ContractTxInfo {

    private String contractAddress;

    private String txHash;

    private long blockHeight;

    private long time;

    private int type;

    private String fee;
}
