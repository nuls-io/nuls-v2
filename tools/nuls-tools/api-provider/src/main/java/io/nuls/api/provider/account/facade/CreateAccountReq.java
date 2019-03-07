package io.nuls.api.provider.account.facade;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 15:36
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class CreateAccountReq {

    private int chainId;

    private int count;

    private String password;

    public CreateAccountReq(int chainId){
        this.chainId = chainId;
        this.count = 1;
    }

}
