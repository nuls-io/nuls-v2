package io.nuls.crosschain.srorage;

import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;

/**
 * 已注册跨链的交易数据库操作类
 * Registered Cross-Chain Transaction Database Operations Class
 *
 * @author  tag
 * 2019/5/30
 * */
public interface RegisteredCrossChainService {
    /**
     * 保存
     * @param   registeredChainMessage  已注册跨链的链列表
     * @return  保存是否成功
     * */
    boolean save(RegisteredChainMessage registeredChainMessage);

    /**
     * 查询
     * @return  已注册跨链的链信息
     * */
    RegisteredChainMessage get();

    /**
     * 判断指定资产是否可跨链交易
     * @param assetChainId
     * @param assetId
     * @return
     */
    boolean canCross(int assetChainId,int assetId);

}
