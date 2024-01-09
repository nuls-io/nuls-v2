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
import io.nuls.base.data.Address;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 黄牌惩罚信息类
 * Yellow Card Punishment Information Category
 *
 * @author tag
 * 2018/11/20
 */
public class YellowPunishData extends BaseNulsData {
    private List<byte[]> addressList = new ArrayList<>();

    public YellowPunishData() {
    }

    public List<byte[]> getAddressList() {
        return addressList;
    }

    public void setAddressList(List<byte[]> addressList) {
        this.addressList = addressList;
    }

    public Set<byte[]> getAddresses() {
        return new HashSet<>(addressList);
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(addressList.size());
        for (byte[] address : addressList) {
            stream.write(address);
        }

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int count = (int) byteBuffer.readVarInt();
        addressList.clear();
        for (int i = 0; i < count; i++) {
            addressList.add(byteBuffer.readBytes(Address.ADDRESS_LENGTH));
        }

    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfVarInt(addressList.size());
        for (byte[] address : addressList) {
            size += Address.ADDRESS_LENGTH;
        }
        return size;
    }
}
