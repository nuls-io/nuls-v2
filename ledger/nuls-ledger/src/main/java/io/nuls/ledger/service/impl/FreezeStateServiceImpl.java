package io.nuls.ledger.service.impl;

import io.nuls.base.data.BlockHeader;
import io.nuls.ledger.model.AccountState;
import io.nuls.ledger.model.FreezeHeightState;
import io.nuls.ledger.model.FreezeLockTimeState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.thread.TimeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.List;

/**
 * Created by wangkun23 on 2018/12/4.
 */
@Service
public class FreezeStateServiceImpl implements FreezeStateService {

    final Logger logger = LoggerFactory.getLogger(getClass());


    @Autowired
    AccountStateService accountStateService;

    /**
     * 当收到一个确认区块时处理该逻辑
     *
     * @param blockHeader
     */
    @Override
    public void syncBlock(BlockHeader blockHeader) {
        //TODO.. 重新用一张表结构存储存储高度锁定的数据
        //用一张表存储时间锁定的数据
    }
}
