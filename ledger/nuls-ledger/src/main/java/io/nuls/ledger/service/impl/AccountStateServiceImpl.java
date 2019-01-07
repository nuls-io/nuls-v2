/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.service.impl;

import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.ledger.utils.LedgerUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

/**
 * Created by wangkun23 on 2018/11/29.
 * update by lanjinsheng 2018/12/29.
 */
@Service
public class AccountStateServiceImpl implements AccountStateService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private Repository repository;
    @Autowired
    FreezeStateService freezeStateService;

    @Override
    public AccountState createAccount(String address, int chainId, int assetId) {
        String initialNonce = BigInteger.ZERO.toString();
        AccountState accountState = new AccountState(chainId, assetId, initialNonce);
        byte[] key = LedgerUtils.getKey(address, chainId, assetId);
        repository.createAccountState(key, accountState);
        return accountState;
    }
    @Override
    public void updateAccountStateByTx(String assetKey,AccountState orgAccountState,AccountState accountState){
       repository.updateAccountStateAndSnapshot(assetKey,orgAccountState,accountState);
    }

    @Override
    public void rollAccountStateByTx(String assetKey, String txHash, long height) {
        byte[] snapshotKeyBytes = LedgerUtils.getSnapshotKey(assetKey,txHash,height);
        AccountState accountState = repository.getSnapshotAccountState(snapshotKeyBytes);
        try {
            if(null != accountState) {
                repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountState);
                repository.delSnapshotAccountState(snapshotKeyBytes);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isExist(String address, int chainId, int assetId) {
        return getAccountState(address, chainId, assetId) != null;
    }

    /**
     *
     * @param address
     * @param chainId
     * @param assetId
     * @return
     */
    @Override
    public  AccountState getAccountState(String address, int chainId, int assetId) {
        byte[] key = LedgerUtils.getKey(address, chainId, assetId);
        AccountState accountState = repository.getAccountState(key);
        if(null == accountState){
            accountState = new AccountState(chainId,assetId,"0");
            repository.createAccountState(key,accountState);
        }else{
            //解冻时间锁
            if(freezeStateService.recalculateFreeze(accountState)){
                repository.updateAccountState(key,accountState);
            }

        }
        return accountState;
    }

    /**
     *
     * @param address
     * @param chainId
     * @param assetId
     * @param nonce
     * @return
     */
    @Override
    public  String setUnconfirmNonce(String address, int chainId, int assetId, String nonce) {
        AccountState accountState = getAccountState(address, chainId, assetId);
        accountState.setUnconfirmedNonce(nonce);
        byte[] key = LedgerUtils.getKey(address, chainId, assetId);
        //这个改变无需进行账户的snapshot
        repository.updateAccountState(key, accountState);
        return accountState.getUnconfirmedNonce();
    }


}
