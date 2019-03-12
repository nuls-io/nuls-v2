package io.nuls.api.provider.network.facade;

import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-12 16:13
 * @Description:
 * 网络信息
 * network info
 */
@Data
public class NetworkInfo {

    /**
     * 本地最新区块高度
     */
    long localBestHeight;

    /**
     * 网络最新区块高度
     */
    long netBestHeight;

    /**
     * 网络时间偏移值 毫秒数
     */
    long timeOffset;
    /**
     * 被动连接节点数量
     */
    int inCount;
    /**
     * 主动连接节点数量
     */
    int outCount;

}
