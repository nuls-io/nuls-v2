/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.transaction.constant;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2019-12-04
 */
public class TxContext {
    public static short UPDATE_VERSION_V250 = 5;

    public static short UPDATE_VERSION_CONTRACT_ASSET = 8;

    public static short UPDATE_VERSION_ACCOUNT_BLOCK = 11;

    public static short UPDATE_VERSION_ACCOUNT_BLOCK_UPGRADE = 12;

    public static Set<String> ACCOUNT_BLOCK_MANAGER_ADDRESS_SET = new HashSet<>();
    // 最小签名数
    public static int ACCOUNT_BLOCK_MIN_SIGN_COUNT = 0;
}
