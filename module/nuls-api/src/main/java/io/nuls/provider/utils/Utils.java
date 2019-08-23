package io.nuls.provider.utils;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.crypto.AESEncrypt;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.CryptoException;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.ObjectUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-06-20 17:48
 */
public class Utils {

    /**
     * 对交易进行签名，并将签名传入交易中
     * @param transaction
     * @param priKey
     * @param pubKey
     * @param password
     * @return
     * @throws IOException
     */
    public static Transaction signTransaction(Transaction transaction, String priKey, String pubKey, String password) throws IOException {
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        ECKey eckey = null;
        byte[] unencryptedPrivateKey;
        //判断当前账户是否存在私钥，如果不存在私钥这为加密账户
        BigInteger newPriv = null;
        ObjectUtils.canNotEmpty(password, "the password can not be empty");
        try {
            unencryptedPrivateKey = AESEncrypt.decrypt(HexUtil.decode(priKey), password);
            newPriv = new BigInteger(1, unencryptedPrivateKey);
        } catch (CryptoException e) {
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED,"password is wrong");
        }
        eckey = ECKey.fromPrivate(newPriv);
        if (!Arrays.equals(eckey.getPubKey(), HexUtil.decode(pubKey))) {
            throw new NulsRuntimeException(CommonCodeConstanst.FAILED,"password is wrong");
        }
        P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByEckey(transaction, eckey);
        p2PHKSignatures.add(p2PHKSignature);
        transactionSignature.setP2PHKSignatures(p2PHKSignatures);
        transaction.setTransactionSignature(transactionSignature.serialize());
        return transaction;
    }

    public static int extractTxTypeFromTx(String txString) throws NulsException {
        String txTypeHexString = txString.substring(0, 4);
        NulsByteBuffer byteBuffer = new NulsByteBuffer(RPCUtil.decode(txTypeHexString));
        return byteBuffer.readUint16();
    }

    public static int getDepth(Class cls) {
        return getDepth(cls, 1);
    }

    private static int getDepth(Class cls, int depth) {
        if(depth > 3) {
            throw new RuntimeException("exceed depth");
            //return depth;
        }
        if (SdkProviderDocTool.baseType.contains(cls)) {
            return depth;
        }
        Field[] fields = cls.getDeclaredFields();
        int max = depth;
        try{
            for (Field field : fields) {
                // 每次循环初始化最初的层级
                int initial = depth;
                if (SdkProviderDocTool.baseType.contains(field.getType())) {
                    continue;
                }
                Type genericType = field.getGenericType();
                if(genericType instanceof ParameterizedType) {
                    initial++;
                    ParameterizedType pType = (ParameterizedType) genericType;
                    Type[] typeArguments = pType.getActualTypeArguments();
                    for (int i = 0; i < typeArguments.length; i++) {
                        Class<?> aClass = Class.forName(typeArguments[i].getTypeName());
                        if (SdkProviderDocTool.baseType.contains(aClass)) {
                            continue;
                        }
                        int i1 = getDepth(aClass, initial);
                        max = Math.max(i1, max);
                    }
                } else {
                    Class<?> aClass = Class.forName(genericType.getTypeName());
                    if(aClass == field.getType()) {
                        initial++;
                        int i1 = getDepth(aClass, initial);
                        max = Math.max(i1, max);
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return max;
    }

}
