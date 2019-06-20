package io.nuls.api.model.po.db;

import io.nuls.api.utils.DocumentTransferTool;
import org.bson.Document;

import java.math.BigInteger;

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

    public TxRelationInfo(CoinToInfo output, TransactionInfo info, int transferType, BigInteger balance, FeeInfo feeInfo) {
        this.address = output.getAddress();
        this.chainId = output.getChainId();
        this.assetId = output.getAssetsId();
        this.symbol = output.getSymbol();
        this.values = output.getAmount();
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.balance = balance;
        this.transferType = transferType;
        this.fee = feeInfo;
        this.values = feeInfo.getValue();
    }

    public TxRelationInfo(CoinToInfo output, TransactionInfo info, BigInteger amount, int transferType, BigInteger balance, FeeInfo feeInfo) {
        this.address = output.getAddress();
        this.chainId = output.getChainId();
        this.assetId = output.getAssetsId();
        this.symbol = output.getSymbol();
        this.values = amount;
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.balance = balance;
        this.transferType = transferType;
        this.fee = feeInfo;
    }

    public TxRelationInfo(CoinFromInfo input, TransactionInfo info, int transferType, BigInteger balance, FeeInfo feeInfo) {
        this.address = input.getAddress();
        this.chainId = input.getChainId();
        this.assetId = input.getAssetsId();
        this.symbol = input.getSymbol();
        this.values = input.getAmount();
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.balance = balance;
        this.transferType = transferType;
        this.fee = feeInfo;
    }

    public TxRelationInfo(CoinFromInfo input, TransactionInfo info, BigInteger fee, int transferType, BigInteger balance, FeeInfo feeInfo) {
        this.address = input.getAddress();
        this.chainId = input.getChainId();
        this.assetId = input.getAssetsId();
        this.symbol = input.getSymbol();
        this.values = input.getAmount().subtract(fee);
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.balance = balance;
        this.transferType = transferType;
        this.fee = feeInfo;
    }

    public TxRelationInfo(CoinFromInfo input, TransactionInfo info, BigInteger amount, BigInteger balance, FeeInfo feeInfo) {
        this.address = input.getAddress();
        this.chainId = input.getChainId();
        this.assetId = input.getAssetsId();
        this.symbol = input.getSymbol();
        this.values = amount;
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.balance = balance;
        this.transferType = TRANSFER_TO_TYPE;
        this.fee = feeInfo;
    }

    public TxRelationInfo(CoinFromInfo input, TransactionInfo info, int transferType, BigInteger balance, BigInteger amount, FeeInfo feeInfo) {
        this.address = input.getAddress();
        this.chainId = input.getChainId();
        this.assetId = input.getAssetsId();
        this.symbol = input.getSymbol();
        this.values = amount;
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.balance = balance;
        this.transferType = transferType;
        this.fee = feeInfo;
    }

    public TxRelationInfo(String address, TransactionInfo info, AssetInfo assetInfo, BigInteger amount, int transferType, BigInteger balance, FeeInfo feeInfo) {
        this.address = address;
        this.txHash = info.getHash();
        this.type = info.getType();
        this.createTime = info.getCreateTime();
        this.height = info.getHeight();
        this.chainId = assetInfo.getChainId();
        this.assetId = assetInfo.getAssetId();
        this.symbol = assetInfo.getSymbol();
        this.values = amount;
        this.transferType = transferType;
        this.balance = balance;
        this.fee = feeInfo;
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
