package io.nuls.ledger.service;

import io.nuls.base.data.BlockHeader;

import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/12/4.
 */
public interface FreezeStateService {

    /**
     * 当收到一个确认区块时处理该逻辑
     *
     * @param blockHeader
     */
    public void syncBlock(BlockHeader blockHeader);
}
