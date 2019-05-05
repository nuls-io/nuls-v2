package io.nuls.test.cases.block;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.block.BlockService;
import io.nuls.test.cases.BaseTestCase;


public abstract class BaseBlockCase<T,P> extends BaseTestCase<T,P> {

    BlockService blockService = ServiceManager.get(BlockService.class);

}
