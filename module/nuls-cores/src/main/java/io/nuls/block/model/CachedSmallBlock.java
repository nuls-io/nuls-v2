/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.block.model;

import io.nuls.base.data.NulsHash;
import io.nuls.base.data.SmallBlock;
import io.nuls.base.data.Transaction;

import java.util.List;
import java.util.Map;

/**
 * 缓存的区块对象,用于区块广播、转发
 *
 * @author captain
 * @version 1.0
 * @date 18-12-13 下午3:01
 */
public class CachedSmallBlock {

    /**
     * 缺失的交易
     */
    private List<NulsHash> missingTransactions;

    private SmallBlock smallBlock;

    /**
     * 已有的交易集合
     */
    private Map<NulsHash, Transaction> txMap;

    /**
     * 来自哪个节点
     */
    private String nodeId;

    public CachedSmallBlock(List<NulsHash> missingTransactions, SmallBlock smallBlock, Map<NulsHash, Transaction> txMap, String nodeId) {
        this.missingTransactions = missingTransactions;
        this.smallBlock = smallBlock;
        this.txMap = txMap;
        this.nodeId = nodeId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public List<NulsHash> getMissingTransactions() {
        return missingTransactions;
    }

    public void setMissingTransactions(List<NulsHash> missingTransactions) {
        this.missingTransactions = missingTransactions;
    }

    public SmallBlock getSmallBlock() {
        return smallBlock;
    }

    public void setSmallBlock(SmallBlock smallBlock) {
        this.smallBlock = smallBlock;
    }

    public Map<NulsHash, Transaction> getTxMap() {
        return txMap;
    }

    public void setTxMap(Map<NulsHash, Transaction> txMap) {
        this.txMap = txMap;
    }
}
