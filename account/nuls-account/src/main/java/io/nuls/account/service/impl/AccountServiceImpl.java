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

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.AccountKeyStore;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.rpc.call.EventCmdCall;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountKeyStoreService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.AESEncrypt;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.FormatValidUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.CryptoException;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: qinyifeng
 */
@Service
public class AccountServiceImpl implements AccountService, InitializingBean {

    private Lock locker = new ReentrantLock();

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AliasService aliasService;

    @Autowired
    private AccountKeyStoreService keyStoreService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    @Override
    public void afterPropertiesSet() {
        //Initialize local account data to cache
        getAccountList();
    }

    @Override
    public List<Account> createAccount(int chainId, int count, String password) {
        //check params
        if (chainId <= 0 || count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !FormatValidUtils.validPassword(password)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        locker.lock();
        List<Account> accounts = new ArrayList<>();
        try {
            List<AccountPo> accountPos = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                //create account
                Account account = AccountTool.createAccount(chainId);
                if (StringUtils.isNotBlank(password)) {
                    account.encrypt(password);
                }
                accounts.add(account);
                AccountPo po = new AccountPo(account);
                accountPos.add(po);
            }
            //Saving account data in batches
            boolean result = accountStorageService.saveAccountList(accountPos);
            if (result) {
                //If saved successfully, put the account in local cache.
                for (Account account : accounts) {
                    accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
                    //backup account to keystore
                    keyStoreService.backupAccountToKeyStore(null, chainId, account.getAddress().getBase58(), password);
                    //TODO
                    //build event data
                    HashMap<String, Object> eventData = new HashMap<>();
                    eventData.put("address", account.getAddress().getBase58());
                    eventData.put("isEncrypted", account.isEncrypted());
                    //Sending account creation events
                    EventCmdCall.sendEvent(AccountConstant.EVENT_TOPIC_CREATE_ACCOUNT, JSONUtils.obj2json(eventData));
                }
            }
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        } finally {
            locker.unlock();
        }
        return accounts;
    }

