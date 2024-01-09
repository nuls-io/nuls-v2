package io.nuls.consensus.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2019/04/01
 * */
public interface BlockService {
    /**
     * Verify block correctness
     * @param params
     * @return Result
     * */
    Result validBlock(Map<String,Object> params);

    /**
     * Cache the latest block
     * @param params
     * @return Result
     * */
    Result addBlock(Map<String,Object> params);

    /**
     * Receive blocks that require caching
     * @param params
     * @return Result
     * */
    Result receiveHeaderList(Map<String,Object> params);

    /**
     * Rolling back forked blocks
     * @param params
     * @return Result
     * */
    Result chainRollBack(Map<String,Object> params);
}
