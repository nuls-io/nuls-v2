package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class TokenTransfer extends TxDataInfo {

    private String txHash;

    private long height;

    private String contractAddress;

    private String name;

    private String symbol;

    private int decimals;

    private String fromAddress;

    private String toAddress;

    private String value;

    private Long time;

    private String fromBalance;

    private String toBalance;

}
