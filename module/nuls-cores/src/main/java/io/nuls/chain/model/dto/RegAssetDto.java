package io.nuls.chain.model.dto;

import io.nuls.core.rpc.model.ApiModelProperty;

import java.math.BigInteger;

/**
 * @Author: ljs
 * @Time: 2019-08-07 11:41
 * @Description: Function Description
 */
public class RegAssetDto {
    @ApiModelProperty(description = "Asset Chainid")
    private int chainId;
    @ApiModelProperty(description = "assetid")
    private int assetId = 0;
    @ApiModelProperty(description = "Asset symbols")
    private String symbol;
    @ApiModelProperty(description = "Asset Name")
    private String assetName;
    @ApiModelProperty(description = "Mortgage amount")
    private String depositNuls = "0";
    @ApiModelProperty(description = "Destruction amount")
    private String destroyNuls = "0";
    @ApiModelProperty(description = "Initial quantity")
    private String initNumber = "0";
    @ApiModelProperty(description = "Number of available assets")
    private short decimalPlaces = 8;
    @ApiModelProperty(description = "Is it available")
    private boolean enable = true;
    @ApiModelProperty(description = "Creation time")
    private long createTime = 0;
    @ApiModelProperty(description = "Create Address")
    private String address;
    @ApiModelProperty(description = "transactionhash")
    private String txHash;

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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getDepositNuls() {
        return depositNuls;
    }

    public void setDepositNuls(String depositNuls) {
        this.depositNuls = depositNuls;
    }

    public String getDestroyNuls() {
        return destroyNuls;
    }

    public void setDestroyNuls(String destroyNuls) {
        this.destroyNuls = destroyNuls;
    }

    public String getInitNumber() {
        return initNumber;
    }

    public void setInitNumber(String initNumber) {
        this.initNumber = initNumber;
    }

    public short getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(short decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getTxHash() {
        return txHash;
    }

    public void setTxHash(String txHash) {
        this.txHash = txHash;
    }
}


