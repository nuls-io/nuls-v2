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
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.DeleteContractData;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Set;

import static io.nuls.contract.constant.ContractErrorCode.CONTRACT_DELETE_BALANCE;
import static io.nuls.contract.constant.ContractErrorCode.TX_DATA_VALIDATION_ERROR;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2018/10/2
 */
@Component
public class DeleteContractTxValidator{

    @Autowired
    private ContractHelper contractHelper;

    public Result validate(int chainId, DeleteContractTransaction tx) throws NulsException {

        DeleteContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();
        byte[] contractAddressBytes = txData.getContractAddress();
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chainId);

        if (!addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract data error: The contract deleter is not the transaction creator.");
            return Result.getFailed(TX_DATA_VALIDATION_ERROR);
        }

        Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddressBytes);
        if(contractAddressInfoPoResult.isFailed()) {
            return Result.getFailed(contractAddressInfoPoResult.getErrorCode());
        }
        ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
        if(contractAddressInfoPo == null) {
            Log.error("contract data error: The contract does not exist.");
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
        }
        if(!Arrays.equals(sender, contractAddressInfoPo.getSender())) {
            Log.error("contract data error: The contract deleter is not the contract creator.");
            return Result.getFailed(TX_DATA_VALIDATION_ERROR);
        }

        ContractBalance balance = contractHelper.getRealBalance(chainId, AddressTool.getStringAddressByBytes(contractAddressBytes));
        if(balance == null) {
            Log.error("contract data error: That balance of the contract is abnormal.");
            return Result.getFailed(TX_DATA_VALIDATION_ERROR);
        }

        BigInteger totalBalance = balance.getTotal();
        if(totalBalance.compareTo(BigInteger.ZERO) != 0) {
            Log.error("contract data error: The balance of the contract is not 0.");
            return Result.getFailed(CONTRACT_DELETE_BALANCE);
        }


        return getSuccess();
    }
}
