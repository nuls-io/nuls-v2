package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.v2.model.dto.SignDto;

import java.util.List;

@ApiModel(description = "单账户签名表单")
public class EncryptedPriKeysSignForm {

    @ApiModelProperty(description = "交易序列化Hex字符串")
    private String txHex;
    @ApiModelProperty(description = "地址链ID")
    private int chainId;
    @ApiModelProperty(description = "地址前缀")
    private String prefix;

    private List<SignDto> signDtoList;

    public String getTxHex() {
        return txHex;
    }

    public void setTxHex(String txHex) {
        this.txHex = txHex;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public List<SignDto> getSignDtoList() {
        return signDtoList;
    }

    public void setSignDtoList(List<SignDto> signDtoList) {
        this.signDtoList = signDtoList;
    }
}
