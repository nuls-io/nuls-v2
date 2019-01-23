/**
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
package io.nuls.db.constant;

/**
 * @author qinyifeng
 */
public interface DBErrorCode {
    String NULL_PARAMETER = "Parameter can not be null";
    String DB_UNKOWN_EXCEPTION = "DB error";
    String DB_TABLE_EXIST = "DB table exists";
    String DB_TABLE_NOT_EXIST = "DB table not exists";
    String DB_TABLE_CREATE_ERROR = "Create DB table error";
    String DB_TABLE_CREATE_PATH_ERROR = "Create DB table path error";
    String DB_TABLE_DESTROY_ERROR = "Destroy DB table error";
    String DB_TABLE_FAILED_BATCH_CLOSE = "DB batch operation closed";
}