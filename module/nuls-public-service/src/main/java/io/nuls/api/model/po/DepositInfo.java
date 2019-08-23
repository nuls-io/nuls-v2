package io.nuls.api.model.po;


import com.fasterxml.jackson.annotation.JsonIgnore;

import java.math.BigInteger;

public class DepositInfo extends TxDataInfo {

    private String key;

    private String txHash;

    private BigInteger amount;

    private String agentHash;

    private String address;

    private long createTime;

    private String deleteKey;

    private long blockHeight;

    private long deleteHeight;

    private BigInteger fee;

    @JsonIgnore
    private boolean isNew;
    // 0 加入共识，1 退出共识
    private int type;

    public void copyInfoWithDeposit() {
    }

    public void copyInfoWithDeposit(DepositInfo depositInfo) {
        this.amount = depositInfo.amount;
        this.address = depositInfo.address;
        this.agentHash = depositInfo.getAgentHash();
    }


    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public BigInteger getAmount() {
        return amount;
    }

    public void setAmount(BigInteger amount) {
        this.amount = amount;
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

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getDeleteKey() {
        return deleteKey;
    }

    public void setDeleteKey(String deleteKey) {
        this.deleteKey = deleteKey;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getDeleteHeight() {
        return deleteHeight;
    }

    public void setDeleteHeight(long deleteHeight) {
        this.deleteHeight = deleteHeight;
    }

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean aNew) {
        isNew = aNew;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
