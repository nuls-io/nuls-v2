package io.nuls.crosschain.base.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.CoinFrom;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.crosschain.base.message.base.BaseMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 链注册信息
 * @author tag
 * @date 2019/5/17
 */
public class ChainInfo extends BaseMessage {
    private int chainId;
    private String chainName;
    private int minAvailableNodeNum;
    private int maxSignatureCount;
    private int signatureByzantineRatio;
    private List<AssetInfo> assetInfoList;
    private List<String> verifierList;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeString(chainName);
        stream.writeUint16(minAvailableNodeNum);
        stream.writeUint16(maxSignatureCount);
        stream.writeUint16(signatureByzantineRatio);
        int count = (assetInfoList == null || assetInfoList.size() ==0) ? 0 : assetInfoList.size();
        stream.writeVarInt(count);
        if(assetInfoList != null && assetInfoList.size() > 0){
            for (AssetInfo assetInfo:assetInfoList) {
                stream.writeNulsData(assetInfo);
            }
        }
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
        List<AssetInfo> assetInfoList = new ArrayList<>();
        int count = (int) byteBuffer.readVarInt();
        if(count > 0){
            for (int i = 0; i < count; i++) {
                assetInfoList.add(byteBuffer.readNulsData(new AssetInfo()));
            }
        }
        this.assetInfoList = assetInfoList;
        List<String> verifierList = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            verifierList.add(byteBuffer.readString());
        }
        this.verifierList = verifierList;
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfVarInt((assetInfoList == null || assetInfoList.size() ==0) ? 0 : assetInfoList.size());
        size += SerializeUtils.sizeOfUint16() * 4;
        size += SerializeUtils.sizeOfString(chainName);
        if (assetInfoList != null && assetInfoList.size() > 0) {
            for (AssetInfo assetInfo : assetInfoList) {
                size +=  SerializeUtils.sizeOfNulsData(assetInfo);
            }
        }
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

    public List<String> getVerifierList() {
        return verifierList;
    }

    public void setVerifierList(List<String> verifierList) {
        this.verifierList = verifierList;
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
}
