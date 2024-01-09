package io.nuls.base.api.provider.block.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-11 09:33
 * @Description:
 * adopthashGet block header
 * get block header by hash
 */
public class GetBlockHeaderByHashReq extends BaseReq {

    private String hash;

    public GetBlockHeaderByHashReq(String hash) {
        this.hash = hash;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
