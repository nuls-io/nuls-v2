package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.tx.txdata.TxLedgerAsset;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by lanjinsheng on 2019/01/02
 *
 * @author lanjinsheng
 */
public class LedgerUtil {

    public static String getRealAddressStr(String addrContainPre) {
        return AddressTool.getRealAddress(addrContainPre);
    }

    public static String getRealAddressStr(byte[] coinAddr) {
        return AddressTool.getStringAddressNoPrefix(coinAddr);
    }

    /**
     * rockdb key
     *
     * @param address
     * @param assetId
     * @return
     */
    public static String getKeyStr(String address, int assetChainId, int assetId) {
        return address + "-" + assetChainId + "-" + assetId;

    }

    /**
     * rockdb key
     *
     * @param address address
     * @param assetId assetId
     * @return byte[]
     */
    public static byte[] getKey(String address, int assetChainId, int assetId) {
        String key = address + "-" + assetChainId + "-" + assetId;
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            Log.error(e);
        }
        return null;
    }

    public static byte[] getNonceDecode(String nonceStr) {
        return HexUtil.decode(nonceStr);
    }

    public static String getNonceEncode(byte[] nonce) {
        return HexUtil.encode(nonce);
    }

    public static String getNonceEncodeByTx(Transaction tx) {
        byte[] out = new byte[8];
        byte[] in = tx.getHash().getBytes();
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        return HexUtil.encode(out);
    }

    public static byte[] getNonceByTx(Transaction tx) {
        byte[] out = new byte[8];
        byte[] in = tx.getHash().getBytes();
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        return out;
    }

    public static String getNonceEncodeByTxHash(String txHash) {
        return txHash.substring(txHash.length() - 16);
    }

    public static byte[] getNonceDecodeByTxHash(String txHash) {
        byte[] out = new byte[8];
        byte[] in = HexUtil.decode(txHash);
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        return out;
    }

    public static boolean equalsNonces(byte[] nonce1, byte[] nonce2) {
        return Arrays.equals(nonce1, nonce2);
    }

    /**
     * 判断资产是否属于本地节点账户
     *
     * @param chainId chainId
     * @param address address
     * @return boolean
     */
    public static boolean isNotLocalChainAccount(int chainId, byte[] address) {
        try {
            int assetChainId = AddressTool.getChainIdByAddress(address);
            return (chainId != assetChainId);
        } catch (Exception e) {
            LoggerUtil.logger(chainId).error(e);
            return false;
        }
    }

    /**
     * @param txType
     * @return
     */
    public static boolean isCrossTx(int txType) {
        return (txType == TxType.CROSS_CHAIN || txType == TxType.CONTRACT_TOKEN_CROSS_TRANSFER);
    }

    public static String getAccountAssetStrKey(CoinFrom from) {
        return LedgerUtil.getRealAddressStr(from.getAddress()) + "-" + from.getAssetsChainId() + "-" + from.getAssetsId();
    }

    /**
     * rockdb key
     *
     * @param address address
     * @param assetId assetId
     * @return String
     */
    public static String getAccountNoncesStrKey(String address, int assetChainId, int assetId, String nonce) {
        return address + "-" + assetChainId + "-" + assetId + "-" + nonce;
    }

    public static int getVersion(int chainId) {
        int version = ProtocolGroupManager.getCurrentVersion(chainId);
        LoggerUtil.logger(chainId).debug("verion={}", version);
        return version;
    }

    public static TxLedgerAsset map2TxLedgerAsset(Map<String, Object> map) {
        TxLedgerAsset txLedgerAsset = new TxLedgerAsset();
        txLedgerAsset.setName(String.valueOf(map.get("assetName")));
        BigInteger initNumber = new BigInteger(String.valueOf(map.get("initNumber")));
        txLedgerAsset.setInitNumber(initNumber);
        txLedgerAsset.setDecimalPlace(Short.valueOf(map.get("decimalPlace").toString()));
        txLedgerAsset.setSymbol(String.valueOf(map.get("assetSymbol")));
        txLedgerAsset.setAddress(AddressTool.getAddress(map.get("assetOwnerAddress").toString()));
        return txLedgerAsset;

    }

    public static void dealAssetAddressIndex(Map<String, List<String>> assetAddressIndex, int chainId, int assetId, String address) {
        String assetIndexKey = chainId + "-" + assetId;
        List<String> addressList = null;
        if (null == assetAddressIndex.get(assetIndexKey)) {
            addressList = new ArrayList<>();
            assetAddressIndex.put(assetIndexKey, addressList);
        } else {
            addressList = assetAddressIndex.get(assetIndexKey);
        }
        addressList.add(address);
    }

    public static boolean isPermanentLock(long lockTime) {
        return (lockTime < 0);
    }
}
