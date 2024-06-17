package io.nuls.base.api.provider.crosschain.facade;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 11:41
 * @Description: Function Description
 */
public class CrossChainRegisterInfo {

    /**
     * chainId : Chain identification
     * chainName : Chain Name
     * addressType : The address type of the account created on the chain：1Within the ecosystem 2Non ecological interior
     * magicNumber : Network Magic Parameters
     * minAvailableNodeNum : Minimum number of available nodes
     * txConfirmedBlockNum : Number of cross chain transaction confirmation blocks
     * regAddress : NsdxSexqXF4eVXkcGLPpZCPKo92A8xpp
     * regTxHash : FFFFF
     * selfAssetKeyList : List of assets registered off the chain, bychainId_assetId Portfolio assetskeyvalue
     * totalAssetKeyList : The list of assets circulating off the chain, consisting ofchainId_assetId Portfolio assetskeyvalue
     * createTime : Creation time
     * seeds : xxx.xxx.xxx.xxx:8001,xxx.xxx.xxx.xxx:8002
     */

    private int chainId;
    private String chainName;
    private String addressType;
    private String addressPrefix;
    private long magicNumber;
    private int minAvailableNodeNum;
    private String regAddress;
    private String regTxHash;
    private long createTime;
    List<String> verifierList;
    /**
     * according to100To calculate the Byzantine proportion
     */
    int signatureByzantineRatio = 0;
    /**
     * Maximum number of signatures
     */
    int maxSignatureCount = 0;

    private List<String> selfAssetKeyList;
    private List<String> totalAssetKeyList;

    String mainNetVerifierSeeds = "";
    String mainNetCrossConnectSeeds = "";

    private boolean enable = true;


    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getChainName() {
        return chainName;
    }

    public void setChainName(String chainName) {
        this.chainName = chainName;
    }

    public String getAddressType() {
        return addressType;
    }

    public void setAddressType(String addressType) {
        this.addressType = addressType;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getMinAvailableNodeNum() {
        return minAvailableNodeNum;
    }

    public void setMinAvailableNodeNum(int minAvailableNodeNum) {
        this.minAvailableNodeNum = minAvailableNodeNum;
    }

    public String getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(String regAddress) {
        this.regAddress = regAddress;
    }

    public String getRegTxHash() {
        return regTxHash;
    }

    public void setRegTxHash(String regTxHash) {
        this.regTxHash = regTxHash;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }


    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    public List<String> getSelfAssetKeyList() {
        return selfAssetKeyList;
    }

    public void setSelfAssetKeyList(List<String> selfAssetKeyList) {
        this.selfAssetKeyList = selfAssetKeyList;
    }

    public List<String> getTotalAssetKeyList() {
        return totalAssetKeyList;
    }

    public void setTotalAssetKeyList(List<String> totalAssetKeyList) {
        this.totalAssetKeyList = totalAssetKeyList;
    }

    public List<String> getVerifierList() {
        return verifierList;
    }

    public void setVerifierList(List<String> verifierList) {
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

    public String getMainNetVerifierSeeds() {
        return mainNetVerifierSeeds;
    }

    public void setMainNetVerifierSeeds(String mainNetVerifierSeeds) {
        this.mainNetVerifierSeeds = mainNetVerifierSeeds;
    }

    public String getMainNetCrossConnectSeeds() {
        return mainNetCrossConnectSeeds;
    }

    public void setMainNetCrossConnectSeeds(String mainNetCrossConnectSeeds) {
        this.mainNetCrossConnectSeeds = mainNetCrossConnectSeeds;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
