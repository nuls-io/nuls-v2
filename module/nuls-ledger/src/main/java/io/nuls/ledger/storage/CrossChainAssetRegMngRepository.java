/*-
 * ⁣⁣
 * MIT License
 * ⁣⁣
 * Copyright (C) 2017 - 2018 nuls.io
 * ⁣⁣
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 * ⁣⁣
 */
package io.nuls.ledger.storage;

import io.nuls.ledger.model.po.LedgerAsset;

import java.util.List;

/**
 * 跨链资产登记
 *
 * @author: PierreLuo
 * @date: 2020-05-11
 */
public interface CrossChainAssetRegMngRepository {

    void saveCrossChainAsset(int chainId, LedgerAsset ledgerAsset) throws Exception;

    void saveCrossChainAssetList(int chainId, List<LedgerAsset> ledgerAssetList) throws Exception;

    void deleteCrossChainAsset(int chainId, int assetChainId, int assetId) throws Exception;

    void deleteCrossChainAssetList(int chainId, List<String> assetKeyList) throws Exception;

    void batchOperationCrossChainAssetList(int chainId, List<LedgerAsset> saveAssetList, List<String> deleteAssetKeyList) throws Exception;

    LedgerAsset getCrossChainAsset(int chainId, int assetChainId, int assetId) throws Exception;

    List<LedgerAsset> getAllCrossChainAssets(int chainId) throws Exception;

}
