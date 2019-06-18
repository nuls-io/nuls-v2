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
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.util.ContractLedgerUtil;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.nuls.contract.constant.ContractConstant.MININUM_TRANSFER_AMOUNT;
import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.getSuccess;
import static io.nuls.core.constant.TxType.DELETE_CONTRACT;

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
        Chain chain = contractHelper.getChain(chainId);
        int assetsId = chain.getConfig().getAssetsId();
        for(CoinFrom from : fromList) {
            if(from.getAssetsChainId() != chainId || from.getAssetsId() != assetsId) {
                Log.error("contract create error: The chain id or assets id of coin from is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
        }

        CallContractData txData = tx.getTxDataObj();
        if (!ContractUtil.checkPrice(txData.getPrice())) {
            Log.error("contract call error: The minimum value of price is 25.");
            return Result.getFailed(CONTRACT_MINIMUM_PRICE_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract call error: The value of gas limit ranges from 1 to 10,000,000.");
            return Result.getFailed(CONTRACT_GAS_LIMIT_ERROR);
        }
        BigInteger transferValue = txData.getValue();
        byte[] contractAddress = txData.getContractAddress();
        byte[] sender = txData.getSender();
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chainId);

        if (!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract call error: The contract does not exist.");
            return Result.getFailed(CONTRACT_ADDRESS_NOT_EXIST);
        }

        if (!addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract call error: The contract caller is not the transaction creator.");
            return Result.getFailed(CONTRACT_CALLER_ERROR);
        }

        BigInteger contractReceivedValue = BigInteger.ZERO;
        for (CoinTo coin : toList) {
            if(coin.getAssetsChainId() != chainId || coin.getAssetsId() != assetsId) {
                Log.error("contract call error: The chain id or assets id of coin to is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
            byte[] owner = coin.getAddress();
            if (addressSet.contains(AddressTool.getStringAddressByBytes(owner))) {
                continue;
            }

            if (coin.getLockTime() != 0) {
                Log.error("contract call error: Transfer amount cannot be locked.");
                return Result.getFailed(AMOUNT_LOCK_ERROR);
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
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size()).add(BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice())));
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract call error: The contract transaction fee is not right.");
            return Result.getFailed(FEE_NOT_RIGHT);
        }
    }
}
