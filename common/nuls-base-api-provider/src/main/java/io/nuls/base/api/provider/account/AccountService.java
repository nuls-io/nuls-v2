package io.nuls.base.api.provider.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.*;
import io.nuls.base.api.provider.transaction.facade.MultiSignTransferRes;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.core.log.Log;

import java.io.*;
import java.net.URLDecoder;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:29
 * @Description:
 *    Account Services
 *    account service
 */
public interface AccountService {

    /**
     * Create an account
     * create account
     * @param req
     * @return
     */
    Result<String> createAccount(CreateAccountReq req);

    /**
     * Backup account tokey store
     * backup account to key store
     * @param req
     * @return
     */
    Result<String> backupAccount(BackupAccountReq req);

    /**
     * Obtain accountkeyStore
     * get account's keystore
     * @param req
     * @return
     */
    Result<String> getAccountKeyStore(KeyStoreReq req);


    /**
     * Import account through private key
     * import account by private key
     * @param req
     * @return
     */
    Result<String> importAccountByPrivateKey(ImportAccountByPrivateKeyReq req);


    /**
     * adoptkey storeImport account
     * import account by key store
     * @param req
     * @return
     */
    Result<String> importAccountByKeyStore(ImportAccountByKeyStoreReq req);

    Result<String> importKeyStoreFiles(ImportKeyStoreFilesReq req);
    /**
     * Change account password
     * reset account password
     * @param req
     * @return
     */
    Result<Boolean> updatePassword(UpdatePasswordReq req);


    /**
     * Obtain account information based on address
     * get account info by address
     * @param req
     * @return
     */
    Result<AccountInfo> getAccountByAddress(GetAccountByAddressReq req);

    /**
     * Obtain multi signature account information
     * @param req
     * @return
     */
    Result<MultiSigAccount> getMultiSignAccount(GetMultiSignAccountByAddressReq req);

    /**
     * Get account list
     * get all account list
     * @return
     */
    Result<AccountInfo> getAccountList();


    /**
     * Delete specified account
     * remove account by address
     * @param req
     * @return
     */
    Result<Boolean> removeAccount(RemoveAccountReq req);

    /**
     * Query account private key
     * get account private key
     * @param req
     * @return
     */
    Result<String> getAccountPrivateKey(GetAccountPrivateKeyByAddressReq req);


    /**
     * Set account alias
     * set account alias
     * @param req
     * @return
     */
    Result<String> setAccountAlias(SetAccountAliasReq req);

    /**
     * Create a multi signature account
     * @param req
     * @return
     */
    Result<String> createMultiSignAccount(GenerateMultiSignAccountReq req);

    /**
     * Remove a multi signature account
     * @param req
     * @return
     */
    Result<Boolean> removeMultiSignAccount(RemoveMultiSignAccountReq req);

    /**
     * Setting aliases for multiple signed accounts
     * @param req
     * @return
     */
    Result<MultiSignTransferRes> setMultiSignAccountAlias(SetMultiSignAccountAliasReq req);

    /**
     * Obtain based on file addressAccountKeystoreDtoobject
     * Gets the AccountKeystoreDto object based on the file address
     * @param path
     * @return
     */
    default  String getAccountKeystoreDto(String path) {
        File file = null;
        try {
            file = new File(URLDecoder.decode(path, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            Log.error("no files found", e);
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
