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
import io.nuls.account.rpc.call.LegerCmdCall;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Coin;

import java.math.BigInteger;

/**
 * 交易工具类
 *
 * @author: qinyifeng
 * @date: 2018-12-12
 */
public class TxUtil {

    public static boolean isCurrentChainMainAsset2(Coin coin) {
        return isCurrentChainMainAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isCurrentChainMainAsset(int chainId, int assetId) {
        if (chainId == NulsConfig.CURRENT_CHAIN_ID
                && assetId == NulsConfig.CURRENT_MAIN_ASSETS_ID) {
            return true;
        }
        return false;
    }

    public static boolean isNulsAsset(Coin coin) {
        return isNulsAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isNulsAsset(int chainId, int assetId) {
        if (chainId == NulsConfig.MAIN_CHAIN_ID
                && assetId == NulsConfig.MAIN_ASSETS_ID) {
            return true;
        }
        return false;
    }

    public static boolean isTheChainMainAsset(int chainId, Coin coin) {
        return isTheChainMainAsset(chainId, coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isTheChainMainAsset(int chainId, int assetChainId, int assetId) {
        if (assetChainId == chainId
                && assetId == NulsConfig.CURRENT_MAIN_ASSETS_ID) {
            return true;
        }
        return true;
    }

    public static boolean assetExist2(int chainId, int assetId) {
        //todo 查资产是否存在
        return true;
    }

    /**
     * 查询用户账本nonce值
     *
     * @param chainId
     * @param assetId
     * @param addressByte
     * @return
     */
    public static byte[] getNonce(int chainId, int assetId, byte[] addressByte) {
        String address = AddressTool.getStringAddressByBytes(addressByte);
        byte[] nonce = new byte[]{'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h'};
        return nonce;
    }

    /**
     * 查询用户余额
     *
     * @param chainId
     * @param assetId
     * @param addressByte
     * @return
     */
    public static BigInteger getBalance(int chainId, int assetId, byte[] addressByte) {
        String address = AddressTool.getStringAddressByBytes(addressByte);
        return LegerCmdCall.getBalance(chainId, assetId, address);
    }

}
