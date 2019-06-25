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
        byte[] bytes = HexUtil.decode("92d1552a53f2b526895542131bc768eae406ece0b8f5437631d5b0cc750b89e6");
        Digest digest = new SHA512Digest();
        digest.update(bytes, 0, bytes.length);
        byte[] rsData = new byte[digest.getDigestSize()];
        digest.doFinal(rsData, 0);
        int[] result = uint8Array(rsData);
        System.out.println(HexUtil.encode(rsData));
        System.out.println(Arrays.toString(result));
        // sha512 hex string: fc72ef919a913dd93d4d83215c0db7a9895c82aee1987c2cefdf1911caee2a154039be04f02552cf6870f3aa0ada43af8c02b3d20e5db90c5ae2ea146f6824ab
    }

    private int[] uint8Array(byte[] bytes) {
        int[] result = new int[bytes.length];
        for (int i = 0, len = bytes.length; i < len; i++) {
            result[i] = Byte.toUnsignedInt(bytes[i]);
        }
        return result;
    }

}
