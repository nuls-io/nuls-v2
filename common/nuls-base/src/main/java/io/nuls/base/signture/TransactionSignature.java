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


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TransactionSignature extends BaseNulsData {
    protected List<P2PHKSignature> p2PHKSignatures;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        // Old signature data written to stream
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
        // Read signature from stream,Compatible with both old and new versions
        int course = 0;
        List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            course = byteBuffer.getCursor();
            //Read two bytes（Script identifier bit）If both bytes are0x00Then it indicates that the subsequent data stream is script data
            byteBuffer.setCursor(course);
            p2PHKSignatures.add(byteBuffer.readNulsData(new P2PHKSignature()));
        }
        this.p2PHKSignatures = p2PHKSignatures;
    }

    @Override
    public int size() {
        // Current signature data length
        int size = 0;
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
