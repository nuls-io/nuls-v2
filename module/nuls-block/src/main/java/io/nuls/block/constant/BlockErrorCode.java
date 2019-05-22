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


import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.rpc.model.ModuleE;

/**
 * 区块管理模块的错误信息表
 *
 * @author captain
 * @version 1.0
 * @date 18-11-20 上午11:01
 */
public interface BlockErrorCode extends CommonCodeConstanst {

    ErrorCode CHAIN_SWITCH_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0001");
    ErrorCode GENESIS_DIFF = ErrorCode.init(ModuleE.BL.getPrefix() + "_0002");
    ErrorCode INIT_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0003");
    ErrorCode SAVE_GENESIS_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0004");
    ErrorCode UPDATE_HEIGHT_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0005");
    ErrorCode HEADER_REMOVE_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0006");
    ErrorCode TX_ROLLBACK_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0007");
    ErrorCode CS_ROLLBACK_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0008");
    ErrorCode PU_SAVE_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0009");
    ErrorCode CS_SAVE_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0010");
    ErrorCode TX_SAVE_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0011");
    ErrorCode HEADER_SAVE_ERROR = ErrorCode.init(ModuleE.BL.getPrefix() + "_0012");
    ErrorCode OUT_OF_RANGE = ErrorCode.init(ModuleE.BL.getPrefix() + "_0013");
    ErrorCode DUPLICATE_MAIN_BLOCK = ErrorCode.init(ModuleE.BL.getPrefix() + "_0014");
    ErrorCode FORK_BLOCK = ErrorCode.init(ModuleE.BL.getPrefix() + "_0015");
    ErrorCode IRRELEVANT_BLOCK = ErrorCode.init(ModuleE.BL.getPrefix() + "_0016");


}