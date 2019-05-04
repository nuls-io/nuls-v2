package io.nuls.poc.service;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * @author tag
 * 2019/04/01
 * */
public interface BlockService {
    /**
     * 验证区块正确性
     * @param params
     * @return Result
     * */
    Result validBlock(Map<String,Object> params);

    /**
     * 缓存最新区块
     * @param params
     * @return Result
     * */
    Result addBlock(Map<String,Object> params);

    /**
     * 接收需缓存的区块
     * @param params
     * @return Result
     * */
    Result receiveHeaderList(Map<String,Object> params);

    /**
     * 连分叉区块回滚
     * @param params
     * @return Result
     * */
    Result chainRollBack(Map<String,Object> params);
}
