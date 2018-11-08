package io.nuls.chain.model;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
public class Chain {
    private int chainId;
    private String name;
    private AddressType addressType;
    private List<Asset> assetList;
    private int magicNumber;
    private List<Seed>seedList;
    private boolean assetInflow;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AddressType getAddressType() {
        return addressType;
    }

    public void setAddressType(AddressType addressType) {
        this.addressType = addressType;
    }

    public List<Asset> getAssetList() {
        return assetList;
    }

    public void setAssetList(List<Asset> assetList) {
        this.assetList = assetList;
    }

    public int getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(int magicNumber) {
        this.magicNumber = magicNumber;
    }

    public List<Seed> getSeedList() {
        return seedList;
    }

    public void setSeedList(List<Seed> seedList) {
        this.seedList = seedList;
    }

    public boolean isAssetInflow() {
        return assetInflow;
    }

    public void setAssetInflow(boolean assetInflow) {
        this.assetInflow = assetInflow;
    }
}
