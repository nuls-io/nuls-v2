package io.nuls.api.model.po.db;

import io.nuls.api.utils.DocumentTransferTool;
import org.bson.Document;

public class ContractTxInfo {

    private String contractAddress;

    private String txHash;

    private long blockHeight;

    private long time;

    private int type;

    private FeeInfo fee;

    public Document toDocument() {
        Document document = new Document();
        document.append("contractAddress", contractAddress).append("txHash", txHash).append("time", time).append("type", type)
                .append("blockHeight", blockHeight).append("fee", DocumentTransferTool.toDocument(fee));
        return document;
    }

    public static ContractTxInfo toInfo(Document document) {
        ContractTxInfo info = new ContractTxInfo();
        info.setContractAddress(document.getString("contractAddress"));
        info.setTxHash(document.getString("txHash"));
        info.setBlockHeight(document.getLong("blockHeight"));
        info.setTime(document.getLong("time"));
        info.setType(document.getInteger("type"));
        info.setFee(DocumentTransferTool.toInfo((Document) document.get("fee"), FeeInfo.class));
        return info;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public FeeInfo getFee() {
        return fee;
    }

    public void setFee(FeeInfo fee) {
        this.fee = fee;
    }
}
