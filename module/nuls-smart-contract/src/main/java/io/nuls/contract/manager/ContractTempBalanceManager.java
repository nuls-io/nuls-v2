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

package io.nuls.contract.manager;


import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Address;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractBalance;
import io.nuls.core.basic.Result;
import io.nuls.core.core.ioc.SpringLiteContext;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.contract.util.ContractUtil.asString;
import static io.nuls.contract.util.ContractUtil.getSuccess;


/**
 * @author: PierreLuo
 * @date: 2018/6/7
 */
public class ContractTempBalanceManager {

    private int chainId;

    private Map<String, ContractBalance> tempBalanceMap;

    private Lock lock = new ReentrantLock();

    private ContractHelper contractHelper;

    public static ContractTempBalanceManager newInstance(int chainId) {
        ContractTempBalanceManager temp = new ContractTempBalanceManager();
        temp.chainId = chainId;
        temp.tempBalanceMap = new HashMap<>();
        temp.contractHelper = SpringLiteContext.getBean(ContractHelper.class);
        return temp;
    }

    private ContractTempBalanceManager() {
    }

    /**
     * 获取账户可用余额
     *
     * @param address
     * @param bestHeight
     * @return
     */
    public Result<ContractBalance> getBalance(byte[] address, int assetChainId, int assetId) {
        lock.lock();
        try {
            if (address == null || address.length != Address.ADDRESS_LENGTH) {
                return Result.getFailed(ContractErrorCode.PARAMETER_ERROR);
            }

            String addressKey = balanceKey(address, assetChainId,  assetId);
            ContractBalance balance = tempBalanceMap.get(addressKey);
            // 临时余额区没有余额，则从真实余额中取值
            if (balance == null) {
                // 初始化临时余额区
                balance = contractHelper.getRealBalance(chainId, assetChainId, assetId, AddressTool.getStringAddressByBytes(address));
                tempBalanceMap.put(addressKey, balance);
            }
            return getSuccess().setData(balance);
        } finally {
            lock.unlock();
        }
    }

    private String balanceKey(byte[] address, int assetChainId, int assetId) {
        return new StringBuilder(chainId).append(asString(address)).append(ContractConstant.LINE).append(assetChainId).append(ContractConstant.LINE).append(assetId).toString();
    }

    public void addTempBalance(byte[] address, BigInteger amount, int assetChainId, int assetId) {
        lock.lock();
        try {
            ContractBalance contractBalance = tempBalanceMap.get(balanceKey(address, assetChainId,  assetId));
            if (contractBalance != null) {
                contractBalance.addTemp(amount);
            }
        } finally {
            lock.unlock();
        }
    }

    public void minusTempBalance(byte[] address, BigInteger amount, int assetChainId, int assetId) {
        lock.lock();
        try {
            ContractBalance contractBalance = tempBalanceMap.get(balanceKey(address, assetChainId,  assetId));
            if (contractBalance != null) {
                contractBalance.minusTemp(amount);
            }
        } finally {
            lock.unlock();
        }
    }

    public void addLockedTempBalance(byte[] address, BigInteger amount, int assetChainId, int assetId) {
        lock.lock();
        try {
            ContractBalance contractBalance = tempBalanceMap.get(balanceKey(address, assetChainId,  assetId));
            if (contractBalance != null) {
                contractBalance.addLockedTemp(amount);
            }
        } finally {
            lock.unlock();
        }
    }

    public void minusLockedTempBalance(byte[] address, BigInteger amount, int assetChainId, int assetId) {
        lock.lock();
        try {
            ContractBalance contractBalance = tempBalanceMap.get(balanceKey(address, assetChainId,  assetId));
            if (contractBalance != null) {
                contractBalance.minusLockedTemp(amount);
            }
        } finally {
            lock.unlock();
        }
    }

}
