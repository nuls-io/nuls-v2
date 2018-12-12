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

package io.nuls.block.utils.module;

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.block.test.BlockGenerator;

import java.io.IOException;
import java.util.List;

/**
 * 调用交易管理模块的工具类
 *
 * @author captain
 * @version 1.0
 * @date 18-11-9 上午10:44
 */
public class TransactionUtil {

    /**
     * 批量保存交易
     *
     * @param chainId
     * @param transactions
     * @return
     */
    public static boolean save(int chainId, List<Transaction> transactions) {
        return true;
    }

    /**
     * 批量回滚交易
     *
     * @param chainId
     * @param transactions
     * @return
     */
    public static boolean rollback(int chainId, List<NulsDigestData> transactions) {
        return true;
    }

    /**
     * 批量获取交易
     *
     * @param chainId
     * @param hashList
     * @return
     * @throws IOException
     */
    public static List<Transaction> getTransactions(int chainId, List<NulsDigestData> hashList) throws IOException {
        return BlockGenerator.getTransactions();
    }

    /**
     * 获取单个交易
     *
     * @param chainId
     * @param hash
     * @return
     */
    public static Transaction getTransaction(int chainId, NulsDigestData hash) {
        try {
            return BlockGenerator.getTransactions().get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
