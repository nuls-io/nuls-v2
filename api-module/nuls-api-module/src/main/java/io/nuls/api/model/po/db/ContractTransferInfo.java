package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class ContractTransferInfo extends TxDataInfo {

    private String txHash;

    private String contractAddress;

    private String orginTxHash;

    private String fromAddress;

    private String toAddress;

    private Long txValue;

    private Long createTime;
}