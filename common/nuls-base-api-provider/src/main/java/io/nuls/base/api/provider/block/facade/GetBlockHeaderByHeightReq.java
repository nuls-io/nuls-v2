package io.nuls.base.api.provider.block.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:33
 * @Description:
 * Obtain block headers through block height
 * get block header by height
 */
public class GetBlockHeaderByHeightReq extends BaseReq {

    private Long height;

    public GetBlockHeaderByHeightReq(Long height) {
        this.height = height;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }
}
