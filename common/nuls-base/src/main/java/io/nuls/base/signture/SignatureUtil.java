/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.base.signture;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.NulsSignData;
import io.nuls.base.data.Transaction;
import io.nuls.base.script.Script;
import io.nuls.base.script.ScriptBuilder;
import io.nuls.base.script.ScriptChunk;
import io.nuls.base.script.ScriptOpCodes;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 交易签名工具类
 * Transaction Signature Tool Class
 *
 * @author tag
 * 2018/10/10
 */
@Component
public class SignatureUtil {
    /**
     * 验证交易中所有签名正确性
     *
     * @param tx 交易
     */
    public static boolean validateTransactionSignture(Transaction tx) throws NulsException {
        try {
            if (tx.getTransactionSignature() == null || tx.getTransactionSignature().length == 0) {
                throw new NulsException(new Exception());
            }
            if (!tx.isMultiSignTx()) {
                TransactionSignature transactionSignature = new TransactionSignature();
                transactionSignature.parse(tx.getTransactionSignature(), 0);
                if ((transactionSignature.getP2PHKSignatures() == null || transactionSignature.getP2PHKSignatures().size() == 0)) {
                    throw new NulsException(new Exception("Transaction unsigned ！"));
                }
                int signCount = tx.getCoinDataInstance().getFromAddressCount();
                int passCount = 0;
                for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()) {
                    if (!ECKey.verify(tx.getHash().getBytes(), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                        throw new NulsException(new Exception("Transaction signature error !"));
                    }
                    passCount++;
                    if(passCount >= signCount){
                        break;
                    }
                }
            } else {
                MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
                transactionSignature.parse(tx.getTransactionSignature(), 0);
                if ((transactionSignature.getP2PHKSignatures() == null || transactionSignature.getP2PHKSignatures().size() == 0)) {
                    throw new NulsException(new Exception("Transaction unsigned ！"));
                }
                List<P2PHKSignature> validSignatures = transactionSignature.getValidSignature();
                int validCount = 0;
                for (P2PHKSignature signature : validSignatures) {
                    if (ECKey.verify(tx.getHash().getBytes(), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                        validCount++;
                    }
                    if (validCount >= transactionSignature.getM()) {
                        break;
                    }
                }
                if (validCount < transactionSignature.getM()) {
                    throw new NulsException(new Exception("Transaction signature error !"));
                }
            }

        } catch (NulsException e) {
            Log.error("TransactionSignature parse error!");
            throw e;
        }
        return true;
    }

    /**
     * 跨链交易验证签名
     *
     * @param tx 交易
     */
    public static boolean validateCtxSignture(Transaction tx)throws NulsException{
        if (tx.getTransactionSignature() == null || tx.getTransactionSignature().length == 0) {
            return false;
        }
        TransactionSignature transactionSignature = new TransactionSignature();
        transactionSignature.parse(tx.getTransactionSignature(), 0);
        for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()) {
            if (!ECKey.verify(tx.getHash().getBytes(), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                throw new NulsException(new Exception("Transaction signature error !"));
            }
        }
        return true;
    }

    /**
     * 验证数据签名
     *
     * @param digestBytes
     * @param p2PHKSignature
     * @return
     * @throws NulsException
     */
    public static boolean validateSignture(byte[] digestBytes, P2PHKSignature p2PHKSignature) throws NulsException {
        if (null == p2PHKSignature) {
            throw new NulsException(new Exception("P2PHKSignature is null!"));
        }
        if (ECKey.verify(digestBytes, p2PHKSignature.getSignData().getSignBytes(), p2PHKSignature.getPublicKey())) {
            return true;
        }
        return false;
    }

    /**
     * 判断交易是否存在某地址
     *
     * @param tx 交易
     */
    public static boolean containsAddress(Transaction tx, byte[] address, int chainId) throws NulsException {
        Set<String> addressSet = getAddressFromTX(tx, chainId);
        if (addressSet == null || addressSet.size() == 0) {
            return false;
        }
        if (addressSet.contains(AddressTool.getStringAddressByBytes(address))) {
            return true;
        }
        return false;
    }

    /**
     * 获取交易签名地址
     *
     * @param tx 交易
     */
    public static Set<String> getAddressFromTX(Transaction tx, int chainId) throws NulsException {
        Set<String> addressSet = new HashSet<>();
        if (tx.getTransactionSignature() == null || tx.getTransactionSignature().length == 0) {
            return null;
        }
        try {
            List<P2PHKSignature> p2PHKSignatures;
            if (tx.isMultiSignTx()) {
                MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
                transactionSignature.parse(tx.getTransactionSignature(), 0);
                p2PHKSignatures = transactionSignature.getP2PHKSignatures();
            } else {
                TransactionSignature transactionSignature = new TransactionSignature();
                transactionSignature.parse(tx.getTransactionSignature(), 0);
                p2PHKSignatures = transactionSignature.getP2PHKSignatures();
            }

            if ((p2PHKSignatures == null || p2PHKSignatures.size() == 0)) {
                return null;
            }
            for (P2PHKSignature signature : p2PHKSignatures) {
                if (signature.getPublicKey() != null && signature.getPublicKey().length != 0) {
                    addressSet.add(AddressTool.getStringAddressByBytes(AddressTool.getAddress(signature.getPublicKey(), chainId)));
                }
            }
        } catch (NulsException e) {
            Log.error("TransactionSignature parse error!");
            throw e;
        }
        return addressSet;
    }

    /**
     * 生成交易TransactionSignture
     *
     * @param tx         交易
     * @param signEckeys 需要生成普通签名的秘钥
     */
    public static void createTransactionSignture(Transaction tx, List<ECKey> signEckeys) throws IOException {
        if (signEckeys == null || signEckeys.size() == 0) {
            Log.error("TransactionSignature signEckeys is null!");
            throw new NullPointerException();
        }
        TransactionSignature transactionSignature = new TransactionSignature();
        List<P2PHKSignature> p2PHKSignatures = null;
        try {
            p2PHKSignatures = createSignaturesByEckey(tx, signEckeys);
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
        } catch (IOException e) {
            Log.error("TransactionSignature serialize error!");
            throw e;
        }
    }

    /**
     * 生成交易多个传统签名（多地址转账可能会用到）
     *
     * @param tx     交易
     * @param eckeys 秘钥列表
     */
    public static List<P2PHKSignature> createSignaturesByEckey(Transaction tx, List<ECKey> eckeys) {
        List<P2PHKSignature> signatures = new ArrayList<>();
        for (ECKey ecKey : eckeys) {
            signatures.add(createSignatureByEckey(tx, ecKey));
        }
        return signatures;
    }

    public static List<P2PHKSignature> createSignaturesByEckey(NulsHash hash, List<ECKey> eckeys) {
        List<P2PHKSignature> signatures = new ArrayList<>();
        for (ECKey ecKey : eckeys) {
            signatures.add(createSignatureByEckey(hash, ecKey));
        }
        return signatures;
    }

    /**
     * 生成交易的签名传统
     *
     * @param tx     交易
     * @param priKey 私钥
     */
    public static P2PHKSignature createSignatureByPriKey(Transaction tx, String priKey) {
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(priKey)));
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.setPublicKey(ecKey.getPubKey());
        //用当前交易的hash和账户的私钥账户
        p2PHKSignature.setSignData(signDigest(tx.getHash().getBytes(), ecKey));
        return p2PHKSignature;
    }

    /**
     * 生成交易的签名传统
     *
     * @param tx    交易
     * @param ecKey 秘钥
     */
    public static P2PHKSignature createSignatureByEckey(Transaction tx, ECKey ecKey) {
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.setPublicKey(ecKey.getPubKey());
        //用当前交易的hash和账户的私钥账户
        p2PHKSignature.setSignData(signDigest(tx.getHash().getBytes(), ecKey));
        return p2PHKSignature;
    }


    public static P2PHKSignature createSignatureByEckey(NulsHash hash, ECKey ecKey) {
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.setPublicKey(ecKey.getPubKey());
        //用当前交易的hash和账户的私钥账户
        p2PHKSignature.setSignData(signDigest(hash.getBytes(), ecKey));
        return p2PHKSignature;
    }

    /**
     * 生成多个解锁脚本
     *
     * @param signtures 签名列表
     * @param pubkeys   公钥列表
     */
    public static List<Script> createInputScripts(List<byte[]> signtures, List<byte[]> pubkeys) {
        List<Script> scripts = new ArrayList<>();
        if (signtures == null || pubkeys == null || signtures.size() != pubkeys.size()) {
            return null;
        }
        //生成解锁脚本
        for (int i = 0; i < signtures.size(); i++) {
            scripts.add(createInputScript(signtures.get(i), pubkeys.get(i)));
        }
        return scripts;
    }

    /**
     * 生成单个解锁脚本
     *
     * @param signture 签名列表
     * @param pubkey   公钥列表
     */
    public static Script createInputScript(byte[] signture, byte[] pubkey) {
        Script script = null;
        if (signture != null && pubkey != null) {
            script = ScriptBuilder.createNulsInputScript(signture, pubkey);
        }
        return script;
    }

    /**
     * 生成单个鎖定脚本
     */
    public static Script createOutputScript(byte[] address) {
        Script script = null;
        if (address == null || address.length < 23) {
            return null;
        }
        //
        if (address[2] == BaseConstant.P2SH_ADDRESS_TYPE) {
            script = ScriptBuilder.createOutputScript(address, 0);
        } else {
            script = ScriptBuilder.createOutputScript(address, 1);
        }
        return script;
    }

    /**
     * 生成交易的锁定脚本
     *
     * @param tx 交易
     */
    //TODO (修改Transaction引起的编译错误)
