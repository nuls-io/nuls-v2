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
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.model.txdata.DeleteContractData;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2018/10/2
 */
@Component
public class DeleteContractTxValidator {

    @Autowired
    private ContractHelper contractHelper;

    public Result validate(int chainId, DeleteContractTransaction tx) throws NulsException {

        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        if(toList.size() != 0) {
            Log.error("contract delete error: The contract coin to is not empty.");
            return Result.getFailed(CONTRACT_COIN_TO_EMPTY_ERROR);
        }
        Chain chain = contractHelper.getChain(chainId);
        int assetsId = chain.getConfig().getAssetsId();
        for(CoinFrom from : fromList) {
            if(from.getAssetsChainId() != chainId || from.getAssetsId() != assetsId) {
                Log.error("contract delete error: The chain id or assets id of coin from is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
        }

        DeleteContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();
        byte[] contractAddressBytes = txData.getContractAddress();
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chainId);

        if (!addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract delete error: The contract deleter is not the transaction creator.");
            return Result.getFailed(CONTRACT_DELETER_ERROR);
        }

        Result<ContractAddressInfoPo> contractAddressInfoPoResult = contractHelper.getContractAddressInfo(chainId, contractAddressBytes);
        if (contractAddressInfoPoResult.isFailed()) {
            return Result.getFailed(contractAddressInfoPoResult.getErrorCode());
        }
        ContractAddressInfoPo contractAddressInfoPo = contractAddressInfoPoResult.getData();
        if (contractAddressInfoPo == null) {
            Log.error("contract delete error: The contract does not exist.");
            return Result.getFailed(ContractErrorCode.CONTRACT_ADDRESS_NOT_EXIST);
        }
        if (!Arrays.equals(sender, contractAddressInfoPo.getSender())) {
            Log.error("contract delete error: The contract deleter is not the contract creator.");
            return Result.getFailed(CONTRACT_OWNER_ERROR);
        }

        ContractBalance balance = contractHelper.getRealBalance(chainId, AddressTool.getStringAddressByBytes(contractAddressBytes));
        if (balance == null) {
            Log.error("contract delete error: That balance of the contract is abnormal.");
            return Result.getFailed(CONTRACT_BALANCE_ERROR);
        }

        BigInteger totalBalance = balance.getTotal();
        if (totalBalance.compareTo(BigInteger.ZERO) != 0) {
            Log.error("contract delete error: The balance of the contract is not 0.");
            return Result.getFailed(CONTRACT_DELETE_BALANCE);
        }


        return getSuccess();
    }
}
