package io.nuls.chain.model.po;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.chain.model.tx.txdata.TxChain;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.util.NulsDateUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
public class BlockChain extends BaseNulsData {
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
     * 链地址前缀
     */
    private String addressPrefix;
    /**
     * 魔法参数（唯一）
     * Magic number (Unique)
     */
    private long magicNumber;

    /**
     * 是否支持外链资产流入
     * Whether to support the inflow of external assets
     */
    private boolean supportInflowAsset;

    /**
     * 最小可用节点数
     * Minimum number of available nodes
     */
    private int minAvailableNodeNum;


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
     * 最后更新时间
     * Last update time
     */
    private long lastUpdateTime;

    /**
     * 注册链时使用的地址
     * The address used when registering the chain
     */
    private byte[] regAddress;

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
     * 删除链时使用的地址
     * The address used when deleting the chain
     */
    private byte[] delAddress;

    /**
     * 删除链时的交易哈希
     * Transaction hash when deleting the chain
     */
    private String delTxHash;

    /**
     * 删除链时删除的资产序号
     * The asset ID deleted when deleting the chain
     */
    private int delAssetId;

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


    public void addCreateAssetId(String key) {
        selfAssetKeyList.add(key);
    }

    public void removeCreateAssetId(String key) {
        selfAssetKeyList.remove(key);
    }

    public void addCirculateAssetId(String key) {
        totalAssetKeyList.add(key);
    }

    public void removeCirculateAssetId(String key) {
        totalAssetKeyList.remove(key);
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeString(chainName);
        stream.writeString(addressType);
        stream.writeString(addressPrefix);
        stream.writeUint32(magicNumber);
        stream.writeBoolean(supportInflowAsset);
        stream.writeUint32(minAvailableNodeNum);
        stream.writeBoolean(isDelete);
        stream.writeUint32(createTime);
        stream.writeUint32(lastUpdateTime);
        stream.writeBytesWithLength(regAddress);
        stream.writeString(regTxHash);
        stream.writeUint16(regAssetId);
        stream.writeBytesWithLength(delAddress);
        stream.writeString(delTxHash);
        stream.writeUint16(delAssetId);
        stream.writeUint16(selfAssetKeyList.size());
        for (String key : selfAssetKeyList) {
            stream.writeString(key);
        }
        stream.writeUint16(totalAssetKeyList.size());
        for (String key : totalAssetKeyList) {
            stream.writeString(key);
        }

        stream.writeUint16(verifierList.size());
        for (String verifier : verifierList) {
            stream.writeString(verifier);
        }
        stream.writeUint16(signatureByzantineRatio);
        stream.writeUint16(maxSignatureCount);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.chainName = byteBuffer.readString();
        this.addressType = byteBuffer.readString();
        this.addressPrefix = byteBuffer.readString();
        this.magicNumber = byteBuffer.readInt32();
        this.supportInflowAsset = byteBuffer.readBoolean();
        this.minAvailableNodeNum = byteBuffer.readInt32();
        this.isDelete = byteBuffer.readBoolean();
        this.createTime = byteBuffer.readUint32();
        this.lastUpdateTime = byteBuffer.readUint32();
        this.regAddress = byteBuffer.readByLengthByte();
        this.regTxHash = byteBuffer.readString();
        this.regAssetId = byteBuffer.readUint16();
        this.delAddress = byteBuffer.readByLengthByte();
        this.delTxHash = byteBuffer.readString();
        this.delAssetId = byteBuffer.readUint16();
        int selfSize = byteBuffer.readUint16();
        for (int i = 0; i < selfSize; i++) {
            selfAssetKeyList.add(byteBuffer.readString());
        }
        int totalSize = byteBuffer.readUint16();
        for (int i = 0; i < totalSize; i++) {
            totalAssetKeyList.add(byteBuffer.readString());
        }

        int verifierCount = byteBuffer.readUint16();
        for (int i = 0; i < verifierCount; i++) {
            String verifier = byteBuffer.readString();
            this.verifierList.add(verifier);
        }
        this.signatureByzantineRatio = byteBuffer.readUint16();
        this.maxSignatureCount = byteBuffer.readUint16();
    }

    @Override
    public int size() {
        int size = 0;
        // chainId;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(chainName);
        size += SerializeUtils.sizeOfString(addressType);
        size += SerializeUtils.sizeOfString(addressPrefix);
        // magicNumber;
        size += SerializeUtils.sizeOfInt32();
        // supportInflowAsset;
        size += SerializeUtils.sizeOfBoolean();
        // minAvailableNodeNum;
        size += SerializeUtils.sizeOfInt32();
        // isDelete
        size += SerializeUtils.sizeOfBoolean();
        // createTime;
        size += SerializeUtils.sizeOfUint32();
        // lastUpdateTime;
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfBytes(regAddress);
        //txHash
        size += SerializeUtils.sizeOfString(regTxHash);
        //regAssetId
        size += SerializeUtils.sizeOfInt16();
        size += SerializeUtils.sizeOfBytes(delAddress);
        //txHash
        size += SerializeUtils.sizeOfString(delTxHash);
        //delAssetId
        size += SerializeUtils.sizeOfInt16();

        size += SerializeUtils.sizeOfUint16();
        for (String key : selfAssetKeyList) {
            size += SerializeUtils.sizeOfString(key);
        }

        size += SerializeUtils.sizeOfUint16();
        for (String key : totalAssetKeyList) {
            size += SerializeUtils.sizeOfString(key);
        }

        //verifierList
        size += SerializeUtils.sizeOfUint16();
        for (String verifier : verifierList) {
            size += SerializeUtils.sizeOfString(verifier);
        }
        //signatureByzantineRatio
        size += SerializeUtils.sizeOfUint16();
        //maxSignatureCount
        size += SerializeUtils.sizeOfUint16();
        return size;
    }

