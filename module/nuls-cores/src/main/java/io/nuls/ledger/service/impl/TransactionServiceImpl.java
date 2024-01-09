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

import io.nuls.base.data.*;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.AccountBalance;
import io.nuls.ledger.model.Uncfd2CfdKey;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.BlockSnapshotAccounts;
import io.nuls.ledger.model.po.TxUnconfirmed;
import io.nuls.ledger.model.po.sub.AccountStateSnapshot;
import io.nuls.ledger.model.po.sub.AmountNonce;
import io.nuls.ledger.service.*;
import io.nuls.ledger.service.processor.CommontTransactionProcessor;
import io.nuls.ledger.service.processor.LockedTransactionProcessor;
import io.nuls.ledger.storage.LgBlockSyncRepository;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.CoinDataUtil;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LockerUtil;
import io.nuls.ledger.utils.LoggerUtil;
import io.nuls.ledger.validator.CoinDataValidator;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * Implementation of transaction business processing
 *
 * @author lanjinsheng
 */
@Component
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    AccountStateService accountStateService;
    @Autowired
    UnconfirmedStateService unconfirmedStateService;
    @Autowired
    CoinDataValidator coinDataValidator;
    @Autowired
    LockedTransactionProcessor lockedTransactionProcessor;
    @Autowired
    CommontTransactionProcessor commontTransactionProcessor;
    @Autowired
    Repository repository;
    @Autowired
    LgBlockSyncRepository lgBlockSyncRepository;
    @Autowired
    FreezeStateService freezeStateService;
    @Autowired
    ChainAssetsService chainAssetsService;
    @Autowired
    AssetRegMngService assetRegMngService;
    /**
     * Cache a blocknoncevalue
     */
    private Map<String, Integer> ledgerNonce = new ConcurrentHashMap<String, Integer>(5120);
    /**
     * Cache a blockhashvalue
     */
    private Map<String, Integer> ledgerHash = new ConcurrentHashMap<>(5120);


    /**
     * Unconfirmed transaction data processing
     *
     * @param transaction
     */

    @Override
    public ValidateResult unConfirmTxProcess(int addressChainId, Transaction transaction) throws Exception {
        //Directly update unconfirmed transactions
        CoinData coinData = CoinDataUtil.parseCoinData(transaction.getCoinData());
        if (null == coinData) {
            //For example, in a yellow card transaction, return directly
            return ValidateResult.getSuccess();
        }
        if (!coinDataValidator.validateTxAmount(coinData, transaction.getType())) {
            return ValidateResult.getResult(LedgerErrorCode.TX_AMOUNT_INVALIDATE, new String[]{transaction.getHash().toHex()});
        }
        /*Verification of unconfirmed transactions*/
        Map<String, TxUnconfirmed> accountsMap = new ConcurrentHashMap<>(8);
        byte[] txNonce = LedgerUtil.getNonceByTx(transaction);
        ValidateResult validateResult = coinDataValidator.analysisCoinData(addressChainId, transaction, accountsMap, txNonce);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        Set keys = accountsMap.keySet();
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            TxUnconfirmed txUnconfirmed = accountsMap.get(it.next());
            ValidateResult updateResult = unconfirmedStateService.updateUnconfirmedTx(transaction.getHash().toHex(), addressChainId, txNonce, txUnconfirmed);
            if (!updateResult.isSuccess()) {
                return updateResult;
            }
        }
        return ValidateResult.getSuccess();
    }


    private boolean confirmBlockTxProcess(long blockHeight, int addressChainId, List<Transaction> txList,
                                          Map<String, AccountBalance> updateAccounts, List<Uncfd2CfdKey> delUncfd2CfdKeys,
                                          Map<String, Integer> clearUncfs, Map<String, List<String>> assetAddressIndex) throws Exception {
        for (Transaction transaction : txList) {
            byte[] nonce8Bytes = LedgerUtil.getNonceByTx(transaction);
            String nonce8Str = LedgerUtil.getNonceEncode(nonce8Bytes);
            String txHash = transaction.getHash().toHex();
            ledgerHash.put(txHash, 1);
            //Verify transactions from cache
            CoinData coinData = CoinDataUtil.parseCoinData(transaction.getCoinData());
            if (null == coinData) {
                //For example, in yellow card trading, seed nodes generatecoinbaseDirectly return
                LoggerUtil.logger(addressChainId).info("txHash = {},coinData is null continue.", txHash);
                continue;
            }
            List<CoinFrom> froms = coinData.getFrom();
            for (CoinFrom from : froms) {
                String address = LedgerUtil.getRealAddressStr(from.getAddress());
                if (LedgerUtil.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                    //Non local network account address,Not processed
                    logger(addressChainId).info("address={} not localChainAccount", address);
                    if (LedgerUtil.isCrossTx(transaction.getType())) {
                        //Non local network account address,Not processed
                        continue;
                    } else {
                        LoggerUtil.logger(addressChainId).error("address={} Not local chain Exception", address);
                        return false;
                    }
                }
                if (assetRegMngService.isContractAsset(from.getAssetsChainId(), from.getAssetsId())) {
                    //If non cross chain transactions are received in the ledgerfromIt is a contract asset, reporting an error
                    LoggerUtil.logger(addressChainId).info("hash={} asset={}-{}  from is contract asset", txHash, from.getAssetsChainId(), from.getAssetsId());
                    continue;
                }
                boolean process;
                AccountBalance accountBalance = getAccountBalance(addressChainId, from, updateAccounts, address);
                //How many types of assets are under the collection chain, and how many addresses are under the assets
                LedgerUtil.dealAssetAddressIndex(assetAddressIndex, from.getAssetsChainId(), from.getAssetsId(), address);
                if (from.getLocked() == 0) {
                    AmountNonce amountNonce = new AmountNonce(from.getNonce(), nonce8Bytes, from.getAmount());
                    accountBalance.getPreAccountState().getNonces().add(amountNonce);
                    //Determine if there is an unconfirmed process transaction. If it exists, make a confirmation record. If it does not exist, clear the unconfirmed record
                    String accountKeyStr = LedgerUtil.getKeyStr(address, from.getAssetsChainId(), from.getAssetsId());
                    if (unconfirmedStateService.existTxUnconfirmedTx(addressChainId, accountKeyStr, nonce8Str)) {
                        delUncfd2CfdKeys.add(new Uncfd2CfdKey(accountKeyStr, nonce8Str));
                    } else {
                        clearUncfs.put(accountKeyStr, 1);
                    }
                    //Non unlocked transaction processing
                    process = commontTransactionProcessor.processFromCoinData(from, nonce8Bytes, accountBalance.getNowAccountState());
                    ledgerNonce.put(LedgerUtil.getAccountNoncesStrKey(address, from.getAssetsChainId(), from.getAssetsId(), nonce8Str), 1);
                } else {
                    process = lockedTransactionProcessor.processCoinData(from, nonce8Bytes, txHash, accountBalance.getNowAccountState(), transaction.getTime(), address, true);
                }
                if (!process) {
                    logger(addressChainId).error("address={},txHash = {} processFromCoinData is fail.", addressChainId, transaction.getHash().toHex());
                    return false;
                }
            }
            List<CoinTo> tos = coinData.getTo();
            for (CoinTo to : tos) {
                String address = LedgerUtil.getRealAddressStr(to.getAddress());
                if (LedgerUtil.isNotLocalChainAccount(addressChainId, to.getAddress())) {
                    //Non local network account address,Not processed
                    logger(addressChainId).info("address={} not localChainAccount", address);
                    if (LedgerUtil.isCrossTx(transaction.getType())) {
                        continue;
                    } else {
                        LoggerUtil.logger(addressChainId).error("address={} Not local chain Exception", address);
                        return false;
                    }
                }
                if (assetRegMngService.isContractAsset(to.getAssetsChainId(), to.getAssetsId())) {
                    //If non cross chain transactions are received in the ledgertoIt is a contractual asset,Not recorded
                    LoggerUtil.logger(addressChainId).info("hash={} asset={}-{} rec contract asset", txHash, to.getAssetsChainId(), to.getAssetsId());
                    continue;
                }
                AccountBalance accountBalance = getAccountBalance(addressChainId, to, updateAccounts, address);
                //How many types of assets are under the collection chain, and how many addresses are under the assets
                LedgerUtil.dealAssetAddressIndex(assetAddressIndex, to.getAssetsChainId(), to.getAssetsId(), address);
                if (to.getLockTime() == 0) {
                    //Non locked transaction processing
                    commontTransactionProcessor.processToCoinData(to, accountBalance.getNowAccountState());
                } else {
                    //Lock transaction processing
                    lockedTransactionProcessor.processCoinData(to, nonce8Bytes, txHash, accountBalance.getNowAccountState(), transaction.getTime(), address, false);
                }
            }
        }
        return true;
    }

    /**
     * Confirmed block data processing
     * Submit transaction：1.Transaction repository（Mirror the previous account status,Store Recentlyx=500Within block transactions） 2.Update account
     *
     * @param addressChainId
     * @param txList
     * @param blockHeight
     * @return
     */
    @Override
    public boolean confirmBlockProcess(int addressChainId, List<Transaction> txList, long blockHeight) {
        try {
            cleanBlockCommitTempDatas();
            LockerUtil.LEDGER_LOCKER.lock();
            long currentDbHeight = repository.getBlockHeight(addressChainId);
            if ((blockHeight - currentDbHeight) != 1) {
                //The height is inconsistent, and there is an issue with the data
                logger(addressChainId).error("addressChainId ={},blockHeight={},ledgerBlockHeight={}", addressChainId, blockHeight, currentDbHeight);
                return false;
            }
            int accountMapSize = txList.size() * 3;
            //Batch transactions are processed based on the transaction amount of the account, and then submitted as atomic blocks,updateAccountsUsed for account calculation caching, and finally processed uniformly
            Map<String, AccountBalance> updateAccounts = new HashMap<>(accountMapSize);
            Map<String, AccountState> updateMemAccounts = new HashMap<>(accountMapSize);
            //Overall block backup
            BlockSnapshotAccounts blockSnapshotAccounts = new BlockSnapshotAccounts();
            Map<byte[], byte[]> accountStatesMap = new HashMap<>(accountMapSize);
            List<Uncfd2CfdKey> delUncfd2CfdKeys = new ArrayList<>();
            Map<String, Integer> clearUncfs = new HashMap<>(txList.size());
            Map<String, List<String>> assetAddressIndex = new HashMap<>(4);
            try {
                if (!confirmBlockTxProcess(blockHeight, addressChainId, txList, updateAccounts, delUncfd2CfdKeys, clearUncfs, assetAddressIndex)) {
                    return false;
                }
                //Handling of overall transactions
                //Update ledger information
                for (Map.Entry<String, AccountBalance> entry : updateAccounts.entrySet()) {
                    //Caching data
                    blockSnapshotAccounts.addAccountState(entry.getValue().getPreAccountState());
                    freezeStateService.recalculateFreeze(addressChainId, entry.getValue().getNowAccountState());
                    entry.getValue().getNowAccountState().setLatestUnFreezeTime(NulsDateUtils.getCurrentTimeSeconds());
                    accountStatesMap.put(entry.getKey().getBytes(LedgerConstant.DEFAULT_ENCODING), entry.getValue().getNowAccountState().serialize());
                    updateMemAccounts.put(entry.getKey(), entry.getValue().getNowAccountState());
                }
            } catch (Exception e) {
                logger(addressChainId).error("confirmBlockProcess blockSnapshotAccounts addAccountState error!");
                logger(addressChainId).error(e);
                cleanBlockCommitTempDatas();
                return false;
            }
            //Submit overall data
            try {
                //Backup History
                repository.saveBlockSnapshot(addressChainId, blockHeight, blockSnapshotAccounts);
                //Update off chain asset types and asset address collection data.
                chainAssetsService.updateChainAssets(addressChainId, assetAddressIndex);
                //Update ledger
                if (accountStatesMap.size() > 0) {
                    repository.batchUpdateAccountState(addressChainId, accountStatesMap, updateMemAccounts);
                }
                for (Map.Entry<String, Integer> entry : clearUncfs.entrySet()) {
                    //Perform transactions received from other nodes in the network, refresh local unconfirmed data processing
                    unconfirmedStateService.clearAccountUnconfirmed(addressChainId, entry.getKey());
                }
                //Delete unconfirmed transactions for jumps
                unconfirmedStateService.batchDeleteUnconfirmedTx(addressChainId, delUncfd2CfdKeys);
                //Delete expired cache data
                if (blockHeight > LedgerConstant.CACHE_ACCOUNT_BLOCK) {
                    repository.delBlockSnapshot(addressChainId, (blockHeight - LedgerConstant.CACHE_ACCOUNT_BLOCK));
                }
            } catch (Exception e) {
                //Need to roll back data
                cleanBlockCommitTempDatas();
                logger(addressChainId).error(e);
                LoggerUtil.logger(addressChainId).error("confirmBlockProcess  error! go rollBackBlock!addrChainId={},height={}", addressChainId, blockHeight);
                rollBackBlock(addressChainId, blockSnapshotAccounts.getAccounts(), blockHeight);
                return false;
            }
            //Fully submit,Store the current height.
            repository.saveOrUpdateBlockHeight(addressChainId, blockHeight);
            return true;
        } catch (Exception e) {
            LoggerUtil.logger(addressChainId).error("confirmBlockProcess error", e);
            cleanBlockCommitTempDatas();
            return false;
        } finally {
            LockerUtil.LEDGER_LOCKER.unlock();

        }

    }

    private AccountBalance getAccountBalance(int addressChainId, Coin coin, Map<String, AccountBalance> updateAccounts, String address) {
        int assetChainId = coin.getAssetsChainId();
        int assetId = coin.getAssetsId();
        String key = LedgerUtil.getKeyStr(address, assetChainId, assetId);
        AccountBalance accountBalance = updateAccounts.get(key);
        if (null == accountBalance) {
            //Account processing cache in transactionsAccountBalance
            AccountState accountState = accountStateService.getAccountStateReCal(address, addressChainId, assetChainId, assetId);
            AccountStateSnapshot bakAccountState = new AccountStateSnapshot(addressChainId, assetChainId, assetId, address, accountState.deepClone());
            accountBalance = new AccountBalance(accountState, bakAccountState);
            updateAccounts.put(key, accountBalance);
        }
        return accountBalance;
    }

    /**
     * @param addressChainId
     * @param preAccountStates
     * @return
     */
    @Override
    public synchronized boolean rollBackBlock(int addressChainId, List<AccountStateSnapshot> preAccountStates, long blockHeight) {
        try {
            //Rollback account information
            accountStateService.rollAccountState(addressChainId, preAccountStates);
            //Rollback backup data
            repository.delBlockSnapshot(addressChainId, blockHeight);
        } catch (Exception e) {
            logger(addressChainId).error("rollBackBlock error!!", e);
            return false;
        }
        return true;
    }

    /**
     * @param addressChainId
     * @return
     */
    @Override
    public boolean rollBackConfirmTxs(int addressChainId, long blockHeight, List<Transaction> txs) {
        try {
            LockerUtil.LEDGER_LOCKER.lock();
            cleanBlockCommitTempDatas();
            long currentDbHeight = repository.getBlockHeight(addressChainId);
            if ((blockHeight - currentDbHeight) == 1) {
                logger(addressChainId).debug("addressChainId ={},blockHeight={},ledgerBlockHeight={}", addressChainId, blockHeight, currentDbHeight);
                return true;
            }
            if (blockHeight != currentDbHeight) {
                //The height is inconsistent, and there is an issue with the data
                logger(addressChainId).error("addressChainId ={},blockHeight={},ledgerBlockHeight={}", addressChainId, blockHeight, currentDbHeight);
                return false;
            }
            BlockSnapshotAccounts blockSnapshotAccounts = repository.getBlockSnapshot(addressChainId, blockHeight);
            if (null == blockSnapshotAccounts) {
                logger(addressChainId).error("addressChainId ={},blockHeight={},blockSnapshotAccounts is null.", addressChainId, blockHeight);
                return false;
            }
            //Rollback height
            repository.saveOrUpdateBlockHeight(addressChainId, (blockHeight - 1));
            List<AccountStateSnapshot> preAccountStates = blockSnapshotAccounts.getAccounts();
            accountStateService.rollAccountState(addressChainId, preAccountStates);
            //Delete backup data
            repository.delBlockSnapshot(addressChainId, blockHeight);
            //RollBACKnonceCache information
            txs.forEach(tx -> {
                String txHash = tx.getHash().toHex();
                //Verify transactions from cache
                CoinData coinData = CoinDataUtil.parseCoinData(tx.getCoinData());
                //Delete backuphash
                try {
                    lgBlockSyncRepository.deleteAccountHash(addressChainId, txHash);
                } catch (Exception e) {
                    LoggerUtil.logger(addressChainId).error(e);
                }
                if (null != coinData) {
                    //Update account status
                    String nonce8BytesStr = LedgerUtil.getNonceEncodeByTx(tx);
                    List<CoinFrom> froms = coinData.getFrom();
                    for (CoinFrom from : froms) {
                        if (LedgerUtil.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                            continue;
                        }
                        if (from.getLocked() == 0) {
                            try {
                                //The cost of deleting backupsnonceValue.
                                lgBlockSyncRepository.deleteAccountNonces(addressChainId, LedgerUtil.getAccountNoncesStrKey(LedgerUtil.getRealAddressStr(from.getAddress()), from.getAssetsChainId(), from.getAssetsId(), nonce8BytesStr));
                            } catch (Exception e) {
                                LoggerUtil.logger(addressChainId).error(e);
                            }
                        }
                    }
                }
            });
        } catch (Exception e) {
            logger(addressChainId).error("rollBackConfirmTxs error!!", e);
            repository.saveOrUpdateBlockHeight(addressChainId, blockHeight);
            return false;
        } finally {
            LockerUtil.LEDGER_LOCKER.unlock();
        }
        return true;
    }

    @Override
    public boolean rollBackUnconfirmTx(int addressChainId, Transaction transaction) {
        //Rolling back unconfirmed transactions,It's just rolling back unconfirmednoncevalue
        CoinData coinData = CoinDataUtil.parseCoinData(transaction.getCoinData());
        if (null == coinData) {
            //For example, in a yellow card transaction, return directly
            return true;
        }
        List<CoinFrom> froms = coinData.getFrom();
        String txHash = transaction.getHash().toHex();
        for (CoinFrom from : froms) {
            if (LedgerUtil.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                //Non local network account address,Not processed
                if (LedgerUtil.isCrossTx(transaction.getType())) {
                    //Non local network account address,Not processed
                    continue;
                } else {
                    LoggerUtil.logger(addressChainId).error("address={} Not local chain Exception", LedgerUtil.getRealAddressStr(from.getAddress()));
                    return false;
                }
            }
            String address = LedgerUtil.getRealAddressStr(from.getAddress());
            int assetChainId = from.getAssetsChainId();
            int assetId = from.getAssetsId();
            String assetKey = LedgerUtil.getKeyStr(address, assetChainId, assetId);
            return unconfirmedStateService.rollUnconfirmedTx(addressChainId, assetKey, txHash);
        }
        return true;
    }

    /**
     * Clear cached data during block submission
     *
     * @return
     */
    private void cleanBlockCommitTempDatas() {
        ledgerNonce.clear();
        ledgerHash.clear();
    }

    @Override
    public boolean fromNonceExist(int addressChainId, String accountNonceKey) throws Exception {
        return ledgerNonce.containsKey(accountNonceKey);
//        return (lgBlockSyncRepository.existAccountNonce(addressChainId, accountNonceKey));
    }

    @Override
    public boolean hadTxExist(int addressChainId, String hash) throws Exception {
        return ledgerHash.containsKey(hash);
//        return (lgBlockSyncRepository.existAccountHash(addressChainId, hash));
    }

}
