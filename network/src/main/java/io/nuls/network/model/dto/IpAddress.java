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
package io.nuls.network.model.dto;


import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
/**
 * ip&port
 * @author lan
 * @date 2018/11/01
 *
 */
public class IpAddress extends BaseNulsData {

    private static final int IPSIZE=16;
    private InetAddress ip;
    private int port;
    public IpAddress(){
        super();
    }


    public IpAddress(String ipStr,int port){
        try {
            this.ip=InetAddress.getByName(ipStr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        this.port=port;
    }


    public IpAddress(InetAddress ip,int port){
        this.ip=ip;
        this.port=port;
    }

    public void setIpStr(String ipStr){
        try {
            this.ip=InetAddress.getByName(ipStr);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
    public InetAddress getIp() {
        return ip;
    }

    public void setIp(InetAddress ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

        @Override
        public int size() {
            int s = 0;
            s += IPSIZE; // ip 16byte
            s += SerializeUtils.sizeOfUint16(); // port 2byte
            return s;
        }


    @Override
    public void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        byte[] ipBytes = ip.getAddress();
        if (ipBytes.length == SerializeUtils.sizeOfInt32()) {
            byte[] v6addr = new byte[IPSIZE];
            System.arraycopy(ipBytes, 0, v6addr, 12, 4);
            v6addr[10] = (byte) 0xFF;
            v6addr[11] = (byte) 0xFF;
            ipBytes = v6addr;
        }
        stream.write(ipBytes);
        stream.writeUint16(port);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        try {
            byte[] ipAddrBytes =byteBuffer.readBytes(IPSIZE);
            ip = InetAddress.getByAddress(ipAddrBytes);
            port=byteBuffer.readUint16();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);  // Cannot happen.
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
