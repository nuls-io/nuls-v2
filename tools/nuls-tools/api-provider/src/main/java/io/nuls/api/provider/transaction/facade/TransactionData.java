package io.nuls.api.provider.transaction.facade;

import io.nuls.base.constant.TxStatusEnum;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 16:39
 * @Description: 功能描述
 */
public class TransactionData {

    private int type;

    private String time;

    private String transactionSignature;

    private String remark;

    private String hash;

    private long blockHeight = -1L;

    private TxStatusEnum status = TxStatusEnum.UNCONFIRM;

    private int size;

    /**
     * 在区块中的顺序，存储在rocksDB中是无序的，保存区块时赋值，取出后根据此值排序
     */
    private int inBlockIndex;

    private List<TransactionCoinData> form;

    private List<TransactionCoinData> to;


    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getTransactionSignature() {
        return transactionSignature;
    }

    public void setTransactionSignature(String transactionSignature) {
        this.transactionSignature = transactionSignature;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public TxStatusEnum getStatus() {
        return status;
    }

    public void setStatus(TxStatusEnum status) {
        this.status = status;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getInBlockIndex() {
        return inBlockIndex;
    }

    public void setInBlockIndex(int inBlockIndex) {
        this.inBlockIndex = inBlockIndex;
    }

    public List<TransactionCoinData> getForm() {
        return form;
    }

    public void setForm(List<TransactionCoinData> form) {
        this.form = form;
    }

    public List<TransactionCoinData> getTo() {
        return to;
    }

    public void setTo(List<TransactionCoinData> to) {
        this.to = to;
    }
}
