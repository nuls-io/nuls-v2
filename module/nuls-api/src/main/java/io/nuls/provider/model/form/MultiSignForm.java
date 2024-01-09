package io.nuls.provider.model.form;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;
import io.nuls.v2.model.dto.SignDto;

import java.util.List;

@ApiModel(description = "Multi account signature form")
public class MultiSignForm {

    @ApiModelProperty(description = "keystoreaggregate", required = true, type = @TypeDescriptor(value = List.class, collectionElement = SignDto.class))
    private List<SignDto> dtoList;
    @ApiModelProperty(description = "Transaction serializationHexcharacter string")
    private String txHex;

    public List<SignDto> getDtoList() {
        return dtoList;
    }

    public void setDtoList(List<SignDto> dtoList) {
        this.dtoList = dtoList;
    }

    public String getTxHex() {
        return txHex;
    }

    public void setTxHex(String txHex) {
        this.txHex = txHex;
    }
}
