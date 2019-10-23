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
import java.util.Map;

/**
 * 账本资产注册
 *
 * @author lanjinsheng
 */
public interface AssetRegMngRepository {

    void saveLedgerAssetReg(int chainId, LedgerAsset ledgerAsset) throws Exception;

    void batchSaveLedgerAssetReg(int chainId, Map<byte[], byte[]> ledgerAssets, Map<byte[], byte[]> ledgerAssetsHashs) throws Exception;

    void batchRollBackLedgerAssetReg(int chainId, List<byte[]> txHashs) throws Exception;

    int getLedgerAssetIdByContractAddr(int chainId, byte[] address) throws Exception;

    int getLedgerAssetIdByHash(int chainId, byte[] hash) throws Exception;

    LedgerAsset getLedgerAssetByAssetId(int chainId, int assetId) throws Exception;

    void deleteLedgerAssetReg(int chainId, int assetId) throws Exception;

    void deleteLedgerAssetRegIndex(int chainId, byte[] address) throws Exception;

    List<LedgerAsset> getAllRegLedgerAssets(int chainId) throws Exception;

    int loadDatas(int chainId) throws Exception;

    void batchUpdateAccountState(int addressChainId, Map<byte[], byte[]> accountStateMap) throws Exception;

    void batchDelAccountState(int addressChainId, List<byte[]> keys) throws Exception;

    boolean isContractAsset(int chainId, int assetId);
}
