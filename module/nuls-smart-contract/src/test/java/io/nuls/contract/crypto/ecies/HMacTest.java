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

import io.nuls.core.crypto.HexUtil;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.macs.HMac;
import org.bouncycastle.crypto.params.KeyParameter;
import org.junit.Test;

/**
 * @author: PierreLuo
 * @date: 2019-06-01
 */
public class HMacTest {

    @Test
    public void test() {
        String iv = "00000000000000000000000000000000";
        String pubKeyA = "0410baeeb267e1d680adf4e2ad0eb61b6a3173657971c0209425406883f09ac639c7dad2baf8ab2d66e6b64c3cbd4dd488de91cc47b5ead45db299a929c4ebd468";
        String ciphertext = "b4d6ecbd61b3630abf609e102fcbd125";
        String dataToMac = iv + pubKeyA + ciphertext;
        String macKey = "785ac461b5c8607c39ec4f63e1004f19a77c371e6f91293f66d4c19c02524265";
        HMac mac = new HMac(new SHA256Digest());
        mac.init(new KeyParameter(HexUtil.decode(macKey)));

        byte[] byteArray = HexUtil.decode(dataToMac);
        mac.update(byteArray, 0, byteArray.length);
        byte[] macOutput = new byte[mac.getMacSize()];
        mac.doFinal(macOutput, 0);
        System.out.println(HexUtil.encode(macOutput));
        // hmac hex string: bcf0e6f47bf5622e3596104f4d1bcd0bc4f643a196f0520be834bb0b4d1043fa
    }
}
