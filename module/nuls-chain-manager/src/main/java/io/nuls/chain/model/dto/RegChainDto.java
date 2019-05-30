package io.nuls.chain.model.dto;

import io.nuls.base.basic.AddressTool;
import io.nuls.chain.model.po.BlockChain;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
public class RegChainDto {
    /**
     * 链序号
     * Chain ID
     */
    private int chainId;

    /**
     * 链名称
     * Chain name
     */
    private String chainName;

    /**
     * 地址类型（Nuls生态，其他）
     * Address type (Nuls ecology and others)
     */
    private String addressType;

    /**
     * 魔法参数（唯一）
     * Magic number (Unique)
     */
    private long magicNumber;



    /**
     * 最小可用节点数
     * Minimum number of available nodes
     */
    private int minAvailableNodeNum;



    /**
     * 交易确认区块数
     * Transaction confirmation block counts
     */
    private int txConfirmedBlockNum;

    /**
     * 删除链时，设置为true
     * When deleting a chain, set to true
     */
    private boolean isDelete = false;

    /**
     * 创建时间
     * Create time
     */
    private long createTime;


    /**
     * 注册链时使用的地址
     * The address used when registering the chain
     */
    private String regAddress;

    /**
     * 注册链时的交易哈希
     * Transaction hash when registering the chain
     */
    private String regTxHash;

    /**
     * 注册链时添加的资产序号
     * The asset ID added when registering the chain
     */
    private int regAssetId;


    /**
     * 本链创建的所有资产，Key=chaiId_assetId
     * All assets created by this chain, Key=chaiId_assetId
     */
    List<String> selfAssetKeyList = new ArrayList<>();

    /**
     * 链上流通的所有资产，Key=chaiId_assetId
     * All assets circulating in the chain, Key=chaiId_assetId
     */
    List<String> totalAssetKeyList = new ArrayList<>();

    private String seeds;

    public void buildRegChainDto(BlockChain blockChain){
            this.addressType=blockChain.getAddressType();
            this.chainId=blockChain.getChainId();
            this.regAssetId = blockChain.getRegAssetId();
            this.chainName = blockChain.getChainName();
            this.magicNumber=blockChain.getMagicNumber();
            this.regTxHash = blockChain.getRegTxHash();
            this.isDelete = blockChain.isDelete();
            this.minAvailableNodeNum = blockChain.getMinAvailableNodeNum();
            this.selfAssetKeyList =blockChain.getSelfAssetKeyList();
            this.totalAssetKeyList =blockChain.getTotalAssetKeyList();
            this.regAddress = AddressTool.getStringAddressByBytes(blockChain.getRegAddress());
            this.createTime = blockChain.getCreateTime();
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
}
