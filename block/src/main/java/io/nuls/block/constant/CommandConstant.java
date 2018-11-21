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
 * 存储对外提供的接口命令
 * @author captain
 * @date 18-11-9 下午2:15
 * @version 1.0
 */
public interface CommandConstant {

    String COMPLETE_MESSAGE = "bl_Complete";
    String BLOCK_MESSAGE = "bl_Block";
    String GET_BLOCK_MESSAGE = "bl_GetBlock";
    String FORWARD_SMALL_BLOCK_MESSAGE = "bl_Forward";
    String GET_BLOCKS_BY_HEIGHT_MESSAGE = "bl_GetBlocks";
    String GET_TXGROUP_MESSAGE = "bl_GetTxs";
    String SMALL_BLOCK_MESSAGE = "bl_sBlock";
    String GET_SMALL_BLOCK_MESSAGE = "bl_GetsBlock";
    String TXGROUP_MESSAGE = "bl_Txs";
}
