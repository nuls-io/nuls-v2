package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 20:44
 * @Description:
 *   通过key store导入账户
 *   import account by key store
 */
public class ImportAccountByKeyStoreReq extends BaseReq {

    private String password;

    private String keyStore;

    private boolean overwrite;

    public ImportAccountByKeyStoreReq(String password, String keyStore, boolean overwrite) {
        this.password = password;
        this.keyStore = keyStore;
        this.overwrite = overwrite;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public boolean isOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
}
