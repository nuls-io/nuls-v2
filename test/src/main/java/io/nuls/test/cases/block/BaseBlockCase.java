package io.nuls.test.cases.block;

import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.block.BlockService;
import io.nuls.test.cases.TestCaseIntf;


public abstract class BaseBlockCase<T,P> implements TestCaseIntf<T,P> {

    BlockService blockService = ServiceManager.get(BlockService.class);

}
