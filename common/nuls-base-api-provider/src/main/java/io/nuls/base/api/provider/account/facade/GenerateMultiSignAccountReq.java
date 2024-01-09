package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-07-18 14:07
 * @Description: Create a multi signature account
 */
public class GenerateMultiSignAccountReq extends BaseReq {

    /**
     * Public key set(Public key of any ordinary address or ordinary account address existing in the current node)
     */
    private List<String> pubKeys;

    /**
     * Minimum number of signatures
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
