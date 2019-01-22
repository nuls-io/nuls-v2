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

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.Coin;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;

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
     * 验证交易
     * Verifying transactions
     *
     * @param chainId 链ID/chain id
     * @param tx      交易/transaction info
     * @return boolean
     */
    public boolean validateTx(int chainId, Transaction tx) throws NulsException, IOException {
        switch (tx.getType()) {
            case (AccountConstant.TX_TYPE_TRANSFER):
                return transferTxValidate(chainId, tx);
            default:
                return false;
        }
    }

    /**
     * 转账交易验证
     * transfer transaction validation
     *
     * @param chainId 链ID/chain id
     * @param tx      转账交易/transfer transaction
     * @return boolean
     */
    private boolean transferTxValidate(int chainId, Transaction tx) throws NulsException {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            throw new NulsException(AccountErrorCode.CHAIN_NOT_EXIST);
        }
        if (!baseTxValidate(chain, tx)) {
            return false;
        }
        return true;
    }

    /**
     * 交易基础验证
     * 基础字段
     * 交易size
     * 交易类型
     * 交易签名
     * from的地址必须是发起链的地址（from里面的资产是否存在）
     * to的地址必须是发起链的地址（to里面的资产是否存在）
     * 交易手续费
     *
     * @param chain
     * @param tx
     * @return Result
     */
    private boolean baseTxValidate(Chain chain, Transaction tx) throws NulsException {
        // 验证字段非空、大小、长度、交易类型
        if (null == tx) {
            throw new NulsException(AccountErrorCode.TX_NOT_EXIST);
        }
        if (tx.getHash() == null || tx.getHash().size() == 0 || tx.getHash().size() > AccountConstant.TX_HASH_DIGEST_BYTE_MAX_LEN) {
            throw new NulsException(AccountErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (tx.getTime() == 0L) {
            throw new NulsException(AccountErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (tx.getRemark() != null && tx.getRemark().length > AccountConstant.TX_REMARK_MAX_LEN) {
            throw new NulsException(AccountErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (tx.size() > AccountConstant.TX_MAX_SIZE) {
            throw new NulsException(AccountErrorCode.TX_SIZE_TOO_LARGE);
        }
        if (AccountConstant.TX_TYPE_TRANSFER != tx.getType()) {
            throw new NulsException(AccountErrorCode.TX_TYPE_ERROR);
        }
        // 转账交易必须包含coinData
        if (tx.getCoinData() == null || tx.getCoinData().length == 0) {
            throw new NulsException(AccountErrorCode.TX_COINDATA_NOT_EXIST);
        }
        //coinData基础验证以及手续费 (from中所有的当前链主资产-to中所有的当前链主资产)
        CoinData coinData = TxUtil.getCoinData(tx);
        if (!validateCoinFromBase(chain, coinData.getFrom())) {
            return false;
        }
        if (!validateCoinToBase(chain, coinData.getTo())) {
            return false;
        }
        if (!validateFee(chain, tx.size(), coinData)) {
            return false;
        }
        if (!validateSign(chain, tx, coinData)) {
            return false;
        }
        return true;
    }

    /**
     * 交易签名验证
     * Transaction signature verification
     *
     * @param chain
     * @param tx    交易/transaction
     * @return boolean
     */
    private boolean validateSign(Chain chain, Transaction tx, CoinData coinData) throws NulsException {
        int chainId = chain.getConfig().getChainId();
        // 确认验证签名正确性
        if (!SignatureUtil.validateTransactionSignture(tx)) {
            throw new NulsException(AccountErrorCode.SIGNATURE_ERROR);
        }

        // 判断from中地址和签名的地址是否匹配
        for (CoinFrom coinFrom : coinData.getFrom()) {
            if (tx.isMultiSignTx()) {
                //如果是多签交易，校验from地址是否属于多签地址
                MultiSigAccount multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(chainId, AddressTool.getStringAddressByBytes(coinFrom.getAddress()));
                if (null == multiSigAccount) {
                    throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
                }
                for (byte[] bytes : multiSigAccount.getPubKeyList()) {
                    if (!SignatureUtil.containsAddress(tx, bytes, chainId)) {
                        throw new NulsException(AccountErrorCode.SIGN_ADDRESS_NOT_MATCH);
                    }
                }
            } else if (!SignatureUtil.containsAddress(tx, coinFrom.getAddress(), chainId)) {
                throw new NulsException(AccountErrorCode.SIGN_ADDRESS_NOT_MATCH);
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
            int assetsChainId = coinFrom.getAssetsChainId();
            int assetsId = coinFrom.getAssetsId();
            // 发送方from中地址和资产对应的链id必须发起链id
            if (chainId != addrChainId || chainId != assetsChainId) {
                throw new NulsException(AccountErrorCode.CHAINID_ERROR);
            }
            // 链中是否存在该资产
            if (chain.getConfig().getAssetsId() != assetsId) {
                throw new NulsException(AccountErrorCode.ASSETID_ERROR);
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
            int assetsChainId = coinTo.getAssetsChainId();
            int assetsId = coinTo.getAssetsId();
            // 接收方to中地址和资产对应的链id必须发起链id
            if (chainId != addrChainId || chainId != assetsChainId) {
                throw new NulsException(AccountErrorCode.CHAINID_ERROR);
            }
            // 链中是否存在该资产
            if (chain.getConfig().getAssetsId() != assetsId) {
                throw new NulsException(AccountErrorCode.ASSETID_ERROR);
            }
        }
        return true;
    }

    /**
     * 验证交易手续费是否正确
     *
     * @param chain    链id
     * @param txSize   tx size
     * @param coinData
     * @return Result
     */
    private boolean validateFee(Chain chain, int txSize, CoinData coinData) {
        BigInteger feeFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : coinData.getFrom()) {
            feeFrom = feeFrom.add(accrueFee(chain, coinFrom));
        }
        BigInteger feeTo = BigInteger.ZERO;
        for (CoinTo coinTo : coinData.getTo()) {
            feeTo = feeTo.add(accrueFee(chain, coinTo));
        }
        //交易中实际的手续费
        BigInteger fee = feeFrom.subtract(feeTo);
        if (BigIntegerUtils.isEqualOrLessThan(fee, BigInteger.ZERO)) {
            Result.getFailed(AccountErrorCode.INSUFFICIENT_FEE);
        }
        //根据交易大小重新计算手续费，用来验证实际手续费
        BigInteger targetFee = TransactionFeeCalculator.getNormalTxFee(txSize);
        if (BigIntegerUtils.isLessThan(fee, targetFee)) {
            Result.getFailed(AccountErrorCode.INSUFFICIENT_FEE);
        }
        return true;
    }

    /**
     * 累积计算当前coinfrom中可用于计算手续费的资产
     *
     * @param chain chain id
     * @param coin  coinfrom
     * @return BigInteger
     */
    private BigInteger accrueFee(Chain chain, Coin coin) {
        BigInteger fee = BigInteger.ZERO;
        //所有链内交易，只算发起链的主资产
        if (TxUtil.isChainAssetExist(chain, coin)) {
            fee = fee.add(coin.getAmount());
        }
        return fee;
    }

}
