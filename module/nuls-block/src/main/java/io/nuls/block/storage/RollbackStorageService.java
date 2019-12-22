package io.nuls.block.storage;

import io.nuls.block.model.RollbackInfoPo;

public interface RollbackStorageService {
    public boolean save(RollbackInfoPo po, int chainId);

    public RollbackInfoPo get(int chainId);
}
