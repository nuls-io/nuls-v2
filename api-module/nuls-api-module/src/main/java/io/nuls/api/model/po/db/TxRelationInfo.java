package io.nuls.api.model.po.db;

import lombok.Data;

import java.math.BigInteger;

@Data
public class TxRelationInfo {

    private String txHash;

    private String address;

    private int type;

    private long createTime;

    private long height;

    private int chainId;

    private int assetId;

    private BigInteger values;

    private BigInteger fee;

    private BigInteger balance;

    public TxRelationInfo() {

    }

    public TxRelationInfo(String address, TransactionInfo info, int chainId, int assetId, BigInteger values, BigInteger balance) {
        this.address = address;
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.chainId = chainId;
        this.assetId = assetId;
        this.fee = info.getFee();
        this.values = values;
        this.balance = balance;
    }
}
