/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Transaction signature tool class
 * Transaction Signature Tool Class
 *
 * @author tag
 * 2018/10/10
 */
@Component
public class SignatureUtil {

    private static final int MAIN_CHAIN_ID = 1;

    /**
     * Verify the correctness of all signatures in the transaction
     *
     * @param chainId Current ChainID
     * @param tx      transaction
     */
    public static boolean validateTransactionSignture(int chainId, Transaction tx) throws NulsException {
        // Determine hard fork,Need a height
        long hardForkingHeight = 878000;
        boolean forked = tx.getBlockHeight() <= 0 || tx.getBlockHeight() > hardForkingHeight;
        if (chainId != MAIN_CHAIN_ID) {
            forked = true;
        }
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
                if (forked) {
                    //The new logic after using hard forks here
                    for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()) {
                        if (!ECKey.verify(tx.getHash().getBytes(), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                            throw new NulsException(new Exception("Transaction signature error !"));
                        }
                    }
                } else {
                    int signCount = tx.getCoinDataInstance().getFromAddressCount();
                    int passCount = 0;
                    for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()) {
                        if (!ECKey.verify(tx.getHash().getBytes(), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                            throw new NulsException(new Exception("Transaction signature error !"));
                        }
                        passCount++;
                        if (passCount >= signCount) {
                            break;
                        }
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
                    if (!forked && validCount >= transactionSignature.getM()) {
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
     * Cross chain transaction verification signature
     *
     * @param tx transaction
     */
    public static boolean ctxSignatureValid(int chainId, Transaction tx) throws NulsException {
        if (tx.getTransactionSignature() == null || tx.getTransactionSignature().length == 0) {
            throw new NulsException(new Exception());
        }
        TransactionSignature transactionSignature = new TransactionSignature();
        transactionSignature.parse(tx.getTransactionSignature(), 0);
        if ((transactionSignature.getP2PHKSignatures() == null || transactionSignature.getP2PHKSignatures().size() == 0)) {
            throw new NulsException(new Exception("Transaction unsigned ！"));
        }
        Set<String> fromAddressSet = tx.getCoinDataInstance().getFromAddressList();
        int signCount = tx.getCoinDataInstance().getFromAddressCount();
        int passCount = 0;
        String signAddress;
        for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()) {
            if (!ECKey.verify(tx.getHash().getBytes(), signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                throw new NulsException(new Exception("Transaction signature error !"));
            }
            signAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddress(signature.getPublicKey(), chainId));
            if (!fromAddressSet.contains(signAddress)) {
                continue;
            }
            fromAddressSet.remove(signAddress);
            passCount++;
            if (passCount >= signCount && fromAddressSet.isEmpty()) {
                break;
            }
        }
        if (passCount < signCount || !fromAddressSet.isEmpty()) {
            throw new NulsException(new Exception("Transaction signature error !"));
        }
        return true;
    }

    /**
     * Cross chain transaction verification signature
     *
     * @param tx transaction
     */
    public static boolean validateCtxSignture(Transaction tx) throws NulsException {
        if (tx.getTransactionSignature() == null || tx.getTransactionSignature().length == 0) {
            if (tx.getType() == TxType.VERIFIER_INIT || tx.getType() == TxType.VERIFIER_CHANGE) {
                return true;
            }
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
     * Verify data signature
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
     * Determine if a certain address exists in the transaction
     *
     * @param tx transaction
     */
    public static boolean containsAddress(Transaction tx, byte[] address, int chainId) throws NulsException {
        Set<String> addressSet = getAddressFromTX(tx, chainId);
        if (addressSet == null || addressSet.size() == 0) {
            return false;
        }
        return addressSet.contains(AddressTool.getStringAddressByBytes(address));
    }

    /**
     * Obtain transaction signature address
     *
     * @param tx transaction
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
     *  Verify signature（Excluding multiple signatures）If the signature verification is successful, return the address in the signature
     * @param chainId
     * @param data
     * @param signatureBytes
     * @return
     */
    public static Set<String> getAddressesAndVerifySignature(int chainId, byte[] data, byte[] signatureBytes) {
        if (signatureBytes == null || signatureBytes.length == 0) {
            return null;
        }
        List<P2PHKSignature> p2PHKSignatures;
        Set<String> addressSet = new HashSet<>();
        try {
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.parse(signatureBytes, 0);
            p2PHKSignatures = transactionSignature.getP2PHKSignatures();
        } catch (Exception e) {
           return null;
        }
        if ((p2PHKSignatures == null || p2PHKSignatures.size() == 0)) {
            return null;
        }
        for (P2PHKSignature signature : p2PHKSignatures) {
            try {
                if(!validateSignture(data,signature)){
                    return null;
                }
            } catch (NulsException e) {
                return null;
            }
            if (signature.getPublicKey() != null && signature.getPublicKey().length != 0) {
                addressSet.add(AddressTool.getStringAddressByBytes(AddressTool.getAddress(signature.getPublicKey(), chainId)));
            }
        }
        return addressSet;
    }

    /**
     * Extract address list from signature（Do not verify signature）
     * @param chainId
     * @param signatureBytes
     * @return
     */
    public static Set<String> getAddressesFromSignature(int chainId,  byte[] signatureBytes) {
        if (signatureBytes == null || signatureBytes.length == 0) {
            return null;
        }
        List<P2PHKSignature> p2PHKSignatures;
        Set<String> addressSet = new HashSet<>();
        try {
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.parse(signatureBytes, 0);
            p2PHKSignatures = transactionSignature.getP2PHKSignatures();
        } catch (Exception e) {
            return null;
        }
        if ((p2PHKSignatures == null || p2PHKSignatures.size() == 0)) {
            return null;
        }
        for (P2PHKSignature signature : p2PHKSignatures) {
            if (signature.getPublicKey() != null && signature.getPublicKey().length != 0) {
                addressSet.add(AddressTool.getStringAddressByBytes(AddressTool.getAddress(signature.getPublicKey(), chainId)));
            }
        }
        return addressSet;
    }
    /**
     * Generate transactionsTransactionSignture
     *
     * @param tx         transaction
     * @param signEckeys A key that needs to generate a regular signature
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
     * Generate multiple traditional signatures for transactions（Multiple address transfers may require）
     *
     * @param tx     transaction
     * @param eckeys Key List
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
     * Traditional signature generation for transactions
     *
     * @param tx     transaction
     * @param priKey Private key
     */
    public static P2PHKSignature createSignatureByPriKey(Transaction tx, String priKey) {
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(1, HexUtil.decode(priKey)));
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.setPublicKey(ecKey.getPubKey());
        //Using the current transaction'shashAnd the private key account of the account
        p2PHKSignature.setSignData(signDigest(tx.getHash().getBytes(), ecKey));
        return p2PHKSignature;
    }

    /**
     * Traditional signature generation for transactions
     *
     * @param tx    transaction
     * @param ecKey Secret key
     */
    public static P2PHKSignature createSignatureByEckey(Transaction tx, ECKey ecKey) {
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.setPublicKey(ecKey.getPubKey());
        //Using the current transaction'shashAnd the private key account of the account
        p2PHKSignature.setSignData(signDigest(tx.getHash().getBytes(), ecKey));
        return p2PHKSignature;
    }


    public static P2PHKSignature createSignatureByEckey(NulsHash hash, ECKey ecKey) {
        P2PHKSignature p2PHKSignature = new P2PHKSignature();
        p2PHKSignature.setPublicKey(ecKey.getPubKey());
        //Using the current transaction'shashAnd the private key account of the account
        p2PHKSignature.setSignData(signDigest(hash.getBytes(), ecKey));
        return p2PHKSignature;
    }

    /**
     * Generate multiple unlock scripts
     *
     * @param signtures Signature List
     * @param pubkeys   Public Key List
     */
    public static List<Script> createInputScripts(List<byte[]> signtures, List<byte[]> pubkeys) {
        List<Script> scripts = new ArrayList<>();
        if (signtures == null || pubkeys == null || signtures.size() != pubkeys.size()) {
            return null;
        }
        //Generate unlock script
        for (int i = 0; i < signtures.size(); i++) {
            scripts.add(createInputScript(signtures.get(i), pubkeys.get(i)));
        }
        return scripts;
    }

    /**
     * Generate a single unlock script
     *
     * @param signture Signature List
     * @param pubkey   Public Key List
     */
    public static Script createInputScript(byte[] signture, byte[] pubkey) {
        Script script = null;
        if (signture != null && pubkey != null) {
            script = ScriptBuilder.createNulsInputScript(signture, pubkey);
        }
        return script;
    }

    /**
     * Generate a single locking script
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
     * Generate locking scripts for transactions
     *
     * @param tx transaction
     */
    //TODO (modifyTransactionCompilation errors caused by)
/*    public static boolean createOutputScript(Transaction tx) {
        CoinData coinData = tx.getCoinData();
        //Generate lock script
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
     * Script for generating transactions（Multiple signatures,P2SH）
     *
     * @param signtures Signature List
     * @param pubkeys   Public Key List
     */
    public static Script createP2shScript(List<byte[]> signtures, List<byte[]> pubkeys, int m) {
        Script scriptSig = null;
        //Generate redemption script
        Script redeemScript = ScriptBuilder.createByteNulsRedeemScript(m, pubkeys);
        //Create an unlock script based on the redemption script
        scriptSig = ScriptBuilder.createNulsP2SHMultiSigInputScript(signtures, redeemScript);
        return scriptSig;
    }


    /**
     * Verified script（Multiple signatures,P2SH）
     *
     * @param digestBytes Verified signature data
     * @param chunks      Script that needs to be verified
     */
    public static boolean validScriptSign(byte[] digestBytes, List<ScriptChunk> chunks) {
        if (chunks == null || chunks.size() < 2) {
            return false;
        }
        //If the script is written inOP_0The beginning represents that the script is multi signed/P2SHscript
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
     * How many signatures are required to obtain from the redemption script
     *
     * @param redeemScript Redemption script
     */
    public static int getM(Script redeemScript) {
        return Script.decodeFromOpN(redeemScript.getChunks().get(0).opcode);
    }

    /**
     * Get the public key from the script
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
     * Multi signature script signature verification
     *
     * @param digestBytes Verified signature data
     * @param signtures   Signature List
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
     * Generate transaction signature
     *
     * @param digest Transaction data that requires signature
     * @param ecKey  Signature private key
     */
    public static NulsSignData signDigest(byte[] digest, ECKey ecKey) {
        byte[] signbytes = ecKey.sign(digest);
        NulsSignData nulsSignData = new NulsSignData();
        nulsSignData.setSignBytes(signbytes);
        return nulsSignData;
    }
}
