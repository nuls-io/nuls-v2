package io.nuls.api.provider.account;

import io.nuls.api.provider.Result;
import io.nuls.api.provider.account.facade.CreateAccountReq;
import io.nuls.api.provider.account.facade.CreateAccountRes;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:29
 * @Description: 功能描述
 */
public interface AccountService {

    String hello();

    Result<String> createAccount(CreateAccountReq req);

}
