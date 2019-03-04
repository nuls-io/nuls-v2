package io.nuls.api.model.po.db;

import lombok.Data;

@Data
public class SyncInfo {

    private int chainId;

    private long bestHeight;

    private boolean isFinish;

    private int step;
}
