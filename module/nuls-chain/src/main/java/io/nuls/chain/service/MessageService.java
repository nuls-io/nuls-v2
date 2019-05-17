package io.nuls.chain.service;

import io.nuls.chain.model.dto.ChainAssetTotalCirculate;

import java.util.List;

/**
 *消息协议服务接口
 *Message protocol service interface
 *
 * @author lanjinsheng
 * @date 2018/12/04
 */
public interface MessageService {
    /**
     *
     * @param chainId
     * @return
     */
    boolean initChainIssuingAssets(int chainId);

    /**
     *
     * @param chainId
     * @param chainAssetTotalCirculates
     */
    void recChainIssuingAssets(int chainId,List<ChainAssetTotalCirculate> chainAssetTotalCirculates);

    /**
     *
     * @param chainId
     */
    void dealChainIssuingAssets(int chainId);
}
