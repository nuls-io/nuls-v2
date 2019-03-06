package io.nuls.api.model.po.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncInfo {

    private int chainId;

    private long bestHeight;

    private boolean isFinish;

    private int step;
}
