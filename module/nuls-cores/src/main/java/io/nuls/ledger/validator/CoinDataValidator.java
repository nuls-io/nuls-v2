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
package io.nuls.ledger.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.TempAccountNonce;
import io.nuls.ledger.model.ValidateResult;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.AccountStateUnconfirmed;
import io.nuls.ledger.model.po.TxUnconfirmed;
import io.nuls.ledger.model.po.sub.FreezeHeightState;
import io.nuls.ledger.model.po.sub.FreezeLockTimeState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.TransactionService;
import io.nuls.ledger.service.UnconfirmedStateService;
import io.nuls.ledger.service.processor.TxLockedProcessor;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.CoinDataUtil;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * validate Coin Data
 * Created by wangkun23 on 2018/11/22.
 * updatge  by lanjinsheng on 2018/12/28.
 *
 * @author lanjinsheng
 */
@Component
public class CoinDataValidator {
    /**
     * key String:chainId
     * value:Map<key是交易hash  value是欲提交交易>
     */
    private Map<String, Map<String, String>> chainsBatchValidateTxMap = new ConcurrentHashMap<>();
    /**
     * key String:chainId
     * value map :key是账号资产 value是待确认支出列表
     */
    private Map<String, Map<String, List<TempAccountNonce>>> chainsAccountNonceMap = new ConcurrentHashMap<>();
    /**
     * key String:chainId
     * value map :key是账号资产 value是待确认账户
     */
    private Map<String, Map<String, AccountState>> chainsAccountStateMap = new ConcurrentHashMap<>();
    /**
     * key String:chainId
     * value map :key是账号资产 value是时间锁定信息
     */
    private Map<String, Map<String, List<FreezeLockTimeState>>> chainsLockedTimeMap = new ConcurrentHashMap<>();
    /**
     * key String:chainId
     * value map :key是账号资产 value是时间锁定信息
     */
    private Map<String, Map<String, List<FreezeHeightState>>> chainsLockedHeightMap = new ConcurrentHashMap<>();


    @Autowired
    private AccountStateService accountStateService;

    @Autowired
    private UnconfirmedStateService unconfirmedStateService;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private Repository repository;
    @Autowired
    private TxLockedProcessor txLockedProcessor;

    public Map<String, String> getBatchValidateTxMap(int addressChainId) {
        return chainsBatchValidateTxMap.get(String.valueOf(addressChainId));
    }

    public Map<String, List<TempAccountNonce>> getAccountBalanceValidateMap(int addressChainId) {
        return chainsAccountNonceMap.get(String.valueOf(addressChainId));
    }

    public Map<String, AccountState> getAccountValidateMap(int addressChainId) {
        return chainsAccountStateMap.get(String.valueOf(addressChainId));
    }

    public Map<String, List<FreezeLockTimeState>> getFreezeLockTimeValidateMap(int addressChainId) {
        if (null == chainsLockedTimeMap.get(String.valueOf(addressChainId))) {
            chainsLockedTimeMap.put(String.valueOf(addressChainId), new ConcurrentHashMap<>());
        }
        return chainsLockedTimeMap.get(String.valueOf(addressChainId));
    }

    public List<FreezeLockTimeState> getFreezeLockTimeValidateList(Map<String, List<FreezeLockTimeState>> timeLockedMap, String assetKey) {
        List<FreezeLockTimeState> timeStateList = timeLockedMap.get(assetKey);
        if (null == timeStateList) {
            timeStateList = new ArrayList<>();
            timeLockedMap.put(assetKey, timeStateList);
        }
        return timeStateList;
    }

    public Map<String, List<FreezeHeightState>> getFreezeLockHeightValidateMap(int addressChainId) {
        if (null == chainsLockedHeightMap.get(String.valueOf(addressChainId))) {
            chainsLockedHeightMap.put(String.valueOf(addressChainId), new ConcurrentHashMap<>());
        }
        return chainsLockedHeightMap.get(String.valueOf(addressChainId));
    }

    public List<FreezeHeightState> getFreezeLockHeightValidateList(Map<String, List<FreezeHeightState>> heightMap, String assetKey) {
        List<FreezeHeightState> heightStateList = heightMap.get(assetKey);
        if (null == heightStateList) {
            heightStateList = new ArrayList<>();
            heightMap.put(assetKey, heightStateList);
        }
        return heightStateList;
    }


    /**
     * 开始批量校验
     */
    public boolean beginBatchPerTxValidate(int chainId) {
        Map<String, String> batchValidateTxMap = getBatchValidateTxMap(chainId);
        if (null == batchValidateTxMap) {
            batchValidateTxMap = new ConcurrentHashMap<>(1024);
            chainsBatchValidateTxMap.put(String.valueOf(chainId), batchValidateTxMap);
        }
        Map<String, List<TempAccountNonce>> accountBalanceValidateTxMap = getAccountBalanceValidateMap(chainId);
        if (null == accountBalanceValidateTxMap) {
            accountBalanceValidateTxMap = new ConcurrentHashMap<>(1024);
            chainsAccountNonceMap.put(String.valueOf(chainId), accountBalanceValidateTxMap);
        }
        Map<String, AccountState> accountStateMap = getAccountValidateMap(chainId);
        if (null == accountStateMap) {
            accountStateMap = new ConcurrentHashMap<>(1024);
            chainsAccountStateMap.put(String.valueOf(chainId), accountStateMap);
        }
        Map<String, List<FreezeLockTimeState>> timeMap = getFreezeLockTimeValidateMap(chainId);
        if (null == timeMap) {
            timeMap = new ConcurrentHashMap<>(1024);
            chainsLockedTimeMap.put(String.valueOf(chainId), timeMap);
        }

        Map<String, List<FreezeHeightState>> heightMap = getFreezeLockHeightValidateMap(chainId);
        if (null == heightMap) {
            heightMap = new ConcurrentHashMap<>(1024);
            chainsLockedHeightMap.put(String.valueOf(chainId), heightMap);
        }

        batchValidateTxMap.clear();
        accountBalanceValidateTxMap.clear();
        accountStateMap.clear();
        timeMap.clear();
        heightMap.clear();
        return true;

    }

