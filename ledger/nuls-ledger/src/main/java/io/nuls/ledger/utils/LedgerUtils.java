package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.UnconfirmedNonce;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;

import java.io.UnsupportedEncodingException;

/**
 * Created by lanjinsheng on 2019/01/02
 */
public class LedgerUtils {
    /**
     * rockdb key
     *
     * @param address
     * @param assetId
     * @return
     */
    public static String getKeyStr(String address, int assetChainId, int assetId) {
       return  address + "-" + assetChainId + "-" + assetId;

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
     * @param assetKey
     * @param txHash
     * @param height
     * @return
     */
    public static byte[] getSnapshotTxKey(String assetKey,String txHash,long height) {
        String key = assetKey+ "-"+txHash+"-"+height;
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getSnapshotTxKeyStr(String assetKey,String txHash,long height) {
       return assetKey+ "-"+txHash+"-"+height;
    }
    /**
     * 获取账户缓存快照的key值索引，key值组成 = 资产key-区块高度
     * 存储的value值是 List<accountstates>
     * @param assetKey
     * @param height
     * @return
     */
    public static byte[] getSnapshotHeightKey(String assetKey,long height) {
        String key = assetKey+ "-"+height;
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getSnapshotHeightKeyStr(String assetKey,long height) {
        return assetKey+ "-"+height;
    }


    public static String getNonceStrByTxHash(Transaction tx){
        byte [] nonce8Bytes = ByteUtils.copyOf(tx.getHash().getDigestBytes(), 8);
        String nonce8BytesStr = HexUtil.encode(nonce8Bytes);
        return nonce8BytesStr;
    }

    public static boolean isExpiredNonce(UnconfirmedNonce unconfirmedNonce){
        if(TimeUtils.getCurrentTime() - unconfirmedNonce.getTime() > LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME){
            return true;
        }
        return false;
    }

    /**
     * 判断资产是否属于本地节点账户
     * @param chainId chainId
     * @param address address
     * @return boolean
     */
    public static boolean isNotLocalChainAccount(int chainId,byte [] address){
        try {
            int assetChainId = AddressTool.getChainIdByAddress(address);
            return (chainId != assetChainId);
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }
    }
}
