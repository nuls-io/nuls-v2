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
package io.nuls.contract.model.bo;

import io.nuls.contract.model.tx.ContractTransferTransaction;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.Log;

import java.util.*;

/**
 * @author: PierreLuo
 * @date: 2018/11/21
 */
public class CallableResult {

    private String contract;
    private List<ContractResult> resultList;
    private List<ContractResult> reCallList;
    private Map<String, Set<ContractResult>> failedMap;

    private List<ContractTransferTransaction> transferTransactions;

    public static CallableResult newInstance() {
        return new CallableResult();
    }

    public CallableResult() {
        this.resultList = new ArrayList<>();
        this.reCallList = new ArrayList<>();
        this.failedMap = new HashMap<>();
        this.transferTransactions = new ArrayList<>();
    }

    public void putFailed(int chainId, ContractResult contractResult) {
        ContractUtil.putAll(chainId, failedMap, contractResult);
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public List<ContractResult> getResultList() {
        return resultList;
    }

    public void setResultList(List<ContractResult> resultList) {
        this.resultList = resultList;
    }

    public List<ContractResult> getReCallList() {
        return reCallList;
    }

    public void setReCallList(List<ContractResult> reCallList) {
        this.reCallList = reCallList;
    }

    public Map<String, Set<ContractResult>> getFailedMap() {
        return failedMap;
    }

    public void setFailedMap(Map<String, Set<ContractResult>> failedMap) {
        this.failedMap = failedMap;
    }

    public List<ContractTransferTransaction> getTransferTransactions() {
        return transferTransactions;
    }

    public void setTransferTransactions(List<ContractTransferTransaction> transferTransactions) {
        this.transferTransactions = transferTransactions;
    }
}
