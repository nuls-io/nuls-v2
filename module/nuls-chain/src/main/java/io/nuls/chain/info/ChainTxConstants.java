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
package io.nuls.chain.info;

/**
 * @author lan
 * @program nuls2
 * @description 交易类型常量,  Constant of transaction type
 * @date 2018/11/19
 */
public interface ChainTxConstants {

    /**
     * 注册链的时候必须同时注册一种资产（增加注册链成本，防止恶意使用）
     * When register a chain, must register an asset at the same time (increase the cost of the registration chain to prevent malicious use)
     */
    int TX_TYPE_REGISTER_CHAIN_AND_ASSET = 11;

    /**
     * 当销毁链上最后一种资产的时候，同时销毁链
     * When the last asset in the chain is destroyed, the chain is destroyed at the same time
     */
    int TX_TYPE_DESTROY_ASSET_AND_CHAIN = 12;

    /**
     * 为一条链增加一种资产
     * Adding an asset to a chain
     */
    int TX_TYPE_ADD_ASSET_TO_CHAIN = 13;

    /**
     * 从一条链上删除一种资产
     * Delete an asset from a chain
     */
    int TX_TYPE_REMOVE_ASSET_FROM_CHAIN = 14;
}
