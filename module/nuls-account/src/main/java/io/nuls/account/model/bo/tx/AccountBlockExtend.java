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

package io.nuls.account.model.bo.tx;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * @author: PierreLuo
 * @date: 2022/1/21
 */
public class AccountBlockExtend extends BaseNulsData {

    private AccountBlockInfo[] infos;

    private byte[] extend;

    public AccountBlockExtend() {
    }

    @Override
    public int size() {
        int size = 0;
        // length
        size += SerializeUtils.sizeOfUint16();
        for (AccountBlockInfo info : infos) {
            size += SerializeUtils.sizeOfNulsData(info);
        }
        size += SerializeUtils.sizeOfBytes(extend);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(infos.length);
        for (AccountBlockInfo info : infos) {
            stream.writeNulsData(info);
        }
        stream.writeBytesWithLength(extend);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int length = byteBuffer.readUint16();
        AccountBlockInfo[] _infos = new AccountBlockInfo[length];
        for (int i = 0; i < length; i++) {
            _infos[i] = byteBuffer.readNulsData(new AccountBlockInfo());
        }
        this.infos = _infos;
        this.extend = byteBuffer.readByLengthByte();
    }

    public AccountBlockInfo[] getInfos() {
        return infos;
    }

    public void setInfos(AccountBlockInfo[] infos) {
        this.infos = infos;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }
}
