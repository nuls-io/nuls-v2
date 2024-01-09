/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.core.constant;

import io.nuls.core.parse.I18nUtils;

/**
 * All management tools for system return codes, and all constants should be implemented using this class. This class integrates internationalization operations, and all information that requires internationalization should use this class
 * All of the system's return code management tools, all of the constants should be implemented using this class.
 * This class integrates internationalization operations, and all information that needs internationalization should be used.
 *
 * @author: Niels Wang
 */
public class ErrorCode {
    /**
     * Internationalization encoding of message content
     * Internationalized encoding of message content.
     */
    private String msg;

    /**
     * Return code, used to mark unique results
     * The return code is used to mark the unique result.
     */
    private String code;

    public ErrorCode() {

    }

    protected ErrorCode(String code) {
        this.code = code;
        this.msg = code;
        if (null == code) {
            throw new RuntimeException("the errorcode code cann't be null!");
        }
    }

    /**
     * According to the system language settings, return the string corresponding to the internationalization code
     * According to the system language Settings, return the string corresponding to the internationalization encoding.
     *
     * @return String
     */
    public String getMsg() {
        return I18nUtils.get(msg);
    }

    public String getCode() {
        return code;
    }

    public static final ErrorCode init(String code) {
        return new ErrorCode(code);
    }

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }
        if (!(obj instanceof ErrorCode)) {
            return false;
        }
        return code.equals(((ErrorCode) obj).getCode());
    }
}
