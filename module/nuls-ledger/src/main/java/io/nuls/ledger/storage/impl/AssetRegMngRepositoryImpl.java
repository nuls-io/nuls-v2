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

import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.LedgerAsset;
import io.nuls.ledger.storage.AssetRegMngRepository;
import io.nuls.ledger.storage.DataBaseArea;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */
@Component
public class AssetRegMngRepositoryImpl implements AssetRegMngRepository, InitializingBean {
    /**
     * 缓存合约资产id
     */
    Map<String, Integer> DB_CONTRACT_ASSETS_IDS_MAP = new ConcurrentHashMap<>();

    String getLedgerAssetRegMngTableName(int chainId) throws Exception {
        String tableName = DataBaseArea.TB_LEDGER_ASSET_REG_MNG + LedgerConstant.DOWN_LINE + chainId;
        if (!RocksDBService.existTable(tableName)) {
            RocksDBService.createTable(tableName);
        }
        return tableName;
    }

    String getLedgerAssetRegHashIndexTableName(int chainId) throws Exception {
        String tableName = DataBaseArea.TB_LEDGER_ASSET_REG_HASH_INDEX + "_" + chainId;
        if (!RocksDBService.existTable(tableName)) {
            RocksDBService.createTable(tableName);
        }
        return tableName;
    }

    String getLedgerAssetRegContractAddrIndexTableName(int chainId) throws Exception {
        String tableName = DataBaseArea.TB_LEDGER_ASSET_REG_CONTRACT_INDEX + "_" + chainId;
        if (!RocksDBService.existTable(tableName)) {
            RocksDBService.createTable(tableName);
        }
        return tableName;
    }


    @Override
    public void afterPropertiesSet() throws NulsException {

    }

    @Override
    public void saveLedgerAssetReg(int chainId, LedgerAsset ledgerAsset) throws Exception {
        String assetRegTable = getLedgerAssetRegMngTableName(chainId);
        String assetContractAddrTable = getLedgerAssetRegContractAddrIndexTableName(chainId);
        if (LedgerConstant.CONTRACT_ASSET_TYPE == ledgerAsset.getAssetType()) {
            DB_CONTRACT_ASSETS_IDS_MAP.put(chainId + LedgerConstant.DOWN_LINE + ledgerAsset.getAssetId(), 1);
        }
        RocksDBService.put(assetContractAddrTable, ledgerAsset.getAssetOwnerAddress(), ByteUtils.intToBytes(ledgerAsset.getAssetId()));
        RocksDBService.put(assetRegTable, ByteUtils.intToBytes(ledgerAsset.getAssetId()), ledgerAsset.serialize());

    }

    @Override
    public void batchSaveLedgerAssetReg(int chainId, Map<byte[], byte[]> ledgerAssets, Map<byte[], byte[]> ledgerAssetsHashs) throws Exception {
        //update account
        String assetRegTable = getLedgerAssetRegMngTableName(chainId);
        String assetHashTable = getLedgerAssetRegHashIndexTableName(chainId);
        if (ledgerAssetsHashs.size() > 0) {
            RocksDBService.batchPut(assetHashTable, ledgerAssetsHashs);
        }
        if (ledgerAssets.size() > 0) {
            RocksDBService.batchPut(assetRegTable, ledgerAssets);
        }
    }

    @Override
    public void batchRollBackLedgerAssetReg(int chainId, List<byte[]> txHashs) throws Exception {
        String assetHashTable = getLedgerAssetRegHashIndexTableName(chainId);
        List<byte[]> assetIds = RocksDBService.multiGetAsList(assetHashTable, txHashs);
        if (null != assetIds && assetIds.size() > 0) {
            String assetRegTable = getLedgerAssetRegMngTableName(chainId);
            RocksDBService.deleteKeys(assetRegTable, assetIds);
        }
        if (txHashs.size() > 0) {
            RocksDBService.deleteKeys(assetHashTable, txHashs);
        }
    }

