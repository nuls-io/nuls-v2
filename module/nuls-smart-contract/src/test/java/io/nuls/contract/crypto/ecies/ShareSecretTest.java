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
package io.nuls.contract.crypto.ecies;

import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import org.bouncycastle.crypto.agreement.ECDHBasicAgreement;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.util.BigIntegers;
import org.junit.Test;

import java.math.BigInteger;

import static io.nuls.core.crypto.ECKey.CURVE;

/**
 * @author: PierreLuo
 * @date: 2019-06-01
 */
public class ShareSecretTest {

    @Test
    public void test() {
        ECPrivateKeyParameters privKeyA = new ECPrivateKeyParameters(
                new BigInteger(1, HexUtil.decode("8653b44d4acebec2cd64a015b2e509c70c9049a692e71b08fe7f52cc1fa5595f")), CURVE);
        ECDHBasicAgreement agreement = new ECDHBasicAgreement();
        agreement.init(privKeyA);
        ECPublicKeyParameters pubKeyB = new ECPublicKeyParameters(
                CURVE.getCurve().decodePoint(HexUtil.decode("02fd82681e79fbe293aef1a48c6c9b1252591340bb46de1444ad5de400ff84a433")), CURVE);
        BigInteger result = agreement.calculateAgreement(pubKeyB);
        byte[] sharedSecret = BigIntegers.asUnsignedByteArray(agreement.getFieldSize(), result);
        System.out.println(HexUtil.encode(sharedSecret));
        // sharedSecret hex string: 692c40fdbe605b9966beee978ab290e7a35056dffe9ed092a87e62fce468791d
    }

    @Test
    public void getEckey() {
        ECKey key = new ECKey();
        byte[] encoded0 = key.getPubKeyPoint().getEncoded(true);
        byte[] encoded1 = key.getPubKeyPoint().getEncoded(false);
        System.out.println(HexUtil.encode(encoded0));
        System.out.println(HexUtil.encode(encoded1));
        System.out.println(key.getPublicKeyAsHex());
        System.out.println(key.getPrivateKeyAsHex());

    }
}
