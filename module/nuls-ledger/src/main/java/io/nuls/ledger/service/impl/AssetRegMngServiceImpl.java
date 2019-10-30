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
package io.nuls.ledger.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.ledger.config.LedgerConfig;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.constant.LedgerErrorCode;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.model.po.LedgerAsset;
import io.nuls.ledger.model.tx.txdata.TxLedgerAsset;
import io.nuls.ledger.service.AssetRegMngService;
import io.nuls.ledger.service.ChainAssetsService;
import io.nuls.ledger.storage.AssetRegMngRepository;
import io.nuls.ledger.storage.Repository;
import io.nuls.ledger.utils.LedgerUtil;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 资产登记与管理接口
 *
 * @author lanjinsheng .
 * @date 2019/10/22
 */
@Component
public class AssetRegMngServiceImpl implements AssetRegMngService {
    @Autowired
    LedgerConfig ledgerConfig;
    @Autowired
    ChainAssetsService chainAssetsService;
    /**
     * key chainID,value  assetId
     */
    Map<String, AtomicInteger> DB_ASSETS_ID_MAX_MAP = new ConcurrentHashMap<>();
    @Autowired
    AssetRegMngRepository assetRegMngRepository;
    @Autowired
    Repository repository;
    Map<String, Object> localChainDefaultAsset = new HashMap<>();

    public Map<String, Object> getLocalChainDefaultAsset() {
        if (localChainDefaultAsset.size() > 0) {
            return localChainDefaultAsset;
        }
        localChainDefaultAsset.put("assetId", ledgerConfig.getAssetId());
        localChainDefaultAsset.put("assetType", LedgerConstant.COMMON_ASSET_TYPE);
        localChainDefaultAsset.put("assetOwnerAddress", "");
        localChainDefaultAsset.put("initNumber", "");
        localChainDefaultAsset.put("decimalPlace", ledgerConfig.getDecimals());
        localChainDefaultAsset.put("assetName", ledgerConfig.getSymbol());
        localChainDefaultAsset.put("assetSymbol", ledgerConfig.getSymbol());
        localChainDefaultAsset.put("txHash", "");
        return localChainDefaultAsset;
    }

    @Override
    public void initDBAssetsIdMap() throws Exception {
        int assetId = assetRegMngRepository.loadDatas(ledgerConfig.getChainId());
        if (assetId == 0) {
            assetId = ledgerConfig.getAssetId();
        }
        DB_ASSETS_ID_MAX_MAP.put(String.valueOf(ledgerConfig.getChainId()), new AtomicInteger(assetId));
    }

    @Override
    public synchronized int getAndSetAssetIdByTemp(int chainId, int assetSize) {
        AtomicInteger assetIdAtomic = DB_ASSETS_ID_MAX_MAP.get(String.valueOf(chainId));
        int assetId = assetIdAtomic.addAndGet(assetSize);
        return assetId;
    }

    @Override
    public int getRegAssetId(int chainId) {
        AtomicInteger assetIdAtomic = DB_ASSETS_ID_MAX_MAP.get(String.valueOf(chainId));
        return assetIdAtomic.get();
    }

    @Override
    public String getRegAssetContractAddr(int chainId, int assetId) throws Exception {
        LedgerAsset ledgerAsset = assetRegMngRepository.getLedgerAssetByAssetId(chainId, assetId);
        if (null != ledgerAsset) {
            return AddressTool.getStringAddressByBytes(ledgerAsset.getAssetOwnerAddress());
        }
        return null;
    }

    @Override
    public int getRegAssetId(int chainId, String contractAddr) throws Exception {
        int assetId = assetRegMngRepository.getLedgerAssetIdByContractAddr(chainId, AddressTool.getAddress(contractAddr));
        return assetId;
    }

    @Override
    public ErrorCode batchAssetRegValidator(TxLedgerAsset txLedgerAsset, byte[] address, BigInteger destroyAsset, int chainId) {
        ErrorCode errorCode = commonRegValidator(txLedgerAsset);
        if (null != errorCode) {
            return errorCode;
        }
        //判断地址是否为本地chainId地址
        boolean isAddressValidate = (AddressTool.getChainIdByAddress(txLedgerAsset.getAddress()) == chainId);
        if (!isAddressValidate) {
            return LedgerErrorCode.ERROR_ADDRESS_ERROR;
        }
        //判断黑洞地址
        if (!Arrays.equals(address, AddressTool.getAddressByPubKeyStr(ledgerConfig.getBlackHolePublicKey(), chainId))) {
            LoggerUtil.COMMON_LOG.error("toAddress is not blackHole");
            return LedgerErrorCode.TX_IS_WRONG;
        }
        long decimal = (long) Math.pow(10, Integer.valueOf(ledgerConfig.getDecimals()));
        BigInteger destroyAssetTx = BigInteger.valueOf(ledgerConfig.getAssetRegDestroyAmount()).multiply(BigInteger.valueOf(decimal));
        if (!BigIntegerUtils.isEqual(destroyAsset, destroyAssetTx)) {
            LoggerUtil.COMMON_LOG.error("destroyNuls={} is error", destroyAsset);
            return LedgerErrorCode.TX_IS_WRONG;
        }
        return null;
    }

