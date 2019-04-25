package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.tools.crypto.HexUtil;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by lanjinsheng on 2019/01/02
 *
 * @author lanjinsheng
 */
public class LedgerUtil {
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
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取账户缓存快照的key值，key值组成 = 资产key-txHash-区块高度
     *
     * @param assetKey
     * @param height
     * @return
     */
    public static byte[] getBlockSnapshotKey(String assetKey, long height) {
        String key = assetKey + "-" + height;
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSnapshotTxKeyStr(String assetKey, String txHash, long height) {
        return assetKey + "-" + txHash + "-" + height;
    }

    public static String getBlockSnapshotKeyStr(String assetKey, long height) {
        return assetKey + "-" + height;
    }

    /**
     * 获取账户缓存快照的key值索引，key值组成 = 资产key-区块高度
     * 存储的value值是 List<accountstates>
     *
     * @param assetKey
     * @param height
     * @return
     */
    public static byte[] getSnapshotHeightKey(String assetKey, long height) {
        String key = assetKey + "-" + height;
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSnapshotHeightKeyStr(String assetKey, long height) {
        return assetKey + "-" + height;
    }

    public static byte[] getNonceDecode(String nonceStr) {
        return HexUtil.decode(nonceStr);
    }

    public static String getNonceEncode(byte[] nonce) {
        return HexUtil.encode(nonce);
    }

    public static String getNonceEncodeByTx(Transaction tx) {
        byte[] out = new byte[8];
        byte[] in = tx.getHash().getDigestBytes();
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        String nonce8BytesStr = HexUtil.encode(out);
        return nonce8BytesStr;
    }

    public static byte[] getNonceByTx(Transaction tx) {
        byte[] out = new byte[8];
        byte[] in = tx.getHash().getDigestBytes();
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        return out;
    }

    public static String getNonceEncodeByTxHash(String txHash) {
        byte[] out = new byte[8];
        byte[] in = HexUtil.decode(txHash);
        int copyEnd = in.length;
        System.arraycopy(in, (copyEnd - 8), out, 0, 8);
        String nonce8BytesStr = HexUtil.encode(out);
        return nonce8BytesStr;
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * rockdb key
     *
     * @param address address
     * @param assetId assetId
     * @return byte[]
     */
    public static byte[] getAccountNoncesByteKey(String address, int assetChainId, int assetId, String nonce) {
        String key = address + "-" + assetChainId + "-" + assetId + "-" + nonce;
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getAccountNoncesByteKey(String assetKey, String nonce) {
        String key = assetKey + "-" + nonce;
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAccountNoncesStrKey(String assetKey, byte[] nonce) {
        return assetKey + "-" + getNonceEncode(nonce);
    }

    public static byte[] getAccountNoncesByteKey(CoinFrom from, byte[] nonce) {
        String key = AddressTool.getStringAddressByBytes(from.getAddress()) + "-" + from.getAssetsChainId() + "-" + from.getAssetsId() + "-" + getNonceEncode(nonce);
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAccountNoncesStringKey(CoinFrom from, byte[] nonce) {
        return AddressTool.getStringAddressByBytes(from.getAddress()) + "-" + from.getAssetsChainId() + "-" + from.getAssetsId() + "-" + getNonceEncode(nonce);
    }

    public static byte[] getAccountAssetByteKey(CoinFrom from) {
        String key = AddressTool.getStringAddressByBytes(from.getAddress()) + from.getAssetsChainId() + "-" + from.getAssetsId();
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAccountAssetStrKey(CoinFrom from) {
        return AddressTool.getStringAddressByBytes(from.getAddress()) + from.getAssetsChainId() + "-" + from.getAssetsId();
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
