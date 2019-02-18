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
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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
            //多签交易
            if (tx.isMultiSignTx()) {
                MultiSignTxSignature txSignature = new MultiSignTxSignature();
                txSignature.parse(tx.getTransactionSignature(), 0);
                List<String> pubKeyList = new ArrayList<>();
                for (byte[] pubKey : txSignature.getPubKeyList()) {
                    pubKeyList.add(HexUtil.encode(pubKey));
                }
                //根据签名对象中的公钥列表、最小签名数生成多签账户
                MultiSigAccount multiSigAccount = multiSignAccountService.createMultiSigAccount(chainId, pubKeyList, txSignature.getM());
                //校验from地址是否与多重签名公钥列表生成的多签地址一致
                if (!Arrays.equals(multiSigAccount.getAddress().getAddressBytes(), coinFrom.getAddress())) {
                    throw new NulsException(AccountErrorCode.SIGN_ADDRESS_NOT_MATCH);
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
        Set<String> uniqueCoin = new HashSet<>();
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
            //验证账户地址,资产链id,资产id的组合唯一性
            boolean rs = uniqueCoin.add(AddressTool.getStringAddressByBytes(coinTo.getAddress()) + "-" + assetsChainId + "-" + assetsId);
            if (!rs) {
                throw new NulsException(AccountErrorCode.COINTO_DUPLICATE_COIN);
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
