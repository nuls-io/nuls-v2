/*
 * MIT License
 *
 * Copyright (c) 2018-2019 nuls.io
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
 */

package io.nuls.account.util.validator;

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.account.util.TxUtil.getSuccess;

/**
 * Transaction verification tools
 * Transaction Verification Tool Class
 *
 * @author qinyifeng
 * 2019/01/17
 */
@Component
public class TxValidator {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TransactionService transactionService;
    @Autowired
    private MultiSignAccountService multiSignAccountService;

    /**
     * Transaction type
     * fromThe address of must be the address of the initiating chain（fromDoes the asset inside exist）
     * toThe address of must be the address of the initiating chain（toDoes the asset inside exist）
     * Transaction fees
     *
     * Transfer transaction validator
     * Basic transaction verification has been verified by transaction management
     *
     * @param chain
     * @param tx
     * @return Result
     */
    public Result validate(Chain chain, Transaction tx) throws NulsException {
        CoinData coinData = TxUtil.getCoinData(tx);
        Result result = validateCoinFromBase(chain, coinData.getFrom());
        if (result.isFailed()) {
            return result;
        }
        result = validateCoinToBase(chain, coinData.getTo());
        if (result.isFailed()) {
            return result;
        }
        result = validateCoinDataAsset(chain, coinData);
        if (result.isFailed()) {
            return result;
        }
        return result;
    }

    /**
     * Verify assets other than transaction fees fromIs the asset amount in greater than or equal totoThe asset amounts in must correspond equally
     * @return
     */
    public Result validateCoinDataAsset(Chain chain, CoinData coinData) throws NulsException{
        //fromMedium assetsid-Asset ChainidAs akeyThe total amount of an asset stored
        Map<String, BigInteger> mapFrom = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
        for (CoinFrom coinFrom : coinData.getFrom()) {
            if (!TxUtil.isChainAssetExist(chain, coinFrom)) {
                String key = coinFrom.getAssetsChainId() + "-" + coinFrom.getAssetsId();
                BigInteger amount = mapFrom.get(key);
                if(null != amount) {
                    amount = amount.add(coinFrom.getAmount());
                }else{
                    amount = coinFrom.getAmount();
                }
                mapFrom.put(key, amount);
            }
        }
        //toMedium assetsid-Asset ChainidAs akeyThe total amount of an asset stored
        Map<String, BigInteger> mapTo = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
        for (CoinTo coinTO : coinData.getTo()) {
            if (!TxUtil.isChainAssetExist(chain, coinTO)) {
                String key = coinTO.getAssetsChainId() + "-" + coinTO.getAssetsId();
                BigInteger amount = mapTo.get(key);
                if(null != amount) {
                    amount = amount.add(coinTO.getAmount());
                }else{
                    amount = coinTO.getAmount();
                }
                mapTo.put(key, amount);
            }
        }
        //comparefromandtoIs the value of the same asset equal
        for(Map.Entry<String, BigInteger> entry : mapFrom.entrySet()){
            if(entry.getValue().compareTo(mapTo.get(entry.getKey())) == -1){
                return Result.getFailed(AccountErrorCode.COINFROM_UNDERPAYMENT);
            }
        }
        return getSuccess();
    }

    /**
     * Verify payment party data for transactions
     * 1.SenderfromThe chain corresponding to the address and asset in the middleidChain must be initiatedid
     * 2.Verify the existence of assets
     *
     * @param chain
     * @param listFrom
     * @return Result
     */
    public Result validateCoinFromBase(Chain chain, List<CoinFrom> listFrom) throws NulsException {
        if (null == listFrom || listFrom.size() == 0) {
            return Result.getFailed(AccountErrorCode.TX_COINFROM_NOT_FOUND);
        }
        int chainId = chain.getConfig().getChainId();
        for (CoinFrom coinFrom : listFrom) {
            int addrChainId = AddressTool.getChainIdByAddress(coinFrom.getAddress());
            //Black hole address cannot initiate transfer
            if(AddressTool.isBlackHoleAddress(NulsConfig.BLACK_HOLE_PUB_KEY,addrChainId,coinFrom.getAddress())){
                return Result.getFailed(AccountErrorCode.ADDRESS_TRANSFER_BAN);
            }
            // SenderfromThe chain corresponding to the middle addressidMust be the initiator of the chainid
            if (chainId != addrChainId) {
                return Result.getFailed(AccountErrorCode.CHAINID_ERROR);
            }
        }
        return getSuccess();
    }

    /**
     * Verify the payee data of the transaction(coinToDo they belong to the same chain)
     * 1.Do all addresses and assets of the payee belong to the same chain
     * 2.Verify the existence of assets
     *
     * @param listTo
     * @return Result
     */
    public Result validateCoinToBase(Chain chain, List<CoinTo> listTo) throws NulsException {
        if (null == listTo || listTo.size() == 0) {
            return Result.getFailed(AccountErrorCode.TX_COINTO_NOT_FOUND);
        }
        int chainId = chain.getConfig().getChainId();
        for (CoinTo coinTo : listTo) {
            int addrChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            // RecipienttoThe chain corresponding to the middle addressidChain must be initiatedid
            if (chainId != addrChainId) {
                return Result.getFailed(AccountErrorCode.CHAINID_ERROR);
            }
        }
        return getSuccess();
    }

}
