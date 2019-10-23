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
package io.nuls.ledger.service;

import io.nuls.core.constant.ErrorCode;
import io.nuls.ledger.model.po.LedgerAsset;
import io.nuls.ledger.model.tx.txdata.TxLedgerAsset;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

/**
 * 资产登记与管理接口
 *
 * @author lanjinsheng .
 * @date 2019/10/22
 */
public interface AssetRegMngService {
    void initDBAssetsIdMap() throws Exception;

    int getAndSetAssetIdByTemp(int chainId, int assetSize);

    ErrorCode batchAssetRegValidator(TxLedgerAsset txLedgerAsset, byte[] address, BigInteger destroyAsset, int chainId);

    ErrorCode commonRegValidator(TxLedgerAsset asset);

    void registerTxAssets(int chainId, List<LedgerAsset> ledgerAssets) throws Exception;

    void rollBackTxAssets(int chainId, List<LedgerAsset> ledgerAssets) throws Exception;

    int registerContractAsset(int chainId, LedgerAsset ledgerAssets) throws Exception;

    void rollBackContractAsset(int chainId, String contractAddress) throws Exception;

    List<Map<String, Object>> getLedgerRegAssets(int chainId, int assetType) throws Exception;

    Map<String, Object> getLedgerRegAsset(int chainId, String txHash) throws Exception;

    Map<String, Object> getLedgerRegAsset(int chainId, int assetId) throws Exception;

    int getRegAssetId(int chainId);

    String getRegAssetContractAddr(int chainId, int assetId) throws Exception;

    int getRegAssetId(int chainId, String contractAddr) throws Exception;

    boolean isContractAsset(int chainId, int assetId);
}
