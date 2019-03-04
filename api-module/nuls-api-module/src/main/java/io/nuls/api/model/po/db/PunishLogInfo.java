package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class PunishLogInfo extends TxDataInfo {

    private String txHash;

    private Integer type;

    private String address;

    private Long time;

    private Long blockHeight;

    private Long roundIndex;

    private int index;

    private String reason;


}