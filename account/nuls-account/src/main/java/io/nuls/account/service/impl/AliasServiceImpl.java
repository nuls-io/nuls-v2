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
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.account.util.AccountTool;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.basic.Result;
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

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: EdwardChan
 *
 * Nov.16th 2018
 *
 */
@Service
public class AliasServiceImpl implements AliasService, InitializingBean {

    private Lock locker = new ReentrantLock();

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AliasStorageService aliasStorageService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public Result<String> setAlias(short chainId, String address, String password, String aliasName) {
        return null;
    }

    @Override
    public Result<String> getAliasFee(short chaindId, String address, String aliasName) {
        return null;
    }

    /**
     * get the alias by address
     *
     * @param chainId
     * @param address
     * @return the alias,if the alias is not exist,it will be return null
     * @auther EdwardChan
     * <p>
     * Nov.12th 2018
     */
    @Override
    public String getAliasByAddress(short chainId, String address) {
        //check if the account is legal
        if (!AddressTool.validAddress(chainId, address)) {
            Log.debug("the address is illegal,chainId:{},address:{}", chainId, address);
            return null;
        }
        //get aliasPO
        AliasPo result = aliasStorageService.getAliasByAddress(chainId, address);
        if (result == null) {
            return null;
        }
        return result.getAlias();
    }

    @Override
    public boolean isAliasUsable(short chainId, String alias) {
        return null == aliasStorageService.getAlias(chainId,alias);
    }

    @Override
    public String setMultiSigAlias(short chainId, String address, String signAddress, String password, String alias) {
        return null;
    }

    @Override
    public boolean aliasTxValidate(short chainId, String alias) {
        return false;
    }

    @Override
    public boolean aliasTxCommit(AliasPo aliaspo) throws NulsException {
        boolean result = false;
        try {
            result = aliasStorageService.saveAlias(aliaspo);
            if (!result) {
                this.rollbackAlias(aliaspo);
            }
            AccountPo po = accountStorageService.getAccount(aliaspo.getAddress());
            if (null != po) {
                po.setAlias(aliaspo.getAlias());
                result = accountStorageService.updateAccount(po);
                if (!result) {
                    this.rollbackAlias(aliaspo);
                }
                Account account = po.toAccount();
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            }
        } catch (Exception e) {
            Log.error("",e);
            this.rollbackAlias(aliaspo);
            return false;
        }
        return result;
    }
    @Override
    public boolean rollbackAlias(AliasPo aliasPo) throws NulsException {
        boolean result = true;
        try {
            AliasPo po = aliasStorageService.getAlias(aliasPo.getChainId(),aliasPo.getAlias());
            if (po != null && Arrays.equals(po.getAddress(), aliasPo.getAddress())) {
                aliasStorageService.removeAlias(aliasPo.getChainId(),aliasPo.getAlias());
                AccountPo accountPo = accountStorageService.getAccount(aliasPo.getAddress());
                if (accountPo != null) {
                    accountPo.setAlias("");
                    result = accountStorageService.updateAccount(accountPo);
                    if (!result) {
                        return result;
                    }
                    Account account = accountPo.toAccount();
                    accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
                }
            }
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
        }
        return result;
    }


}
