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
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易验证工具类
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
     * 交易类型
     * from的地址必须是发起链的地址（from里面的资产是否存在）
     * to的地址必须是发起链的地址（to里面的资产是否存在）
     * 交易手续费
     *
     * 转账交易验证器
     * 交易基础验证已由交易管理验证
     *
     * @param chain
     * @param tx
     * @return Result
     */
    public boolean validate(Chain chain, Transaction tx) throws NulsException {
        CoinData coinData = TxUtil.getCoinData(tx);
        if (!validateCoinFromBase(chain, coinData.getFrom())) {
            return false;
        }
        if (!validateCoinToBase(chain, coinData.getTo())) {
            return false;
        }
        if (!validateCoinDataAsset(chain, coinData)) {
            return false;
        }

        return true;
    }

    /**
     * 验证除了手续费以外的资产 from中的资产金额是否大于等于to中的资产金额要对应相等
     * @return
     */
    public boolean validateCoinDataAsset(Chain chain, CoinData coinData) throws NulsException{
        //from中资产id-资产链id作为key，存一个资产的金额总和
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
        //to中资产id-资产链id作为key，存一个资产的金额总和
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
        //比较from和to相同资产的值是否相等
        for(Map.Entry<String, BigInteger> entry : mapFrom.entrySet()){
            if(entry.getValue().compareTo(mapTo.get(entry.getKey())) == -1){
                throw new NulsException(AccountErrorCode.COINFROM_UNDERPAYMENT);
            }
        }
        return true;
    }

    /**
     * 验证交易的付款方数据
     * 1.发送方from中地址和资产对应的链id必须发起链id
     * 2.验证资产是否存在
     *
     * @param chain
     * @param listFrom
     * @return Result
     */
    public boolean validateCoinFromBase(Chain chain, List<CoinFrom> listFrom) throws NulsException {
        if (null == listFrom || listFrom.size() == 0) {
            throw new NulsException(AccountErrorCode.TX_COINFROM_NOT_FOUND);
        }
        int chainId = chain.getConfig().getChainId();
        for (CoinFrom coinFrom : listFrom) {
            int addrChainId = AddressTool.getChainIdByAddress(coinFrom.getAddress());
            //黑洞地址不能发起转账
            if(AddressTool.isBlackHoleAddress(NulsConfig.BLACK_HOLE_PUB_KEY,addrChainId,coinFrom.getAddress())){
                throw new NulsException(AccountErrorCode.ADDRESS_TRANSFER_BAN);
            }
            // 发送方from中地址对应的链id必须是发起链的id
            if (chainId != addrChainId) {
                throw new NulsException(AccountErrorCode.CHAINID_ERROR);
            }
        }
        return true;
    }

    /**
     * 验证交易的收款方数据(coinTo是不是属于同一条链)
     * 1.收款方所有地址和资产是不是属于同一条链
     * 2.验证资产是否存在
     *
     * @param listTo
     * @return Result
     */
    public boolean validateCoinToBase(Chain chain, List<CoinTo> listTo) throws NulsException {
        if (null == listTo || listTo.size() == 0) {
            throw new NulsException(AccountErrorCode.TX_COINTO_NOT_FOUND);
        }
        int chainId = chain.getConfig().getChainId();
        for (CoinTo coinTo : listTo) {
            int addrChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            // 接收方to中地址对应的链id必须发起链id
            if (chainId != addrChainId) {
                throw new NulsException(AccountErrorCode.CHAINID_ERROR);
            }
        }
        return true;
    }

}
