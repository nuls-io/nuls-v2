/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.chain.model.tx;

/**
 * @author tangyi
 */

public enum BlockChainTxType {
    /**
     * 注册链（必须同时注册一种资产）
     * Register chain(An asset must be registered at the same time)
     */
    REGISTER_CHAIN_AND_ASSET(10101),

    /**
     * 销毁链（删除最后一种资产的时候同时销毁链）
     * Destroy chain (Destroy chain when the last asset is deleted)
     */
    DESTROY_ASSET_AND_CHAIN(10102),

    /**
     * 在链上新增资产
     * Add an asset to the chain
     */
    ADD_ASSET_TO_CHAIN(10103),

    /**
     * 从链上删除资产
     * Delete an asset from the chain
     */
    REMOVE_ASSET_FROM_CHAIN(10104);

    private int key;

    BlockChainTxType(int key) {
        this.key = key;
    }

    public int getKey() {
        return this.key;
    }

    public static BlockChainTxType valueOf(int key) {
        for (BlockChainTxType txType : BlockChainTxType.values()) {
            if (txType.getKey() == key) {
                return txType;
            }
        }
        throw new IllegalArgumentException("Key is not exist:" + key);
    }
}
