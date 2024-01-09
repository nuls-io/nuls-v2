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
 * 链的类别
 *
 * @author captain
 * @version 1.0
 * @date 18-11-28 下午12:01
 */
public enum ChainTypeEnum {

    /**
     * 与主链相连
     * Connected to the master chain
     */
    MASTER_APPEND,

    /**
     * 与主链分叉
     * Forked from the master chain
     */
    MASTER_FORK,

    /**
     * 与分叉链相连
     * Connected to a fork chain
     */
    FORK_APPEND,

    /**
     * 与分叉链分叉
     * Forked from a fork chain
     */
    FORK_FORK,

    /**
     * 与孤儿链相连
     * Connected to a orphan chain
     */
    ORPHAN_APPEND,

    /**
     * 与孤儿链分叉
     * Forked from a orphan chain
     */
    ORPHAN_FORK,

    /**
     * 与主链重复
     * duplicate chain
     */
    MASTER_DUPLICATE,

    /**
     * 与分叉链重复
     * duplicate chain
     */
    FORK_DUPLICATE,

    /**
     * 与孤儿链重复
     * duplicate chain
     */
    ORPHAN_DUPLICATE,

    /**
     * 部分重复
     */
    PARTIALLY_DUPLICATE,

    /**
     * 链上区块数据错误
     * orphan chain
     */
    DATA_ERROR,

    /**
     * 孤儿链
     * orphan chain
     */
    ORPHAN,

    /**
     * 分叉链
     * fork chain
     */
    FORK,

    /**
     * 主链
     * master chain
     */
    MASTER;

    @Override
    public String toString() {
        return name();
    }
}
