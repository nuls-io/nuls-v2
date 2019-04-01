package io.nuls.ledger.utils;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.config.LedgerConfig;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.UnconfirmedAmount;
import io.nuls.ledger.model.po.UnconfirmedNonce;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.io.UnsupportedEncodingException;

/**
 * Created by lanjinsheng on 2019/01/02
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
        LedgerConfig ledgerConfig = SpringLiteContext.getBean(LedgerConfig.class);
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
     * @param height
     * @return
     */
    public static byte[] getBlockSnapshotKey(String assetKey,long height) {
        String key = assetKey+"-"+height;
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

    public static String getBlockSnapshotKeyStr(String assetKey,long height) {
        return assetKey+"-"+height;
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
        byte[] out = new byte[8];
        byte [] in = tx.getHash().getDigestBytes();
        int copyEnd = in.length;
        System.arraycopy(in,  (copyEnd-8), out, 0, 8);
        String nonce8BytesStr = RPCUtil.encode(out);
        return nonce8BytesStr;
    }
    public static String getNonceStrByTxHash(String txHash){
        byte[] out = new byte[8];
        byte [] in = RPCUtil.decode(txHash);
        int copyEnd = in.length;
        System.arraycopy(in,  (copyEnd-8), out, 0, 8);
        String nonce8BytesStr = RPCUtil.encode(out);
        return nonce8BytesStr;
    }

    public static boolean isExpiredNonce(UnconfirmedNonce unconfirmedNonce){
        if(TimeUtil.getCurrentTime() - unconfirmedNonce.getTime() > LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME){
            return true;
        }
        return false;
    }
    public static boolean isExpiredAmount(UnconfirmedAmount unconfirmedAmount){
        if(TimeUtil.getCurrentTime() - unconfirmedAmount.getTime() > LedgerConstant.UNCONFIRM_NONCE_EXPIRED_TIME){
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
    /**
     * rockdb key
     *
     * @param address address
     * @param assetId assetId
     * @return byte[]
     */
    public static byte[] getAccountNoncesByteKey(String address, int assetChainId, int assetId,String nonce) {
        LedgerConfig ledgerConfig = SpringLiteContext.getBean(LedgerConfig.class);
        String key = address + "-" + assetChainId + "-" + assetId+"-"+nonce;
        try {
            return (key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }
    /**
     * rockdb key
     *
     * @param address address
     * @param assetId assetId
     * @return String
     */
    public static String getAccountNoncesStrKey(String address, int assetChainId, int assetId,String nonce) {
        LedgerConfig ledgerConfig = SpringLiteContext.getBean(LedgerConfig.class);
        return address + "-" + assetChainId + "-" + assetId+"-"+nonce;
    }
}
