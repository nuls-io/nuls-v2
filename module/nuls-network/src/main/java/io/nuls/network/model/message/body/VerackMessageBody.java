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

package io.nuls.network.model.message.body;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.tools.exception.NulsException;

import java.io.IOException;

/**
 * vrack protocol message body
 *
 * @author lan
 */
public class VerackMessageBody extends MessageBody {
    public static int VER_SUCCESS = 1;
    public static int VER_CONNECT_MAX = 2;
    public static int VER_FAIL = 100;

    int ackCode = VER_SUCCESS;

    public VerackMessageBody(int ackCode) {
        this.ackCode = ackCode;
    }

    public VerackMessageBody() {

    }

    @Override
    public int size() {
        return 1;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write((byte) ackCode);

    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        ackCode = buffer.readByte() & 0xFF;
        ;
    }

    public int getAckCode() {
        return ackCode;
    }

    public void setAckCode(int ackCode) {
        this.ackCode = ackCode;
    }
}
