package io.nuls.base.api.provider.crosschain.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-06 16:50
 * @Description: Function Description
 */
public class RegisterChainReq extends BaseReq {


    /**
     * chainId : 152
     * assetId : 85
     * chainName : nuls chain
     * addressType : 1
     * magicNumber : 454546
     * minAvailableNodeNum : 5
     * txConfirmedBlockNum : 30
     * address : NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp
     * symbol : NULS
     * assetName : Nass
     * initNumber : 1000000000
     * decimalPlaces : 8
     * password : xxxxxxxxxxxxx
     */

    private String address;
    private String chainName;
    private String addressPrefix;

    private Long magicNumber;

    /**
     * Initialize Verifier Information
     */
    private String verifierList = "";
    /**
     * according to100To calculate the Byzantine proportion
     */
    private int signatureByzantineRatio = 0;
    /**
     * Maximum number of signatures
     */
    private int maxSignatureCount = 0;

    private int assetId;
    private String symbol;
    private String assetName;
    private Long initNumber;
    private String addressType;
    private int minAvailableNodeNum;
    private int decimalPlaces;
    private String password;

    public RegisterChainReq(String address, Integer chainId, String chainName, String addressPrefix,Long magicNumber,
                            int maxSignatureCount, int signatureByzantineRatio, String verifierList,
                            int assetId, String symbol, String assetName, Long initNumber, String addressType,
                            String password) {
        this.setChainId(chainId);
        this.address = address;
        this.chainName = chainName;
        this.addressPrefix = addressPrefix;
        this.magicNumber = magicNumber;
        this.assetId = assetId;
        this.symbol = symbol;
        this.assetName = assetName;
        this.initNumber = initNumber;
        this.addressType = addressType;
        this.password = password;
        this.maxSignatureCount = maxSignatureCount;
        this.signatureByzantineRatio = signatureByzantineRatio;
        this.verifierList = verifierList;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public Long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(Long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getMinAvailableNodeNum() {
        return minAvailableNodeNum;
    }

    public void setMinAvailableNodeNum(int minAvailableNodeNum) {
        this.minAvailableNodeNum = minAvailableNodeNum;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
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

    public Long getInitNumber() {
        return initNumber;
    }

    public void setInitNumber(Long initNumber) {
        this.initNumber = initNumber;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerifierList() {
        return verifierList;
    }

    public void setVerifierList(String verifierList) {
        this.verifierList = verifierList;
    }

    public int getSignatureByzantineRatio() {
        return signatureByzantineRatio;
    }

    public void setSignatureByzantineRatio(int signatureByzantineRatio) {
        this.signatureByzantineRatio = signatureByzantineRatio;
    }

    public int getMaxSignatureCount() {
        return maxSignatureCount;
    }

    public void setMaxSignatureCount(int maxSignatureCount) {
        this.maxSignatureCount = maxSignatureCount;
    }
}

