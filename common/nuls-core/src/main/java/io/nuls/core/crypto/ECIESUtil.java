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
package io.nuls.core.crypto;

import io.nuls.core.exception.CryptoException;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.BigIntegers;
import org.junit.Assert;

import java.math.BigInteger;

import static io.nuls.core.crypto.ECKey.CURVE;

/**
 * @author: PierreLuo
 * @date: 2019-06-03
 */
public class ECIESUtil {

    public static byte[] encrypt(byte[] userPubKey, byte[] msg) {
        ECKey ephemECKey = new ECKey();
        ECPrivateKeyParameters ephemPrivateKey = new ECPrivateKeyParameters(new BigInteger(1, ephemECKey.getPrivKeyBytes()), CURVE);
        byte[] ephemPublicKeyBytes = ephemECKey.getPubKeyPoint().getEncoded(false);
        // derive
        byte[] sharedSecret = deriveSharedSecret(ephemPrivateKey, userPubKey);

        // sha512
        byte[] rsData = Sha512Hash.sha512(sharedSecret);

        // aes-256-cbc
        byte[] encryptionKey = new byte[32];
        byte[] macKey = new byte[32];
        System.arraycopy(rsData, 0, encryptionKey, 0, 32);
        System.arraycopy(rsData, 32, macKey, 0, 32);
        // iv: EncryptedData.DEFAULT_IV
        EncryptedData encrypt = AESEncrypt.encrypt(msg, new KeyParameter(encryptionKey));
        byte[] encryptedBytes = encrypt.getEncryptedBytes();

        // HMac with sha256
        byte[] dataToMacBytes = Arrays.concatenate(EncryptedData.DEFAULT_IV, ephemPublicKeyBytes, encryptedBytes);
        byte[] macOutput = HMacWithSha256.hmac(dataToMacBytes, macKey);
        // ephemPublicKeyBytes size is 65, macOutput size is 32
        return Arrays.concatenate(ephemPublicKeyBytes, EncryptedData.DEFAULT_IV, encryptedBytes, macOutput);
    }

    public static byte[] decrypt(byte[] userPriKey, String encryptedData) throws CryptoException {
        byte[] decode = HexUtil.decode(encryptedData);
        int encryptSize = decode.length - 65 - 16 - 32;
        byte[] encryptedBytes = new byte[encryptSize];
        byte[] ephemPublicKeyBytes = new byte[65];
        byte[] iv = new byte[16];
        byte[] mac = new byte[32];
        System.arraycopy(decode, 0, ephemPublicKeyBytes, 0, 65);
        System.arraycopy(decode, 65, iv, 0, 16);
        System.arraycopy(decode, 65 + 16, encryptedBytes, 0, encryptSize);
        System.arraycopy(decode, decode.length - 32, mac, 0, 32);
        return decrypt(userPriKey, ephemPublicKeyBytes, iv, encryptedBytes, mac);
    }

    private static byte[] decrypt(byte[] userPriKey, byte[] ephemPublicKeyBytes, byte[] iv, byte[] encryptedBytes, byte[] mac) throws CryptoException {
        ECPublicKeyParameters ephemPublicKey = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(ephemPublicKeyBytes), CURVE);
        // derive
        byte[] sharedSecret = deriveSharedSecret(userPriKey, ephemPublicKey);

        // sha512
        byte[] rsData = Sha512Hash.sha512(sharedSecret);

        // verify HMac with sha256
        byte[] encryptionKey = new byte[32];
        byte[] macKey = new byte[32];
        System.arraycopy(rsData, 0, encryptionKey, 0, 32);
        System.arraycopy(rsData, 32, macKey, 0, 32);
        byte[] dataToMacBytes = Arrays.concatenate(iv, ephemPublicKeyBytes, encryptedBytes);
        byte[] macOutput = HMacWithSha256.hmac(dataToMacBytes, macKey);
        Assert.assertTrue("mac invalid", Arrays.areEqual(macOutput, mac));

        // aes-256-cbc
        // iv: EncryptedData.DEFAULT_IV
        EncryptedData aesData = new EncryptedData(encryptedBytes);
        byte[] decrypt = AESEncrypt.decrypt(aesData, new KeyParameter(encryptionKey));
        return decrypt;
    }

    private static byte[] deriveSharedSecret(ECPrivateKeyParameters priKeyParaA, byte[] pubKeyB) {
        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(priKeyParaA);
        ECPublicKeyParameters pubKeyParaB = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(pubKeyB), CURVE);
        BigInteger result = agreement.calculateAgreement(pubKeyParaB);
        byte[] sharedSecret = BigIntegers.asUnsignedByteArray(agreement.getFieldSize(), result);
        return sharedSecret;
    }

    private static byte[] deriveSharedSecret(byte[] priKeyA, ECPublicKeyParameters pubKeyParaB) {
        ECPrivateKeyParameters priKeyParaA = new ECPrivateKeyParameters(new BigInteger(1, priKeyA), CURVE);
        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(priKeyParaA);
        BigInteger result = agreement.calculateAgreement(pubKeyParaB);
        byte[] sharedSecret = BigIntegers.asUnsignedByteArray(agreement.getFieldSize(), result);
        return sharedSecret;
    }
}
