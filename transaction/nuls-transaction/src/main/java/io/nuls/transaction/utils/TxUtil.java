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
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.logback.NulsLogger;
import io.nuls.tools.model.DateUtils;
import io.nuls.tools.model.StringUtils;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.Log;

/**
 * @author: Charlie
 * @date: 2018-12-05
 */
public class TxUtil {

    private static TxConfig txConfig = SpringLiteContext.getBean(TxConfig.class);

    public static CoinData getCoinData(Transaction tx) throws NulsException {
        if (null == tx) {
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
        if (null == txBytes || txBytes.length == 0) {
            throw new NulsException(TxErrorCode.DATA_NOT_FOUND);
        }
        try {
            return Transaction.getInstance(txBytes);
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_TX_ERROR);
        }
    }

    public static <T> T getInstance(byte[] bytes, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (null == bytes || bytes.length == 0) {
            throw new NulsException(TxErrorCode.DATA_NOT_FOUND);
        }
        try {
            BaseNulsData baseNulsData = clazz.getDeclaredConstructor().newInstance();
            baseNulsData.parse(new NulsByteBuffer(bytes));
            return (T) baseNulsData;
        } catch (NulsException e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_ERROR);
        } catch (Exception e) {
            Log.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_ERROR);
        }
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
            throw new NulsException(TxErrorCode.DATA_NOT_FOUND);
        }
        return getInstance(RPCUtil.decode(data), clazz);
    }


    /**
     * HEX反序列化
     * @param hex
     * @param clazz
     * @param <T>
     * @return
     * @throws NulsException
     */
    public static <T> T getInstance(String hex, Class<? extends BaseNulsData> clazz) throws NulsException {
        if (StringUtils.isBlank(hex)) {
            throw new NulsException(TxErrorCode.DATA_NOT_FOUND);
        }
        return getInstance(HexUtil.decode(hex), clazz);
    }


    public static boolean isNulsAsset(Coin coin) {
        return isNulsAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isNulsAsset(int chainId, int assetId) {

        if (chainId == txConfig.getMainChainId()
                && assetId == txConfig.getMainAssetId()) {
            return true;
        }
        return false;
    }

    public static boolean isChainAssetExist(Chain chain, Coin coin) {
        if (chain.getConfig().getChainId() == coin.getAssetsChainId() &&
                chain.getConfig().getAssetId() == coin.getAssetsId()) {
            return true;
        }
        return false;
    }

    /**
     * 从智能合约TxData中获取地址
     *
     * @param txData
     * @return
     */
    public static String extractContractAddress(byte[] txData) {
        if (txData == null) {
            return null;
        }
        int length = txData.length;
        if (length < Address.ADDRESS_LENGTH * 2) {
            return null;
        }
        byte[] contractAddress = new byte[Address.ADDRESS_LENGTH];
        System.arraycopy(txData, Address.ADDRESS_LENGTH, contractAddress, 0, Address.ADDRESS_LENGTH);
        return AddressTool.getStringAddressByBytes(contractAddress);
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

    public static boolean isLegalContractAddress(byte[] addressBytes, Chain chain) {
        if (addressBytes == null) {
            return false;
        }
        return AddressTool.validContractAddress(addressBytes, chain.getChainId());
    }

    public static void txInformationDebugPrint(Chain chain, Transaction tx, NulsLogger nulsLogger) {
        if (tx.getType() == 1) {
            return;
        }
        nulsLogger.debug("");
        nulsLogger.debug("**************************************************");
        nulsLogger.debug("Transaction information");
        nulsLogger.debug("type: {}", tx.getType());
        nulsLogger.debug("txHash: {}", tx.getHash().getDigestHex());
        nulsLogger.debug("time: {}", DateUtils.timeStamp2DateStr(tx.getTime()));
        nulsLogger.debug("size: {}B,  -{}KB, -{}MB",
                String.valueOf(tx.getSize()), String.valueOf(tx.getSize() / 1024), String.valueOf(tx.getSize() / 1024 / 1024));
        byte[] remark = tx.getRemark();
        try {
            String remarkStr =  remark == null ? "" : new String(tx.getRemark(),"UTF-8");
            nulsLogger.debug("remark: {}", remarkStr);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        CoinData coinData = null;
        try {
            if (tx.getCoinData() != null) {
                coinData = tx.getCoinDataInstance();
            }
        } catch (NulsException e) {
            e.printStackTrace();
        }
        if (coinData != null) {
            nulsLogger.debug("coinData:");
            List<CoinFrom> coinFromList = coinData.getFrom();
            if (coinFromList == null) {
                nulsLogger.debug("\tcoinFrom: null");
            } else if (coinFromList.size() == 0) {
                nulsLogger.debug("\tcoinFrom: size 0");
            } else {
                nulsLogger.debug("\tcoinFrom: ");
                for (int i = 0; i < coinFromList.size(); i++) {
                    CoinFrom coinFrom = coinFromList.get(i);
                    nulsLogger.debug("\tFROM_{}:", i);
                    nulsLogger.debug("\taddress: {}", AddressTool.getStringAddressByBytes(coinFrom.getAddress()));
                    nulsLogger.debug("\tamount: {}", coinFrom.getAmount());
                    nulsLogger.debug("\tassetChainId: [{}]", coinFrom.getAssetsChainId());
                    nulsLogger.debug("\tassetId: [{}]", coinFrom.getAssetsId());
                    nulsLogger.debug("\tnonce: {}", HexUtil.encode(coinFrom.getNonce()));
                    nulsLogger.debug("\tlocked(0普通交易，-1解锁金额交易（退出共识，退出委托)): [{}]", coinFrom.getLocked());
                    nulsLogger.debug("");
                }
            }

            List<CoinTo> coinToList = coinData.getTo();
            if (coinToList == null) {
                nulsLogger.debug("\tcoinTo: null");
            } else if (coinToList.size() == 0) {
                nulsLogger.debug("\tcoinTo: size 0");
            } else {
                nulsLogger.debug("\tcoinTo: ");
                for (int i = 0; i < coinToList.size(); i++) {
                    CoinTo coinTo = coinToList.get(i);
                    nulsLogger.debug("\tTO_{}:", i);
                    nulsLogger.debug("\taddress: {}", AddressTool.getStringAddressByBytes(coinTo.getAddress()));
                    nulsLogger.debug("\tamount: {}", coinTo.getAmount());
                    nulsLogger.debug("\tassetChainId: [{}]", coinTo.getAssetsChainId());
                    nulsLogger.debug("\tassetId: [{}]", coinTo.getAssetsId());
                    nulsLogger.debug("\tlocked(解锁高度或解锁时间，-1为永久锁定): [{}]", coinTo.getLockTime());
                    nulsLogger.debug("");
                }
            }

        } else {
            nulsLogger.debug("coinData: null");
        }
        nulsLogger.debug("**************************************************");
        nulsLogger.debug("");
    }


    /**
     * 对交易进行模块分组
     *
     * @param chain
     * @param moduleVerifyMap
     * @param tx
     * @throws NulsException
     */
    public static void moduleGroups(Chain chain, Map<TxRegister, List<String>> moduleVerifyMap, Transaction tx) throws NulsException {
        //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
        TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
        String txStr;
        try {
            txStr = RPCUtil.encode(tx.serialize());
        } catch (Exception e) {
            throw new NulsException(e);
        }
        if (moduleVerifyMap.containsKey(txRegister)) {
            moduleVerifyMap.get(txRegister).add(txStr);
        } else {
            List<String> txStrList = new ArrayList<>();
            txStrList.add(txStr);
            moduleVerifyMap.put(txRegister, txStrList);
        }
    }
}
