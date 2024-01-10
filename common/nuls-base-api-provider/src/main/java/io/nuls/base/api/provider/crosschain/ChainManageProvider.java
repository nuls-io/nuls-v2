package io.nuls.base.api.provider.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.*;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 16:07
 * @Description: Function Description
 */
public interface ChainManageProvider {


    /**
     * Register a friend chain on the main network to enable cross chain transactions
     *
     * @param req
     * @return
     */
    Result<Map> registerChain(RegisterChainReq req);

    /**
     * @param req
     * @return
     */
    Result<Map> updateChain(RegisterChainReq req);

    /**
     * Cancellation of assets
     *
     * @param req
     * @return
     */
    Result<String> disableCrossAsset(DisableAssetReq req);

    /**
     * Increase chain assets
     *
     * @param req
     * @return
     */
    Result<String> addCrossAsset(AddCrossAssetReq req);
    Result<String> addCrossLocalAsset(AddCrossLocalAssetReq req);

    /**
     * Obtain registration information for chains registered for cross chain transactions
     *
     * @param req
     * @return
     */
    Result<CrossChainRegisterInfo> getCrossChainInfo(GetCrossChainInfoReq req);

    Result<Map> getCrossChainsSimpleInfo();

    Result<CrossAssetRegisterInfo> getCrossAssetInfo(GetCrossAssetInfoReq req);


}
