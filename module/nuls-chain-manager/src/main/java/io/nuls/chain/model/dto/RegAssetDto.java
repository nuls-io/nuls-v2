package io.nuls.chain.model.dto;

import io.nuls.core.rpc.model.ApiModelProperty;

import java.math.BigInteger;

/**
 * @Author: ljs
 * @Time: 2019-08-07 11:41
 * @Description: 功能描述
 */
public class RegAssetDto {
    @ApiModelProperty(description = "资产链id")
    private int chainId;
    @ApiModelProperty(description = "资产id")
    private int assetId = 0;
    @ApiModelProperty(description = "资产符号")
    private String symbol;
    @ApiModelProperty(description = "资产名称")
    private String assetName;
    @ApiModelProperty(description = "抵押金额")
    private String depositNuls = "0";
    @ApiModelProperty(description = "销毁金额")
    private String destroyNuls = "0";
    @ApiModelProperty(description = "初始数量")
    private String initNumber = "0";
    @ApiModelProperty(description = "资产可用位数")
    private short decimalPlaces = 8;
    @ApiModelProperty(description = "是否可用")
    private boolean enable = true;
    @ApiModelProperty(description = "创建时间")
    private long createTime = 0;
    @ApiModelProperty(description = "创建地址")
    private String address;
    @ApiModelProperty(description = "交易hash")
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


