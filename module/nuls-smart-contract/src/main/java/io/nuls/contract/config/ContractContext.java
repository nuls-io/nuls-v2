/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.contract.config;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.nuls.base.basic.AddressTool;
import io.nuls.contract.enums.TokenTypeStatus;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.core.basic.Result;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author: PierreLuo
 * @date: 2019-03-15
 */
public class ContractContext {

    public static Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;

    /**
     * 数据库存储地址
     * database path
     */
    public static String DATA_PATH;

    /**
     * 主网链ID（卫星链ID）
     */
    public static int MAIN_CHAIN_ID = 1;

    /**
     * 主网链资产ID（卫星链资产ID，NULS资产）
     */
    public static int MAIN_ASSETS_ID = 1;
    /**
     * 当前链ID
     */
    public static int CHAIN_ID = 1;
    /**
     * 当前链资产ID
     */
    public static int ASSET_ID = 1;

    /**
     * 代币跨链系统合约地址
     */
    public static byte[] CROSS_CHAIN_SYSTEM_CONTRACT;

    public static short UPDATE_VERSION_V240 = 4;

    public static short UPDATE_VERSION_V250 = 5;

    public static short UPDATE_VERSION_CONTRACT_ASSET = 8;

    public static short UPDATE_VERSION_CONTRACT_BALANCE = 9;

    public static short UPDATE_VERSION_ACCOUNT_TRANSFER_ON_CONTRACT_CALL = 13;
    public static short PROTOCOL_14 = 14;
    public static short PROTOCOL_15 = 15;
    public static short PROTOCOL_16 = 16;
    private static final LoadingCache<String, ContractAddressInfoPo> CONTRACT_INFO_CACHE;
    private static ContractHelper contractHelper;

    static {
        CONTRACT_INFO_CACHE = CacheBuilder.newBuilder()
                .initialCapacity(200)
                .maximumSize(200)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .build(new CacheLoader<String, ContractAddressInfoPo>() {
                    @Override
                    public ContractAddressInfoPo load(String contract) {
                        Result<ContractAddressInfoPo> contractAddressInfoResult = contractHelper.getContractAddressInfo(CHAIN_ID, AddressTool.getAddress(contract));
                        ContractAddressInfoPo po = contractAddressInfoResult.getData();
                        return po;
                    }
                });
    }

    public static void loadContractAddressInfo(ContractAddressInfoPo info) {
        if (info != null) {
            CONTRACT_INFO_CACHE.put(AddressTool.getStringAddressByBytes(info.getContractAddress()), info);
        }
    }

    public static int getTokenType(String contract) {
        ContractAddressInfoPo contractAddressInfo = getContractAddressInfo(contract);
        if (contractAddressInfo == null) {
            return TokenTypeStatus.NOT_TOKEN.status();
        }
        return contractAddressInfo.getTokenType();
    }

    public static ContractAddressInfoPo getContractAddressInfo(String contract) {
        try {
            return CONTRACT_INFO_CACHE.get(contract);
        } catch (ExecutionException e) {
            return null;
        }
    }

    public static void setContractHelper(ContractHelper contractHelper) {
        if (contractHelper == null) {
            return;
        }
        ContractContext.contractHelper = contractHelper;
    }

}
