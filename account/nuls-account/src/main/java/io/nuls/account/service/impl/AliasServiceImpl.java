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
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.*;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.FormatValidUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: EdwardChan
 * <p>
 * The service about alias
 * <p>
 * Nov.16th 2018
 */
@Service
public class AliasServiceImpl implements AliasService, InitializingBean {

    private Lock locker = new ReentrantLock();

    @Autowired
    private AccountStorageService accountStorageService;

    @Autowired
    private AliasStorageService aliasStorageService;

    @Autowired
    private AccountService accountService;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public Transaction setAlias(int chainId, String address, String password, String aliasName) {
        Transaction tx = null;
        try {
            Account account = accountService.getAccount(chainId, address);
            if (null == account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            if (account.isEncrypted() && account.isLocked()) {
                if (!account.validatePassword(password)) {
                    throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }
            if (StringUtils.isNotBlank(account.getAlias())) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
            }
            if (!isAliasUsable(chainId, aliasName)) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_EXIST);
            }

            tx = createAliasTrasaction(chainId, address, aliasName);
            //TODO how to save the chainId?EdwardChan
            //todo sign the transaction
            //todo send the transaction to transaction manage module
        } catch (Exception e) {
            Log.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION,e);
        }
        return tx;
    }

    @Override
    public BigInteger getAliasFee(int chaindId, String address, String aliasName) {
        Transaction tx = null;
        BigInteger fee = null;
        try {
            //create a set alias transaction
            tx = createAliasTrasaction(chaindId, address, aliasName);
            fee = TransactionFeeCalculator.getNormalTxFee(tx.size());
            //todo whether need to other operation if the fee is too big
        } catch (Exception e) {
            Log.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION,e);
        }
        return fee;
    }

    @Override
    public String getAliasByAddress(int chainId, String address) {
        //check if the account is legal
        if (!AddressTool.validAddress(chainId, address)) {
            Log.debug("the address is illegal,chainId:{},address:{}", chainId, address);
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        //get aliasPO
        AliasPo result = aliasStorageService.getAliasByAddress(chainId, address);
        if (result == null) {
            return null;
        }
        return result.getAlias();
    }

    @Override
    public boolean isAliasUsable(int chainId, String alias) {
        return null == aliasStorageService.getAlias(chainId, alias);
    }

    @Override
    public String setMultiSigAlias(int chainId, String address, String signAddress, String password, String alias) {
        return null;
    }

    @Override
    public List<Transaction> accountTxValidate(int chainId, List<Transaction> txList) {
        Set<Transaction> result = new HashSet<>();
        if (null == txList || txList.isEmpty()) {
            return new ArrayList<>(result);
        }
        Map<String, Transaction> aliasNamesMap = new HashMap<>();
        Map<String, Transaction> accountAddressMap = new HashMap<>();
        try {
            for (Transaction transaction : txList) {
                if (transaction.getType() == AccountConstant.TX_TYPE_ACCOUNT_ALIAS) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(transaction.getTxData()));
                    String address = AddressTool.getStringAddressByBytes(alias.getAddress());
                    //check alias
                    Transaction tmp = aliasNamesMap.get(alias.getAlias());
                    if (tmp != null) { // the alias is already exist
                        result.add(transaction);
                        result.add(tmp);
                        continue;
                    } else {
                        aliasNamesMap.put(alias.getAlias(), transaction);
                    }
                    //check address
                    tmp = accountAddressMap.get(address);
                    if (tmp != null) { // the address is already exist
                        result.add(transaction);
                        result.add(tmp);
                        continue;
                    } else {
                        accountAddressMap.put(address, transaction);
                    }
                }
            }
        } catch (Exception e) {
            Log.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION,e);
        }
        return new ArrayList<>(result);
    }

    @Override
    public boolean aliasTxValidate(int chainId, Transaction transaction) throws Exception {
        Alias alias = new Alias();
        alias.parse(new NulsByteBuffer(transaction.getTxData()));
        String address = AddressTool.getStringAddressByBytes(alias.getAddress());
//        if (transaction.isSystemTx()) {
//            throw new NulsRuntimeException(AccountErrorCode.TX_TYPE_ERROR);
//        }
        if (BaseConstant.CONTRACT_ADDRESS_TYPE == alias.getAddress()[2]) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!FormatValidUtils.validAlias(alias.getAlias())) {
            throw new NulsRuntimeException(AccountErrorCode.ALIAS_FORMAT_WRONG);
        }
        if (!isAliasUsable(chainId, alias.getAlias())) {
            throw new NulsRuntimeException(AccountErrorCode.ALIAS_EXIST);
        }
        AliasPo aliasPo = aliasStorageService.getAliasByAddress(chainId, address);
        if (aliasPo != null) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
        }
        // check the CoinData

