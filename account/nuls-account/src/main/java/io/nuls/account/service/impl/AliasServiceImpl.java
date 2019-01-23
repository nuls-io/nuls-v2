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
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.rpc.call.TransactionCmdCall;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.log.LogUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.FormatValidUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Autowired
    private ChainManager chainManager;

    private AccountCacheService accountCacheService = AccountCacheService.getInstance();

    @Override
    public void afterPropertiesSet() {
    }

    @Override
    public Transaction setAlias(int chainId, String address, String password, String aliasName) {
        Transaction tx = null;
        try {

            if (!AddressTool.validAddress(chainId, address)) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            if (StringUtils.isNotBlank(aliasName)) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
            }
            if (!FormatValidUtils.validAlias(aliasName)) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_FORMAT_WRONG);
            }
            Account account = accountService.getAccount(chainId, address);
            if (null == account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            if (account.isEncrypted() && account.isLocked()) {
                if (!account.validatePassword(password)) {
                    throw new NulsRuntimeException(AccountErrorCode.PASSWORD_IS_WRONG);
                }
            }
            if (!isAliasUsable(chainId, aliasName)) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_EXIST);
            }
            account.setChainId(chainId);
            account.setAlias(aliasName);
            tx = createAliasTrasactionWithoutSign(account);
            signTransaction(tx, account, password);
            TransactionCmdCall.newTx(account.getChainId(), HexUtil.encode(tx.serialize()));

            //TODO how to save the chainId?EdwardChan
            //todo sign the transaction
            //todo send the transaction to transaction manage module
        } catch (Exception e) {
            LogUtil.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION, e);
        }
        return tx;
    }

    @Override
    public BigInteger getAliasFee(int chainId, String address, String aliasName) {
        Transaction tx = null;
        BigInteger fee = null;
        try {
            if (!AddressTool.validAddress(chainId, address)) {
                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
            }
            if (StringUtils.isNotBlank(aliasName)) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
            }
            if (!FormatValidUtils.validAlias(aliasName)) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_FORMAT_WRONG);
            }
            Account account = accountService.getAccount(chainId, address);
            if (null == account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }

            //create a set alias transaction
            tx = createAliasTrasactionWithoutSign(account);

            fee = TransactionFeeCalculator.getNormalTxFee(tx.size());
            //todo whether need to other operation if the fee is too big
        } catch (Exception e) {
            LogUtil.error("", e);
            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION, e);
        }
        return fee;
    }

    @Override
    public String getAliasByAddress(int chainId, String address) {
        //check if the account is legal
        if (!AddressTool.validAddress(chainId, address)) {
            LogUtil.debug("the address is illegal,chainId:{},address:{}", chainId, address);
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
//            LogUtilUtil.error("", e);
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
            LogUtil.error("", e);
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
            LogUtil.error("", e);
            throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR, e);
        }
        return result;
    }

    private Transaction createAliasTrasactionWithoutSign(Account account) throws NulsException, IOException {
        Transaction tx = null;
        //Second:build the transaction
        tx = new AliasTransaction();
        tx.setTime(TimeService.currentTimeMillis());
        Alias alias = new Alias();
        alias.setAlias(account.getAlias());
        alias.setAddress(account.getAddress().getAddressBytes());
        tx.setTxData(alias.serialize());
        Chain chain = chainManager.getChainMap().get(account.getChainId());
        int assetsId = chain.getConfig().getAssetsId();
        //TODO 不同链设置别名是否都燃烧nuls?
        CoinFrom coinFrom = new CoinFrom(account.getAddress().getAddressBytes(), account.getChainId(), assetsId);
        coinFrom.setAddress(account.getAddress().getAddressBytes());
        CoinTo coinTo = new CoinTo(AccountConstant.BLACK_HOLE_ADDRESS, account.getChainId(), assetsId, BigInteger.ONE);
        int txSize = tx.size() + coinFrom.size() + coinTo.size() + 72;
        //计算手续费
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        //总费用为
        BigInteger totalAmount = BigInteger.ONE.add(fee);
        coinFrom.setAmount(totalAmount);
        //检查余额是否充足
        BigInteger mainAsset = TxUtil.getBalance(account.getChainId(), account.getChainId(), assetsId, coinFrom.getAddress());
        if (BigIntegerUtils.isLessThan(mainAsset, totalAmount)) { //余额不足
            throw new NulsRuntimeException(AccountErrorCode.INSUFFICIENT_FEE);
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(Arrays.asList(coinFrom));
        coinData.setTo(Arrays.asList(coinTo));
        tx.setCoinData(coinData.serialize());
        //计算交易数据摘要哈希
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        return tx;
    }

    private Transaction signTransaction(Transaction transaction, Account account, String password) throws NulsException, IOException {
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        ECKey eckey = account.getEcKey(password);
        P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByEckey(transaction, eckey);
        p2PHKSignatures.add(p2PHKSignature);
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        transaction.setTransactionSignature(transactionSignature.serialize());
        return transaction;
    }
}
