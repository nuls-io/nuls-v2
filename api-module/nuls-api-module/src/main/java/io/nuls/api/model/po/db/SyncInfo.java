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

    private int step;

    public boolean isFinish() {
        return this.step == 100;
    }
}