//        CoinData coinData = transaction.getCoinData();
//        if (null == coinData) {
//            throw new NulsRuntimeException(AccountErrorCode.COINDATA_NOT_FOUND);
//        }
//        if (null != coinData.getTo()) {
//            boolean burned = false;
//            for (Coin coin : coinData.getTo()) {
//                if (Arrays.equals(coin.getOwner(), AccountConstant.BLACK_HOLE_ADDRESS) && coin.getNa().equals(Na.NA)) {
//                    burned = true;
//                    break;
//                }
//            }
//            if (!burned) {
//                throw new NulsRuntimeException(AccountErrorCode.MUST_BURN_A_NULS);
//            }
//        }
        //TODO 验证签名
//        TransactionSignature sig = new TransactionSignature();
//        try {
//            sig.parse(transaction.getTransactionSignature(), 0);
//        } catch (NulsException e) {
//            Log.error("", e);
//            throw new NulsRuntimeException(e.getErrorCode());
//        }
//        boolean sign;
//        try {
//            sign = SignatureUtil.containsAddress(transaction, alias.getAddress());
//        } catch (NulsException e) {
//            sign = false;
//        }
//        if (!sign) {
//            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
//        }
        return true;
    }

    @Override
    public boolean aliasTxCommit(int chainId, Alias alias) throws NulsException {
        boolean result = false;
        try {
            result = aliasStorageService.saveAlias(chainId, alias);
            if (!result) {
                this.rollbackAlias(chainId, alias);
            }
            AccountPo po = accountStorageService.getAccount(alias.getAddress());
            if (null != po) {
                po.setAlias(alias.getAlias());
                result = accountStorageService.updateAccount(po);
                if (!result) {
                    this.rollbackAlias(chainId, alias);
                }
                Account account = po.toAccount();
                accountCacheService.localAccountMaps.put(account.getAddress().getBase58(), account);
            }
        } catch (Exception e) {
            Log.error("", e);
            this.rollbackAlias(chainId, alias);
            return false;
        }
        return result;
    }

    @Override
    public boolean rollbackAlias(int chainId, Alias alias) throws NulsException {
        boolean result = true;
        try {
            AliasPo po = aliasStorageService.getAlias(chainId, alias.getAlias());
            if (po != null && Arrays.equals(po.getAddress(), alias.getAddress())) {
                aliasStorageService.removeAlias(chainId, alias.getAlias());
                AccountPo accountPo = accountStorageService.getAccount(alias.getAddress());
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
            Log.error("",e);
            throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR,e);
        }
        return result;
    }

    private Transaction createAliasTrasaction(int chainId, String address, String aliasName) throws IOException {
        Transaction tx = null;
        //First:check parameter
        if (!AddressTool.validAddress(chainId, address)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!FormatValidUtils.validAlias(aliasName)) {
            throw new NulsRuntimeException(AccountErrorCode.ALIAS_FORMAT_WRONG);
        }
        //Second:build the transaction
        tx = new Transaction(AccountConstant.TX_TYPE_ACCOUNT_ALIAS);
        tx.setTime(TimeService.currentTimeMillis());
        Alias alias = new Alias();
        alias.setAlias(aliasName);
        alias.setAddress(AddressTool.getAddress(address));
        tx.setTxData(alias.serialize());
        //Third:build the coinData
        CoinData coinData = new CoinData();
        //TODO how to get the transaction coin data
        //List<Coin> toList = new ArrayList<>();
        //toList.add(new Coin(agent.getAgentAddress(), agent.getDeposit(), ConsensusConstant.CONSENSUS_LOCK_TIME));
        //coinData.setTo(toList);
        tx.setCoinData(coinData.serialize());
        return tx;

    }

}
