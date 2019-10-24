package io.nuls.base.api.provider.crosschain;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.crosschain.facade.*;

import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-05-07 16:07
 * @Description: 功能描述
 */
public interface ChainManageProvider {


    /**
     * 在主网注册一条友链，使其可以实现跨链交易
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
     * 注销资产
     *
     * @param req
     * @return
     */
    Result<String> disableCrossAsset(DisableAssetReq req);

    /**
     * 增加链资产
     *
     * @param req
     * @return
     */
    Result<String> addCrossAsset(AddCrossAssetReq req);
    Result<String> addCrossLocalAsset(AddCrossLocalAssetReq req);

    /**
     * 获取注册了跨链交易的链的注册信息
     *
     * @param req
     * @return
     */
    Result<CrossChainRegisterInfo> getCrossChainInfo(GetCrossChainInfoReq req);

    Result<Map> getCrossChainsSimpleInfo();

    Result<CrossAssetRegisterInfo> getCrossAssetInfo(GetCrossAssetInfoReq req);


}
