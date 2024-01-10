/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.chain.model.dto;

import io.nuls.base.basic.AddressTool;
import io.nuls.chain.model.po.BlockChain;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
@ApiModel(description = "Asset Chain Information")
public class RegChainDto {
    /**
     * Chain number
     * Chain ID
     */
    @ApiModelProperty(description = "chainid")
    private int chainId;

    /**
     * Chain Name
     * Chain name
     */
    @ApiModelProperty(description = "Chain Name")
    private String chainName;

    /**
     * Address type（NulsEcology, Other）
     * Address type (Nuls ecology and others)
     */
    @ApiModelProperty(description = "Address type（1：NulsEcology,2：other）")
    private String addressType;

    /**
     * Address prefix
     * Address prefix
     */
    @ApiModelProperty(description = "Address prefix")
    private String addressPrefix;

    /**
     * Magic parameters（only）
     * Magic number (Unique)
     */
    @ApiModelProperty(description = "Magic parameters")
    private long magicNumber;


    /**
     * Minimum number of available nodes
     * Minimum number of available nodes
     */
    @ApiModelProperty(description = "Minimum number of available nodes")
    private int minAvailableNodeNum;


    /**
     * Number of transaction confirmation blocks
     * Transaction confirmation block counts
     */
    @ApiModelProperty(description = "Number of transaction confirmation blocks")
    private int txConfirmedBlockNum;

    /**
     * When deleting a chain, set totrue
     * When deleting a chain, set to true
     */
    @ApiModelProperty(description = "Has it been cancelled")
    private boolean isDelete = false;

    /**
     * Creation time
     * Create time
     */
    @ApiModelProperty(description = "Creation time")
    private long createTime;


    /**
     * The address used when registering the chain
     * The address used when registering the chain
     */
    @ApiModelProperty(description = "The address used when registering the chain")
    private String regAddress;

    /**
     * Transaction hash during registration chain
     * Transaction hash when registering the chain
     */
    @ApiModelProperty(description = "Transaction hash during registration chain")
    private String regTxHash;

    /**
     * Asset serial number added during registration chain
     * The asset ID added when registering the chain
     */
    @ApiModelProperty(description = "Asset serial number added during registration chain")
    private int regAssetId;


    /**
     * All assets created in this chain,Key=chaiId_assetId
     * All assets created by this chain, Key=chaiId_assetId
     */
    @ApiModelProperty(description = "All assets created in this chain,Key=chaiId_assetId")
    List<String> selfAssetKeyList = new ArrayList<>();

    /**
     * All assets circulating on the chain,Key=chaiId_assetId
     * All assets circulating in the chain, Key=chaiId_assetId
     */
    @ApiModelProperty(description = "All assets circulating on the chain,Key=chaiId_assetId")
    List<String> totalAssetKeyList = new ArrayList<>();

    /**
     * Initialize Verifier Information
     */
    @ApiModelProperty(description = "Verifier List")
    List<String> verifierList = new ArrayList<String>();
    /**
     * according to100To calculate the Byzantine proportion
     */
    @ApiModelProperty(description = "Byzantine proportion")
    int signatureByzantineRatio = 0;
    /**
     * Maximum number of signatures
     */
    @ApiModelProperty(description = "Maximum number of signatures")
    int maxSignatureCount = 0;

    @ApiModelProperty(description = "List of main network validators,Comma separated")
    String mainNetVerifierSeeds = "";

    @ApiModelProperty(description = "Main network connection seeds provided across chains,Comma separated")
    String mainNetCrossConnectSeeds = "";
    @ApiModelProperty(description = "Is it available")
    boolean enable = true;

    public void buildRegChainDto(BlockChain blockChain) {
        this.addressType = blockChain.getAddressType();
        this.addressPrefix = blockChain.getAddressPrefix();
        this.chainId = blockChain.getChainId();
        this.regAssetId = blockChain.getRegAssetId();
        this.chainName = blockChain.getChainName();
        this.magicNumber = blockChain.getMagicNumber();
        this.regTxHash = blockChain.getRegTxHash();
        this.isDelete = blockChain.isDelete();
        this.minAvailableNodeNum = blockChain.getMinAvailableNodeNum();
        this.selfAssetKeyList = blockChain.getSelfAssetKeyList();
        this.totalAssetKeyList = blockChain.getTotalAssetKeyList();
        this.regAddress = AddressTool.getStringAddressByBytes(blockChain.getRegAddress());
        this.createTime = blockChain.getCreateTime();
        this.verifierList = blockChain.getVerifierList();
        this.maxSignatureCount = blockChain.getMaxSignatureCount();
        this.signatureByzantineRatio = blockChain.getSignatureByzantineRatio();
        if (blockChain.isDelete()) {
            enable = false;
        }
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

    public int getTxConfirmedBlockNum() {
        return txConfirmedBlockNum;
    }

    public void setTxConfirmedBlockNum(int txConfirmedBlockNum) {
        this.txConfirmedBlockNum = txConfirmedBlockNum;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
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

    public int getRegAssetId() {
        return regAssetId;
    }

    public void setRegAssetId(int regAssetId) {
        this.regAssetId = regAssetId;
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


    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
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