    @Override
    public int getLedgerAssetIdByContractAddr(int chainId, byte[] address) throws Exception {
        String assetContractAddrTable = getLedgerAssetRegContractAddrIndexTableName(chainId);
        byte[] assetIdByte = RocksDBService.get(assetContractAddrTable, address);
        if (null == assetIdByte) {
            return 0;
        }
        return ByteUtils.bytesToInt(assetIdByte);
    }

    @Override
    public int getLedgerAssetIdByHash(int chainId, byte[] hash) throws Exception {
        String hashTableName = getLedgerAssetRegHashIndexTableName(chainId);
        byte[] assetIdByte = RocksDBService.get(hashTableName, hash);
        if (null != assetIdByte) {
            return ByteUtils.bytesToInt(assetIdByte);
        }
        return 0;
    }

    @Override
    public LedgerAsset getLedgerAssetByAssetId(int chainId, int assetId) throws Exception {
        String assetRegTable = getLedgerAssetRegMngTableName(chainId);
        byte[] assetByte = RocksDBService.get(assetRegTable, ByteUtils.intToBytes(assetId));
        if (null != assetRegTable) {
            LedgerAsset ledgerAsset = new LedgerAsset();
            ledgerAsset.parse(assetByte, 0);
            return ledgerAsset;
        }
        return null;
    }

    @Override
    public void deleteLedgerAssetReg(int chainId, int assetId) throws Exception {
        String assetRegTable = getLedgerAssetRegMngTableName(chainId);
        RocksDBService.delete(assetRegTable, ByteUtils.intToBytes(assetId));
        DB_CONTRACT_ASSETS_IDS_MAP.remove(chainId + LedgerConstant.DOWN_LINE + assetId);
    }

    @Override
    public void deleteLedgerAssetRegIndex(int chainId, byte[] address) throws Exception {
        String assetContractAddrTable = getLedgerAssetRegContractAddrIndexTableName(chainId);
        RocksDBService.delete(assetContractAddrTable, address);
    }

    @Override
    public List<LedgerAsset> getAllRegLedgerAssets(int chainId) throws Exception {
        String assetRegTable = getLedgerAssetRegMngTableName(chainId);
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

    @Override
    public int loadDatas(int chainId) throws Exception {
        String assetHashTable = getLedgerAssetRegHashIndexTableName(chainId);
        int assetId = 0;
        List<byte[]> list1 = RocksDBService.valueList(assetHashTable);
        if (list1 != null) {
            for (byte[] value : list1) {
                int hashAssetId = ByteUtils.bytesToInt(value);
                if (hashAssetId > assetId) {
                    assetId = hashAssetId;
                }
            }
        }
        String assetContractTable = getLedgerAssetRegContractAddrIndexTableName(chainId);
        List<byte[]> list2 = RocksDBService.valueList(assetContractTable);
        if (list2 != null) {
            for (byte[] value : list2) {
                int addressAssetId = ByteUtils.bytesToInt(value);
                DB_CONTRACT_ASSETS_IDS_MAP.put(chainId + LedgerConstant.DOWN_LINE + addressAssetId, 1);
                if (addressAssetId > assetId) {
                    assetId = addressAssetId;
                }
            }
        }
        return 0;
    }

    @Override
    public void batchUpdateAccountState(int addressChainId, Map<byte[], byte[]> accountStateMap) throws Exception {
        //update account
        RocksDBService.batchPut(getLedgerAccountTableName(addressChainId), accountStateMap);

    }

    @Override
    public void batchDelAccountState(int addressChainId, List<byte[]> keys) throws Exception {
        //update account
        RocksDBService.deleteKeys(getLedgerAccountTableName(addressChainId), keys);

    }

    @Override
    public boolean isContractAsset(int chainId, int assetId) {
        return (null != DB_CONTRACT_ASSETS_IDS_MAP.get(chainId + LedgerConstant.DOWN_LINE + assetId));
    }

    String getLedgerAccountTableName(int chainId) {
        return DataBaseArea.TB_LEDGER_ACCOUNT + LedgerConstant.DOWN_LINE + chainId;
    }
}
