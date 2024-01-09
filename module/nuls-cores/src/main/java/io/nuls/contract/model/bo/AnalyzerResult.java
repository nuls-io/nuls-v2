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
package io.nuls.contract.model.bo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2018/11/19
 */
public class AnalyzerResult {

    private List<ContractResult> reCallTxList;

    private List<ContractResult> successList;

    private Set<ContractResult> failedSet;

    public static AnalyzerResult newInstance() {
        return new AnalyzerResult();
    }

    public AnalyzerResult() {
        this.reCallTxList = new ArrayList<>();
        this.successList = new ArrayList<>();
        this.failedSet = new HashSet<>();
    }

    public List<ContractResult> getReCallTxList() {
        return reCallTxList;
    }

    public void setReCallTxList(List<ContractResult> reCallTxList) {
        this.reCallTxList = reCallTxList;
    }

    public List<ContractResult> getSuccessList() {
        return successList;
    }

    public void setSuccessList(List<ContractResult> successList) {
        this.successList = successList;
    }

    public Set<ContractResult> getFailedSet() {
        return failedSet;
    }

    public void setFailedSet(Set<ContractResult> failedSet) {
        this.failedSet = failedSet;
    }
}
