package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.v2.model.dto.SignDto;

import java.util.List;

@ApiModel(description = "Single account signature form")
public class EncryptedPriKeysSignForm {

    @ApiModelProperty(description = "Transaction serializationHexcharacter string")
    private String txHex;
    @ApiModelProperty(description = "Address chainID")
    private int chainId;
    @ApiModelProperty(description = "Address prefix")
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
