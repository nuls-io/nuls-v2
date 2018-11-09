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

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.base.basic.AddressTool;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: qinyifeng
 */
@Service
public class AccountServiceImpl implements AccountService {

    private Lock locker = new ReentrantLock();

    @Autowired
    private AccountStorageService accountStorageService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    @Override
    public List<Account> createAccount(short chainId, int count, String password) {
        // check params
        if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
            throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
        }
        if (StringUtils.isNotBlank(password) && !AccountTool.validPassword(password)) {
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
            //批量保存账户数据
            boolean result = accountStorageService.saveAccountList(accountPos);
            if (result) {
                //如果保存成功，将账户放入本地缓存
                for (Account account : accounts) {
                    accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
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
    public Account getAccount(short chainId, String address) {
        if (!AddressTool.validAddress(address, chainId)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        Account account = getAccountByAddress(address, chainId);
        if (null == account) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
        }
        return account;
    }

    @Override
    public List<Account> getAccountList() {
        List<Account> list = new ArrayList<>();
        if (accountCacheService.localAccountMaps != null) {
            Collection<Account> values = accountCacheService.localAccountMaps.values();
            Iterator<Account> iterator = values.iterator();
            while (iterator.hasNext()) {
                list.add(iterator.next());
            }
        } else {
            accountCacheService.localAccountMaps = new ConcurrentHashMap<>();
            //查询所有账户列表
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
            //放入本地缓存
            for (Account account : list) {
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            }
        }
        Collections.sort(list, (Account o1, Account o2) -> (o2.getCreateTime().compareTo(o1.getCreateTime())));
        return list;
    }

    /**
     * 根据账户地址字符串,获取账户对象(内部调用)
     * Get account object based on account address string
     *
     * @return Account
     */
    private Account getAccountByAddress(String address, short chainId) {
        if (!AddressTool.validAddress(address, chainId)) {
            return null;
        }
        // 从缓存中查询账户
        Account accountCache = accountCacheService.getAccountByAddress(address);
        if (null != accountCache) {
            return accountCache;
        }
        // 如果还未缓存账户，则查询本地所有账户并缓存
        if (accountCacheService.localAccountMaps == null) {
            getAccountList();
        }
        return accountCacheService.localAccountMaps.get(address);
    }
}
