/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.chain.cmd;

import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.chain.info.CmConstants;
import io.nuls.chain.info.CmErrorCode;
import io.nuls.chain.info.CmRuntimeInfo;
import io.nuls.chain.model.dto.AccountBalance;
import io.nuls.chain.model.dto.Asset;
import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.model.tx.txdata.TxChain;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * @author lan
 * @program nuls2.0
 * @description
 * @date 2018/11/28
 **/
public class BaseChainCmd extends BaseCmd {

    boolean isMainChain(int chainId) {
        return Integer.valueOf(CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID)) == chainId;
    }

    boolean isMainAsset(String assetKey) {
        String chainId = CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_CHAIN_ID);
        String assetId = CmConstants.CHAIN_ASSET_MAP.get(CmConstants.NULS_ASSET_ID);
        return CmRuntimeInfo.getAssetKey(Integer.valueOf(chainId), Integer.valueOf(assetId)).equals(assetKey);
    }

    /**
     * 注册链或资产封装coinData,x%资产进入黑洞，y%资产进入锁定
     *
     * @param address
     * @param chainId
     * @param assetsId
     * @param amount         必须是整形
     * @param txSize
     * @param accountBalance
     * @return
     * @throws NulsRuntimeException
     */
    public CoinData getRegCoinData(byte[] address, int chainId, int assetsId, String amount,
                                   int txSize, AccountBalance accountBalance) throws NulsRuntimeException {
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;
        CoinData coinData = new CoinData();
        String lockRate = CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSITNULS_lOCK);
        BigInteger lockAmount = new BigDecimal(amount).multiply(new BigDecimal(lockRate)).toBigInteger();
        BigInteger destroyAmount = new BigInteger(amount).subtract(lockAmount);
        CoinTo to1 = new CoinTo(address, chainId, assetsId, lockAmount, -1);
        CoinTo to2 = new CoinTo(CmConstants.BLACK_HOLE_ADDRESS, chainId, assetsId, destroyAmount, 0);
        coinData.addTo(to1);
        txSize += to1.size();
        coinData.addTo(to2);
        txSize += to2.size();
        //手续费
        CoinFrom from = new CoinFrom(address, chainId, assetsId, new BigDecimal(amount).toBigInteger(), ByteUtils.copyOf(accountBalance.getNonce().getBytes(), 8), (byte) 0);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getMaxFee(txSize);
        String fromAmount = BigIntegerUtils.addToString(amount, fee.toString());
        if (BigIntegerUtils.isLessThan(accountBalance.getAvailable(), fromAmount)) {
            throw new NulsRuntimeException(CmErrorCode.BALANCE_NOT_ENOUGH);
        }
        from.setAmount(BigIntegerUtils.stringToBigInteger(fromAmount));
        coinData.addFrom(from);
        return coinData;
    }

    /**
     * 注销资产进行处理
     *
     * @param address
     * @param chainId
     * @param assetsId
     * @param amount
     * @param txSize
     * @param txHash
     * @param accountBalance
     * @return
     * @throws NulsRuntimeException
     */
    public CoinData getDisableCoinData(byte[] address, int chainId, int assetsId, String amount,
                                       int txSize, String txHash, AccountBalance accountBalance) throws NulsRuntimeException {
        txSize = txSize + P2PHKSignature.SERIALIZE_LENGTH;

        String lockRate = CmConstants.PARAM_MAP.get(CmConstants.ASSET_DEPOSITNULS_lOCK);
        BigInteger lockAmount = new BigDecimal(amount).multiply(new BigDecimal(lockRate)).toBigInteger();
        CoinTo to = new CoinTo(address, chainId, assetsId, lockAmount, 0);

        CoinData coinData = new CoinData();
        coinData.addTo(to);
        txSize += to.size();
        //手续费
        CoinFrom from = new CoinFrom(address, chainId, assetsId, new BigInteger(amount), ByteUtils.copyOf(txHash.getBytes(), 8), (byte) -1);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getMaxFee(txSize);
        String fromAmount = BigIntegerUtils.addToString(amount, fee.toString());
        if (BigIntegerUtils.isLessThan(accountBalance.getAvailable(), fromAmount)) {
            throw new NulsRuntimeException(CmErrorCode.BALANCE_NOT_ENOUGH);
        }
        from.setAmount(new BigInteger(fromAmount));
        coinData.addFrom(from);
        return coinData;
    }

    protected Chain buildChainTxData(String txHex, Transaction tx, boolean isDelete) {
        try {
            byte[] txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes, 0);
            TxChain txChain = new TxChain();
            txChain.parse(tx.getTxData(), 0);
            Chain chain = new Chain(txChain, isDelete);
            if (isDelete) {
                chain.setDelTxHash(tx.getHash().toString());
            } else {
                chain.setRegTxHash(tx.getHash().toString());
            }
            return chain;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }

    protected Asset buildAssetTxData(String txHex, Transaction tx) {
        try {
            byte[] txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes, 0);
            TxChain txChain = new TxChain();
            txChain.parse(tx.getTxData(), 0);
            Asset asset = new Asset(txChain);
            asset.setTxHash(tx.getHash().toString());
            return asset;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
}