/*    public static boolean createOutputScript(Transaction tx) {
        CoinData coinData = tx.getCoinData();
        //生成锁定脚本
        for (Coin coin : coinData.getTo()) {
            Script scriptPubkey = null;
            byte[] toAddr = coin.getAddress();
            if (toAddr[2] == BaseConstant.DEFAULT_ADDRESS_TYPE) {
                scriptPubkey = ScriptUtil.createP2PKHOutputScript(toAddr);
            } else if (toAddr[2] == BaseConstant.P2SH_ADDRESS_TYPE) {
                scriptPubkey = ScriptUtil.createP2SHOutputScript(toAddr);
            }
            if (scriptPubkey != null && scriptPubkey.getProgram().length > 0) {
                coin.setOwner(scriptPubkey.getProgram());
            }
        }
        return true;
    }*/

    /**
     * 生成交易的脚本（多重签名，P2SH）
     *
     * @param signtures 签名列表
     * @param pubkeys   公钥列表
     */
    public static Script createP2shScript(List<byte[]> signtures, List<byte[]> pubkeys, int m) {
        Script scriptSig = null;
        //生成赎回脚本
        Script redeemScript = ScriptBuilder.createByteNulsRedeemScript(m, pubkeys);
        //根据赎回脚本创建解锁脚本
        scriptSig = ScriptBuilder.createNulsP2SHMultiSigInputScript(signtures, redeemScript);
        return scriptSig;
    }


    /**
     * 验证的脚本（多重签名，P2SH）
     *
     * @param digestBytes 验证的签名数据
     * @param chunks      需要验证的脚本
     */
    public static boolean validScriptSign(byte[] digestBytes, List<ScriptChunk> chunks) {
        if (chunks == null || chunks.size() < 2) {
            return false;
        }
        //如果脚本是以OP_0开头则代表该脚本为多重签名/P2SH脚本
        if (chunks.get(0).opcode == ScriptOpCodes.OP_0) {
            byte[] redeemByte = chunks.get(chunks.size() - 1).data;
            Script redeemScript = new Script(redeemByte);
            List<ScriptChunk> redeemChunks = redeemScript.getChunks();

            LinkedList<byte[]> signtures = new LinkedList<byte[]>();
            for (int i = 1; i < chunks.size() - 1; i++) {
                signtures.add(chunks.get(i).data);
            }

            LinkedList<byte[]> pubkeys = new LinkedList<byte[]>();
            int m = Script.decodeFromOpN(redeemChunks.get(0).opcode);
            if (signtures.size() < m) {
                return false;
            }

            for (int j = 1; j < redeemChunks.size() - 2; j++) {
                pubkeys.add(redeemChunks.get(j).data);
            }

            int n = Script.decodeFromOpN(redeemChunks.get(redeemChunks.size() - 2).opcode);
            if (n != pubkeys.size() || n < m) {
                return false;
            }
            return validMultiScriptSign(digestBytes, signtures, pubkeys);
        } else {
            if (!ECKey.verify(digestBytes, chunks.get(0).data, chunks.get(1).data)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 从赎回脚本中获取需要多少人签名
     *
     * @param redeemScript 赎回脚本
     */
    public static int getM(Script redeemScript) {
        return Script.decodeFromOpN(redeemScript.getChunks().get(0).opcode);
    }

    /**
     * 获取脚本中的公钥
     */
/*    public static String getScriptAddress(List<ScriptChunk> chunks) {
        if (chunks.get(0).opcode == ScriptOpCodes.OP_0) {
            byte[] redeemByte = chunks.get(chunks.size() - 1).entity;
            Script redeemScript = new Script(redeemByte);
            Address address = new Address(BaseConstant.DEFAULT_CHAIN_ID, BaseConstant.P2SH_ADDRESS_TYPE, SerializeUtils.sha256hash160(redeemScript.getProgram()));
            return address.toString();
        } else {
            return AddressTool.getStringAddressByBytes(AddressTool.getAddress(chunks.get(1).entity,BaseConstant.DEFAULT_CHAIN_ID));
        }
    }*/

    /**
     * 多重签名脚本签名验证
     *
     * @param digestBytes 验证的签名数据
     * @param signtures   签名列表
     */
    public static boolean validMultiScriptSign(byte[] digestBytes, LinkedList<byte[]> signtures, LinkedList<byte[]> pubkeys) {
        while (signtures.size() > 0) {
            byte[] pubKey = pubkeys.pollFirst();
            if (ECKey.verify(digestBytes, signtures.getFirst(), pubKey)) {
                signtures.pollFirst();
            }
            if (signtures.size() > pubkeys.size()) {
                return false;
            }
        }
        return true;
    }

    /**
     * 生成交易签名
     *
     * @param digest 需要签名的交易数据
     * @param ecKey  签名的私钥
     */
    public static NulsSignData signDigest(byte[] digest, ECKey ecKey) {
        byte[] signbytes = ecKey.sign(digest);
        NulsSignData nulsSignData = new NulsSignData();
        nulsSignData.setSignBytes(signbytes);
        return nulsSignData;
    }
}
