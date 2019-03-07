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
import io.nuls.base.signture.SignatureUtil;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.Set;

import static io.nuls.contract.constant.ContractErrorCode.ILLEGAL_CONTRACT_ADDRESS;
import static io.nuls.contract.constant.ContractErrorCode.TX_DATA_VALIDATION_ERROR;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * 
 * @author: PierreLuo
 * @date: 2019-03-07
 */
@Component
public class CreateContractTxValidator {

    public Result validate(int chainId, CreateContractTransaction tx) throws NulsException {
        CreateContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();
        byte[] contractAddress = txData.getContractAddress();
        if(!ContractUtil.isLegalContractAddress(contractAddress)) {
            Log.error("contract data error: Illegal contract address.");
            return Result.getFailed(ILLEGAL_CONTRACT_ADDRESS);
        }
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chainId);

        if (!addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract data error: The contract creater is not the transaction creator.");
            return Result.getFailed(TX_DATA_VALIDATION_ERROR);
        }

        return getSuccess();
    }
}
