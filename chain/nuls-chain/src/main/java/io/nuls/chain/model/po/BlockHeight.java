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
package io.nuls.chain.model.po;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lan
 * @description
 * @date 2019/02/21
 **/
@EqualsAndHashCode(callSuper = true)
@Data
public class BlockHeight extends BaseNulsData {
    private long blockHeight = 0;
    private boolean isCommit = false;
    private List<Long> bakHeighList = new ArrayList<>();

    public void addBakHeight(long height){
        bakHeighList.add(height);
    }
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(blockHeight);
        stream.writeBoolean(isCommit);
        stream.writeUint16(bakHeighList.size());
        for (Long height : bakHeighList) {
            stream.writeUint32(height);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.blockHeight = byteBuffer.readUint32();
        this.isCommit = byteBuffer.readBoolean();
        int totalSize = byteBuffer.readUint16();
        for (int i = 0; i < totalSize; i++) {
            bakHeighList.add(byteBuffer.readUint32());
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfBoolean();
        size += SerializeUtils.sizeOfUint16();
        size += (size * SerializeUtils.sizeOfUint32());
        return size;
    }

}
