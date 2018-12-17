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

import com.google.common.primitives.UnsignedBytes;
import io.nuls.base.basic.*;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.NulsSignData;
import io.nuls.tools.basic.Result;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.Comparator;

public class P2PHKSignature extends BaseNulsData {

    public static final int SERIALIZE_LENGTH = 110;

    private NulsSignData signData;

    private byte[] publicKey;

    public P2PHKSignature() {
    }

    public P2PHKSignature(byte[] signBytes, byte[] publicKey) {
        this.signData = new NulsSignData();
        try {
            this.signData.parse(signBytes, 0);
        } catch (NulsException e) {
            Log.error(e);
        }
        this.publicKey = publicKey;
    }

    public P2PHKSignature(NulsSignData signData, byte[] publicKey) {
        this.signData = signData;
        this.publicKey = publicKey;
    }

    public NulsSignData getSignData() {
        return signData;
    }

    public void setSignData(NulsSignData signData) {
        this.signData = signData;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public Result verifySign(NulsDigestData digestData) {
        boolean b = ECKey.verify(digestData.getDigestBytes(), signData.getSignBytes(), this.getPublicKey());
        if (b) {
            return new Result(true);
        } else {
            return new Result(false);
        }
    }

    public byte[] getSignerHash160() {
        return SerializeUtils.sha256hash160(getPublicKey());
    }

    public static P2PHKSignature createFromBytes(byte[] bytes) throws NulsException {
        P2PHKSignature sig = new P2PHKSignature();
        sig.parse(bytes, 0);
        return sig;
    }

    public byte[] getBytes() {
        try {
            return this.serialize();
        } catch (IOException e) {
            Log.error(e);
            return null;
        }
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(publicKey.length);
        stream.write(publicKey);
        stream.writeNulsData(signData);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int length = byteBuffer.readByte();
        this.publicKey = byteBuffer.readBytes(length);
        this.signData = new NulsSignData();
        this.signData.parse(byteBuffer);
    }

    @Override
    public int size() {
        int size = 1 + publicKey.length;
        size += SerializeUtils.sizeOfNulsData(signData);
        return size;
    }

    public Result verifySignature(NulsDigestData digestData) {
        boolean b = ECKey.verify(digestData.getDigestBytes(), signData.getSignBytes(), publicKey);
        if (b) {
            return new Result(true);
        } else {
            return new Result(false);
        }
    }

    public static final Comparator<P2PHKSignature> PUBKEY_COMPARATOR = new Comparator<P2PHKSignature>() {
        private Comparator<byte[]> comparator = UnsignedBytes.lexicographicalComparator();

        @Override
        public int compare(P2PHKSignature k1, P2PHKSignature k2) {
            return comparator.compare(k1.getPublicKey(), k2.getPublicKey());
        }
    };
}
