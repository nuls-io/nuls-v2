package io.nuls.api.provider.account;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.account.facade.BackupAccountReq;
import io.nuls.api.provider.account.facade.CreateAccountReq;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:29
 * @Description: 功能描述
 */
public interface AccountService {

    Result<String> createAccount(CreateAccountReq req);

    Result<Boolean> backupAccount(BackupAccountReq req);

}
