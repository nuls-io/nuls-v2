package io.nuls.base.api.provider.account.facade;

import io.nuls.base.api.provider.BaseReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 17:50
 * @Description: 功能描述
 */
public class BackupAccountReq extends BaseReq {

    private String password;

    private String address;

    /**
     * key store save path
     */
    private String filePath;

    public BackupAccountReq(String password, String address, String filePath) {
        this.password = password;
        this.address = address;
        this.filePath = filePath;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
}
