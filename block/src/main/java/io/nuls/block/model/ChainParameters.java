/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2018 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.block.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChainParameters extends BaseNulsData {

    /**
     * 链名
     */
    private String CHAIN_NAME;
    /**
     * 链ID
     */
    private int CHAIN_ID;
    /**
     * 区块大小阈值
     */
    private int BLOCK_MAX_SIZE;
    /**
     * 网络重置阈值
     */
    private int RESET_TIME;
    /**
     * 分叉链比主链高几个区块就进行链切换
     */
    private int CHAIN_SWTICH_THRESHOLD;
    /**
     * 分叉链、孤儿链区块最大缓存数量
     */
    private int CACHE_SIZE;
    /**
     * 接收新区块的范围
     */
    private int HEIGHT_RANGE;
    /**
     * 每次回滚区块最大值
     */
    private int MAX_ROLLBACK;
    /**
     * 一致节点比例
     */
    private int CONSISTENCY_NODE_PERCENT;
    /**
     * 系统运行最小节点数
     */
    private int MIN_NODE_AMOUNT;
    /**
     * 每次从一个节点下载多少区块
     */
    private int DOWNLOAD_NUMBER;
    /**
     * 区块头中扩展字段的最大长度
     */
    private int EXTEND_MAX_SIZE;
    /**
     * 为阻止恶意节点提前出块,设置此参数
     * 区块时间戳大于当前时间多少就丢弃该区块
     */
    private int VALID_BLOCK_INTERVAL;
    /**
     * 同步区块时最多缓存多少个区块
     */
    private int BLOCK_CACHE;
    /**
     * 系统正常运行时最多缓存多少个从别的节点接收到的小区块
     */
    private int SMALL_BLOCK_CACHE;
    /**
     * 孤儿链最大年龄
     */
    private int ORPHAN_CHAIN_MAX_AGE;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(CHAIN_NAME);
        stream.writeUint16(CHAIN_ID);
        stream.writeUint16(BLOCK_MAX_SIZE);
        stream.writeUint16(RESET_TIME);
        stream.writeUint16(CHAIN_SWTICH_THRESHOLD);
        stream.writeUint16(CACHE_SIZE);
        stream.writeUint16(HEIGHT_RANGE);
        stream.writeUint16(MAX_ROLLBACK);
        stream.writeUint16(CONSISTENCY_NODE_PERCENT);
        stream.writeUint16(MIN_NODE_AMOUNT);
        stream.writeUint16(DOWNLOAD_NUMBER);
        stream.writeUint16(EXTEND_MAX_SIZE);
        stream.writeUint16(VALID_BLOCK_INTERVAL);
        stream.writeUint16(BLOCK_CACHE);
        stream.writeUint16(SMALL_BLOCK_CACHE);
        stream.writeUint16(ORPHAN_CHAIN_MAX_AGE);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.CHAIN_NAME = byteBuffer.readString();
        this.CHAIN_ID = byteBuffer.readUint16();
        this.BLOCK_MAX_SIZE = byteBuffer.readUint16();
        this.RESET_TIME = byteBuffer.readUint16();
        this.CHAIN_SWTICH_THRESHOLD = byteBuffer.readUint16();
        this.CACHE_SIZE = byteBuffer.readUint16();
        this.HEIGHT_RANGE = byteBuffer.readUint16();
        this.MAX_ROLLBACK = byteBuffer.readUint16();
        this.CONSISTENCY_NODE_PERCENT = byteBuffer.readUint16();
        this.MIN_NODE_AMOUNT = byteBuffer.readUint16();
        this.DOWNLOAD_NUMBER = byteBuffer.readUint16();
        this.EXTEND_MAX_SIZE = byteBuffer.readUint16();
        this.VALID_BLOCK_INTERVAL = byteBuffer.readUint16();
        this.BLOCK_CACHE = byteBuffer.readUint16();
        this.SMALL_BLOCK_CACHE = byteBuffer.readUint16();
    }

    @Override
    public int size() {
        int size = 0;
        size += (15 * SerializeUtils.sizeOfUint16());
        size += SerializeUtils.sizeOfString(CHAIN_NAME);
        return size;
    }
}
