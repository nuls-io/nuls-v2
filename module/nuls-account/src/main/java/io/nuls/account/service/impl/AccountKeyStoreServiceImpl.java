/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.account.service.impl;

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.AccountKeyStore;
import io.nuls.account.model.dto.AccountKeyStoreDTO;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rockdb.util.DBUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @author: qinyifeng
 */
@Component
public class AccountKeyStoreServiceImpl implements AccountKeyStoreService {

    @Autowired
    private AccountService accountService;

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AliasService aliasService;

    @Override
    public String backupAccountToKeyStore(String path, int chainId, String address, String password) {
        //export account to keystore
        AccountKeyStore accountKeyStore = this.accountToKeyStore(chainId, address, password);
        //backup keystore files
        String backupPath = this.backUpKeyStore(path, new AccountKeyStoreDTO(accountKeyStore));
        return backupPath;
    }

    @Override
    public AccountKeyStore getKeyStore(int chainId, String address, String password) {
        AccountKeyStore accountKeyStore = this.accountToKeyStore(chainId, address, password);
        return accountKeyStore;
    }

    /**
     * 账户转为keystore
     *
     * @param chainId
     * @param address
     * @param password
     * @return
     */
    public AccountKeyStore accountToKeyStore(int chainId, String address, String password) {
        //check params
        if (!AddressTool.validAddress(chainId, address)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        //check the account is exist
        Account account = accountService.getAccount(chainId, address);
        if (null == account) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        AccountKeyStore accountKeyStore = new AccountKeyStore();
        //验证密码
        //verify the password
        if (!account.validatePassword(password)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        //如果账户加密,不导出明文私钥
        //if the account is encrypted , the plaintext private key is not exported
        if (account.isEncrypted()) {
            accountKeyStore.setEncryptedPrivateKey(HexUtil.encode(account.getEncryptedPriKey()));
        } else {
            accountKeyStore.setPrikey(account.getPriKey());
        }
        accountKeyStore.setAddress(account.getAddress().toString());
        accountKeyStore.setPubKey(account.getPubKey());
        return accountKeyStore;
    }

    /**
     * 备份keystore文件
     * backup keystore file
     */
    public String backUpKeyStore(String path, AccountKeyStoreDTO accountKeyStoreDto) {
        //如果备份地址为空，则使用系统默认备份地址
        //if the backup address is empty, the default backup address of the system is used
        if (StringUtils.isBlank(path)) {
            if (StringUtils.isBlank(NulsConfig.ACCOUNTKEYSTORE_FOLDER_NAME)) {
                URL resource = ClassLoader.getSystemClassLoader().getResource("");
                path = resource.getPath();
            } else {
                path = NulsConfig.ACCOUNTKEYSTORE_FOLDER_NAME;
            }
            try {
                path = URLDecoder.decode(path, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LoggerUtil.LOG.error(e);
            }
        }
        File backupFile = DBUtils.loadDataPath(path);
        //if not directory,create directory
        if (!backupFile.isDirectory()) {
            if (!backupFile.mkdirs()) {
                throw new NulsRuntimeException(AccountErrorCode.FILE_OPERATION_FAILD);
            }
            if (!backupFile.exists() && !backupFile.mkdir()) {
                throw new NulsRuntimeException(AccountErrorCode.FILE_OPERATION_FAILD);
            }
        }
        //根据账户地址生成文件名
        //generate filename based on account address
        String fileName = accountKeyStoreDto.getAddress().concat(AccountConstant.ACCOUNTKEYSTORE_FILE_SUFFIX);
        //创建备份文件
        //create backup file
        backupFile = new File(backupFile, fileName);
        try {
            //如果文件不存在，则创建该文件
            //if the file does not exist, the file is created
            if (!backupFile.exists() && !backupFile.createNewFile()) {
                throw new NulsRuntimeException(AccountErrorCode.FILE_OPERATION_FAILD);
            }
        } catch (IOException e) {
            throw new NulsRuntimeException(AccountErrorCode.IO_ERROR);
        }
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(backupFile);
            //convert keystore to JSON to store
            fileOutputStream.write(JSONUtils.obj2json(accountKeyStoreDto).getBytes());
        } catch (Exception e) {
            throw new NulsRuntimeException(AccountErrorCode.PARSE_JSON_FAILD);
        } finally {
            if (fileOutputStream != null) {
                try {
                    fileOutputStream.close();
                } catch (IOException e) {
                    LoggerUtil.LOG.error(e);
                }
            }
        }
        //If it is a windows system path, remove the first /
        if (System.getProperties().getProperty(AccountConstant.OS_NAME).toUpperCase().indexOf(AccountConstant.OS_WINDOWS) != -1) {
            if (path.startsWith(AccountConstant.SLASH)) {
                path = path.substring(1);
            }
            path = path.replace(AccountConstant.SLASH, "\\");
        }
        String backupFileName = path + File.separator + fileName;
        return backupFileName;
    }
}
