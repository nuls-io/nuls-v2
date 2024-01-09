package io.nuls.crosschain.base.model.bo;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.core.rpc.util.SerializeUtil;
import io.nuls.crosschain.base.constant.CrossChainConstant;
import io.nuls.crosschain.base.message.base.BaseMessage;

import java.io.IOException;
import java.util.*;

/**
 * 链注册信息
 * @author tag
 * @date 2019/5/17
 */
@ApiModel
public class ChainInfo extends BaseMessage {
    @ApiModelProperty(description = "链ID")
    private int chainId;
    @ApiModelProperty(description = "链名称")
    private String chainName;
    @ApiModelProperty(description = "最小链接数")
    private int minAvailableNodeNum;
    @ApiModelProperty(description = "最大签名数")
    private int maxSignatureCount;
    @ApiModelProperty(description = "签名拜占庭比例")
    private int signatureByzantineRatio;
    @ApiModelProperty(description = "链账户前缀")
    private String addressPrefix;
    @ApiModelProperty(description = "链资产列表", type = @TypeDescriptor(value = List.class, collectionElement = AssetInfo.class))
    private List<AssetInfo> assetInfoList;
    @ApiModelProperty(description = "验证人列表")
    private Set<String> verifierList;
    @ApiModelProperty(description = "注册时间")
    private long registerTime;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeString(chainName);
        stream.writeUint16(minAvailableNodeNum);
        stream.writeUint16(maxSignatureCount);
        stream.writeUint16(signatureByzantineRatio);
        stream.writeString(addressPrefix);
        stream.writeUint32(registerTime);
        int count = (assetInfoList == null || assetInfoList.size() ==0) ? 0 : assetInfoList.size();
        stream.writeVarInt(count);
        if(assetInfoList != null && assetInfoList.size() > 0){
            for (AssetInfo assetInfo:assetInfoList) {
                stream.writeNulsData(assetInfo);
            }
        }
        int verifierCount = (verifierList == null || verifierList.size() ==0) ? 0 : verifierList.size();
        stream.writeVarInt(verifierCount);
        if(verifierList != null && verifierList.size() > 0){
            for (String verifier:verifierList) {
                stream.writeString(verifier);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.chainName = byteBuffer.readString();
        this.minAvailableNodeNum = byteBuffer.readUint16();
        this.maxSignatureCount = byteBuffer.readUint16();
        this.signatureByzantineRatio = byteBuffer.readUint16();
        this.addressPrefix = byteBuffer.readString();
        this.registerTime = byteBuffer.readUint32();
        List<AssetInfo> assetInfoList = new ArrayList<>();
        int count = (int) byteBuffer.readVarInt();
        if(count > 0){
            for (int i = 0; i < count; i++) {
                assetInfoList.add(byteBuffer.readNulsData(new AssetInfo()));
            }
        }
        this.assetInfoList = assetInfoList;

        int verifierCount = (int) byteBuffer.readVarInt();
        Set<String> verifierList = new HashSet<>();
        if(verifierCount > 0){
            for (int i = 0; i < verifierCount; i++) {
                verifierList.add(byteBuffer.readString());
            }
        }
        this.verifierList = verifierList;
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfVarInt((assetInfoList == null || assetInfoList.size() ==0) ? 0 : assetInfoList.size());
        size += SerializeUtils.sizeOfUint16() * 4;
        size += SerializeUtils.sizeOfString(chainName);
        size += SerializeUtils.sizeOfString(addressPrefix);
        size += SerializeUtils.sizeOfUint32();
        if (assetInfoList != null && assetInfoList.size() > 0) {
            for (AssetInfo assetInfo : assetInfoList) {
                size +=  SerializeUtils.sizeOfNulsData(assetInfo);
            }
        }
        size += SerializeUtils.sizeOfVarInt((verifierList == null || verifierList.size() ==0) ? 0 : verifierList.size());
        if(verifierList != null && !verifierList.isEmpty()){
            for (String verifier:verifierList) {
                size += SerializeUtils.sizeOfString(verifier);
            }
        }
        return size;
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

    public List<AssetInfo> getAssetInfoList() {
        return assetInfoList;
    }

    public void setAssetInfoList(List<AssetInfo> assetInfoList) {
        this.assetInfoList = assetInfoList;
    }

    public int getMinAvailableNodeNum() {
        return minAvailableNodeNum;
    }

    public void setMinAvailableNodeNum(int minAvailableNodeNum) {
        this.minAvailableNodeNum = minAvailableNodeNum;
    }

    public int getMaxSignatureCount() {
        return maxSignatureCount;
    }

    public void setMaxSignatureCount(int maxSignatureCount) {
        this.maxSignatureCount = maxSignatureCount;
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

    public Set<String> getVerifierList() {
        return verifierList;
    }

    public void setVerifierList(Set<String> verifierList) {
        this.verifierList = verifierList;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(long registerTime) {
        this.registerTime = registerTime;
    }

    public int getMinPassCount(){
        int minPassCount = getVerifierList().size() * getSignatureByzantineRatio()/ CrossChainConstant.MAGIC_NUM_100;
        if(minPassCount == 0){
            minPassCount = 1;
        }
        return minPassCount;
    }

    public int getMinPassCount(int count){
        int minPassCount = count * getSignatureByzantineRatio()/ CrossChainConstant.MAGIC_NUM_100;
        if(minPassCount == 0){
            minPassCount = 1;
        }
        return minPassCount;
    }

    public boolean verifyAssetAvailability(int chainId, int assetId) {
        if (chainId != this.chainId) {
            return false;
        }
        for (AssetInfo assetInfo : assetInfoList) {
            if (assetInfo.getAssetId() == assetId && assetInfo.isUsable()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if(this == obj){
            return true;
        }

        if(obj == null){
            return false;
        }

        if(obj instanceof ChainInfo){
            return this.chainId == ((ChainInfo) obj).getChainId();
        }
        return false;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + this.chainId;
        return result;
    }
}
