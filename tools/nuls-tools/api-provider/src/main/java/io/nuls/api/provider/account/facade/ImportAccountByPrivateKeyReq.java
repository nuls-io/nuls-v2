package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 20:32
 * @Description:
 *  通过私钥导入账户
 *  import account by private key
 */
@Data
@AllArgsConstructor
public class ImportAccountByPrivateKeyReq extends BaseReq {

    private String password;

    private String priKey;

    private boolean overwrite;

}
