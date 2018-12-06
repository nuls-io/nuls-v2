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

package io.nuls.transaction.utils;

import io.nuls.base.data.Coin;
import io.nuls.transaction.constant.TxConstant;

import java.math.BigInteger;

/**
 * @author: Charlie
 * @date: 2018-12-05
 */
public class TxUtil {

    public static boolean isCurrentChainMainAsset(Coin coin){
        return isCurrentChainMainAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isCurrentChainMainAsset(int chainId, int assetId){
        if(chainId == TxConstant.CURRENT_CHAINID
                && assetId ==TxConstant.CURRENT_CHAIN_ASSETID) {
            return true;
        }
        return false;
    }

    public static boolean isNulsAsset(Coin coin){
        return isNulsAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isNulsAsset(int chainId, int assetId){
        if(chainId == TxConstant.NUlS_CHAINID
                && assetId ==TxConstant.NUlS_CHAIN_ASSETID) {
            return true;
        }
        return false;
    }

    public static boolean assetExist(int chainId, int assetId){
        //todo 查资产是否存在
        return false;
    }

    public static byte[] getNonce(byte[] address, int chainId, int assetId){
        //todo 查nonce
        return new byte[8];
    }

    public static BigInteger getBalance(byte[] address, int chainId, int assetId){
        //todo 查余额;
        return BigInteger.TEN;
    }

    public static String getPrikey(String address, String password){
        //todo 查私钥;
        return "";
    }
}
