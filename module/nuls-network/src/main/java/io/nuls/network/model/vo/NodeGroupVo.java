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

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;

/**
 * @description: nodeGroup vo on rpc
 * @author: lan
 * @create: 2018/11/09
 **/
@ApiModel(name = "网络组详情")
public class NodeGroupVo implements Ivo {
    @ApiModelProperty(description = "链ID")
    private int chainId;
    @ApiModelProperty(description = "网络魔法参数")
    private long magicNumber;
    @ApiModelProperty(description = "总连接数")
    private int totalCount;
    @ApiModelProperty(description = "本地网络已连接节点数")
    private int connectCount;
    @ApiModelProperty(description = "本地网络待接节点数")
    private int disConnectCount;
    @ApiModelProperty(description = "本地网络入网连接节点数")
    private int inCount;
    @ApiModelProperty(description = "本地网络出网连接节点数")
    private int outCount;
    @ApiModelProperty(description = "跨链网络连接节点数")
    private int connectCrossCount;
    @ApiModelProperty(description = "跨链网络待接节点数")
    private int disConnectCrossCount;
    @ApiModelProperty(description = "跨链网络入网节点数")
    private int inCrossCount;
    @ApiModelProperty(description = "跨链网络出网节点数")
    private int outCrossCount;
    @ApiModelProperty(description = "本地网络是否已工作")
    private int isActive;
    @ApiModelProperty(description = "跨链网络是否已工作")
    private int isCrossActive;
    @ApiModelProperty(description = "网络组是否是卫星链节点")
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
