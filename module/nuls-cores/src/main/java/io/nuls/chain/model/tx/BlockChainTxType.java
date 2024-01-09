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
package io.nuls.chain.model.tx;

import io.nuls.core.constant.TxType;

/**
 * @author tangyi
 */

public enum BlockChainTxType {
    /**
     * Registration Chain（Must register one asset at the same time）
     * Register chain(An asset must be registered at the same time)
     */
    REGISTER_CHAIN_AND_ASSET(TxType.REGISTER_CHAIN_AND_ASSET),

    /**
     * Destruction chain（When deleting the last asset, destroy the chain at the same time）
     * Destroy chain (Destroy chain when the last asset is deleted)
     */
    DESTROY_ASSET_AND_CHAIN(TxType.DESTROY_CHAIN_AND_ASSET),

    /**
     * Add assets on the chain
     * Add an asset to the chain
     */
    ADD_ASSET_TO_CHAIN(TxType.ADD_ASSET_TO_CHAIN),

    /**
     * Remove assets from the chain
     * Delete an asset from the chain
     */
    REMOVE_ASSET_FROM_CHAIN(TxType.REMOVE_ASSET_FROM_CHAIN);

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
