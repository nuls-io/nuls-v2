package io.nuls.crosschain.base.model.bo;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.crosschain.base.message.base.BaseMessage;

import java.io.IOException;

/**
 * 资产注册信息
 * @author tag
 * @date 2019/5/17
 */
@ApiModel
public class AssetInfo extends BaseMessage {
    @ApiModelProperty(description = "资产ID")
    private int assetId;
    @ApiModelProperty(description = "资产符号")
    private String symbol;
    @ApiModelProperty(description = "资产名称")
    private String assetName;
    @ApiModelProperty(description = "是否可用")
    private boolean usable;
    @ApiModelProperty(description = "精度")
    private int decimalPlaces;

    public AssetInfo(){}

    public AssetInfo(int assetId,String symbol,String assetName,boolean usable,int decimalPlaces){
        this.assetId = assetId;
        this.symbol = symbol;
        this.assetName = assetName;
        this.usable = usable;
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(assetId);
        stream.writeString(symbol);
        stream.writeString(assetName);
        stream.writeBoolean(usable);
        stream.writeUint16(decimalPlaces);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.assetId = byteBuffer.readUint16();
        this.symbol = byteBuffer.readString();
        this.assetName = byteBuffer.readString();
        this.usable = byteBuffer.readBoolean();
        this.decimalPlaces = byteBuffer.readUint16();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfString(symbol);
        size += SerializeUtils.sizeOfString(assetName);
        size += SerializeUtils.sizeOfBoolean();
        size += SerializeUtils.sizeOfUint16();
        return size;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
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

    public boolean isUsable() {
        return usable;
    }

    public void setUsable(boolean usable) {
        this.usable = usable;
    }

    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }
}
