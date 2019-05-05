package io.nuls.base.api.provider.transaction.facade;

import io.nuls.core.constant.TxStatusEnum;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TransactionData)) return false;

        TransactionData that = (TransactionData) o;

        if (type != that.type) return false;
        if (blockHeight != that.blockHeight) return false;
        if (size != that.size) return false;
        if (inBlockIndex != that.inBlockIndex) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (transactionSignature != null ? !transactionSignature.equals(that.transactionSignature) : that.transactionSignature != null)
            return false;
        if (remark != null ? !remark.equals(that.remark) : that.remark != null) return false;
        if (hash != null ? !hash.equals(that.hash) : that.hash != null) return false;
        if (status != that.status) return false;
        if (form != null ? !form.equals(that.form) : that.form != null) return false;
        return to != null ? to.equals(that.to) : that.to == null;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (transactionSignature != null ? transactionSignature.hashCode() : 0);
        result = 31 * result + (remark != null ? remark.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (int) (blockHeight ^ (blockHeight >>> 32));
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + size;
        result = 31 * result + inBlockIndex;
        result = 31 * result + (form != null ? form.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        return result;
    }


    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"type\":")
                .append(type)
                .append(",\"time\":\"")
                .append(time).append('\"')
                .append(",\"transactionSignature\":\"")
                .append(transactionSignature).append('\"')
                .append(",\"remark\":\"")
                .append(remark).append('\"')
                .append(",\"hash\":\"")
                .append(hash).append('\"')
                .append(",\"blockHeight\":")
                .append(blockHeight)
                .append(",\"status\":")
                .append(status)
                .append(",\"size\":")
                .append(size)
                .append(",\"inBlockIndex\":")
                .append(inBlockIndex)
                .append(",\"form\":")
                .append(form)
                .append(",\"to\":")
                .append(to)
                .append('}').toString();
    }


}
