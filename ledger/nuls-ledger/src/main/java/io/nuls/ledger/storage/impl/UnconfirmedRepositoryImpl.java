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

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.db.service.RocksDBService;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;
import io.nuls.ledger.storage.DataBaseArea;
import io.nuls.ledger.storage.UnconfirmedRepository;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.exception.NulsException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */
@Service
public class UnconfirmedRepositoryImpl implements UnconfirmedRepository, InitializingBean {
    public UnconfirmedRepositoryImpl() {

    }

    Map<String, Map<String, Map<String, TxUnconfirmed>>> chainAccountUnconfirmedTxs = new HashMap<>(1024);
    Map<String, Map<String, AccountStateUnconfirmed>> chainAccountUnconfirmed = new HashMap<>(1024);


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
            map = new HashMap<>(1024);
            chainAccountUnconfirmed.put(String.valueOf(chainId), map);
        }
        map.put(accountKey, accountStateUnconfirmed);
    }

    @Override
    public Map<String, Map<String, TxUnconfirmed>> getMemAccountUnconfirmedTxs(int chainId) {
        return chainAccountUnconfirmedTxs.get(String.valueOf(chainId));
    }

    @Override
    public Map<String, TxUnconfirmed> getMemUnconfirmedTxs(int chainId, String accountKey) {
        Map<String, Map<String, TxUnconfirmed>> accountUnconfirmedTxs = getMemAccountUnconfirmedTxs(chainId);
        if (null != accountUnconfirmedTxs) {
            Map<String, TxUnconfirmed> unconfirmedMap = accountUnconfirmedTxs.get(accountKey);
            return unconfirmedMap;
        }
        return null;
    }

    @Override
    public TxUnconfirmed getMemUnconfirmedTx(int chainId, String accountKey, String nonceKey) {
        Map<String, Map<String, TxUnconfirmed>> accountUnconfirmedTxs = getMemAccountUnconfirmedTxs(chainId);
        if (null != accountUnconfirmedTxs) {
            Map<String, TxUnconfirmed> unconfirmedMap = accountUnconfirmedTxs.get(accountKey);
            if (null != unconfirmedMap) {
                return unconfirmedMap.get(nonceKey);
            }
        }
        return null;
    }

    @Override
    public  void delMemUnconfirmedTx(int chainId, String accountKey, String nonceKey) {
        Map<String, Map<String, TxUnconfirmed>> accountUnconfirmedTxs = getMemAccountUnconfirmedTxs(chainId);
        if(null != accountUnconfirmedTxs){
            Map<String, TxUnconfirmed>  unconfirmedTxs = accountUnconfirmedTxs.get(accountKey);
            if(null!= unconfirmedTxs){
                unconfirmedTxs.remove(nonceKey);
            }
        }
    }

    @Override
    public void saveMemUnconfirmedTxs(int chainId, String accountKey, Map<String, TxUnconfirmed> map) {
        Map<String, Map<String, TxUnconfirmed>> accountUnconfirmedTxs = getMemAccountUnconfirmedTxs(chainId);
        if (null == accountUnconfirmedTxs) {
            accountUnconfirmedTxs = new HashMap<>(1024);
            chainAccountUnconfirmedTxs.put(String.valueOf(chainId), accountUnconfirmedTxs);
        }
        Map<String, TxUnconfirmed> unconfirmedTxMap = accountUnconfirmedTxs.get(accountKey);
        if (null == unconfirmedTxMap) {
            unconfirmedTxMap = new HashMap<>(128);
            accountUnconfirmedTxs.put(accountKey, unconfirmedTxMap);
        }
        unconfirmedTxMap.putAll(map);
    }

    @Override
    public void saveMemUnconfirmedTx(int chainId, String accountKey, String nonce, TxUnconfirmed txUnconfirmed) {
        Map<String, Map<String, TxUnconfirmed>> accountUnconfirmedTxs = getMemAccountUnconfirmedTxs(chainId);
        if (null == accountUnconfirmedTxs) {
            accountUnconfirmedTxs = new HashMap<>(1024);
            chainAccountUnconfirmedTxs.put(String.valueOf(chainId), accountUnconfirmedTxs);
        }
        Map<String, TxUnconfirmed> unconfirmedTxMap = accountUnconfirmedTxs.get(accountKey);
        if (null == unconfirmedTxMap) {
            unconfirmedTxMap = new HashMap<>(128);
            accountUnconfirmedTxs.put(accountKey, unconfirmedTxMap);
        }
        unconfirmedTxMap.put(nonce, txUnconfirmed);
    }

    @Override
    public void addUncfd2Cfd(int chainId, String accountKey, BigInteger addAmount) {
        Map<String, AccountStateUnconfirmed> accountStateUnconfirmedMap = chainAccountUnconfirmed.get(String.valueOf(chainId));
        if (null == accountStateUnconfirmedMap) {
            return;
        }
        AccountStateUnconfirmed accountStateUnconfirmed = accountStateUnconfirmedMap.get(accountKey);
        if (null == accountStateUnconfirmed) {
            return;
        } else {
            accountStateUnconfirmed.setToConfirmedAmount(accountStateUnconfirmed.getToConfirmedAmount().add(addAmount));
        }
    }

    @Override
    public void clearMemUnconfirmedTxs(int chainId, String accountKey, TxUnconfirmed txUnconfirmed) {
        Map<String, Map<String, TxUnconfirmed>> accountUnconfirmedTxs = getMemAccountUnconfirmedTxs(chainId);
        if (null != accountUnconfirmedTxs) {
            Map<String, TxUnconfirmed> unconfirmedMap = accountUnconfirmedTxs.get(accountKey);
            if (null != unconfirmedMap) {
                while (null != txUnconfirmed) {
                    String key = LedgerUtil.getNonceEncode(txUnconfirmed.getNonce());
                    unconfirmedMap.remove(key);
                    String keyNext = LedgerUtil.getNonceEncode(txUnconfirmed.getNextNonce());
                    txUnconfirmed = unconfirmedMap.get(keyNext);
                }
            }
        }
    }

    @Override
    public void afterPropertiesSet() throws NulsException {

    }
}
