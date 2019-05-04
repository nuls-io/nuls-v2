package io.nuls.test.cases.account;

import io.nuls.test.cases.TestCase;
import io.nuls.test.cases.TestCaseChain;
import io.nuls.test.cases.TestCaseIntf;
import io.nuls.core.core.annotation.Component;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 10:30
 * @Description:
 * 账户操作指令集成测试
 * 测试流程如下
 * 0.创建一个账户。
 * 1.通过1中的地址查询账户
 * 2.备份此账户到{user.dir}目录
 * 3.删除这个账户
 * 4.通过【2】中的keystore导入账户
 * 5.通过address和密码查询这个账户的私钥
 * 6.删除账户，通过私钥再次导入账户
 * 7.修改账户密码
 * 8.查询账户列表，验证列表中是否有次账户
 * 9.删除此账户，删除【2】中的keystore
 */
@TestCase("account")
@Component
public class AccountCase extends TestCaseChain {

    @Override
    public Class<? extends TestCaseIntf>[] testChain() {
        return new Class[]{
                CreateAccountCase.class,
                GetAccountByAddressCase.class,
                GetAddressByAccountInfoAdapder.class,
                BackupAccountCase.class,
                RemoveAccountCase.class,
                ImportAccountKeystoreCase.class,
                GetAccountPriKeyCase.class,
                ImportAccountByPriKeyCase.class,
                UpdatePasswordCase.class,
                GetAccountListCase.class
        };
    }

    @Override
    public String title() {
        return "账户模块集成测试";
    }

}
