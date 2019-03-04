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
import io.nuls.ledger.model.AccountBalance;
import io.nuls.ledger.model.UnconfirmedTx;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.BlockSnapshotAccounts;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.BlockDataService;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.service.processor.CommontTransactionProcessor;
import io.nuls.ledger.service.processor.LockedTransactionProcessor;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.CoinDataUtils;
import io.nuls.ledger.utils.LedgerUtils;
import io.nuls.ledger.utils.LockerUtils;
import io.nuls.ledger.validator.CoinDataValidator;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * Created by wangkun23 on 2018/11/28.
 * update by lanjinsheng on 2019/01/02
 */
@Service
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    AccountStateService accountStateService;
    @Autowired
    CoinDataValidator coinDataValidator;
    @Autowired
    BlockDataService blockDataService;
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
    public boolean unConfirmTxProcess(int addressChainId, Transaction transaction) {
        //直接更新未确认交易
        CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接返回
            return true;
        }
        ValidateResult validateResult = coinDataValidator.validateCoinData(addressChainId, transaction);
        if (validateResult.getValidateCode() != CoinDataValidator.VALIDATE_SUCCESS_CODE) {
            logger.error("validateResult = {}={}", validateResult.getValidateCode(), validateResult.getValidateDesc());
            return false;
        }
        String currentTxNonce = LedgerUtils.getNonceStrByTxHash(transaction);
        Map<String, UnconfirmedTx> accountsMap = new ConcurrentHashMap<>();
        List<CoinFrom> froms = coinData.getFrom();
        List<CoinTo> tos = coinData.getTo();
        String txHash = transaction.getHash().toString();
        for (CoinFrom from : froms) {
            if (LedgerUtils.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                //非本地网络账户地址,不进行处理
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(from.getAddress());
            int assetChainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            String accountKey = LedgerUtils.getKeyStr(address, assetChainId, assetId);
            if (from.getLocked() == 0) {
                //非解锁交易处理
                CoinDataUtils.calTxFromAmount(accountsMap, from, txHash, accountKey);
            } else {
                //解锁交易处理[未确认解锁交易From，暂时不处理]
                logger.info("unConfirmTxProcess account = {} unlocked tx.txHash = {}", accountKey, txHash);
            }
        }
        for (CoinTo to : tos) {
            String address = AddressTool.getStringAddressByBytes(to.getAddress());
            int assetChainId = to.getAssetsChainId();
            int assetId = to.getAssetsId();
            if (to.getLockTime() == 0) {
                //普通交易
                String accountKey = LedgerUtils.getKeyStr(address, assetChainId, assetId);
                CoinDataUtils.calTxToAmount(accountsMap, to, txHash, accountKey);
            }
        }
        Set keys = accountsMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            UnconfirmedTx unconfirmedTx = accountsMap.get(it.next());
            accountStateService.setUnconfirmTx(addressChainId, currentTxNonce, unconfirmedTx);
        }
        return true;
    }


    /**
     * 已确认区块数据处理
     * 提交交易：1.交易存库（上一个账户状态进行镜像,存储最近x=500区块以内交易） 2.更新账户
     *
     * @param addressChainId
     * @param txList
     * @param blockHeight
     * @return
     */
    @Override
    public boolean confirmBlockProcess(int addressChainId, List<Transaction> txList, long blockHeight) {
        try {
            /*--begin 缓存区块交易数据,作为接口交互联调使用*/
            blockDataService.saveLatestBlockDatas(addressChainId, blockHeight, txList);
            /*--end*/
            LockerUtils.BLOCK_SYNC_LOCKER.lock();
            long currentDbHeight = repository.getBlockHeight(addressChainId);
            if ((blockHeight - currentDbHeight) != 1) {
                //高度不一致，数据出问题了
                logger.error("addressChainId ={},blockHeight={},ledgerBlockHeight={}", addressChainId, blockHeight, currentDbHeight);
                return false;
            }
            //批量交易按交易进行账户的金额处理，再按区块为原子性进行提交,updateAccounts用于账户缓存，最后统一处理
            Map<String, AccountBalance> updateAccounts = new HashMap<>();
            //整体区块备份
            BlockSnapshotAccounts blockSnapshotAccounts = new BlockSnapshotAccounts();
            for (Transaction transaction : txList) {
                //从缓存校验交易
                if (coinDataValidator.hadValidateTx(addressChainId, transaction)) {
                    CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
                    if (null == coinData) {
                        //例如黄牌交易，直接返回
                        continue;
                    }
                    //更新账户状态
                    String nonce8BytesStr = LedgerUtils.getNonceStrByTxHash(transaction);
                    String txHash = transaction.getHash().toString();
                    List<CoinFrom> froms = coinData.getFrom();
                    for (CoinFrom from : froms) {
                        if (LedgerUtils.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                            //非本地网络账户地址,不进行处理
                            logger.info("address={} not localChainAccount", AddressTool.getStringAddressByBytes(from.getAddress()));
                            continue;
                        }
                        boolean process = false;
                        AccountBalance accountBalance = getAccountBalance(addressChainId, from, txHash, blockHeight, updateAccounts);
                        if (from.getLocked() == 0) {
                            if (!coinDataValidator.validateNonces(accountBalance, nonce8BytesStr, HexUtil.encode(from.getNonce()))) {
                                logger.info("nonce1={},nonce2={} validate fail.", accountBalance.getNonces().get(accountBalance.getNonces().size() - 1), HexUtil.encode(from.getNonce()));
                                return false;
                            }
                            //非解锁交易处理
                            process = commontTransactionProcessor.processFromCoinData(from, nonce8BytesStr, transaction.getHash().toString(), accountBalance.getNowAccountState());
                        } else {
                            process = lockedTransactionProcessor.processFromCoinData(from, nonce8BytesStr, transaction.getHash().toString(), accountBalance.getNowAccountState());
                        }
                        if (!process) {
                            logger.info("address={},txHash = {} processFromCoinData is fail.", addressChainId, transaction.getHash().toString());
                            return false;
                        }
                    }
                    List<CoinTo> tos = coinData.getTo();
                    for (CoinTo to : tos) {
                        if (LedgerUtils.isNotLocalChainAccount(addressChainId, to.getAddress())) {
                            //非本地网络账户地址,不进行处理
                            logger.info("address={} not localChainAccount", AddressTool.getStringAddressByBytes(to.getAddress()));
                            continue;
                        }
                        AccountBalance accountBalance = getAccountBalance(addressChainId, to, txHash, transaction.getBlockHeight(), updateAccounts);

                        if (to.getLockTime() == 0) {
                            //非锁定交易处理
                            commontTransactionProcessor.processToCoinData(to, nonce8BytesStr, transaction.getHash().toString(), accountBalance.getNowAccountState());
                        } else {
                            //锁定交易处理
                            lockedTransactionProcessor.processToCoinData(to, nonce8BytesStr, transaction.getHash().toString(), accountBalance.getNowAccountState());
                        }
                    }
                } else {
                    return false;
                }

            }
            //整体交易的处理
            try {
                for (Map.Entry<String, AccountBalance> entry : updateAccounts.entrySet()) {
                    //缓存数据
                    blockSnapshotAccounts.addAccountState(entry.getValue().getPreAccountState());
                }
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("confirmBlockProcess blockSnapshotAccounts addAccountState error!");
                return false;
            }
            //提交整体数据
            try {
                //备份历史
                repository.saveBlockSnapshot(addressChainId, blockHeight, blockSnapshotAccounts);
                blockSnapshotAccounts.getAccounts().clear();
                //更新账本信息
                for (Map.Entry<String, AccountBalance> entry : updateAccounts.entrySet()) {
                    accountStateService.updateAccountStateByTx(entry.getKey(), blockSnapshotAccounts, entry.getValue().getNowAccountState());
                    logger.info("updateAccountStateByTx account={} Available  balance={}", entry.getKey(), entry.getValue().getNowAccountState().getAvailableAmount());
                }
                //更新备份历史，因为执行期间可能存在未确认交易的变更（非必须逻辑）
                repository.saveBlockSnapshot(addressChainId, blockHeight, blockSnapshotAccounts);
            } catch (Exception e) {
                e.printStackTrace();
                //需要回滚数据
                logger.error("confirmBlockProcess  error! go rollBackBlock!");
                rollBackBlock(addressChainId, blockSnapshotAccounts.getAccounts(), blockHeight);
                return false;
            }
            //完全提交,存储当前高度。
            repository.saveOrUpdateBlockHeight(addressChainId, blockHeight);
            return true;
        } catch (Exception e) {
            logger.error("confirmBlockProcess error", e);
            return false;
        } finally {
            LockerUtils.BLOCK_SYNC_LOCKER.unlock();
        }

    }

    private AccountBalance getAccountBalance(int addressChainId, Coin coin, String txHash, long height, Map<String, AccountBalance> updateAccounts) {
        String address = AddressTool.getStringAddressByBytes(coin.getAddress());
        int assetChainId = coin.getAssetsChainId();
        int assetId = coin.getAssetsId();
        String key = LedgerUtils.getKeyStr(address, assetChainId, assetId);
        AccountBalance accountBalance = updateAccounts.get(key);
        if (null == accountBalance) {
            //交易里的账户处理缓存AccountBalance
            AccountState accountState = accountStateService.getAccountState(address, addressChainId, assetChainId, assetId);
            AccountState orgAccountState = (AccountState) accountState.deepClone();
            accountState.setTxHash(txHash);
            accountState.setHeight(height);
            accountBalance = new AccountBalance(accountState, orgAccountState);
            updateAccounts.put(key, accountBalance);
        }
        accountBalance.getNowAccountState().setTxHash(txHash);
        return accountBalance;
    }

    /**
     * @param addressChainId
     * @param preAccountStates
     * @return
     */
    @Override
    public synchronized boolean rollBackBlock(int addressChainId, List<AccountState> preAccountStates, long blockHeight) {
        try {
            //回滚账号信息
            for (AccountState accountState : preAccountStates) {
                String key = LedgerUtils.getKeyStr(accountState.getAddress(), accountState.getAssetChainId(), accountState.getAssetId());
                accountStateService.rollAccountState(key, accountState);
                logger.info("rollBack account={},assetChainId={},assetId={}, height={},lastHash= {} ", key, accountState.getAssetChainId(), accountState.getAssetId(),
                        accountState.getHeight(), accountState.getTxHash());
            }
            //回滚备份数据
            repository.delBlockSnapshot(addressChainId, blockHeight);
        } catch (Exception e) {
            logger.error("rollBackBlock error!!", e);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * @param addressChainId
     * @return
     */
    @Override
    public boolean rollBackConfirmTxs(int addressChainId, long blockHeight) {
        try {
            LockerUtils.BLOCK_SYNC_LOCKER.lock();
            long currentDbHeight = repository.getBlockHeight(addressChainId);
            if (blockHeight != currentDbHeight) {
                //高度不一致，数据出问题了
                logger.error("addressChainId ={},blockHeight={},ledgerBlockHeight={}", addressChainId, blockHeight, currentDbHeight);
                return false;
            }
            //回滚高度
            repository.saveOrUpdateBlockHeight(addressChainId, (blockHeight - 1));
            BlockSnapshotAccounts blockSnapshotAccounts = repository.getBlockSnapshot(addressChainId, blockHeight);
            List<AccountState> preAccountStates = blockSnapshotAccounts.getAccounts();
            for (AccountState accountState : preAccountStates) {
                String key = LedgerUtils.getKeyStr(accountState.getAddress(), accountState.getAssetChainId(), accountState.getAssetId());
                accountStateService.rollAccountState(key, accountState);
                logger.info("rollBack account={},assetChainId={},assetId={}, height={},lastHash= {} ", key, accountState.getAssetChainId(), accountState.getAssetId(),
                        accountState.getHeight(), accountState.getTxHash());
            }
            //删除备份数据
            repository.delBlockSnapshot(addressChainId, blockHeight);

        } catch (Exception e) {
            logger.error("rollBackConfirmTxs error!!", e);
            e.printStackTrace();
            repository.saveOrUpdateBlockHeight(addressChainId, blockHeight);
            return false;
        } finally {
            LockerUtils.BLOCK_SYNC_LOCKER.unlock();
        }
        return true;
    }

    @Override
    public boolean rollBackUnconfirmTx(int addressChainId, Transaction transaction) {
        //回滚未确认交易,就是回滚未确认nonce值
        CoinData coinData = CoinDataUtils.parseCoinData(transaction.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接返回
            return true;
        }
        List<CoinFrom> froms = coinData.getFrom();
        String txHash = transaction.getHash().toString();
        for (CoinFrom from : froms) {
            if (LedgerUtils.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                //非本地网络账户地址,不进行处理
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(from.getAddress());
            int assetChainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            String assetKey = LedgerUtils.getKeyStr(address, assetChainId, assetId);
            accountStateService.rollUnconfirmTx(addressChainId, assetKey, HexUtil.encode(from.getNonce()), txHash);
        }
        return true;
    }
}
