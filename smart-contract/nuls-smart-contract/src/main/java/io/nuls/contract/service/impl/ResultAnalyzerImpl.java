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
package io.nuls.contract.service.impl;

import io.nuls.contract.model.bo.AnalyzerResult;
import io.nuls.contract.model.bo.CallableResult;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.contract.service.ResultAnalyzer;
import io.nuls.tools.core.annotation.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2019-01-17
 */
@Service
public class ResultAnalyzerImpl implements ResultAnalyzer {

    @Override
    public AnalyzerResult analysis(List<CallableResult> callableResultList) {

        AnalyzerResult analyzerResult = AnalyzerResult.newInstance();

        for (CallableResult callableResult : callableResultList) {
            analyzerResult.getReCallTxList().addAll(callableResult.getReCallList());
            analyzerResult.getSuccessList().addAll(callableResult.getResultList());
            addAllFailedResults(analyzerResult.getFailedSet(), callableResult.getFailedMap());
        }
        return analyzerResult;
    }

    private void addAllFailedResults(Set<ContractResult> failedSet, Map<String, Set<ContractResult>> failedMap) {
        Collection<Set<ContractResult>> setCollection = failedMap.values();
        for (Set<ContractResult> set : setCollection) {
            failedSet.addAll(set);
        }
    }
}
