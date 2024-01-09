package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 20:44
 * @Description:
 *   adoptkey storeImport account
 *   import account by key store
 */
public class ImportKeyStoreFilesReq extends BaseReq {

    private String dirPath;

    public String getDirPath() {
        return dirPath;
    }

    public void setDirPath(String dirPath) {
        this.dirPath = dirPath;
    }
}