    @Override
    public ErrorCode commonRegValidator(TxLedgerAsset asset) {
        if (asset.getDecimalPlace() < LedgerConstant.DECIMAL_PLACES_MIN || asset.getDecimalPlace() > LedgerConstant.DECIMAL_PLACES_MAX) {
            return LedgerErrorCode.ERROR_ASSET_DECIMALPLACES;
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getSymbol())) {
            return LedgerErrorCode.ERROR_ASSET_SYMBOL;
        }
        if (!FormatValidUtils.validTokenNameOrSymbol(asset.getName())) {
            return LedgerErrorCode.ERROR_ASSET_NAME;
        }
        return null;
    }

    @Override
    public void registerTxAssets(int chainId, List<LedgerAsset> ledgerAssets) throws Exception {
        Map<byte[], byte[]> assets = new HashMap<>(ledgerAssets.size());
        Map<byte[], byte[]> hashMap = new HashMap<>(ledgerAssets.size());
        int assetId = getRegAssetId(chainId);
        LoggerUtil.COMMON_LOG.info("1=assetId={}" + assetId);
        Map<byte[], byte[]> accountStatesMap = new HashMap<>(ledgerAssets.size());
        Map<String, List<String>> assetAddressIndex = new HashMap<>(4);
        for (LedgerAsset ledgerAsset : ledgerAssets) {
            ledgerAsset.setAssetType(LedgerConstant.COMMON_ASSET_TYPE);
            assetId++;
            LoggerUtil.COMMON_LOG.info("assetId={}" + assetId);
            ledgerAsset.setAssetId(assetId);
            assets.put(ByteUtils.intToBytes(ledgerAsset.getAssetId()), ledgerAsset.serialize());
            hashMap.put(HexUtil.decode(ledgerAsset.getTxHash()), ByteUtils.intToBytes(ledgerAsset.getAssetId()));
            String address = LedgerUtil.getRealAddressStr(ledgerAsset.getAssetOwnerAddress());
            String key = LedgerUtil.getKeyStr(address, chainId, assetId);
            AccountState accountState = new AccountState();
            long decimal = (long) Math.pow(10, Integer.valueOf(ledgerAsset.getDecimalPlace()));
            BigInteger amount = ledgerAsset.getInitNumber().multiply(BigInteger.valueOf(decimal));
            accountState.setTotalToAmount(amount);
            accountStatesMap.put(key.getBytes(LedgerConstant.DEFAULT_ENCODING), accountState.serialize());
            LedgerUtil.dealAssetAddressIndex(assetAddressIndex, chainId, ledgerAsset.getAssetId(), address);
        }
        assetRegMngRepository.batchSaveLedgerAssetReg(chainId, assets, hashMap);
        getAndSetAssetIdByTemp(chainId, ledgerAssets.size());
        //资产信息入账本
        //更新链下资产种类，及资产地址集合数据。
        chainAssetsService.updateChainAssets(chainId, assetAddressIndex);
        //更新账本
        if (accountStatesMap.size() > 0) {
            assetRegMngRepository.batchUpdateAccountState(chainId, accountStatesMap);
        }
    }

    @Override
    public void rollBackTxAssets(int chainId, List<LedgerAsset> ledgerAssets) throws Exception {
        List<byte[]> list = new ArrayList<>();
        List<byte[]> delKeys = new ArrayList<>();
        Map<String, AccountState> delMap = new ConcurrentHashMap<>();
        for (LedgerAsset ledgerAsset : ledgerAssets) {
            byte[] hash = HexUtil.decode(ledgerAsset.getTxHash());
            list.add(hash);
            int assetId = assetRegMngRepository.getLedgerAssetIdByHash(chainId, hash);
            String address = LedgerUtil.getRealAddressStr(ledgerAsset.getAssetOwnerAddress());
            String key = LedgerUtil.getKeyStr(address, chainId, assetId);
            delMap.put(key, new AccountState());
            delKeys.add(key.getBytes(LedgerConstant.DEFAULT_ENCODING));
        }
        repository.clearAccountStateMem(chainId, delMap);
        assetRegMngRepository.batchRollBackLedgerAssetReg(chainId, list);
        assetRegMngRepository.batchDelAccountState(chainId, delKeys);
        //回滚资产后，进行缓存数据重置
        initDBAssetsIdMap();
    }

    @Override
    public int registerContractAsset(int chainId, LedgerAsset ledgerAsset) throws Exception {
        ledgerAsset.setAssetId(getAndSetAssetIdByTemp(chainId, 1));
        assetRegMngRepository.saveLedgerAssetReg(chainId, ledgerAsset);
        return ledgerAsset.getAssetId();
    }

    @Override
    public void rollBackContractAsset(int chainId, String contractAddress) throws Exception {
        byte[] address = AddressTool.getAddress(contractAddress);
        int assetId = assetRegMngRepository.getLedgerAssetIdByContractAddr(chainId, address);
        if (assetId > 0) {
            assetRegMngRepository.deleteLedgerAssetReg(chainId, assetId);
            assetRegMngRepository.deleteLedgerAssetRegIndex(chainId, address);
        }
    }

    Map<String, Object> getAssetMapByLedgerAsset(LedgerAsset ledgerAsset) {
        Map<String, Object> map = new HashMap<>();
        map.put("assetId", ledgerAsset.getAssetId());
        map.put("assetType", ledgerAsset.getAssetType());
        map.put("assetOwnerAddress", AddressTool.getStringAddressByBytes(ledgerAsset.getAssetOwnerAddress()));
        map.put("initNumber", ledgerAsset.getInitNumber());
        map.put("decimalPlace", ledgerAsset.getDecimalPlace());
        map.put("assetName", ledgerAsset.getAssetName());
        map.put("assetSymbol", ledgerAsset.getSymbol());
        map.put("txHash", ledgerAsset.getTxHash());
        return map;
    }

    @Override
    public List<Map<String, Object>> getLedgerRegAssets(int chainId, int assetType) throws Exception {
        List<LedgerAsset> assets = assetRegMngRepository.getAllRegLedgerAssets(chainId);
        List<Map<String, Object>> rtList = new ArrayList<>();
        Map<String, Object> defaultAsset = new HashMap<>();
        if (ledgerConfig.getChainId() == chainId) {
            defaultAsset = getLocalChainDefaultAsset();
        }
        if (LedgerConstant.COMMON_ASSET_TYPE == assetType) {
            rtList.add(defaultAsset);
            for (LedgerAsset ledgerAsset : assets) {
                if (LedgerConstant.COMMON_ASSET_TYPE == ledgerAsset.getAssetType()) {
                    rtList.add(getAssetMapByLedgerAsset(ledgerAsset));
                }
            }
        } else if (LedgerConstant.CONTRACT_ASSET_TYPE == assetType) {
            for (LedgerAsset ledgerAsset : assets) {
                if (LedgerConstant.CONTRACT_ASSET_TYPE == ledgerAsset.getAssetType()) {
                    rtList.add(getAssetMapByLedgerAsset(ledgerAsset));
                }
            }
        } else {
            rtList.add(defaultAsset);
            for (LedgerAsset ledgerAsset : assets) {
                rtList.add(getAssetMapByLedgerAsset(ledgerAsset));
            }
        }
        return rtList;
    }

    @Override
    public Map<String, Object> getLedgerRegAsset(int chainId, String txHash) throws Exception {
        byte[] hashByte = HexUtil.decode(txHash);
        int assetId = assetRegMngRepository.getLedgerAssetIdByHash(chainId, hashByte);
        if (assetId > 0) {
            LedgerAsset ledgerAsset = assetRegMngRepository.getLedgerAssetByAssetId(chainId, assetId);
            if (null != ledgerAsset) {
                return getAssetMapByLedgerAsset(ledgerAsset);
            }
        }
        return null;
    }

    @Override
    public Map<String, Object> getLedgerRegAsset(int chainId, int assetId) throws Exception {
        if (assetId == ledgerConfig.getAssetId()) {
            return getLocalChainDefaultAsset();
        }
        LedgerAsset ledgerAsset = assetRegMngRepository.getLedgerAssetByAssetId(chainId, assetId);
        if (null != ledgerAsset) {
            return getAssetMapByLedgerAsset(ledgerAsset);
        }
        return null;
    }

    @Override
    public boolean isContractAsset(int chainId, int assetId) {
        return assetRegMngRepository.isContractAsset(chainId, assetId);
    }

}


