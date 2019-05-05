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

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * @author: PierreLuo
 * @date: 2019-03-16
 */
public class ContractContainer {
    private String contractAddress;
    private boolean hasCreate = false;
    private boolean isDelete = false;
    private CallableResult callableResult;
    private Set<String> commitSet;
    private List<Future<ContractResult>> futureList;

    public ContractContainer(String contractAddress, Set<String> commitSet, List<Future<ContractResult>> futureList) {
        this.contractAddress = contractAddress;
        this.commitSet = commitSet;
        this.futureList = futureList;
        this.callableResult = new CallableResult();
        this.callableResult.setContract(contractAddress);
    }

    public void loadFutureList() throws ExecutionException, InterruptedException {
        for (Future<ContractResult> future : futureList) {
            future.get();
        }
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public boolean isHasCreate() {
        return hasCreate;
    }

    public void setHasCreate(boolean hasCreate) {
        this.hasCreate = hasCreate;
    }

    public boolean isDelete() {
        return isDelete;
    }

    public void setDelete(boolean delete) {
        isDelete = delete;
    }

    public CallableResult getCallableResult() {
        return callableResult;
    }

    public void setCallableResult(CallableResult callableResult) {
        this.callableResult = callableResult;
    }

    public Set<String> getCommitSet() {
        return commitSet;
    }

    public void setCommitSet(Set<String> commitSet) {
        this.commitSet = commitSet;
    }

    public List<Future<ContractResult>> getFutureList() {
        return futureList;
    }

    public void setFutureList(List<Future<ContractResult>> futureList) {
        this.futureList = futureList;
    }
}
