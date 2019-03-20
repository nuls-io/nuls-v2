package io.nuls.test.cases.block;

import io.nuls.api.provider.block.facade.BlockHeaderData;
import io.nuls.test.cases.AbstractRemoteTestCase;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 20:09
 * @Description: 功能描述
 */
public abstract class BlockAbstractRemoteTestCase<T extends BlockHeaderData> extends AbstractRemoteTestCase<T> {

    @Override
    public boolean equals(BlockHeaderData source, BlockHeaderData remote){
        source.setTime(null);
        source.setRoundStartTime(null);
        remote.setTime(null);
        remote.setRoundStartTime(null);
        return source.equals(remote);
    }


}
