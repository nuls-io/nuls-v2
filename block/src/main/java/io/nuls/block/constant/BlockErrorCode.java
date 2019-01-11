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


import io.nuls.tools.constant.ErrorCode;

/**
 * todo 错误码细化
 * 区块管理模块的错误信息表
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:01
 */
public interface BlockErrorCode {
    ErrorCode SUCCESS = ErrorCode.init("0");
    ErrorCode PARAMETER_ERROR = ErrorCode.init("10001");
    ErrorCode SERIALIZE_ERROR = ErrorCode.init("10002");
    ErrorCode DATA_ERROR = ErrorCode.init("10003");
    /**
     * 链合并失败
     */
    ErrorCode CHAIN_MERGE_ERROR = ErrorCode.init("20001");
    ErrorCode DATA_PARSE_ERROR = ErrorCode.init("20002");
    ErrorCode UNDEFINED_ERROR = ErrorCode.init("20003");


    /**
     * 收到的区块超出范围
     */
    ErrorCode OUT_OF_RANGE = ErrorCode.init("30001");
    /**
     * 收到主链上重复的区块
     */
    ErrorCode DUPLICATE_MAIN_BLOCK = ErrorCode.init("30002");
    /**
     * 收到分叉链上重复的区块
     */
    ErrorCode DUPLICATE_FORK_BLOCK = ErrorCode.init("30003");
    /**
     * 收到孤儿链上重复的区块
     */
    ErrorCode DUPLICATE_ORPHAN_BLOCK = ErrorCode.init("30004");
    /**
     * 收到孤儿区块
     */
    ErrorCode ORPHAN_BLOCK = ErrorCode.init("30005");
    /**
     * 收到分叉区块
     */
    ErrorCode FORK_BLOCK = ErrorCode.init("30006");
    /**
     * 区块与链没有关系(不重复　不分叉　不相连)
     */
    ErrorCode IRRELEVANT_BLOCK = ErrorCode.init("30007");
    /**
     * 链回滚失败
     */
    ErrorCode ROLLBACK_CHAIN_ERROR = ErrorCode.init("30008");
}