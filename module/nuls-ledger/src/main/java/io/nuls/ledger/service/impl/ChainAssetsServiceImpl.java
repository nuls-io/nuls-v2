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

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Service;
import io.nuls.core.model.ByteUtils;
import io.nuls.ledger.constant.LedgerConstant;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.ledger.service.AccountStateService;
import io.nuls.ledger.service.ChainAssetsService;
import io.nuls.ledger.storage.AccountIndexRepository;
import io.nuls.ledger.utils.LoggerUtil;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 交易业务处理实现
 *
 * @author lanjinsheng
 */
@Service
public class ChainAssetsServiceImpl implements ChainAssetsService {

    @Autowired
    AccountIndexRepository accountIndexRepository;
    @Autowired
    AccountStateService accountStateService;

    @Override
    public void updateChainAssets(int addressChainid, Map<String, List<String>> assetAddressIndex) {
        try {
            byte[] value = ByteUtils.intToBytes(1);
            for (Map.Entry<String, List<String>> entry : assetAddressIndex.entrySet()) {
                String assetIndex = entry.getKey();
                byte[] indexBytes = assetIndex.getBytes(LedgerConstant.DEFAULT_ENCODING);
                accountIndexRepository.updateAssetsIndex(addressChainid, indexBytes, indexBytes);
                //地址存储
                List<String> assetAddress = entry.getValue();
                String[] assetChainAssetId = assetIndex.split("-");
                int assetChainId = Integer.valueOf(assetChainAssetId[0]);
                int assetId = Integer.valueOf(assetChainAssetId[1]);
                Map<byte[], byte[]> assetAddressMap = new HashMap<>();
                for (String address : assetAddress) {
                    byte[] addrBytes = address.getBytes(LedgerConstant.DEFAULT_ENCODING);
                    assetAddressMap.put(addrBytes, value);
                }
                accountIndexRepository.updateAssetsAddressIndex(addressChainid,
                        assetChainId,
                        assetId, assetAddressMap);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LoggerUtil.logger(addressChainid).error(e);
        }
    }

    @Override
    public List<Map<String, Object>> getAssetsByChainId(int addressChainId) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<String> assetKeys = accountIndexRepository.assetsKeyList(addressChainId);
        if (null != assetKeys) {
            for (String assetKey : assetKeys) {
                String[] assetChainAssetId = assetKey.split("-");
                int assetChainId = Integer.valueOf(assetChainAssetId[0]);
                int assetId = Integer.valueOf(assetChainAssetId[1]);
                Map<String, Object> asset = getAssetByChainAssetId(addressChainId, assetChainId, assetId);
                list.add(asset);
            }
        }
        return list;
    }

    @Override
    public Map<String, Object> getAssetByChainAssetId(int addressChainId, int assetChainId, int assetId) {
        List<String> addressKeys = accountIndexRepository.assetsAddressKeyList(addressChainId, assetChainId, assetId);
        Map<String, Object> asset = new HashMap<>();
        BigInteger amount = BigInteger.ZERO;
        BigInteger freeze = BigInteger.ZERO;
        if (null != addressKeys) {
            for (String addressKey : addressKeys) {
                AccountState accountState = accountStateService.getAccountStateReCal(addressKey, addressChainId, assetChainId, assetId);
                amount = amount.add(accountState.getAvailableAmount());
                freeze = freeze.add(accountState.getFreezeTotal());
            }
        }
        asset.put("assetId", assetId);
        asset.put("availableAmount", amount);
        asset.put("freeze", freeze);
        return asset;
    }
}
