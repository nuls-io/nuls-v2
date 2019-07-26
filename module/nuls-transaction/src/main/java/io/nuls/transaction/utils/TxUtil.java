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

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.*;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.constant.TxConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.TxRegister;

import java.io.UnsupportedEncodingException;
import java.util.*;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2018-12-05
 */
public class TxUtil {

    public static CoinData getCoinData(Transaction tx) throws NulsException {
        if (null == tx) {
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        try {
            return tx.getCoinDataInstance();
        } catch (NulsException e) {
            LOG.error(e);
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
            LOG.error(e);
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
            LOG.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_ERROR);
        } catch (Exception e) {
            LOG.error(e);
            throw new NulsException(TxErrorCode.DESERIALIZE_ERROR);
        }
    }

    /**
     * RPCUtil 反序列化
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     * @throws NulsException
     */
    public static <T> T getInstanceRpcStr(String data, Class<? extends BaseNulsData> clazz) throws NulsException {
        return getInstance(RPCUtil.decode(data), clazz);
    }

    /**
     * HEX反序列化
     *
     * @param hex
     * @param clazz
     * @param <T>
     * @return
     * @throws NulsException
     */
    public static <T> T getInstance(String hex, Class<? extends BaseNulsData> clazz) throws NulsException {
        return getInstance(HexUtil.decode(hex), clazz);
    }


    public static boolean isNulsAsset(Coin coin) {
        return isNulsAsset(coin.getAssetsChainId(), coin.getAssetsId());
    }

