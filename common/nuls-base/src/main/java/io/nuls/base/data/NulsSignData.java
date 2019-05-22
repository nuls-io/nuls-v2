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
package io.nuls.base.data;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.SerializeUtils;
import java.io.IOException;
import java.math.BigInteger;

/**
 * @author facjas
 */
public class NulsSignData extends BaseNulsData {

    /**
     * 签名字节组
     */
    protected byte[] signBytes;

    public NulsSignData() {
    }

    @Override
    public int size() {
        return SerializeUtils.sizeOfBytes(signBytes);
    }

    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeBytesWithLength(signBytes);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.signBytes = byteBuffer.readByLengthByte();
    }

    public byte[] getSignBytes() {
        return signBytes;
    }

    public void setSignBytes(byte[] signBytes) {
        this.signBytes = signBytes;
    }

    public NulsSignData sign(NulsHash NulsHash, BigInteger privkey) {
        ECKey ecKey = ECKey.fromPrivate(privkey);
        byte[] signBytes = ecKey.sign(NulsHash.getDigestBytes(), privkey);
        NulsSignData signData = new NulsSignData();
        try {
            signData.parse(signBytes, 0);
        } catch (NulsException e) {
            Log.error(e);
        }
        return signData;
    }

    @Override
    public String toString() {
        try {
            return HexUtil.encode(serialize());
        } catch (IOException e) {
            Log.error(e);
            return super.toString();
        }
    }
}
