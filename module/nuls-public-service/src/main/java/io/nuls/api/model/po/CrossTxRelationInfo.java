package io.nuls.api.model.po;

import org.bson.Document;

import java.math.BigInteger;

import static io.nuls.api.constant.ApiConstant.TRANSFER_FROM_TYPE;
import static io.nuls.api.constant.ApiConstant.TRANSFER_TO_TYPE;


public class CrossTxRelationInfo {

    private String txHash;

    private String address;

    private long createTime;

    private long height;

    private int chainId;

    private int assetId;

    private int decimal;

    private String symbol;

    private BigInteger values;

    // -1 : from , 1: to
    private int transferType;

    private int status;

    public CrossTxRelationInfo() {

    }

    public CrossTxRelationInfo(CoinToInfo output, TransactionInfo tx, int decimal) {
        this.address = output.getAddress();
        this.chainId = output.getChainId();
        this.assetId = output.getAssetsId();
        this.height = tx.getHeight();
        this.symbol = output.getSymbol();
        this.values = output.getAmount();
        this.txHash = tx.getHash();
        this.createTime = tx.getCreateTime();
        this.transferType = TRANSFER_TO_TYPE;
        this.decimal = decimal;
    }

    public CrossTxRelationInfo(CoinFromInfo input, TransactionInfo tx, int decimal) {
        this.address = input.getAddress();
        this.chainId = input.getChainId();
        this.assetId = input.getAssetsId();
        this.height = tx.getHeight();
        this.symbol = input.getSymbol();
        this.values = input.getAmount();
        this.txHash = tx.getHash();
        this.createTime = tx.getCreateTime();
        this.transferType = TRANSFER_FROM_TYPE;
        this.decimal = decimal;
    }

    public Document toDocument() {
        Document document = new Document();
        document.append("address", address).append("txHash", txHash).append("createTime", createTime)
                .append("chainId", chainId).append("assetId", assetId).append("symbol", symbol).append("height", height)
                .append("values", values.toString()).append("transferType", transferType);
        return document;
    }

    public static CrossTxRelationInfo toInfo(Document document) {
        try {
            CrossTxRelationInfo relationInfo = new CrossTxRelationInfo();
            relationInfo.setAddress(document.getString("address"));
            relationInfo.setTxHash(document.getString("txHash"));
            relationInfo.setCreateTime(document.getLong("createTime"));
            relationInfo.setChainId(document.getInteger("chainId"));
            relationInfo.setAssetId(document.getInteger("assetId"));
            relationInfo.setSymbol(document.getString("symbol"));
            relationInfo.setTransferType(document.getInteger("transferType"));
            relationInfo.setValues(new BigInteger(document.getString("values")));
            relationInfo.setHeight(document.getLong("height"));
            return relationInfo;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
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


    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public BigInteger getValues() {
        return values;
    }

    public void setValues(BigInteger values) {
        this.values = values;
    }

    public int getTransferType() {
        return transferType;
    }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getDecimal() {
        return decimal;
    }

    public void setDecimal(int decimal) {
        this.decimal = decimal;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }
}
