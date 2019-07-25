package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.ledger.constant.LedgerConstant;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

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
        return txType == TxType.CROSS_CHAIN;

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
}
