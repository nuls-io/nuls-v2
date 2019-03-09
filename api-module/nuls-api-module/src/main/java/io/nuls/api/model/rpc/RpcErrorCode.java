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

package io.nuls.api.model.rpc;

/**
 * @author Niels
 */
public enum RpcErrorCode {
    // 参数不对
    PARAMS_ERROR(1000, "Parameters is wrong!"),

    // 合约未验证
    CONTRACT_NOT_VALIDATION_ERROR(100, "Contract code not certified!"),

    // 合约已验证
    CONTRACT_VALIDATION_ERROR(101, "The contract code has been certified!"),

    // 合约验证失败
    CONTRACT_VALIDATION_FAILED(102, "Contract verification failed."),

    //数据未找到
    DATA_NOT_EXISTS(404, "Data is not exists!"),

    //交易解析错误
    TX_PARSE_ERROR(999, "Transaction parse error!"),

    //脚本执行错误
    TX_SHELL_ERROR(755, "Shell execute error!"),

    //系统未知错误
    SYS_UNKNOWN_EXCEPTION(10002, "System unknown error!");
    private int code;

    private String message;

    RpcErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }}
