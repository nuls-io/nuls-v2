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
package io.nuls.chain.service;

import io.nuls.chain.model.dto.ChainEventResult;
import io.nuls.chain.model.po.Asset;
import io.nuls.chain.model.po.BlockChain;

import java.math.BigInteger;
import java.util.Map;

/**
 * @author lan
 * @description
 * @date 2019/02/20
 **/
public interface ValidateService {
    ChainEventResult assetDisableValidator(Asset asset) throws Exception;

    ChainEventResult assetAddValidator(Asset asset) throws Exception;

    ChainEventResult chainAddValidator(BlockChain blockChain) throws Exception;

    ChainEventResult chainDisableValidator(BlockChain blockChain) throws Exception;


    ChainEventResult batchChainRegValidator(BlockChain blockChain, Asset asset, Map<String, Integer> tempChains, Map<String, Integer> tempAssets) throws Exception;

    ChainEventResult batchAssetRegValidator(Asset asset, Map<String, Integer> tempAssets) throws Exception;

    ChainEventResult assetCirculateValidator(int fromChainId, int toChainId, Map<String, BigInteger> fromAssetMap, Map<String, BigInteger> toAssetMap) throws Exception;
}
