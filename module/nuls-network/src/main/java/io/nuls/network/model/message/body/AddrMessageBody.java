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
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.constant.ToolsConstant;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.network.model.dto.IpAddressShare;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * peer地址协议消息体
 * addr protocol message body
 *
 * @author lan
 * @date 2018/11/01
 */
public class AddrMessageBody extends BaseNulsData {
    private int chainId = 0;
    private byte isCross = 0;
    private List<IpAddressShare> ipAddressList = new ArrayList<>();


    public AddrMessageBody() {

    }

    public void addAddr(IpAddressShare addr) {
        ipAddressList.add(addr);
    }

    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint16();
        s += 1;
        if (ipAddressList.size() > 0) {
            s += ipAddressList.size() * (new IpAddressShare().size());
        } else {
            s = 4;
        }
        return s;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeByte(isCross);
        if (0 == ipAddressList.size()) {
            stream.write(ToolsConstant.PLACE_HOLDER);
        } else {
            for (IpAddressShare address : ipAddressList) {
                address.serializeToStream(stream);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        try {
            chainId = buffer.readUint16();
            isCross = buffer.readByte();
            while (!buffer.isFinished()) {
                IpAddressShare address = new IpAddressShare();
                address.parse(buffer);
                ipAddressList.add(address);
            }
        } catch (Exception e) {
            //如果需要抛出异常则不需要打印日志，由异常捕获者打印日常
            throw new NulsException(e);
        }
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public byte getIsCross() {
        return isCross;
    }

    public void setIsCross(byte isCross) {
        this.isCross = isCross;
    }

    public List<IpAddressShare> getIpAddressList() {
        return ipAddressList;
    }

    public void setIpAddressList(List<IpAddressShare> ipAddressList) {
        this.ipAddressList = ipAddressList;
    }
}
