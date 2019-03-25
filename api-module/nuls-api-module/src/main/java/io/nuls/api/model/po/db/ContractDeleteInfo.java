package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class ContractDeleteInfo extends TxDataInfo {

    private String txHash;

    private String contractAddress;

    private String creater;

    private ContractResultInfo resultInfo;

}