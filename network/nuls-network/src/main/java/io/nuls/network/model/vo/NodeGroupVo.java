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
package io.nuls.network.model.vo;

/**
 * @program: nuls2
 * @description: nodeGroup vo on rpc
 * @author: lan
 * @create: 2018/11/09
 **/
public class NodeGroupVo  implements  Ivo{
    private int chainId;
    private long magicNumber;
    private int totalCount;
    private int connectCount;
    private int disConnectCount;
    private int inCount;
    private int outCount;
    private int connectCrossCount;
    private int disConnectCrossCount;
    private int inCrossCount;
    private int outCrossCount;

    private long blockHeight;
    private String blockHash;
    private int isActive;
    private int isCrossActive;
    private int isMoonNet;

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getInCount() {
        return inCount;
    }

    public void setInCount(int inCount) {
        this.inCount = inCount;
    }

    public int getOutCount() {
        return outCount;
    }

    public void setOutCount(int outCount) {
        this.outCount = outCount;
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

    public int getIsActive() {
        return isActive;
    }

    public void setIsActive(int isActive) {
        this.isActive = isActive;
    }

    public int getIsCrossActive() {
        return isCrossActive;
    }

    public void setIsCrossActive(int isCrossActive) {
        this.isCrossActive = isCrossActive;
    }

    public int getIsMoonNet() {
        return isMoonNet;
    }

    public void setIsMoonNet(int isMoonNet) {
        this.isMoonNet = isMoonNet;
    }

    public int getInCrossCount() {
        return inCrossCount;
    }

    public void setInCrossCount(int inCrossCount) {
        this.inCrossCount = inCrossCount;
    }

    public int getOutCrossCount() {
        return outCrossCount;
    }

    public void setOutCrossCount(int outCrossCount) {
        this.outCrossCount = outCrossCount;
    }

    public int getConnectCount() {
        return connectCount;
    }

    public void setConnectCount(int connectCount) {
        this.connectCount = connectCount;
    }

    public int getDisConnectCount() {
        return disConnectCount;
    }

    public void setDisConnectCount(int disConnectCount) {
        this.disConnectCount = disConnectCount;
    }

    public int getConnectCrossCount() {
        return connectCrossCount;
    }

    public void setConnectCrossCount(int connectCrossCount) {
        this.connectCrossCount = connectCrossCount;
    }

    public int getDisConnectCrossCount() {
        return disConnectCrossCount;
    }

    public void setDisConnectCrossCount(int disConnectCrossCount) {
        this.disConnectCrossCount = disConnectCrossCount;
    }
}
