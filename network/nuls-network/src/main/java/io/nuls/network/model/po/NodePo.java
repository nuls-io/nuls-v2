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
import io.nuls.network.model.Node;
import io.nuls.network.model.dto.Dto;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * @program: nuls2.0
 * @description: node po
 * @author: lan
 * @create: 2018/11/08
 **/
public class NodePo extends  BasePo{
    private long magicNumber;
    private String id;

    private String ip;

    private int port = 0;
    /**
     * 非跨链连接中存跨链port，用于跨链连接的地址回复
     */
    private int crossPort = 0;

    /**
     * 是否跨链连接
     */
    private boolean isCrossConnect=false;

    public NodePo(){
        super();
    }
    public NodePo(long magicNumber,String id,String ip,int port,int crossPort,boolean isCrossConnect){
        this.magicNumber = magicNumber;
        this.ip=ip;
        this.id=id;
        this.port=port;
        this.crossPort=crossPort;
        this.isCrossConnect=isCrossConnect;
    }
    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint32(magicNumber);
        stream.writeString(id);
        stream.writeString(ip);
        stream.writeUint16(port);
        stream.writeUint16(crossPort);
        stream.writeBoolean(isCrossConnect);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.magicNumber =byteBuffer.readUint32();
        this.id = byteBuffer.readString();
        this.ip = byteBuffer.readString();
        this.port = byteBuffer.readUint16();
        this.crossPort = byteBuffer.readUint16();
        this.isCrossConnect = byteBuffer.readBoolean();
    }

    @Override
    public int size() {
        int size=0;
        size += SerializeUtils.sizeOfUint32();
        size += SerializeUtils.sizeOfString(id);
        size += SerializeUtils.sizeOfString(ip);
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfBoolean();
        return size;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public boolean isCrossConnect() {
        return isCrossConnect;
    }

    public void setCrossConnect(boolean crossConnect) {
        isCrossConnect = crossConnect;
    }

    public int getCrossPort() {
        return crossPort;
    }

    public void setCrossPort(int crossPort) {
        this.crossPort = crossPort;
    }

    @Override
    public Dto parseDto() {
        Node   node = new Node(magicNumber,ip, port, Node.OUT, isCrossConnect);
        node.setRemoteCrossPort(crossPort);
        return node;
    }

}
