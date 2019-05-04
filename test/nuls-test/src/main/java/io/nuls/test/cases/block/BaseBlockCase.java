package io.nuls.test.cases.block;

import io.nuls.api.provider.ServiceManager;
import io.nuls.api.provider.block.BlockService;
import io.nuls.test.cases.BaseTestCase;
import io.nuls.test.cases.TestCaseIntf;


public abstract class BaseBlockCase<T,P> extends BaseTestCase<T,P> {

    BlockService blockService = ServiceManager.get(BlockService.class);

}
