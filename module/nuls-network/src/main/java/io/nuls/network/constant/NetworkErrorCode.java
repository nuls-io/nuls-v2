/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.network.constant;


import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.ErrorCode;

/**
 * 错误码管理器
 * NetworkErrorCode
 *
 * @author Lan
 */
public interface NetworkErrorCode extends CommonCodeConstanst {
    ErrorCode NET_MESSAGE_ERROR = ErrorCode.init(ModuleE.NW.getPrefix() + "_0001");
    ErrorCode NET_MESSAGE_SEND_FAIL = ErrorCode.init(ModuleE.NW.getPrefix() + "_0002");
    ErrorCode NET_MESSAGE_SEND_EXCEPTION = ErrorCode.init(ModuleE.NW.getPrefix() + "_0003");
    ErrorCode NET_BROADCAST_FAIL = ErrorCode.init(ModuleE.NW.getPrefix() + "_0004");
    ErrorCode NET_NODE_DEAD = ErrorCode.init(ModuleE.NW.getPrefix() + "_0005");
    ErrorCode NET_NODE_MISS_CHANNEL = ErrorCode.init(ModuleE.NW.getPrefix() + "_0006");
}