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

package io.nuls.provider.model.jsonrpc;

/**
 * @author Niels
 */
public enum RpcErrorCode {
    // Wrong parameters
    PARAMS_ERROR("1000", "Parameters is wrong!"),

    // Contract not verified
    CONTRACT_NOT_VALIDATION_ERROR("100", "Contract code not certified!"),

    // Contract verified
    CONTRACT_VALIDATION_ERROR("101", "The contract code has been certified!"),

    // Contract verification failed
    CONTRACT_VALIDATION_FAILED("102", "Contract verification failed."),

    //Data not found
    DATA_NOT_EXISTS("404", "Data not found!"),

    //Transaction parsing error
    TX_PARSE_ERROR("999", "Transaction parse error!"),

    //Script execution error
    TX_SHELL_ERROR("755", "Shell execute error!"),

    //Unknown system error
    SYS_UNKNOWN_EXCEPTION("10002", "System unknown error!");

    private String code;

    private String message;

    RpcErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }}
