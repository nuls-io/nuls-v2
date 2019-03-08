package io.nuls.api.provider.transaction;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.transaction.facade.TransferReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 16:18
 * @Description: 功能描述
 */
public interface TransferService {

    /**
     *  发起交易
     *  transfer
     * @param req
     * @return
     */
    Result<String> transfer(TransferReq req);

}
