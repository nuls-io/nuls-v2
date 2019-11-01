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
 * 交易业务处理实现
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
     * 缓存一个区块的nonce值
     */
    private Map<String, Integer> ledgerNonce = new ConcurrentHashMap<String, Integer>(5120);
    /**
     * 缓存一个区块的hash值
     */
    private Map<String, Integer> ledgerHash = new ConcurrentHashMap<>(5120);


    /**
     * 未确认交易数据处理
     *
     * @param transaction
     */

    @Override
    public ValidateResult unConfirmTxProcess(int addressChainId, Transaction transaction) throws Exception {
        //直接更新未确认交易
        CoinData coinData = CoinDataUtil.parseCoinData(transaction.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接返回
            return ValidateResult.getSuccess();
        }
        if (!coinDataValidator.validateTxAmount(coinData, transaction.getType())) {
            return ValidateResult.getResult(LedgerErrorCode.TX_AMOUNT_INVALIDATE, new String[]{transaction.getHash().toHex()});
        }
        /*未确认交易的校验*/
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
            //从缓存校验交易
            CoinData coinData = CoinDataUtil.parseCoinData(transaction.getCoinData());
            if (null == coinData) {
                //例如黄牌交易，种子节点产生的coinbase直接返回
                LoggerUtil.logger(addressChainId).info("txHash = {},coinData is null continue.", txHash);
                continue;
            }
            List<CoinFrom> froms = coinData.getFrom();
            for (CoinFrom from : froms) {
                String address = LedgerUtil.getRealAddressStr(from.getAddress());
                if (LedgerUtil.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                    //非本地网络账户地址,不进行处理
                    logger(addressChainId).info("address={} not localChainAccount", address);
                    if (LedgerUtil.isCrossTx(transaction.getType())) {
                        //非本地网络账户地址,不进行处理
                        continue;
                    } else {
                        LoggerUtil.logger(addressChainId).error("address={} Not local chain Exception", address);
                        return false;
                    }
                }
                if(assetRegMngService.isContractAsset(from.getAssetsChainId(),from.getAssetsId())){
                    //账本非跨链交易如果收到from是合约资产的，报错
                    LoggerUtil.logger(addressChainId).info("hash={} asset={}-{}  from is contract asset", txHash,from.getAssetsChainId(),from.getAssetsId());
                    continue;
                }
                boolean process;
                AccountBalance accountBalance = getAccountBalance(addressChainId, from, updateAccounts, address);
                //归集链下有多少种类资产，资产下有多少地址
                LedgerUtil.dealAssetAddressIndex(assetAddressIndex, from.getAssetsChainId(), from.getAssetsId(), address);
                if (from.getLocked() == 0) {
                    AmountNonce amountNonce = new AmountNonce(from.getNonce(), nonce8Bytes, from.getAmount());
                    accountBalance.getPreAccountState().getNonces().add(amountNonce);
                    //判断是否存在未确认过程交易，如果存在则进行确认记录，如果不存在，则进行未确认的清空记录
                    String accountKeyStr = LedgerUtil.getKeyStr(address, from.getAssetsChainId(), from.getAssetsId());
                    if (unconfirmedStateService.existTxUnconfirmedTx(addressChainId, accountKeyStr, nonce8Str)) {
                        delUncfd2CfdKeys.add(new Uncfd2CfdKey(accountKeyStr, nonce8Str));
                    } else {
                        clearUncfs.put(accountKeyStr, 1);
                    }
                    //非解锁交易处理
                    process = commontTransactionProcessor.processFromCoinData(from, nonce8Bytes, accountBalance.getNowAccountState());
                    ledgerNonce.put(LedgerUtil.getAccountNoncesStrKey(address, from.getAssetsChainId(), from.getAssetsId(), nonce8Str), 1);
                } else {
                    process = lockedTransactionProcessor.processFromCoinData(from, nonce8Bytes, txHash, accountBalance.getNowAccountState(), address);
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
                    //非本地网络账户地址,不进行处理
                    logger(addressChainId).info("address={} not localChainAccount", address);
                    if (LedgerUtil.isCrossTx(transaction.getType())) {
                        continue;
                    } else {
                        LoggerUtil.logger(addressChainId).error("address={} Not local chain Exception", address);
                        return false;
                    }
                }
                if(assetRegMngService.isContractAsset(to.getAssetsChainId(),to.getAssetsId())){
                    //账本非跨链交易如果收到to是合约资产的,不进行入账
                    LoggerUtil.logger(addressChainId).info("hash={} asset={}-{} rec contract asset", txHash,to.getAssetsChainId(),to.getAssetsId());
                    continue;
                }
                AccountBalance accountBalance = getAccountBalance(addressChainId, to, updateAccounts, address);
                //归集链下有多少种类资产，资产下有多少地址
                LedgerUtil.dealAssetAddressIndex(assetAddressIndex, to.getAssetsChainId(), to.getAssetsId(), address);
                if (to.getLockTime() == 0) {
                    //非锁定交易处理
                    commontTransactionProcessor.processToCoinData(to, accountBalance.getNowAccountState());
                } else {
                    //锁定交易处理
                    lockedTransactionProcessor.processToCoinData(to, nonce8Bytes, txHash, accountBalance.getNowAccountState(), transaction.getTime(), address);
                }
            }
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
            cleanBlockCommitTempDatas();
            LockerUtil.LEDGER_LOCKER.lock();
            long currentDbHeight = repository.getBlockHeight(addressChainId);
            if ((blockHeight - currentDbHeight) != 1) {
                //高度不一致，数据出问题了
                logger(addressChainId).error("addressChainId ={},blockHeight={},ledgerBlockHeight={}", addressChainId, blockHeight, currentDbHeight);
                return false;
            }
            int accountMapSize = txList.size() * 3;
            //批量交易按交易进行账户的金额处理，再按区块为原子性进行提交,updateAccounts用于账户计算缓存，最后统一处理
            Map<String, AccountBalance> updateAccounts = new HashMap<>(accountMapSize);
            Map<String, AccountState> updateMemAccounts = new HashMap<>(accountMapSize);
            //整体区块备份
            BlockSnapshotAccounts blockSnapshotAccounts = new BlockSnapshotAccounts();
            Map<byte[], byte[]> accountStatesMap = new HashMap<>(accountMapSize);
            List<Uncfd2CfdKey> delUncfd2CfdKeys = new ArrayList<>();
            Map<String, Integer> clearUncfs = new HashMap<>(txList.size());
            Map<String, List<String>> assetAddressIndex = new HashMap<>(4);
            try {
                if (!confirmBlockTxProcess(blockHeight, addressChainId, txList, updateAccounts, delUncfd2CfdKeys, clearUncfs, assetAddressIndex)) {
                    return false;
                }
                //整体交易的处理
                //更新账本信息
                for (Map.Entry<String, AccountBalance> entry : updateAccounts.entrySet()) {
                    //缓存数据
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
            //提交整体数据
            try {
                //备份历史
                repository.saveBlockSnapshot(addressChainId, blockHeight, blockSnapshotAccounts);
                //更新链下资产种类，及资产地址集合数据。
                chainAssetsService.updateChainAssets(addressChainId, assetAddressIndex);
                //更新账本
                if (accountStatesMap.size() > 0) {
                    repository.batchUpdateAccountState(addressChainId, accountStatesMap, updateMemAccounts);
                }
                for (Map.Entry<String, Integer> entry : clearUncfs.entrySet()) {
                    //进行收到网络其他节点的交易，刷新本地未确认数据处理
                    unconfirmedStateService.clearAccountUnconfirmed(addressChainId, entry.getKey());
                }
                //删除跃迁的未确认交易
                unconfirmedStateService.batchDeleteUnconfirmedTx(addressChainId, delUncfd2CfdKeys);
                //删除过期缓存数据
                if (blockHeight > LedgerConstant.CACHE_ACCOUNT_BLOCK) {
                    repository.delBlockSnapshot(addressChainId, (blockHeight - LedgerConstant.CACHE_ACCOUNT_BLOCK));
                }
            } catch (Exception e) {
                //需要回滚数据
                cleanBlockCommitTempDatas();
                logger(addressChainId).error(e);
                LoggerUtil.logger(addressChainId).error("confirmBlockProcess  error! go rollBackBlock!addrChainId={},height={}", addressChainId, blockHeight);
                rollBackBlock(addressChainId, blockSnapshotAccounts.getAccounts(), blockHeight);
                return false;
            }
            //完全提交,存储当前高度。
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
            //交易里的账户处理缓存AccountBalance
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
            //回滚账号信息
            accountStateService.rollAccountState(addressChainId, preAccountStates);
            //回滚备份数据
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
                //高度不一致，数据出问题了
                logger(addressChainId).error("addressChainId ={},blockHeight={},ledgerBlockHeight={}", addressChainId, blockHeight, currentDbHeight);
                return false;
            }
            BlockSnapshotAccounts blockSnapshotAccounts = repository.getBlockSnapshot(addressChainId, blockHeight);
            if (null == blockSnapshotAccounts) {
                logger(addressChainId).error("addressChainId ={},blockHeight={},blockSnapshotAccounts is null.", addressChainId, blockHeight);
                return false;
            }
            //回滚高度
            repository.saveOrUpdateBlockHeight(addressChainId, (blockHeight - 1));
            List<AccountStateSnapshot> preAccountStates = blockSnapshotAccounts.getAccounts();
            accountStateService.rollAccountState(addressChainId, preAccountStates);
            //删除备份数据
            repository.delBlockSnapshot(addressChainId, blockHeight);
            //回滚nonce缓存信息
            txs.forEach(tx -> {
                String txHash = tx.getHash().toHex();
                //从缓存校验交易
                CoinData coinData = CoinDataUtil.parseCoinData(tx.getCoinData());
                //删除备份的hash
                try {
                    lgBlockSyncRepository.deleteAccountHash(addressChainId, txHash);
                } catch (Exception e) {
                    LoggerUtil.logger(addressChainId).error(e);
                }
                if (null != coinData) {
                    //更新账户状态
                    String nonce8BytesStr = LedgerUtil.getNonceEncodeByTx(tx);
                    List<CoinFrom> froms = coinData.getFrom();
                    for (CoinFrom from : froms) {
                        if (LedgerUtil.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                            continue;
                        }
                        if (from.getLocked() == 0) {
                            try {
                                //删除备份的花费nonce值。
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
        //回滚未确认交易,就是回滚未确认nonce值
        CoinData coinData = CoinDataUtil.parseCoinData(transaction.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接返回
            return true;
        }
        List<CoinFrom> froms = coinData.getFrom();
        String txHash = transaction.getHash().toHex();
        for (CoinFrom from : froms) {
            if (LedgerUtil.isNotLocalChainAccount(addressChainId, from.getAddress())) {
                //非本地网络账户地址,不进行处理
                if (LedgerUtil.isCrossTx(transaction.getType())) {
                    //非本地网络账户地址,不进行处理
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
     * 清除区块提交时候的缓存数据
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
