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
import io.nuls.base.data.BlockHeader;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.model.bo.ContractTokenInfo;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.rpc.call.BlockCall;
import io.nuls.contract.storage.ContractTokenAddressStorageService;
import io.nuls.core.basic.Result;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.contract.constant.ContractErrorCode.ADDRESS_ERROR;
import static io.nuls.contract.util.ContractUtil.getSuccess;

/**
 * @author: PierreLuo
 * @date: 2019-03-08
 */
public class ContractTokenBalanceManager {

    private ContractHelper contractHelper;

    private ContractTokenAddressStorageService contractTokenAddressStorageService;

    private int chainId;

    /**
     * key: String - local account address
     * value:
     * key: String - contract address
     * value: ContractTokenInfo - token name && amount
     */
    private Map<String, Map<String, ContractTokenInfo>> contractTokenOfLocalAccount = new ConcurrentHashMap<>();

    private Lock tokenLock = new ReentrantLock();

    private Set<String> initializedAddressSet;

    public static ContractTokenBalanceManager newInstance(int chainId) {
        ContractTokenBalanceManager manager = new ContractTokenBalanceManager();
        manager.chainId = chainId;
        manager.contractHelper = SpringLiteContext.getBean(ContractHelper.class);
        manager.contractTokenAddressStorageService = SpringLiteContext.getBean(ContractTokenAddressStorageService.class);
        manager.initializedAddressSet = ConcurrentHashMap.newKeySet();
        return manager;
    }

    private ContractTokenBalanceManager() {
    }

    public Result initAllTokensByImportAccount(String account) throws NulsException {
        initializedAddressSet.remove(account);
        return this.initAllTokensByAccount(account);
    }

    private Result initAllTokensByAccount(String account) throws NulsException {
        if (!initializedAddressSet.add(account)) {
            return getSuccess();
        }
        if (!AddressTool.validAddress(chainId, account)) {
            return Result.getFailed(ADDRESS_ERROR);
        }
        Result<List<byte[]>> allNrc20ListResult = contractTokenAddressStorageService.getAllNrc20AddressList(chainId);
        if (allNrc20ListResult.isFailed()) {
            return allNrc20ListResult;
        }
        BlockHeader blockHeader = BlockCall.getLatestBlockHeader(chainId);
        List<byte[]> contractAddressInfoPoList = allNrc20ListResult.getData();
        for (byte[] address : contractAddressInfoPoList) {
            initialContractToken(account, blockHeader, AddressTool.getStringAddressByBytes(address));
        }

        return getSuccess();
    }

    public void initialContractToken(String account, BlockHeader blockHeader, String contract) {
        tokenLock.lock();
        try {
            Result<ContractTokenInfo> result = contractHelper.getContractToken(chainId, blockHeader, account, contract);
            if (result.isFailed()) {
                return;
            }
            ContractTokenInfo tokenInfo = result.getData();
            BigInteger amount = tokenInfo.getAmount();
            if (amount == null || amount.equals(BigInteger.ZERO)) {
                return;
            }
            Map<String, ContractTokenInfo> tokens = contractTokenOfLocalAccount.get(account);
            if (tokens == null) {
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
            if (tokens == null) {
                tokens = new HashMap<>();
            }
            tokens.put(contract, tokenInfo);
            contractTokenOfLocalAccount.put(account, tokens);
        } finally {
            tokenLock.unlock();
        }
    }

    public Result<List<ContractTokenInfo>> getAllTokensByAccount(String account) throws NulsException {
        Result result = this.initAllTokensByAccount(account);
        if (result.isFailed()) {
            return result;
        }
        Map<String, ContractTokenInfo> tokensMap = contractTokenOfLocalAccount.get(account);
        if (tokensMap == null || tokensMap.size() == 0) {
            return getSuccess().setData(new ArrayList<>());
        }
        List<ContractTokenInfo> resultList = new ArrayList<>();
        Set<Map.Entry<String, ContractTokenInfo>> entries = tokensMap.entrySet();
        String contractAddress;
        ContractTokenInfo info;
        for (Map.Entry<String, ContractTokenInfo> entry : entries) {
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
            if (tokens == null) {
                return getSuccess();
            } else {
                ContractTokenInfo info = tokens.get(contract);
                if (info == null) {
                    return getSuccess();
                }
                BigInteger currentToken = info.getAmount();
                if (currentToken == null) {
                    return getSuccess();
                } else {
                    if (currentToken.compareTo(token) < 0) {
                        return Result.getFailed(ContractErrorCode.INSUFFICIENT_TOKEN_BALANCE);
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
                if (tokens == null) {
                    break;
                } else {
                    ContractTokenInfo info = tokens.get(contract);
                    if (info == null) {
                        return getSuccess();
                    }
                    BigInteger currentToken = info.getAmount();
                    if (currentToken == null) {
                        break;
                    } else {
                        currentToken = currentToken.add(token);
                        tokens.put(contract, info.setAmount(currentToken));
                    }
                }
            } while (false);
        } finally {
            tokenLock.unlock();
        }
        return getSuccess();
    }

}
