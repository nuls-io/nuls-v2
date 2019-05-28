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
    private List<AssetInfo> assetInfoList;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeString(chainName);
        int count = (assetInfoList == null || assetInfoList.size() ==0) ? 0 : assetInfoList.size();
        stream.writeVarInt(count);
        if(assetInfoList != null && assetInfoList.size() > 0){
            for (AssetInfo assetInfo:assetInfoList) {
                stream.writeNulsData(assetInfo);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.chainName = byteBuffer.readString();
        List<AssetInfo> assetInfoList = new ArrayList<>();
        int count = (int) byteBuffer.readVarInt();
        if(count > 0){
            for (int i = 0; i < count; i++) {
                assetInfoList.add(byteBuffer.readNulsData(new AssetInfo()));
            }
        }
        this.assetInfoList = assetInfoList;
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfVarInt((assetInfoList == null || assetInfoList.size() ==0) ? 0 : assetInfoList.size());
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(chainName);
        if (assetInfoList != null && assetInfoList.size() > 0) {
            for (AssetInfo assetInfo : assetInfoList) {
                size +=  SerializeUtils.sizeOfNulsData(assetInfo);
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
