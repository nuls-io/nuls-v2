package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class AliasInfo extends TxDataInfo {

    private String address;

    private String alias;
}
