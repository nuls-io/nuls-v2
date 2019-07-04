package io.nuls.base.api.provider.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.*;
import io.nuls.core.log.Log;

import java.io.*;
import java.net.URLDecoder;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:29
 * @Description:
 *    账户服务
 *    account service
 */
public interface AccountService {

    /**
     * 创建账户
     * create account
     * @param req
     * @return
     */
    Result<String> createAccount(CreateAccountReq req);

    /**
     * 备份账户到key store
     * backup account to key store
     * @param req
     * @return
     */
    Result<String> backupAccount(BackupAccountReq req);

    /**
     * 获取账户keyStore
     * get account's keystore
     * @param req
     * @return
     */
    Result<String> getAccountKeyStore(KeyStoreReq req);


    /**
     * 通过私钥导入账户
     * import account by private key
     * @param req
     * @return
     */
    Result<String> importAccountByPrivateKey(ImportAccountByPrivateKeyReq req);


    /**
     * 通过key store导入账户
     * import account by key store
     * @param req
     * @return
     */
    Result<String> importAccountByKeyStore(ImportAccountByKeyStoreReq req);

    /**
     * 修改账户密码
     * reset account password
     * @param req
     * @return
     */
    Result<Boolean> updatePassword(UpdatePasswordReq req);


    /**
     * 根据地址获取账户信息
     * get account info by address
     * @param req
     * @return
     */
    Result<AccountInfo> getAccountByAddress(GetAccountByAddressReq req);

    /**
     * 获取账户列表
     * get all account list
     * @return
     */
    Result<AccountInfo> getAccountList();


    /**
     * 删除指定账户
     * remove account by address
     * @param req
     * @return
     */
    Result<Boolean> removeAccount(RemoveAccountReq req);

    /**
     * 查询账户私钥
     * get account private key
     * @param req
     * @return
     */
    Result<String> getAccountPrivateKey(GetAccountPrivateKeyByAddressReq req);


    /**
     * 设置账户别名
     * set account alias
     * @param req
     * @return
     */
    Result<String> setAccountAlias(SetAccountAliasReq req);


    /**
     * 根据文件地址获取AccountKeystoreDto对象
     * Gets the AccountKeystoreDto object based on the file address
     * @param path
     * @return
     */
    default  String getAccountKeystoreDto(String path) {
        File file = null;
        try {
            file = new File(URLDecoder.decode(path, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.error("未找到文件", e);
        }
        if (null != file && file.isFile()) {
            StringBuilder ks = new StringBuilder();
            BufferedReader bufferedReader = null;
            String str;
            try {
                bufferedReader = new BufferedReader(new FileReader(file));
                while ((str = bufferedReader.readLine()) != null) {
                    if (!str.isEmpty()) {
                        ks.append(str);
                    }
                }
                return ks.toString();
            } catch (Exception e) {
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        Log.error("system error", e);
                    }
                }
            }
        }
        return null;
    }

}
