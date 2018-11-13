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
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.Dto;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;

/**
 * @program: nuls2
 * @description: db bean
 * @author: lan
 * @create: 2018/11/07
 **/
public class NodeGroupPo extends BasePo {
    private long magicNumber = 0;
    private int chainId = 0;
    private int maxOut = 0;
    private int maxIn = 0;
    /**
     * 友链跨链最大连接数
     */
    private int maxCrossOut = 0;
    private int maxCrossIn = 0;

    private int minAvailableCount = 0;
    /**
     * 跨链网络是否激活,卫星链上的默认跨链true,
     * 友链默认false，在跨链模块请求时候这个属性才为true
     */
    private boolean isCrossActive=false;

    /**
     * 卫星链注册的跨链Group时为false
     */
    private boolean isSelf=true;
    /**
     * 是否卫星网络,只要卫星链上的节点就是true，如果是友链节点为false
     */
    private boolean isMoonNet=false;
    @Override
    public int size() {
        int size=0;
        size += SerializeUtils.sizeOfInt64();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfBoolean();
        size += SerializeUtils.sizeOfBoolean();
        size += SerializeUtils.sizeOfBoolean();
        return size;
    }



    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeInt64(magicNumber);
        stream.writeUint32(chainId);
        stream.writeUint32(maxOut);
        stream.writeUint32(maxIn);
        stream.writeUint32(maxCrossOut);
        stream.writeUint32(maxCrossIn);
        stream.writeUint32(minAvailableCount);
        stream.writeBoolean(isCrossActive);
        stream.writeBoolean(isSelf);
        stream.writeBoolean(isMoonNet);
    }


    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
       this.magicNumber = byteBuffer.readInt64();
       this.chainId = byteBuffer.readInt32();
       this.maxOut = byteBuffer.readInt32();
       this.maxIn = byteBuffer.readInt32();
       this.maxCrossOut = byteBuffer.readInt32();
       this.maxCrossIn = byteBuffer.readInt32();
       this.minAvailableCount = byteBuffer.readInt32();
       this.isCrossActive = byteBuffer.readBoolean();
       this.isSelf = byteBuffer.readBoolean();
       this.isMoonNet = byteBuffer.readBoolean();

    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getMaxOut() {
        return maxOut;
    }

    public void setMaxOut(int maxOut) {
        this.maxOut = maxOut;
    }

    public int getMaxIn() {
        return maxIn;
    }

    public void setMaxIn(int maxIn) {
        this.maxIn = maxIn;
    }

    public int getMaxCrossOut() {
        return maxCrossOut;
    }

    public void setMaxCrossOut(int maxCrossOut) {
        this.maxCrossOut = maxCrossOut;
    }

    public int getMaxCrossIn() {
        return maxCrossIn;
    }

    public void setMaxCrossIn(int maxCrossIn) {
        this.maxCrossIn = maxCrossIn;
    }

    public int getMinAvailableCount() {
        return minAvailableCount;
    }

    public void setMinAvailableCount(int minAvailableCount) {
        this.minAvailableCount = minAvailableCount;
    }

    public boolean isCrossActive() {
        return isCrossActive;
    }

    public void setCrossActive(boolean crossActive) {
        isCrossActive = crossActive;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }

    public boolean isMoonNet() {
        return isMoonNet;
    }

    public void setMoonNet(boolean moonNet) {
        isMoonNet = moonNet;
    }

    @Override
    public Dto parseDto() {
        NodeGroup nodeGroup=new NodeGroup(magicNumber,chainId,maxIn,maxOut,minAvailableCount,isMoonNet);
        return nodeGroup;
    }
}
