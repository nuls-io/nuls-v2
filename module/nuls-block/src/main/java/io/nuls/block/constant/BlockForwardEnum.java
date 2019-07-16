/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.constant;

/**
 * 标记区块广播、转发过程中的状态
 *
 * @author captain
 * @version 1.0
 * @date 18-11-28 下午12:01
 */
public enum BlockForwardEnum {

    /**
     * 数据错误,可能是恶意构造的数据
     * Data error, possibly maliciously constructed data
     */
    ERROR,

    /**
     * 没有收到区块
     * Block not received
     */
    EMPTY,

    /**
     * 收到部分区块,还缺失一部分交易
     * Received part of the block, still missing part of the transaction
     */
    INCOMPLETE,

    /**
     * 收到完整区块
     * Receive full block
     */
    COMPLETE;

    @Override
    public String toString() {
        return name();
    }
}
