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
package io.nuls.transaction.model.bo.config;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

/**
 * Transaction module chain setting
 * @author: Charlie
 * @date: 2019/03/14
 */

public class ConfigBean extends BaseNulsData {

    /** chain id*/
    private int chainId;
    /** assets id*/
    private int assetId;
    /** 单个交易数据最大值(B)*/
    private long txMaxSize;
    /**
     * 打包时在获取交易之后留给模块统一验证的时间阈值,
     * 包括统一验证有被过滤掉的交易时需要重新验证等.
     */
    private int moduleVerifyPercent;
    /** 打包获取交易给RPC传输到共识的预留时间,超时则需要处理交易还原待打包队列*/
    private int packageRpcReserveTime;
    /** 接收新交易的文件队列最大容量**/
    private long txUnverifiedQueueSize;
    /** 孤儿交易生命时间,超过会被清理**/
    private int orphanTtl;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        stream.writeUint16(assetId);
        stream.writeUint32(txMaxSize);
        stream.writeUint16(moduleVerifyPercent);
        stream.writeUint16(packageRpcReserveTime);
        stream.writeUint32(txUnverifiedQueueSize);
        stream.writeUint16(orphanTtl);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        this.assetId = byteBuffer.readUint16();
        this.txMaxSize = byteBuffer.readUint32();
        this.moduleVerifyPercent = byteBuffer.readUint16();
        this.packageRpcReserveTime = byteBuffer.readUint16();
        this.txUnverifiedQueueSize = byteBuffer.readUint32();
        this.orphanTtl = byteBuffer.readUint16();
    }

    @Override
    public int size() {
        int size = 5 * SerializeUtils.sizeOfUint16();
        size += 2 * SerializeUtils.sizeOfUint32();
        return  size;
    }

    public ConfigBean() {
    }

    public ConfigBean(int chainId, int assetId, int txMaxSize, int moduleVerifyPercent, int packageRpcReserveTime, int txUnverifiedQueueSize, int orphanTtl) {
        this.chainId = chainId;
        this.assetId = assetId;
        this.txMaxSize = txMaxSize;
        this.moduleVerifyPercent = moduleVerifyPercent;
        this.packageRpcReserveTime = packageRpcReserveTime;
        this.txUnverifiedQueueSize = txUnverifiedQueueSize;
        this.orphanTtl = orphanTtl;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public long getTxMaxSize() {
        return txMaxSize;
    }

    public void setTxMaxSize(long txMaxSize) {
        this.txMaxSize = txMaxSize;
    }

    public int getModuleVerifyPercent() {
        return moduleVerifyPercent;
    }

    public void setModuleVerifyPercent(int moduleVerifyPercent) {
        this.moduleVerifyPercent = moduleVerifyPercent;
    }

    public int getPackageRpcReserveTime() {
        return packageRpcReserveTime;
    }

    public void setPackageRpcReserveTime(int packageRpcReserveTime) {
        this.packageRpcReserveTime = packageRpcReserveTime;
    }

    public long getTxUnverifiedQueueSize() {
        return txUnverifiedQueueSize;
    }

    public void setTxUnverifiedQueueSize(long txUnverifiedQueueSize) {
        this.txUnverifiedQueueSize = txUnverifiedQueueSize;
    }

    public int getOrphanTtl() {
        return orphanTtl;
    }

    public void setOrphanTtl(int orphanTtl) {
        this.orphanTtl = orphanTtl;
    }

}
