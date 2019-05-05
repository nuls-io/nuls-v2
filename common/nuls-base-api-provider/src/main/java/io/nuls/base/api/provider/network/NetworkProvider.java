package io.nuls.base.api.provider.network;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.network.facade.NetworkInfo;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 16:11
 * @Description: 功能描述
 */
public interface NetworkProvider {

    /**
     * get network info
     * @return
     */
    Result<NetworkInfo> getInfo();

    /**
     * get network nodes
     * @return
     */
    Result<String> getNodes();

}
