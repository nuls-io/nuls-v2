package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 20:44
 * @Description:
 *   通过key store导入账户
 *   import account by key store
 */
@Data
@AllArgsConstructor
public class ImportAccountByKeyStoreReq extends BaseReq {

    private String password;

    private String keyStore;

    private boolean overwrite;

}