    public static boolean isNulsAsset(int chainId, int assetId) {
        TxConfig txConfig = SpringLiteContext.getBean(TxConfig.class);
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

    public static void txInformationDebugPrint(Transaction tx) {
        if (tx.getType() == 1) {
            return;
        }
        LOG.debug("");
        LOG.debug("**************************************************");
        LOG.debug("Transaction information");
        LOG.debug("type: {}", tx.getType());
        LOG.debug("txHash: {}", tx.getHash().toHex());
        LOG.debug("time: {}", NulsDateUtils.timeStamp2DateStr(tx.getTime()));
        LOG.debug("size: {}B,  -{}KB, -{}MB",
                String.valueOf(tx.getSize()), String.valueOf(tx.getSize() / 1024), String.valueOf(tx.getSize() / 1024 / 1024));
        byte[] remark = tx.getRemark();
        try {
            String remarkStr = remark == null ? "" : new String(tx.getRemark(), "UTF-8");
            LOG.debug("remark: {}", remarkStr);
        } catch (UnsupportedEncodingException e) {
            LOG.error(e);
        }

        CoinData coinData = null;
        try {
            if (tx.getCoinData() != null) {
                coinData = tx.getCoinDataInstance();
            }
        } catch (NulsException e) {
            LOG.error(e);
        }
        if (coinData != null) {
            LOG.debug("coinData:");
            List<CoinFrom> coinFromList = coinData.getFrom();
            if (coinFromList == null) {
                LOG.debug("\tcoinFrom: null");
            } else if (coinFromList.size() == 0) {
                LOG.debug("\tcoinFrom: size 0");
            } else {
                LOG.debug("\tcoinFrom: ");
                for (int i = 0; i < coinFromList.size(); i++) {
                    CoinFrom coinFrom = coinFromList.get(i);
                    LOG.debug("\tFROM_{}:", i);
                    LOG.debug("\taddress: {}", AddressTool.getStringAddressByBytes(coinFrom.getAddress()));
                    LOG.debug("\tamount: {}", coinFrom.getAmount());
                    LOG.debug("\tassetChainId: [{}]", coinFrom.getAssetsChainId());
                    LOG.debug("\tassetId: [{}]", coinFrom.getAssetsId());
                    LOG.debug("\tnonce: {}", HexUtil.encode(coinFrom.getNonce()));
                    LOG.debug("\tlocked(0普通交易，-1解锁金额交易（退出共识，退出委托)): [{}]", coinFrom.getLocked());
                    LOG.debug("");
                }
            }

            List<CoinTo> coinToList = coinData.getTo();
            if (coinToList == null) {
                LOG.debug("\tcoinTo: null");
            } else if (coinToList.size() == 0) {
                LOG.debug("\tcoinTo: size 0");
            } else {
                LOG.debug("\tcoinTo: ");
                for (int i = 0; i < coinToList.size(); i++) {
                    CoinTo coinTo = coinToList.get(i);
                    LOG.debug("\tTO_{}:", i);
                    LOG.debug("\taddress: {}", AddressTool.getStringAddressByBytes(coinTo.getAddress()));
                    LOG.debug("\tamount: {}", coinTo.getAmount());
                    LOG.debug("\tassetChainId: [{}]", coinTo.getAssetsChainId());
                    LOG.debug("\tassetId: [{}]", coinTo.getAssetsId());
                    LOG.debug("\tlocked(解锁高度或解锁时间，-1为永久锁定): [{}]", coinTo.getLockTime());
                    LOG.debug("");
                }
            }

        } else {
            LOG.debug("coinData: null");
        }
        LOG.debug("**************************************************");
        LOG.debug("");
    }


    /**
     * 对交易进行模块分组
     *
     * @param chain
     * @param moduleVerifyMap
     * @param tx
     * @throws NulsException
     */
    public static void moduleGroups(Chain chain, Map<String, List<String>> moduleVerifyMap, Transaction tx) throws NulsException {
        //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
        String txStr;
        try {
            txStr = RPCUtil.encode(tx.serialize());
        } catch (Exception e) {
            throw new NulsException(e);
        }
        moduleGroups(chain, moduleVerifyMap, tx.getType(), txStr);
    }

    /**
     * 对交易进行模块分组
     *
     * @param chain
     * @param moduleVerifyMap
     * @param txType
     * @param txStr
     * @throws NulsException
     */
    public static void moduleGroups(Chain chain, Map<String, List<String>> moduleVerifyMap, int txType, String txStr) {
        //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
        TxRegister txRegister = TxManager.getTxRegister(chain, txType);
        moduleGroups(moduleVerifyMap, txRegister, txStr);
    }

    public static void moduleGroups(Map<String, List<String>> moduleVerifyMap, TxRegister txRegister, String txStr) {
        //根据模块的统一验证器名，对所有交易进行分组，准备进行各模块的统一验证
        String moduleCode = txRegister.getModuleCode();
        if (moduleVerifyMap.containsKey(moduleCode)) {
            moduleVerifyMap.get(moduleCode).add(txStr);
        } else {
            List<String> txStrList = new ArrayList<>();
            txStrList.add(txStr);
            moduleVerifyMap.put(moduleCode, txStrList);
        }
    }


    public static byte[] getNonce(byte[] preHash) {
        byte[] nonce = new byte[8];
        byte[] in = preHash;
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), nonce, 0, 8);
        return nonce;
    }


    /**
     * 通过交易字符串解析交易类型
     *
     * @param txString
     * @return
     * @throws NulsException
     */
    public static int extractTxTypeFromTx(String txString) throws NulsException {
        String txTypeHexString = txString.substring(0, 4);
        NulsByteBuffer byteBuffer = new NulsByteBuffer(RPCUtil.decode(txTypeHexString));
        return byteBuffer.readUint16();
    }


    /**
     * 根据待打包队列存交易的map实际交易数, 来计算是放弃当前交易
     *
     * @return
     */
    public static boolean discardTx(int packableTxMapSize) {
        Random random = new Random();
        int number = random.nextInt(10);
        if (packableTxMapSize >= TxConstant.PACKABLE_TX_MAP_MAX_SIZE) {
            //扔80%
            if (number < 8) {
                return true;
            }
        } else if (packableTxMapSize >= TxConstant.PACKABLE_TX_MAP_HEAVY_SIZE) {
            //扔50%
            if (number < 5) {
                return true;
            }
        } else if (packableTxMapSize >= TxConstant.PACKABLE_TX_MAP_STRESS_SIZE) {
            //扔30%
            if (number < 3) {
                return true;
            }
        }
        return false;
    }


    /**
     * 获取两个集合的不同元素
     *
     * @param collectionMax
     * @param collectionMin
     * @return
     */
    public static Collection getDiffent(Collection collectionMax, Collection collectionMin) {
        //使用LinkeList防止差异过大时,元素拷贝
        Collection collection = new LinkedList();
        Collection max = collectionMax;
        Collection min = collectionMin;
        //先比较大小,这样会减少后续map的if判断次数
        if (collectionMax.size() < collectionMin.size()) {
            max = collectionMin;
            min = collectionMax;
        }
        Map<Object, Integer> map = new HashMap<>(max.size() * 2);
        for (Object object : max) {
            map.put(object, 1);
        }
        for (Object object : min) {
            if (map.get(object) == null) {
                collection.add(object);
            } else {
                map.put(object, 2);
            }
        }
        for (Map.Entry<Object, Integer> entry : map.entrySet()) {
            if (entry.getValue() == 1) {
                collection.add(entry.getKey());
            }
        }
        return collection;
    }

    /**
     * 获取两个集合的不同元素,去除重复
     *
     * @param collmax
     * @param collmin
     * @return
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    public static Collection getDiffentNoDuplicate(Collection collmax, Collection collmin) {
        return new HashSet(getDiffent(collmax, collmin));
    }


    /**
     * 输出三个换行符, 日志用
     * @return
     */
    public static String nextLine(){
        String lineSeparator = System.getProperty("line.separator");
        return lineSeparator + lineSeparator;
    }
}
