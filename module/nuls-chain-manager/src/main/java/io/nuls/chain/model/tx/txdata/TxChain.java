package io.nuls.chain.model.tx.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
public class TxChain extends BaseNulsData {
    private String name;
    private String addressType;
    private long magicNumber;
    private boolean supportInflowAsset = true;
    private int minAvailableNodeNum = 1;
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

    /**
     * 下面这些是创建链的时候，必须携带的资产信息
     */
    private TxAsset defaultAsset = new TxAsset();


    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(name);
        stream.writeString(addressType);
        stream.writeUint32(magicNumber);
        stream.writeBoolean(supportInflowAsset);
        stream.writeUint32(minAvailableNodeNum);
        stream.writeNulsData(defaultAsset);
        stream.writeUint16(verifierList.size());
        for (String verifier : verifierList) {
            stream.writeString(verifier);
        }
        stream.writeUint16(signatureByzantineRatio);
        stream.writeUint16(maxSignatureCount);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.name = byteBuffer.readString();
        this.addressType = byteBuffer.readString();
        this.magicNumber = byteBuffer.readUint32();
        this.supportInflowAsset = byteBuffer.readBoolean();
        this.minAvailableNodeNum = byteBuffer.readInt32();
        int verifierCount = byteBuffer.readUint16();
        for (int i = 0; i < verifierCount; i++) {
            String verifier = byteBuffer.readString();
            this.verifierList.add(verifier);
        }
        this.signatureByzantineRatio = byteBuffer.readUint16();
        this.maxSignatureCount = byteBuffer.readUint16();
        this.defaultAsset = byteBuffer.readNulsData(new TxAsset());
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(name);
        size += SerializeUtils.sizeOfString(addressType);
        // magicNumber;
        size += SerializeUtils.sizeOfUint32();
        // supportInflowAsset;
        size += SerializeUtils.sizeOfBoolean();
        // minAvailableNodeNum;
        size += SerializeUtils.sizeOfInt32();
        //verifierList
        size += SerializeUtils.sizeOfUint16();
        for (String verifier : verifierList) {
            size += SerializeUtils.sizeOfString(verifier);
        }
        //signatureByzantineRatio
        size += SerializeUtils.sizeOfUint16();
        //maxSignatureCount
        size += SerializeUtils.sizeOfUint16();
        //assetTx
        size += SerializeUtils.sizeOfNulsData(defaultAsset);
        return size;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public TxAsset getDefaultAsset() {
        return defaultAsset;
    }

    public void setDefaultAsset(TxAsset defaultAsset) {
        this.defaultAsset = defaultAsset;
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
}