    /**
     * 开始批量校验,整个区块校验，场景：接收到的外部的区块包
     */
    public boolean blockValidate(int chainId, long height, List<Transaction> txs) {
        LoggerUtil.logger(chainId).debug("blocksValidate chainId={},height={},txsNumber={}", chainId, height, txs.size());
        long currentDbHeight = repository.getBlockHeight(chainId);
        if ((height - currentDbHeight) > 1 || (height - currentDbHeight) <= 0) {
            LoggerUtil.logger(chainId).error("addressChainId ={},blockHeight={},ledgerBlockHeight={}", chainId, height, currentDbHeight);
            return false;
        }
        Set<String> batchValidateTxSet = new HashSet<>(txs.size());
        Map<String, List<TempAccountNonce>> accountValidateTxMap = new HashMap<>(1024);
        Map<String, AccountState> accountStateMap = new HashMap<>(1024);
        Map<String, Object> lockedCancelNonceMap = new HashMap<>(32);
        Map<String, List<FreezeLockTimeState>> lockedTimeMap = new ConcurrentHashMap<String, List<FreezeLockTimeState>>();
        Map<String, List<FreezeHeightState>> lockedHeightMap = new ConcurrentHashMap<String, List<FreezeHeightState>>();

        for (Transaction tx : txs) {
            tx.setBlockHeight(height);
            if (LoggerUtil.logger(chainId).isDebugEnabled()) {
                LoggerUtil.logger(chainId).debug("[TEST] blocksValidate tx type: {}, hash: {}", tx.getType(), tx.getHash().toHex());
            }
            ValidateResult validateResult = blockTxsValidate(chainId, tx, batchValidateTxSet, accountValidateTxMap, accountStateMap, lockedCancelNonceMap,
                    lockedTimeMap, lockedHeightMap);
            if (!validateResult.isSuccess()) {
                LoggerUtil.logger(chainId).error("code={},msg={}", validateResult.getValidateCode(), validateResult.getValidateDesc());
                return false;
            }
        }
        //遍历余额判断
        for (Map.Entry<String, AccountState> entry : accountStateMap.entrySet()) {
            //缓存数据
            if (BigIntegerUtils.isLessThan(entry.getValue().getAvailableAmount(), BigInteger.ZERO)) {
                //余额不足
                logger(chainId).info("{}==balance is not enough", entry.getKey());
                return false;
            }
        }
        return true;
    }


    /**
     * 批量逐笔校验
     * 批量校验 非解锁交易，余额校验与coindata校验一致,从数据库获取金额校验。
     * nonce校验与coindata不一样，是从批量累计中获取，进行批量连贯性校验。
     * 解锁交易的验证与coidate一致。
     * <p>
     * 批量校验的过程中所有错误按恶意双花来进行处理，
     * 返回VALIDATE_DOUBLE_EXPENSES_CODE
     *
     * @param chainId
     * @param tx
     * @return ValidateResult
     */
    public ValidateResult bathValidatePerTx(int chainId, Transaction tx) {
        Map<String, String> batchValidateTxMap = getBatchValidateTxMap(chainId);
        Map<String, List<TempAccountNonce>> accountBalanceValidateTxMap = getAccountBalanceValidateMap(chainId);
        return confirmedTxValidate(chainId, tx, batchValidateTxMap, accountBalanceValidateTxMap);

    }

    /**
     * 未确认交易数据处理
     *
     * @param transaction
     */

    public ValidateResult verifyCoinData(int addressChainId, Transaction transaction) throws Exception {
        /*未确认交易的校验*/
        ValidateResult validateResult = validateCoinData(addressChainId, transaction);
        if (!validateResult.isSuccess()) {
            LoggerUtil.logger(addressChainId).error("validateResult = {}={}", validateResult.getValidateCode(), validateResult.getValidateDesc());
            return validateResult;
        }
        return ValidateResult.getSuccess();
    }

