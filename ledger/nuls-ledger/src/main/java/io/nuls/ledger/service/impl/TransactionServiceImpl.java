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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.ledger.db.Repository;
import io.nuls.ledger.model.AccountBalance;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.service.processor.CommontTransactionProcessor;
import io.nuls.ledger.service.processor.LockedTransactionProcessor;
import io.nuls.ledger.utils.CoinDataUtils;
import io.nuls.ledger.utils.LedgerUtils;
import io.nuls.ledger.validator.CoinDataValidator;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by wangkun23 on 2018/11/28.
 * update by lanjinsheng on 2019/01/02
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    AccountStateService accountStateService;
    @Autowired
    CoinDataValidator coinDataValidator;
    @Autowired
    LockedTransactionProcessor lockedTransactionProcessor;
    @Autowired
    CommontTransactionProcessor commontTransactionProcessor;

    @Autowired
    Repository repository;
    /**
     * 未确认交易数据处理
     *
     * @param transaction
     */

    @Override
    public boolean unConfirmTxProcess(int addressChainId,Transaction transaction) {
        //直接更新未确认交易
        CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
        byte [] nonce8Bytes = ByteUtils.copyOf(transaction.getHash().getDigestBytes(), 8);
        String currentTxNonce =  HexUtil.encode(nonce8Bytes);
        List<CoinFrom> froms = coinData.getFrom();
        for (CoinFrom from : froms) {
            if(LedgerUtils.isNotLocalChainAccount(addressChainId,from.getAddress())){
                //非本地网络账户地址,不进行处理
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(from.getAddress());
            int assetChainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            String nonce = HexUtil.encode(from.getNonce());
            if(from.getLocked()>0){
                //解锁交易处理

            }else {
                //非解锁交易处理

                accountStateService.setUnconfirmNonce(address, addressChainId,assetChainId, assetId, nonce,currentTxNonce);
            }
        }
        return true;
    }

    /**
     * 已确认交易数据处理
     *
     * @param transaction
     */
    @Override
    public synchronized boolean  confirmTxProcess(int addressChainId,Transaction transaction) {
        //从缓存校验交易
        if(coinDataValidator.hadValidateTx(addressChainId,transaction)){
            //提交交易：1.交易存库（最近100区块交易） 2.更新账户
            //批量校验数据不需要存库
            //批量交易按交易进行账户的金额处理，再按交易为原子性进行提交,updateAccounts用于一笔交易的账户缓存
            Map<String,AccountBalance> updateAccounts = new HashMap<>();
            //更新账户状态
            CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
            byte [] nonce8Bytes = ByteUtils.copyOf(transaction.getHash().getDigestBytes(), 8);
            String txHash =  transaction.getHash().toString();
            String nonce8BytesStr = HexUtil.encode(nonce8Bytes);
            List<CoinFrom> froms = coinData.getFrom();
            for (CoinFrom from : froms) {
                if(LedgerUtils.isNotLocalChainAccount(addressChainId,from.getAddress())){
                    //非本地网络账户地址,不进行处理
                    continue;
                }
                AccountBalance accountBalance = getAccountBalance(addressChainId,from,txHash,transaction.getBlockHeight(),updateAccounts);
                if(from.getLocked() > 0){
                    lockedTransactionProcessor.processFromCoinData(from,nonce8BytesStr,transaction.getHash().toString(),  accountBalance.getNowAccountState());
                }else {
                    //非解锁交易处理
                    commontTransactionProcessor.processFromCoinData(from,nonce8BytesStr,transaction.getHash().toString(),  accountBalance.getNowAccountState());
                }
            }
            List<CoinTo> tos = coinData.getTo();
            for (CoinTo to : tos) {
                if(LedgerUtils.isNotLocalChainAccount(addressChainId,to.getAddress())){
                    //非本地网络账户地址,不进行处理
                    continue;
                }
                AccountBalance accountBalance = getAccountBalance(addressChainId,to,txHash,transaction.getBlockHeight(),updateAccounts);
                if(to.getLockTime() > 0){
                    //锁定交易处理
                    lockedTransactionProcessor.processToCoinData(to,nonce8BytesStr,transaction.getHash().toString(), accountBalance.getNowAccountState());
                }else {
                    //非锁定交易处理
                    commontTransactionProcessor.processToCoinData(to,nonce8BytesStr,transaction.getHash().toString(),  accountBalance.getNowAccountState());
                }
            }
            //提交交易中的所有账号记录
            try {
                for (Map.Entry<String, AccountBalance> entry : updateAccounts.entrySet()) {
                    accountStateService.updateAccountStateByTx(entry.getKey(),entry.getValue().getPreAccountState(),entry.getValue().getNowAccountState());
                }
            }catch(Exception e){
                e.printStackTrace();
                //回滚
                rollBackConfirmTx(addressChainId,transaction);
            }
            return true;
        }
        return false;
    }
    private AccountBalance getAccountBalance(int addressChainId,Coin coin,String txHash,long height, Map<String,AccountBalance> updateAccounts){
        String address = AddressTool.getStringAddressByBytes(coin.getAddress());
        int assetChainId = coin.getAssetsChainId();
        int assetId = coin.getAssetsId();
        String key = LedgerUtils.getKeyStr(address,assetChainId,assetId);
        AccountBalance accountBalance = updateAccounts.get(key);
        if(null == accountBalance){
            //交易里的账户处理缓存AccountBalance
            AccountState accountState  = accountStateService.getAccountState(address,addressChainId,assetChainId,assetId);
            AccountState orgAccountState = (AccountState)accountState.deepClone();
            accountState.setTxHash(txHash);
            accountState.setHeight(height);
            accountBalance = new AccountBalance(accountState,orgAccountState);
            updateAccounts.put(key,accountBalance);
        }
        return accountBalance;
    }
    /**
     * 交易回滚，获取交易的的区块高度，hash值，
     * 从快照里去获取对应的账户高度，hash值的存储，进行回复账户信息
     * 回滚必须要有逆序，如果顺序不对，回滚将失败
     *
     * @param transaction
     */
    @Override
    public synchronized boolean rollBackConfirmTx(int addressChainId,Transaction transaction) {
        //更新账户状态
        CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
        String txHash = transaction.getHash().toString();
        long height = transaction.getBlockHeight();
        List<CoinFrom> froms = coinData.getFrom();
        List<CoinTo> tos = coinData.getTo();
        //获取账号信息
        for (CoinFrom from : froms) {
            if(LedgerUtils.isNotLocalChainAccount(addressChainId,from.getAddress())){
                //非本地网络账户地址,不进行处理
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(from.getAddress());
            int assetChainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            String key = LedgerUtils.getKeyStr(address,assetChainId,assetId);
            accountStateService.rollAccountStateByTx(addressChainId,key,txHash,height);
        }
        for (CoinTo to : tos) {
            if(LedgerUtils.isNotLocalChainAccount(addressChainId,to.getAddress())){
                //非本地网络账户地址,不进行处理
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(to.getAddress());
            int assetChainId = to.getAssetsChainId();
            int assetId = to.getAssetsId();
            String key = LedgerUtils.getKeyStr(address,assetChainId,assetId);
            accountStateService.rollAccountStateByTx(addressChainId,key,txHash,height);
        }
        return true;
    }

    @Override
    public boolean rollBackUnconfirmTx(int addressChainId, Transaction transaction) {
        //回滚未确认交易,就是回滚未确认nonce值
        CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
        List<CoinFrom> froms = coinData.getFrom();
        for (CoinFrom from : froms) {
            if(LedgerUtils.isNotLocalChainAccount(addressChainId,from.getAddress())){
                //非本地网络账户地址,不进行处理
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(from.getAddress());
            int assetChainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            String assetKey = LedgerUtils.getKeyStr(address,assetChainId,assetId);
            accountStateService.rollUnconfirmTx(addressChainId,assetKey,HexUtil.encode(from.getNonce()));
        }
        return true;
    }
}
