/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.account.util;

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.NonceBalance;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.rpc.call.LedgerCall;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.Coin;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.Transaction;
import io.nuls.core.basic.Result;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;

import java.math.BigInteger;
import java.util.HashMap;

/**
 * 交易工具类
 *
 * @author: qinyifeng
 * @date: 2018-12-12
 */
public class TxUtil {

    /**
     * 校验该资产是否是该链主资产
     *
     * @param chain
     * @param assetId
     * @return
     */
    public static boolean isMainAsset(Chain chain,int assetChainId, int assetId) {
        if (chain == null) {
            throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
        }
        if (chain.getConfig().getChainId() == assetChainId && chain.getConfig().getAssetId() == assetId) {
            return true;
        }
        return false;
    }

    /**
     * 是否主网资产
     *
     * @param chainId
     * @param assetId
     * @return
     */
    public static boolean isNulsAsset(int chainId, int assetId) {
        if (chainId == NulsConfig.MAIN_CHAIN_ID
                && assetId == NulsConfig.MAIN_ASSETS_ID) {
            return true;
        }
        return false;
    }

    public static boolean isNulsAsset(Coin coin) {
        return isNulsAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    /**
     * 是否是指定链的资产
     *
     * @param chain
     * @param coin
     * @return
     */
    public static boolean isChainAssetExist(Chain chain, Coin coin) {
        if (chain.getConfig().getChainId() == coin.getAssetsChainId() &&
                chain.getConfig().getAssetId() == coin.getAssetsId()) {
            return true;
        }
        return false;
    }


    /**
     * 查询账户余额（未确认）
     *
     * @param chain
     * @param assetChainId
     * @param assetId
     * @param addressByte
     * @return
     */
    public static BigInteger getBalance(Chain chain, int assetChainId, int assetId, byte[] addressByte) {
        String address = AddressTool.getStringAddressByBytes(addressByte);
        HashMap balanceNonce = LedgerCall.getBalanceNonce(chain, assetChainId, assetId, address);
        if (balanceNonce != null) {
            Object available = balanceNonce.get("available");
            return BigIntegerUtils.stringToBigInteger(String.valueOf(available));
        }
        return new BigInteger("0");
    }

    /**
     * 查询账户余额（未确认）
     *
     * @param chain
     * @param assetChainId
     * @param assetId
     * @param addressByte
     * @return
     */
    public static NonceBalance getBalanceNonce(Chain chain, int assetChainId, int assetId, byte[] addressByte) {
        String address = AddressTool.getStringAddressByBytes(addressByte);
        HashMap balanceNonce = LedgerCall.getBalanceNonce(chain, assetChainId, assetId, address);
        if (balanceNonce != null) {
            Object available = balanceNonce.get("available");
            String strNonce = (String)balanceNonce.get("nonce");
            return new NonceBalance(RPCUtil.decode(strNonce), BigIntegerUtils.stringToBigInteger(String.valueOf(available)));
        }
        return new NonceBalance(null, new BigInteger("0"));
    }

    /**
     * 查询账户余额（已确认）
     *
     * @param chain
     * @param assetChainId
     * @param assetId
     * @param addressByte
     * @return
     */
    public static BigInteger getConfirmedBalance(Chain chain, int assetChainId, int assetId, byte[] addressByte) {
        String address = AddressTool.getStringAddressByBytes(addressByte);
        return LedgerCall.getBalance(chain, assetChainId, assetId, address);
    }

    public static CoinData getCoinData(Transaction tx) throws NulsException {
        if (null == tx) {
            throw new NulsException(AccountErrorCode.TX_NOT_EXIST);
        }
        try {
            return tx.getCoinDataInstance();
        } catch (NulsException e) {
            LoggerUtil.LOG.error(e);
            throw new NulsException(AccountErrorCode.DESERIALIZE_ERROR);
        }
    }

    public static Transaction getTransaction(byte[] txBytes) throws NulsException {
        if (null == txBytes || txBytes.length == 0) {
            throw new NulsException(AccountErrorCode.DATA_NOT_FOUND);
        }
        try {
            return Transaction.getInstance(txBytes);
        } catch (NulsException e) {
            LoggerUtil.LOG.error(e);
            throw new NulsException(AccountErrorCode.DESERIALIZE_ERROR);
        }
    }

    public static Transaction getTransaction(String hex) throws NulsException {
        if (StringUtils.isBlank(hex)) {
            throw new NulsException(AccountErrorCode.DATA_NOT_FOUND);
        }
        return getTransaction(RPCUtil.decode(hex));
    }

    /**
     * RPCUtil 反序列化
     * @param data
     * @param clazz
     * @param <T>
     * @return
     * @throws NulsException
     */
    public static <T> T getInstanceRpcStr(String data, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (StringUtils.isBlank(data)) {
            throw new NulsException(AccountErrorCode.DATA_NOT_FOUND);
        }
        return getInstance(RPCUtil.decode(data), clazz);
    }

    public static <T> T getInstance(byte[] bytes, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (null == bytes || bytes.length == 0) {
            throw new NulsException(AccountErrorCode.DATA_NOT_FOUND);
        }
        try {
            BaseNulsData baseNulsData = clazz.getDeclaredConstructor().newInstance();
            baseNulsData.parse(new NulsByteBuffer(bytes));
            return (T) baseNulsData;
        } catch (NulsException e) {
            LoggerUtil.LOG.error(e);
            throw new NulsException(AccountErrorCode.DESERIALIZE_ERROR);
        } catch (Exception e) {
            LoggerUtil.LOG.error(e);
            throw new NulsException(AccountErrorCode.DESERIALIZE_ERROR);
        }
    }

    public static <T> T getInstance(String hex, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (StringUtils.isBlank(hex)) {
            throw new NulsException(AccountErrorCode.DATA_NOT_FOUND);
        }
        return getInstance(RPCUtil.decode(hex), clazz);
    }

    public static Result getSuccess() {
        return Result.getSuccess(AccountErrorCode.SUCCESS);
    }

}
