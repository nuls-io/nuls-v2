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
import io.nuls.network.model.dto.IpAddress;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * Version protocol message body
 */
public class VersionMessageBody extends BaseNulsData {

    private long protocolVersion;
    private IpAddress addrYou = new IpAddress();
    private int portYouCross;
    private IpAddress addrMe = new IpAddress();
    private int portMeCross;
    private long blockHeight;
    private String blockHash = "";
    private String extend = "";

    public VersionMessageBody() {

    }


    @Override
    public int size() {
        int s = 0;
        s += SerializeUtils.sizeOfUint32(); // protocolVersion
        s += addrYou.size(); // addrYou 18byte
        s += SerializeUtils.sizeOfUint16(); // addrYou  cross port 2byte
        s += addrMe.size(); // addrMe  16byte
        s += SerializeUtils.sizeOfUint16(); // addrMe cross port 2byte
        s += SerializeUtils.sizeOfUint32(); // blockHeight
        s += SerializeUtils.sizeOfString(blockHash); // blockHash
        s += SerializeUtils.sizeOfString(extend); // extend
        return s;
    }

    /**
     * serialize important field
     */
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(protocolVersion);
        addrYou.serializeToStream(stream);
        stream.writeUint16(portYouCross);
        addrMe.serializeToStream(stream);
        stream.writeUint16(portMeCross);
        stream.writeUint32(blockHeight);
        stream.writeString(blockHash);
        stream.writeString(extend);
    }

    @Override
    public void parse(NulsByteBuffer buffer) throws NulsException {
        try {
            protocolVersion = buffer.readUint32();
            addrYou.parse(buffer);
            portYouCross = buffer.readUint16();
            addrMe.parse(buffer);
            portMeCross = buffer.readUint16();
            blockHeight = buffer.readUint32();
            blockHash = buffer.readString();
            extend = buffer.readString();
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    public long getProtocolVersion() {
        return protocolVersion;
    }

    public void setProtocolVersion(long protocolVersion) {
        this.protocolVersion = protocolVersion;
    }


    public String getExtend() {
        return extend;
    }

    public void setExtend(String extend) {
        this.extend = extend;
    }


    public int getPortYouCross() {
        return portYouCross;
    }

    public void setPortYouCross(int portYouCross) {
        this.portYouCross = portYouCross;
    }


    public int getPortMeCross() {
        return portMeCross;
    }

    public void setPortMeCross(int portMeCross) {
        this.portMeCross = portMeCross;
    }

    public IpAddress getAddrYou() {
        return addrYou;
    }

    public void setAddrYou(IpAddress addrYou) {
        this.addrYou = addrYou;
    }

    public IpAddress getAddrMe() {
        return addrMe;
    }

    public void setAddrMe(IpAddress addrMe) {
        this.addrMe = addrMe;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }
}
