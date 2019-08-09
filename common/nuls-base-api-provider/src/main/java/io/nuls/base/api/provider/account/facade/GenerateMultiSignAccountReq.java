package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-07-18 14:07
 * @Description: 创建多签账户
 */
public class GenerateMultiSignAccountReq extends BaseReq {

    /**
     * 公钥集合(任意普通地址的公钥或存在于当前节点中的普通账户地址)
     */
    private List<String> pubKeys;

    /**
     * 最小签名数
     */
    private int minSigns;

    public List<String> getPubKeys() {
        return pubKeys;
    }

    public void setPubKeys(List<String> pubKeys) {
        this.pubKeys = pubKeys;
    }

    public int getMinSigns() {
        return minSigns;
    }

    public void setMinSigns(int minSigns) {
        this.minSigns = minSigns;
    }
}
