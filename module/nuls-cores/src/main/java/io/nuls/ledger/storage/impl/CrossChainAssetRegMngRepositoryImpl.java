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
package io.nuls.ledger.storage.impl;

import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.BatchOperation;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.LedgerAsset;
import io.nuls.ledger.storage.CrossChainAssetRegMngRepository;
import io.nuls.ledger.storage.DataBaseArea;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2020-05-11
 */
@Component
public class CrossChainAssetRegMngRepositoryImpl implements CrossChainAssetRegMngRepository {

    String tableNamePrefix = DataBaseArea.TB_LEDGER_CROSS_CHAIN_ASSET + LedgerConstant.DOWN_LINE;

    String getTableName(int chainId) throws Exception {
        String tableName = tableNamePrefix + chainId;
        if (!RocksDBService.existTable(tableName)) {
            RocksDBService.createTable(tableName);
        }
        return tableName;
    }

    byte[] genAssetKey(int assetChainId, int assetId) {
        return ByteUtils.toBytes(assetChainId + LedgerConstant.DOWN_LINE + assetId, StandardCharsets.UTF_8.toString());
    }

    @Override
    public void saveCrossChainAsset(int chainId, LedgerAsset ledgerAsset) throws Exception {
        String assetRegTable = getTableName(chainId);
        RocksDBService.put(assetRegTable, genAssetKey(ledgerAsset.getChainId(), ledgerAsset.getAssetId()), ledgerAsset.serialize());
    }

    @Override
    public void saveCrossChainAssetList(int chainId, List<LedgerAsset> ledgerAssetList) throws Exception {
        String assetRegTable = getTableName(chainId);
        Map<byte[], byte[]> valuesMap = new HashMap<>();
        for(LedgerAsset asset : ledgerAssetList) {
            valuesMap.put(genAssetKey(asset.getChainId(), asset.getAssetId()), asset.serialize());
        }
        RocksDBService.batchPut(assetRegTable, valuesMap);
    }

    @Override
    public void deleteCrossChainAsset(int chainId, int assetChainId, int assetId) throws Exception {
        String assetRegTable = getTableName(chainId);
        RocksDBService.delete(assetRegTable, genAssetKey(assetChainId, assetId));
    }

    @Override
    public void deleteCrossChainAssetList(int chainId, List<String> assetKeyList) throws Exception {
        String assetRegTable = getTableName(chainId);
        List<byte[]> keyList = new ArrayList<>();
        for(String assetKey : assetKeyList) {
            keyList.add(ByteUtils.toBytes(assetKey, StandardCharsets.UTF_8.toString()));
        }
        RocksDBService.deleteKeys(assetRegTable, keyList);
    }

    @Override
    public void batchOperationCrossChainAssetList(int chainId, List<LedgerAsset> saveAssetList, List<String> deleteAssetKeyList) throws Exception {
        String assetRegTable = getTableName(chainId);
        BatchOperation batch = RocksDBService.createWriteBatch(assetRegTable);
        if(saveAssetList != null && !saveAssetList.isEmpty()) {
            for(LedgerAsset asset : saveAssetList) {
                batch.put(genAssetKey(asset.getChainId(), asset.getAssetId()), asset.serialize());
            }
        }
        if(deleteAssetKeyList != null && !deleteAssetKeyList.isEmpty()) {
            for(String assetKey : deleteAssetKeyList) {
                batch.delete(ByteUtils.toBytes(assetKey, StandardCharsets.UTF_8.toString()));
            }
        }
        batch.executeBatch();
    }

    @Override
    public LedgerAsset getCrossChainAsset(int chainId, int assetChainId, int assetId) throws Exception {
        String assetRegTable = getTableName(chainId);
        byte[] assetByte = RocksDBService.get(assetRegTable, genAssetKey(assetChainId, assetId));
        if (null != assetRegTable) {
            LedgerAsset ledgerAsset = new LedgerAsset();
            ledgerAsset.parse(assetByte, 0);
            return ledgerAsset;
        }
        return null;
    }

    @Override
    public List<LedgerAsset> getAllCrossChainAssets(int chainId) throws Exception {
        String assetRegTable = getTableName(chainId);
        List<byte[]> list = RocksDBService.valueList(assetRegTable);
        List<LedgerAsset> rtList = new ArrayList<>();
        if (null != list) {
            for (byte[] assetByte : list) {
                LedgerAsset ledgerAsset = new LedgerAsset();
                ledgerAsset.parse(assetByte, 0);
                rtList.add(ledgerAsset);
            }
        }
        return rtList;
    }


}
