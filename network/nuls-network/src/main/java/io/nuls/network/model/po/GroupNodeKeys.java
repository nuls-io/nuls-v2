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
package io.nuls.network.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.network.model.dto.Dto;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @program: nuls2
 * @description: key chainId, value nodeId list
 * @author: lan
 * @create: 2018/11/08
 **/
public class GroupNodeKeys extends BasePo{
    private int chainId=0;
    private List<String> nodeKeys=new ArrayList<>();

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(chainId);
        int nodeKeysSize = (nodeKeys == null ? 0 : nodeKeys.size());
        stream.writeVarInt(nodeKeysSize);
        if (null != nodeKeys) {
            for (String nodeKey : nodeKeys) {
                stream.writeString(nodeKey);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readInt32();
        int nodeKeysSize = (int) byteBuffer.readVarInt();
        if (0 < nodeKeysSize) {
            for (int i = 0; i < nodeKeysSize; i++) {
                String nodeKey=byteBuffer.readString();
                nodeKeys.add(nodeKey);
            }
        }
    }

    @Override
    public int size() {
        int size=0;
        size += SerializeUtils.sizeOfUint32();
        size+= SerializeUtils.sizeOfVarInt(nodeKeys == null ? 0 : nodeKeys.size());
        if (null != nodeKeys) {
            for (String nodeKey : nodeKeys) {
                size += SerializeUtils.sizeOfString(nodeKey);
            }
        }
        return size;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public List<String> getNodeKeys() {
        return nodeKeys;
    }

    public void setNodeKeys(List<String> nodeKeys) {
        this.nodeKeys = nodeKeys;
    }

    @Override
    public Dto parseDto() {
        return null;
    }
}
