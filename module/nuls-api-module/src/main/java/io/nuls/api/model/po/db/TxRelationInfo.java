package io.nuls.api.model.po.db;

import io.nuls.api.utils.DocumentTransferTool;
import org.bson.Document;

import java.math.BigInteger;

import static io.nuls.api.constant.ApiConstant.TRANSFER_FROM_TYPE;
import static io.nuls.api.constant.ApiConstant.TRANSFER_TO_TYPE;


public class TxRelationInfo {

    private String txHash;

    private String address;

    private int type;

    private long createTime;

    private long height;

    private int chainId;

    private int assetId;

    private String symbol;

    private BigInteger values;

    private FeeInfo fee;

    private BigInteger balance;

    // -1 : from , 1: to
    private int transferType;

    private int status;

    public TxRelationInfo() {

    }

    public TxRelationInfo(String address, String txHash) {
        this.address = address;
        this.txHash = txHash;
    }

    public TxRelationInfo(CoinToInfo output, TransactionInfo tx, BigInteger balance) {
        this.address = output.getAddress();
        this.chainId = output.getChainId();
        this.assetId = output.getAssetsId();
        this.symbol = output.getSymbol();
        this.values = output.getAmount();
        this.txHash = tx.getHash();
        this.type = tx.getType();
        this.createTime = tx.getCreateTime();
        this.height = tx.getHeight();
        this.fee = tx.getFee();
        this.balance = balance;
        this.transferType = TRANSFER_TO_TYPE;
    }

    public TxRelationInfo(CoinToInfo output, TransactionInfo tx, BigInteger values, BigInteger balance) {
        this.address = output.getAddress();
        this.chainId = output.getChainId();
        this.assetId = output.getAssetsId();
        this.symbol = output.getSymbol();
        this.values = values;
        this.txHash = tx.getHash();
        this.type = tx.getType();
        this.createTime = tx.getCreateTime();
        this.height = tx.getHeight();
        this.fee = tx.getFee();
        this.balance = balance;
        this.transferType = TRANSFER_TO_TYPE;
    }

    public TxRelationInfo(CoinFromInfo input, TransactionInfo tx, BigInteger balance) {
        this.address = input.getAddress();
        this.chainId = input.getChainId();
        this.assetId = input.getAssetsId();
        this.symbol = input.getSymbol();
        this.values = input.getAmount();
        this.txHash = tx.getHash();
        this.type = tx.getType();
        this.createTime = tx.getCreateTime();
        this.height = tx.getHeight();
        this.fee = tx.getFee();
        this.balance = balance;
        this.transferType = TRANSFER_FROM_TYPE;
    }

    public TxRelationInfo(CoinFromInfo input, TransactionInfo tx, BigInteger values, BigInteger balance) {
        this.address = input.getAddress();
        this.chainId = input.getChainId();
        this.assetId = input.getAssetsId();
        this.symbol = input.getSymbol();
        this.values = values;
        this.txHash = tx.getHash();
        this.type = tx.getType();
        this.createTime = tx.getCreateTime();
        this.height = tx.getHeight();
        this.fee = tx.getFee();
        this.balance = balance;
        this.transferType = TRANSFER_FROM_TYPE;
    }

    public TxRelationInfo(String address, TransactionInfo tx, AssetInfo assetInfo, BigInteger values, int transferType, BigInteger balance) {
        this.address = address;
        this.txHash = tx.getHash();
        this.type = tx.getType();
        this.createTime = tx.getCreateTime();
        this.height = tx.getHeight();
        this.fee = tx.getFee();
        this.chainId = assetInfo.getChainId();
        this.assetId = assetInfo.getAssetId();
        this.symbol = assetInfo.getSymbol();
        this.values = values;
        this.transferType = transferType;
        this.balance = balance;
    }


    public Document toDocument() {
        Document document = new Document();
        document.append("address", address).append("txHash", txHash).append("createTime", createTime).append("type", type)
                .append("height", height).append("chainId", chainId).append("assetId", assetId).append("symbol", symbol)
                .append("values", values.toString()).append("transferType", transferType).append("balance", balance.toString()).append("fee", DocumentTransferTool.toDocument(fee));
        return document;
    }

    public static TxRelationInfo toInfo(Document document) {
        try {
            TxRelationInfo relationInfo = new TxRelationInfo();
            relationInfo.setAddress(document.getString("address"));
            relationInfo.setTxHash(document.getString("txHash"));
            relationInfo.setCreateTime(document.getLong("createTime"));
            relationInfo.setType(document.getInteger("type"));
            relationInfo.setHeight(document.getLong("height"));
            relationInfo.setChainId(document.getInteger("chainId"));
            relationInfo.setAssetId(document.getInteger("assetId"));
            relationInfo.setSymbol(document.getString("symbol"));
            relationInfo.setTransferType(document.getInteger("transferType"));
            relationInfo.setBalance(new BigInteger(document.getString("balance")));
            relationInfo.setValues(new BigInteger(document.getString("values")));
            relationInfo.setFee(DocumentTransferTool.toInfo((Document) document.get("fee"), FeeInfo.class));
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

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
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

    public BigInteger getBalance() {
        return balance;
    }

    public void setBalance(BigInteger balance) {
        this.balance = balance;
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

    public FeeInfo getFee() {
        return fee;
    }

    public void setFee(FeeInfo feeInfo) {
        this.fee = feeInfo;
    }
}
