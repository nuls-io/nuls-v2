package io.nuls.test.cases.transcation;

import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.test.Config;
import io.nuls.test.cases.BaseTestCase;
import io.nuls.core.core.annotation.Autowired;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 10:13
 * @Description: 功能描述
 */
public abstract class BaseTranscationCase<T,P> extends BaseTestCase<T,P> {


    protected TransferService transferService = ServiceManager.get(TransferService.class);

    @Autowired protected Config config;

}
