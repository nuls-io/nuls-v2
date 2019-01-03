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

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionPO;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2018-12-05
 */
public class TxUtil {

    public static CoinData getCoinData(Transaction tx) throws NulsException {
        if(null == tx){
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        try {
            return tx.getCoinDataInstance();
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_COINDATA_ERROR);
        }
    }

    public static Transaction getTransaction(byte[] txBytes) throws NulsException {
        if(null == txBytes || txBytes.length == 0){
            throw new NulsException(TxErrorCode.DATA_NOT_FOUND);
        }
        try {
            return Transaction.getInstance(txBytes);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_TX_ERROR);
        }
    }

    public static Transaction getTransaction(String hex) throws NulsException {
        if(StringUtils.isBlank(hex)){
            throw new NulsException(TxErrorCode.DATA_NOT_FOUND);
        }
       return getTransaction(HexUtil.decode(hex));
    }

    public static <T> T getInstance(byte[] bytes, Class<? extends BaseNulsData> clazz) throws NulsException {
        if(null == bytes || bytes.length == 0){
            throw new NulsException(TxErrorCode.DATA_NOT_FOUND);
        }
        try {
            BaseNulsData baseNulsData = clazz.getDeclaredConstructor().newInstance();
            baseNulsData.parse(new NulsByteBuffer(bytes));
            return (T) baseNulsData;
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_ERROR);
        } catch (Exception e){
            Log.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_ERROR);
        }
    }

    public static <T> T getInstance(String hex, Class<? extends BaseNulsData> clazz) throws NulsException {
        if(StringUtils.isBlank(hex)){
            throw new NulsException(TxErrorCode.DATA_NOT_FOUND);
        }
       return getInstance(HexUtil.decode(hex), clazz);
    }


    public static boolean isNulsAsset(Coin coin) {
        return isNulsAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isNulsAsset(int chainId, int assetId) {
        if (chainId == TxConstant.NULS_CHAINID
                && assetId == TxConstant.NULS_CHAIN_ASSETID) {
            return true;
        }
        return false;
    }

    public static boolean isChainAssetExist(Chain chain, Coin coin) {
        if(chain.getConfig().getChainId() == coin.getAssetsChainId() &&
                chain.getConfig().getAssetsId() == coin.getAssetsId()){
            return true;
        }
        return false;
    }

    public static List<TransactionPO> tx2PO(Transaction tx) throws NulsException{
        List<TransactionPO> list = new ArrayList<>();
        if(null == tx.getCoinData()){
            return list;
        }
        CoinData coinData = tx.getCoinDataInstance();
        if(coinData.getFrom() != null){
            TransactionPO transactionPO = null;
            for(CoinFrom coinFrom : coinData.getFrom()){
                transactionPO = new TransactionPO();
                transactionPO.setAddress(AddressTool.getStringAddressByBytes(coinFrom.getAddress()));
                transactionPO.setHash(tx.getHash().getDigestHex());
                transactionPO.setType(tx.getType());
                transactionPO.setAssetChainId(coinFrom.getAssetsChainId());
                transactionPO.setAssetId(coinFrom.getAssetsId());
                transactionPO.setAmount(coinFrom.getAmount());
                // 0普通交易，-1解锁金额交易（退出共识，退出委托）
                byte locked = coinFrom.getLocked();
                int state = 0;
                if(locked == -1){
                    state = 3;
                }
                transactionPO.setState(state);
                list.add(transactionPO);
            }
        }
        if(coinData.getTo() != null){
            TransactionPO transactionPO = null;
            for(CoinTo coinTo : coinData.getTo()){
                transactionPO = new TransactionPO();
                transactionPO.setAddress(AddressTool.getStringAddressByBytes(coinTo.getAddress()));
                transactionPO.setAssetChainId(coinTo.getAssetsChainId());
                transactionPO.setAssetId(coinTo.getAssetsId());
                transactionPO.setAmount(coinTo.getAmount());
                transactionPO.setHash(tx.getHash().getDigestHex());
                transactionPO.setType(tx.getType());
                // 解锁高度或解锁时间，-1为永久锁定
                Long lockTime = coinTo.getLockTime();
                int state = 1;
                if(lockTime != 0){
                    state = 2;
                }
                transactionPO.setState(state);
                list.add(transactionPO);
            }
        }
        return list;
    }


    /**
     * 获取跨链交易tx中froms里面地址的链id
     *
     * @param tx
     * @return
     */
    public static int getCrossTxFromsOriginChainId(Transaction tx) throws NulsException {
        CoinData coinData = TxUtil.getCoinData(tx);
        if (null == coinData.getFrom() || coinData.getFrom().size() == 0) {
            throw new NulsException(TxErrorCode.COINFROM_NOT_FOUND);
        }
        return AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());

    }

    /**
     * 获取跨链交易tx中tos里面地址的链id
     *
     * @param tx
     * @return
     */
    public static int getCrossTxTosOriginChainId(Transaction tx) throws NulsException {
        CoinData coinData = TxUtil.getCoinData(tx);
        if (null == coinData.getTo() || coinData.getTo().size() == 0) {
            throw new NulsException(TxErrorCode.COINFROM_NOT_FOUND);
        }
        return AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());

    }

}
