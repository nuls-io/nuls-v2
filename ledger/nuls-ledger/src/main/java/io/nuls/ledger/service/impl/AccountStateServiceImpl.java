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
import io.nuls.ledger.model.po.UnconfirmedNonce;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.FreezeStateService;
import io.nuls.ledger.utils.LedgerUtils;
import io.nuls.ledger.utils.LockerUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.List;

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
    public AccountState createAccount(String address, int addressChainId, int assetChainId, int assetId) {
        AccountState accountState = new AccountState(addressChainId,assetChainId, assetId, LedgerConstant.INIT_NONCE);
        byte[] key = LedgerUtils.getKey(address, assetChainId, assetId);
        repository.createAccountState(key, accountState);
        return accountState;
    }
    @Override
    public void updateAccountStateByTx(String assetKey,AccountState orgAccountState,AccountState accountState){
       repository.updateAccountStateAndSnapshot(assetKey,orgAccountState,accountState);
    }

    @Override
    public void rollAccountStateByTx(int addressChainId,String assetKey, String txHash, long height) {
        //账户处理锁
        synchronized (LockerUtils.getAccountLocker(assetKey)) {
            byte[] snapshotKeyBytes = LedgerUtils.getSnapshotTxKey(assetKey, txHash, height);
            AccountState accountState = repository.getSnapshotAccountState(addressChainId,snapshotKeyBytes);
            try {
                if (null != accountState) {
                    if(!accountState.getTxHash().equalsIgnoreCase(txHash)){
                        //当前的hash不在回滚里，错误的回滚顺序
                        logger.error("TxHash not validate{}={}",accountState.getTxHash(),txHash);
                        return;
                    }
                    repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountState);
                    repository.delSnapshotAccountState(addressChainId,snapshotKeyBytes);
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public void rollUnconfirmTx(int addressChainId,String assetKey,String nonce) {
        //账户处理锁
        synchronized (LockerUtils.getAccountLocker(assetKey)) {
               try {
                   AccountState accountState = repository.getAccountState(addressChainId,assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING));
                   List<UnconfirmedNonce> list =  accountState.getUnconfirmedNonces();
                   int i = 0;
                   for(UnconfirmedNonce unconfirmedNonce : list){
                       i++;
                       if(unconfirmedNonce.getNonce().equalsIgnoreCase(nonce)) {
                           break;
                       }
                   }
                   int size = list.size();
                   //从第list的index=i-1起进行清空
                   if(i>0) {
                       for (int j = (i-1); j < size; j++) {
                           list.remove(j);
                       }
                       repository.updateAccountState(assetKey.getBytes(LedgerConstant.DEFAULT_ENCODING), accountState);
                   }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }
    /**
     *
     * @param address
     * @param assetChainId
     * @param assetId
     * @return
     */
    @Override
    public  AccountState getAccountState(String address,int addressChainId, int assetChainId, int assetId) {
        //账户处理锁
        synchronized (LockerUtils.getAccountLocker(address, assetChainId, assetId)) {
            byte[] key = LedgerUtils.getKey(address, assetChainId, assetId);
            AccountState accountState = repository.getAccountState(addressChainId,key);
            if (null == accountState) {
                accountState = new AccountState(addressChainId,assetChainId, assetId, LedgerConstant.INIT_NONCE);
                repository.createAccountState(key, accountState);
            } else {
                //清理未确认交易
                if(accountState.getUnconfirmedNonces().size()>0){
                    if(LedgerUtils.isExpiredNonce(accountState.getUnconfirmedNonces().get(0))){
                        accountState.getUnconfirmedNonces().clear();
                    }
                }
                //解冻时间锁
                if (freezeStateService.recalculateFreeze(accountState)) {
                    repository.updateAccountState(key, accountState);
                }
            }
            return accountState;
        }

    }

    /**
     *
     * @param address
     * @param addressChainId
     * @param assetChainId
     * @param assetId
     * @param nonce
     * @return
     */
    @Override
    public void setUnconfirmNonce(String address, int addressChainId, int assetChainId, int assetId, String nonce,String newNonce) {
        //账户同步锁
        synchronized (LockerUtils.getAccountLocker(address, assetChainId, assetId)) {
            AccountState accountState = getAccountState(address,addressChainId,assetChainId, assetId);
            accountState.setUnconfirmedNonce(newNonce);
            byte[] key = LedgerUtils.getKey(address, assetChainId, assetId);
            //这个改变无需进行账户的snapshot
            repository.updateAccountState(key, accountState);
        }

    }


}
