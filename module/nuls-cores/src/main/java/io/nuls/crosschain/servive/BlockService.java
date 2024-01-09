package io.nuls.crosschain.servive;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * Provide interfaces for block module calls
 * @author tag
 * @date 2019/4/25
 */
public interface BlockService {
    /**
     * Receive the latest block height
     * @param params  parameter
     * @return        Message processing results
     * */
    Result newBlockHeight(Map<String,Object> params);

    /**
     * Node synchronization status change
     * Node synchronization state change
     * @param params  parameter
     * @return        Message processing results
     * */
    Result syncStatusUpdate(Map<String,Object> params);
}
