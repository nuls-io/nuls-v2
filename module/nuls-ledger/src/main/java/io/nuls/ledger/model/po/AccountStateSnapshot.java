/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */
public class AccountStateSnapshot extends BaseNulsData {
    private BakAccountState bakAccountState;
    private List<AmountNonce> nonces = new ArrayList<>();

    public AccountStateSnapshot() {
        super();
    }

    public AccountStateSnapshot(BakAccountState bakAccountState, List<AmountNonce> nonces) {
        this.bakAccountState = bakAccountState;
        this.nonces = nonces;
    }



    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(bakAccountState);
        stream.writeUint16(nonces.size());
        for (AmountNonce nonce : nonces) {
            stream.writeNulsData(nonce);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.bakAccountState = byteBuffer.readNulsData(new BakAccountState());
        int nonceCount = byteBuffer.readUint16();
        for (int i = 0; i < nonceCount; i++) {
            AmountNonce amountNonce = new AmountNonce();
            byteBuffer.readNulsData(amountNonce);
            this.nonces.add(amountNonce);
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfNulsData(bakAccountState);
        size += SerializeUtils.sizeOfUint16();
        for (AmountNonce nonce : nonces) {
            size += SerializeUtils.sizeOfNulsData(nonce);
        }
        return size;
    }

    public BakAccountState getBakAccountState() {
        return bakAccountState;
    }

    public void setBakAccountState(BakAccountState bakAccountState) {
        this.bakAccountState = bakAccountState;
    }

    public List<AmountNonce> getNonces() {
        return nonces;
    }

    public void setNonces(List<AmountNonce> nonces) {
        this.nonces = nonces;
    }
}
