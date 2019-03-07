package io.nuls.api.provider.account.facade;

import io.nuls.api.provider.BaseReq;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-07 17:50
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class BackupAccountReq extends BaseReq {

    private String password;

    private String address;

    /**
     * key store save path
     */
    private String filePath;

}
