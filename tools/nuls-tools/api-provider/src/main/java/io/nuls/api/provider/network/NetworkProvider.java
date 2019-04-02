package io.nuls.api.provider.network;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.network.facade.NetworkInfo;

import java.util.List;

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
