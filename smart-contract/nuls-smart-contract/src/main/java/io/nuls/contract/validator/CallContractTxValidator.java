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
import io.nuls.base.data.CoinTo;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.util.ContractLedgerUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Set;

import static io.nuls.contract.constant.ContractConstant.MININUM_TRANSFER_AMOUNT;
import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * 
 * @author: PierreLuo
 * @date: 2019-03-07
 */
@Component
public class CallContractTxValidator{

    public Result validate(int chainId, CallContractTransaction tx) throws NulsException {
        CallContractData txData = tx.getTxDataObj();
        BigInteger transferValue = BigInteger.valueOf(txData.getValue());
        byte[] contractAddress = txData.getContractAddress();
        byte[] sender = txData.getSender();
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chainId);

        if(!ContractLedgerUtil.isExistContractAddress(chainId, contractAddress)) {
            Log.error("contract entity error: The contract does not exist.");
            return Result.getFailed(CONTRACT_ADDRESS_NOT_EXIST);
        }

        if (!addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract entity error: The contract caller is not the transaction creator.");
            return Result.getFailed(TX_DATA_VALIDATION_ERROR);
        }

        BigInteger contractReceivedValue = BigInteger.ZERO;
        for (CoinTo coin : tx.getCoinDataObj().getTo()) {
            byte[] owner = coin.getAddress();
            if (owner.length > 23) {
                owner = coin.getAddress();
            }
            // Keep the change maybe a very small coin
            if (addressSet.contains(AddressTool.getStringAddressByBytes(owner))) {
                // When the receiver sign this tx,Allow it transfer small coin
                continue;
            }

            if (coin.getLockTime() != 0) {
                Log.error("contract entity error: The amount of the transfer cannot be locked(UTXO status error).");
                return Result.getFailed(UTXO_STATUS_CHANGE);
            }

            if (!Arrays.equals(owner, contractAddress)) {
                Log.error("contract entity error: The receiver is not the contract address.");
                return Result.getFailed(TX_DATA_VALIDATION_ERROR);
            } else {
                contractReceivedValue = contractReceivedValue.add(coin.getAmount());
            }

            if (coin.getAmount().compareTo(MININUM_TRANSFER_AMOUNT) < 0) {
                Log.error("contract entity error: The amount of the transfer is too small.");
                return Result.getFailed(TOO_SMALL_AMOUNT);
            }
        }
        if (contractReceivedValue.compareTo(transferValue) < 0) {
            Log.error("contract entity error: Insufficient amount to transfer to the contract address.");
            return Result.getFailed(INVALID_AMOUNT);
        }
        return getSuccess();
    }
}
