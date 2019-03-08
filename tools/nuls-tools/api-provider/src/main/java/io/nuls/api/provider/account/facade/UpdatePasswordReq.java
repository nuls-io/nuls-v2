package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-08 11:57
 * @Description:
 * 修改密码
 * reset account password
 */
@Data
@AllArgsConstructor
public class UpdatePasswordReq extends BaseReq {

    private String address;

    private String password;

    private String newPassword;

}


