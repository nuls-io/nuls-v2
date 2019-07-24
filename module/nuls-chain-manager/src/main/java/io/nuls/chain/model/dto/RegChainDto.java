/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
@ApiModel(description = "资产所属链信息")
public class RegChainDto {
    /**
     * 链序号
     * Chain ID
     */
    @ApiModelProperty(description = "链id")
    private int chainId;

    /**
     * 链名称
     * Chain name
     */
    @ApiModelProperty(description = "链名称")
    private String chainName;

    /**
     * 地址类型（Nuls生态，其他）
     * Address type (Nuls ecology and others)
     */
    @ApiModelProperty(description = "地址类型（1：Nuls生态，2：其他）")
    private String addressType;

    /**
     * 地址前缀
     * Address prefix
     */
    @ApiModelProperty(description = "地址前缀")
    private String addressPrefix;

    /**
     * 魔法参数（唯一）
     * Magic number (Unique)
     */
    @ApiModelProperty(description = "魔法参数")
    private long magicNumber;


    /**
     * 最小可用节点数
     * Minimum number of available nodes
     */
    @ApiModelProperty(description = "最小可用节点数")
    private int minAvailableNodeNum;


    /**
     * 交易确认区块数
     * Transaction confirmation block counts
     */
    @ApiModelProperty(description = "交易确认区块数")
    private int txConfirmedBlockNum;

    /**
     * 删除链时，设置为true
     * When deleting a chain, set to true
     */
    @ApiModelProperty(description = "是否已注销")
    private boolean isDelete = false;

    /**
     * 创建时间
     * Create time
     */
    @ApiModelProperty(description = "创建时间")
    private long createTime;


    /**
     * 注册链时使用的地址
     * The address used when registering the chain
     */
    @ApiModelProperty(description = "注册链时使用的地址")
    private String regAddress;

    /**
     * 注册链时的交易哈希
     * Transaction hash when registering the chain
     */
    @ApiModelProperty(description = "注册链时的交易哈希")
    private String regTxHash;

    /**
     * 注册链时添加的资产序号
     * The asset ID added when registering the chain
     */
    @ApiModelProperty(description = "注册链时添加的资产序号")
    private int regAssetId;


    /**
     * 本链创建的所有资产，Key=chaiId_assetId
     * All assets created by this chain, Key=chaiId_assetId
     */
    @ApiModelProperty(description = "本链创建的所有资产，Key=chaiId_assetId")
    List<String> selfAssetKeyList = new ArrayList<>();

    /**
     * 链上流通的所有资产，Key=chaiId_assetId
     * All assets circulating in the chain, Key=chaiId_assetId
     */
    @ApiModelProperty(description = "链上流通的所有资产，Key=chaiId_assetId")
    List<String> totalAssetKeyList = new ArrayList<>();
    @ApiModelProperty(description = "跨链提供的主网连接种子")
    private String seeds;
    /**
     * 初始化验证人信息
     */
    List<String> verifierList = new ArrayList<String>();
    /**
     * 按100来计算拜占庭比例
     */
    int signatureByzantineRatio = 0;
    /**
     * 最大签名数量
     */
    int maxSignatureCount = 0;

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

    public String getSeeds() {
        return seeds;
    }

    public void setSeeds(String seeds) {
        this.seeds = seeds;
    }

    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }
}
