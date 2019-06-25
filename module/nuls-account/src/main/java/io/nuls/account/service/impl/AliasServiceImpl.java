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
import io.nuls.account.model.NonceBalance;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.AliasTransaction;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.po.AccountPo;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.rpc.call.TransactionCall;
import io.nuls.account.service.AccountCacheService;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.storage.AccountStorageService;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.NulsDateUtils;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.account.util.TxUtil.getSuccess;

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
    public Transaction setAlias(Chain chain, String address, String password, String aliasName) throws Exception {
        Transaction tx = null;
        int chainId = chain.getChainId();
        if (!AddressTool.validAddress(chainId, address)) {
            throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
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
        if (StringUtils.isNotBlank(account.getAlias())) {
            throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
        }
        if (!isAliasUsable(chainId, aliasName)) {
            throw new NulsRuntimeException(AccountErrorCode.ALIAS_EXIST);
        }

        account.setChainId(chainId);
        //创建别名交易
        tx = createAliasTrasactionWithoutSign(chain, account, aliasName);
        //签名别名交易
        signTransaction(tx, account, password);

        if (!TransactionCall.newTx(chain, tx)) {
            throw new  NulsRuntimeException(AccountErrorCode.FAILED);
        }
        return tx;
    }

//    @Override
//    public BigInteger getAliasFee(Chain chain, String address, String aliasName) {
//        Transaction tx = null;
//        BigInteger fee = null;
//        try {
//            int chainId = chain.getChainId();
//            if (!AddressTool.validAddress(chainId, address)) {
//                throw new NulsRuntimeException(AccountErrorCode.ADDRESS_ERROR);
//            }
//            if (!FormatValidUtils.validAlias(aliasName)) {
//                throw new NulsRuntimeException(AccountErrorCode.ALIAS_FORMAT_WRONG);
//            }
//            Account account = accountService.getAccount(chainId, address);
//            if (null == account) {
//                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
//            }
//            if (StringUtils.isNotBlank(account.getAlias())) {
//                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
//            }
//
//            //create a set alias transaction
//            tx = createAliasTrasactionWithoutSign(chain, account, aliasName);
//
//            fee = TransactionFeeCalculator.getNormalTxFee(tx.size());
//            //todo whether need to other operation if the fee is too big
//        } catch (Exception e) {
//            LoggerUtil.LOG.error("", e);
//            throw new NulsRuntimeException(AccountErrorCode.SYS_UNKOWN_EXCEPTION, e);
//        }
//        return fee;
//    }

    @Override
    public String getAliasByAddress(int chainId, String address) {
        //check if the account is legal
        if (!AddressTool.validAddress(chainId, address)) {
            LoggerUtil.LOG.debug("the address is illegal,chainId:{},address:{}", chainId, address);
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
    public Result aliasTxValidate(int chainId, Transaction transaction) throws NulsException {
        Alias alias = new Alias();
        alias.parse(new NulsByteBuffer(transaction.getTxData()));
        String address = AddressTool.getStringAddressByBytes(alias.getAddress());
        if (AddressTool.validContractAddress(alias.getAddress(), chainId)) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        if (!FormatValidUtils.validAlias(alias.getAlias())) {
            return Result.getFailed(AccountErrorCode.ALIAS_FORMAT_WRONG);
        }
        if (!isAliasUsable(chainId, alias.getAlias())) {
            LoggerUtil.LOG.error("alias is disable,alias: " + alias.getAlias() + ",address: " + alias.getAddress());
            return Result.getFailed(AccountErrorCode.ALIAS_EXIST);
        }
        AliasPo aliasPo = aliasStorageService.getAliasByAddress(chainId, address);
        if (aliasPo != null) {
            LoggerUtil.LOG.error("alias is already exist,alias: " + alias.getAlias() + ",address: " + alias.getAddress());
            return Result.getFailed(AccountErrorCode.ACCOUNT_ALREADY_SET_ALIAS);
        }
        // check the CoinData
        CoinData coinData = transaction.getCoinDataInstance();
        if (null == coinData) {
            return Result.getFailed(AccountErrorCode.TX_COINDATA_NOT_EXIST);
        }
        if (null != coinData.getFrom()){
            byte[] addr = null;
            for(CoinFrom coinFrom : coinData.getFrom()){
                if(addr == null){
                    addr = coinFrom.getAddress();
                }
                if(!Arrays.equals(coinFrom.getAddress(), addr)){
                    LoggerUtil.LOG.error("alias coin contains multiple different addresses, txhash:{}", transaction.getHash().toHex());
                    return Result.getFailed(AccountErrorCode.TX_DATA_VALIDATION_ERROR);
                }

            }
        }
        if (null != coinData.getTo()) {
            boolean burned = false;
            for (Coin coin : coinData.getTo()) {
                if (AddressTool.isBlackHoleAddress(NulsConfig.BLACK_HOLE_PUB_KEY,chainId,coin.getAddress()) && coin.getAmount().equals(AccountConstant.ALIAS_FEE)) {
                    burned = true;
                    break;
                }
            }
            if (!burned) {
                return Result.getFailed(AccountErrorCode.MUST_BURN_A_NULS);
            }
        }
        TransactionSignature sig = new TransactionSignature();
        try {
            sig.parse(transaction.getTransactionSignature(), 0);
        } catch (NulsException e) {
            LoggerUtil.LOG.error("", e);
            return Result.getFailed(e.getErrorCode());
        }
        boolean sign = SignatureUtil.containsAddress(transaction, alias.getAddress(), chainId);
        if (!sign) {
            return Result.getFailed(AccountErrorCode.ADDRESS_ERROR);
        }
        return getSuccess();
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
                accountCacheService.getLocalAccountMaps().put(account.getAddress().getBase58(), account);
            }
        } catch (Exception e) {
            LoggerUtil.LOG.error("", e);
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
                result = aliasStorageService.removeAlias(chainId, alias.getAlias());
                AccountPo accountPo = accountStorageService.getAccount(alias.getAddress());
                if (accountPo != null) {
                    accountPo.setAlias("");
                    result = accountStorageService.updateAccount(accountPo);
                    if (!result) {
                        return result;
                    }
                    Account account = accountPo.toAccount();
                    accountCacheService.getLocalAccountMaps().put(account.getAddress().getBase58(), account);
                }
            }
        } catch (Exception e) {
            LoggerUtil.LOG.error("", e);
            throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR, e);
        }
        return result;
    }

    private Transaction createAliasTrasactionWithoutSign(Chain chain, Account account, String aliasName) throws NulsException, IOException {
        Transaction tx = null;
        //Second:build the transaction
        tx = new AliasTransaction();
        tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
        Alias alias = new Alias();
        alias.setAlias(aliasName);
        alias.setAddress(account.getAddress().getAddressBytes());
        tx.setTxData(alias.serialize());
        //设置别名烧毁账户所属本链的主资产
        int assetsId = chain.getConfig().getAssetId();
        //查询账本获取nonce值
        NonceBalance nonceBalance = TxUtil.getBalanceNonce(chain, account.getChainId(), assetsId, account.getAddress().getAddressBytes());
        byte[] nonce = nonceBalance.getNonce();
        CoinFrom coinFrom = new CoinFrom(account.getAddress().getAddressBytes(), account.getChainId(), assetsId, AccountConstant.ALIAS_FEE, nonce, AccountConstant.NORMAL_TX_LOCKED);
        coinFrom.setAddress(account.getAddress().getAddressBytes());
        CoinTo coinTo = new CoinTo(AddressTool.getAddress(NulsConfig.BLACK_HOLE_PUB_KEY,account.getChainId()), account.getChainId(), assetsId, AccountConstant.ALIAS_FEE);
        int txSize = tx.size() + coinFrom.size() + coinTo.size() + P2PHKSignature.SERIALIZE_LENGTH;
        //计算手续费
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(txSize);
        //总费用为
        BigInteger totalAmount = AccountConstant.ALIAS_FEE.add(fee);
        coinFrom.setAmount(totalAmount);
        //检查余额是否充足
        BigInteger mainAsset = nonceBalance.getAvailable();
        //余额不足
        if (BigIntegerUtils.isLessThan(mainAsset, totalAmount)) {
            throw new NulsRuntimeException(AccountErrorCode.INSUFFICIENT_FEE);
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(Arrays.asList(coinFrom));
        coinData.setTo(Arrays.asList(coinTo));
        tx.setCoinData(coinData.serialize());
        //计算交易数据摘要哈希
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
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
