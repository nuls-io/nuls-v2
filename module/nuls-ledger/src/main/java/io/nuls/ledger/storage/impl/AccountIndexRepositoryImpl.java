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

import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.ledger.storage.AccountIndexRepository;
import io.nuls.ledger.storage.DataBaseArea;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ByteUtils;

import java.util.List;
import java.util.Map;

import static io.nuls.ledger.utils.LoggerUtil.logger;

/**
 * @author lanjinsheng
 * @date 2018/11/19
 */
@Service
public class AccountIndexRepositoryImpl implements AccountIndexRepository, InitializingBean {


    String getLedgerAssetIndexTableName(int chainId) {
        return  DataBaseArea.TB_LEDGER_ASSET_INDEX+ "_" +chainId;
    }

    String getLedgerAddressIndexTableName(int addressChainId,int assetChainId,int assetId) {
        return  DataBaseArea.TB_LEDGER_ASSET_ADDR_INDEX+ "_" +addressChainId+ "_" +assetChainId+ "_" +assetId;
    }
    /**
     * 初始化数据库
     */
    public String initAssetsIndexDb(int addressChainId) {
        String table = getLedgerAssetIndexTableName(addressChainId);
        try {
            if (!RocksDBService.existTable(table)) {
                RocksDBService.createTable(table);
            }
        } catch (Exception e) {
            logger(addressChainId).error(e);
        }
        return table;

    }
    public String initLedgerAddressIndexDb(int addressChainId,int assetChainId,int assetId) {
        String table = getLedgerAddressIndexTableName(addressChainId,assetChainId,assetId);
        try {
            if (!RocksDBService.existTable(table)) {
                RocksDBService.createTable(table);
            }
        } catch (Exception e) {
            logger(addressChainId).error(e);
        }
        return table;
    }
    @Override
    public void updateAssetsIndex(int addressChainId, byte[] key, byte[] value) {
       String table = initAssetsIndexDb(addressChainId);
        try {
            RocksDBService.put(table,key,value);
        } catch (Exception e) {
            logger(addressChainId).error(e);
        }
    }

      @Override
    public void updateAssetsAddressIndex(int addressChainId,int assetChainId,int assetId, byte[] addressKey, byte[] value) {
        String table = initLedgerAddressIndexDb(addressChainId,assetChainId,assetId);
        try {
            RocksDBService.put(table,addressKey,value);
        } catch (Exception e) {
            logger(addressChainId).error(e);
        }
    }
    @Override
    public void updateAssetsAddressIndex(int addressChainId,int assetChainId,int assetId, Map<byte[], byte[]> kvs) {
        String table = initLedgerAddressIndexDb(addressChainId,assetChainId,assetId);
        try {
            RocksDBService.batchPut(table,kvs);
        } catch (Exception e) {
            logger(addressChainId).error(e);
        }
    }

    @Override
    public List<String> assetsKeyList(int addressChainId) {
        String table = initAssetsIndexDb(addressChainId);
        List<byte[]>  assetKeys =  RocksDBService.keyList(table);
        return ByteUtils.bytesToStrings(assetKeys);
    }

    @Override
    public List<String> assetsAddressKeyList(int addressChainId, int assetChainId, int assetId) {
        String table = initLedgerAddressIndexDb(addressChainId,assetChainId,assetId);
        List<byte[]>  assetKeys =  RocksDBService.keyList(table);
        return ByteUtils.bytesToStrings(assetKeys);
    }

    @Override
    public void afterPropertiesSet() throws NulsException {

    }
}
