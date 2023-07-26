package io.nuls.block.storage.impl;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.block.constant.Constant;
import io.nuls.block.model.RollbackInfoPo;
import io.nuls.block.storage.RollbackStorageService;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import static io.nuls.block.utils.LoggerUtil.COMMON_LOG;

@Component
public class RollbackServiceImpl implements RollbackStorageService {
    public boolean save(RollbackInfoPo po, int chainId) {
        byte[] bytes;
        try {
            bytes = po.serialize();
            return RocksDBService.put(Constant.ROLLBACK_HEIGHT, ByteUtils.intToBytes(chainId), bytes);
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return false;
        }
    }

    public RollbackInfoPo get(int chainId) {
        try {
            RollbackInfoPo po = new RollbackInfoPo();
            byte[] bytes = RocksDBService.get(Constant.ROLLBACK_HEIGHT, ByteUtils.intToBytes(chainId));
            if(bytes == null){
                return null;
            }
            po.parse(new NulsByteBuffer(bytes));
            return po;
        } catch (Exception e) {
            COMMON_LOG.error("", e);
            return null;
        }
    }
}
