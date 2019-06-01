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
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA512Digest;
import org.junit.Test;

import java.util.Arrays;

/**
 * @author: PierreLuo
 * @date: 2019-06-01
 */
public class Sha512Test {

    @Test
    public void test() {
        byte[] bytes = HexUtil.decode("692c40fdbe605b9966beee978ab290e7a35056dffe9ed092a87e62fce468791d");
        Digest digest = new SHA512Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] rsData = new byte[digest.getDigestSize()];
        digest.doFinal(rsData, 0);
        int[] result = uint8Array(rsData);
        System.out.println(HexUtil.encode(rsData));
        System.out.println(Arrays.toString(result));
        // sha512 hex string: 0da59e256e2ab8e510bfd90e020264e396e6d4e028d2c6f565810c58e7c9eb7d785ac461b5c8607c39ec4f63e1004f19a77c371e6f91293f66d4c19c02524265
    }

    private int[] uint8Array(byte[] bytes) {
        int[] result = new int[bytes.length];
        for (int i = 0, len = bytes.length; i < len; i++) {
            result[i] = Byte.toUnsignedInt(bytes[i]);
        }
        return result;
    }

}
