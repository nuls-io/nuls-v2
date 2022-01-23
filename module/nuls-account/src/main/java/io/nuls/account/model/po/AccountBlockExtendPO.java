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

package io.nuls.account.model.po;


import io.nuls.account.model.bo.tx.AccountBlockInfo;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * @author: PierreLuo
 * @date: 2022/1/18
 */
public class AccountBlockExtendPO extends BaseNulsData {

    private byte[] address;

    private int[] types;

    private String[] contracts;

    private byte[] extend;

    public AccountBlockExtendPO() {
    }

    public AccountBlockExtendPO(byte[] address, AccountBlockInfo info) {
        this.address = address;
        this.types = info.getTypes();
        this.contracts = info.getContracts();
        this.extend = info.getExtend();
    }

    public AccountBlockExtendPO(byte[] address, int[] types, String[] contracts, byte[] extend) {
        this.address = address;
        this.types = types;
        this.contracts = contracts;
        this.extend = extend;
    }

    @Override
    public int size() {
        int size = 0;
        size += Address.ADDRESS_LENGTH;
        // length
        size += SerializeUtils.sizeOfUint16();
        if (types != null) {
            size += SerializeUtils.sizeOfUint16() * types.length;
        }
        size += SerializeUtils.sizeOfUint16();
        if (contracts != null) {
            for (String contract : contracts) {
                size += SerializeUtils.sizeOfString(contract);
            }
        }
        size += SerializeUtils.sizeOfBytes(extend);
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.write(address);
        if (types == null) {
            stream.writeUint16(0);
        } else {
            stream.writeUint16(types.length);
            for (int type : types) {
                stream.writeUint16(type);
            }
        }
        if (contracts == null) {
            stream.writeUint16(0);
        } else {
            stream.writeUint16(contracts.length);
            for (String address : contracts) {
                stream.writeString(address);
            }
        }
        stream.writeBytesWithLength(extend);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.address = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
        int length0 = byteBuffer.readUint16();
        int[] _types = new int[length0];
        for (int i = 0; i < length0; i++) {
            _types[i] = byteBuffer.readUint16();
        }
        int length = byteBuffer.readUint16();
        String[] _addresses = new String[length];
        for (int i = 0; i < length; i++) {
            _addresses[i] = byteBuffer.readString();
        }
        this.types = _types;
        this.contracts = _addresses;
        this.extend = byteBuffer.readByLengthByte();
    }

    public int[] getTypes() {
        return types;
    }

    public void setTypes(int[] types) {
        this.types = types;
    }

    public String[] getContracts() {
        return contracts;
    }

    public void setContracts(String[] contracts) {
        this.contracts = contracts;
    }

    public byte[] getAddress() {
        return address;
    }

    public void setAddress(byte[] address) {
        this.address = address;
    }

    public byte[] getExtend() {
        return extend;
    }

    public void setExtend(byte[] extend) {
        this.extend = extend;
    }
}
