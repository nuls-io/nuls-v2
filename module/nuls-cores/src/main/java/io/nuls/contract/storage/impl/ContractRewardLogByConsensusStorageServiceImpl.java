/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.storage.impl;


import io.nuls.base.data.CoinTo;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.model.po.ContractOfflineTxHashPo;
import io.nuls.contract.model.po.ContractRewardLogByConsensusPo;
import io.nuls.contract.storage.ContractOfflineTxHashListStorageService;
import io.nuls.contract.storage.ContractRewardLogByConsensusStorageService;
import io.nuls.contract.util.ContractDBUtil;
import io.nuls.contract.util.ContractUtil;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import org.apache.commons.lang3.ArrayUtils;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static io.nuls.contract.constant.ContractDBConstant.DB_NAME_CONTRACT_OFFLINE_TX_HASH_LIST;

/**
 * @author: PierreLuo
 * @date: 2019-05-24
 */
@Component
public class ContractRewardLogByConsensusStorageServiceImpl implements ContractRewardLogByConsensusStorageService {

    private final String baseArea = DB_NAME_CONTRACT_OFFLINE_TX_HASH_LIST + "_";
    private final byte[] asset_prefix = "csr-a-".getBytes(StandardCharsets.UTF_8);// consensus reward assets
    private final byte[] asset_amount_prefix = "csr-am-".getBytes(StandardCharsets.UTF_8);// consensus reward asset amount


    private byte[] assetsKey(byte[] address) {
        byte[] key = ArrayUtils.addAll(asset_prefix, address);
        return key;
    }

    @Override
    public Set<String> getAssets(int chainId, byte[] address)  {
        byte[] key = this.assetsKey(address);
        return this.getAssetsByKey(chainId, key);
    }

    private Set<String> getAssetsByKey(int chainId, byte[] key)  {
        byte[] bytes = RocksDBService.get(baseArea + chainId, key);
        if (bytes == null) {
            return new HashSet<>();
        }
        String result = new String(bytes, StandardCharsets.UTF_8);
        String[] split = result.split(",");
        Set<String> res = new HashSet<>();
        for (String s : split) {
            res.add(s.trim());
        }
        return res;
    }

    @Override
    public Map<String, String> getAssetsMap(int chainId, byte[] address) {
        Set<String> assets = this.getAssets(chainId, address);
        Map<String, String> res = new HashMap<>();
        for (String asset : assets) {
            String[] split = asset.split("-");
            int assetChainId = Integer.parseInt(split[0]);
            int assetId = Integer.parseInt(split[1]);
            res.put(asset, this.getAssetAmount(chainId, address, assetChainId, assetId).toString());
        }
        return res;
    }

    private byte[] assetAmountKey(byte[] address, int assetChainId, int assetId) {
        byte[] key = ArrayUtils.addAll(asset_amount_prefix, address);
        key = ArrayUtils.addAll(key, ("-" + assetChainId + "-" + assetId).getBytes(StandardCharsets.UTF_8));
        return key;
    }

    private BigInteger getAssetAmount(int chainId, byte[] key) {
        byte[] bytes = RocksDBService.get(baseArea + chainId, key);
        if (bytes == null) {
            return BigInteger.ZERO;
        }
        return new BigInteger(new String(bytes, StandardCharsets.UTF_8));
    }

    @Override
    public BigInteger getAssetAmount(int chainId, byte[] address, int assetChainId, int assetId) {
        byte[] key = this.assetAmountKey(address, assetChainId, assetId);
        return this.getAssetAmount(chainId, key);
    }

    @Override
    public Result save(int chainId, List<CoinTo> tos) throws Exception {
        if (tos == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        for (CoinTo to : tos) {
            byte[] assetsKey = this.assetsKey(to.getAddress());
            Set<String> assets = this.getAssetsByKey(chainId, assetsKey);
            String assetKey = to.getAssetsChainId() + "-" + to.getAssetsId();
            if (assets.add(assetKey)) {
                StringBuilder sb = new StringBuilder();
                for (String asset : assets) {
                    sb.append(asset).append(",");
                }
                sb.deleteCharAt(sb.length() - 1);
                RocksDBService.put(baseArea + chainId, assetsKey, sb.toString().getBytes(StandardCharsets.UTF_8));
            }
            byte[] assetAmountKey = this.assetAmountKey(to.getAddress(), to.getAssetsChainId(), to.getAssetsId());
            BigInteger assetAmount = this.getAssetAmount(chainId, assetAmountKey);
            RocksDBService.put(baseArea + chainId, assetAmountKey, assetAmount.add(to.getAmount()).toString().getBytes(StandardCharsets.UTF_8));
        }
        return ContractUtil.getSuccess();
    }

    @Override
    public Result delete(int chainId, List<CoinTo> tos) throws Exception {
        if (tos == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        for (CoinTo to : tos) {
            byte[] assetAmountKey = this.assetAmountKey(to.getAddress(), to.getAssetsChainId(), to.getAssetsId());
            BigInteger assetAmount = this.getAssetAmount(chainId, assetAmountKey);
            RocksDBService.put(baseArea + chainId, assetAmountKey, assetAmount.subtract(to.getAmount()).toString().getBytes(StandardCharsets.UTF_8));
        }
        return ContractUtil.getSuccess();
    }

}
