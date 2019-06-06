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
import java.nio.charset.StandardCharsets;

import static io.nuls.core.crypto.ECKey.CURVE;

/**
 * @author: PierreLuo
 * @date: 2019-06-03
 */
public class ECIESUtil {
    private static final ECPrivateKeyParameters EPHEM_PRIVATE_KEY = new ECPrivateKeyParameters(new BigInteger(1, HexUtil.decode("8653b44d4acebec2cd64a015b2e509c70c9049a692e71b08fe7f52cc1fa5595f")), CURVE);
    private static final byte[] EPHEM_PUBLIC_KEY_BYTES = HexUtil.decode("0410baeeb267e1d680adf4e2ad0eb61b6a3173657971c0209425406883f09ac639c7dad2baf8ab2d66e6b64c3cbd4dd488de91cc47b5ead45db299a929c4ebd468");
    private static final ECPublicKeyParameters EPHEM_PUBLIC_KEY = new ECPublicKeyParameters(CURVE.getCurve().decodePoint(EPHEM_PUBLIC_KEY_BYTES), CURVE);

    public static ECIESEncryptedData encryptByPublicKey(byte[] userPubKey, String msg) {
        // derive
        byte[] sharedSecret = deriveSharedSecret(EPHEM_PRIVATE_KEY, userPubKey);

        // sha512
        byte[] rsData = Sha512Hash.sha512(sharedSecret);

        // aes-256-cbc
        byte[] encryptionKey = new byte[32];
        byte[] macKey = new byte[32];
        System.arraycopy(rsData, 0, encryptionKey, 0, 32);
        System.arraycopy(rsData, 32, macKey, 0, 32);
        // iv: EncryptedData.DEFAULT_IV
        EncryptedData encrypt = AESEncrypt.encrypt(msg.getBytes(StandardCharsets.UTF_8), new KeyParameter(encryptionKey));
        byte[] encryptedBytes = encrypt.getEncryptedBytes();

        // HMac with sha256
        byte[] dataToMacBytes = Arrays.concatenate(EncryptedData.DEFAULT_IV, EPHEM_PUBLIC_KEY_BYTES, encryptedBytes);
        byte[] macOutput = HMacWithSha256.hmac(dataToMacBytes, macKey);
        return new ECIESEncryptedData(encryptedBytes, macOutput);
    }

    public static String decryptByPrivateKey(byte[] userPriKey, ECIESEncryptedData encryptedData) throws CryptoException {
        byte[] encryptedBytes = encryptedData.getEncrypt();
        byte[] mac = encryptedData.getMac();
        // derive
        byte[] sharedSecret = deriveSharedSecret(userPriKey, EPHEM_PUBLIC_KEY);

        // sha512
        byte[] rsData = Sha512Hash.sha512(sharedSecret);

        // verify HMac with sha256
        byte[] encryptionKey = new byte[32];
        byte[] macKey = new byte[32];
        System.arraycopy(rsData, 0, encryptionKey, 0, 32);
        System.arraycopy(rsData, 32, macKey, 0, 32);
        byte[] dataToMacBytes = Arrays.concatenate(EncryptedData.DEFAULT_IV, EPHEM_PUBLIC_KEY_BYTES, encryptedBytes);
        byte[] macOutput = HMacWithSha256.hmac(dataToMacBytes, macKey);
        Assert.assertTrue("mac invalid", Arrays.areEqual(macOutput, mac));

        // aes-256-cbc
        // iv: EncryptedData.DEFAULT_IV
        EncryptedData aesData = new EncryptedData(encryptedBytes);
        byte[] decrypt = AESEncrypt.decrypt(aesData, new KeyParameter(encryptionKey));
        return new String(decrypt, StandardCharsets.UTF_8);
    }

    public static ECIESEncryptedData encryptByPrivateKey(byte[] userPriKey, String msg) {
        // derive
        byte[] sharedSecret = deriveSharedSecret(userPriKey, EPHEM_PUBLIC_KEY);

        // sha512
        byte[] rsData = Sha512Hash.sha512(sharedSecret);

        // aes-256-cbc
        byte[] encryptionKey = new byte[32];
        byte[] macKey = new byte[32];
        System.arraycopy(rsData, 0, encryptionKey, 0, 32);
        System.arraycopy(rsData, 32, macKey, 0, 32);
        // iv: EncryptedData.DEFAULT_IV
        EncryptedData encrypt = AESEncrypt.encrypt(msg.getBytes(StandardCharsets.UTF_8), new KeyParameter(encryptionKey));
        byte[] encryptedBytes = encrypt.getEncryptedBytes();

        // HMac with sha256
        byte[] dataToMacBytes = Arrays.concatenate(EncryptedData.DEFAULT_IV, EPHEM_PUBLIC_KEY_BYTES, encryptedBytes);
        byte[] macOutput = HMacWithSha256.hmac(dataToMacBytes, macKey);
        return new ECIESEncryptedData(encryptedBytes, macOutput);
    }

    public static String decryptByPublicKey(byte[] userPubKey, ECIESEncryptedData encryptedData) throws CryptoException {
        byte[] encryptedBytes = encryptedData.getEncrypt();
        byte[] mac = encryptedData.getMac();
        // derive
        byte[] sharedSecret = deriveSharedSecret(EPHEM_PRIVATE_KEY, userPubKey);

        // sha512
        byte[] rsData = Sha512Hash.sha512(sharedSecret);

        // verify HMac with sha256
        byte[] encryptionKey = new byte[32];
        byte[] macKey = new byte[32];
        System.arraycopy(rsData, 0, encryptionKey, 0, 32);
        System.arraycopy(rsData, 32, macKey, 0, 32);
        byte[] dataToMacBytes = Arrays.concatenate(EncryptedData.DEFAULT_IV, EPHEM_PUBLIC_KEY_BYTES, encryptedBytes);
        byte[] macOutput = HMacWithSha256.hmac(dataToMacBytes, macKey);
        Assert.assertTrue("mac invalid", Arrays.areEqual(macOutput, mac));

        // aes-256-cbc
        // iv: EncryptedData.DEFAULT_IV
        EncryptedData aesData = new EncryptedData(encryptedBytes);
        byte[] decrypt = AESEncrypt.decrypt(aesData, new KeyParameter(encryptionKey));
        return new String(decrypt, StandardCharsets.UTF_8);
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