    public BlockChain() {
        super();
    }

    public BlockChain(TxChain txChain) {
        this.addressType = txChain.getAddressType();
        this.addressPrefix = txChain.getAddressPrefix();
        this.chainId = txChain.getDefaultAsset().getChainId();
        this.magicNumber = txChain.getMagicNumber();
        this.minAvailableNodeNum = txChain.getMinAvailableNodeNum();
        this.chainName = txChain.getName();
        this.supportInflowAsset = txChain.isSupportInflowAsset();
        this.signatureByzantineRatio = txChain.getSignatureByzantineRatio();
        this.verifierList = txChain.getVerifierList();
        this.maxSignatureCount = txChain.getMaxSignatureCount();
    }

    public byte[] parseToTransaction(Asset asset) throws IOException {
        TxChain txChain = new TxChain();
        txChain.setAddressType(this.addressType);
        txChain.setAddressPrefix(this.addressPrefix);
        txChain.getDefaultAsset().setChainId(this.chainId);
        txChain.setMagicNumber(this.magicNumber);
        txChain.setMinAvailableNodeNum(this.minAvailableNodeNum);
        txChain.setName(this.chainName);
        txChain.setSupportInflowAsset(this.supportInflowAsset);
        txChain.setVerifierList(this.getVerifierList());
        txChain.setMaxSignatureCount(this.getMaxSignatureCount());
        txChain.setSignatureByzantineRatio(this.getSignatureByzantineRatio());

        txChain.getDefaultAsset().setAddress(asset.getAddress());
        txChain.getDefaultAsset().setAssetId(asset.getAssetId());
        txChain.getDefaultAsset().setSymbol(asset.getSymbol());
        txChain.getDefaultAsset().setName(asset.getAssetName());
        txChain.getDefaultAsset().setDepositNuls(asset.getDepositNuls());
        txChain.getDefaultAsset().setInitNumber(asset.getInitNumber());
        txChain.getDefaultAsset().setDestroyNuls(asset.getDestroyNuls());
        txChain.getDefaultAsset().setDecimalPlaces(asset.getDecimalPlaces());
        return txChain.serialize();
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

    public boolean isSupportInflowAsset() {
        return supportInflowAsset;
    }

    public void setSupportInflowAsset(boolean supportInflowAsset) {
        this.supportInflowAsset = supportInflowAsset;
    }

    public int getMinAvailableNodeNum() {
        return minAvailableNodeNum;
    }

    public void setMinAvailableNodeNum(int minAvailableNodeNum) {
        this.minAvailableNodeNum = minAvailableNodeNum;
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

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(long lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public byte[] getRegAddress() {
        return regAddress;
    }

    public void setRegAddress(byte[] regAddress) {
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

    public byte[] getDelAddress() {
        return delAddress;
    }

    public void setDelAddress(byte[] delAddress) {
        this.delAddress = delAddress;
    }

    public String getDelTxHash() {
        return delTxHash;
    }

    public void setDelTxHash(String delTxHash) {
        this.delTxHash = delTxHash;
    }

    public int getDelAssetId() {
        return delAssetId;
    }

    public void setDelAssetId(int delAssetId) {
        this.delAssetId = delAssetId;
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

    public String getAddressPrefix() {
        return addressPrefix;
    }

    public void setAddressPrefix(String addressPrefix) {
        this.addressPrefix = addressPrefix;
    }

    public int getMaxSignatureCount() {
        return maxSignatureCount;
    }

    public void setMaxSignatureCount(int maxSignatureCount) {
        this.maxSignatureCount = maxSignatureCount;
    }

    public void map2pojo(Map<String, Object> map) {
        this.setAddressType(String.valueOf(map.get("addressType")));
        this.setChainId(Integer.valueOf(map.get(Constants.CHAIN_ID).toString()));
        if(null == map.get("addressPrefix") || String.valueOf(map.get("addressPrefix")).equals("")){
            this.setAddressPrefix(AddressTool.getPrefix(this.getChainId()));
        }else {
            this.setAddressPrefix(String.valueOf(map.get("addressPrefix")).toUpperCase());
        }
        this.setChainName(String.valueOf(map.get("chainName")));
        this.setMagicNumber(Long.valueOf(map.get("magicNumber").toString()));
        this.setMinAvailableNodeNum(Integer.valueOf(map.get("minAvailableNodeNum").toString()));
        this.setRegAddress(AddressTool.getAddress(map.get("address").toString()));
        this.setCreateTime(NulsDateUtils.getCurrentTimeSeconds());
        String[] verifierList = String.valueOf(map.get("verifierList")).split(",");
        this.setVerifierList(Arrays.asList(verifierList));
        this.setSignatureByzantineRatio(Integer.valueOf(map.get("signatureByzantineRatio").toString()));
        this.setMaxSignatureCount(Integer.valueOf(map.get("maxSignatureCount").toString()));
    }
}
