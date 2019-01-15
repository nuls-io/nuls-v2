/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
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
     * waiting
     */
    MASTER_APPEND,

    /**
     * 与主链分叉
     * running
     */
    MASTER_FORK,

    /**
     * 与分叉链相连
     * Running exception
     */
    FORK_APPEND,

    /**
     * 与分叉链分叉
     * Running exception
     */
    FORK_FORK,

    /**
     * 与孤儿链相连
     * Running exception
     */
    ORPHAN_APPEND,

    /**
     * 与孤儿链分叉
     * Running exception
     */
    ORPHAN_FORK,

    /**
     * 孤儿链
     * Running exception
     */
    ORPHAN,

    /**
     * 分叉链
     * Running exception
     */
    FORK,

    /**
     * 主链
     * Running exception
     */
    MASTER;

    @Override
    public String toString() {
        return name();
    }
}
