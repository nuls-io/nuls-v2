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

import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.util.MapUtil;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: PierreLuo
 * @date: 2019-03-22
 */
public class ContractTxCreateUnconfirmedManager {
    /**
     * key: accountAddress
     * value(Map):
     * key: contractAddress
     * value(Map):
     * key: txHash / contractAddress / time/ success(optional)
     * value: txHash-V / contractAddress-V / time-V/ success-V(true,false)
     */
    private Map<String, Map<String, Map<String, String>>> localUnconfirmedCreateContractTransaction;
    private ReentrantLock lock;
    private int chainId;

    public static ContractTxCreateUnconfirmedManager newInstance(int chainId) {
        ContractTxCreateUnconfirmedManager manager = new ContractTxCreateUnconfirmedManager();
        manager.localUnconfirmedCreateContractTransaction = MapUtil.createLinkedHashMap(4);
        manager.lock = new ReentrantLock();
        manager.chainId = chainId;
        return manager;
    }

    private ContractTxCreateUnconfirmedManager() {
    }

    public void saveLocalUnconfirmedCreateContractTransaction(String sender, Map<String, String> resultMap, long time) {
        lock.lock();
        try {
            LinkedHashMap<String, String> map = MapUtil.createLinkedHashMap(3);
            map.putAll(resultMap);
            map.put("time", String.valueOf(time));
            String contractAddress = map.get("contractAddress");
            Map<String, Map<String, String>> unconfirmedOfAccountMap = localUnconfirmedCreateContractTransaction.get(sender);
            if (unconfirmedOfAccountMap == null) {
                unconfirmedOfAccountMap = MapUtil.createLinkedHashMap(4);
                unconfirmedOfAccountMap.put(contractAddress, map);
                localUnconfirmedCreateContractTransaction.put(sender, unconfirmedOfAccountMap);
            } else {
                unconfirmedOfAccountMap.put(contractAddress, map);
            }
        } finally {
            lock.unlock();
        }
    }

    public LinkedList<Map<String, String>> getLocalUnconfirmedCreateContractTransaction(String sender) {
        Map<String, Map<String, String>> unconfirmedOfAccountMap = localUnconfirmedCreateContractTransaction.get(sender);
        if (unconfirmedOfAccountMap == null) {
            return null;
        }
        return new LinkedList<>(unconfirmedOfAccountMap.values());
    }

    public void removeLocalUnconfirmedCreateContractTransaction(String sender, String contractAddress, ContractResult contractResult) {
        lock.lock();
        try {
            Map<String, Map<String, String>> unconfirmedOfAccountMap = localUnconfirmedCreateContractTransaction.get(sender);
            if (unconfirmedOfAccountMap == null) {
                return;
            }
            // 合约创建成功，删除未确认交易
            if (contractResult.isSuccess()) {
                unconfirmedOfAccountMap.remove(contractAddress);
            } else {
                // 合约执行失败，保留未确认交易，并标注错误信息
                Map<String, String> dataMap = unconfirmedOfAccountMap.get(contractAddress);
                if (dataMap != null) {
                    dataMap.put("success", "false");
                    dataMap.put("msg", contractResult.getErrorMessage());
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public void removeLocalFailedUnconfirmedCreateContractTransaction(String sender, String contractAddress) {
        lock.lock();
        try {
            Map<String, Map<String, String>> unconfirmedOfAccountMap = localUnconfirmedCreateContractTransaction.get(sender);
            if (unconfirmedOfAccountMap == null) {
                return;
            }
            Map<String, String> dataMap = unconfirmedOfAccountMap.get(contractAddress);
            if (dataMap != null) {
                String success = dataMap.get("success");
                if ("false".equals(success)) {
                    unconfirmedOfAccountMap.remove(contractAddress);
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
