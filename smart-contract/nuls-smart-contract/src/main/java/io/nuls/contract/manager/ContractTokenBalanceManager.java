/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.contract.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractTokenInfo;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2019-03-08
 */
public class ContractTokenBalanceManager {

    private ContractHelper contractHelper;

    private ContractAddressStorageService contractAddressStorageService;

    private int chainId;

    private Lock tokenLock = new ReentrantLock();

    public static ContractTokenBalanceManager newInstance(int chainId) {
        ContractTokenBalanceManager manager = new ContractTokenBalanceManager();
        manager.chainId = chainId;
        manager.contractHelper = SpringLiteContext.getBean(ContractHelper.class);
        manager.contractAddressStorageService = SpringLiteContext.getBean(ContractAddressStorageService.class);
        return manager;
    }

    private ContractTokenBalanceManager() {}

    /**
     * key: String - local account address
     *      value:
     *          key: String - contract address
     *          value: ContractTokenInfo - token name && amount
     */
    private Map<String, Map<String, ContractTokenInfo>> contractTokenOfLocalAccount = new ConcurrentHashMap<>();



    public void initAllTokensByAccount(String account) {
        if(!AddressTool.validAddress(chainId, account)) {
            return;
        }
        Result<List<ContractAddressInfoPo>> allContractInfoListResult = contractAddressStorageService.getAllNrc20ContractInfoList(chainId);
        if(allContractInfoListResult.isFailed()) {
            return;
        }
        List<ContractAddressInfoPo> contractAddressInfoPoList = allContractInfoListResult.getData();
        for(ContractAddressInfoPo po : contractAddressInfoPoList) {
            initialContractToken(account, AddressTool.getStringAddressByBytes(po.getContractAddress()));
        }
    }

    public void initialContractToken(String account, String contract) {
        tokenLock.lock();
        try {
            Result<ContractTokenInfo> result = contractHelper.getContractToken(chainId, account, contract);
            if(result.isFailed()) {
                return;
            }
            ContractTokenInfo tokenInfo = result.getData();
            BigInteger amount = tokenInfo.getAmount();
            if(amount == null || amount.equals(BigInteger.ZERO)) {
                return;
            }
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            if(tokens == null) {
                tokens = new HashMap<>();
            }
            tokens.put(contract, tokenInfo);
            contractTokenOfLocalAccount.put(account, tokens);
        } finally {
            tokenLock.unlock();
        }
    }

    public void refreshContractToken(String account, String contract, ContractAddressInfoPo po, BigInteger value) {
        tokenLock.lock();
        try {
            ContractTokenInfo tokenInfo = new ContractTokenInfo(contract, po.getNrc20TokenName(), po.getDecimals(), value, po.getNrc20TokenSymbol(), po.getBlockHeight());
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            if(tokens == null) {
                tokens = new HashMap<>();
            }
            tokens.put(contract, tokenInfo);
            contractTokenOfLocalAccount.put(account, tokens);
        } finally {
            tokenLock.unlock();
        }
    }

    public Result<List<ContractTokenInfo>> getAllTokensByAccount(String account) {
        Map<String, ContractTokenInfo> tokensMap = contractTokenOfLocalAccount.get(account);
        if(tokensMap == null || tokensMap.size() == 0) {
            return getSuccess().setData(new ArrayList<>());
        }
        List<ContractTokenInfo> resultList = new ArrayList<>();
        Set<Map.Entry<String, ContractTokenInfo>> entries = tokensMap.entrySet();
        String contractAddress;
        ContractTokenInfo info;
        for(Map.Entry<String, ContractTokenInfo> entry : entries) {
            contractAddress = entry.getKey();
            info = entry.getValue();
            info.setContractAddress(contractAddress);
            resultList.add(info);
        }
        return getSuccess().setData(resultList);
    }


    public Result subtractContractToken(String account, String contract, BigInteger token) {
        tokenLock.lock();
        try {
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            if(tokens == null) {
                return getSuccess();
            } else {
                ContractTokenInfo info = tokens.get(contract);
                if(info == null) {
                    return getSuccess();
                }
                BigInteger currentToken = info.getAmount();
                if(currentToken == null) {
                    return getSuccess();
                } else {
                    if(currentToken.compareTo(token) < 0) {
                        return Result.getFailed(ContractErrorCode.INSUFFICIENT_BALANCE);
                    }
                    currentToken = currentToken.subtract(token);
                    tokens.put(contract, info.setAmount(currentToken));
                }
            }
            return getSuccess();
        } finally {
            tokenLock.unlock();
        }
    }

    public Result addContractToken(String account, String contract, BigInteger token) {
        tokenLock.lock();
        try {
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            do {
                if(tokens == null) {
                    break;
                } else {
                    ContractTokenInfo info = tokens.get(contract);
                    if(info == null) {
                        return getSuccess();
                    }
                    BigInteger currentToken = info.getAmount();
                    if(currentToken == null) {
                        break;
                    } else {
                        currentToken = currentToken.add(token);
                        tokens.put(contract, info.setAmount(currentToken));
                    }
                }
            } while(false);
        } finally {
            tokenLock.unlock();
        }
        return getSuccess();
    }
}
