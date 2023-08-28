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

package io.nuls.consensus.model.bo.tx.txdata;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsHash;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * 停止节点信息类
 * Stop Node Information Class
 *
 * @author tag
 * 2018/11/20
 */
public class DelayStopAgent extends BaseNulsData {

    private NulsHash agentHash;

    private long height;

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(this.agentHash.getBytes());
        stream.writeUint32(height);

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.agentHash = byteBuffer.readHash();
        this.height = byteBuffer.readUint32();
    }

    @Override
    public int size() {
        return this.agentHash.getBytes().length + SerializeUtils.sizeOfUint32();
    }

    public NulsHash getAgentHash() {
        return agentHash;
    }

    public void setAgentHash(NulsHash agentHash) {
        this.agentHash = agentHash;
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }
}
