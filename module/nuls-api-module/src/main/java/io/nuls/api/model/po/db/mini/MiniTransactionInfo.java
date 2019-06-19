package io.nuls.api.model.po.db.mini;

import io.nuls.api.model.po.db.FeeInfo;
import io.nuls.api.utils.DocumentTransferTool;
import org.bson.Document;

import java.math.BigInteger;

public class MiniTransactionInfo {

    private String hash;

    private int type;

    private long height;

    private long createTime;

    private FeeInfo fee;

    private BigInteger value;

    private int status;

    public static MiniTransactionInfo toInfo(Document document) {
        MiniTransactionInfo info = new MiniTransactionInfo();
        info.hash = document.getString("_id");
        info.type = document.getInteger("type");
        info.height = document.getLong("height");
        info.createTime = document.getLong("createTime");
        info.value = new BigInteger(document.getString("value"));
        info.status = document.getInteger("status");
        info.fee = DocumentTransferTool.toInfo((Document) document.get("fee"), FeeInfo.class);

        return info;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public FeeInfo getFee() {
        return fee;
    }

    public void setFee(FeeInfo fee) {
        this.fee = fee;
    }

    public BigInteger getValue() {
        return value;
    }

    public void setValue(BigInteger value) {
        this.value = value;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
