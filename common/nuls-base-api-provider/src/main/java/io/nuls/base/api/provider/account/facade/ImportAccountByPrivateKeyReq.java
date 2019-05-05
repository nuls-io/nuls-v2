package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 20:32
 * @Description:
 *  通过私钥导入账户
 *  import account by private key
 */
public class ImportAccountByPrivateKeyReq extends BaseReq {

    private String password;

    private String priKey;

    private boolean overwrite;

    public ImportAccountByPrivateKeyReq(String password, String priKey, boolean overwrite) {
        this.password = password;
        this.priKey = priKey;
        this.overwrite = overwrite;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPriKey() {
        return priKey;
    }

    public void setPriKey(String priKey) {
        this.priKey = priKey;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
