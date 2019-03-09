package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class PunishLogInfo extends TxDataInfo {

    private String txHash;

    private int type;

    private String address;

    private long time;

    private long blockHeight;

    private long roundIndex;

    private int index;

    private String reason;


}