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
     * value:Map<keyIt's a transactionhash  valueI want to submit a transaction>
     */
    private Map<String, Map<String, String>> chainsBatchValidateTxMap = new ConcurrentHashMap<>();
    /**
     * key String:chainId
     * value map :keyIt is an account asset valueIt is a list of pending expenses to be confirmed
     */
    private Map<String, Map<String, List<TempAccountNonce>>> chainsAccountNonceMap = new ConcurrentHashMap<>();
    /**
     * key String:chainId
     * value map :keyIt is an account asset valueIt is a pending confirmation account
     */
    private Map<String, Map<String, AccountState>> chainsAccountStateMap = new ConcurrentHashMap<>();
    /**
     * key String:chainId
     * value map :keyIt is an account asset valueIt's time lock information
     */
    private Map<String, Map<String, List<FreezeLockTimeState>>> chainsLockedTimeMap = new ConcurrentHashMap<>();
    /**
     * key String:chainId
     * value map :keyIt is an account asset valueIt's time lock information
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
     * Start batch verification
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
     * Start batch verification,Whole block verification, scenario：Received external block packets
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
        //Traverse balance judgment
        for (Map.Entry<String, AccountState> entry : accountStateMap.entrySet()) {
            //Caching data
            if (BigIntegerUtils.isLessThan(entry.getValue().getAvailableAmount(), BigInteger.ZERO)) {
                //Insufficient balance
                logger(chainId).info("{}==balance is not enough", entry.getKey());
                return false;
            }
        }
        return true;
    }


    /**
     * Batch check one by one
     * Batch verification Non unlocked transactions, balance verification andcoindataVerify consistency,Retrieve amount verification from the database.
     * nonceChecksumcoindataDifferent, it is obtained from batch accumulation for batch coherence verification.
     * Verification and unlocking of transactionscoidateConsistent.
     * <p>
     * During the batch verification process, all errors are handled as malicious double flowers,
     * returnVALIDATE_DOUBLE_EXPENSES_CODE
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
     * Unconfirmed transaction data processing
     *
     * @param transaction
     */

    public ValidateResult verifyCoinData(int addressChainId, Transaction transaction) throws Exception {
        /*Verification of unconfirmed transactions*/
        ValidateResult validateResult = validateCoinData(addressChainId, transaction);
        if (!validateResult.isSuccess()) {
            LoggerUtil.logger(addressChainId).error("validateResult = {}={}", validateResult.getValidateCode(), validateResult.getValidateDesc());
            return validateResult;
        }
        return ValidateResult.getSuccess();
    }

    /**
     * Package single verification
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
        // Determine hard fork,Need a height
        long hardForkingHeight = 878000;
        boolean forked = blockHeight <= 0 || blockHeight > hardForkingHeight;

        for (CoinFrom coinFrom : coinFroms) {
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //Non local network account address,Not processed
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
            //Determine if it is an unlock operation
            if (coinFrom.getLocked() == 0) {
                ValidateResult validateResult = isValidateCommonTxBatch(chainId, accountState, coinFrom, nonce8Bytes, accountValidateTxMap);
                if (!validateResult.isSuccess()) {
                    logger(chainId).error("fail tx type:" + txType);
                    return validateResult;
                }
                balanceValidateMap.computeIfPresent(assetKey, (k , v) -> v.subtract(coinFrom.getAmount()));
            } else {
                //To unlock a transaction, you need to access it fromfrom Go inside to obtain the required height or time data for verification
                //Unlocking transactions only requires obtaining data from confirmed data for verification
                if (!isValidateFreezeTxWithTemp(timeStates, heightStates, coinFrom.getLocked(), coinFrom.getAmount(), coinFrom.getNonce())) {
                    return ValidateResult.getResult(LedgerErrorCode.DOUBLE_EXPENSES, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce())});
                }
            }
        }
        return ValidateResult.getSuccess();
    }

    /**
     * Just processing the amount
     * Package single verification
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
                    //Non local network account address,Not processed
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
            //Determine if it is an unlock operation
            if (coinTo.getLockTime() == 0) {
                // Calculation of available balance increase
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
        //Verify first, then put each transaction into the cache
        //Transactional hashIf the value already exists, returnfalse, transactionalfrom coin nonce If not continuous, there are double flowers.
        String txHash = tx.getHash().toHex();
        int txType = tx.getType();
        if (null != batchValidateTxMap.get(txHash)) {
            logger(chainId).error("{} tx exist!", txHash);
            return ValidateResult.getResult(LedgerErrorCode.TX_EXIST, new String[]{"--", txHash});
        }
        //Determine if the transaction is already being packaged or completed
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
            //For example, in a yellow card transaction, return directly
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
        //Traverse balance judgment
        for (Map.Entry<String, BigInteger> entry : balanceValidateMap.entrySet()) {
            //Caching data
            if (BigIntegerUtils.isLessThan(entry.getValue(), BigInteger.ZERO)) {
                //Insufficient balance
                logger(chainId).info("balance is not enough:{}===availableAmount={}",
                        entry.getKey(),
                        entry.getValue()
                );
                return ValidateResult.getResult(LedgerErrorCode.BALANCE_NOT_ENOUGH, new String[]{entry.getKey(),
                        BigIntegerUtils.bigIntegerToString(entry.getValue())});
            }
        }

        // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= After full verification, store the data -=-=-=-=-===-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-

        for (int i = 0, length = coinFroms.size(); i < length; i++) {
            CoinFrom coinFrom = coinFroms.get(i);
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //Non local network account address,Not processed
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

            //Determine if it is an unlock operation
            if (coinFrom.getLocked() == 0) {
                List<TempAccountNonce> list = accountValidateTxMap.computeIfAbsent(assetKey, a -> new ArrayList<>());
                list.add(new TempAccountNonce(assetKey, coinFrom.getNonce(), nonce8Bytes));
                accountState.addTotalFromAmount(coinFrom.getAmount());
            } else {
                //Verification passed,Process cache
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
            //Determine if it is an unlock operation
            if (coinTo.getLockTime() == 0) {
                accountState.addTotalToAmount(coinTo.getAmount());
            } else {
                //Verification passed,Process cache
                txLockedProcessor.processCoinData(coinTo, LedgerUtil.getNonceDecodeByTxHash(txHash), txHash, timeList, heightList, address, false);
            }
        }

        batchValidateTxMap.put(txHash, txHash);
        return ValidateResult.getSuccess();
    }

    /**
     * For conducting regular transactionscoindata Verification, submit verification for unconfirmed verification
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
            //New
            preNonce = accountState.getNonce();
            amount = accountState.getAvailableAmount();
        } else {
            preNonce = accountStateUnconfirmed.getNonce();
            amount = accountState.getAvailableAmount().subtract(accountStateUnconfirmed.getAmount());
        }
        String fromNonceStr = LedgerUtil.getNonceEncode(fromNonce);
        //Directly connect to unconfirmednonceNow
        if (LedgerUtil.equalsNonces(fromNonce, preNonce)) {
            if (BigIntegerUtils.isLessThan(amount, fromAmount)) {
                logger(addressChainId).error("dbAmount={},fromAmount={},balance is not enough", BigIntegerUtils.bigIntegerToString(amount), BigIntegerUtils.bigIntegerToString(fromAmount));
                return ValidateResult.getResult(LedgerErrorCode.BALANCE_NOT_ENOUGH, new String[]{address + "-" + assetChainId + "-" + assetId,
                        BigIntegerUtils.bigIntegerToString(amount.subtract(fromAmount))});
            }
            return ValidateResult.getSuccess();
        }

        try {
            //If the database is no longer at its initial value, then this transaction can be considered as Double Flower
            if (LedgerUtil.equalsNonces(fromNonce, LedgerConstant.getInitNonceByte())) {
                logger(addressChainId).info("DOUBLE_EXPENSES_CODE address={},fromNonceStr={},dbNonce={},tx={}", address, fromNonceStr, LedgerUtil.getNonceEncode(preNonce), LedgerUtil.getNonceEncode(txNonce));
                return ValidateResult.getResult(LedgerErrorCode.DOUBLE_EXPENSES, new String[]{address, fromNonceStr});
            }
            //datanoncevalue== Currently submittedhashvalue
            if (LedgerUtil.equalsNonces(preNonce, txNonce)) {
                logger(addressChainId).info("DOUBLE_EXPENSES_CODE address={},fromNonceStr={},dbNonce={},tx={}", address, fromNonceStr, LedgerUtil.getNonceEncode(preNonce), LedgerUtil.getNonceEncode(txNonce));
                return ValidateResult.getResult(LedgerErrorCode.DOUBLE_EXPENSES, new String[]{address, fromNonceStr});
            }
            //It's not connected up there, butfromNonceIf it has been stored again, it will result in double spending
            if (transactionService.fromNonceExist(addressChainId, LedgerUtil.getAccountNoncesStrKey(address, assetChainId, assetId, fromNonceStr))) {
                logger(addressChainId).info("DOUBLE_EXPENSES_CODE address={},fromNonceStr={},tx={} fromNonce exist", address, fromNonceStr, LedgerUtil.getNonceEncode(txNonce));
                return ValidateResult.getResult(LedgerErrorCode.DOUBLE_EXPENSES, new String[]{address, fromNonceStr});
            }
        } catch (Exception e) {
            LoggerUtil.logger(addressChainId).error(e);
            return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromNonceStr, "exception:" + e.getMessage()});
        }
        //Orphan transaction, the status of this transaction is unclear, it is an orphan
//        logger(addressChainId).debug("ORPHAN #############address={},fromNonceStr={},dbNonce={},tx={}", address, fromNonceStr, LedgerUtil.getNonceEncode(preNonce), LedgerUtil.getNonceEncode(txNonce));
        return ValidateResult.getResult(LedgerErrorCode.ORPHAN, new String[]{address, fromNonceStr, LedgerUtil.getNonceEncode(preNonce)});
    }

    /**
     * Batch verification for regular transactions
     * Unlike unconfirmed single transaction verification, batch verification requires verifying the data in the batch poolnonceContinuity
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
            //nonce Repeated
            logger(chainId).info("{}=={}=={}== nonce is repeat", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromCoinNonceStr, "nonce repeat"});
        }
        //Not an unlock operation
        //Retrieve cached transactions from batch validation pool
        List<TempAccountNonce> list = accountValidateTxMap.get(assetKey);
        if (null == list || list.isEmpty()) {
            //Starting from scratch
            if (!LedgerUtil.equalsNonces(accountState.getNonce(), coinFrom.getNonce())) {
                logger(chainId).error("package validate fail(validateCommonTxBatch):{}=={}=={}==nonce is error!dbNonce:{}!=fromNonce:{},tx={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), LedgerUtil.getNonceEncode(accountState.getNonce()), fromCoinNonceStr, LedgerUtil.getNonceEncode(txNonce));
                //nonceDiscontinuous orphan processing, double flower scenes are deleted by the transaction module
                return ValidateResult.getResult(LedgerErrorCode.ORPHAN, new String[]{address, fromCoinNonceStr, LedgerUtil.getNonceEncode(accountState.getNonce())});
            }
        } else {
            //Retrieve objects from existing cached data for operation,nonceMust be coherent
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
        // Determine hard fork,Need a height
        long hardForkingHeight = 878000;
        boolean forked = blockHeight <= 0 || blockHeight > hardForkingHeight;

        for (CoinFrom coinFrom : coinFroms) {
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //Non local network account address,Not processed
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
            // Available balance initialization
            balanceValidateMap.computeIfAbsent(assetKey, a -> availableAmount);
            //Determine if it is an unlock operation
            if (coinFrom.getLocked() == 0) {
                //Not an unlock operation
                String fromCoinNonce = LedgerUtil.getNonceEncode(coinFrom.getNonce());
                if (LedgerUtil.equalsNonces(coinFrom.getNonce(), txNonce)) {
                    //nonce Repeated
                    logger(chainId).info("{}=={}=={}== nonce is repeat", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromCoinNonce, "nonce repeat"});
                }
                //Retrieve cached transactions from batch validation pool
                List<TempAccountNonce> list = accountValidateTxMap.get(assetKey);
                if (null == list || list.isEmpty()) {
                    //Starting from scratch
                    if (!LedgerUtil.equalsNonces(accountState.getNonce(), coinFrom.getNonce())) {
                        logger(chainId).error("validate fail:(isBlockValidateCommonTx failed)：{}=={}=={}==nonce is error!dbNonce:{}!=fromNonce:{},tx={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), LedgerUtil.getNonceEncode(accountState.getNonce()), fromCoinNonce, LedgerUtil.getNonceEncode(txNonce));
                        //Determine iffromNonceHas it already been stored,If stored, then this is an abnormal transaction double flower
                        logger(chainId).error("txType:{}, hash:{}", txType, txHash);
                        return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromCoinNonce, "dbNonce=" + LedgerUtil.getNonceEncode(accountState.getNonce())});
                    }
                } else {
                    //Retrieve objects from existing cached data for operation,nonceMust be coherent
                    TempAccountNonce tempAccountState = list.get(list.size() - 1);
                    if (!LedgerUtil.equalsNonces(tempAccountState.getNextNonce(), coinFrom.getNonce())) {
                        logger(chainId).info("isValidateCommonTxBatch {}=={}=={}==nonce is error!tempNonce:{}!=fromNonce:{},tx={}", address, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), LedgerUtil.getNonceEncode(tempAccountState.getNextNonce()), fromCoinNonce, LedgerUtil.getNonceEncode(txNonce));
                        return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, fromCoinNonce, "last pool nonce=" + LedgerUtil.getNonceEncode(tempAccountState.getNextNonce())});
                    }
                }
                // Calculation of available balance deduction
                balanceValidateMap.computeIfPresent(assetKey, (k , v) -> v.subtract(coinFrom.getAmount()));
            } else {
                //To unlock a transaction, you need to access it fromfrom Go inside to obtain the required height or time data for verification
                //Unlocking transactions only requires obtaining data from confirmed data for verification
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
        //Verify first, then put each transaction into the cache
        //Transactional hashIf the value already exists, returnfalse, transactionalfrom coin nonce If not continuous, there are double flowers.
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
            //For example, in a yellow card transaction, return directly
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

        //Traverse balance judgment
        for (Map.Entry<String, BigInteger> entry : balanceValidateMap.entrySet()) {
            //Caching data
            if (BigIntegerUtils.isLessThan(entry.getValue(), BigInteger.ZERO)) {
                //Insufficient balance
                logger(chainId).info("balance is not enough:{}===availableAmount={}",
                        entry.getKey(),
                        entry.getValue()
                );
                return ValidateResult.getResult(LedgerErrorCode.BALANCE_NOT_ENOUGH, new String[]{entry.getKey(),
                        BigIntegerUtils.bigIntegerToString(entry.getValue())});
            }
        }

        // -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-= After verification, store the data -=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=

        for (CoinFrom coinFrom : coinFroms) {
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(txType)) {
                    //Non local network account address,Not processed
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

            //Determine if it is an unlock operation
            if (coinFrom.getLocked() == 0) {
                //Not an unlock operation
                //Accumulated balance
                accountState.addTotalFromAmount(coinFrom.getAmount());
                List<TempAccountNonce> list = accountValidateTxMap.computeIfAbsent(assetKey, a -> new ArrayList<>());
                list.add(new TempAccountNonce(assetKey, coinFrom.getNonce(), txNonce));
            } else {
                //To unlock a transaction, you need to access it fromfrom Go inside to obtain the required height or time data for verification
                //Unlocking transactions only requires obtaining data from confirmed data for verification
                String lockedNonce = coinFrom.getAssetsChainId() + "-" + coinFrom.getAssetsId() + "-" + LedgerUtil.getNonceEncode(coinFrom.getNonce());
                lockedCancelNonceMap.put(lockedNonce, 1);
                //Processing cache
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

            //Determine if it is an unlock operation
            if (coinTo.getLockTime() == 0) {
                accountState.addTotalToAmount(coinTo.getAmount());
            } else {
//           //Verification passed,Process cache
                txLockedProcessor.processCoinData(coinTo, LedgerUtil.getNonceDecodeByTxHash(txHash), txHash, timeList, heightList, address, false);
            }
        }
        batchValidateTxSet.add(txHash);
        return ValidateResult.getSuccess();
    }

    /**
     * Verify unlocking transactions
     *
     * @param locked
     * @param accountState
     * @param fromAmount
     * @param fromNonce
     * @return
     */
    private boolean isValidateFreezeTx(byte locked, AccountState accountState, BigInteger fromAmount, byte[] fromNonce) {
        boolean isValidate = false;
        //Unlock transaction and verify if it exists
        if (locked == LedgerConstant.UNLOCKED_TIME) {
            //Time unlocking
            List<FreezeLockTimeState> list = accountState.getFreezeLockTimeStates();
            for (FreezeLockTimeState freezeLockTimeState : list) {
                if (LedgerUtil.equalsNonces(freezeLockTimeState.getNonce(), fromNonce) && freezeLockTimeState.getAmount().compareTo(fromAmount) == 0) {
                    //Find transaction
                    isValidate = true;
                    break;
                }
            }
        } else if (locked == LedgerConstant.UNLOCKED_HEIGHT) {
            //Height unlocking
            List<FreezeHeightState> list = accountState.getFreezeHeightStates();
            for (FreezeHeightState freezeHeightState : list) {
                if (LedgerUtil.equalsNonces(freezeHeightState.getNonce(), fromNonce) && freezeHeightState.getAmount().compareTo(fromAmount) == 0) {
                    //Find transaction
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
        //Unlock transaction and verify if it exists
        if (locked == LedgerConstant.UNLOCKED_TIME) {
            //Time unlocking
            if (null != timeList) {
                for (FreezeLockTimeState freezeLockTimeState : timeList) {
                    if (LedgerUtil.equalsNonces(freezeLockTimeState.getNonce(), fromNonce) && freezeLockTimeState.getAmount().compareTo(fromAmount) == 0) {
                        //Find transaction
                        isValidate = true;
                        break;
                    }
                }
            }

        } else if (locked == LedgerConstant.UNLOCKED_HEIGHT) {
            //Height unlocking
            if (null != heightList) {
                for (FreezeHeightState freezeHeightState : heightList) {
                    if (LedgerUtil.equalsNonces(freezeHeightState.getNonce(), fromNonce) && freezeHeightState.getAmount().compareTo(fromAmount) == 0) {
                        //Find transaction
                        isValidate = true;
                        break;
                    }
                }
            }
        }
        return isValidate;
    }

    /**
     * carry outcoinDataVerification of values,Verification conducted during local transaction generation
     * Only the verification of unconfirmed transactions is used
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
            //For example, in a yellow card transaction, return directly
            return ValidateResult.getSuccess();
        }
        if (!validateTxAmount(coinData, tx.getType())) {
            return ValidateResult.getResult(LedgerErrorCode.TX_AMOUNT_INVALIDATE, new String[]{txHash});
        }
        /*
         * Verify firstnonceIs the value normal
         */
        List<CoinFrom> coinFroms = coinData.getFrom();
        for (CoinFrom coinFrom : coinFroms) {
            if (LedgerUtil.isNotLocalChainAccount(addressChainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(tx.getType())) {
                    //Non local network account address,Not processed
                    continue;
                } else {
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{LedgerUtil.getRealAddressStr(coinFrom.getAddress()), "--", "address Not local chain Exception"});
                }
            }
            String address = LedgerUtil.getRealAddressStr(coinFrom.getAddress());
            AccountState accountState = accountStateService.getAccountStateReCal(address, addressChainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
            //Ordinary transactions
            if (coinFrom.getLocked() == 0) {
                return validateCommonCoinData(addressChainId, coinFrom.getAssetsChainId(), coinFrom.getAssetsId(), accountState, address, coinFrom.getAmount(), coinFrom.getNonce(), txNonce, true);
            } else {
                if (!isValidateFreezeTx(coinFrom.getLocked(), accountState, coinFrom.getAmount(), coinFrom.getNonce())) {
                    //Confirmed transaction not found frozen transaction
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
            //For example, in a yellow card transaction, return directly
            return ValidateResult.getSuccess();
        }
        /*
         * Verify firstnonceIs the value normal
         */
        List<CoinFrom> coinFroms = coinData.getFrom();

        for (CoinFrom coinFrom : coinFroms) {
            if (LedgerUtil.isNotLocalChainAccount(addressChainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(tx.getType())) {
                    //Non local network account address,Not processed
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
            //Ordinary transactions
            if (coinFrom.getLocked() == 0) {
                ValidateResult validateResult = validateCommonCoinData(addressChainId, assetChainId, assetId, accountState, address, coinFrom.getAmount(), coinFrom.getNonce(), txNonce, false);
                if (validateResult.isSuccess()) {
                    CoinDataUtil.calTxFromAmount(accountsMap, coinFrom, txNonce, accountKey, address);
                } else {
                    return validateResult;
                }
            } else {
                if (!isValidateFreezeTx(coinFrom.getLocked(), accountState, coinFrom.getAmount(), coinFrom.getNonce())) {
                    //Confirmed transaction not found frozen transaction
                    return ValidateResult.getResult(LedgerErrorCode.VALIDATE_FAIL, new String[]{address, LedgerUtil.getNonceEncode(coinFrom.getNonce()), "freeze tx is not exist"});
                }
            }
        }
        return ValidateResult.getSuccess();
    }

    /**
     * Batch packaging single transaction rollback processing
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
            //For example, in yellow card trading, simply remove and return.
            chainsBatchValidateTxMap.remove(txHash);
            return true;
        }
        List<CoinFrom> coinFroms = coinData.getFrom();
        byte[] nonce8Bytes = LedgerUtil.getNonceByTx(tx);
        for (CoinFrom coinFrom : coinFroms) {
            if (LedgerUtil.isNotLocalChainAccount(chainId, coinFrom.getAddress())) {
                if (LedgerUtil.isCrossTx(tx.getType())) {
                    //Non local network account address,Not processed
                    continue;
                } else {
                    LoggerUtil.logger(chainId).error("address={} Not local chain Exception", LedgerUtil.getRealAddressStr(coinFrom.getAddress()));
                    return false;
                }
            }
            //Determine if it is an unlock operation
            if (coinFrom.getLocked() == 0) {
                String assetKey = LedgerUtil.getKeyStr(LedgerUtil.getRealAddressStr(coinFrom.getAddress()), coinFrom.getAssetsChainId(), coinFrom.getAssetsId());
                //RollBACKaccountBalanceValidateTxMapCaching data
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
                //Unlock transaction,There are currently no cache records available
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
