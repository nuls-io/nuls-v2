package io.nuls.provider.model.form;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 15:11
 * @Description: Function Description
 */
public class Base {

    @JsonIgnore
    private Integer chainId;

    public Integer getChainId() {
        return chainId;
    }

    public void setChainId(Integer chainId) {
        this.chainId = chainId;
    }
}
