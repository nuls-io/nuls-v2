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
import io.nuls.contract.config.ContractConfig;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.txdata.CreateContractData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.FormatValidUtils;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static io.nuls.contract.constant.ContractErrorCode.*;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2019-03-07
 */
@Component
public class CreateContractTxValidator {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractConfig contractConfig;

    public Result validate(int chainId, CreateContractTransaction tx) throws NulsException {
        CoinData coinData = tx.getCoinDataInstance();
        List<CoinFrom> fromList = coinData.getFrom();
        List<CoinTo> toList = coinData.getTo();
        // 检查 toList, 除了黑洞地址外，其他不被允许 000000000000000000000000000000000000000000000000000000000000000000
        int toListSize = toList.size();
        do {
            if(toListSize == 0) {
                break;
            }
            if(toListSize == 1) {
                CoinTo coinTo = toList.get(0);
                byte[] blockHoleAddress = AddressTool.getAddress(HexUtil.decode(contractConfig.getBlackHolePublicKey()), contractConfig.getChainId());
                if(Arrays.equals(blockHoleAddress, coinTo.getAddress())) {
                    break;
                }
            }
            Log.error("contract create error: The contract coin to is not empty.");
            return Result.getFailed(CONTRACT_COIN_TO_EMPTY_ERROR);
        } while (false);

        CreateContractData txData = tx.getTxDataObj();
        byte[] sender = txData.getSender();
        boolean existSender = false;
        Chain chain = contractHelper.getChain(chainId);
        int assetsId = chain.getConfig().getAssetId();
        for(CoinFrom from : fromList) {
            if(from.getAssetsChainId() != chainId || from.getAssetsId() != assetsId) {
                Log.error("contract create error: The chain id or assets id of coin from is error.");
                return Result.getFailed(CONTRACT_COIN_ASSETS_ERROR);
            }
            if(!existSender && Arrays.equals(from.getAddress(), sender)) {
                existSender = true;
            }
        }
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chainId);
        if (!existSender || !addressSet.contains(AddressTool.getStringAddressByBytes(sender))) {
            Log.error("contract create error: The contract creator is not the transaction creator.");
            return Result.getFailed(CONTRACT_CREATOR_ERROR);
        }
        String alias = txData.getAlias();
        if(!FormatValidUtils.validAlias(alias)) {
            Log.error("contract create error: The contract alias format error.");
            return Result.getFailed(CONTRACT_ALIAS_FORMAT_ERROR);
        }
        if (!ContractUtil.checkPrice(txData.getPrice())) {
            Log.error("contract create error: The minimum value of price is 25.");
            return Result.getFailed(CONTRACT_MINIMUM_PRICE_ERROR);
        }
        if (!ContractUtil.checkGasLimit(txData.getGasLimit())) {
            Log.error("contract create error: The value of gas limit ranges from 1 to 10,000,000.");
            return Result.getFailed(CONTRACT_GAS_LIMIT_ERROR);
        }

        byte[] contractAddress = txData.getContractAddress();
        if (!ContractUtil.isLegalContractAddress(chainId, contractAddress)) {
            Log.error("contract create error: Illegal contract address.");
            return Result.getFailed(ILLEGAL_CONTRACT_ADDRESS);
        }

        BigInteger realFee = tx.getFee();
        BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size()).add(BigInteger.valueOf(txData.getGasLimit()).multiply(BigInteger.valueOf(txData.getPrice())));
        if (realFee.compareTo(fee) >= 0) {
            return getSuccess();
        } else {
            Log.error("contract create error: The contract transaction fee is not right.");
            return Result.getFailed(FEE_NOT_RIGHT);
        }
    }
}
