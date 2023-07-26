package io.nuls.ledger.model;

import io.nuls.ledger.storage.impl.RepositoryImpl;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.core.core.ioc.SpringLiteContext;

/**
 * 链信息类
 * Chain information class
 *
 * @author lanjinsheng
 * @date 2019/02/11
 **/
public class LedgerChain {

    int chainId;

    public LedgerChain(int chainId) {
        this.chainId = chainId;

    }
}