    /**
     * 打包单笔校验
     *
     * @param chainId
     * @param nonce8Bytes
     * @param coinFroms
     * @param accountValidateTxMap
     * @param accountStateMap
     * @param balanceValidateMap
     * @return
     */
    private ValidateResult analysisFromCoinPerTx(int chainId, int txType, long blockHeight, byte[] nonce8Bytes,
                                                 List<CoinFrom> coinFroms, Map<String, List<TempAccountNonce>> accountValidateTxMap,
                                                 Map<String, AccountState> accountStateMap, Map<String, BigInteger> balanceValidateMap) {
        // 判断硬分叉,需要一个高度
        long hardForkingHeight = 878000;
        boolean forked = blockHeight <= 0 || blockHeight > hardForkingHeight;

        for (CoinFrom coinFrom : coinFroms) {
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //非本地网络账户地址,不进行处理
                    continue;
                } else {
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce()), "address Not local chain Exception"});
                }
            }
            if (AddressTool.isBlackHoleAddress(LedgerConstant.blackHolePublicKey, chainId, coinFrom.getAddress())) {
                return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce()), "address is blackHoleAddress Exception"});
            }
            if (forked && LedgerUtil.isBlackHoleAddress(chainId, coinFrom.getAddress())) {
                return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce()), "address is blackHoleAddress Exception[x]"});
            }
            String assetKey = LedgerUtil.getKeyStr(address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            AccountState accountState = accountStateMap.get(assetKey);
            List<FreezeLockTimeState> timeStates = getFreezeLockTimeValidateList(getFreezeLockTimeValidateMap(chainId), assetKey);
            List<FreezeHeightState> heightStates = getFreezeLockHeightValidateList(getFreezeLockHeightValidateMap(chainId), assetKey);
            if (null == accountState) {
                accountState = accountStateService.getAccountStateReCal(address, chainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                accountStateMap.put(assetKey, accountState);
                timeStates.addAll(accountState.getFreezeLockTimeStates());
                heightStates.addAll(accountState.getFreezeHeightStates());
            }
            BigInteger availableAmount = accountState.getAvailableAmount();
            balanceValidateMap.computeIfAbsent(assetKey, a -> availableAmount);
            //判断是否是解锁操作
            if (coinFrom.getLocked() == 0) {
                ValidateResult validateResult = isValidateCommonTxBatch(chainId, accountState, coinFrom, nonce8Bytes, accountValidateTxMap);
                if (!validateResult.isSuccess()) {
                    logger(chainId).error("fail tx type:" + txType);
                    return validateResult;
                }
                balanceValidateMap.computeIfPresent(assetKey, (k , v) -> v.subtract(coinFrom.getAmount()));
            } else {
                //解锁交易，需要从from 里去获取需要的高度数据或时间数据，进行校验
                //解锁交易只需要从已确认的数据中去获取数据进行校验
                if (!isValidateFreezeTxWithTemp(timeStates, heightStates, coinFrom.getLocked(), coinFrom.getAmount(), coinFrom.getNonce())) {
                    return ValidateResult.getResult(LedgerErrorCode.DOUBLE_EXPENSES, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce())});
                }
            }
        }
        return ValidateResult.getSuccess();
    }

    /**
     * 只是对金额进行处理
     * 打包单笔校验
     *
     * @param chainId
     * @param coinTos
     * @param accountStateMap
     * @return
     */
    private ValidateResult analysisToCoinPerTx(int chainId, int txType, List<CoinTo> coinTos,
                                               Map<String, AccountState> accountStateMap,
                                               Map<String, List<FreezeLockTimeState>> timeStatesMap,
                                               Map<String, List<FreezeHeightState>> heightStatesMap,
                                               Map<String, BigInteger> balanceValidateMap) {
        for (CoinTo coinTo : coinTos) {
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinTo.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //非本地网络账户地址,不进行处理
                    continue;
                } else {
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{LedgerUtil.getRealAddressStr(coinTo.getAddress()), "--", "address Not local chain Exception"});
                }
            }
            String address = LedgerUtil.getRealAddressStr(coinTo.getAddress());
            String assetKey = LedgerUtil.getKeyStr(address, coinTo.getAssetsChainId(), coinTo.getAssetsId());
            AccountState accountState = accountStateMap.get(assetKey);
            List<FreezeLockTimeState> timeList = getFreezeLockTimeValidateList(timeStatesMap, assetKey);
            List<FreezeHeightState> heightList = getFreezeLockHeightValidateList(heightStatesMap, assetKey);
            if (null == accountState) {
                accountState = accountStateService.getAccountStateReCal(address, chainId, coinTo.getAssetsChainId(), coinTo.getAssetsId());
                accountStateMap.put(assetKey, accountState);
                timeList.addAll(accountState.getFreezeLockTimeStates());
                heightList.addAll(accountState.getFreezeHeightStates());
            }
            BigInteger availableAmount = accountState.getAvailableAmount();
            balanceValidateMap.computeIfAbsent(assetKey, a -> availableAmount);
            //判断是否是解锁操作
            if (coinTo.getLockTime() == 0) {
                // 可用余额增加计算
                balanceValidateMap.computeIfPresent(assetKey, (k , v) -> v.add(coinTo.getAmount()));
            }
        }
        return ValidateResult.getSuccess();
    }

    /**
     * @param chainId
     * @param tx
     * @param batchValidateTxMap
     * @return
     */
    public ValidateResult confirmedTxValidate(int chainId, Transaction tx, Map<String, String> batchValidateTxMap,
                                              Map<String, List<TempAccountNonce>> accountValidateTxMap) {
        Map<String, AccountState> accountStateMap = getAccountValidateMap(chainId);
        Map<String, BigInteger> balanceValidateMap = new HashMap<>(64);
        //先校验，再逐笔放入缓存
        //交易的 hash值如果已存在，返回false，交易的from coin nonce 如果不连续，则存在双花。
        String txHash = tx.getHash().toHex();
        int txType = tx.getType();
        if (null != batchValidateTxMap.get(txHash)) {
            logger(chainId).error("{} tx exist!", txHash);
            return ValidateResult.getResult(LedgerErrorCode.TX_EXIST, new String[]{"--", txHash});
        }
        //判断是否已经在打包或已完成的交易
        try {
            if (transactionService.hadTxExist(chainId, txHash)) {
                return ValidateResult.getResult(LedgerErrorCode.TX_EXIST, new String[]{"--", txHash});
            }
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
            return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{"--", txHash, "unknown error"});
        }
        CoinData coinData = CoinDataUtil.parseCoinData(tx.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接返回
            batchValidateTxMap.put(txHash, txHash);
            return ValidateResult.getSuccess();
        }
        if (!validateTxAmount(coinData, txType)) {
            return ValidateResult.getResult(LedgerErrorCode.TX_AMOUNT_INVALIDATE, new String[]{txHash});
        }
        List<CoinFrom> coinFroms = coinData.getFrom();
        List<CoinTo> coinTos = coinData.getTo();

        byte[] nonce8Bytes = LedgerUtil.getNonceByTx(tx);
        if (logger(chainId).isDebugEnabled()) {
            logger(chainId).debug("[TEST] confirmedTxValidate txType: {}, txHash: {}, nonce: {}", txType, txHash, HexUtil.encode(nonce8Bytes));
        }
        Map<String, List<FreezeLockTimeState>> timeStatesMap = getFreezeLockTimeValidateMap(chainId);
        Map<String, List<FreezeHeightState>> heightStatesMap = getFreezeLockHeightValidateMap(chainId);

        ValidateResult validateResult = analysisFromCoinPerTx(chainId, txType, tx.getBlockHeight(), nonce8Bytes, coinFroms, accountValidateTxMap,accountStateMap, balanceValidateMap);
        if (!validateResult.isSuccess()) {
            return validateResult;
        }
        ValidateResult toCoinValidateResult = analysisToCoinPerTx(chainId, txType, coinTos, accountStateMap, timeStatesMap, heightStatesMap, balanceValidateMap);
        if (!toCoinValidateResult.isSuccess()) {
            return validateResult;
        }
        //遍历余额判断
        for (Map.Entry<String, BigInteger> entry : balanceValidateMap.entrySet()) {
            //缓存数据
            if (BigIntegerUtils.isLessThan(entry.getValue(), BigInteger.ZERO)) {
                //余额不足
                logger(chainId).info("balance is not enough:{}===availableAmount={}",
                        entry.getKey(),
                        entry.getValue()
                );
                return ValidateResult.getResult(LedgerErrorCode.BALANCE_NOT_ENOUGH, new String[]{entry.getKey(),
                        BigIntegerUtils.bigIntegerToString(entry.getValue())});
            }
        }

        // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= 全验证通过后，存储数据 -=-=-=-=-===-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

        for (int i = 0, length = coinFroms.size(); i < length; i++) {
            CoinFrom coinFrom = coinFroms.get(i);
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //非本地网络账户地址,不进行处理
                    continue;
                }
            }
            String assetKey = LedgerUtil.getKeyStr(address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            AccountState accountState = accountStateMap.get(assetKey);
            List<FreezeLockTimeState> timeStates = getFreezeLockTimeValidateList(timeStatesMap, assetKey);
            List<FreezeHeightState> heightStates = getFreezeLockHeightValidateList(heightStatesMap, assetKey);

            if (null == accountState) {
                accountState = accountStateService.getAccountStateReCal(address, chainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                accountStateMap.put(assetKey, accountState);
                timeStates.addAll(accountState.getFreezeLockTimeStates());
                heightStates.addAll(accountState.getFreezeHeightStates());
            }

            //判断是否是解锁操作
            if (coinFrom.getLocked() == 0) {
                List<TempAccountNonce> list = accountValidateTxMap.computeIfAbsent(assetKey, a -> new ArrayList<>());
                list.add(new TempAccountNonce(assetKey, coinFrom.getNonce(), nonce8Bytes));
                accountState.addTotalFromAmount(coinFrom.getAmount());
            } else {
                //校验通过,将缓存处理
                txLockedProcessor.processCoinData(coinFrom, coinFrom.getNonce(), txHash, timeStates, heightStates, address, true);
            }
        }
        for (CoinTo coinTo : coinTos) {
            String address = LedgerUtil.getRealAddressStr(coinTo.getAddress());
            String assetKey = LedgerUtil.getKeyStr(address, coinTo.getAssetsChainId(), coinTo.getAssetsId());
            AccountState accountState = accountStateMap.get(assetKey);
            List<FreezeLockTimeState> timeList = getFreezeLockTimeValidateList(timeStatesMap, assetKey);
            List<FreezeHeightState> heightList = getFreezeLockHeightValidateList(heightStatesMap, assetKey);
            if (null == accountState) {
                accountState = accountStateService.getAccountStateReCal(address, chainId, coinTo.getAssetsChainId(), coinTo.getAssetsId());
                accountStateMap.put(assetKey, accountState);
                timeList.addAll(accountState.getFreezeLockTimeStates());
                heightList.addAll(accountState.getFreezeHeightStates());
            }
            //判断是否是解锁操作
            if (coinTo.getLockTime() == 0) {
                accountState.addTotalToAmount(coinTo.getAmount());
            } else {
                //校验通过,将缓存处理
                txLockedProcessor.processCoinData(coinTo, LedgerUtil.getNonceDecodeByTxHash(txHash), txHash, timeList, heightList, address, false);
            }
        }

        batchValidateTxMap.put(txHash, txHash);
        return ValidateResult.getSuccess();
    }

    /**
     * 进行普通交易的coindata 校验，未确认校验的提交校验
     *
     * @param accountState
     * @param address
     * @param fromAmount
     * @param fromNonce
     * @return
     */
    private ValidateResult validateCommonCoinData(int addressChainId, int assetChainId, int assetId, AccountState accountState, String address, BigInteger fromAmount, byte[] fromNonce, byte[] txNonce, boolean containUncomfirmedAmount) {
        AccountStateUnconfirmed accountStateUnconfirmed = null;
        if (containUncomfirmedAmount) {
            accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedInfo(address, addressChainId, assetChainId, assetId, accountState);
        } else {
            accountStateUnconfirmed = unconfirmedStateService.getUnconfirmedJustNonce(address, addressChainId, assetChainId, assetId, accountState);
        }
        byte[] preNonce = null;
        BigInteger amount = BigInteger.ZERO;
        if (null == accountStateUnconfirmed) {
            //新建
            preNonce = accountState.getNonce();
            amount = accountState.getAvailableAmount();
        } else {
            preNonce = accountStateUnconfirmed.getNonce();
            amount = accountState.getAvailableAmount().subtract(accountStateUnconfirmed.getAmount());
        }
        String fromNonceStr = LedgerUtil.getNonceEncode(fromNonce);
        //直接连接上未确认nonce了
        if (LedgerUtil.equalsNonces(fromNonce, preNonce)) {
            if (BigIntegerUtils.isLessThan(amount, fromAmount)) {
                logger(addressChainId).error("dbAmount={},fromAmount={},balance is not enough", BigIntegerUtils.bigIntegerToString(amount), BigIntegerUtils.bigIntegerToString(fromAmount));
                return ValidateResult.getResult(LedgerErrorCode.BALANCE_NOT_ENOUGH, new String[]{address + "-" + assetChainId + "-" + assetId,
                        BigIntegerUtils.bigIntegerToString(amount.subtract(fromAmount))});
            }
            return ValidateResult.getSuccess();
        }

        try {
            //数据库已经不为初始值了，则这笔交易可以认为双花
            if (LedgerUtil.equalsNonces(fromNonce, LedgerConstant.getInitNonceByte())) {
                logger(addressChainId).info("DOUBLE_EXPENSES_CODE address={},fromNonceStr={},dbNonce={},tx={}", address, fromNonceStr, LedgerUtil.getNonceEncode(preNonce), LedgerUtil.getNonceEncode(txNonce));
                return ValidateResult.getResult(LedgerErrorCode.DOUBLE_EXPENSES, new String[]{address, fromNonceStr});
            }
            //数据nonce值== 当前提交的hash值
            if (LedgerUtil.equalsNonces(preNonce, txNonce)) {
                logger(addressChainId).info("DOUBLE_EXPENSES_CODE address={},fromNonceStr={},dbNonce={},tx={}", address, fromNonceStr, LedgerUtil.getNonceEncode(preNonce), LedgerUtil.getNonceEncode(txNonce));
                return ValidateResult.getResult(LedgerErrorCode.DOUBLE_EXPENSES, new String[]{address, fromNonceStr});
            }
            //上面没连接上，但是fromNonce又存储过，则双花了
            if (transactionService.fromNonceExist(addressChainId, LedgerUtil.getAccountNoncesStrKey(address, assetChainId, assetId, fromNonceStr))) {
                logger(addressChainId).info("DOUBLE_EXPENSES_CODE address={},fromNonceStr={},tx={} fromNonce exist", address, fromNonceStr, LedgerUtil.getNonceEncode(txNonce));
                return ValidateResult.getResult(LedgerErrorCode.DOUBLE_EXPENSES, new String[]{address, fromNonceStr});
            }
        } catch (Exception e) {
            LoggerUtil.logger(addressChainId).error(e);
            return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromNonceStr, "exception:" + e.getMessage()});
        }
        //孤儿交易了，这笔交易不清楚状况，是孤儿
