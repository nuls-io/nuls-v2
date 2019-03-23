package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class ContractCallInfo extends TxDataInfo {

    private String contractAddress;

    private String createTxHash;

    private String creater;

    private Long gasLimit;

    private Long price;

    private String methodName;

    private String methodDesc;

    private String args;

    private ContractResultInfo resultInfo;

}
