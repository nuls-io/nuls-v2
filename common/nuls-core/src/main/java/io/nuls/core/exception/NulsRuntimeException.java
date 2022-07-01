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
package io.nuls.core.exception;

import io.nuls.core.constant.ErrorCode;
import io.nuls.core.model.StringUtils;

import java.text.MessageFormat;

/**
 * @author Niels
 */
public class NulsRuntimeException extends RuntimeException {

    private String code;
    private String message;
    private ErrorCode errorCode;

    /**
     * Constructs a new exception with the specified detail validator.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param errorCode the detail validator. The detail validator is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     */
    public NulsRuntimeException(ErrorCode errorCode) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
        this.errorCode = errorCode;
    }

    /**
     * Constructs a new exception with the specified detail validator.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param errorCode the detail validator. The detail validator is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     */
    public NulsRuntimeException(ErrorCode errorCode, String message) {
        super(errorCode.getMsg());
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
        if (StringUtils.isNotBlank(errorCode.getMsg())) {
            this.message = errorCode.getMsg() + ";" + message;
        } else {
            this.message = message;
        }
    }

    /**
     * Constructs a new exception with the specified detail validator and
     * cause.  Note that the detail validator associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail validator.
     *
     * @param errorCode the detail validator (which is saved for later retrieval
     *                  by the {@link #getMessage()} method).
     * @param cause     the cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method).  (A <tt>null</tt> value is
     *                  permitted, and indicates that the cause is nonexistent or
     *                  unknown.)
     * @since 1.4
     */
    public NulsRuntimeException(ErrorCode errorCode, Throwable cause) {
        super(errorCode.getMsg(), cause);
        this.code = errorCode.getCode();
        this.message = errorCode.getMsg();
        this.errorCode = errorCode;
    }


    /**
     * Constructs a new exception with the specified detail validator and
     * cause.  Note that the detail validator associated with
     * {@code cause} is <i>not</i> automatically incorporated in
     * this exception's detail validator.
     *
     * @param errorCode the detail validator (which is saved for later retrieval
     *                  by the {@link #getMessage()} method).
     * @param message   the detail message. The detail message is saved for
     *                  later retrieval by the {@link #getMessage()} method.
     * @param cause     the cause (which is saved for later retrieval by the
     *                  {@link #getCause()} method).  (A <tt>null</tt> value is
     *                  permitted, and indicates that the cause is nonexistent or
     *                  unknown.)
     * @since 1.4
     */
    public NulsRuntimeException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode.getMsg(), cause);
        this.code = errorCode.getCode();
        this.errorCode = errorCode;
        if (StringUtils.isNotBlank(errorCode.getMsg())) {
            this.message = errorCode.getMsg() + ";" + message;
        } else {
            this.message = message;
        }
    }

    /**
     * Constructs a new exception with the specified cause and a detail
     * validator of <tt>(cause==null ? null : cause.toString())</tt> (which
     * typically contains the class and detail validator of <tt>cause</tt>).
     * This constructor is useful for exceptions that are little more than
     * wrappers for other throwables (for example, {@link
     * java.security.PrivilegedActionException}).
     *
     * @param cause the cause (which is saved for later retrieval by the
     *              {@link #getCause()} method).  (A <tt>null</tt> value is
     *              permitted, and indicates that the cause is nonexistent or
     *              unknown.)
     * @since 1.4
     */
    public NulsRuntimeException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a new exception with the specified detail validator,
     * cause, suppression enabled or disabled, and writable stack
     * trace enabled or disabled.
     *
     * @param message            the detail validator.
     * @param cause              the cause.  (A {@code null} value is permitted,
     *                           and indicates that the cause is nonexistent or unknown.)
     * @param enableSuppression  whether or not suppression is enabled
     *                           or disabled
     * @param writableStackTrace whether or not the stack trace should
     *                           be writable
     * @since 1.7
     */
    protected NulsRuntimeException(ErrorCode message, Throwable cause,
                                   boolean enableSuppression,
                                   boolean writableStackTrace) {
        super(message.getMsg(), cause, enableSuppression, writableStackTrace);
        this.code = message.getCode();
        this.message = message.getMsg();
        this.errorCode = message;
    }

//    public NulsRuntimeException(ErrorCode errorCode, String msg) {
//        super(msg);
//        this.code = errorCode.getCode();
//        this.message = errorCode.getMsg() + ":" + msg;
//        this.errorCode = errorCode;
//    }

    @Override
    public String getMessage() {
        if (StringUtils.isBlank(message)) {
            return super.getMessage();
        }
        return message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public String format() {
        return MessageFormat.format("NulsRuntimeException -code: [{0}], -msg: {1}", this.code, this.message);
    }
}