//        logger(addressChainId).debug("ORPHAN #############address={},fromNonceStr={},dbNonce={},tx={}", address, fromNonceStr, LedgerUtil.getNonceEncode(preNonce), LedgerUtil.getNonceEncode(txNonce));
        return ValidateResult.getResult(LedgerErrorCode.ORPHAN, new String[]{address, fromNonceStr, LedgerUtil.getNonceEncode(preNonce)});
    }

    /**
     * 进行普通交易的批量校验
     * 与未确认的单笔交易校验不同的是，批量校验要校验批量池中的nonce连续性
     *
     * @param accountState
     * @param coinFrom
     * @param txNonce
     * @return
     */
    private ValidateResult isValidateCommonTxBatch(int chainId, AccountState accountState, CoinFrom coinFrom, byte[] txNonce,
                                                   Map<String, List<TempAccountNonce>> accountValidateTxMap) {
        String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
        String assetKey = LedgerUtil.getKeyStr(address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
        String fromCoinNonceStr = LedgerUtil.getNonceEncode(coinFrom.getNonce());
        if (LedgerUtil.equalsNonces(coinFrom.getNonce(), txNonce)) {
            //nonce 重复了
            logger(chainId).info("{}=={}=={}== nonce is repeat", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromCoinNonceStr, "nonce repeat"});
        }
        //不是解锁操作
        //从批量校验池中获取缓存交易
        List<TempAccountNonce> list = accountValidateTxMap.get(assetKey);
        if (null == list || list.isEmpty()) {
            //从头开始处理
            if (!LedgerUtil.equalsNonces(accountState.getNonce(), coinFrom.getNonce())) {
                logger(chainId).error("package validate fail(validateCommonTxBatch):{}=={}=={}==nonce is error!dbNonce:{}!=fromNonce:{},tx={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), LedgerUtil.getNonceEncode(accountState.getNonce()), fromCoinNonceStr, LedgerUtil.getNonceEncode(txNonce));
                //nonce不连续按孤儿处理，双花场景由交易模块来进行删除
                return ValidateResult.getResult(LedgerErrorCode.ORPHAN, new String[]{address, fromCoinNonceStr, LedgerUtil.getNonceEncode(accountState.getNonce())});
            }
        } else {
            //从已有的缓存数据中获取对象进行操作,nonce必须连贯
            TempAccountNonce tempAccountState = list.get(list.size() - 1);
            if (!LedgerUtil.equalsNonces(tempAccountState.getNextNonce(), coinFrom.getNonce())) {
                logger(chainId).error("package validate fail(validateCommonTxBatch):{}=={}=={}==nonce is error!tempNonce:{}!=fromNonce:{},tx={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), LedgerUtil.getNonceEncode(tempAccountState.getNextNonce()), fromCoinNonceStr, LedgerUtil.getNonceEncode(txNonce));
                return ValidateResult.getResult(LedgerErrorCode.ORPHAN, new String[]{address, fromCoinNonceStr, "last pool nonce=" + LedgerUtil.getNonceEncode(tempAccountState.getNextNonce())});
            }
        }
        return ValidateResult.getSuccess();
    }

    private ValidateResult analysisFromCoinBlokTx(int chainId, int txType, long blockHeight, byte[] txNonce, List<CoinFrom> coinFroms,
                                                  Map<String, List<TempAccountNonce>> accountValidateTxMap, Map<String, AccountState> accountStateMap,
                                                  Map<String, Object> lockedCancelNonceMap,
                                                  Map<String, List<FreezeLockTimeState>> timeLockMap, Map<String, List<FreezeHeightState>> heightLockMap,
                                                  String txHash, Map<String, BigInteger> balanceValidateMap) {
        // 判断硬分叉,需要一个高度
        long hardForkingHeight = 878000;
        boolean forked = blockHeight <= 0 || blockHeight > hardForkingHeight;

        for (CoinFrom coinFrom : coinFroms) {
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //非本地网络账户地址,不进行处理
                    continue;
                } else {
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, "--", "address Not local chain Exception"});
                }
            }
            if (AddressTool.isBlackHoleAddress(LedgerConstant.blackHolePublicKey, chainId, coinFrom.getAddress())) {
                return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce()), "address is blackHoleAddress Exception"});
            }
            if (forked && LedgerUtil.isBlackHoleAddress(chainId, coinFrom.getAddress())) {
                return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce()), "address is blackHoleAddress Exception[x]"});
            }

            String assetKey = LedgerUtil.getKeyStr(address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            AccountState accountState = accountStateMap.get(assetKey);
            List<FreezeLockTimeState> timeList = getFreezeLockTimeValidateList(timeLockMap, assetKey);
            List<FreezeHeightState> heightList = getFreezeLockHeightValidateList(heightLockMap, assetKey);
            if (null == accountState) {
                accountState = accountStateService.getAccountStateReCal(address, chainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                accountStateMap.put(assetKey, accountState);
                timeList.addAll(accountState.getFreezeLockTimeStates());
                heightList.addAll(accountState.getFreezeHeightStates());
            }
            BigInteger availableAmount = accountState.getAvailableAmount();
            // 可用余额初始化
            balanceValidateMap.computeIfAbsent(assetKey, a -> availableAmount);
            //判断是否是解锁操作
            if (coinFrom.getLocked() == 0) {
                //不是解锁操作
                String fromCoinNonce = LedgerUtil.getNonceEncode(coinFrom.getNonce());
                if (LedgerUtil.equalsNonces(coinFrom.getNonce(), txNonce)) {
                    //nonce 重复了
                    logger(chainId).info("{}=={}=={}== nonce is repeat", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromCoinNonce, "nonce repeat"});
                }
                //从批量校验池中获取缓存交易
                List<TempAccountNonce> list = accountValidateTxMap.get(assetKey);
                if (null == list || list.isEmpty()) {
                    //从头开始处理
                    if (!LedgerUtil.equalsNonces(accountState.getNonce(), coinFrom.getNonce())) {
                        logger(chainId).error("validate fail:(isBlockValidateCommonTx failed)：{}=={}=={}==nonce is error!dbNonce:{}!=fromNonce:{},tx={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), LedgerUtil.getNonceEncode(accountState.getNonce()), fromCoinNonce, LedgerUtil.getNonceEncode(txNonce));
                        //判断是否fromNonce是否已经存储了,如果存储了，则这笔是异常交易双花
                        logger(chainId).error("txType:{}, hash:{}", txType, txHash);
                        return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromCoinNonce, "dbNonce=" + LedgerUtil.getNonceEncode(accountState.getNonce())});
                    }
                } else {
                    //从已有的缓存数据中获取对象进行操作,nonce必须连贯
                    TempAccountNonce tempAccountState = list.get(list.size() - 1);
                    if (!LedgerUtil.equalsNonces(tempAccountState.getNextNonce(), coinFrom.getNonce())) {
                        logger(chainId).info("isValidateCommonTxBatch {}=={}=={}==nonce is error!tempNonce:{}!=fromNonce:{},tx={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), LedgerUtil.getNonceEncode(tempAccountState.getNextNonce()), fromCoinNonce, LedgerUtil.getNonceEncode(txNonce));
                        return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromCoinNonce, "last pool nonce=" + LedgerUtil.getNonceEncode(tempAccountState.getNextNonce())});
                    }
                }
                // 可用余额扣减计算
                balanceValidateMap.computeIfPresent(assetKey, (k , v) -> v.subtract(coinFrom.getAmount()));
            } else {
                //解锁交易，需要从from 里去获取需要的高度数据或时间数据，进行校验
                //解锁交易只需要从已确认的数据中去获取数据进行校验
                String lockedNonce = coinFrom.getAssetsChainId() + "-" + coinFrom.getAssetsId() + "-" + LedgerUtil.getNonceEncode(coinFrom.getNonce());
                if (!isValidateFreezeTxWithTemp(timeList, heightList, coinFrom.getLocked(), coinFrom.getAmount(), coinFrom.getNonce())) {
                    logger(chainId).error("validate fail,locked tx={} address={} lockNonce={} failed", LedgerUtil.getNonceEncode(txNonce), address, lockedNonce);
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, lockedNonce, "validate fail"});
                }
                if (null != lockedCancelNonceMap.get(lockedNonce)) {
                    logger(chainId).error("validate fail,locked tx={} address={} nonce={} repeat", LedgerUtil.getNonceEncode(txNonce), address, lockedNonce);
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, lockedNonce, "validate fail,locked nonce repeat"});
                }
            }
        }
        return ValidateResult.getSuccess();
    }

    public ValidateResult blockTxsValidate(int chainId, Transaction tx, Set<String> batchValidateTxSet, Map<String, List<TempAccountNonce>> accountValidateTxMap,
                                           Map<String, AccountState> accountStateMap, Map<String, Object> lockedCancelNonceMap, Map<String, List<FreezeLockTimeState>> lockedTimeMap,
                                           Map<String, List<FreezeHeightState>> lockedHeightMap) {
        //先校验，再逐笔放入缓存
        //交易的 hash值如果已存在，返回false，交易的from coin nonce 如果不连续，则存在双花。
        String txHash = tx.getHash().toHex();
        int txType = tx.getType();
        if (batchValidateTxSet.contains(txHash)) {
            logger(chainId).error("{} tx exist!", txHash);
            return ValidateResult.getResult(LedgerErrorCode.TX_EXIST, new String[]{"--", txHash});
        }
        try {
            if (transactionService.hadTxExist(chainId, txHash)) {
                logger(chainId).error("{} tx exist!", txHash);
                return ValidateResult.getResult(LedgerErrorCode.TX_EXIST, new String[]{"--", txHash});
            }
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
        }

        CoinData coinData = CoinDataUtil.parseCoinData(tx.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接返回
            batchValidateTxSet.add(txHash);
            return ValidateResult.getSuccess();
        }
        if (!validateTxAmount(coinData, txType)) {
            return ValidateResult.getResult(LedgerErrorCode.TX_AMOUNT_INVALIDATE, new String[]{txHash});
        }

        Map<String, BigInteger> balanceValidateMap = new HashMap<>(64);
        List<CoinFrom> coinFroms = coinData.getFrom();
        List<CoinTo> coinTos = coinData.getTo();
        byte[] txNonce = LedgerUtil.getNonceByTx(tx);
        ValidateResult fromCoinsValidateResult = analysisFromCoinBlokTx(chainId, txType, tx.getBlockHeight(), txNonce, coinFroms, accountValidateTxMap,
                accountStateMap, lockedCancelNonceMap, lockedTimeMap, lockedHeightMap, txHash, balanceValidateMap);
        if (!fromCoinsValidateResult.isSuccess()) {
            logger(chainId).error("from coins error! txtype:{}", txType);
            return fromCoinsValidateResult;
        }
        ValidateResult toCoinValidateResult = analysisToCoinPerTx(chainId, txType, coinTos, accountStateMap, lockedTimeMap, lockedHeightMap, balanceValidateMap);
        if (!toCoinValidateResult.isSuccess()) {
            logger(chainId).error("to coins error!");
            return toCoinValidateResult;
        }

        //遍历余额判断
        for (Map.Entry<String, BigInteger> entry : balanceValidateMap.entrySet()) {
            //缓存数据
            if (BigIntegerUtils.isLessThan(entry.getValue(), BigInteger.ZERO)) {
                //余额不足
                logger(chainId).info("balance is not enough:{}===availableAmount={}",
                        entry.getKey(),
                        entry.getValue()
                );
                return ValidateResult.getResult(LedgerErrorCode.BALANCE_NOT_ENOUGH, new String[]{entry.getKey(),
                        BigIntegerUtils.bigIntegerToString(entry.getValue())});
            }
        }

        // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= 校验通过后，存储数据 -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

        for (CoinFrom coinFrom : coinFroms) {
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //非本地网络账户地址,不进行处理
                    continue;
                }
            }
            String assetKey = LedgerUtil.getKeyStr(address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            AccountState accountState = accountStateMap.get(assetKey);
            List<FreezeLockTimeState> timeList = getFreezeLockTimeValidateList(lockedTimeMap, assetKey);
            List<FreezeHeightState> heightList = getFreezeLockHeightValidateList(lockedHeightMap, assetKey);

            if (null == accountState) {
                accountState = accountStateService.getAccountStateReCal(address, chainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                accountStateMap.put(assetKey, accountState);
                timeList.addAll(accountState.getFreezeLockTimeStates());
                heightList.addAll(accountState.getFreezeHeightStates());
            }

            //判断是否是解锁操作
            if (coinFrom.getLocked() == 0) {
                //不是解锁操作
                //余额累计
                accountState.addTotalFromAmount(coinFrom.getAmount());
                List<TempAccountNonce> list = accountValidateTxMap.computeIfAbsent(assetKey, a -> new ArrayList<>());
                list.add(new TempAccountNonce(assetKey, coinFrom.getNonce(), txNonce));
            } else {
                //解锁交易，需要从from 里去获取需要的高度数据或时间数据，进行校验
                //解锁交易只需要从已确认的数据中去获取数据进行校验
                String lockedNonce = coinFrom.getAssetsChainId() + "-" + coinFrom.getAssetsId() + "-" + LedgerUtil.getNonceEncode(coinFrom.getNonce());
                lockedCancelNonceMap.put(lockedNonce, 1);
                //处理缓存
                txLockedProcessor.processCoinData(coinFrom, coinFrom.getNonce(), txHash, timeList, heightList, address, true);
            }
        }

        for (CoinTo coinTo : coinTos) {
            String address = LedgerUtil.getRealAddressStr(coinTo.getAddress());
            String assetKey = LedgerUtil.getKeyStr(address, coinTo.getAssetsChainId(), coinTo.getAssetsId());
            AccountState accountState = accountStateMap.get(assetKey);
            List<FreezeLockTimeState> timeList = getFreezeLockTimeValidateList(lockedTimeMap, assetKey);
            List<FreezeHeightState> heightList = getFreezeLockHeightValidateList(lockedHeightMap, assetKey);
            if (null == accountState) {
                accountState = accountStateService.getAccountStateReCal(address, chainId, coinTo.getAssetsChainId(), coinTo.getAssetsId());
                accountStateMap.put(assetKey, accountState);
                timeList.addAll(accountState.getFreezeLockTimeStates());
                heightList.addAll(accountState.getFreezeHeightStates());
            }

            //判断是否是解锁操作
            if (coinTo.getLockTime() == 0) {
                accountState.addTotalToAmount(coinTo.getAmount());
            } else {
//           //校验通过,将缓存处理
                txLockedProcessor.processCoinData(coinTo, LedgerUtil.getNonceDecodeByTxHash(txHash), txHash, timeList, heightList, address, false);
            }
        }
        batchValidateTxSet.add(txHash);
        return ValidateResult.getSuccess();
    }

    /**
     * 进行解锁交易的校验
     *
     * @param locked
     * @param accountState
     * @param fromAmount
     * @param fromNonce
     * @return
     */
    private boolean isValidateFreezeTx(byte locked, AccountState accountState, BigInteger fromAmount, byte[] fromNonce) {
        boolean isValidate = false;
        //解锁交易，校验是否存在该笔交易
        if (locked == LedgerConstant.UNLOCKED_TIME) {
            //时间解锁
            List<FreezeLockTimeState> list = accountState.getFreezeLockTimeStates();
            for (FreezeLockTimeState freezeLockTimeState : list) {
                if (LedgerUtil.equalsNonces(freezeLockTimeState.getNonce(), fromNonce) && freezeLockTimeState.getAmount().compareTo(fromAmount) == 0) {
                    //找到交易
                    isValidate = true;
                    break;
                }
            }
        } else if (locked == LedgerConstant.UNLOCKED_HEIGHT) {
            //高度解锁
            List<FreezeHeightState> list = accountState.getFreezeHeightStates();
            for (FreezeHeightState freezeHeightState : list) {
                if (LedgerUtil.equalsNonces(freezeHeightState.getNonce(), fromNonce) && freezeHeightState.getAmount().compareTo(fromAmount) == 0) {
                    //找到交易
                    isValidate = true;
                    break;
                }
            }
        }
        return isValidate;
    }

    private boolean isValidateFreezeTxWithTemp(List<FreezeLockTimeState> timeList, List<FreezeHeightState> heightList, byte locked, BigInteger fromAmount,
                                               byte[] fromNonce) {
        boolean isValidate = false;
        //解锁交易，校验是否存在该笔交易
        if (locked == LedgerConstant.UNLOCKED_TIME) {
            //时间解锁
            if (null != timeList) {
                for (FreezeLockTimeState freezeLockTimeState : timeList) {
                    if (LedgerUtil.equalsNonces(freezeLockTimeState.getNonce(), fromNonce) && freezeLockTimeState.getAmount().compareTo(fromAmount) == 0) {
                        //找到交易
                        isValidate = true;
                        break;
                    }
                }
            }

        } else if (locked == LedgerConstant.UNLOCKED_HEIGHT) {
            //高度解锁
            if (null != heightList) {
                for (FreezeHeightState freezeHeightState : heightList) {
                    if (LedgerUtil.equalsNonces(freezeHeightState.getNonce(), fromNonce) && freezeHeightState.getAmount().compareTo(fromAmount) == 0) {
                        //找到交易
                        isValidate = true;
                        break;
                    }
                }
            }
        }
        return isValidate;
    }

    /**
     * 进行coinData值的校验,在本地交易产生时候进行的校验
     * 即只有未确认交易的校验使用到
     *
     * @param addressChainId
     * @param tx
     * @return
     */
    public ValidateResult validateCoinData(int addressChainId, Transaction tx) throws Exception {
        String txHash = tx.getHash().toHex();
        byte[] txNonce = LedgerUtil.getNonceByTx(tx);
        if (transactionService.hadTxExist(addressChainId, txHash)) {
            return ValidateResult.getResult(LedgerErrorCode.TX_EXIST, new String[]{"--", txHash});
        }
        CoinData coinData = CoinDataUtil.parseCoinData(tx.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接返回
            return ValidateResult.getSuccess();
        }
        if (!validateTxAmount(coinData, tx.getType())) {
            return ValidateResult.getResult(LedgerErrorCode.TX_AMOUNT_INVALIDATE, new String[]{txHash});
        }
        /*
         * 先校验nonce值是否正常
         */
        List<CoinFrom> coinFroms = coinData.getFrom();
        for (CoinFrom coinFrom : coinFroms) {
            if (LedgerUtil.isNotLocalChainAccount(addressChainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(tx.getType())) {
                    //非本地网络账户地址,不进行处理
                    continue;
                } else {
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{LedgerUtil.getRealAddressStr(coinFrom.getAddress()), "--", "address Not local chain Exception"});
                }
            }
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            AccountState accountState = accountStateService.getAccountStateReCal(address, addressChainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            //普通交易
            if (coinFrom.getLocked() == 0) {
                return validateCommonCoinData(addressChainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), accountState, address, coinFrom.getAmount(), coinFrom.getNonce(), txNonce, true);
            } else {
                if (!isValidateFreezeTx(coinFrom.getLocked(), accountState, coinFrom.getAmount(), coinFrom.getNonce())) {
                    //确认交易未找到冻结的交易
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce()), "freeze tx is not exist"});
                }
            }
        }
        return ValidateResult.getSuccess();
    }

    public ValidateResult analysisCoinData(int addressChainId, Transaction tx, Map<String, TxUnconfirmed> accountsMap, byte[] txNonce) throws Exception {
        String txHash = tx.getHash().toHex();
        if (transactionService.hadTxExist(addressChainId, txHash)) {
            return ValidateResult.getResult(LedgerErrorCode.TX_EXIST, new String[]{"--", txHash});
        }
        CoinData coinData = CoinDataUtil.parseCoinData(tx.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接返回
            return ValidateResult.getSuccess();
        }
        /*
         * 先校验nonce值是否正常
         */
        List<CoinFrom> coinFroms = coinData.getFrom();

        for (CoinFrom coinFrom : coinFroms) {
            if (LedgerUtil.isNotLocalChainAccount(addressChainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(tx.getType())) {
                    //非本地网络账户地址,不进行处理
                    continue;
                } else {
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{LedgerUtil.getRealAddressStr(coinFrom.getAddress()), "--", "address Not local chain Exception"});
                }
            }
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            int assetChainId = coinFrom.getAssetsChainId();
            int assetId = coinFrom.getAssetsId();
            String accountKey = LedgerUtil.getKeyStr(address, assetChainId, assetId);
            AccountState accountState = accountStateService.getAccountStateReCal(address, addressChainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            //普通交易
            if (coinFrom.getLocked() == 0) {
                ValidateResult validateResult = validateCommonCoinData(addressChainId, assetChainId, assetId, accountState, address, coinFrom.getAmount(), coinFrom.getNonce(), txNonce, false);
                if (validateResult.isSuccess()) {
                    CoinDataUtil.calTxFromAmount(accountsMap, coinFrom, txNonce, accountKey, address);
                } else {
                    return validateResult;
                }
            } else {
                if (!isValidateFreezeTx(coinFrom.getLocked(), accountState, coinFrom.getAmount(), coinFrom.getNonce())) {
                    //确认交易未找到冻结的交易
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce()), "freeze tx is not exist"});
                }
            }
        }
        return ValidateResult.getSuccess();
    }

    /**
     * 批量打包单笔交易回滚处理
     */
    public boolean rollbackTxValidateStatus(int chainId, Transaction tx) {
        Map<String, List<TempAccountNonce>> accountBalanceValidateTxMap = getAccountBalanceValidateMap(chainId);
        String txHash = tx.getHash().toHex();
        if (null == chainsBatchValidateTxMap.get(txHash)) {
            logger(chainId).info("{} tx not exist!", txHash);
            return true;
        }
        CoinData coinData = CoinDataUtil.parseCoinData(tx.getCoinData());
        if (null == coinData) {
            //例如黄牌交易，直接移除返回.
            chainsBatchValidateTxMap.remove(txHash);
            return true;
        }
        List<CoinFrom> coinFroms = coinData.getFrom();
        byte[] nonce8Bytes = LedgerUtil.getNonceByTx(tx);
        for (CoinFrom coinFrom : coinFroms) {
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(tx.getType())) {
                    //非本地网络账户地址,不进行处理
                    continue;
                } else {
                    LoggerUtil.logger(chainId).error("address={} Not local chain Exception", LedgerUtil.getRealAddressStr(coinFrom.getAddress()));
                    return false;
                }
            }
            //判断是否是解锁操作
            if (coinFrom.getLocked() == 0) {
                String assetKey = LedgerUtil.getKeyStr(LedgerUtil.getRealAddressStr(coinFrom.getAddress()), coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                //回滚accountBalanceValidateTxMap缓存数据
                List<TempAccountNonce> list = accountBalanceValidateTxMap.get(assetKey);
                if (null == list) {
                    continue;
                } else {
                    TempAccountNonce tempAccountState = list.get(list.size() - 1);
                    if (LedgerUtil.equalsNonces(tempAccountState.getNextNonce(), nonce8Bytes)) {
                        list.remove(list.size() - 1);
                    }
                }
            } else {
                //解锁交易,暂无缓存记录
            }
        }
        chainsBatchValidateTxMap.remove(txHash);
        return true;
    }

    /**
     * @param coinData
     * @param txType
     * @return
     */
    public boolean validateTxAmount(CoinData coinData, int txType) {
        if (txType == TxType.CONTRACT_RETURN_GAS || txType == TxType.COIN_BASE) {
            return true;
        }
        Map<String, BigInteger> assetMap = new HashMap<>();
        List<String> assetKeys = new ArrayList<>();
        List<CoinFrom> froms = coinData.getFrom();
        for (CoinFrom from : froms) {
            String assetKey = from.getAssetsChainId() + "_" + from.getAssetsId();
            if (null == assetMap.get(assetKey)) {
                assetMap.put(assetKey, BigInteger.ZERO);
                assetKeys.add(assetKey);
            }
            String fromKey = assetKey + "from";
            if (null == assetMap.get(fromKey)) {
                assetMap.put(fromKey, from.getAmount());
            } else {
                BigInteger fromAmount = assetMap.get(fromKey).add(from.getAmount());
                assetMap.put(fromKey, fromAmount);
            }
        }
        List<CoinTo> tos = coinData.getTo();
        for (CoinTo to : tos) {
            String assetKey = to.getAssetsChainId() + "_" + to.getAssetsId();
            if (null == assetMap.get(assetKey)) {
                assetMap.put(assetKey, BigInteger.ZERO);
                assetKeys.add(assetKey);
            }
            String toKey = assetKey + "to";
            if (null == assetMap.get(toKey)) {
                assetMap.put(toKey, to.getAmount());
            } else {
                BigInteger fromAmount = assetMap.get(toKey).add(to.getAmount());
                assetMap.put(toKey, fromAmount);
            }
        }
        BigInteger assetKeyFrom, assetKeyTo;
        for (String assetKey : assetKeys) {
            assetKeyFrom = assetMap.get(assetKey + "from");
            assetKeyTo = assetMap.get(assetKey + "to");
            if(null == assetKeyFrom){
                LoggerUtil.COMMON_LOG.error("asset From is null,txType={}",txType);
                return false;
            }
            if (null == assetKeyTo) {
                continue;
            }
            if (BigIntegerUtils.isLessThan(assetKeyFrom, assetKeyTo)) {
                LoggerUtil.COMMON_LOG.error("fromAmount is less than to amount");
                return false;
            }
        }
        return true;
    }
}
