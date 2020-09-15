package io.nuls.base.api.provider.crosschain.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2020/9/11 15:09
 * @Description: 重新对跨链交易进行拜赞庭验证
 */
public class RehandleCtxReq extends BaseReq {

    private String ctxHash;

    private long blockHeight;

    public RehandleCtxReq(String ctxHash, long blockHeight) {
        this.ctxHash = ctxHash;
        this.blockHeight = blockHeight;
    }

    public String getCtxHash() {
        return ctxHash;
    }

    public void setCtxHash(String ctxHash) {
        this.ctxHash = ctxHash;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }
}
