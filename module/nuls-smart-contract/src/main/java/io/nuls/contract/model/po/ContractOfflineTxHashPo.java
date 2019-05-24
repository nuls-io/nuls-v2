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

package io.nuls.contract.model.po;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-05-24
 */
public class ContractOfflineTxHashPo extends BaseNulsData {


    private List<byte[]> hashList;

    public ContractOfflineTxHashPo() {
    }

    public ContractOfflineTxHashPo(List<byte[]> hashList) {
        this.hashList = hashList;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        for(byte[] hash : hashList) {
            stream.write(hash);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        hashList = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            hashList.add(byteBuffer.readBytes(NulsHash.HASH_LENGTH));
        }
    }

    @Override
    public int size() {
        return NulsHash.HASH_LENGTH * hashList.size();
    }

    public List<byte[]> getHashList() {
        return hashList;
    }

    public void setHashList(List<byte[]> hashList) {
        this.hashList = hashList;
    }
}
