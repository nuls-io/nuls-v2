package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 15:36
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class CreateAccountReq extends BaseReq {

    private int count;

    private String password;

    public CreateAccountReq(){
        this.count = 1;
    }


}
