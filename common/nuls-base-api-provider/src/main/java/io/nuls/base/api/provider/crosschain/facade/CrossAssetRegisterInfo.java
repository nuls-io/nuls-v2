package io.nuls.base.api.provider.crosschain.facade;

import java.math.BigInteger;
import java.util.List;

/**
 * @Author: ljs
 * @Time: 2019-08-07 11:41
 * @Description: 功能描述
 */
public class CrossAssetRegisterInfo {
    private int chainId;
    private int assetId = 0;
    private String symbol;
    private String assetName;
    private BigInteger depositNuls = BigInteger.ZERO;
    private BigInteger destroyNuls = BigInteger.ZERO;
    private BigInteger initNumber = BigInteger.ZERO;
    private short decimalPlaces = 8;
    private boolean enable = true;
    private long createTime = 0;
    private String address;
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

    public BigInteger getDepositNuls() {
        return depositNuls;
    }

    public void setDepositNuls(BigInteger depositNuls) {
        this.depositNuls = depositNuls;
    }

    public BigInteger getDestroyNuls() {
        return destroyNuls;
    }

    public void setDestroyNuls(BigInteger destroyNuls) {
        this.destroyNuls = destroyNuls;
    }

    public BigInteger getInitNumber() {
        return initNumber;
    }

    public void setInitNumber(BigInteger initNumber) {
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


