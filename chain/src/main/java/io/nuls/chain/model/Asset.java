package io.nuls.chain.model;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */
public class Asset {
    private int assetId;
    private String symbol;
    private String name;
    private int depositNuls;
    private long initCirculation;
    private byte decimalPlaces;
    private boolean available;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getDepositNuls() {
        return depositNuls;
    }

    public void setDepositNuls(int depositNuls) {
        this.depositNuls = depositNuls;
    }

    public long getInitCirculation() {
        return initCirculation;
    }

    public void setInitCirculation(long initCirculation) {
        this.initCirculation = initCirculation;
    }

    public byte getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(byte decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
