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
package io.nuls.network.model;

import io.nuls.network.model.dto.Dto;
import io.nuls.network.model.po.BasePo;

/**
 * the bean for node peer and nodeGroup relation properties
 * @author lan
 * @date 2018/11/01
 *
 */
public class NodeGroupConnector implements Dto{
    private long magicNumber;
    private boolean isSeed=false;
    private long version=0;
    private long blockHeight=0;
    private String blockHash="";
    private volatile  int status=Node.WAIT;


    public NodeGroupConnector(long magicNumber){
        this.magicNumber=magicNumber;
    }
    public NodeGroupConnector(long magicNumber,boolean isSeed,int status){
        this.magicNumber=magicNumber;
        this.isSeed=isSeed;
        this.status=status;
    }
    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public boolean isSeed() {
        return isSeed;
    }

    public void setSeed(boolean seed) {
        isSeed = seed;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
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

    @Override
    public BasePo parseToPo() {
        return null;
    }
}
