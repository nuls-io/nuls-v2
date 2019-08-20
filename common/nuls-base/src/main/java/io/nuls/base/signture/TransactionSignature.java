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


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionSignature extends BaseNulsData {

    protected List<P2PHKSignature> p2PHKSignatures;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        int signCount = p2PHKSignatures == null ? 0 : p2PHKSignatures.size();
        stream.writeVarInt(signCount);
        // 旧签名数据写入流中
        if (p2PHKSignatures != null && p2PHKSignatures.size() > 0) {
            for (P2PHKSignature p2PHKSignature : p2PHKSignatures) {
                if (p2PHKSignature != null) {
                    stream.writeNulsData(p2PHKSignature);
                }
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int signCount = (int) byteBuffer.readVarInt();
        if (0 < signCount) {
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            for (int i = 0; i < signCount; i++) {
                p2PHKSignatures.add(byteBuffer.readNulsData(new P2PHKSignature()));
            }
            this.p2PHKSignatures = p2PHKSignatures;
        }
    }

    @Override
    public int size() {
        // 当前签名数据长度
        int size = SerializeUtils.sizeOfVarInt(p2PHKSignatures == null ? 0 : p2PHKSignatures.size());
        if (p2PHKSignatures != null && p2PHKSignatures.size() > 0) {
            for (P2PHKSignature p2PHKSignature : p2PHKSignatures) {
                if (p2PHKSignature != null) {
                    size += SerializeUtils.sizeOfNulsData(p2PHKSignature);
                }
            }
        }
        return size;
    }

    public List<P2PHKSignature> getP2PHKSignatures() {
        return p2PHKSignatures;
    }

    public void setP2PHKSignatures(List<P2PHKSignature> p2PHKSignatures) {
        this.p2PHKSignatures = p2PHKSignatures;
    }

    public int getSignersCount() {
        return p2PHKSignatures == null ? 0 : p2PHKSignatures.size();
    }
}