    @Override
    public Account getAccount(int chainId, String address) {
        //check params
        if (!AddressTool.validAddress(chainId, address)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        //check the account is exist
        Account account = getAccountByAddress(chainId, address);
        return account;
    }

    @Override
    public List<Account> getAccountList() {
        List<Account> list = new ArrayList<>();
        //If local account data is loaded into the cache
        if (accountCacheService.localAccountMaps.size() > 0) {
            Collection<Account> values = accountCacheService.localAccountMaps.values();
            Iterator<Account> iterator = values.iterator();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
        } else {
            //Query all accounts list
            List<AccountPo> poList = accountStorageService.getAccountList();
            Set<String> addressList = new HashSet<>();
            if (null == poList || poList.isEmpty()) {
                return list;
            }
            for (AccountPo po : poList) {
                Account account = po.toAccount();
                list.add(account);
                addressList.add(account.getAddress().getBase58());
            }
            //put the account in local cache.
            for (Account account : list) {
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            }
        }
        //sort by createTime desc
        Collections.sort(list, (Account o1, Account o2) -> (o2.getCreateTime().compareTo(o1.getCreateTime())));
        return list;
    }

    /**
     * 根据账户地址字符串,获取账户对象(内部调用)
     * Get account object based on account address string
     *
     * @return Account
     */
    private Account getAccountByAddress(int chainId, String address) {
        //check params
        if (!AddressTool.validAddress(chainId, address)) {
            return null;
        }
        // If the account is not yet cached, all local accounts are queried and cached
        if (accountCacheService.localAccountMaps == null || accountCacheService.localAccountMaps.size() == 0) {
            getAccountList();
        }
        return accountCacheService.localAccountMaps.get(address);
    }

    /**
     * set the password for exist account
     *
     * @param chainId
     * @param address
     * @param password
     * @return true or false
     * @auther EdwardChan
     * <p>
     * Nov.10th 2018
     */
    @Override
    public boolean setPassword(int chainId, String address, String password) {
        //check if the account is legal
        if (!AddressTool.validAddress(chainId, address)) {
            Log.debug("the address is illegal,chainId:{},address:{}", chainId, address);
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(password)) {
            Log.debug("the password should't be null,chainId:{},address:{}", chainId, address);
            throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
        }
        if (!FormatValidUtils.validPassword(password)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        //check if the account is exist
        Account account = getAccountByAddress(chainId, address);
        if (account == null) {
            Log.debug("the account isn't exist,chainId:{},address:{}", chainId, address);
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        //check if the account has encrypt
        if (account.isEncrypted()) {
            Log.debug("the account has encrypted,chainId:{},address:{}", chainId, address);
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_IS_ALREADY_ENCRYPTED);
        }
        //encrypt the account
        try {
            account.encrypt(password);
        } catch (Exception e) {
            Log.error("encrypt the account occur exception,chainId:{},address:{}", chainId, address, e);
        }
        //save the account
        AccountPo po = new AccountPo(account);
        boolean result = accountStorageService.saveAccount(po);
        if (!result) {
            Log.debug("save the account failed,chainId:{},address:{}", chainId, address);
        }
        //backup account to keystore
        keyStoreService.backupAccountToKeyStore(null, chainId, account.getAddress().getBase58(), password);
        return result;
    }

    @Override
    public boolean changePassword(int chainId, String address, String oldPassword, String newPassword) {
        //check params
        if (!AddressTool.validAddress(chainId, address)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!FormatValidUtils.validPassword(oldPassword) || !FormatValidUtils.validPassword(newPassword)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        //check if the account is exist
        Account account = this.getAccountByAddress(chainId, address);
        if (null == account) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        //If the account is not encrypted
        if (!account.isEncrypted()) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_UNENCRYPTED);
        }
        //Verify that the account password is correct
        if (!account.validatePassword(oldPassword)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        try {
            //Unlock account by password
            account.unlock(oldPassword);
            //Encrypting the account by the new password
            account.encrypt(newPassword, true);
            AccountPo po = new AccountPo(account);
            //save the account to the database
            boolean result = accountStorageService.updateAccount(po);
            //save the account to the cache
            accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            //backup account to keystore
            keyStoreService.backupAccountToKeyStore(null, chainId, account.getAddress().getBase58(), newPassword);
            //build event data
            HashMap<String, Object> eventData = new HashMap<>();
            eventData.put("address", account.getAddress().getBase58());
            //Sending update account password events
            EventCmdCall.sendEvent(AccountConstant.EVENT_TOPIC_UPDATE_PASSWORD, JSONUtils.obj2json(eventData));
            return result;
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsRuntimeException(e.getErrorCode());
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
    }

    @Override
    public String setOfflineAccountPassword(int chainId, String address, String priKey, String password) {
        //check params
        if (StringUtils.isBlank(address) || !AddressTool.validAddress(chainId, address)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(priKey) || !ECKey.isValidPrivteHex(priKey)) {
            throw new NulsRuntimeException(AccountErrorCode.PRIVATE_KEY_WRONG);
        }
        if (StringUtils.isBlank(password) || !FormatValidUtils.validPassword(password)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        try {
            //create account by private key
            Account account = AccountTool.createAccount(chainId, priKey);
            //验证地址是否正确 Verify that the address is correct.
            if (!address.equals(account.getAddress().getBase58())) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            //encrypt for account
            account.encrypt(password);
            return HexUtil.encode(account.getEncryptedPriKey());
        } catch (NulsException e) {
            throw new NulsRuntimeException(e.getErrorCode());
        }
    }

    @Override
    public String changeOfflinePassword(int chainId, String address, String priKey, String oldPassword, String newPassword) {
        //check params
        if (StringUtils.isBlank(address) || !AddressTool.validAddress(chainId, address)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isBlank(priKey)) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isBlank(oldPassword) || !FormatValidUtils.validPassword(oldPassword)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }
        if (StringUtils.isBlank(newPassword) || !FormatValidUtils.validPassword(newPassword)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
        }

        try {
            //The private key is decrypted by password
            byte[] priKeyBytes = AESEncrypt.decrypt(HexUtil.decode(priKey), oldPassword);
            if (!ECKey.isValidPrivteHex(HexUtil.encode(priKeyBytes))) {
                throw new NulsRuntimeException(AccountErrorCode.PRIVATE_KEY_WRONG);
            }
            //create offline account by private key
            Account account = AccountTool.createAccount(chainId, HexUtil.encode(priKeyBytes));
            if (!address.equals(account.getAddress().getBase58())) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            //Encrypting the account by the new password
            account.encrypt(newPassword);
            return HexUtil.encode(account.getEncryptedPriKey());
        } catch (NulsException e) {
            throw new NulsRuntimeException(e.getErrorCode());
        } catch (CryptoException e) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
    }

    /**
     * check if the account is encrypted
     *
     * @param chainId
     * @param address
     * @return true or false
     * @auther EdwardChan
     * <p>
     * Nov.10th 2018
     */
    @Override
    public boolean isEncrypted(int chainId, String address) {
        //check if the account is legal
        if (!AddressTool.validAddress(chainId, address)) {
            Log.debug("the address is illegal,chainId:{},address:{}", chainId, address);
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        //check if the account is exist
        Account account = getAccountByAddress(chainId, address);
        if (account == null) {
            Log.debug("the account isn't exist,chainId:{},address:{}", chainId, address);
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        boolean result = account.isEncrypted();
        Log.debug("the account is Encrypted:{},chainId:{},address:{}", result, chainId, address);
        return result;
    }

    @Override
    public boolean removeAccount(int chainId, String address, String password) {
        //check params
        if (!AddressTool.validAddress(chainId, address)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        //Check whether the account exists
        Account account = getAccountByAddress(chainId, address);
        if (account == null) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        //The account is encrypted, verify password
        if (account.isEncrypted()) {
            if (!account.validatePassword(password)) {
                throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        }
        boolean result;
        try {
            //Delete the account from the database
            result = accountStorageService.removeAccount(account.getAddress());
            //Delete the account from the cache
            accountCacheService.localAccountMaps.remove(account.getAddress().getBase58());

            //build event data
            HashMap<String, Object> eventData = new HashMap<>();
            eventData.put("address", account.getAddress().getBase58());
            //Sending account remove events
            EventCmdCall.sendEvent(AccountConstant.EVENT_TOPIC_REMOVE_ACCOUNT, JSONUtils.obj2json(eventData));
        } catch (Exception e) {
            Log.error(e);
            throw new NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return result;
    }

    @Override
    public boolean setRemark(int chainId, String address, String remark) {
        //check whether the account exists
        Account account = this.getAccountByAddress(chainId, address);
        if (null == account) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        if (StringUtils.isBlank(remark)) {
            remark = null;
        }
        //check if the remark is legal
        if (!FormatValidUtils.validRemark(remark)) {
            throw new NulsRuntimeException(AccountErrorCode.REMARK_TOO_LONG);
        }
        //save the account to the database
        account.setRemark(remark);
        boolean result = accountStorageService.updateAccount(new AccountPo(account));
        //save the account to the cache
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        return result;
    }

    @Override
    public String getPrivateKey(int chainId, String address, String password) {
        //check whether the account exists
        Account account = this.getAccountByAddress(chainId, address);
        if (null == account) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        //加过密(有密码) 就验证密码 Already encrypted(Added password), verify password
        if (account.isEncrypted()) {
            try {
                byte[] priKeyBytes = account.getPriKey(password);
                return HexUtil.encode(priKeyBytes);
            } catch (NulsException e) {
                throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
            }
        } else {
            return null;
            //do not return unencrypted private key
            //throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_UNENCRYPTED);
        }
    }

    @Override
    public List<String> getAllPrivateKey(int chainId, String password) {
        //Query all accounts list
        List<Account> localAccountList = this.getAccountList();
        if (localAccountList == null || localAccountList.isEmpty()) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        //Query all accounts of the specified chain
        if (chainId > 0) {
            List<Account> chainAccountList = new ArrayList<>();
            localAccountList.stream().filter((p) -> p.getChainId() == chainId).forEach(account -> chainAccountList.add(account));
            localAccountList.clear();
            localAccountList.addAll(chainAccountList);
        }
        //Check if the password is correct.
        if (StringUtils.isNotBlank(password) && !FormatValidUtils.validPassword(password)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        List<String> list = new ArrayList<>();
        for (Account account : localAccountList) {
            if (account.isEncrypted()) {
                //If an account is encrypted but no password is transmitted, return error.
                if (StringUtils.isBlank(password)) {
                    throw new NulsRuntimeException(AccountErrorCode.HAVE_ENCRYPTED_ACCOUNT);
                }
                try {
                    //Decrypt unencrypted private key
                    byte[] priKeyBytes = account.getPriKey(password);
                    //Encryption for private key
                    list.add(HexUtil.encode(priKeyBytes));
                } catch (NulsException e) {
                    throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            } else {
                //There is an unencrypted account in the account collection, but the password is transmitted.
                if (StringUtils.isNotBlank(password)) {
                    throw new NulsRuntimeException(AccountErrorCode.HAVE_UNENCRYPTED_ACCOUNT);
                }
                //Encryption for private key
                list.add(HexUtil.encode(account.getPriKey()));
            }
        }
        return list;
    }

    @Override
    public Account importAccountByPrikey(int chainId, String prikey, String password, boolean overwrite) throws NulsException {
        //check params
        if (!ECKey.isValidPrivteHex(prikey)) {
            throw new NulsRuntimeException(AccountErrorCode.PRIVATE_KEY_WRONG);
        }
        if (StringUtils.isNotBlank(password) && !FormatValidUtils.validPassword(password)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        //not allowed to cover
        if (!overwrite) {
            Address address = AccountTool.newAddress(chainId, prikey);
            //Query account already exists
            Account account = this.getAccountByAddress(chainId, address.getBase58());
            if (null != account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_EXIST);
            }
        }
        //create account by private key
        Account account;
        try {
            account = AccountTool.createAccount(chainId, prikey);
        } catch (NulsException e) {
            throw new NulsRuntimeException(AccountErrorCode.PRIVATE_KEY_WRONG);
        }
        //encrypting account private key
        if (FormatValidUtils.validPassword(password)) {
            account.encrypt(password);
        }
        //Query account already exists
        Account acc = getAccountByAddress(chainId, account.getAddress().getBase58());
        if (null == acc) {
            //查询全网该链所有别名对比地址符合就设置
            //query the whole network. All the aliases of the chain match the addresses
            account.setAlias(aliasService.getAliasByAddress(chainId, account.getAddress().getBase58()));
        } else {
            //if the local account already exists
            account.setAlias(acc.getAlias());
        }
        //save account to db
        accountStorageService.saveAccount(new AccountPo(account));
        //put the account in local cache
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        //backup account to keystore
        keyStoreService.backupAccountToKeyStore(null, chainId, account.getAddress().getBase58(), password);
        return account;
    }

    @Override
    public Account importAccountByKeyStore(AccountKeyStore keyStore, int chainId, String password, boolean overwrite) throws NulsException {
        //check params
        if (null == keyStore || StringUtils.isBlank(keyStore.getAddress())) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (!AddressTool.validAddress(chainId, keyStore.getAddress())) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !FormatValidUtils.validPassword(password)) {
            throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
        }
        //not allowed to cover
        if (!overwrite) {
            //Query account already exists
            Account account = getAccountByAddress(chainId, keyStore.getAddress());
            if (null != account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_EXIST);
            }
        }
        Account account;
        byte[] priKey;
        //if the private key is not encrypted, it is not empty
        if (null != keyStore.getPrikey() && keyStore.getPrikey().length > 0) {
            if (!ECKey.isValidPrivteHex(HexUtil.encode(keyStore.getPrikey()))) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            //create account by private key
            priKey = keyStore.getPrikey();
            account = AccountTool.createAccount(chainId, HexUtil.encode(priKey));
            //如果私钥生成的地址和keystore的地址不相符，说明私钥错误
            //if the address generated by the private key does not match the address of the keystore, the private key error
            if (!account.getAddress().getBase58().equals(keyStore.getAddress())) {
                throw new NulsRuntimeException(AccountErrorCode.PRIVATE_KEY_WRONG);
            }
        } else if (null == keyStore.getPrikey() && null != keyStore.getEncryptedPrivateKey()) {
            //加密私钥不为空,验证密码是否正确
            //encrypting private key is not empty,verify that the password is correct
            if (!FormatValidUtils.validPassword(password)) {
                throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            try {
                //create account by private key
                priKey = AESEncrypt.decrypt(HexUtil.decode(keyStore.getEncryptedPrivateKey()), password);
                account = AccountTool.createAccount(chainId, HexUtil.encode(priKey));
            } catch (CryptoException e) {
                throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
            }
            //如果私钥生成的地址和keystore的地址不相符，说明私钥错误
            //if the address generated by the private key does not match the address of the keystore, the private key error
            if (!account.getAddress().getBase58().equals(keyStore.getAddress())) {
                throw new NulsRuntimeException(AccountErrorCode.PRIVATE_KEY_WRONG);
            }
        } else {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        //Query account already exists
        Account acc = getAccountByAddress(chainId, account.getAddress().getBase58());
        if (null == acc) {
            //查询全网该链所有别名对比地址符合就设置
            //query the whole network. All the aliases of the chain match the addresses
            account.setAlias(aliasService.getAliasByAddress(chainId, account.getAddress().getBase58()));
        } else {
            //if the local account already exists
            account.setAlias(acc.getAlias());
        }

        //encrypting account private key
        if (FormatValidUtils.validPassword(password)) {
            account.encrypt(password);
        }
        //save account to db
        accountStorageService.saveAccount(new AccountPo(account));
        //put the account in local cache
        accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
        //backup account to keystore
        keyStoreService.backupAccountToKeyStore(null, chainId, account.getAddress().getBase58(), password);
        return account;
    }

}
