/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.contract.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.common.NCUtils;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.rpc.call.AccountCall;
import io.nuls.contract.util.ContractLedgerUtil;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

import static io.nuls.contract.config.ContractContext.*;
import static io.nuls.contract.constant.ContractConstant.MININUM_TRANSFER_AMOUNT;
import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2019-03-07
 */
@Component
public class CallContractTxValidator {

    @Autowired
    private ContractHelper contractHelper;

    public Result validate(int chainId, CallContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        CallContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();
        boolean existSender = false;
        Chain chain = contractHelper.getChain(chainId);
        int assetsId = chain.getConfig().getAssetId();
        for (CoinFrom from : fromList) {
            if (from.getAssetsChainId() != chainId || from.getAssetsId() != assetsId) {
                Log.error("contract call error: The chain id or assets id of coin from is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
            if (!existSender && Arrays.equals(from.getAddress(), sender)) {
                existSender = true;
            }
        }
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chainId);
        if (!existSender || !addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract call error: The contract caller is not the transaction creator.");
            return Result.getFailed(CONTRACT_CALLER_ERROR);
        }
        if (!ContractUtil.checkPrice(txData.getPrice())) {
            Log.error("contract call error: The gas price is error.");
            return Result.getFailed(CONTRACT_MINIMUM_PRICE_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 1 to 10,000,000.");
            return Result.getFailed(CONTRACT_GAS_LIMIT_ERROR);
        }
        BigInteger transferValue = txData.getValue();
        byte[] contractAddress = txData.getContractAddress();


        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(CONTRACT_ADDRESS_NOT_EXIST);
        }

        BigInteger contractReceivedValue = BigInteger.ZERO;
        for (CoinTo coin : toList) {
            if (coin.getAssetsChainId() != chainId || coin.getAssetsId() != assetsId) {
                Log.error("contract call error: The chain id or assets id of coin to is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
            if (coin.getLockTime() != 0) {
                Log.error("contract call error: Transfer amount cannot be locked.");
                return Result.getFailed(AMOUNT_LOCK_ERROR);
            }
            byte[] owner = coin.getAddress();
            if (addressSet.contains(AddressTool.getStringAddressByBytes(owner))) {
                continue;
            }
            if (!Arrays.equals(owner, contractAddress)) {
                Log.error("contract call error: The receiver is not the contract address.");
                return Result.getFailed(CONTRACT_RECEIVER_ERROR);
            } else {
                contractReceivedValue = contractReceivedValue.add(coin.getAmount());
            }

            if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                Log.error("contract call error: The amount of the transfer is too small.");
                return Result.getFailed(TOO_SMALL_AMOUNT);
            }
        }
        if (contractReceivedValue.compareTo(transferValue) < 0) {
            Log.error("contract call error: Insufficient balance to transfer to the contract address.");
            return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
        }

        BigInteger realFee = tx.getFee();

        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size(), chain.getConfig().getFeeUnit(chainId, assetsId)).add(BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice())));
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(FEE_NOT_RIGHT);
        }
    }

    public Result validateV8(int chainId, CallContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        CallContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();

        Set<String> signatureAddressSet = SignatureUtil.getAddressFromTX(tx, chainId);
        if (!signatureAddressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract call error: The contract caller is not the transaction signer.");
            return Result.getFailed(CONTRACT_CALLER_SIGN_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 25 to 10,000,000.");
            return Result.getFailed(CONTRACT_GAS_LIMIT_ERROR);
        }

        byte[] contractAddress = txData.getContractAddress();

        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(CONTRACT_ADDRESS_NOT_EXIST);
        }

        Map<String, BigInteger> multyAssetMap = new HashMap<>();
        Set<String> multyAssetKeys = new HashSet<>();
        int assetChainId, assetId;
        String assetKey;
        BigInteger nulsValue = BigInteger.ZERO;
        for (CoinFrom from : fromList) {
            assetChainId = from.getAssetsChainId();
            assetId = from.getAssetsId();
            assetKey = assetChainId + "_" + assetId;
            if (LOCAL_CHAIN_ID == assetChainId && LOCAL_MAIN_ASSET_ID == assetId) {
                nulsValue = nulsValue.add(from.getAmount());
            } else {
                multyAssetKeys.add(assetKey);
                BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "from", BigInteger.ZERO);
                multyAssetMap.put(assetKey + "from", multyAssetValue.add(from.getAmount()));
            }
        }

        int toSize = toList.size();
        BigInteger transferNulsValue = txData.getValue();
        BigInteger contractReceivedNulsValue = BigInteger.ZERO;
        if (toSize > 0) {
            for (CoinTo coin : toList) {
                if (coin.getLockTime() != 0) {
                    Log.error("contract call error: Transfer amount cannot be locked.");
                    return Result.getFailed(AMOUNT_LOCK_ERROR);
                }
                byte[] owner = coin.getAddress();
                if (!Arrays.equals(owner, contractAddress)) {
                    Log.error("contract call error: The receiver is not the contract address.");
                    return Result.getFailed(CONTRACT_RECEIVER_ERROR);
                }
                assetChainId = coin.getAssetsChainId();
                assetId = coin.getAssetsId();
                boolean mainAsset = assetChainId == LOCAL_CHAIN_ID && assetId == LOCAL_MAIN_ASSET_ID;
                if (!mainAsset) {
                    if (coin.getAmount().compareTo(BigInteger.ZERO) == 0) {
                        Log.error("contract call error: Transfer amount cannot be zero.");
                        return Result.getFailed(TOO_SMALL_AMOUNT);
                    }
                    assetKey = assetChainId + "_" + assetId;
                    multyAssetKeys.add(assetKey);
                    BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "to", BigInteger.ZERO);
                    multyAssetMap.put(assetKey + "to", multyAssetValue.add(coin.getAmount()));
                    continue;
                }
                if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                    Log.error("contract call error: The amount of the transfer is too small.");
                    return Result.getFailed(TOO_SMALL_AMOUNT);
                }
                contractReceivedNulsValue = contractReceivedNulsValue.add(coin.getAmount());
            }

        }

        // Other asset verification
        BigInteger assetKeyFrom, assetKeyTo;
        for (String multyAssetKey : multyAssetKeys) {
            assetKeyFrom = multyAssetMap.get(multyAssetKey + "from");
            assetKeyTo = multyAssetMap.get(multyAssetKey + "to");
            if (null == assetKeyFrom) {
                Log.error("contract call error: Illegal coinFrom in the contract.");
                return Result.getFailed(CONTRACT_COIN_FROM_ERROR);
            }
            if (null == assetKeyTo) {
                Log.error("contract call error: Illegal coinTo in the contract.");
                return Result.getFailed(CONTRACT_COIN_TO_ERROR);
            }
            if (!BigIntegerUtils.isEqual(assetKeyFrom, assetKeyTo)) {
                Log.error("contract call error: The amount of coin data is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
        }

        // Main asset verification
        if (contractReceivedNulsValue.compareTo(transferNulsValue) < 0) {
            Log.error("contract call error: Insufficient balance of nuls to transfer to the contract address.");
            return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
        }

        if (transferNulsValue.compareTo(BigInteger.ZERO) > 0) {
            // Handling fee accounts can also be used to transfer assets to contracts
            if (nulsValue.compareTo(transferNulsValue) < 0) {
                Log.error("contract call error: Insufficient balance to transfer to the contract address.");
                return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
            }
        }

        BigInteger realFee = coinData.getFeeByAsset(LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID);
        Chain chain = contractHelper.getChain(chainId);
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size(), chain.getConfig().getFeeUnit(LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID)).add(BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice())));
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(FEE_NOT_RIGHT);
        }
    }

    /**
     * 1. New feature, allowing transfer to other addresses when calling contracts
     */
    public Result validateV13(int chainId, CallContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        CallContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();

        Set<String> signatureAddressSet = SignatureUtil.getAddressFromTX(tx, chainId);
        if (!signatureAddressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract call error: The contract caller is not the transaction signer.");
            return Result.getFailed(CONTRACT_CALLER_SIGN_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 0 to 10,000,000.");
            return Result.getFailed(CONTRACT_GAS_LIMIT_ERROR);
        }

        byte[] contractAddress = txData.getContractAddress();

        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(CONTRACT_ADDRESS_NOT_EXIST);
        }

        Map<String, BigInteger> multyAssetMap = new HashMap<>();
        Set<String> multyAssetKeys = new HashSet<>();
        int assetChainId, assetId;
        String assetKey;
        BigInteger nulsValue = BigInteger.ZERO;
        for (CoinFrom from : fromList) {
            assetChainId = from.getAssetsChainId();
            assetId = from.getAssetsId();
            assetKey = assetChainId + "_" + assetId;
            if (LOCAL_CHAIN_ID == assetChainId && LOCAL_MAIN_ASSET_ID == assetId) {
                nulsValue = nulsValue.add(from.getAmount());
            } else {
                multyAssetKeys.add(assetKey);
                BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "from", BigInteger.ZERO);
                multyAssetMap.put(assetKey + "from", multyAssetValue.add(from.getAmount()));
            }
        }

        int toSize = toList.size();
        BigInteger transferNulsToContractValue = txData.getValue();
        BigInteger contractReceivedNulsValue = BigInteger.ZERO;
        // The caller transfers funds to other accounts while calling the contract
        BigInteger transferNulsToOtherAccountValue = BigInteger.ZERO;
        if (toSize > 0) {
            for (CoinTo coin : toList) {
                if (coin.getLockTime() != 0) {
                    Log.error("contract call error: Transfer amount cannot be locked.");
                    return Result.getFailed(AMOUNT_LOCK_ERROR);
                }
                byte[] to = coin.getAddress();
                boolean isContractReceiver = Arrays.equals(to, contractAddress);
                assetChainId = coin.getAssetsChainId();
                assetId = coin.getAssetsId();
                boolean mainAsset = assetChainId == LOCAL_CHAIN_ID && assetId == LOCAL_MAIN_ASSET_ID;
                if (!mainAsset) {
                    if (coin.getAmount().compareTo(BigInteger.ZERO) == 0) {
                        Log.error("contract call error: Transfer amount cannot be zero.");
                        return Result.getFailed(TOO_SMALL_AMOUNT);
                    }
                    assetKey = assetChainId + "_" + assetId;
                    multyAssetKeys.add(assetKey);
                    BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "to", BigInteger.ZERO);
                    multyAssetMap.put(assetKey + "to", multyAssetValue.add(coin.getAmount()));
                    continue;
                }
                if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                    Log.error("contract call error: The amount of the transfer is too small.");
                    return Result.getFailed(TOO_SMALL_AMOUNT);
                }
                if (isContractReceiver) {
                    contractReceivedNulsValue = contractReceivedNulsValue.add(coin.getAmount());
                } else {
                    // inspecttoIs the address on the account whitelist
                    String toStr = AddressTool.getStringAddressByBytes(to);
                    boolean whiteAddress = AccountCall.validationWhitelistForTransferOnContractCall(chainId, toStr);
                    if (!whiteAddress) {
                        Log.error("contract call error: The receiver is not a whitelisted address.");
                        return Result.getFailed(CONTRACT_COIN_TO_ERROR);
                    }
                    transferNulsToOtherAccountValue = transferNulsToOtherAccountValue.add(coin.getAmount());
                }
            }

        }

        // Other asset verification
        BigInteger assetKeyFrom, assetKeyTo;
        for (String multyAssetKey : multyAssetKeys) {
            assetKeyFrom = multyAssetMap.get(multyAssetKey + "from");
            assetKeyTo = multyAssetMap.get(multyAssetKey + "to");
            if (null == assetKeyFrom) {
                Log.error("contract call error: Illegal coinFrom in the contract.");
                return Result.getFailed(CONTRACT_COIN_FROM_ERROR);
            }
            if (null == assetKeyTo) {
                Log.error("contract call error: Illegal coinTo in the contract.");
                return Result.getFailed(CONTRACT_COIN_TO_ERROR);
            }
            if (!BigIntegerUtils.isEqual(assetKeyFrom, assetKeyTo)) {
                Log.error("contract call error: The amount of coin data is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
        }

        // Main asset verification
        if (contractReceivedNulsValue.compareTo(transferNulsToContractValue) < 0) {
            Log.error("contract call error: Insufficient balance of nuls to transfer to the contract address.");
            return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
        }

        if (transferNulsToContractValue.compareTo(BigInteger.ZERO) > 0) {
            // Handling fee accounts can also be used to transfer assets to contracts
            if (nulsValue.compareTo(transferNulsToContractValue.add(transferNulsToOtherAccountValue)) < 0) {
                Log.error("contract call error: Insufficient balance to transfer to the contract address.");
                return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
            }
        }

        BigInteger realFee = coinData.getFeeByAsset(LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID);
        Chain chain = contractHelper.getChain(chainId);
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size(), chain.getConfig().getFeeUnit(LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID)).add(BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice())));
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(FEE_NOT_RIGHT);
        }
    }

    /**
     * 1. New feature that supports multiple address signatures when calling contracts
     */
    public Result validateV14(int chainId, CallContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        CallContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();

        if (fromList == null || fromList.isEmpty()) {
            Log.error("contract call error: The contract caller is not the transaction signer.[0]");
            return Result.getFailed(CONTRACT_CALLER_SIGN_ERROR);
        }
        boolean existSender = false;
        for (CoinFrom from : fromList) {
            if (Arrays.equals(from.getAddress(), sender)) {
                existSender = true;
                break;
            }
        }
        if (!existSender) {
            Log.error("contract call error: The contract caller is not the transaction signer.[1]");
            return Result.getFailed(CONTRACT_CALLER_SIGN_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 0 to 10,000,000.");
            return Result.getFailed(CONTRACT_GAS_LIMIT_ERROR);
        }

        byte[] contractAddress = txData.getContractAddress();

        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(CONTRACT_ADDRESS_NOT_EXIST);
        }

        Map<String, BigInteger> multyAssetMap = new HashMap<>();
        Set<String> multyAssetKeys = new HashSet<>();
        int assetChainId, assetId;
        String assetKey;
        BigInteger nulsValue = BigInteger.ZERO;
        for (CoinFrom from : fromList) {
            assetChainId = from.getAssetsChainId();
            assetId = from.getAssetsId();
            assetKey = assetChainId + "_" + assetId;
            if (LOCAL_CHAIN_ID == assetChainId && LOCAL_MAIN_ASSET_ID == assetId) {
                nulsValue = nulsValue.add(from.getAmount());
            } else {
                multyAssetKeys.add(assetKey);
                BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "from", BigInteger.ZERO);
                multyAssetMap.put(assetKey + "from", multyAssetValue.add(from.getAmount()));
            }
        }

        int toSize = toList.size();
        BigInteger transferNulsToContractValue = txData.getValue();
        BigInteger contractReceivedNulsValue = BigInteger.ZERO;
        // The caller transfers funds to other accounts while calling the contract
        BigInteger transferNulsToOtherAccountValue = BigInteger.ZERO;
        if (toSize > 0) {
            for (CoinTo coin : toList) {
                if (coin.getLockTime() != 0) {
                    Log.error("contract call error: Transfer amount cannot be locked.");
                    return Result.getFailed(AMOUNT_LOCK_ERROR);
                }
                byte[] to = coin.getAddress();
                boolean isContractReceiver = Arrays.equals(to, contractAddress);
                assetChainId = coin.getAssetsChainId();
                assetId = coin.getAssetsId();
                boolean mainAsset = assetChainId == LOCAL_CHAIN_ID && assetId == LOCAL_MAIN_ASSET_ID;
                if (!mainAsset) {
                    if (coin.getAmount().compareTo(BigInteger.ZERO) == 0) {
                        Log.error("contract call error: Transfer amount cannot be zero.");
                        return Result.getFailed(TOO_SMALL_AMOUNT);
                    }
                    assetKey = assetChainId + "_" + assetId;
                    multyAssetKeys.add(assetKey);
                    BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "to", BigInteger.ZERO);
                    multyAssetMap.put(assetKey + "to", multyAssetValue.add(coin.getAmount()));
                    continue;
                }
                if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                    Log.error("contract call error: The amount of the transfer is too small.");
                    return Result.getFailed(TOO_SMALL_AMOUNT);
                }
                if (isContractReceiver) {
                    contractReceivedNulsValue = contractReceivedNulsValue.add(coin.getAmount());
                } else {
                    // inspecttoIs the address on the account whitelist
                    String toStr = AddressTool.getStringAddressByBytes(to);
                    boolean whiteAddress = AccountCall.validationWhitelistForTransferOnContractCall(chainId, toStr);
                    if (!whiteAddress) {
                        Log.error("contract call error: The receiver is not a whitelisted address.");
                        return Result.getFailed(CONTRACT_COIN_TO_ERROR);
                    }
                    transferNulsToOtherAccountValue = transferNulsToOtherAccountValue.add(coin.getAmount());
                }
            }

        }

        // Other asset verification
        BigInteger assetKeyFrom, assetKeyTo;
        for (String multyAssetKey : multyAssetKeys) {
            assetKeyFrom = multyAssetMap.get(multyAssetKey + "from");
            assetKeyTo = multyAssetMap.get(multyAssetKey + "to");
            if (null == assetKeyFrom) {
                Log.error("contract call error: Illegal coinFrom in the contract.");
                return Result.getFailed(CONTRACT_COIN_FROM_ERROR);
            }
            if (null == assetKeyTo) {
                Log.error("contract call error: Illegal coinTo in the contract.");
                return Result.getFailed(CONTRACT_COIN_TO_ERROR);
            }
            if (!BigIntegerUtils.isEqual(assetKeyFrom, assetKeyTo)) {
                Log.error("contract call error: The amount of coin data is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
        }

        // Main asset verification
        if (contractReceivedNulsValue.compareTo(transferNulsToContractValue) < 0) {
            Log.error("contract call error: Insufficient balance of nuls to transfer to the contract address.");
            return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
        }

        if (transferNulsToContractValue.compareTo(BigInteger.ZERO) > 0) {
            // Handling fee accounts can also be used to transfer assets to contracts
            if (nulsValue.compareTo(transferNulsToContractValue.add(transferNulsToOtherAccountValue)) < 0) {
                Log.error("contract call error: Insufficient balance to transfer to the contract address.");
                return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
            }
        }

        BigInteger realFee = coinData.getFeeByAsset(LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID);

        Chain chain = contractHelper.getChain(chainId);
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size(), chain.getConfig().getFeeUnit(LOCAL_CHAIN_ID, LOCAL_MAIN_ASSET_ID)).add(BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice())));
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(FEE_NOT_RIGHT);
        }
    }


    /**
     * 1. New function: support multi-signature address when calling contracts
     */
    public Result validateV20(int chainId, CallContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        CallContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();

        if (fromList == null || fromList.isEmpty()) {
            Log.error("contract call error: The contract caller is not the transaction signer.[0]");
            return Result.getFailed(CONTRACT_CALLER_SIGN_ERROR);
        }
        boolean existSender = false;
        for (CoinFrom from : fromList) {
            if (Arrays.equals(from.getAddress(), sender)) {
                existSender = true;
                break;
            }
        }
        if (!existSender) {
            Log.error("contract call error: The contract caller is not the transaction signer.[1]");
            return Result.getFailed(CONTRACT_CALLER_SIGN_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 0 to 10,000,000.");
            return Result.getFailed(CONTRACT_GAS_LIMIT_ERROR);
        }

        byte[] contractAddress = txData.getContractAddress();

        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(CONTRACT_ADDRESS_NOT_EXIST);
        }

        Map<String, BigInteger> multyAssetMap = new HashMap<>();
        Set<String> multyAssetKeys = new HashSet<>();
        int assetChainId, assetId;
        String assetKey;
        BigInteger nulsValue = BigInteger.ZERO;
        for (CoinFrom from : fromList) {
            assetChainId = from.getAssetsChainId();
            assetId = from.getAssetsId();
            assetKey = assetChainId + "_" + assetId;
            if (LOCAL_CHAIN_ID == assetChainId && LOCAL_MAIN_ASSET_ID == assetId) {
                nulsValue = nulsValue.add(from.getAmount());
            } else {
                multyAssetKeys.add(assetKey);
                BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "from", BigInteger.ZERO);
                multyAssetMap.put(assetKey + "from", multyAssetValue.add(from.getAmount()));
            }
        }

        int toSize = toList.size();
        BigInteger transferNulsToContractValue = txData.getValue();
        BigInteger contractReceivedNulsValue = BigInteger.ZERO;
        // The caller transfers funds to other accounts while calling the contract
        BigInteger transferNulsToOtherAccountValue = BigInteger.ZERO;
        if (toSize > 0) {
            for (CoinTo coin : toList) {
                if (coin.getLockTime() != 0) {
                    Log.error("contract call error: Transfer amount cannot be locked.");
                    return Result.getFailed(AMOUNT_LOCK_ERROR);
                }
                byte[] to = coin.getAddress();
                boolean isContractReceiver = Arrays.equals(to, contractAddress);
                assetChainId = coin.getAssetsChainId();
                assetId = coin.getAssetsId();
                boolean mainAsset = assetChainId == LOCAL_CHAIN_ID && assetId == LOCAL_MAIN_ASSET_ID;
                if (!mainAsset) {
                    if (coin.getAmount().compareTo(BigInteger.ZERO) == 0) {
                        Log.error("contract call error: Transfer amount cannot be zero.");
                        return Result.getFailed(TOO_SMALL_AMOUNT);
                    }
                    assetKey = assetChainId + "_" + assetId;
                    multyAssetKeys.add(assetKey);
                    BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "to", BigInteger.ZERO);
                    multyAssetMap.put(assetKey + "to", multyAssetValue.add(coin.getAmount()));
                    continue;
                }
                if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                    Log.error("contract call error: The amount of the transfer is too small.");
                    return Result.getFailed(TOO_SMALL_AMOUNT);
                }
                if (isContractReceiver) {
                    contractReceivedNulsValue = contractReceivedNulsValue.add(coin.getAmount());
                } else {
                    // inspecttoIs the address on the account whitelist
                    String toStr = AddressTool.getStringAddressByBytes(to);
                    boolean whiteAddress = AccountCall.validationWhitelistForTransferOnContractCall(chainId, toStr);
                    if (!whiteAddress) {
                        Log.error("contract call error: The receiver is not a whitelisted address.");
                        return Result.getFailed(CONTRACT_COIN_TO_ERROR);
                    }
                    transferNulsToOtherAccountValue = transferNulsToOtherAccountValue.add(coin.getAmount());
                }
            }

        }

        // Other asset verification
        BigInteger assetKeyFrom, assetKeyTo;
        for (String multyAssetKey : multyAssetKeys) {
            assetKeyFrom = multyAssetMap.get(multyAssetKey + "from");
            assetKeyTo = multyAssetMap.get(multyAssetKey + "to");
            if (null == assetKeyFrom) {
                Log.error("contract call error: Illegal coinFrom in the contract.");
                return Result.getFailed(CONTRACT_COIN_FROM_ERROR);
            }
            if (FEE_ASSETS_SET.contains(multyAssetKey.replace("_", "-"))) {
                continue;
            }
            if (null == assetKeyTo) {
                Log.error("contract call error: Illegal coinTo in the contract.");
                return Result.getFailed(CONTRACT_COIN_TO_ERROR);
            }
            if (!BigIntegerUtils.isEqual(assetKeyFrom, assetKeyTo)) {
                Log.error("contract call error: The amount of coin data is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
        }

        // Main asset verification
        if (contractReceivedNulsValue.compareTo(transferNulsToContractValue) < 0) {
            Log.error("contract call error: Insufficient balance of nuls to transfer to the contract address.");
            return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
        }

        if (transferNulsToContractValue.compareTo(BigInteger.ZERO) > 0) {
            // Handling fee accounts can also be used to transfer assets to contracts
            if (nulsValue.compareTo(transferNulsToContractValue.add(transferNulsToOtherAccountValue)) < 0) {
                Log.error("contract call error: Insufficient balance to transfer to the contract address.");
                return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
            }
        }

        BigInteger realFee = BigInteger.ZERO;
        int[] arr = new int[0];
        for (String key : FEE_ASSETS_SET) {
            if (realFee.compareTo(BigInteger.ZERO) != 0) {
                break;
            }
            arr = NCUtils.splitTokenId(key);
            realFee = coinData.getFeeByAsset(arr[0], arr[1]);
        }

        Chain chain = contractHelper.getChain(chainId);
        BigInteger scFee = BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice()));
        scFee = new BigDecimal(scFee).multiply(BigDecimal.valueOf(chain.getConfig().getFeeCoefficient(arr[0], arr[1]))).toBigInteger();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size(), chain.getConfig().getFeeUnit(arr[0], arr[1])).add(scFee);
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(FEE_NOT_RIGHT);
        }
    }

    /**
     * Remove restrictions: Minimum amount of NULS that can be transferred out of the contract
     */
    public Result validateV22(int chainId, CallContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        CallContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();

        if (fromList == null || fromList.isEmpty()) {
            Log.error("contract call error: The contract caller is not the transaction signer.[0]");
            return Result.getFailed(CONTRACT_CALLER_SIGN_ERROR);
        }
        boolean existSender = false;
        for (CoinFrom from : fromList) {
            if (Arrays.equals(from.getAddress(), sender)) {
                existSender = true;
                break;
            }
        }
        if (!existSender) {
            Log.error("contract call error: The contract caller is not the transaction signer.[1]");
            return Result.getFailed(CONTRACT_CALLER_SIGN_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 0 to 10,000,000.");
            return Result.getFailed(CONTRACT_GAS_LIMIT_ERROR);
        }

        byte[] contractAddress = txData.getContractAddress();

        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(CONTRACT_ADDRESS_NOT_EXIST);
        }

        Map<String, BigInteger> multyAssetMap = new HashMap<>();
        Set<String> multyAssetKeys = new HashSet<>();
        int assetChainId, assetId;
        String assetKey;
        BigInteger nulsValue = BigInteger.ZERO;
        for (CoinFrom from : fromList) {
            assetChainId = from.getAssetsChainId();
            assetId = from.getAssetsId();
            assetKey = assetChainId + "_" + assetId;
            if (LOCAL_CHAIN_ID == assetChainId && LOCAL_MAIN_ASSET_ID == assetId) {
                nulsValue = nulsValue.add(from.getAmount());
            } else {
                multyAssetKeys.add(assetKey);
                BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "from", BigInteger.ZERO);
                multyAssetMap.put(assetKey + "from", multyAssetValue.add(from.getAmount()));
            }
        }

        int toSize = toList.size();
        BigInteger transferNulsToContractValue = txData.getValue();
        BigInteger contractReceivedNulsValue = BigInteger.ZERO;
        // The caller transfers funds to other accounts while calling the contract
        BigInteger transferNulsToOtherAccountValue = BigInteger.ZERO;
        if (toSize > 0) {
            for (CoinTo coin : toList) {
                if (coin.getLockTime() != 0) {
                    Log.error("contract call error: Transfer amount cannot be locked.");
                    return Result.getFailed(AMOUNT_LOCK_ERROR);
                }
                byte[] to = coin.getAddress();
                boolean isContractReceiver = Arrays.equals(to, contractAddress);
                assetChainId = coin.getAssetsChainId();
                assetId = coin.getAssetsId();
                boolean mainAsset = assetChainId == LOCAL_CHAIN_ID && assetId == LOCAL_MAIN_ASSET_ID;
                if (!mainAsset) {
                    if (coin.getAmount().compareTo(BigInteger.ZERO) == 0) {
                        Log.error("contract call error: Transfer amount cannot be zero.");
                        return Result.getFailed(TOO_SMALL_AMOUNT);
                    }
                    assetKey = assetChainId + "_" + assetId;
                    multyAssetKeys.add(assetKey);
                    BigInteger multyAssetValue = multyAssetMap.getOrDefault(assetKey + "to", BigInteger.ZERO);
                    multyAssetMap.put(assetKey + "to", multyAssetValue.add(coin.getAmount()));
                    continue;
                }
                //if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                //    Log.error("contract call error: The amount of the transfer is too small.");
                //    return Result.getFailed(TOO_SMALL_AMOUNT);
                //}
                if (isContractReceiver) {
                    contractReceivedNulsValue = contractReceivedNulsValue.add(coin.getAmount());
                } else {
                    // inspecttoIs the address on the account whitelist
                    String toStr = AddressTool.getStringAddressByBytes(to);
                    boolean whiteAddress = AccountCall.validationWhitelistForTransferOnContractCall(chainId, toStr);
                    if (!whiteAddress) {
                        Log.error("contract call error: The receiver is not a whitelisted address.");
                        return Result.getFailed(CONTRACT_COIN_TO_ERROR);
                    }
                    transferNulsToOtherAccountValue = transferNulsToOtherAccountValue.add(coin.getAmount());
                }
            }

        }

        // Other asset verification
        BigInteger assetKeyFrom, assetKeyTo;
        for (String multyAssetKey : multyAssetKeys) {
            assetKeyFrom = multyAssetMap.get(multyAssetKey + "from");
            assetKeyTo = multyAssetMap.get(multyAssetKey + "to");
            if (null == assetKeyFrom) {
                Log.error("contract call error: Illegal coinFrom in the contract.");
                return Result.getFailed(CONTRACT_COIN_FROM_ERROR);
            }
            if (FEE_ASSETS_SET.contains(multyAssetKey.replace("_", "-"))) {
                continue;
            }
            if (null == assetKeyTo) {
                Log.error("contract call error: Illegal coinTo in the contract.");
                return Result.getFailed(CONTRACT_COIN_TO_ERROR);
            }
            if (!BigIntegerUtils.isEqual(assetKeyFrom, assetKeyTo)) {
                Log.error("contract call error: The amount of coin data is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
        }

        // Main asset verification
        if (contractReceivedNulsValue.compareTo(transferNulsToContractValue) < 0) {
            Log.error("contract call error: Insufficient balance of nuls to transfer to the contract address.");
            return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
        }

        if (transferNulsToContractValue.compareTo(BigInteger.ZERO) > 0) {
            // Handling fee accounts can also be used to transfer assets to contracts
            if (nulsValue.compareTo(transferNulsToContractValue.add(transferNulsToOtherAccountValue)) < 0) {
                Log.error("contract call error: Insufficient balance to transfer to the contract address.");
                return Result.getFailed(INSUFFICIENT_BALANCE_TO_CONTRACT);
            }
        }

        BigInteger realFee = BigInteger.ZERO;
        int[] arr = new int[0];
        for (String key : FEE_ASSETS_SET) {
            if (realFee.compareTo(BigInteger.ZERO) != 0) {
                break;
            }
            arr = NCUtils.splitTokenId(key);
            realFee = coinData.getFeeByAsset(arr[0], arr[1]);
        }

        Chain chain = contractHelper.getChain(chainId);
        BigInteger scFee = BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice()));
        scFee = new BigDecimal(scFee).multiply(BigDecimal.valueOf(chain.getConfig().getFeeCoefficient(arr[0], arr[1]))).toBigInteger();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size(), chain.getConfig().getFeeUnit(arr[0], arr[1])).add(scFee);
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(FEE_NOT_RIGHT);
        }
    }
}
