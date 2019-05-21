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
package io.nuls.ledger.storage.impl;

import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.ledger.utils.LedgerUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */
@Service
public class UnconfirmedRepositoryImpl implements UnconfirmedRepository, InitializingBean {
    public UnconfirmedRepositoryImpl() {

    }

    /**
     * key1=chainId,  Map1=账户资产对应的未确认交易记录， key2= addr+assetkey+nonce,value=TxUnconfirmed
     */
//    Map<String, Map<String, TxUnconfirmed>> chainAccountUnconfirmedTxs = new HashMap<>(1);
    /**
     * key1=chainId,  Map1=未确认账户状态， key2= addr+assetkey  value=AccountStateUnconfirmed
     */
    Map<String, Map<String, AccountStateUnconfirmed>> chainAccountUnconfirmed = new HashMap<>(1);

    @Override
    public AccountStateUnconfirmed getMemAccountStateUnconfirmed(int chainId, String accountKey) {
        Map<String, AccountStateUnconfirmed> map = chainAccountUnconfirmed.get(String.valueOf(chainId));
        if (null != map) {
            return map.get(accountKey);
        }
        return null;
    }

    @Override
    public void delMemAccountStateUnconfirmed(int chainId, String accountKey) {
        Map<String, AccountStateUnconfirmed> map = chainAccountUnconfirmed.get(String.valueOf(chainId));
        if (null != map) {
            map.remove(accountKey);
        }
    }

    @Override
    public void saveMemAccountStateUnconfirmed(int chainId, String accountKey, AccountStateUnconfirmed accountStateUnconfirmed) {
        Map<String, AccountStateUnconfirmed> map = chainAccountUnconfirmed.get(String.valueOf(chainId));
        if (null == map) {
            map = new HashMap<>();
            chainAccountUnconfirmed.put(String.valueOf(chainId), map);
        }
        map.put(accountKey, accountStateUnconfirmed);
    }


    @Override
    public TxUnconfirmed getMemUnconfirmedTx(int chainId, String accountKey, String nonceKey) {
        AccountStateUnconfirmed accountStateUnconfirmed = getMemAccountStateUnconfirmed(chainId, accountKey);
        if (null != accountStateUnconfirmed) {
            return accountStateUnconfirmed.getTxUnconfirmed(nonceKey);
        }
        return null;
    }

    @Override
    public void delMemUnconfirmedTx(int chainId, String accountKey, String nonceKey) {
        AccountStateUnconfirmed accountStateUnconfirmed = getMemAccountStateUnconfirmed(chainId, accountKey);
        if (null != accountStateUnconfirmed) {
            accountStateUnconfirmed.delTxUnconfirmed(nonceKey);
        }
    }

    @Override
    public void saveMemUnconfirmedTxs(int chainId, String accountKey, Map<String, TxUnconfirmed> txUnconfirmedMap) {
        AccountStateUnconfirmed accountStateUnconfirmed = getMemAccountStateUnconfirmed(chainId, accountKey);
        if (null != accountStateUnconfirmed) {
            accountStateUnconfirmed.addTxUnconfirmeds(txUnconfirmedMap);
        }
    }

    @Override
    public void saveMemUnconfirmedTx(int chainId, String accountKey, String nonce, TxUnconfirmed txUnconfirmed) {
        AccountStateUnconfirmed accountStateUnconfirmed = getMemAccountStateUnconfirmed(chainId, accountKey);
        if (null != accountStateUnconfirmed) {
            accountStateUnconfirmed.addTxUnconfirmed(nonce, txUnconfirmed);
        }
    }

    @Override
    public void addUncfd2Cfd(int chainId, String accountKey, BigInteger addAmount) {
        AccountStateUnconfirmed accountStateUnconfirmed = getMemAccountStateUnconfirmed(chainId, accountKey);
        if (null == accountStateUnconfirmed) {
            return;
        }
        accountStateUnconfirmed.setToConfirmedAmount(accountStateUnconfirmed.getToConfirmedAmount().add(addAmount));
    }

    @Override
    public void clearMemUnconfirmedTxs(int chainId, String accountKey, TxUnconfirmed txUnconfirmed) {
        AccountStateUnconfirmed accountStateUnconfirmed = getMemAccountStateUnconfirmed(chainId, accountKey);
        if(null ==accountStateUnconfirmed){
            return;
        }
        Map<String, TxUnconfirmed> accountUnconfirmedTxs = accountStateUnconfirmed.getTxUnconfirmedMap();
        if (null != accountUnconfirmedTxs) {
            String key = LedgerUtil.getNonceEncode(txUnconfirmed.getNonce());
            TxUnconfirmed memTxUnconfirmed = accountUnconfirmedTxs.get(key);
            while (null != memTxUnconfirmed) {
                key = LedgerUtil.getNonceEncode(txUnconfirmed.getNonce());
                accountUnconfirmedTxs.remove(key);
                String keyNext = LedgerUtil.getNonceEncode(txUnconfirmed.getNextNonce());
                memTxUnconfirmed = accountUnconfirmedTxs.get(keyNext);
            }
        }
    }

    /**
     * 账号对应的未确认全部清空
     *
     * @param chainId
     * @param accountKey
     */
    @Override
    public void clearMemUnconfirmedTxs(int chainId, String accountKey) {
        AccountStateUnconfirmed accountStateUnconfirmed = getMemAccountStateUnconfirmed(chainId, accountKey);
        if (null == accountStateUnconfirmed) {
            return;
        }
        accountStateUnconfirmed.clearTxUnconfirmeds();
    }

    @Override
    public void afterPropertiesSet() throws NulsException {

    }

}
